package com.disasterconnect.entity;

import com.disasterconnect.entity.enums.ResourceStatus;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "resource_update_logs")
public class ResourceUpdateLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "log_id")
    private Long logId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resource_id", nullable = false)
    private ReliefResource resource;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by", nullable = false)
    private User updatedBy;

    @Column(name = "previous_quantity")
    private Double previousQuantity;

    @Column(name = "new_quantity", nullable = false)
    private Double newQuantity;

    @Enumerated(EnumType.STRING)
    @Column(name = "previous_status", length = 20)
    private ResourceStatus previousStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "new_status", nullable = false, length = 20)
    private ResourceStatus newStatus;

    @Column(name = "notes", length = 255)
    private String notes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public ResourceUpdateLog() {
    }

    public ResourceUpdateLog(ReliefResource resource, User updatedBy, Double previousQuantity,
                             Double newQuantity, ResourceStatus previousStatus,
                             ResourceStatus newStatus, String notes) {
        this.resource = resource;
        this.updatedBy = updatedBy;
        this.previousQuantity = previousQuantity;
        this.newQuantity = newQuantity;
        this.previousStatus = previousStatus;
        this.newStatus = newStatus;
        this.notes = notes;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
    }

    // Getters and Setters

    public Long getLogId() {
        return logId;
    }

    public void setLogId(Long logId) {
        this.logId = logId;
    }

    public ReliefResource getResource() {
        return resource;
    }

    public void setResource(ReliefResource resource) {
        this.resource = resource;
    }

    public User getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(User updatedBy) {
        this.updatedBy = updatedBy;
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
