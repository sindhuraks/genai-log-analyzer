package com.amex.wicse.log_analyzer.rest;

import com.amex.wicse.log_analyzer.service.LogIngestionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/logs")
public class LogIngestion {

    private final LogIngestionService logIngestionService;

    public LogIngestion(LogIngestionService logIngestionService) {
        this.logIngestionService = logIngestionService;
    }

    @PostMapping("/{logType}/ingest")
    public ResponseEntity<String> ingestLog(
            @PathVariable("logType") String logType,
            @RequestParam("file") MultipartFile file
    ) {
        try {
            int inserted;
            System.out.println("logType = [" + logType + "]");
            switch (logType.toLowerCase()) {
                case "apache":
                    inserted = logIngestionService.save(file);
                    break;
                case "hdfs":
                    inserted = logIngestionService.saveHDFS(file);
                    break;
                case "zookeeper":
                    inserted = logIngestionService.saveZookeeper(file);
                    break;
                default:
                    return ResponseEntity.badRequest().body("Unsupported log type: " + logType);
            }
            return ResponseEntity.ok("Inserted " + inserted + " rows for type " + logType);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to ingest " + logType + " logs: " + e.getMessage());
        }
    }
}
