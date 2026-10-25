package com.disasterconnect.entity;

import com.disasterconnect.entity.enums.AssistanceType;
import com.disasterconnect.entity.enums.RequestPriority;
import com.disasterconnect.entity.enums.RequestStatus;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "assistance_requests")
public class AssistanceRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "request_id")
    private Long requestId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "citizen_id", nullable = false)
    private User citizen;

    @Enumerated(EnumType.STRING)
    @Column(name = "assistance_type", nullable = false, length = 50)
    private AssistanceType assistanceType;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String description;

    @Column(name = "location_name", nullable = false, length = 255)
    private String locationName;

    @Column
    private Double latitude;

    @Column
    private Double longitude;

    @Column(name = "people_count", nullable = false)
    private Integer peopleCount = 1;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RequestPriority priority = RequestPriority.MEDIUM;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RequestStatus status = RequestStatus.PENDING;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to")
    private User assignedTo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_by")
    private User assignedBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public AssistanceRequest() {
    }

    public AssistanceRequest(User citizen, AssistanceType assistanceType, String description,
                             String locationName, Double latitude, Double longitude,
                             Integer peopleCount, RequestPriority priority) {
        this.citizen = citizen;
        this.assistanceType = assistanceType;
        this.description = description;
        this.locationName = locationName;
        this.latitude = latitude;
        this.longitude = longitude;
        this.peopleCount = (peopleCount != null && peopleCount > 0) ? peopleCount : 1;
        this.priority = priority != null ? priority : RequestPriority.MEDIUM;
        this.status = RequestStatus.PENDING;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
        if (this.status == null) {
            this.status = RequestStatus.PENDING;
        }
        if (this.peopleCount == null || this.peopleCount < 1) {
            this.peopleCount = 1;
        }
        if (this.priority == null) {
            this.priority = RequestPriority.MEDIUM;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }

    // Getters and Setters

    public Long getRequestId() {
        return requestId;
    }

    public void setRequestId(Long requestId) {
        this.requestId = requestId;
    }

    public User getCitizen() {
        return citizen;
    }

    public void setCitizen(User citizen) {
        this.citizen = citizen;
    }

    public AssistanceType getAssistanceType() {
        return assistanceType;
    }

    public void setAssistanceType(AssistanceType assistanceType) {
        this.assistanceType = assistanceType;
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

    public Integer getPeopleCount() {
        return peopleCount;
    }

    public void setPeopleCount(Integer peopleCount) {
        this.peopleCount = peopleCount;
    }

    public RequestPriority getPriority() {
        return priority;
    }

    public void setPriority(RequestPriority priority) {
        this.priority = priority;
    }

    public RequestStatus getStatus() {
        return status;
    }

    public void setStatus(RequestStatus status) {
        this.status = status;
    }

    public User getAssignedTo() {
        return assignedTo;
    }

    public void setAssignedTo(User assignedTo) {
        this.assignedTo = assignedTo;
    }

    public User getAssignedBy() {
        return assignedBy;
    }

    public void setAssignedBy(User assignedBy) {
        this.assignedBy = assignedBy;
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
