package com.amex.wicse.log_analyzer.rest;

import com.amex.wicse.log_analyzer.model.ApacheLogAnomaly;
import com.amex.wicse.log_analyzer.model.HDFSLogAnomaly;
import com.amex.wicse.log_analyzer.model.ZookeeperLogAnomaly;
import com.amex.wicse.log_analyzer.service.AnomalyDetectionService;
import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/anomalies")
public class AnomalyDetection {

    private final AnomalyDetectionService anomalyDetectionService;

    public AnomalyDetection(AnomalyDetectionService anomalyDetectionService) {
        this.anomalyDetectionService = anomalyDetectionService;
    }

    @PostMapping("/{srcType}")
    public ResponseEntity<String> detectAnomalies(
            @PathVariable("srcType") String srcType
    ) {
        try {
                int inserted;

                switch(srcType.toLowerCase()) {
                    case "apache":
                        List<ApacheLogAnomaly> apacheAnomalies = anomalyDetectionService.saveApacheAnomalies();
                        inserted = apacheAnomalies.size();
                        break;
                    case "hdfs":
                        List<HDFSLogAnomaly> hdfsAnomalies = anomalyDetectionService.saveHDFSAnomalies();
                        inserted = hdfsAnomalies.size();
                        break;
                    case "zookeeper":
                        List<ZookeeperLogAnomaly> zookeeperAnomalies = anomalyDetectionService.saveZookeeperAnomalies();
                        inserted = zookeeperAnomalies.size();
                        break;
                    default:
                        return ResponseEntity.badRequest().body("Unsupported log type: " + srcType);
                }
            return ResponseEntity.ok("Detected " + inserted + " " + srcType + " anomalies");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to store " + srcType + " anomaly logs: " + e.getMessage());
        }
    }

    @GetMapping("/{anomalyId}")
    public ResponseEntity<?> getAnomalyInfo(@PathVariable("anomalyId") String anomalyId) {
        try {
            Object anomaly = anomalyDetectionService.getAnomalyById(anomalyId);
            return ResponseEntity.ok(anomaly);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to get anomaly info: " + e.getMessage());
        }
    }
}
