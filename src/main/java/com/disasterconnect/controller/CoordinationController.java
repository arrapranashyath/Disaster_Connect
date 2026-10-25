package com.disasterconnect.controller;

import com.disasterconnect.dto.ApiResponse;
import com.disasterconnect.dto.AssistanceResponse;
import com.disasterconnect.dto.CoordinationDashboardResponse;
import com.disasterconnect.dto.VolunteerSummaryResponse;
import com.disasterconnect.security.UserPrincipal;
import com.disasterconnect.service.AssistanceService;
import com.disasterconnect.service.CoordinationService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/coordination")
public class CoordinationController {

    private final CoordinationService coordinationService;
    private final AssistanceService assistanceService;

    public CoordinationController(CoordinationService coordinationService,
                                  AssistanceService assistanceService) {
        this.coordinationService = coordinationService;
        this.assistanceService = assistanceService;
    }

    /**
     * GET /api/coordination/dashboard — NGO / Authority / Admin coordination dashboard per REQ-COOR-1.
     */
    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<CoordinationDashboardResponse>> getDashboard(
            @AuthenticationPrincipal UserPrincipal principal) {

        CoordinationDashboardResponse dashboard = coordinationService.getDashboard(principal);
        return ResponseEntity.ok(ApiResponse.success("Coordination dashboard retrieved successfully", dashboard));
    }

    /**
     * GET /api/coordination/volunteers — List verified volunteers available for assignment per REQ-COOR-2.
     */
    @GetMapping("/volunteers")
    public ResponseEntity<ApiResponse<List<VolunteerSummaryResponse>>> getAvailableVolunteers(
            @AuthenticationPrincipal UserPrincipal principal) {

        List<VolunteerSummaryResponse> volunteers = coordinationService.getAvailableVolunteers(principal);
        return ResponseEntity.ok(ApiResponse.success("Available volunteers retrieved successfully", volunteers));
    }

    /**
     * GET /api/coordination/my-tasks — Current volunteer's assigned tasks per REQ-COOR-3.
     */
    @GetMapping("/my-tasks")
    public ResponseEntity<ApiResponse<Page<AssistanceResponse>>> getMyTasks(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Page<AssistanceResponse> tasks = assistanceService.getAssignedToMe(principal, page, size);
        return ResponseEntity.ok(ApiResponse.success("Your coordination tasks retrieved successfully", tasks));
    }
}
