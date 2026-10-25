package com.disasterconnect.dto;

import com.disasterconnect.entity.enums.RequestPriority;
import com.disasterconnect.entity.enums.ResourceType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;

public class ResourceUpdateRequest {

    private ResourceType resourceType;

    @DecimalMin(value = "0.01", message = "Required quantity must be greater than 0")
    private Double requiredQuantity;

    @DecimalMin(value = "0.0", message = "Available quantity cannot be negative")
    private Double availableQuantity;

    @Size(max = 30, message = "Unit cannot exceed 30 characters")
    private String unit;

    @Size(max = 255, message = "Location cannot exceed 255 characters")
    private String location;

    private RequestPriority priority;

    private Double criticalThresholdPercent;

    private Double lowThresholdPercent;

    @Size(max = 255, message = "Notes cannot exceed 255 characters")
    private String notes;

    public ResourceUpdateRequest() {
    }

    public ResourceType getResourceType() {
        return resourceType;
    }

    public void setResourceType(ResourceType resourceType) {
        this.resourceType = resourceType;
    }

    public Double getRequiredQuantity() {
        return requiredQuantity;
    }

    public void setRequiredQuantity(Double requiredQuantity) {
        this.requiredQuantity = requiredQuantity;
    }

    public Double getAvailableQuantity() {
        return availableQuantity;
    }

    public void setAvailableQuantity(Double availableQuantity) {
        this.availableQuantity = availableQuantity;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public RequestPriority getPriority() {
        return priority;
    }

    public void setPriority(RequestPriority priority) {
        this.priority = priority;
    }

    public Double getCriticalThresholdPercent() {
        return criticalThresholdPercent;
    }

    public void setCriticalThresholdPercent(Double criticalThresholdPercent) {
        this.criticalThresholdPercent = criticalThresholdPercent;
    }

    public Double getLowThresholdPercent() {
        return lowThresholdPercent;
    }

    public void setLowThresholdPercent(Double lowThresholdPercent) {
        this.lowThresholdPercent = lowThresholdPercent;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
