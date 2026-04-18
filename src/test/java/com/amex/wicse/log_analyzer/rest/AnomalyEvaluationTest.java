package com.amex.wicse.log_analyzer.rest;

import com.amex.wicse.log_analyzer.model.ApacheLogAnomaly;
import com.amex.wicse.log_analyzer.model.HDFSLogAnomaly;
import com.amex.wicse.log_analyzer.model.ZookeeperLogAnomaly;
import com.amex.wicse.log_analyzer.repo.ExplanationsRepo;
import com.amex.wicse.log_analyzer.service.AnomalyDetectionService;
import com.amex.wicse.log_analyzer.service.AnomalyEvaluationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.ResponseEntity;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AnomalyEvaluationTest {

    @Mock
    private AnomalyEvaluationService anomalyEvaluationService;
    @Mock
    private AnomalyDetectionService anomalyDetectionService;
    @Mock
    private ExplanationsRepo explanationsRepo;
    @InjectMocks
    private AnomalyEvaluation controller;

    @Test
    void testEvaluateExplanation_OpenAI_Success() {

        String anomalyId = "apache_error_123";
        String model = "openai";
        String anomalyContent = "mod_jk child workerEnv in error state 6";
        String explanation = "Some explanation";

        ApacheLogAnomaly anomaly = new ApacheLogAnomaly();
        anomaly.setContent(anomalyContent);

        when(anomalyDetectionService.getAnomalyById(anomalyId))
                .thenReturn(Optional.of(anomaly));

        com.amex.wicse.log_analyzer.model.Explanations exp =
                new com.amex.wicse.log_analyzer.model.Explanations();
        exp.setExplanation(explanation);

        when(explanationsRepo.findByAnomalyIdAndModel(anomalyId, model))
                .thenReturn(Optional.of(exp));

        when(anomalyEvaluationService.evaluateWithAnthropicModel(anomalyContent, explanation))
                .thenReturn(Map.of("claude_eval", "{ " +
                        "\"correctness\": 0.9 " +
                        "\"completeness\": 0.8 " +
                        "\"clarity\": 0.95 " +
                        "\"answer_relevance\": 0.9 " +
                        "}"));

        ResponseEntity<?> response =
                controller.evaluateExplanation(anomalyId, model);

        assertEquals(200, response.getStatusCodeValue());

        Map<?, ?> body = (Map<?, ?>) response.getBody();

        assertEquals(anomalyId, body.get("anomalyId"));
        assertEquals(model, body.get("model"));
        assertTrue(body.containsKey("claude_evaluation"));

        String eval = (String) body.get("claude_evaluation");
        assertTrue(eval.contains("correctness"));
        assertTrue(eval.contains("completeness"));
        assertTrue(eval.contains("clarity"));
        assertTrue(eval.contains("answer_relevance"));
        verify(anomalyDetectionService).getAnomalyById(anomalyId);
        verify(explanationsRepo).findByAnomalyIdAndModel(anomalyId, model);
        verify(anomalyEvaluationService)
                .evaluateWithAnthropicModel(anomalyContent, explanation);
    }

    @Test
    void testEvaluateExplanation_HDFS_Success() {

        String anomalyId = "hdfs_warn_78";
        String model = "openai";
        String anomalyContent = "Block read exception in DataNode";
        String explanation = "HDFS explanation";

        HDFSLogAnomaly anomaly = new HDFSLogAnomaly();
        anomaly.setContent(anomalyContent);

        when(anomalyDetectionService.getAnomalyById(anomalyId))
                .thenReturn(Optional.of(anomaly));

        com.amex.wicse.log_analyzer.model.Explanations exp =
                new com.amex.wicse.log_analyzer.model.Explanations();
        exp.setExplanation(explanation);

        when(explanationsRepo.findByAnomalyIdAndModel(anomalyId, model))
                .thenReturn(Optional.of(exp));

        when(anomalyEvaluationService.evaluateWithAnthropicModel(anomalyContent, explanation))
                .thenReturn(Map.of("claude_eval", "{ \"correctness\": 0.85 }"));

        ResponseEntity<?> response =
                controller.evaluateExplanation(anomalyId, model);

        assertEquals(200, response.getStatusCodeValue());

        Map<?, ?> body = (Map<?, ?>) response.getBody();

        assertEquals(anomalyId, body.get("anomalyId"));
        assertEquals(model, body.get("model"));
        assertTrue(body.containsKey("claude_evaluation"));

        String eval = (String) body.get("claude_evaluation");
        assertTrue(eval.contains("correctness"));

        verify(anomalyDetectionService).getAnomalyById(anomalyId);
        verify(explanationsRepo).findByAnomalyIdAndModel(anomalyId, model);
        verify(anomalyEvaluationService)
                .evaluateWithAnthropicModel(anomalyContent, explanation);
    }

    @Test
    void testEvaluateExplanation_Zookeeper_Success() {

        String anomalyId = "zk_error_45";
        String model = "openai";
        String anomalyContent = "Connection loss to quorum peer";
        String explanation = "Zookeeper explanation";

        ZookeeperLogAnomaly anomaly = new ZookeeperLogAnomaly();
        anomaly.setContent(anomalyContent);

        when(anomalyDetectionService.getAnomalyById(anomalyId))
                .thenReturn(Optional.of(anomaly));

        com.amex.wicse.log_analyzer.model.Explanations exp =
                new com.amex.wicse.log_analyzer.model.Explanations();
        exp.setExplanation(explanation);

        when(explanationsRepo.findByAnomalyIdAndModel(anomalyId, model))
                .thenReturn(Optional.of(exp));

        when(anomalyEvaluationService.evaluateWithAnthropicModel(anomalyContent, explanation))
                .thenReturn(Map.of("claude_eval", "{ \"correctness\": 0.8 }"));

        ResponseEntity<?> response =
                controller.evaluateExplanation(anomalyId, model);

        assertEquals(200, response.getStatusCodeValue());

        Map<?, ?> body = (Map<?, ?>) response.getBody();

        assertEquals(anomalyId, body.get("anomalyId"));
        assertEquals(model, body.get("model"));
        assertTrue(body.containsKey("claude_evaluation"));

        String eval = (String) body.get("claude_evaluation");
        assertTrue(eval.contains("correctness"));

        verify(anomalyDetectionService).getAnomalyById(anomalyId);
        verify(explanationsRepo).findByAnomalyIdAndModel(anomalyId, model);
        verify(anomalyEvaluationService)
                .evaluateWithAnthropicModel(anomalyContent, explanation);
    }

    @Test
    void testEvaluateExplanation_ExplanationNotFound() {

        when(anomalyDetectionService.getAnomalyById("id"))
                .thenReturn(Optional.of(new ApacheLogAnomaly()));

        when(explanationsRepo.findByAnomalyIdAndModel("id", "openai"))
                .thenReturn(Optional.empty());

        ResponseEntity<?> response =
                controller.evaluateExplanation("id", "openai");

        assertEquals(400, response.getStatusCodeValue());
    }

}
