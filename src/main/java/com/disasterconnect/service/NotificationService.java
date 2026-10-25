package com.disasterconnect.service;

import com.disasterconnect.dto.NotificationResponse;
import com.disasterconnect.entity.Notification;
import com.disasterconnect.entity.User;
import com.disasterconnect.entity.enums.NotificationType;
import com.disasterconnect.exception.ResourceNotFoundException;
import com.disasterconnect.repository.NotificationRepository;
import com.disasterconnect.repository.UserRepository;
import com.disasterconnect.security.UserPrincipal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public NotificationService(NotificationRepository notificationRepository,
                               UserRepository userRepository) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

    /**
     * REQ-NOTIF-4: Retrieve user's in-app notifications with optional unread filter.
     */
    @Transactional(readOnly = true)
    public Page<NotificationResponse> getUserNotifications(UserPrincipal principal,
                                                          Boolean unreadOnly,
                                                          Pageable pageable) {
        User user = userRepository.findById(principal.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + principal.getUserId()));

        if (Boolean.TRUE.equals(unreadOnly)) {
            return notificationRepository.findByUserAndIsReadOrderByCreatedAtDesc(user, false, pageable)
                    .map(NotificationResponse::fromEntity);
        }
        return notificationRepository.findByUserOrderByCreatedAtDesc(user, pageable)
                .map(NotificationResponse::fromEntity);
    }

    /**
     * Get count of unread notifications for badge display.
     */
    @Transactional(readOnly = true)
    public long getUnreadCount(UserPrincipal principal) {
        User user = userRepository.findById(principal.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + principal.getUserId()));
        return notificationRepository.countByUserAndIsReadFalse(user);
    }

    /**
     * REQ-NOTIF-4: Mark a single notification as read.
     */
    @Transactional
    public NotificationResponse markAsRead(Long notificationId, UserPrincipal principal) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with id: " + notificationId));

        if (!notification.getUser().getUserId().equals(principal.getUserId())) {
            throw new AccessDeniedException("You are not authorized to access this notification");
        }

        notification.setRead(true);
        Notification updated = notificationRepository.save(notification);
        return NotificationResponse.fromEntity(updated);
    }

    /**
     * Mark all notifications of current user as read.
     */
    @Transactional
    public void markAllAsRead(UserPrincipal principal) {
        User user = userRepository.findById(principal.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + principal.getUserId()));

        List<Notification> unreadList = notificationRepository.findByUserOrderByCreatedAtDesc(user);
        for (Notification n : unreadList) {
            if (!n.isRead()) {
                n.setRead(true);
            }
        }
        notificationRepository.saveAll(unreadList);
    }

    /**
     * Create a notification directly for a recipient.
     */
    @Transactional
    public Notification createNotification(User recipient, NotificationType type,
                                          String title, String message,
                                          String relatedEntityType, Long relatedEntityId) {
        Notification notification = new Notification(
                recipient, type, title, message, relatedEntityType, relatedEntityId
        );
        return notificationRepository.save(notification);
    }

    /**
     * REQ-NOTIF-1: In-app notifications when an alert is created for affected users.
     */
    @Transactional
    public void notifyAlertUsers(String affectedArea, String title, String message, Long alertId) {
        List<User> users = userRepository.findAll();
        for (User user : users) {
            // Match area if user location is specified or alert is general
            boolean matches = user.getLocation() == null ||
                    affectedArea == null ||
                    affectedArea.isBlank() ||
                    user.getLocation().toLowerCase().contains(affectedArea.toLowerCase()) ||
                    affectedArea.toLowerCase().contains(user.getLocation().toLowerCase());

            if (matches) {
                createNotification(user, NotificationType.ALERT, title, message, "DISASTER_ALERT", alertId);
            }
        }
    }

    /**
     * REQ-NOTIF-2: In-app notifications when an assistance request changes status.
     */
    @Transactional
    public void notifyHelpRequestStatusChange(User citizen, String title, String message, Long requestId) {
        if (citizen != null) {
            createNotification(citizen, NotificationType.HELP_REQUEST, title, message, "ASSISTANCE_REQUEST", requestId);
        }
    }

    /**
     * REQ-NOTIF-3: In-app notifications when a missing person record status changes.
     */
    @Transactional
    public void notifyMissingPersonStatusChange(User reporter, String title, String message, Long missingId) {
        if (reporter != null) {
            createNotification(reporter, NotificationType.MISSING_PERSON, title, message, "MISSING_PERSON", missingId);
        }
    }
}
