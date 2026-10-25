package com.disasterconnect.controller;

import com.disasterconnect.dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test")
public class TestAccessController {

    @GetMapping("/public")
    public ResponseEntity<ApiResponse<String>> publicAccess() {
        return ResponseEntity.ok(ApiResponse.success("Public access granted", "PUBLIC_DATA"));
    }

    @GetMapping("/citizen")
    @PreAuthorize("hasAnyRole('CITIZEN', 'ADMIN')")
    public ResponseEntity<ApiResponse<String>> citizenAccess() {
        return ResponseEntity.ok(ApiResponse.success("Citizen access granted", "CITIZEN_DATA"));
    }

    @GetMapping("/volunteer")
    @PreAuthorize("hasAnyRole('VOLUNTEER', 'ADMIN')")
    public ResponseEntity<ApiResponse<String>> volunteerAccess() {
        return ResponseEntity.ok(ApiResponse.success("Volunteer access granted", "VOLUNTEER_DATA"));
    }

    @GetMapping("/ngo")
    @PreAuthorize("hasAnyRole('NGO', 'ADMIN')")
    public ResponseEntity<ApiResponse<String>> ngoAccess() {
        return ResponseEntity.ok(ApiResponse.success("NGO access granted", "NGO_DATA"));
    }

    @GetMapping("/authority")
    @PreAuthorize("hasAnyRole('AUTHORITY', 'ADMIN')")
    public ResponseEntity<ApiResponse<String>> authorityAccess() {
        return ResponseEntity.ok(ApiResponse.success("Authority access granted", "AUTHORITY_DATA"));
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> adminAccess() {
        return ResponseEntity.ok(ApiResponse.success("Admin access granted", "ADMIN_DATA"));
    }
}
