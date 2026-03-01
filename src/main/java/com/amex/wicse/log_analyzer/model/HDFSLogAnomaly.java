package com.amex.wicse.log_analyzer.model;

import jakarta.persistence.*;

@Entity
@Table(name = "hdfs_anomalies")
public class HDFSLogAnomaly {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "anomaly_id", unique = true, nullable = false)
    private String anomalyId;
    @Column(name = "level", columnDefinition = "text")
    private String level;
    @Column(name = "component", columnDefinition = "text")
    private String component;
    @Column(name = "content", columnDefinition = "text")
    private String content;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAnomalyId() {
        return anomalyId;
    }

    public void setAnomalyId(String anomalyId) {
        this.anomalyId = anomalyId;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getComponent() {
        return component;
    }

    public void setComponent(String component) {
        this.component = component;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

}
