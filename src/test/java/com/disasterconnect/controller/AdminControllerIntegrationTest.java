package com.disasterconnect.controller;

import com.disasterconnect.dto.*;
import com.disasterconnect.entity.NGOProfile;
import com.disasterconnect.entity.User;
import com.disasterconnect.entity.enums.Role;
import com.disasterconnect.entity.enums.VerificationStatus;
import com.disasterconnect.repository.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class AdminControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AdminActionLogRepository adminActionLogRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private ReliefCampaignRepository campaignRepository;

    @Autowired
    private MissingPersonRepository missingPersonRepository;

    @Autowired
    private ResourceUpdateLogRepository resourceUpdateLogRepository;

    @Autowired
    private ReliefResourceRepository reliefResourceRepository;

    @Autowired
    private ShelterRepository shelterRepository;

    @Autowired
    private VolunteerProfileRepository volunteerProfileRepository;

    @Autowired
    private NGOProfileRepository ngoProfileRepository;

    @Autowired
    private RequestStatusLogRepository requestStatusLogRepository;

    @Autowired
    private AssistanceRequestRepository assistanceRequestRepository;

    @Autowired
    private IncidentReportRepository incidentReportRepository;

    @Autowired
    private DisasterAlertRepository disasterAlertRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User adminUser;
    private User pendingNgoUser;
    private NGOProfile pendingNgoProfile;
    private User citizenUser;

    private String adminToken;
    private String citizenToken;

    @BeforeEach
    void setUp() throws Exception {
        cleanupDatabase();

        // 1. Admin
        adminUser = userRepository.save(new User("Admin Master", "adm_test@example.com", passwordEncoder.encode("Password123!"), "+911111111111", Role.ADMIN, "Pune"));
        adminToken = login("adm_test@example.com", "Password123!");

        // 2. Pending NGO
        pendingNgoUser = userRepository.save(new User("Hope Aid User", "ngo_pending@example.com", passwordEncoder.encode("Password123!"), "+912222222222", Role.NGO, "Pune"));
        pendingNgoProfile = new NGOProfile(pendingNgoUser, "Hope Aid Network", "NGO-HOPE-77");
        pendingNgoProfile.setVerificationStatus(VerificationStatus.PENDING);
        pendingNgoProfile = ngoProfileRepository.save(pendingNgoProfile);

        // 3. Citizen
        citizenUser = userRepository.save(new User("Regular Citizen", "cit_test@example.com", passwordEncoder.encode("Password123!"), "+913333333333", Role.CITIZEN, "Pune"));
        citizenToken = login("cit_test@example.com", "Password123!");
    }

    @org.junit.jupiter.api.AfterEach
    void tearDown() {
        cleanupDatabase();
    }

    private void cleanupDatabase() {
        adminActionLogRepository.deleteAll();
        notificationRepository.deleteAll();
        campaignRepository.deleteAll();
        missingPersonRepository.deleteAll();
        resourceUpdateLogRepository.deleteAll();
        reliefResourceRepository.deleteAll();
        shelterRepository.deleteAll();
        volunteerProfileRepository.deleteAll();
        ngoProfileRepository.deleteAll();
        requestStatusLogRepository.deleteAll();
        assistanceRequestRepository.deleteAll();
        incidentReportRepository.deleteAll();
        disasterAlertRepository.deleteAll();
        userRepository.deleteAll();
    }

    private String login(String email, String password) throws Exception {
        LoginRequest loginRequest = new LoginRequest(email, password);
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        ApiResponse<AuthResponse> response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                new TypeReference<>() {}
        );
        return response.getData().getToken();
    }

    @Test
    @DisplayName("REQ-ADMIN-1: Admin retrieves pending NGO verification queue")
    void testGetPendingNgos_Admin_Success() throws Exception {
        mockMvc.perform(get("/api/admin/ngos/pending")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content.length()").value(1))
                .andExpect(jsonPath("$.data.content[0].orgName").value("Hope Aid Network"))
                .andExpect(jsonPath("$.data.content[0].verificationStatus").value("PENDING"));
    }

    @Test
    @DisplayName("REQ-ADMIN-1: Admin approves pending NGO")
    void testVerifyNgo_Admin_Approve_Success() throws Exception {
        mockMvc.perform(patch("/api/admin/ngos/" + pendingNgoProfile.getNgoId() + "/verify")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("status", "VERIFIED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.verificationStatus").value("VERIFIED"));

        NGOProfile updated = ngoProfileRepository.findById(pendingNgoProfile.getNgoId()).orElseThrow();
        assertEquals(VerificationStatus.VERIFIED, updated.getVerificationStatus());

        // Verify audit log recorded
        assertEquals(1, adminActionLogRepository.count());
        // Verify notification sent to NGO user
        assertEquals(1, notificationRepository.findByUserOrderByCreatedAtDesc(pendingNgoUser).size());
    }

    @Test
    @DisplayName("REQ-ADMIN-1: Non-admin cannot access verification queue (Forbidden)")
    void testGetPendingNgos_Citizen_Forbidden() throws Exception {
        mockMvc.perform(get("/api/admin/ngos/pending")
                        .header("Authorization", "Bearer " + citizenToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("REQ-ADMIN-3: Admin suspends user and user login is rejected")
    void testSuspendUser_Admin_Success() throws Exception {
        UserSuspendRequest suspendRequest = new UserSuspendRequest(true, "Violated terms of service");

        mockMvc.perform(patch("/api/admin/users/" + citizenUser.getUserId() + "/suspend")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(suspendRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        User updatedUser = userRepository.findById(citizenUser.getUserId()).orElseThrow();
        assertTrue(updatedUser.isSuspended());

        // Subsequent login must fail with 401 Unauthorized
        LoginRequest loginRequest = new LoginRequest("cit_test@example.com", "Password123!");
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());

        // Existing JWT token must also be rejected with 401 Unauthorized
        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer " + citizenToken))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("REQ-ADMIN-3: Admin cannot suspend their own account (BadRequest)")
    void testSuspendSelf_Admin_BadRequest() throws Exception {
        UserSuspendRequest suspendRequest = new UserSuspendRequest(true, "Self suspend");

        mockMvc.perform(patch("/api/admin/users/" + adminUser.getUserId() + "/suspend")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(suspendRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("REQ-ADMIN-2: Admin retrieves system dashboard summary metrics")
    void testGetSystemSummary_Admin_Success() throws Exception {
        mockMvc.perform(get("/api/admin/summary")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalUsers").value(3))
                .andExpect(jsonPath("$.data.totalNgos").value(1))
                .andExpect(jsonPath("$.data.pendingNgos").value(1));
    }

    @Test
    @DisplayName("REQ-ADMIN-4: Admin retrieves audit logs")
    void testGetAuditLogs_Admin_Success() throws Exception {
        // Perform an action first to generate a log
        mockMvc.perform(patch("/api/admin/ngos/" + pendingNgoProfile.getNgoId() + "/verify")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("status", "VERIFIED"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/admin/audit-logs")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content.length()").value(1))
                .andExpect(jsonPath("$.data.content[0].action").value("VERIFY_NGO"));
    }
}
