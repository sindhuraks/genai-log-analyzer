package com.amex.wicse.log_analyzer.rest;

import com.amex.wicse.log_analyzer.model.ApacheLogAnomaly;
import com.amex.wicse.log_analyzer.model.HDFSLogAnomaly;
import com.amex.wicse.log_analyzer.model.ZookeeperLogAnomaly;
import com.amex.wicse.log_analyzer.repo.ExplanationsRepo;
import com.amex.wicse.log_analyzer.service.AnomalyDetectionService;
import com.amex.wicse.log_analyzer.service.AnomalyExplanationService;
import com.amex.wicse.log_analyzer.service.AnomalyRecurrenceService;
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
public class AnomalyExplanationTest {

    @Mock
    private AnomalyExplanationService anomalyExplanationService;
    @Mock
    private AnomalyDetectionService anomalyDetectionService;

    @Mock
    private ExplanationsRepo explanationsRepo;

    @InjectMocks
    private AnomalyExplanation controller;

    @Test
    void testOpenAiExplanation_Success() {

        String anomalyId = "apache_error_123";
        String anomalyContent = "mod_jk child workerEnv in error state 6";

        ApacheLogAnomaly anomaly = new ApacheLogAnomaly();
        anomaly.setContent(anomalyContent);

        when(anomalyDetectionService.getAnomalyById(anomalyId))
                .thenReturn(Optional.of(anomaly));

        when(anomalyExplanationService.explainWithOpenAIModel(anomalyContent, anomalyId))
                .thenReturn(Map.of("openai_explanation", "Let's analyze the log anomaly you've provided:\\n\\n### 1. What the error means?\\nThe log entry `mod_jk child workerEnv in error state 6` indicates that a worker in the Apache mod_jk module (which is used to connect Apache HTTP Server with Tomcat or other servlet containers) has encountered an error. The \\\"child worker\\\" refers to a specific worker thread that is responsible for handling requests. The \\\"error state 6\\\" is a specific error code that mod_jk uses to indicate a particular type of failure.\\n\\nIn mod_jk, error states are typically defined as follows:\\n- State 0: Idle\\n- State 1: Busy\\n- State 2: Error\\n- State 3: Stopped\\n- State 4: Starting\\n- State 5: Stopping\\n- State 6: In error state (specific error)\\n\\nError state 6 generally indicates that the worker has encountered a critical issue that prevents it from processing requests. This could be due to various reasons, such as network issues, configuration problems, or the backend application (like Tomcat) being down or unreachable.\\n\\n### 2. Root cause:\\nThe root cause of the error could be one or more of the following:\\n- **Backend Server Unreachable**: The Tomcat server that the mod_jk worker is trying to connect to may be down, misconfigured, or not responding.\\n- **Network Issues**: There may be network connectivity issues between the Apache server and the Tomcat server, such as firewall rules blocking traffic or DNS resolution problems.\\n- **Configuration Errors**: There may be misconfigurations in the `workers.properties` file or in the Apache configuration that prevent proper communication between the servers.\\n- **Resource Exhaustion**: The Tomcat server might be running out of resources (like memory or threads), causing it to fail to respond to requests.\\n\\n### 3. Suggested mitigation to avoid the problem:\\nTo mitigate this issue and avoid future occurrences, consider the following actions:\\n\\n- **Monitor Backend Health**: Implement health checks for the backend Tomcat server to ensure it is running and responsive. Use monitoring tools to alert you when the server goes down or becomes unresponsive.\\n  \\n- **Review Configuration**: Double-check the `workers.properties` and Apache configuration files for any misconfigurations. Ensure that the worker settings, such as connection timeouts and load balancing configurations, are correctly set.\\n\\n- **Network Diagnostics**: Conduct network diagnostics"));

        ResponseEntity<?> response = controller.explainAnomaly(anomalyId, "openai");

        assertEquals(200, response.getStatusCodeValue());
        Map<?, ?> body = (Map<?, ?>) response.getBody();
        assertEquals(anomalyId, body.get("anomalyId"));
        assertTrue(body.containsKey("openai_explanation"));
        String explanation = (String) body.get("openai_explanation");
        assertTrue(explanation.contains("Let's analyze the log anomaly"));
        verify(anomalyDetectionService).getAnomalyById(anomalyId);
        verify(anomalyExplanationService).explainWithOpenAIModel(anomalyContent, anomalyId);
    }

    @Test
    void testOpenllamaExplanation_Success() {

        String anomalyId = "hdfs_warn_78";
        String anomalyContent = "10.251.39.144:50010:Got exception while serving blk_-8083036675630459841 to /10.251.39.209:";

        HDFSLogAnomaly anomaly = new HDFSLogAnomaly();
        anomaly.setContent(anomalyContent);

        when(anomalyDetectionService.getAnomalyById(anomalyId))
                .thenReturn(Optional.of(anomaly));

        when(anomalyExplanationService.explainWithOllamaModel(anomalyContent, anomalyId))
                .thenReturn(Map.of("ollama_explanation", "The error indicates that an exception occurred while serving a block with the specified ID to the client at IP address 10.251.39.209.The root cause of this issue is likely due to a bug or malfunction within the system's storage subsystem, causing data corruption or other issues when reading from or writing to disk.To mitigate this issue and prevent similar errors from occurring in the future, suggested solutions include:\n- Regularly performing backups of critical data to ensure it can be restored if needed."));
        ResponseEntity<?> response = controller.explainAnomaly(anomalyId, "ollama");

        assertEquals(200, response.getStatusCodeValue());
        Map<?, ?> body = (Map<?, ?>) response.getBody();
        assertEquals(anomalyId, body.get("anomalyId"));
        assertTrue(body.containsKey("ollama_explanation"));
        String explanation = (String) body.get("ollama_explanation");
        assertTrue(explanation.contains("The error indicates that an exception"));
        verify(anomalyDetectionService).getAnomalyById(anomalyId);
        verify(anomalyExplanationService).explainWithOllamaModel(anomalyContent, anomalyId);
    }

    @Test
    void testAnthropicExplanation_Success() {

        String anomalyId = "zoo_warn_418";
        String anomalyContent = "[NIOServerCxn.Factory:0.0.0.0/0.0.0.0:2181:ZooKeeperServer] - Connection request from old client /10.10.34.40:40996; will be dropped if server is in r-o mode";

        ZookeeperLogAnomaly anomaly = new ZookeeperLogAnomaly();
        anomaly.setContent(anomalyContent);

        when(anomalyDetectionService.getAnomalyById(anomalyId))
                .thenReturn(Optional.of(anomaly));

        when(anomalyExplanationService.explainWithAnthropicModel(anomalyContent, anomalyId))
                .thenReturn(Map.of("claude_explanation", "The error message indicates that a connection request has been received from an \"old client\" (identified as 10.10.34.40:40996) and that this connection will be dropped if the ZooKeeper server is in read-only mode. The root cause of this error is that the client application is trying to connect to the ZooKeeper server using an outdated or incompatible protocol version. ZooKeeper servers may sometimes enter a read-only mode, which means they can only serve read requests and not write requests. To mitigate this issue, the following steps can be taken:\n- Ensure that the client application is using the latest version of the ZooKeeper client library to ensure compatibility with the server's protocol."));
        ResponseEntity<?> response = controller.explainAnomaly(anomalyId, "anthropic");

        assertEquals(200, response.getStatusCodeValue());
        Map<?, ?> body = (Map<?, ?>) response.getBody();
        assertEquals(anomalyId, body.get("anomalyId"));
        assertTrue(body.containsKey("claude_explanation"));
        String explanation = (String) body.get("claude_explanation");
        assertTrue(explanation.contains("The error message indicates that a connection request"));
        verify(anomalyDetectionService).getAnomalyById(anomalyId);
        verify(anomalyExplanationService).explainWithAnthropicModel(anomalyContent, anomalyId);
    }

}
