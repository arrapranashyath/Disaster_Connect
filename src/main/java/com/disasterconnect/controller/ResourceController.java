package com.disasterconnect.controller;

import com.disasterconnect.dto.ApiResponse;
import com.disasterconnect.dto.ResourceCreateRequest;
import com.disasterconnect.dto.ResourceResponse;
import com.disasterconnect.dto.ResourceUpdateRequest;
import com.disasterconnect.entity.enums.RequestPriority;
import com.disasterconnect.entity.enums.ResourceStatus;
import com.disasterconnect.entity.enums.ResourceType;
import com.disasterconnect.security.UserPrincipal;
import com.disasterconnect.service.ResourceService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/resources")
public class ResourceController {

    private final ResourceService resourceService;

    public ResourceController(ResourceService resourceService) {
        this.resourceService = resourceService;
    }

    /**
     * POST /api/resources — Verified NGO or Admin records a new resource entry per REQ-RES-1.
     */
    @PostMapping
    public ResponseEntity<ApiResponse<ResourceResponse>> createResource(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody ResourceCreateRequest request) {

        ResourceResponse response = resourceService.createResource(principal, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Relief resource recorded successfully", response));
    }

    /**
     * GET /api/resources — List and filter relief resources per REQ-RES-2.
     * Accessible by authenticated users (NGO, Authority, Admin, Citizen).
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<ResourceResponse>>> searchResources(
            @RequestParam(required = false) ResourceType type,
            @RequestParam(required = false) ResourceStatus status,
            @RequestParam(required = false) RequestPriority priority,
            @RequestParam(required = false) String location,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Page<ResourceResponse> response = resourceService.searchResources(type, status, priority, location, page, size);
        return ResponseEntity.ok(ApiResponse.success("Relief resources retrieved successfully", response));
    }

    /**
     * GET /api/resources/area — View resource status by area to identify shortages per REQ-RES-2.
     * @param area The area/location name
     * @param shortagesOnly If true, filters only LOW and CRITICAL shortages
     */
    @GetMapping("/area")
    public ResponseEntity<ApiResponse<List<ResourceResponse>>> getResourcesByArea(
            @RequestParam String area,
            @RequestParam(defaultValue = "false") boolean shortagesOnly) {

        List<ResourceResponse> response = resourceService.getResourcesByArea(area, shortagesOnly);
        return ResponseEntity.ok(ApiResponse.success("Area resources retrieved successfully", response));
    }

    /**
     * GET /api/resources/{id} — View single resource with update history audit trail per REQ-RES-4.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ResourceResponse>> getResourceById(@PathVariable Long id) {
        ResourceResponse response = resourceService.getResourceById(id);
        return ResponseEntity.ok(ApiResponse.success("Relief resource details retrieved successfully", response));
    }

    /**
     * PUT/PATCH /api/resources/{id} — Update resource quantities and threshold status per REQ-RES-1 and REQ-RES-3.
     */
    @RequestMapping(value = "/{id}", method = {RequestMethod.PUT, RequestMethod.PATCH})
    public ResponseEntity<ApiResponse<ResourceResponse>> updateResource(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody ResourceUpdateRequest request) {

        ResourceResponse response = resourceService.updateResource(id, principal, request);
        return ResponseEntity.ok(ApiResponse.success("Relief resource updated successfully", response));
    }

    /**
     * DELETE /api/resources/{id} — Delete resource entry (owning NGO or Admin).
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteResource(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {

        resourceService.deleteResource(id, principal);
        return ResponseEntity.ok(ApiResponse.success("Relief resource deleted successfully", null));
    }
}
