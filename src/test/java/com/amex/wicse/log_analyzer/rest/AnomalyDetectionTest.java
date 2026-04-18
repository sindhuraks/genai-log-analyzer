package com.amex.wicse.log_analyzer.rest;

import com.amex.wicse.log_analyzer.model.ApacheLogAnomaly;
import com.amex.wicse.log_analyzer.model.HDFSLogAnomaly;
import com.amex.wicse.log_analyzer.model.ZookeeperLogAnomaly;
import com.amex.wicse.log_analyzer.service.AnomalyDetectionService;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.junit.jupiter.api.Test;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
@ExtendWith(MockitoExtension.class)
public class AnomalyDetectionTest {

    @Mock
    private AnomalyDetectionService anomalyDetectionService;

    @InjectMocks
    private AnomalyDetection controller;

    @Test
    void testDetectApacheAnomalies_Success() {

        when(anomalyDetectionService.saveApacheAnomalies())
                .thenReturn(List.of(new ApacheLogAnomaly(), new ApacheLogAnomaly()));

        ResponseEntity<String> response = controller.detectAnomalies("apache");

        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Detected 2 apache anomalies", response.getBody());

        verify(anomalyDetectionService).saveApacheAnomalies();
    }

    @Test
    void testDetectHDFSAnomalies_Success() {

        when(anomalyDetectionService.saveHDFSAnomalies())
                .thenReturn(List.of(new HDFSLogAnomaly()));

        ResponseEntity<String> response = controller.detectAnomalies("hdfs");

        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Detected 1 hdfs anomalies", response.getBody());

        verify(anomalyDetectionService).saveHDFSAnomalies();
    }

    @Test
    void testDetectZookeeperAnomalies_Success() {

        when(anomalyDetectionService.saveZookeeperAnomalies())
                .thenReturn(List.of(new ZookeeperLogAnomaly(), new ZookeeperLogAnomaly(), new ZookeeperLogAnomaly()));

        ResponseEntity<String> response = controller.detectAnomalies("zookeeper");

        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Detected 3 zookeeper anomalies", response.getBody());

        verify(anomalyDetectionService).saveZookeeperAnomalies();
    }

    @Test
    void testDetectAnomalies_InvalidType() {

        ResponseEntity<String> response = controller.detectAnomalies("invalid");

        assertEquals(400, response.getStatusCodeValue());
        assertTrue(response.getBody().contains("Unsupported log type"));
    }

    @Test
    void testDetectAnomalies_Exception() {

        when(anomalyDetectionService.saveApacheAnomalies())
                .thenThrow(new RuntimeException("DB error"));

        ResponseEntity<String> response = controller.detectAnomalies("apache");

        assertEquals(400, response.getStatusCodeValue());
        assertTrue(response.getBody().contains("Failed to store apache anomaly logs"));
    }

    @Test
    void testGetAnomalyInfo_Success() {

        ApacheLogAnomaly anomaly = new ApacheLogAnomaly();

        when(anomalyDetectionService.getAnomalyById("apache_1"))
                .thenReturn(Optional.of(anomaly));

        ResponseEntity<?> response = controller.getAnomalyInfo("apache_1");

        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody() instanceof Optional);

        verify(anomalyDetectionService).getAnomalyById("apache_1");
    }

    @Test
    void testGetAnomalyInfo_Exception() {

        when(anomalyDetectionService.getAnomalyById("id"))
                .thenThrow(new RuntimeException("not found"));

        ResponseEntity<?> response = controller.getAnomalyInfo("id");

        assertEquals(400, response.getStatusCodeValue());
        assertTrue(response.getBody().toString().contains("Failed to get anomaly info"));
    }

    @Test
    void testListAllAnomalies_Success() {

        Map<String, Object> mockData = Map.of(
                "apache", List.of(new ApacheLogAnomaly()),
                "hdfs", List.of(new HDFSLogAnomaly()),
                "zookeeper", List.of(new ZookeeperLogAnomaly())
        );

        when(anomalyDetectionService.listAnomalies())
                .thenReturn(mockData);

        ResponseEntity<?> response = controller.listAllAnomalies();

        assertEquals(200, response.getStatusCodeValue());

        Map<?, ?> body = (Map<?, ?>) response.getBody();
        assertTrue(body.containsKey("apache"));
        assertTrue(body.containsKey("hdfs"));
        assertTrue(body.containsKey("zookeeper"));

        verify(anomalyDetectionService).listAnomalies();
    }

    @Test
    void testListAllAnomalies_Exception() {

        when(anomalyDetectionService.listAnomalies())
                .thenThrow(new RuntimeException("failure"));

        ResponseEntity<?> response = controller.listAllAnomalies();

        assertEquals(400, response.getStatusCodeValue());
        assertTrue(response.getBody().toString().contains("Failed to fetch anomalies"));
    }

}
