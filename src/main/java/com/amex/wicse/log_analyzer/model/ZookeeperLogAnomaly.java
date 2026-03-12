package com.amex.wicse.log_analyzer.model;

import jakarta.persistence.*;

@Entity
@Table(name = "zookeeper_anomalies")
public class ZookeeperLogAnomaly {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "anomaly_id", unique = true, nullable = false)
    private String anomalyId;
    @Column(name = "date")
    private String date;
    @Column(name = "time")
    private String time;
    @Column(name = "level", columnDefinition = "text")
    private String level;
    @Column(name = "node", columnDefinition = "text")
    private String node;

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

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getNode() {
        return node;
    }

    public void setNode(String node) {
        this.node = node;
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

    @Column(name = "component", columnDefinition = "text")
    private String component;
    @Column(name = "content", columnDefinition = "text")
    private String content;
}
