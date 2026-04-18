package com.amex.wicse.log_analyzer.rest;

import com.amex.wicse.log_analyzer.service.AnomalyRecurrenceService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnomalyRecurrenceTest {

    @Mock
    private AnomalyRecurrenceService anomalyRecurrenceService;

    @InjectMocks
    private AnomalyRecurrence controller;

    @Test
    void testCheckRecurrence_Success() {
        when(anomalyRecurrenceService.checkRecurrence("apache_error_123"))
                .thenReturn(Map.of(
                        "anomalyId", "apache_error_123",
                        "recurrent", true,
                        "occurrenceCount", 2,
                        "firstSeen", "2026-03-17 10:04:01"
                ));

        ResponseEntity<?> response = controller.checkRecurrence("apache_error_123");

        assertEquals(200, response.getStatusCodeValue());
        Map<?, ?> body = (Map<?, ?>) response.getBody();
        assertEquals("apache_error_123", body.get("anomalyId"));
        assertEquals(true, body.get("recurrent"));
        assertEquals(2, body.get("occurrenceCount"));
        assertEquals("2026-03-17 10:04:01", body.get("firstSeen"));
    }

    @Test
    void testCheckRecurrence_Exception() {
        when(anomalyRecurrenceService.checkRecurrence("apache_error_123"))
                .thenThrow(new RuntimeException("error"));

        ResponseEntity<?> response = controller.checkRecurrence("apache_error_123");

        assertEquals(400, response.getStatusCodeValue());
        assertTrue(response.getBody().toString().contains("Error checking recurrence"));
    }
}