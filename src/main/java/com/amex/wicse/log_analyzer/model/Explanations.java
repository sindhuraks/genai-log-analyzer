package com.amex.wicse.log_analyzer.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "explanations", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"anomaly_id", "model"})
})
public class Explanations {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "anomaly_id", nullable = false)
    private String anomalyId;
    @Column(name = "model", columnDefinition = "text", nullable = false)
    private String model;
    @Column(name = "explanation", columnDefinition = "text")
    private String explanation;
    @Column(name = "created_at", columnDefinition = "TIMESTAMP", nullable = false)
    private LocalDateTime createdAt;

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

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
