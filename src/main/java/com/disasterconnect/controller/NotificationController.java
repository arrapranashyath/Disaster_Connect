package com.disasterconnect.controller;

import com.disasterconnect.dto.ApiResponse;
import com.disasterconnect.dto.NotificationResponse;
import com.disasterconnect.security.UserPrincipal;
import com.disasterconnect.service.NotificationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    /**
     * GET /api/notifications — Retrieve in-app notifications (REQ-NOTIF-4).
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<NotificationResponse>>> getUserNotifications(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) Boolean unreadOnly,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<NotificationResponse> response = notificationService.getUserNotifications(principal, unreadOnly, pageable);
        return ResponseEntity.ok(ApiResponse.success("Notifications retrieved successfully", response));
    }

    /**
     * GET /api/notifications/unread-count — Count of unread notifications for badge display.
     */
    @GetMapping("/unread-count")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getUnreadCount(
            @AuthenticationPrincipal UserPrincipal principal) {

        long count = notificationService.getUnreadCount(principal);
        return ResponseEntity.ok(ApiResponse.success("Unread notification count retrieved", Map.of("unreadCount", count)));
    }

    /**
     * PATCH /api/notifications/{id}/read — Mark single notification as read (REQ-NOTIF-4).
     */
    @PatchMapping("/{id}/read")
    public ResponseEntity<ApiResponse<NotificationResponse>> markAsRead(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {

        NotificationResponse response = notificationService.markAsRead(id, principal);
        return ResponseEntity.ok(ApiResponse.success("Notification marked as read", response));
    }

    /**
     * PATCH /api/notifications/read-all — Mark all notifications as read.
     */
    @PatchMapping("/read-all")
    public ResponseEntity<ApiResponse<Void>> markAllAsRead(
            @AuthenticationPrincipal UserPrincipal principal) {

        notificationService.markAllAsRead(principal);
        return ResponseEntity.ok(ApiResponse.success("All notifications marked as read", null));
    }
}
