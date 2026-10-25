package com.disasterconnect.dto;

import com.disasterconnect.entity.enums.AlertSeverity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class AlertCreateRequest {

    @NotBlank(message = "Disaster type is required")
    @Size(max = 50, message = "Disaster type must not exceed 50 characters")
    private String disasterType;

    @NotBlank(message = "Title is required")
    @Size(max = 150, message = "Title must not exceed 150 characters")
    private String title;

    @NotBlank(message = "Description is required")
    private String description;

    @NotBlank(message = "Affected area is required")
    @Size(max = 150, message = "Affected area must not exceed 150 characters")
    private String affectedArea;

    @NotNull(message = "Severity is required")
    private AlertSeverity severity;

    private String safetyInstructions;

    public AlertCreateRequest() {
    }

    public AlertCreateRequest(String disasterType, String title, String description,
                              String affectedArea, AlertSeverity severity, String safetyInstructions) {
        this.disasterType = disasterType;
        this.title = title;
        this.description = description;
        this.affectedArea = affectedArea;
        this.severity = severity;
        this.safetyInstructions = safetyInstructions;
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
}
