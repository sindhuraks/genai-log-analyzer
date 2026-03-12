package com.amex.wicse.log_analyzer.service;

import com.amex.wicse.log_analyzer.model.*;
import com.amex.wicse.log_analyzer.repo.*;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class AnomalyDetectionService {

    private final ApacheLogRepo apacheLogRepo;
    private final ApacheAnomalyRepo apacheAnomalyRepo;
    private final HDFSLogRepo hdfsLogRepo;
    private final HDFSAnomalyRepo hdfsAnomalyRepo;
    private final ZookeeperLogRepo zookeeperLogRepo;
    private final ZookeeperAnomalyRepo zookeeperAnomalyRepo;
    private final ConcurrentHashMap<String, AtomicInteger> anomalyCounters = new ConcurrentHashMap<>();
    public AnomalyDetectionService(ApacheLogRepo apacheLogRepo,
                                   ApacheAnomalyRepo apacheAnomalyRepo, HDFSLogRepo hdfsLogRepo, HDFSAnomalyRepo hdfsAnomalyRepo, ZookeeperLogRepo zookeeperLogRepo, ZookeeperAnomalyRepo zookeeperAnomalyRepo) {
        this.apacheLogRepo = apacheLogRepo;
        this.apacheAnomalyRepo = apacheAnomalyRepo;
        this.hdfsLogRepo = hdfsLogRepo;
        this.hdfsAnomalyRepo = hdfsAnomalyRepo;
        this.zookeeperLogRepo = zookeeperLogRepo;
        this.zookeeperAnomalyRepo = zookeeperAnomalyRepo;
    }

    @Transactional
    public List<ApacheLogAnomaly> saveApacheAnomalies() {
        List<String> anomalyLevels = List.of("error", "warn", "emerg");

        try {
            List<ApacheLog> logs = apacheLogRepo.findByLevelIn(anomalyLevels);
            List<ApacheLogAnomaly> anomalies = logs.stream()
                    .map(this::logToAnomaly)
                    .toList();
            return apacheAnomalyRepo.saveAll(anomalies);  // saveAll returns List<S>
        } catch (Exception e) {
            throw new RuntimeException("fail to store anomaly: " + e.getMessage());
        }
    }

    @Transactional
    public List<HDFSLogAnomaly> saveHDFSAnomalies() {
        List<String> anomalyLevels = List.of("ERROR", "WARN", "FATAL");

        try {
            List<HDFSLog> logs = hdfsLogRepo.findByLevelIn(anomalyLevels);
            List<HDFSLogAnomaly> anomalies = logs.stream()
                    .map(this::logToHDFSAnomaly)
                    .toList();
            return hdfsAnomalyRepo.saveAll(anomalies);
        } catch (Exception e) {
            throw new RuntimeException("fail to store anomaly: " + e.getMessage());
        }
    }

    @Transactional
    public List<ZookeeperLogAnomaly> saveZookeeperAnomalies() {
        List<String> anomalyLevels = List.of("ERROR", "WARN", "FATAL");

        try {
            List<ZookeeperLog> logs = zookeeperLogRepo.findByLevelIn(anomalyLevels);
            List<ZookeeperLogAnomaly> anomalies = logs.stream()
                    .map(this::logToZookeeperAnomaly)
                    .toList();
            return zookeeperAnomalyRepo.saveAll(anomalies);
        } catch (Exception e) {
            throw new RuntimeException("fail to store anomaly: " + e.getMessage());
        }
    }

    private ApacheLogAnomaly logToAnomaly(ApacheLog log) {
        ApacheLogAnomaly anomaly = new ApacheLogAnomaly();
        anomaly.setAnomalyId(generateAnomalyId("apache", log.getLevel()));
        anomaly.setTime(log.getTime());
        anomaly.setLevel(log.getLevel());
        anomaly.setContent(log.getContent());
        return anomaly;
    }

    private HDFSLogAnomaly logToHDFSAnomaly(HDFSLog log) {
        HDFSLogAnomaly anomaly = new HDFSLogAnomaly();
        anomaly.setAnomalyId(generateAnomalyId("hdfs", log.getLevel()));
        anomaly.setDate(log.getDate());
        anomaly.setTime(log.getTime());
        anomaly.setLevel(log.getLevel());
        anomaly.setContent(log.getContent());
        anomaly.setComponent(log.getComponent());
        return anomaly;
    }

    private ZookeeperLogAnomaly logToZookeeperAnomaly(ZookeeperLog log) {
        ZookeeperLogAnomaly anomaly = new ZookeeperLogAnomaly();
        anomaly.setAnomalyId(generateAnomalyId("zoo", log.getLevel()));
        anomaly.setDate(log.getDate());
        anomaly.setTime(log.getTime());
        anomaly.setLevel(log.getLevel());
        anomaly.setContent(log.getContent());
        anomaly.setComponent(log.getComponent());
        anomaly.setNode(log.getNode());
        return anomaly;
    }

    // generates anomaly ids of type : srctype_level_no
    private String generateAnomalyId(String src, String level) {
        String key = src.toLowerCase() + "_" + level.toLowerCase();
        anomalyCounters.putIfAbsent(key, new AtomicInteger(0));
        int next = anomalyCounters.get(key).incrementAndGet();
        return key+"_"+next;
    }

    public Object getAnomalyById(String anomalyId) {
        try {
            if (anomalyId.startsWith("apache")) {
                return apacheAnomalyRepo.findByAnomalyId(anomalyId);
            } else if (anomalyId.startsWith("hdfs")) {
                return hdfsAnomalyRepo.findByAnomalyId(anomalyId);
            } else {
                return zookeeperAnomalyRepo.findByAnomalyId(anomalyId);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error getting anomaly: " + e.getMessage());
        }
    }
}
