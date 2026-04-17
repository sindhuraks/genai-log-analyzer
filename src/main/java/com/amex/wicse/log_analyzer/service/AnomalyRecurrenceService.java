package com.amex.wicse.log_analyzer.service;

import com.amex.wicse.log_analyzer.model.ApacheLogAnomaly;
import com.amex.wicse.log_analyzer.model.HDFSLogAnomaly;
import com.amex.wicse.log_analyzer.model.ZookeeperLogAnomaly;
import com.amex.wicse.log_analyzer.repo.*;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class AnomalyRecurrenceService {

    private final ApacheAnomalyRepo apacheAnomalyRepo;
    private final HDFSAnomalyRepo hdfsAnomalyRepo;
    private final ZookeeperAnomalyRepo zookeeperAnomalyRepo;

    private static final DateTimeFormatter APACHE_FMT = DateTimeFormatter.ofPattern("EEE MMM dd HH:mm:ss yyyy", Locale.ENGLISH);
    private static final DateTimeFormatter HDFS_FMT = DateTimeFormatter.ofPattern("yyMMdd HHmmss");
    private static final DateTimeFormatter ZOO_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss,SSS");

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
        LocalDateTime earliest = null;

        for (Object obj : allAnomalies) {

            String content = extractContent(obj);

            if (content != null && content.equals(currentContent)) {
                count++;

                String timestamp = extractTimestamp(obj);
                LocalDateTime currentDt = parseToDateTime(timestamp, anomalyId);
                System.out.println(currentDt);

                if (currentDt != null) {
                    if (earliest == null || currentDt.isBefore(earliest)) {
                        earliest = currentDt;
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

    private LocalDateTime parseToDateTime(String timestamp, String anomalyId) {
        try {
            System.out.println(timestamp);
            if (timestamp == null || timestamp.isEmpty()) return null;

            if (anomalyId.startsWith("apache")) {
                return LocalDateTime.parse(timestamp, APACHE_FMT);
            } else if (anomalyId.startsWith("hdfs")) {
                return LocalDateTime.parse(timestamp, HDFS_FMT);
            } else {
                String cleanedTimeStamp = timestamp.replace("\"", "");
                return LocalDateTime.parse(cleanedTimeStamp, ZOO_FMT);
            }
        } catch (Exception e) {
            return null;
        }
    }

}
