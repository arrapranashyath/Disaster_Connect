package com.disasterconnect.controller;

import com.disasterconnect.dto.AlertCreateRequest;
import com.disasterconnect.dto.AlertStatusUpdateRequest;
import com.disasterconnect.dto.LoginRequest;
import com.disasterconnect.dto.RegisterRequest;
import com.disasterconnect.entity.User;
import com.disasterconnect.entity.enums.AlertSeverity;
import com.disasterconnect.entity.enums.AlertStatus;
import com.disasterconnect.entity.enums.Role;
import com.disasterconnect.repository.DisasterAlertRepository;
import com.disasterconnect.repository.IncidentReportRepository;
import com.disasterconnect.repository.NGOProfileRepository;
import com.disasterconnect.repository.UserRepository;
import com.disasterconnect.repository.VolunteerProfileRepository;
import com.fasterxml.jackson.databind.JsonNode;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AlertControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private DisasterAlertRepository alertRepository;

    @Autowired
    private VolunteerProfileRepository volunteerProfileRepository;

    @Autowired
    private NGOProfileRepository ngoProfileRepository;

    @Autowired
    private IncidentReportRepository incidentRepository;

        @Autowired
    private com.disasterconnect.repository.ResourceUpdateLogRepository resourceUpdateLogRepository;

    @Autowired
    private com.disasterconnect.repository.ReliefResourceRepository reliefResourceRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private com.disasterconnect.repository.AssistanceRequestRepository assistanceRequestRepository;

    @Autowired
    private com.disasterconnect.repository.RequestStatusLogRepository requestStatusLogRepository;

    @Autowired
    private com.disasterconnect.repository.AdminActionLogRepository adminActionLogRepository;

    @Autowired
    private com.disasterconnect.repository.NotificationRepository notificationRepository;

    @Autowired
    private com.disasterconnect.repository.ReliefCampaignRepository reliefCampaignRepository;

    @Autowired
    private com.disasterconnect.repository.MissingPersonRepository missingPersonRepository;

    @Autowired
    private com.disasterconnect.repository.ShelterRepository shelterRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        // Delete in FK-safe order: Phase 6 tables first, then Phase 1-5 tables, then users
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
        incidentRepository.deleteAll();
        alertRepository.deleteAll();
        userRepository.deleteAll();
    }

    // -----------------------------------------------------------------------
    // Helper: register and return JWT token
    // -----------------------------------------------------------------------

    private String registerAndLogin(String name, String email, String password, Role role) throws Exception {
        if (role == Role.ADMIN || role == Role.AUTHORITY) {
            User user = new User(name, email, passwordEncoder.encode(password), "+919000000001", role, "Pune");
            userRepository.save(user);
        } else {
            RegisterRequest reg = new RegisterRequest(name, email, password, "+919000000001", role, "Pune");
            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(reg)))
                    .andExpect(status().isCreated());
        }

        LoginRequest login = new LoginRequest(email, password);
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andReturn();

        String body = result.getResponse().getContentAsString();
        JsonNode root = objectMapper.readTree(body);
        return root.path("data").path("token").asText();
    }

    // -----------------------------------------------------------------------
    // Tests: GET /api/alerts (public)
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("GET /api/alerts should be accessible without authentication")
    void testGetAlertsPublicAccess() throws Exception {
        mockMvc.perform(get("/api/alerts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    // -----------------------------------------------------------------------
    // Tests: POST /api/alerts (AUTHORITY or ADMIN only)
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("POST /api/alerts should succeed for AUTHORITY user")
    void testCreateAlertAsAuthority() throws Exception {
        String token = registerAndLogin("Auth Officer", "authority@example.com", "pass1234", Role.AUTHORITY);

        AlertCreateRequest request = new AlertCreateRequest();
        request.setDisasterType("FLOOD");
        request.setTitle("Severe Flooding in Pune");
        request.setDescription("Major flooding reported in low-lying areas.");
        request.setAffectedArea("Pune, Maharashtra");
        request.setSeverity(AlertSeverity.HIGH);
        request.setSafetyInstructions("Evacuate immediately.");

        mockMvc.perform(post("/api/alerts")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value("Severe Flooding in Pune"))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"))
                .andExpect(jsonPath("$.data.issuedBy.name").value("Auth Officer"));
    }

    @Test
    @DisplayName("POST /api/alerts should succeed for ADMIN user")
    void testCreateAlertAsAdmin() throws Exception {
        String token = registerAndLogin("Admin User", "admin@example.com", "pass1234", Role.ADMIN);

        AlertCreateRequest request = new AlertCreateRequest();
        request.setDisasterType("EARTHQUAKE");
        request.setTitle("Earthquake Warning");
        request.setDescription("Minor tremors detected.");
        request.setAffectedArea("Mumbai");
        request.setSeverity(AlertSeverity.MEDIUM);

        mockMvc.perform(post("/api/alerts")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("POST /api/alerts should return 403 for CITIZEN user")
    void testCreateAlertAsCitizenForbidden() throws Exception {
        String token = registerAndLogin("Plain Citizen", "citizen@example.com", "pass1234", Role.CITIZEN);

        AlertCreateRequest request = new AlertCreateRequest();
        request.setDisasterType("FLOOD");
        request.setTitle("Flood Alert");
        request.setDescription("Test description.");
        request.setAffectedArea("Pune");
        request.setSeverity(AlertSeverity.LOW);

        mockMvc.perform(post("/api/alerts")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /api/alerts should return 401 when unauthenticated")
    void testCreateAlertUnauthenticated() throws Exception {
        AlertCreateRequest request = new AlertCreateRequest();
        request.setDisasterType("FLOOD");
        request.setTitle("Test Alert");
        request.setDescription("Test description.");
        request.setAffectedArea("Pune");
        request.setSeverity(AlertSeverity.LOW);

        mockMvc.perform(post("/api/alerts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /api/alerts should return 400 when required fields are missing")
    void testCreateAlertValidationFailure() throws Exception {
        String token = registerAndLogin("Auth Officer", "auth2@example.com", "pass1234", Role.AUTHORITY);

        // Missing title, description, severity
        AlertCreateRequest request = new AlertCreateRequest();
        request.setDisasterType("FLOOD");
        request.setAffectedArea("Pune");

        mockMvc.perform(post("/api/alerts")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // -----------------------------------------------------------------------
    // Tests: GET /api/alerts/{id}
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("GET /api/alerts/{id} should return 404 for non-existent alert")
    void testGetAlertByIdNotFound() throws Exception {
        mockMvc.perform(get("/api/alerts/9999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/alerts/{id} should return alert without authentication")
    void testGetAlertByIdPublic() throws Exception {
        // Create alert first
        String token = registerAndLogin("Auth Officer", "auth3@example.com", "pass1234", Role.AUTHORITY);

        AlertCreateRequest request = new AlertCreateRequest();
        request.setDisasterType("CYCLONE");
        request.setTitle("Cyclone Warning");
        request.setDescription("Cyclone approaching.");
        request.setAffectedArea("Chennai");
        request.setSeverity(AlertSeverity.CRITICAL);

        MvcResult createResult = mockMvc.perform(post("/api/alerts")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode root = objectMapper.readTree(createResult.getResponse().getContentAsString());
        long alertId = root.path("data").path("alertId").asLong();

        // Now fetch without auth
        mockMvc.perform(get("/api/alerts/" + alertId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("Cyclone Warning"))
                .andExpect(jsonPath("$.data.severity").value("CRITICAL"));
    }

    // -----------------------------------------------------------------------
    // Tests: PATCH /api/alerts/{id}/status
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("PATCH /api/alerts/{id}/status should update to RESOLVED for AUTHORITY")
    void testUpdateAlertStatusAsAuthority() throws Exception {
        String token = registerAndLogin("Auth Officer", "auth4@example.com", "pass1234", Role.AUTHORITY);

        AlertCreateRequest createReq = new AlertCreateRequest();
        createReq.setDisasterType("FIRE");
        createReq.setTitle("Forest Fire");
        createReq.setDescription("Fire in forest area.");
        createReq.setAffectedArea("Mahabaleshwar");
        createReq.setSeverity(AlertSeverity.HIGH);

        MvcResult createResult = mockMvc.perform(post("/api/alerts")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReq)))
                .andReturn();

        long alertId = objectMapper.readTree(createResult.getResponse().getContentAsString())
                .path("data").path("alertId").asLong();

        AlertStatusUpdateRequest updateReq = new AlertStatusUpdateRequest();
        updateReq.setStatus(AlertStatus.RESOLVED);

        mockMvc.perform(patch("/api/alerts/" + alertId + "/status")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("RESOLVED"));
    }

    @Test
    @DisplayName("PATCH /api/alerts/{id}/status should return 403 for CITIZEN")
    void testUpdateAlertStatusAsCitizenForbidden() throws Exception {
        String authorityToken = registerAndLogin("Auth Officer", "auth5@example.com", "pass1234", Role.AUTHORITY);

        AlertCreateRequest createReq = new AlertCreateRequest();
        createReq.setDisasterType("FIRE");
        createReq.setTitle("Forest Fire");
        createReq.setDescription("Fire in forest area.");
        createReq.setAffectedArea("Mahabaleshwar");
        createReq.setSeverity(AlertSeverity.HIGH);

        MvcResult createResult = mockMvc.perform(post("/api/alerts")
                        .header("Authorization", "Bearer " + authorityToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReq)))
                .andReturn();

        long alertId = objectMapper.readTree(createResult.getResponse().getContentAsString())
                .path("data").path("alertId").asLong();

        String citizenToken = registerAndLogin("Citizen User", "citizen.update@example.com", "pass1234", Role.CITIZEN);
        AlertStatusUpdateRequest updateReq = new AlertStatusUpdateRequest();
        updateReq.setStatus(AlertStatus.RESOLVED);

        mockMvc.perform(patch("/api/alerts/" + alertId + "/status")
                        .header("Authorization", "Bearer " + citizenToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/alerts with status and area filters should return matching alerts")
    void testGetAlertsWithFilters() throws Exception {
        String token = registerAndLogin("Auth Officer", "auth6@example.com", "pass1234", Role.AUTHORITY);

        AlertCreateRequest alert1 = new AlertCreateRequest();
        alert1.setDisasterType("FLOOD");
        alert1.setTitle("Pune Flood");
        alert1.setDescription("Water levels rising");
        alert1.setAffectedArea("Pune District");
        alert1.setSeverity(AlertSeverity.HIGH);

        mockMvc.perform(post("/api/alerts")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(alert1)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/alerts")
                        .param("status", "ACTIVE")
                        .param("affectedArea", "Pune"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].affectedArea").value("Pune District"));

        mockMvc.perform(get("/api/alerts")
                        .param("status", "INVALID_STATUS"))
                .andExpect(status().isBadRequest());
    }
}
