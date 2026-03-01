package com.amex.wicse.log_analyzer.service;

import com.amex.wicse.log_analyzer.model.ApacheLogAnomaly;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AnomalyExplanationService {

    @Value("${spring.ai.anthropic.api-key}")
    private String anthropicKey;
    private final RestTemplate restTemplate = new RestTemplate();

    public String buildPrompt(String anomaly) {

        return """
               You are a SRE assistant.
               Analyze the following system log anomaly and explain:
               1. What the error means?
               2. Root cause
               3. Suggested mitigation to avoid the problem
               
               Log anomaly:\n %s
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

        Map response = restTemplate.postForObject(url, entity, Map.class);

        List content = (List) response.get("content");
        Map first = (Map) content.get(0);

        return first.get("text").toString();

    }

    public Map<String, Object> explainWithAllModels(String anomaly) {

        String prompt = buildPrompt(anomaly);
        System.out.println(prompt);
        String claudeAiResponse = callAnthropicAI(prompt);
        Map<String, Object> result = new HashMap<>();
        result.put("claude_explanation", claudeAiResponse);

        return result;
    }
}
