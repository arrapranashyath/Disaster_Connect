package com.disasterconnect.dto;

import com.disasterconnect.entity.enums.RequestPriority;
import com.disasterconnect.entity.enums.ResourceStatus;
import com.disasterconnect.entity.enums.ResourceType;

import java.time.Instant;
import java.util.List;

public class ResourceResponse {

    private Long resourceId;
    private Long ngoId;
    private String organizationName;
    private ResourceType resourceType;
    private Double requiredQuantity;
    private Double availableQuantity;
    private Double deficitQuantity;
    private String unit;
    private String location;
    private RequestPriority priority;
    private ResourceStatus status;
    private Double criticalThresholdPercent;
    private Double lowThresholdPercent;
    private Instant createdAt;
    private Instant updatedAt;
    private List<UpdateLogEntry> updateHistory;

    public static class UpdateLogEntry {
        private Long logId;
        private Long updatedById;
        private String updatedByName;
        private Double previousQuantity;
        private Double newQuantity;
        private ResourceStatus previousStatus;
        private ResourceStatus newStatus;
        private String notes;
        private Instant createdAt;

        public UpdateLogEntry() {
        }

        public UpdateLogEntry(Long logId, Long updatedById, String updatedByName,
                              Double previousQuantity, Double newQuantity,
                              ResourceStatus previousStatus, ResourceStatus newStatus,
                              String notes, Instant createdAt) {
            this.logId = logId;
            this.updatedById = updatedById;
            this.updatedByName = updatedByName;
            this.previousQuantity = previousQuantity;
            this.newQuantity = newQuantity;
            this.previousStatus = previousStatus;
            this.newStatus = newStatus;
            this.notes = notes;
            this.createdAt = createdAt;
        }

        public Long getLogId() {
            return logId;
        }

        public void setLogId(Long logId) {
            this.logId = logId;
        }

        public Long getUpdatedById() {
            return updatedById;
        }

        public void setUpdatedById(Long updatedById) {
            this.updatedById = updatedById;
        }

        public String getUpdatedByName() {
            return updatedByName;
        }

        public void setUpdatedByName(String updatedByName) {
            this.updatedByName = updatedByName;
        }

        public Double getPreviousQuantity() {
            return previousQuantity;
        }

        public void setPreviousQuantity(Double previousQuantity) {
            this.previousQuantity = previousQuantity;
        }

        public Double getNewQuantity() {
            return newQuantity;
        }

        public void setNewQuantity(Double newQuantity) {
            this.newQuantity = newQuantity;
        }

        public ResourceStatus getPreviousStatus() {
            return previousStatus;
        }

        public void setPreviousStatus(ResourceStatus previousStatus) {
            this.previousStatus = previousStatus;
        }

        public ResourceStatus getNewStatus() {
            return newStatus;
        }

        public void setNewStatus(ResourceStatus newStatus) {
            this.newStatus = newStatus;
        }

        public String getNotes() {
            return notes;
        }

        public void setNotes(String notes) {
            this.notes = notes;
        }

        public Instant getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(Instant createdAt) {
            this.createdAt = createdAt;
        }
    }

    public ResourceResponse() {
    }

    public Long getResourceId() {
        return resourceId;
    }

    public void setResourceId(Long resourceId) {
        this.resourceId = resourceId;
    }

    public Long getNgoId() {
        return ngoId;
    }

    public void setNgoId(Long ngoId) {
        this.ngoId = ngoId;
    }

    public String getOrganizationName() {
        return organizationName;
    }

    public void setOrganizationName(String organizationName) {
        this.organizationName = organizationName;
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

    public Double getDeficitQuantity() {
        return deficitQuantity;
    }

    public void setDeficitQuantity(Double deficitQuantity) {
        this.deficitQuantity = deficitQuantity;
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

    public ResourceStatus getStatus() {
        return status;
    }

    public void setStatus(ResourceStatus status) {
        this.status = status;
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

    public List<UpdateLogEntry> getUpdateHistory() {
        return updateHistory;
    }

    public void setUpdateHistory(List<UpdateLogEntry> updateHistory) {
        this.updateHistory = updateHistory;
    }
}
