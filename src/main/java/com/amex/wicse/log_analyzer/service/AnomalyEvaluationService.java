package com.amex.wicse.log_analyzer.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AnomalyEvaluationService {

    @Value("${spring.ai.anthropic.api-key}")
    private String anthropicKey;
    private final RestTemplate restTemplate = new RestTemplate();
    private static final int MAX_RETRIES = 4;
    private static final long INITIAL_BACKOFF_MS = 1000;
    private final AnomalyExplanationService anomalyExplanationService;

    public AnomalyEvaluationService(AnomalyExplanationService anomalyExplanationService) {
        this.anomalyExplanationService = anomalyExplanationService;
    }

    public String buildJudgePrompt(String anomaly, String explanation) {

        String prompt = anomalyExplanationService.buildPrompt(anomaly);
        return """
                You are an expert SRE evaluating an AI-generated explanation.
                Your task is to evaluate whether the AI-generated explanation correctly answers the given prompt.           
                ------------------------
                Original Prompt:
                %s
                ------------------------  
                LLM Explanation:
                %s
                ------------------------                
                Evaluate on the following metrics (score between 0 and 1):         
                1. Correctness (Does it correctly explain the issue?)
                2. Completeness (Does it explain the root cause and provide mitigation?)
                3. Clarity (Is it understandable?)
                4. Answer Relevance (Does the generated response directly address the prompt?)
                                
                Also provide:
                - Short reasoning (2-3 lines)
                
                Output STRICTLY in JSON:
                {
                  "correctness": 0.X,
                  "completeness": 0.X,
                  "clarity": 0.X,
                  "answer_relevance": 0.X,
                  "reason": "text"
                }
                 """.formatted(prompt, explanation) ;
    }

    public String callAnthropicAI(String prompt) {

        String url = "https://api.anthropic.com/v1/messages";

        Map<String, Object> request = new HashMap<>();
        request.put("model", "claude-sonnet-4-5");
        request.put("max_tokens", 600);

        Map<String, String> msg = new HashMap<>();
        msg.put("role", "user");
        msg.put("content", prompt);

        request.put("messages", List.of(msg));

        HttpHeaders headers = new HttpHeaders();
        headers.set("x-api-key", anthropicKey);
        headers.set("anthropic-version", "2023-06-01");
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);

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

    public Map<String, Object> evaluateWithAnthropicModel(String anomaly, String explanation) {

        String prompt = buildJudgePrompt(anomaly, explanation);
        System.out.println(prompt);
        String claudeAiResponse = callAnthropicAI(prompt);
        Map<String, Object> result = new HashMap<>();
        result.put("claude_eval", claudeAiResponse);
        //saveExplanation(anomalyId, "anthropic", claudeAiResponse);
        return result;
    }
}
