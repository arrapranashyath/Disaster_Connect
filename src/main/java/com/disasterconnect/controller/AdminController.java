package com.disasterconnect.controller;

import com.disasterconnect.dto.*;
import com.disasterconnect.entity.enums.VerificationStatus;
import com.disasterconnect.security.UserPrincipal;
import com.disasterconnect.service.AdminService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    /**
     * GET /api/admin/ngos/pending — Verification Queue for NGOs (REQ-ADMIN-1).
     */
    @GetMapping("/ngos/pending")
    public ResponseEntity<ApiResponse<Page<NgoVerificationResponse>>> getPendingNgos(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "createdAt"));
        Page<NgoVerificationResponse> response = adminService.getPendingNgos(principal, pageable);
        return ResponseEntity.ok(ApiResponse.success("Pending NGOs retrieved successfully", response));
    }

    /**
     * PATCH /api/admin/ngos/{id}/verify — Approve or reject an NGO profile (REQ-ADMIN-1).
     */
    @PatchMapping("/ngos/{id}/verify")
    public ResponseEntity<ApiResponse<NgoVerificationResponse>> verifyNgo(
            @PathVariable Long id,
            @RequestParam VerificationStatus status,
            @AuthenticationPrincipal UserPrincipal principal) {

        NgoVerificationResponse response = adminService.verifyNgo(id, status, principal);
        return ResponseEntity.ok(ApiResponse.success("NGO verification status updated", response));
    }

    /**
     * GET /api/admin/volunteers/pending — Verification Queue for Volunteers.
     */
    @GetMapping("/volunteers/pending")
    public ResponseEntity<ApiResponse<Page<VolunteerSummaryResponse>>> getPendingVolunteers(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "createdAt"));
        Page<VolunteerSummaryResponse> response = adminService.getPendingVolunteers(principal, pageable);
        return ResponseEntity.ok(ApiResponse.success("Pending volunteers retrieved successfully", response));
    }

    /**
     * PATCH /api/admin/volunteers/{id}/verify — Approve or reject a volunteer profile.
     */
    @PatchMapping("/volunteers/{id}/verify")
    public ResponseEntity<ApiResponse<VolunteerSummaryResponse>> verifyVolunteer(
            @PathVariable Long id,
            @RequestParam VerificationStatus status,
            @AuthenticationPrincipal UserPrincipal principal) {

        VolunteerSummaryResponse response = adminService.verifyVolunteer(id, status, principal);
        return ResponseEntity.ok(ApiResponse.success("Volunteer verification status updated", response));
    }

    /**
     * PATCH /api/admin/users/{id}/suspend — Suspend or unsuspend user account (REQ-ADMIN-3).
     */
    @PatchMapping("/users/{id}/suspend")
    public ResponseEntity<ApiResponse<UserResponse>> suspendUser(
            @PathVariable Long id,
            @Valid @RequestBody UserSuspendRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {

        UserResponse response = adminService.suspendUser(id, request, principal);
        return ResponseEntity.ok(ApiResponse.success("User account status updated", response));
    }

    /**
     * GET /api/admin/summary — Overall system dashboard metrics (REQ-ADMIN-2).
     */
    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<AdminSummaryResponse>> getSystemSummary(
            @AuthenticationPrincipal UserPrincipal principal) {

        AdminSummaryResponse response = adminService.getSystemSummary(principal);
        return ResponseEntity.ok(ApiResponse.success("System summary retrieved successfully", response));
    }

    /**
     * GET /api/admin/audit-logs (and /api/admin/audit-log) — Administrative action audit logs (REQ-ADMIN-4, DDS Section 5).
     */
    @GetMapping({"/audit-logs", "/audit-log"})
    public ResponseEntity<ApiResponse<Page<AdminActionLogResponse>>> getAuditLogs(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<AdminActionLogResponse> response = adminService.getAuditLogs(principal, pageable);
        return ResponseEntity.ok(ApiResponse.success("Audit logs retrieved successfully", response));
    }
}
