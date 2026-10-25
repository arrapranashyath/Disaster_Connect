package com.disasterconnect.dto;

import com.disasterconnect.entity.AdminActionLog;

import java.time.Instant;

public class AdminActionLogResponse {

    private Long logId;
    private Long adminId;
    private String adminName;
    private String action;
    private String targetType;
    private Long targetId;
    private String details;
    private Instant createdAt;

    public AdminActionLogResponse() {
    }

    public AdminActionLogResponse(Long logId, Long adminId, String adminName, String action,
                                  String targetType, Long targetId, String details, Instant createdAt) {
        this.logId = logId;
        this.adminId = adminId;
        this.adminName = adminName;
        this.action = action;
        this.targetType = targetType;
        this.targetId = targetId;
        this.details = details;
        this.createdAt = createdAt;
    }

    public static AdminActionLogResponse fromEntity(AdminActionLog log) {
        if (log == null) {
            return null;
        }

        Long adminId = log.getAdmin() != null ? log.getAdmin().getUserId() : null;
        String adminName = log.getAdmin() != null ? log.getAdmin().getName() : null;

        return new AdminActionLogResponse(
                log.getLogId(),
                adminId,
                adminName,
                log.getAction(),
                log.getTargetType(),
                log.getTargetId(),
                log.getDetails(),
                log.getCreatedAt()
        );
    }

    public Long getLogId() {
        return logId;
    }

    public void setLogId(Long logId) {
        this.logId = logId;
    }

    public Long getAdminId() {
        return adminId;
    }

    public void setAdminId(Long adminId) {
        this.adminId = adminId;
    }

    public String getAdminName() {
        return adminName;
    }

    public void setAdminName(String adminName) {
        this.adminName = adminName;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getTargetType() {
        return targetType;
    }

    public void setTargetType(String targetType) {
        this.targetType = targetType;
    }

    public Long getTargetId() {
        return targetId;
    }

    public void setTargetId(Long targetId) {
        this.targetId = targetId;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
