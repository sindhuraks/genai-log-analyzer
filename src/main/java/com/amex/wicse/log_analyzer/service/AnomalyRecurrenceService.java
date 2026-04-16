package com.amex.wicse.log_analyzer.service;

import com.amex.wicse.log_analyzer.model.ApacheLogAnomaly;
import com.amex.wicse.log_analyzer.model.HDFSLogAnomaly;
import com.amex.wicse.log_analyzer.model.ZookeeperLogAnomaly;
import com.amex.wicse.log_analyzer.repo.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class AnomalyRecurrenceService {

    private final ApacheAnomalyRepo apacheAnomalyRepo;
    private final HDFSAnomalyRepo hdfsAnomalyRepo;
    private final ZookeeperAnomalyRepo zookeeperAnomalyRepo;

    public AnomalyRecurrenceService(ApacheAnomalyRepo apacheAnomalyRepo, HDFSAnomalyRepo hdfsAnomalyRepo, ZookeeperAnomalyRepo zookeeperAnomalyRepo) {
        this.apacheAnomalyRepo = apacheAnomalyRepo;
        this.hdfsAnomalyRepo = hdfsAnomalyRepo;
        this.zookeeperAnomalyRepo = zookeeperAnomalyRepo;
    }

    public Map<String, Object> checkRecurrence(String anomalyId) {

        Object anomaly = getAnomaly(anomalyId);
        String currentContent = extractContent(anomaly);
        List<Object> allAnomalies = getAllAnomalies(anomalyId);
        int count = 0;
        String firstSeen = null;

        for (Object obj : allAnomalies) {

            String content = extractContent(obj);

            if (content != null && content.equals(currentContent)) {
                count++;

                String timestamp = extractTimestamp(obj);

                if (timestamp != null && !timestamp.isEmpty()) {
                    if (firstSeen == null || timestamp.compareTo(firstSeen) < 0) {
                        firstSeen = timestamp;
                    }
                }
            }
        }

        return Map.of(
                "anomalyId", anomalyId,
                "recurrent", count > 1,
                "occurrenceCount", count,
                "firstSeen", firstSeen != null ? firstSeen : "N/A"
        );

    }

    private Object getAnomaly(String anomalyId) {

        if (anomalyId.startsWith("apache")) {
            return apacheAnomalyRepo.findByAnomalyId(anomalyId).orElse(null);
        } else if (anomalyId.startsWith("hdfs")) {
            return hdfsAnomalyRepo.findByAnomalyId(anomalyId).orElse(null);
        } else {
            return zookeeperAnomalyRepo.findByAnomalyId(anomalyId).orElse(null);
        }
    }

    private String extractContent(Object anomaly) {

        if (anomaly instanceof ApacheLogAnomaly a) return a.getContent();
        if (anomaly instanceof HDFSLogAnomaly h) return h.getContent();
        if (anomaly instanceof ZookeeperLogAnomaly z) return z.getContent();
        return "";
    }

    private List<Object> getAllAnomalies(String anomalyId) {

        if (anomalyId.startsWith("apache")) {
            return apacheAnomalyRepo.findAll().stream().map(a -> (Object) a).toList();
        } else if (anomalyId.startsWith("hdfs")) {
            return hdfsAnomalyRepo.findAll().stream().map(a -> (Object) a).toList();
        } else {
            return zookeeperAnomalyRepo.findAll().stream().map(a -> (Object) a).toList();
        }
    }

    private String extractTimestamp(Object anomaly) {

        if (anomaly instanceof ApacheLogAnomaly a) {
            return safe(a.getTime());
        }
        if (anomaly instanceof HDFSLogAnomaly h) {
            return safe(h.getDate()) + " " + safe(h.getTime());
        }
        if (anomaly instanceof ZookeeperLogAnomaly z) {
            return safe(z.getDate()) + " " + safe(z.getTime());
        }

        return "";
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }
}
