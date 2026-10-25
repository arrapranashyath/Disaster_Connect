package com.disasterconnect.controller;

import com.disasterconnect.dto.LoginRequest;
import com.disasterconnect.dto.RegisterRequest;
import com.disasterconnect.entity.enums.Role;
import com.disasterconnect.repository.UserRepository;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

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
    private com.disasterconnect.repository.ResourceUpdateLogRepository resourceUpdateLogRepository;

    @Autowired
    private com.disasterconnect.repository.ReliefResourceRepository reliefResourceRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private com.disasterconnect.repository.NGOProfileRepository ngoProfileRepository;

    @Autowired
    private com.disasterconnect.repository.VolunteerProfileRepository volunteerProfileRepository;

    @Autowired
    private com.disasterconnect.repository.IncidentReportRepository incidentReportRepository;

    @Autowired
    private com.disasterconnect.repository.DisasterAlertRepository disasterAlertRepository;

    @Autowired
    private com.disasterconnect.repository.AssistanceRequestRepository assistanceRequestRepository;

    @Autowired
    private com.disasterconnect.repository.RequestStatusLogRepository requestStatusLogRepository;

    @BeforeEach
    void setUp() {
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
    }

    @Test
    @DisplayName("POST /api/auth/register should create user and return 201 with JWT")
    void testRegisterCitizenSuccess() throws Exception {
        RegisterRequest request = new RegisterRequest(
                "John Citizen",
                "john.citizen@example.com",
                "securePass123",
                "+919876543210",
                Role.CITIZEN,
                "Kothrud, Pune"
        );

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.token").exists())
                .andExpect(jsonPath("$.data.user.name").value("John Citizen"))
                .andExpect(jsonPath("$.data.user.email").value("john.citizen@example.com"))
                .andExpect(jsonPath("$.data.user.role").value("CITIZEN"))
                // Ensure password hash is NEVER exposed
                .andExpect(jsonPath("$.data.user.password").doesNotExist())
                .andExpect(jsonPath("$.data.user.passwordHash").doesNotExist());
    }

    @Test
    @DisplayName("POST /api/auth/register with duplicate email should return 409 Conflict")
    void testRegisterDuplicateEmailReturns409() throws Exception {
        RegisterRequest request = new RegisterRequest(
                "Initial User",
                "duplicate.test@example.com",
                "pass123456",
                "+919876543210",
                Role.CITIZEN,
                "Shivajinagar, Pune"
        );

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        // Attempt registering second user with same email
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Email is already registered: duplicate.test@example.com"));
    }

    @Test
    @DisplayName("POST /api/auth/register with ADMIN role should return 400 Bad Request")
    void testRegisterAdminFailsWith400() throws Exception {
        RegisterRequest request = new RegisterRequest(
                "Fake Admin",
                "fake.admin@example.com",
                "pass123456",
                "+919876543210",
                Role.ADMIN,
                "Control Room"
        );

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Self-registration as ADMIN is not permitted."));
    }

    @Test
    @DisplayName("POST /api/auth/login with valid credentials returns 200 and JWT")
    void testLoginSuccess() throws Exception {
        // Register user first
        RegisterRequest regRequest = new RegisterRequest(
                "Alice",
                "alice@example.com",
                "mypassword123",
                "+919876543210",
                Role.CITIZEN,
                "Aundh, Pune"
        );
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(regRequest)))
                .andExpect(status().isCreated());

        // Login
        LoginRequest loginRequest = new LoginRequest("alice@example.com", "mypassword123");
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.token").exists())
                .andExpect(jsonPath("$.data.user.email").value("alice@example.com"))
                .andExpect(jsonPath("$.data.user.role").value("CITIZEN"));
    }

    @Test
    @DisplayName("POST /api/auth/login with invalid password returns 401 Unauthorized")
    void testLoginInvalidCredentialsReturns401() throws Exception {
        RegisterRequest regRequest = new RegisterRequest(
                "Bob",
                "bob@example.com",
                "correctPassword",
                "+919876543210",
                Role.CITIZEN,
                "Baner, Pune"
        );
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(regRequest)))
                .andExpect(status().isCreated());

        // Bad password
        LoginRequest badLogin = new LoginRequest("bob@example.com", "wrongPassword");
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(badLogin)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Invalid email or password"));
    }

    @Test
    @DisplayName("GET /api/auth/me with valid Bearer token returns profile")
    void testGetCurrentUserWithValidToken() throws Exception {
        RegisterRequest regRequest = new RegisterRequest(
                "Charlie",
                "charlie@example.com",
                "charliePass123",
                "+919876543210",
                Role.CITIZEN,
                "Viman Nagar, Pune"
        );
        MvcResult regResult = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(regRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode jsonNode = objectMapper.readTree(regResult.getResponse().getContentAsString());
        String token = jsonNode.get("data").get("token").asText();

        // Access protected /api/auth/me
        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value("charlie@example.com"))
                .andExpect(jsonPath("$.data.name").value("Charlie"));
    }

    @Test
    @DisplayName("GET /api/auth/me without token returns 401 Unauthorized")
    void testGetCurrentUserWithoutTokenReturns401() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("POST /api/auth/register for NGO creates profile with PENDING status")
    void testRegisterNgoSuccessAndReturnsPendingStatus() throws Exception {
        RegisterRequest request = new RegisterRequest(
                "Relief Direct Org",
                "info@reliefdirect.org",
                "securePass123",
                "+919876543210",
                Role.NGO,
                "Kerala, India"
        );
        request.setOrgName("Relief Direct Society");
        request.setRegistrationNumber("REG-NGO-8821");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.user.role").value("NGO"))
                .andExpect(jsonPath("$.data.user.orgName").value("Relief Direct Society"))
                .andExpect(jsonPath("$.data.user.registrationNumber").value("REG-NGO-8821"))
                .andExpect(jsonPath("$.data.user.verificationStatus").value("PENDING"));
    }

    @Test
    @DisplayName("POST /api/auth/register for Volunteer creates profile with skills")
    void testRegisterVolunteerSuccess() throws Exception {
        RegisterRequest request = new RegisterRequest(
                "Sara Connor",
                "sara@volunteers.org",
                "securePass123",
                "+919876543210",
                Role.VOLUNTEER,
                "Pune, India"
        );
        request.setSkills("Paramedic, Water Rescue");
        request.setAvailability("AVAILABLE");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.user.role").value("VOLUNTEER"))
                .andExpect(jsonPath("$.data.user.skills").value("Paramedic, Water Rescue"))
                .andExpect(jsonPath("$.data.user.availability").value("AVAILABLE"))
                .andExpect(jsonPath("$.data.user.verificationStatus").value("PENDING"));
    }
}
