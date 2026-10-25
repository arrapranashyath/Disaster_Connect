package com.disasterconnect.controller;

import com.disasterconnect.dto.*;
import com.disasterconnect.entity.NGOProfile;
import com.disasterconnect.entity.ReliefCampaign;
import com.disasterconnect.entity.User;
import com.disasterconnect.entity.enums.CampaignStatus;
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

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class CampaignControllerIntegrationTest {

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

    private User verifiedNgoUser;
    private NGOProfile verifiedNgoProfile;

    private User pendingNgoUser;
    private NGOProfile pendingNgoProfile;

    private User otherNgoUser;
    private NGOProfile otherNgoProfile;

    private User citizenUser;
    private User adminUser;

    private String verifiedNgoToken;
    private String pendingNgoToken;
    private String otherNgoToken;
    private String citizenToken;
    private String adminToken;

    @BeforeEach
    void setUp() throws Exception {
        cleanupDatabase();

        // 1. Verified NGO
        verifiedNgoUser = userRepository.save(new User("Red Cross NGO", "camp_ver@example.com", passwordEncoder.encode("Password123!"), "+911111111111", Role.NGO, "Pune"));
        verifiedNgoProfile = new NGOProfile(verifiedNgoUser, "Red Cross Disaster Relief", "NGO-VER-01");
        verifiedNgoProfile.setVerificationStatus(VerificationStatus.VERIFIED);
        verifiedNgoProfile = ngoProfileRepository.save(verifiedNgoProfile);
        verifiedNgoToken = login("camp_ver@example.com", "Password123!");

        // 2. Pending NGO
        pendingNgoUser = userRepository.save(new User("Unverified Aid", "camp_pen@example.com", passwordEncoder.encode("Password123!"), "+912222222222", Role.NGO, "Pune"));
        pendingNgoProfile = new NGOProfile(pendingNgoUser, "Unverified Aid Group", "NGO-PEN-02");
        pendingNgoProfile.setVerificationStatus(VerificationStatus.PENDING);
        pendingNgoProfile = ngoProfileRepository.save(pendingNgoProfile);
        pendingNgoToken = login("camp_pen@example.com", "Password123!");

        // 3. Other Verified NGO
        otherNgoUser = userRepository.save(new User("Blue Shield", "camp_other@example.com", passwordEncoder.encode("Password123!"), "+913333333333", Role.NGO, "Mumbai"));
        otherNgoProfile = new NGOProfile(otherNgoUser, "Blue Shield Network", "NGO-OTH-03");
        otherNgoProfile.setVerificationStatus(VerificationStatus.VERIFIED);
        otherNgoProfile = ngoProfileRepository.save(otherNgoProfile);
        otherNgoToken = login("camp_other@example.com", "Password123!");

        // 4. Citizen
        citizenUser = userRepository.save(new User("Citizen Donor", "camp_cit@example.com", passwordEncoder.encode("Password123!"), "+914444444444", Role.CITIZEN, "Pune"));
        citizenToken = login("camp_cit@example.com", "Password123!");

        // 5. Admin
        adminUser = userRepository.save(new User("Admin Master", "camp_adm@example.com", passwordEncoder.encode("Password123!"), "+915555555555", Role.ADMIN, "Pune"));
        adminToken = login("camp_adm@example.com", "Password123!");
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
    @DisplayName("REQ-CAMP-1: Verified NGO creates relief campaign successfully")
    void testCreateCampaign_VerifiedNgo_Success() throws Exception {
        CampaignCreateRequest request = new CampaignCreateRequest(
                "Pune Flood Relief 2026",
                "Providing emergency rations, clean water and medical kits to affected families.",
                500000.0,
                "UPI ID: disasterrelief@upi or Bank Transfer to SBI A/C: 1234567890 (IFSC: SBIN0001234)",
                Instant.now(),
                Instant.now().plus(30, ChronoUnit.DAYS)
        );

        mockMvc.perform(post("/api/campaigns")
                        .header("Authorization", "Bearer " + verifiedNgoToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value("Pune Flood Relief 2026"))
                .andExpect(jsonPath("$.data.targetAmount").value(500000.0))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"))
                .andExpect(jsonPath("$.data.verificationStatus").value("VERIFIED"))
                .andExpect(jsonPath("$.data.ngoId").value(verifiedNgoProfile.getNgoId()));

        assertEquals(1, campaignRepository.count());
    }

    @Test
    @DisplayName("REQ-CAMP-1: Unverified (Pending) NGO cannot create campaign (Forbidden)")
    void testCreateCampaign_PendingNgo_Forbidden() throws Exception {
        CampaignCreateRequest request = new CampaignCreateRequest(
                "Unauthorized Campaign", "Help urgently", 100000.0,
                "Bank info here", null, null
        );

        mockMvc.perform(post("/api/campaigns")
                        .header("Authorization", "Bearer " + pendingNgoToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());

        assertEquals(0, campaignRepository.count());
    }

    @Test
    @DisplayName("REQ-CAMP-1: Citizen cannot create campaign (Forbidden)")
    void testCreateCampaign_Citizen_Forbidden() throws Exception {
        CampaignCreateRequest request = new CampaignCreateRequest(
                "Citizen Campaign", "Need funds", 50000.0,
                "GPay 9999999999", null, null
        );

        mockMvc.perform(post("/api/campaigns")
                        .header("Authorization", "Bearer " + citizenToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("REQ-CAMP-1: Validation error on missing required fields")
    void testCreateCampaign_ValidationError() throws Exception {
        CampaignCreateRequest request = new CampaignCreateRequest(
                "", "", null, "", null, null
        );

        mockMvc.perform(post("/api/campaigns")
                        .header("Authorization", "Bearer " + verifiedNgoToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("REQ-CAMP-2: Public listing of active verified campaigns")
    void testGetActiveCampaigns_Public_Success() throws Exception {
        campaignRepository.save(new ReliefCampaign(
                verifiedNgoProfile, "Shelter Construction Aid", "Building temporary shelters",
                200000.0, 50000.0, "Donations via UPI: shelter@upi",
                CampaignStatus.ACTIVE, VerificationStatus.VERIFIED,
                Instant.now(), Instant.now().plus(60, ChronoUnit.DAYS)
        ));

        // Suspended or pending campaign should not appear
        campaignRepository.save(new ReliefCampaign(
                verifiedNgoProfile, "Suspended Aid", "Not active",
                10000.0, 0.0, "N/A",
                CampaignStatus.SUSPENDED, VerificationStatus.VERIFIED,
                Instant.now(), null
        ));

        mockMvc.perform(get("/api/campaigns"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content.length()").value(1))
                .andExpect(jsonPath("$.data.content[0].title").value("Shelter Construction Aid"));
    }

    @Test
    @DisplayName("REQ-CAMP-2: View single campaign includes donation details")
    void testGetCampaignById_Success() throws Exception {
        ReliefCampaign saved = campaignRepository.save(new ReliefCampaign(
                verifiedNgoProfile, "Medical Supplies Fund", "Medicines for flood victims",
                300000.0, 100000.0, "Donate at https://redcross.org/donate",
                CampaignStatus.ACTIVE, VerificationStatus.VERIFIED,
                Instant.now(), Instant.now().plus(45, ChronoUnit.DAYS)
        ));

        mockMvc.perform(get("/api/campaigns/" + saved.getCampaignId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value("Medical Supplies Fund"))
                .andExpect(jsonPath("$.data.donationInstructions").value("Donate at https://redcross.org/donate"));
    }

    @Test
    @DisplayName("REQ-CAMP-3: Owning NGO updates campaign status to COMPLETED")
    void testUpdateCampaignStatus_OwningNgo_Success() throws Exception {
        ReliefCampaign saved = campaignRepository.save(new ReliefCampaign(
                verifiedNgoProfile, "Blanket Drive", "Distributing blankets",
                50000.0, 50000.0, "UPI: blankets@upi",
                CampaignStatus.ACTIVE, VerificationStatus.VERIFIED,
                Instant.now(), null
        ));

        CampaignStatusUpdateRequest updateReq = new CampaignStatusUpdateRequest(CampaignStatus.COMPLETED);

        mockMvc.perform(patch("/api/campaigns/" + saved.getCampaignId() + "/status")
                        .header("Authorization", "Bearer " + verifiedNgoToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("COMPLETED"));

        ReliefCampaign updated = campaignRepository.findById(saved.getCampaignId()).orElseThrow();
        assertEquals(CampaignStatus.COMPLETED, updated.getStatus());
    }

    @Test
    @DisplayName("REQ-CAMP-3: Another NGO cannot modify other NGO's campaign (Forbidden)")
    void testUpdateCampaignStatus_OtherNgo_Forbidden() throws Exception {
        ReliefCampaign saved = campaignRepository.save(new ReliefCampaign(
                verifiedNgoProfile, "Blanket Drive", "Distributing blankets",
                50000.0, 50000.0, "UPI: blankets@upi",
                CampaignStatus.ACTIVE, VerificationStatus.VERIFIED,
                Instant.now(), null
        ));

        CampaignStatusUpdateRequest updateReq = new CampaignStatusUpdateRequest(CampaignStatus.SUSPENDED);

        mockMvc.perform(patch("/api/campaigns/" + saved.getCampaignId() + "/status")
                        .header("Authorization", "Bearer " + otherNgoToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("REQ-CAMP-3: Admin can verify or reject campaign")
    void testVerifyCampaign_Admin_Success() throws Exception {
        ReliefCampaign saved = campaignRepository.save(new ReliefCampaign(
                verifiedNgoProfile, "Under Review Campaign", "Pending admin check",
                100000.0, 0.0, "UPI: aid@upi",
                CampaignStatus.ACTIVE, VerificationStatus.PENDING,
                Instant.now(), null
        ));

        mockMvc.perform(patch("/api/campaigns/" + saved.getCampaignId() + "/verify")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("status", "VERIFIED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.verificationStatus").value("VERIFIED"));

        ReliefCampaign updated = campaignRepository.findById(saved.getCampaignId()).orElseThrow();
        assertEquals(VerificationStatus.VERIFIED, updated.getVerificationStatus());
    }

    @Test
    @DisplayName("REQ-CAMP-3: Non-admin cannot verify campaign (Forbidden)")
    void testVerifyCampaign_NonAdmin_Forbidden() throws Exception {
        ReliefCampaign saved = campaignRepository.save(new ReliefCampaign(
                verifiedNgoProfile, "Under Review Campaign", "Pending check",
                100000.0, 0.0, "UPI: aid@upi",
                CampaignStatus.ACTIVE, VerificationStatus.PENDING,
                Instant.now(), null
        ));

        mockMvc.perform(patch("/api/campaigns/" + saved.getCampaignId() + "/verify")
                        .header("Authorization", "Bearer " + verifiedNgoToken)
                        .param("status", "VERIFIED"))
                .andExpect(status().isForbidden());
    }
}
