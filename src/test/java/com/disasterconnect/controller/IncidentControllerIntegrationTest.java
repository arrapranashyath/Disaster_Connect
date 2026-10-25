package com.disasterconnect.controller;

import com.disasterconnect.dto.IncidentCreateRequest;
import com.disasterconnect.dto.IncidentStatusUpdateRequest;
import com.disasterconnect.dto.LoginRequest;
import com.disasterconnect.dto.RegisterRequest;
import com.disasterconnect.entity.User;
import com.disasterconnect.entity.enums.AlertSeverity;
import com.disasterconnect.entity.enums.IncidentStatus;
import com.disasterconnect.entity.enums.Role;
import com.disasterconnect.repository.DisasterAlertRepository;
import com.disasterconnect.repository.IncidentReportRepository;
import com.disasterconnect.repository.NGOProfileRepository;
import com.disasterconnect.repository.UserRepository;
import com.disasterconnect.repository.VolunteerProfileRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class IncidentControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private IncidentReportRepository incidentRepository;

    @Autowired
    private DisasterAlertRepository alertRepository;

    @Autowired
    private VolunteerProfileRepository volunteerProfileRepository;

    @Autowired
    private NGOProfileRepository ngoProfileRepository;

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
        // Delete in FK-safe order
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
            User user = new User(name, email, passwordEncoder.encode(password), "+919000000002", role, "Pune");
            userRepository.save(user);
        } else {
            RegisterRequest reg = new RegisterRequest(name, email, password, "+919000000002", role, "Pune");
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

        return objectMapper.readTree(result.getResponse().getContentAsString())
                .path("data").path("token").asText();
    }

    private IncidentCreateRequest buildIncidentRequest() {
        IncidentCreateRequest req = new IncidentCreateRequest();
        req.setIncidentType("FLOOD");
        req.setDescription("Water rising near the bridge.");
        req.setLocationName("MG Road Bridge, Pune");
        req.setLatitude(18.5204);
        req.setLongitude(73.8567);
        req.setSeverity(AlertSeverity.HIGH);
        return req;
    }

    // -----------------------------------------------------------------------
    // Tests: POST /api/incidents (CITIZEN only)
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("POST /api/incidents should succeed for CITIZEN")
    void testSubmitIncidentAsCitizen() throws Exception {
        String token = registerAndLogin("Citizen User", "citizen@incident.com", "pass1234", Role.CITIZEN);

        mockMvc.perform(post("/api/incidents")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildIncidentRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("REPORTED"))
                .andExpect(jsonPath("$.data.incidentType").value("FLOOD"))
                .andExpect(jsonPath("$.data.reportedBy.name").value("Citizen User"));
    }

    @Test
    @DisplayName("POST /api/incidents should return 403 for VOLUNTEER")
    void testSubmitIncidentAsVolunteerForbidden() throws Exception {
        String token = registerAndLogin("Vol User", "volunteer@incident.com", "pass1234", Role.VOLUNTEER);

        mockMvc.perform(post("/api/incidents")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildIncidentRequest())))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /api/incidents should return 401 when unauthenticated")
    void testSubmitIncidentUnauthenticated() throws Exception {
        mockMvc.perform(post("/api/incidents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildIncidentRequest())))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /api/incidents should return 400 when required fields are missing")
    void testSubmitIncidentValidationFailure() throws Exception {
        String token = registerAndLogin("Citizen2", "citizen2@incident.com", "pass1234", Role.CITIZEN);

        IncidentCreateRequest bad = new IncidentCreateRequest();
        // missing incidentType, description, locationName, severity

        mockMvc.perform(post("/api/incidents")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bad)))
                .andExpect(status().isBadRequest());
    }

    // -----------------------------------------------------------------------
    // Tests: GET /api/incidents/my
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("GET /api/incidents/my should return only the citizen's own incidents")
    void testGetMyIncidents() throws Exception {
        String citizenToken = registerAndLogin("My Citizen", "mycitizen@incident.com", "pass1234", Role.CITIZEN);

        // Submit one incident
        mockMvc.perform(post("/api/incidents")
                        .header("Authorization", "Bearer " + citizenToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildIncidentRequest())))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/incidents/my")
                        .header("Authorization", "Bearer " + citizenToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(1));
    }

    @Test
    @DisplayName("GET /api/incidents/my should return 401 when unauthenticated")
    void testGetMyIncidentsUnauthenticated() throws Exception {
        mockMvc.perform(get("/api/incidents/my"))
                .andExpect(status().isUnauthorized());
    }

    // -----------------------------------------------------------------------
    // Tests: GET /api/incidents (AUTHORITY, ADMIN, NGO)
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("GET /api/incidents should return all incidents for AUTHORITY")
    void testGetAllIncidentsAsAuthority() throws Exception {
        // Submit an incident as citizen
        String citizenToken = registerAndLogin("Cit3", "cit3@incident.com", "pass1234", Role.CITIZEN);
        mockMvc.perform(post("/api/incidents")
                        .header("Authorization", "Bearer " + citizenToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildIncidentRequest())))
                .andExpect(status().isCreated());

        String authorityToken = registerAndLogin("Auth Person", "auth@incident.com", "pass1234", Role.AUTHORITY);
        mockMvc.perform(get("/api/incidents")
                        .header("Authorization", "Bearer " + authorityToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(1));
    }

    @Test
    @DisplayName("GET /api/incidents should return 403 for VOLUNTEER")
    void testGetAllIncidentsAsVolunteerForbidden() throws Exception {
        String token = registerAndLogin("Vol2", "vol2@incident.com", "pass1234", Role.VOLUNTEER);
        mockMvc.perform(get("/api/incidents")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    // -----------------------------------------------------------------------
    // Tests: PATCH /api/incidents/{id}/status
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("PATCH /api/incidents/{id}/status should update status for AUTHORITY")
    void testUpdateIncidentStatusAsAuthority() throws Exception {
        String citizenToken = registerAndLogin("Cit4", "cit4@incident.com", "pass1234", Role.CITIZEN);

        MvcResult submitResult = mockMvc.perform(post("/api/incidents")
                        .header("Authorization", "Bearer " + citizenToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildIncidentRequest())))
                .andExpect(status().isCreated())
                .andReturn();

        long incidentId = objectMapper.readTree(submitResult.getResponse().getContentAsString())
                .path("data").path("incidentId").asLong();

        String authorityToken = registerAndLogin("Auth5", "auth5@incident.com", "pass1234", Role.AUTHORITY);

        IncidentStatusUpdateRequest updateReq = new IncidentStatusUpdateRequest();
        updateReq.setStatus(IncidentStatus.UNDER_REVIEW);

        mockMvc.perform(patch("/api/incidents/" + incidentId + "/status")
                        .header("Authorization", "Bearer " + authorityToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("UNDER_REVIEW"));
    }

    @Test
    @DisplayName("PATCH /api/incidents/{id}/status should return 403 for CITIZEN")
    void testUpdateIncidentStatusAsCitizenForbidden() throws Exception {
        String citizenToken = registerAndLogin("Cit6", "cit6@incident.com", "pass1234", Role.CITIZEN);

        MvcResult submitResult = mockMvc.perform(post("/api/incidents")
                        .header("Authorization", "Bearer " + citizenToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildIncidentRequest())))
                .andExpect(status().isCreated())
                .andReturn();

        long incidentId = objectMapper.readTree(submitResult.getResponse().getContentAsString())
                .path("data").path("incidentId").asLong();

        IncidentStatusUpdateRequest updateReq = new IncidentStatusUpdateRequest();
        updateReq.setStatus(IncidentStatus.RESOLVED);

        mockMvc.perform(patch("/api/incidents/" + incidentId + "/status")
                        .header("Authorization", "Bearer " + citizenToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isForbidden());
    }

    // -----------------------------------------------------------------------
    // Tests: Initial status is REPORTED
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("Submitted incident should have initial status REPORTED")
    void testInitialIncidentStatusIsReported() throws Exception {
        String token = registerAndLogin("Cit7", "cit7@incident.com", "pass1234", Role.CITIZEN);

        MvcResult result = mockMvc.perform(post("/api/incidents")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildIncidentRequest())))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode data = objectMapper.readTree(result.getResponse().getContentAsString()).path("data");
        assert "REPORTED".equals(data.path("status").asText()) : "Expected REPORTED but got: " + data.path("status");
    }

    // -----------------------------------------------------------------------
    // Tests: GET /api/incidents/{id} & Access Control
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("GET /api/incidents/{id} should return 200 for owning citizen")
    void testCitizenCanViewOwnIncidentById() throws Exception {
        String citizenToken = registerAndLogin("CitOwner", "owner@incident.com", "pass1234", Role.CITIZEN);

        MvcResult result = mockMvc.perform(post("/api/incidents")
                        .header("Authorization", "Bearer " + citizenToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildIncidentRequest())))
                .andExpect(status().isCreated())
                .andReturn();

        long incidentId = objectMapper.readTree(result.getResponse().getContentAsString())
                .path("data").path("incidentId").asLong();

        mockMvc.perform(get("/api/incidents/" + incidentId)
                        .header("Authorization", "Bearer " + citizenToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.incidentId").value(incidentId));
    }

    @Test
    @DisplayName("GET /api/incidents/{id} should return 403 when citizen tries to view another citizen's incident")
    void testCitizenCannotViewOtherCitizenIncident() throws Exception {
        String citizen1Token = registerAndLogin("CitOne", "cit1@incident.com", "pass1234", Role.CITIZEN);
        String citizen2Token = registerAndLogin("CitTwo", "cit2@incident.com", "pass1234", Role.CITIZEN);

        MvcResult result = mockMvc.perform(post("/api/incidents")
                        .header("Authorization", "Bearer " + citizen1Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildIncidentRequest())))
                .andExpect(status().isCreated())
                .andReturn();

        long incidentId = objectMapper.readTree(result.getResponse().getContentAsString())
                .path("data").path("incidentId").asLong();

        mockMvc.perform(get("/api/incidents/" + incidentId)
                        .header("Authorization", "Bearer " + citizen2Token))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/incidents/{id} should return 200 for AUTHORITY viewing any citizen's incident")
    void testAuthorityCanViewAnyIncidentById() throws Exception {
        String citizenToken = registerAndLogin("CitSource", "citsrc@incident.com", "pass1234", Role.CITIZEN);
        String authorityToken = registerAndLogin("AuthViewer", "authview@incident.com", "pass1234", Role.AUTHORITY);

        MvcResult result = mockMvc.perform(post("/api/incidents")
                        .header("Authorization", "Bearer " + citizenToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildIncidentRequest())))
                .andExpect(status().isCreated())
                .andReturn();

        long incidentId = objectMapper.readTree(result.getResponse().getContentAsString())
                .path("data").path("incidentId").asLong();

        mockMvc.perform(get("/api/incidents/" + incidentId)
                        .header("Authorization", "Bearer " + authorityToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.incidentId").value(incidentId));
    }

    @Test
    @DisplayName("GET /api/incidents should return 403 for CITIZEN")
    void testCitizenCannotListAllIncidents() throws Exception {
        String citizenToken = registerAndLogin("CitList", "citlist@incident.com", "pass1234", Role.CITIZEN);

        mockMvc.perform(get("/api/incidents")
                        .header("Authorization", "Bearer " + citizenToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/incidents should return 200 for NGO")
    void testNgoCanListAllIncidents() throws Exception {
        String ngoToken = registerAndLogin("NGO Rep", "ngo.rep@incident.com", "pass1234", Role.NGO);

        mockMvc.perform(get("/api/incidents")
                        .header("Authorization", "Bearer " + ngoToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
