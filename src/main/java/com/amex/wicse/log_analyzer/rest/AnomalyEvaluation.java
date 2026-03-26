package com.amex.wicse.log_analyzer.rest;

import com.amex.wicse.log_analyzer.model.ApacheLogAnomaly;
import com.amex.wicse.log_analyzer.model.HDFSLogAnomaly;
import com.amex.wicse.log_analyzer.model.ZookeeperLogAnomaly;
import com.amex.wicse.log_analyzer.repo.ExplanationsRepo;
import com.amex.wicse.log_analyzer.service.AnomalyEvaluationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.amex.wicse.log_analyzer.service.AnomalyDetectionService;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/llm")
@CrossOrigin(origins = "http://localhost:3000")
public class AnomalyEvaluation {

    private AnomalyEvaluationService anomalyEvaluationService;
    private AnomalyDetectionService anomalyDetectionService;
    private ExplanationsRepo explanationsRepo;

    public AnomalyEvaluation(AnomalyEvaluationService anomalyEvaluationService, AnomalyDetectionService anomalyDetectionService, ExplanationsRepo explanationsRepo) {
        this.anomalyEvaluationService = anomalyEvaluationService;
        this.anomalyDetectionService = anomalyDetectionService;
        this.explanationsRepo = explanationsRepo;
    }

    @PostMapping("/{anomalyId}/{model}/eval")
    public ResponseEntity<?> evaluateExplanation(@PathVariable("anomalyId") String anomalyId, @PathVariable("model") String model) {
        try {
            Object anomaly = anomalyDetectionService.getAnomalyById(anomalyId);
            Optional<?> optAnomaly = (Optional<?>) anomaly;
            Object logAnomaly = optAnomaly.orElseThrow(() -> new RuntimeException("Anomaly not found"));
            String anomalyContent;
            String explanation = explanationsRepo
                    .findByAnomalyIdAndModel(anomalyId, model)
                    .orElseThrow(() -> new RuntimeException("Explanation not found"))
                    .getExplanation();

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
            Map<String, Object> llmEval;
            response.put("anomalyId", anomalyId);
            response.put("anomaly", anomaly);
            response.put("model", model);

            switch (model.toLowerCase()) {
                case "anthropic":
                    llmEval = anomalyEvaluationService.evaluateWithAnthropicModel(anomalyContent, explanation);
                    response.put("claude_evaluation", llmEval.get("claude_eval"));
                    break;
                case "ollama":
                    llmEval = anomalyEvaluationService.evaluateWithAnthropicModel(anomalyContent, explanation);
                    response.put("claude_evaluation", llmEval.get("claude_eval"));
                    break;
                case "openai":
                    llmEval = anomalyEvaluationService.evaluateWithAnthropicModel(anomalyContent, explanation);
                    response.put("claude_evaluation", llmEval.get("claude_eval"));
                    break;
                default:
                    return ResponseEntity.badRequest().body("Unsupported LLM model: " + model);
            }

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to get anomaly evaluation from LLM: " + e.getMessage());
        }
    }
}
