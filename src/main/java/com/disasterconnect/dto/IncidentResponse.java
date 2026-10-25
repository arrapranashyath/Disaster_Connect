package com.disasterconnect.dto;

import com.disasterconnect.entity.enums.AlertSeverity;
import com.disasterconnect.entity.enums.IncidentStatus;

import java.time.Instant;

public class IncidentResponse {

    private Long incidentId;
    private ReportedByInfo reportedBy;
    private String incidentType;
    private String description;
    private String locationName;
    private Double latitude;
    private Double longitude;
    private AlertSeverity severity;
    private IncidentStatus status;
    private String imageUrl;
    private Instant createdAt;
    private Instant updatedAt;

    public IncidentResponse() {
    }

    /** Slim nested object — exposes only safe user fields, never the password hash. */
    public static class ReportedByInfo {
        private Long userId;
        private String name;
        private String role;

        public ReportedByInfo() {
        }

        public ReportedByInfo(Long userId, String name, String role) {
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

    public Long getIncidentId() {
        return incidentId;
    }

    public void setIncidentId(Long incidentId) {
        this.incidentId = incidentId;
    }

    public ReportedByInfo getReportedBy() {
        return reportedBy;
    }

    public void setReportedBy(ReportedByInfo reportedBy) {
        this.reportedBy = reportedBy;
    }

    public String getIncidentType() {
        return incidentType;
    }

    public void setIncidentType(String incidentType) {
        this.incidentType = incidentType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public AlertSeverity getSeverity() {
        return severity;
    }

    public void setSeverity(AlertSeverity severity) {
        this.severity = severity;
    }

    public IncidentStatus getStatus() {
        return status;
    }

    public void setStatus(IncidentStatus status) {
        this.status = status;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
