package com.amex.wicse.log_analyzer.rest;

import com.amex.wicse.log_analyzer.model.ApacheLogAnomaly;
import com.amex.wicse.log_analyzer.model.HDFSLogAnomaly;
import com.amex.wicse.log_analyzer.model.ZookeeperLogAnomaly;
import com.amex.wicse.log_analyzer.service.AnomalyDetectionService;
import com.amex.wicse.log_analyzer.service.AnomalyExplanationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;

@RestController
@RequestMapping("/api/anomalies")
@CrossOrigin(origins = "http://localhost:3000")
public class AnomalyExplanation {

    private AnomalyExplanationService anomalyExplanationService;
    private AnomalyDetectionService anomalyDetectionService;

    public AnomalyExplanation(AnomalyExplanationService anomalyExplanationService, AnomalyDetectionService anomalyDetectionService) {
        this.anomalyExplanationService = anomalyExplanationService;
        this.anomalyDetectionService = anomalyDetectionService;
    }

    @PostMapping("/{anomalyId}/explain/{model}")
    public ResponseEntity<?> explainAnomaly(@PathVariable("anomalyId") String anomalyId, @PathVariable("model") String model) {
        try {
            Object anomaly = anomalyDetectionService.getAnomalyById(anomalyId);
//            Optional<ApacheLogAnomaly> optAnomaly = (Optional<ApacheLogAnomaly>) anomaly;
//            ApacheLogAnomaly apacheLogAnomaly = optAnomaly.orElseThrow(() -> new RuntimeException("Anomaly not found"));
//            String anomalyContent = apacheLogAnomaly.getContent();

            Optional<?> optAnomaly = (Optional<?>) anomaly;
            Object logAnomaly = optAnomaly.orElseThrow(() -> new RuntimeException("Anomaly not found"));
            String anomalyContent;

            if (logAnomaly instanceof ApacheLogAnomaly) {
                anomalyContent = ((ApacheLogAnomaly) logAnomaly).getContent();
            } else if (logAnomaly instanceof HDFSLogAnomaly) {
                anomalyContent = ((HDFSLogAnomaly) logAnomaly).getContent();
            } else if (logAnomaly instanceof ZookeeperLogAnomaly) {
                anomalyContent = ((ZookeeperLogAnomaly) logAnomaly).getContent();
            } else {
                throw new RuntimeException("Unknown anomaly type: " + logAnomaly.getClass().getName());
            }

            Map<String, Object> response = new HashMap<>();
            Map<String, Object> llmResponse = new HashMap<>();
            response.put("anomalyId", anomalyId);
            response.put("anomaly", anomaly);

            switch (model.toLowerCase()) {
                case "anthropic":
                    llmResponse = anomalyExplanationService.explainWithAnthropicModel(anomalyContent, anomalyId);
                    response.put("claude_explanation", llmResponse.get("claude_explanation"));
                    break;
                case "ollama":
                    llmResponse = anomalyExplanationService.explainWithOllamaModel(anomalyContent, anomalyId);
                    response.put("ollama_explanation", llmResponse.get("ollama_explanation"));
                    break;
                case "openai":
                    llmResponse = anomalyExplanationService.explainWithOpenAIModel(anomalyContent, anomalyId);
                    response.put("openai_explanation", llmResponse.get("openai_explanation"));
                    break;
                default:
                    return ResponseEntity.badRequest().body("Unsupported LLM model: " + model);
            }

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to get anomaly explanation from LLM: " + e.getMessage());
        }
    }
}
