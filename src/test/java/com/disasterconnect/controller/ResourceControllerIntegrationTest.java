package com.disasterconnect.controller;

import com.disasterconnect.dto.*;
import com.disasterconnect.entity.NGOProfile;
import com.disasterconnect.entity.ReliefResource;
import com.disasterconnect.entity.User;
import com.disasterconnect.entity.enums.*;
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

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class ResourceControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

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
    private com.disasterconnect.repository.AdminActionLogRepository adminActionLogRepository;

    @Autowired
    private com.disasterconnect.repository.NotificationRepository notificationRepository;

    @Autowired
    private com.disasterconnect.repository.ReliefCampaignRepository reliefCampaignRepository;

    @Autowired
    private com.disasterconnect.repository.MissingPersonRepository missingPersonRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User adminUser;
    private User verifiedNgoUser;
    private NGOProfile verifiedNgoProfile;
    private User pendingNgoUser;
    private NGOProfile pendingNgoProfile;
    private User citizenUser;
    private User authorityUser;

    private String adminToken;
    private String verifiedNgoToken;
    private String pendingNgoToken;
    private String citizenToken;
    private String authorityToken;

    @BeforeEach
    void setUp() throws Exception {
        adminActionLogRepository.deleteAll();
        notificationRepository.deleteAll();
        reliefCampaignRepository.deleteAll();
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

        // Admin
        adminUser = new User("Admin User", "res_admin@example.com", passwordEncoder.encode("Password123!"), "+911111111111", Role.ADMIN, "Pune");
        adminUser = userRepository.save(adminUser);
        adminToken = login("res_admin@example.com", "Password123!");

        // Verified NGO
        verifiedNgoUser = new User("Red Cross NGO", "res_ngo_ver@example.com", passwordEncoder.encode("Password123!"), "+912222222222", Role.NGO, "Pune");
        verifiedNgoUser = userRepository.save(verifiedNgoUser);
        verifiedNgoProfile = new NGOProfile(verifiedNgoUser, "Red Cross Disaster Relief", "NGO-RED-001");
        verifiedNgoProfile.setVerificationStatus(VerificationStatus.VERIFIED);
        verifiedNgoProfile = ngoProfileRepository.save(verifiedNgoProfile);
        verifiedNgoToken = login("res_ngo_ver@example.com", "Password123!");

        // Pending NGO
        pendingNgoUser = new User("Unverified NGO", "res_ngo_pen@example.com", passwordEncoder.encode("Password123!"), "+913333333333", Role.NGO, "Pune");
        pendingNgoUser = userRepository.save(pendingNgoUser);
        pendingNgoProfile = new NGOProfile(pendingNgoUser, "Hope Aid Network", "NGO-HOPE-002");
        pendingNgoProfile.setVerificationStatus(VerificationStatus.PENDING);
        pendingNgoProfile = ngoProfileRepository.save(pendingNgoProfile);
        pendingNgoToken = login("res_ngo_pen@example.com", "Password123!");

        // Authority
        authorityUser = new User("City Authority", "res_auth@example.com", passwordEncoder.encode("Password123!"), "+914444444444", Role.AUTHORITY, "Pune");
        authorityUser = userRepository.save(authorityUser);
        authorityToken = login("res_auth@example.com", "Password123!");

        // Citizen
        citizenUser = new User("Citizen User", "res_citizen@example.com", passwordEncoder.encode("Password123!"), "+915555555555", Role.CITIZEN, "Pune");
        citizenUser = userRepository.save(citizenUser);
        citizenToken = login("res_citizen@example.com", "Password123!");
    }

    private String login(String email, String password) throws Exception {
        LoginRequest req = new LoginRequest(email, password);
        MvcResult res = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readTree(res.getResponse().getContentAsString())
                .path("data").path("token").asText();
    }

    @Test
    @DisplayName("Verified NGO successfully creates a relief resource (201 Created)")
    void testVerifiedNgoCreateResourceSuccess() throws Exception {
        ResourceCreateRequest request = new ResourceCreateRequest();
        request.setResourceType(ResourceType.WATER);
        request.setRequiredQuantity(1000.0);
        request.setAvailableQuantity(600.0);
        request.setUnit("liters");
        request.setLocation("Shivajinagar Camp, Pune");
        request.setPriority(RequestPriority.HIGH);
        request.setNotes("Initial stock arrived");

        MvcResult result = mockMvc.perform(post("/api/resources")
                        .header("Authorization", "Bearer " + verifiedNgoToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.resourceType").value("WATER"))
                .andExpect(jsonPath("$.data.status").value("AVAILABLE"))
                .andExpect(jsonPath("$.data.deficitQuantity").value(400.0))
                .andReturn();

        ApiResponse<ResourceResponse> response = objectMapper.readValue(
                result.getResponse().getContentAsString(), new TypeReference<>() {});
        assertNotNull(response.getData().getResourceId());
        assertEquals(verifiedNgoProfile.getNgoId(), response.getData().getNgoId());
    }

    @Test
    @DisplayName("REQ-RES-3: System automatically flags resource as LOW when quantity drops below 50%")
    void testResourceStatusAutoFlaggedLow() throws Exception {
        ResourceCreateRequest request = new ResourceCreateRequest();
        request.setResourceType(ResourceType.FOOD);
        request.setRequiredQuantity(1000.0);
        request.setAvailableQuantity(400.0); // 40% <= 50% => LOW
        request.setLocation("Kothrud Relief Hub, Pune");

        mockMvc.perform(post("/api/resources")
                        .header("Authorization", "Bearer " + verifiedNgoToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.status").value("LOW"));
    }

    @Test
    @DisplayName("REQ-RES-3: System automatically flags resource as CRITICAL when quantity drops below 25%")
    void testResourceStatusAutoFlaggedCritical() throws Exception {
        ResourceCreateRequest request = new ResourceCreateRequest();
        request.setResourceType(ResourceType.MEDICINE);
        request.setRequiredQuantity(1000.0);
        request.setAvailableQuantity(150.0); // 15% <= 25% => CRITICAL
        request.setLocation("Central Camp, Pune");

        mockMvc.perform(post("/api/resources")
                        .header("Authorization", "Bearer " + verifiedNgoToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.status").value("CRITICAL"));
    }

    @Test
    @DisplayName("Unverified NGO is rejected (400) when recording resources")
    void testUnverifiedNgoCannotCreateResource() throws Exception {
        ResourceCreateRequest request = new ResourceCreateRequest();
        request.setResourceType(ResourceType.BLANKETS);
        request.setRequiredQuantity(500.0);
        request.setAvailableQuantity(200.0);
        request.setLocation("North Camp");

        mockMvc.perform(post("/api/resources")
                        .header("Authorization", "Bearer " + pendingNgoToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("Citizen is forbidden (403) from recording relief resources")
    void testCitizenForbiddenFromCreatingResource() throws Exception {
        ResourceCreateRequest request = new ResourceCreateRequest();
        request.setResourceType(ResourceType.FOOD);
        request.setRequiredQuantity(100.0);
        request.setAvailableQuantity(50.0);
        request.setLocation("Local Area");

        mockMvc.perform(post("/api/resources")
                        .header("Authorization", "Bearer " + citizenToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Validation failure: invalid or negative quantities return 400 Bad Request")
    void testValidationFailureNegativeQuantity() throws Exception {
        ResourceCreateRequest request = new ResourceCreateRequest();
        request.setResourceType(ResourceType.FOOD);
        request.setRequiredQuantity(-10.0); // Invalid
        request.setAvailableQuantity(-5.0);  // Invalid
        request.setLocation("Camp");

        mockMvc.perform(post("/api/resources")
                        .header("Authorization", "Bearer " + verifiedNgoToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("REQ-RES-2: View resource status by area to identify shortages")
    void testViewShortagesByArea() throws Exception {
        // Resource 1 in Pune: Available
        ReliefResource r1 = new ReliefResource(verifiedNgoProfile, ResourceType.WATER, 1000.0, 900.0, "L", "Pune East", RequestPriority.LOW, 25.0, 50.0);
        // Resource 2 in Pune: Low shortage
        ReliefResource r2 = new ReliefResource(verifiedNgoProfile, ResourceType.MEDICINE, 1000.0, 300.0, "kits", "Pune East", RequestPriority.HIGH, 25.0, 50.0);
        // Resource 3 in Pune: Critical shortage
        ReliefResource r3 = new ReliefResource(verifiedNgoProfile, ResourceType.FOOD, 1000.0, 100.0, "kg", "Pune East", RequestPriority.CRITICAL, 25.0, 50.0);
        reliefResourceRepository.saveAll(List.of(r1, r2, r3));

        // Authority queries shortages in Pune East
        mockMvc.perform(get("/api/resources/area")
                        .header("Authorization", "Bearer " + authorityToken)
                        .param("area", "Pune East")
                        .param("shortagesOnly", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(2)); // Only Low and Critical
    }

    @Test
    @DisplayName("REQ-RES-4: Updating a resource maintains a complete update history audit trail")
    void testUpdateResourceMaintainsAuditTrail() throws Exception {
        ReliefResource resource = new ReliefResource(verifiedNgoProfile, ResourceType.BLANKETS, 500.0, 400.0, "pcs", "Camp Area", RequestPriority.MEDIUM, 25.0, 50.0);
        resource = reliefResourceRepository.save(resource);

        // Update available quantity down to 80 (becomes CRITICAL)
        ResourceUpdateRequest updateReq = new ResourceUpdateRequest();
        updateReq.setAvailableQuantity(80.0);
        updateReq.setNotes("Distributed 320 blankets to flood survivors");

        mockMvc.perform(patch("/api/resources/" + resource.getResourceId())
                        .header("Authorization", "Bearer " + verifiedNgoToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("CRITICAL"))
                .andExpect(jsonPath("$.data.availableQuantity").value(80.0));

        // Fetch details with history
        MvcResult res = mockMvc.perform(get("/api/resources/" + resource.getResourceId())
                        .header("Authorization", "Bearer " + verifiedNgoToken))
                .andExpect(status().isOk())
                .andReturn();

        ApiResponse<ResourceResponse> response = objectMapper.readValue(
                res.getResponse().getContentAsString(), new TypeReference<>() {});
        assertNotNull(response.getData().getUpdateHistory());
        assertTrue(response.getData().getUpdateHistory().size() >= 1);
        assertEquals("CRITICAL", response.getData().getUpdateHistory().get(0).getNewStatus().name());
    }

    @Test
    @DisplayName("Cross-NGO ownership check: NGO cannot update another NGO's resource (403 Forbidden)")
    void testCrossNgoUpdateForbidden() throws Exception {
        // Resource owned by verifiedNgoProfile
        ReliefResource resource = new ReliefResource(verifiedNgoProfile, ResourceType.FOOD, 500.0, 400.0, "kg", "Camp", RequestPriority.MEDIUM, 25.0, 50.0);
        resource = reliefResourceRepository.save(resource);

        // Create second verified NGO
        User secondNgo = new User("Second NGO", "res_ngo_two@example.com", passwordEncoder.encode("Password123!"), "+916666666666", Role.NGO, "Pune");
        secondNgo = userRepository.save(secondNgo);
        NGOProfile secondProfile = new NGOProfile(secondNgo, "Seva Foundation", "NGO-SEVA-003");
        secondProfile.setVerificationStatus(VerificationStatus.VERIFIED);
        ngoProfileRepository.save(secondProfile);
        String secondToken = login("res_ngo_two@example.com", "Password123!");

        ResourceUpdateRequest updateReq = new ResourceUpdateRequest();
        updateReq.setAvailableQuantity(200.0);

        mockMvc.perform(patch("/api/resources/" + resource.getResourceId())
                        .header("Authorization", "Bearer " + secondToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isForbidden());
    }
}
