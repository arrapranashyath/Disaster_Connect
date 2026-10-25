package com.disasterconnect.controller;

import com.disasterconnect.dto.ApiResponse;
import com.disasterconnect.dto.MissingPersonReportRequest;
import com.disasterconnect.dto.MissingPersonResponse;
import com.disasterconnect.dto.MissingPersonStatusUpdateRequest;
import com.disasterconnect.entity.enums.MissingPersonStatus;
import com.disasterconnect.security.UserPrincipal;
import com.disasterconnect.service.MissingPersonService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/missing-persons")
public class MissingPersonController {

    private final MissingPersonService missingPersonService;

    public MissingPersonController(MissingPersonService missingPersonService) {
        this.missingPersonService = missingPersonService;
    }

    /**
     * POST /api/missing-persons — Report a missing person (REQ-MISS-1)
     */
    @PostMapping
    public ResponseEntity<ApiResponse<MissingPersonResponse>> reportMissingPerson(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody MissingPersonReportRequest request) {

        MissingPersonResponse response = missingPersonService.reportMissingPerson(principal, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Missing person report created successfully", response));
    }

    /**
     * GET /api/missing-persons & GET /api/missing-persons/search — Search and discover missing person records (REQ-MISS-2, REQ-MISS-4, DDS Section 5)
     * Contact details masked for unauthenticated callers per REQ-MISS-4.
     */
    @GetMapping({"", "/search"})
    public ResponseEntity<ApiResponse<Page<MissingPersonResponse>>> searchMissingPersons(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) MissingPersonStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal UserPrincipal principal) {

        boolean isAuthenticated = (principal != null);
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<MissingPersonResponse> response = missingPersonService.searchMissingPersons(query, status, isAuthenticated, pageable);
        return ResponseEntity.ok(ApiResponse.success("Missing person records retrieved successfully", response));
    }

    /**
     * GET /api/missing-persons/{id} — View single missing person record
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MissingPersonResponse>> getMissingPersonById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {

        boolean isAuthenticated = (principal != null);
        MissingPersonResponse response = missingPersonService.getMissingPersonById(id, isAuthenticated);
        return ResponseEntity.ok(ApiResponse.success("Missing person record retrieved successfully", response));
    }

    /**
     * PATCH /api/missing-persons/{id}/status — Update status (REQ-MISS-3)
     * Allowed only for the original reporter, Authority, or Admin.
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<MissingPersonResponse>> updateStatus(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody MissingPersonStatusUpdateRequest request) {

        MissingPersonResponse response = missingPersonService.updateStatus(id, request, principal);
        return ResponseEntity.ok(ApiResponse.success("Missing person status updated successfully", response));
    }
}
