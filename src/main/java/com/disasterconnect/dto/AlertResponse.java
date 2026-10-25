package com.disasterconnect.dto;

import com.disasterconnect.entity.enums.AlertSeverity;
import com.disasterconnect.entity.enums.AlertStatus;

import java.time.Instant;

public class AlertResponse {

    private Long alertId;
    private String disasterType;
    private String title;
    private String description;
    private String affectedArea;
    private AlertSeverity severity;
    private String safetyInstructions;
    private AlertStatus status;
    private IssuedByInfo issuedBy;
    private Instant issuedAt;
    private Instant updatedAt;

    public AlertResponse() {
    }

    /** Slim nested object — exposes only safe user fields, never the password hash. */
    public static class IssuedByInfo {
        private Long userId;
        private String name;
        private String role;

        public IssuedByInfo() {
        }

        public IssuedByInfo(Long userId, String name, String role) {
            this.userId = userId;
            this.name = name;
            this.role = role;
        }

        public Long getUserId() {
            return userId;
        }

        public void setUserId(Long userId) {
            this.userId = userId;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }
    }

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

    public IssuedByInfo getIssuedBy() {
        return issuedBy;
    }

    public void setIssuedBy(IssuedByInfo issuedBy) {
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
