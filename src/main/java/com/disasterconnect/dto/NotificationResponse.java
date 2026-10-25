package com.disasterconnect.dto;

import com.disasterconnect.entity.Notification;
import com.disasterconnect.entity.enums.NotificationType;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;

public class NotificationResponse {

    private Long notificationId;
    private Long userId;
    private NotificationType type;
    private String title;
    private String message;
    private String relatedEntityType;
    private Long relatedEntityId;

    @JsonProperty("isRead")
    private boolean isRead;

    private Instant createdAt;

    public NotificationResponse() {
    }

    public NotificationResponse(Long notificationId, Long userId, NotificationType type,
                                String title, String message, String relatedEntityType,
                                Long relatedEntityId, boolean isRead, Instant createdAt) {
        this.notificationId = notificationId;
        this.userId = userId;
        this.type = type;
        this.title = title;
        this.message = message;
        this.relatedEntityType = relatedEntityType;
        this.relatedEntityId = relatedEntityId;
        this.isRead = isRead;
        this.createdAt = createdAt;
    }

    public static NotificationResponse fromEntity(Notification notification) {
        if (notification == null) {
            return null;
        }

        Long userId = notification.getUser() != null ? notification.getUser().getUserId() : null;
        return new NotificationResponse(
                notification.getNotificationId(),
                userId,
                notification.getType(),
                notification.getTitle(),
                notification.getMessage(),
                notification.getRelatedEntityType(),
                notification.getRelatedEntityId(),
                notification.isRead(),
                notification.getCreatedAt()
        );
    }

    public Long getNotificationId() {
        return notificationId;
    }

    public void setNotificationId(Long notificationId) {
        this.notificationId = notificationId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public NotificationType getType() {
        return type;
    }

    public void setType(NotificationType type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getRelatedEntityType() {
        return relatedEntityType;
    }

    public void setRelatedEntityType(String relatedEntityType) {
        this.relatedEntityType = relatedEntityType;
    }

    public Long getRelatedEntityId() {
        return relatedEntityId;
    }

    public void setRelatedEntityId(Long relatedEntityId) {
        this.relatedEntityId = relatedEntityId;
    }

    @JsonProperty("isRead")
    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
