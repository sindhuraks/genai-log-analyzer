package com.amex.wicse.log_analyzer.rest;

import com.amex.wicse.log_analyzer.service.AnomalyRecurrenceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/anomalies")
@CrossOrigin(origins = "http://localhost:3000")
public class AnomalyRecurrence {

    private final AnomalyRecurrenceService anomalyRecurrenceService;

    public AnomalyRecurrence(AnomalyRecurrenceService anomalyRecurrenceService) {
        this.anomalyRecurrenceService = anomalyRecurrenceService;
    }

    @GetMapping("/{anomalyId}/recurrence")
    public ResponseEntity<?> checkRecurrence(@PathVariable String anomalyId) {

        try {
            return ResponseEntity.ok(
                    anomalyRecurrenceService.checkRecurrence(anomalyId)
            );
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body("Error checking recurrence: " + e.getMessage());
        }
    }
}
