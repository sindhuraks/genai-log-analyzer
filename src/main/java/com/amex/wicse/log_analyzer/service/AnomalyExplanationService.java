package com.amex.wicse.log_analyzer.service;

import com.amex.wicse.log_analyzer.model.ApacheLogAnomaly;
import com.amex.wicse.log_analyzer.model.Explanations;
import com.amex.wicse.log_analyzer.repo.ExplanationsRepo;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpStatusCodeException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AnomalyExplanationService {

    @Value("${spring.ai.anthropic.api-key}")
    private String anthropicKey;
    @Value("${spring.ai.ollama.base-url}")
    private String ollamaBaseurl;
    @Value("${spring.ai.openai-sdk.api-key}")
    private String openAIKey;
    private final RestTemplate restTemplate = new RestTemplate();
    private static final int MAX_RETRIES = 4;
    private static final long INITIAL_BACKOFF_MS = 1000;
    private final ExplanationsRepo explanationsRepo;

    public AnomalyExplanationService(ExplanationsRepo explanationsRepo) {
        this.explanationsRepo = explanationsRepo;
    }

    public String buildPrompt(String anomaly) {

        return """
               You are a SRE assistant.
               Analyze the following system log anomaly and explain:
               1. What the error means?
               2. Root cause:
               3. Suggested mitigation to avoid the problem:
               
               Log anomaly:\n %s
               
               After analyzing the anomaly, on a new line output the following:
               4. Confidence : <number between 0 and 1>
               5. Uncertainty : <none|low|medium|high>
                """.formatted(anomaly) ;
    }

    public String callAnthropicAI(String prompt) {

        String url = "https://api.anthropic.com/v1/messages";

        Map<String, Object> request = new HashMap<>();
        request.put("model", "claude-3-haiku-20240307");
        request.put("max_tokens", 500);

        Map<String, String> msg = new HashMap<>();
        msg.put("role", "user");
        msg.put("content", prompt);

        request.put("messages", List.of(msg));

        HttpHeaders headers = new HttpHeaders();
        headers.set("x-api-key", anthropicKey);
        headers.set("anthropic-version", "2023-06-01");
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);

//        Map response = restTemplate.postForObject(url, entity, Map.class);
//
//        List content = (List) response.get("content");
//        Map first = (Map) content.get(0);
//
//        return first.get("text").toString();
        int attempt = 0;
        long backoffMs = INITIAL_BACKOFF_MS;

        while (attempt < MAX_RETRIES) {
            try {
                Map response = restTemplate.postForObject(url, entity, Map.class);
                List content = (List) response.get("content");
                Map first = (Map) content.get(0);
                return first.get("text").toString();

            } catch (HttpStatusCodeException e) {
                int statusCode = e.getStatusCode().value();

                // Retry only on 529 (Overloaded) or 529-equivalent / 503 transient errors
                if ((statusCode == 529 || statusCode == 503 || statusCode == 529) && attempt < MAX_RETRIES - 1) {
                    attempt++;
                    System.out.printf("Anthropic API overloaded (attempt %d/%d). Retrying in %dms...%n",
                            attempt, MAX_RETRIES, backoffMs);
                    try {
                        Thread.sleep(backoffMs);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("Retry interrupted", ie);
                    }
                    backoffMs *= 2; // Exponential backoff
                } else {
                    throw new RuntimeException("Anthropic API error [" + statusCode + "]: " + e.getResponseBodyAsString(), e);
                }
            }
        }
        throw new RuntimeException("Anthropic API is still overloaded after " + MAX_RETRIES + " retries. Please try again later.");
    }

    public String callOllamaAI(String prompt) {

        String url = ollamaBaseurl + "/api/chat";

        Map<String, Object> request = new HashMap<>();
        request.put("model", "llama3-chatqa:8b");

        Map<String, String> msg = new HashMap<>();
        msg.put("role", "user");
        msg.put("content", prompt);

        request.put("messages", List.of(msg));
        request.put("stream", false);

        Map<String, Object> options = new HashMap<>();
        options.put("num_predict", 500);
        options.put("temperature", 0.2);
        request.put("options", options);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);

        Map response = restTemplate.postForObject(url, entity, Map.class);

        Map message = (Map) response.get("message");
        return message.get("content").toString();
    }

    public String callOpenAI(String prompt) {

        String url = "https://api.openai.com/v1/chat/completions";

        Map<String, Object> request = new HashMap<>();
        request.put("model", "gpt-4o-mini");
        request.put("max_tokens", 500);
        request.put("temperature", 0.2);

        Map<String, String> msg = new HashMap<>();
        msg.put("role", "user");
        msg.put("content", prompt);

        request.put("messages", List.of(msg));

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + openAIKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);

        Map response = restTemplate.postForObject(url, entity, Map.class);

        List choices = (List) response.get("choices");
        Map first = (Map) choices.get(0);
        Map message = (Map) first.get("message");
        return message.get("content").toString();

    }

    public Map<String, Object> explainWithAnthropicModel(String anomaly, String anomalyId) {

        String prompt = buildPrompt(anomaly);
        System.out.println(prompt);
        String claudeAiResponse = callAnthropicAI(prompt);
        Map<String, Object> result = new HashMap<>();
        result.put("claude_explanation", claudeAiResponse);
        //anomalyEvaluationService.sendToEvalApi(anomalyId, claudeAiResponse);
        saveExplanation(anomalyId, "anthropic", claudeAiResponse);
        return result;
    }

    public Map<String, Object> explainWithOllamaModel(String anomaly, String anomalyId) {

        String prompt = buildPrompt(anomaly);
        System.out.println(prompt);
        String ollamaAiResponse = callOllamaAI(prompt);
        Map<String, Object> result = new HashMap<>();
        result.put("ollama_explanation", ollamaAiResponse);
        saveExplanation(anomalyId, "ollama", ollamaAiResponse);
        return result;
    }

    public Map<String, Object> explainWithOpenAIModel(String anomaly, String anomalyId) {

        String prompt = buildPrompt(anomaly);
        System.out.println(prompt);
        String openAiResponse = callOpenAI(prompt);
        Map<String, Object> result = new HashMap<>();
        result.put("openai_explanation", openAiResponse);
        saveExplanation(anomalyId, "openai", openAiResponse);
        return result;
    }

    @Transactional
    public void saveExplanation(String anomalyId, String model, String response) {
        Explanations explanation = new Explanations();
        explanation.setAnomalyId(anomalyId);
        explanation.setModel(model);
        explanation.setExplanation(response);
        explanation.setCreatedAt(LocalDateTime.now());
        explanationsRepo.save(explanation);
    }
}
