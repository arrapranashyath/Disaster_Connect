package com.disasterconnect.entity;

import com.disasterconnect.entity.enums.AlertSeverity;
import com.disasterconnect.entity.enums.AlertStatus;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "disaster_alerts")
public class DisasterAlert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "alert_id")
    private Long alertId;

    @Column(name = "disaster_type", nullable = false, length = 50)
    private String disasterType;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String description;

    @Column(name = "affected_area", nullable = false, length = 150)
    private String affectedArea;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AlertSeverity severity;

    @Column(name = "safety_instructions", columnDefinition = "TEXT")
    private String safetyInstructions;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AlertStatus status = AlertStatus.ACTIVE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "issued_by", nullable = false)
    private User issuedBy;

    @Column(name = "issued_at", nullable = false, updatable = false)
    private Instant issuedAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public DisasterAlert() {
    }

    public DisasterAlert(String disasterType, String title, String description, String affectedArea,
                         AlertSeverity severity, String safetyInstructions, AlertStatus status, User issuedBy) {
        this.disasterType = disasterType;
        this.title = title;
        this.description = description;
        this.affectedArea = affectedArea;
        this.severity = severity;
        this.safetyInstructions = safetyInstructions;
        this.status = status != null ? status : AlertStatus.ACTIVE;
        this.issuedBy = issuedBy;
    }

    @PrePersist
    protected void onCreate() {
        this.issuedAt = Instant.now();
        this.updatedAt = Instant.now();
        if (this.status == null) {
            this.status = AlertStatus.ACTIVE;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }

    // Getters and Setters
    public Long getAlertId() {
        return alertId;
    }

    public void setAlertId(Long alertId) {
        this.alertId = alertId;
    }

    public String getDisasterType() {
        return disasterType;
    }

    public void setDisasterType(String disasterType) {
        this.disasterType = disasterType;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAffectedArea() {
        return affectedArea;
    }

    public void setAffectedArea(String affectedArea) {
        this.affectedArea = affectedArea;
    }

    public AlertSeverity getSeverity() {
        return severity;
    }

    public void setSeverity(AlertSeverity severity) {
        this.severity = severity;
    }

    public String getSafetyInstructions() {
        return safetyInstructions;
    }

    public void setSafetyInstructions(String safetyInstructions) {
        this.safetyInstructions = safetyInstructions;
    }

    public AlertStatus getStatus() {
        return status;
    }

    public void setStatus(AlertStatus status) {
        this.status = status;
    }

    public User getIssuedBy() {
        return issuedBy;
    }

    public void setIssuedBy(User issuedBy) {
        this.issuedBy = issuedBy;
    }

    public Instant getIssuedAt() {
        return issuedAt;
    }

    public void setIssuedAt(Instant issuedAt) {
        this.issuedAt = issuedAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
