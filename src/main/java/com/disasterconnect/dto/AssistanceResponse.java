package com.disasterconnect.dto;

import com.disasterconnect.entity.enums.AssistanceType;
import com.disasterconnect.entity.enums.RequestPriority;
import com.disasterconnect.entity.enums.RequestStatus;

import java.time.Instant;
import java.util.List;

public class AssistanceResponse {

    private Long requestId;
    private UserSummary citizen;
    private AssistanceType assistanceType;
    private String description;
    private String locationName;
    private Double latitude;
    private Double longitude;
    private Integer peopleCount;
    private RequestPriority priority;
    private RequestStatus status;
    private UserSummary assignedTo;
    private UserSummary assignedBy;
    private Instant createdAt;
    private Instant updatedAt;
    private List<StatusLogEntry> statusHistory;

    public AssistanceResponse() {
    }

    public static class UserSummary {
        private Long userId;
        private String name;
        private String role;
        private String phone;

        public UserSummary() {
        }

        public UserSummary(Long userId, String name, String role, String phone) {
            this.userId = userId;
            this.name = name;
            this.role = role;
            this.phone = phone;
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

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }
    }

    public static class StatusLogEntry {
        private Long logId;
        private String changedByName;
        private RequestStatus previousStatus;
        private RequestStatus newStatus;
        private String notes;
        private Instant createdAt;

        public StatusLogEntry() {
        }

        public StatusLogEntry(Long logId, String changedByName, RequestStatus previousStatus,
                              RequestStatus newStatus, String notes, Instant createdAt) {
            this.logId = logId;
            this.changedByName = changedByName;
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

        public String getChangedByName() {
            return changedByName;
        }

        public void setChangedByName(String changedByName) {
            this.changedByName = changedByName;
        }

        public RequestStatus getPreviousStatus() {
            return previousStatus;
        }

        public void setPreviousStatus(RequestStatus previousStatus) {
            this.previousStatus = previousStatus;
        }

        public RequestStatus getNewStatus() {
            return newStatus;
        }

        public void setNewStatus(RequestStatus newStatus) {
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

    public Long getRequestId() {
        return requestId;
    }

    public void setRequestId(Long requestId) {
        this.requestId = requestId;
    }

    public UserSummary getCitizen() {
        return citizen;
    }

    public void setCitizen(UserSummary citizen) {
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

    public UserSummary getAssignedTo() {
        return assignedTo;
    }

    public void setAssignedTo(UserSummary assignedTo) {
        this.assignedTo = assignedTo;
    }

    public UserSummary getAssignedBy() {
        return assignedBy;
    }

    public void setAssignedBy(UserSummary assignedBy) {
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

    public List<StatusLogEntry> getStatusHistory() {
        return statusHistory;
    }

    public void setStatusHistory(List<StatusLogEntry> statusHistory) {
        this.statusHistory = statusHistory;
    }
}
