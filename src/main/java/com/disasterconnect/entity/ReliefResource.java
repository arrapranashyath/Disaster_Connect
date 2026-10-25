package com.disasterconnect.entity;

import com.disasterconnect.entity.enums.RequestPriority;
import com.disasterconnect.entity.enums.ResourceStatus;
import com.disasterconnect.entity.enums.ResourceType;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "relief_resources")
public class ReliefResource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "resource_id")
    private Long resourceId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ngo_id")
    private NGOProfile providedBy;

    @Enumerated(EnumType.STRING)
    @Column(name = "resource_type", nullable = false, length = 50)
    private ResourceType resourceType;

    @Column(name = "required_quantity", nullable = false)
    private Double requiredQuantity;

    @Column(name = "available_quantity", nullable = false)
    private Double availableQuantity;

    @Column(name = "unit", nullable = false, length = 30)
    private String unit = "units";

    @Column(name = "location", nullable = false, length = 255)
    private String location;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false, length = 20)
    private RequestPriority priority = RequestPriority.MEDIUM;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ResourceStatus status = ResourceStatus.AVAILABLE;

    @Column(name = "critical_threshold_percent", nullable = false)
    private Double criticalThresholdPercent = 25.0;

    @Column(name = "low_threshold_percent", nullable = false)
    private Double lowThresholdPercent = 50.0;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public ReliefResource() {
    }

    public ReliefResource(NGOProfile providedBy, ResourceType resourceType,
                          Double requiredQuantity, Double availableQuantity, String unit,
                          String location, RequestPriority priority,
                          Double criticalThresholdPercent, Double lowThresholdPercent) {
        this.providedBy = providedBy;
        this.resourceType = resourceType;
        this.requiredQuantity = requiredQuantity;
        this.availableQuantity = availableQuantity;
        this.unit = (unit != null && !unit.isBlank()) ? unit : "units";
        this.location = location;
        this.priority = priority != null ? priority : RequestPriority.MEDIUM;
        this.criticalThresholdPercent = (criticalThresholdPercent != null && criticalThresholdPercent > 0)
                ? criticalThresholdPercent : 25.0;
        this.lowThresholdPercent = (lowThresholdPercent != null && lowThresholdPercent > 0)
                ? lowThresholdPercent : 50.0;
        recalculateStatus();
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
        recalculateStatus();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
        recalculateStatus();
    }

    /**
     * Flags a resource as CRITICAL or LOW when available quantity falls below
     * the configured threshold relative to required quantity per REQ-RES-3.
     */
    public void recalculateStatus() {
        if (requiredQuantity == null || requiredQuantity <= 0) {
            this.status = ResourceStatus.AVAILABLE;
            return;
        }

        double available = availableQuantity != null ? availableQuantity : 0.0;
        double ratioPercent = (available / requiredQuantity) * 100.0;

        double critThresh = (criticalThresholdPercent != null) ? criticalThresholdPercent : 25.0;
        double lowThresh = (lowThresholdPercent != null) ? lowThresholdPercent : 50.0;

        if (ratioPercent <= critThresh) {
            this.status = ResourceStatus.CRITICAL;
        } else if (ratioPercent <= lowThresh) {
            this.status = ResourceStatus.LOW;
        } else {
            this.status = ResourceStatus.AVAILABLE;
        }
    }

    // Getters and Setters

    public Long getResourceId() {
        return resourceId;
    }

    public void setResourceId(Long resourceId) {
        this.resourceId = resourceId;
    }

    public NGOProfile getProvidedBy() {
        return providedBy;
    }

    public void setProvidedBy(NGOProfile providedBy) {
        this.providedBy = providedBy;
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
        recalculateStatus();
    }

    public Double getAvailableQuantity() {
        return availableQuantity;
    }

    public void setAvailableQuantity(Double availableQuantity) {
        this.availableQuantity = availableQuantity;
        recalculateStatus();
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
        recalculateStatus();
    }

    public Double getLowThresholdPercent() {
        return lowThresholdPercent;
    }

    public void setLowThresholdPercent(Double lowThresholdPercent) {
        this.lowThresholdPercent = lowThresholdPercent;
        recalculateStatus();
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
