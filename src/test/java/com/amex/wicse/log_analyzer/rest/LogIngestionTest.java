package com.amex.wicse.log_analyzer.rest;

import com.amex.wicse.log_analyzer.service.LogIngestionService;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.junit.jupiter.api.Test;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class LogIngestionTest {

    @Mock
    private LogIngestionService logIngestionService;

    @InjectMocks
    private LogIngestion controller;

    @Test
    void testIngestApache_Success() {

        MultipartFile file = new MockMultipartFile(
                "apache_2k",
                "apache_2k_structured.csv",
                "text/csv",
                "header\nrow1".getBytes()
        );

        when(logIngestionService.save(file)).thenReturn(5);

        ResponseEntity<String> response = controller.ingestLog("apache", file);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Inserted 5 rows for type apache", response.getBody());

        verify(logIngestionService).save(file);
    }

    @Test
    void testIngestHDFS_Success() {

        MultipartFile file = new MockMultipartFile(
                "hdfs_2k",
                "hdfs_2k_structured.csv",
                "text/csv",
                "header\nrow1".getBytes()
        );

        when(logIngestionService.saveHDFS(file)).thenReturn(3);

        ResponseEntity<String> response = controller.ingestLog("hdfs", file);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Inserted 3 rows for type hdfs", response.getBody());

        verify(logIngestionService).saveHDFS(file);
    }

    @Test
    void testIngestZookeeper_Success() {

        MultipartFile file = new MockMultipartFile(
                "zookeeper_2k",
                "zookeeper_2k_structured.csv",
                "text/csv",
                "header\nrow1".getBytes()
        );

        when(logIngestionService.saveZookeeper(file)).thenReturn(7);

        ResponseEntity<String> response = controller.ingestLog("zookeeper", file);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Inserted 7 rows for type zookeeper", response.getBody());

        verify(logIngestionService).saveZookeeper(file);
    }

    @Test
    void testIngest_InvalidType() {

        MultipartFile file = new MockMultipartFile(
                "file",
                "test.csv",
                "text/csv",
                "data".getBytes()
        );

        ResponseEntity<String> response = controller.ingestLog("invalid", file);

        assertEquals(400, response.getStatusCodeValue());
        assertTrue(response.getBody().contains("Unsupported log type"));
    }
    @Test
    void testIngest_Exception() {

        MultipartFile file = new MockMultipartFile(
                "file",
                "test.csv",
                "text/csv",
                "data".getBytes()
        );

        when(logIngestionService.save(file))
                .thenThrow(new RuntimeException("parse error"));

        ResponseEntity<String> response = controller.ingestLog("apache", file);

        assertEquals(400, response.getStatusCodeValue());
        assertTrue(response.getBody().contains("Failed to ingest apache logs"));
    }
}
