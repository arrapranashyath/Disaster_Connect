package com.disasterconnect.security;

import com.disasterconnect.entity.User;
import com.disasterconnect.entity.enums.Role;
import com.disasterconnect.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityRoleAccessTest {

    @Autowired
    private MockMvc mockMvc;

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
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider tokenProvider;

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

    private String citizenToken;
    private String volunteerToken;
    private String adminToken;

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

        // 1. Citizen
        User citizen = new User("Citizen User", "citizen@test.com", passwordEncoder.encode("pass123"), "111", Role.CITIZEN, "Pune");
        citizen = userRepository.save(citizen);
        citizenToken = tokenProvider.generateTokenForUser(citizen.getUserId(), citizen.getEmail(), Role.CITIZEN.name());

        // 2. Volunteer
        User volunteer = new User("Volunteer User", "volunteer@test.com", passwordEncoder.encode("pass123"), "222", Role.VOLUNTEER, "Pune");
        volunteer = userRepository.save(volunteer);
        volunteerToken = tokenProvider.generateTokenForUser(volunteer.getUserId(), volunteer.getEmail(), Role.VOLUNTEER.name());

        // 3. Admin (Created via repository directly, representing system admin)
        User admin = new User("Admin User", "admin@test.com", passwordEncoder.encode("pass123"), "999", Role.ADMIN, "HQ");
        admin = userRepository.save(admin);
        adminToken = tokenProvider.generateTokenForUser(admin.getUserId(), admin.getEmail(), Role.ADMIN.name());
    }

    @Test
    @DisplayName("Public endpoint should be accessible without authentication")
    void testPublicEndpointAccessibleWithoutToken() throws Exception {
        mockMvc.perform(get("/api/test/public"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value("PUBLIC_DATA"));
    }

    @Test
    @DisplayName("Protected endpoint without token should return 401 Unauthorized")
    void testProtectedEndpointWithoutTokenReturns401() throws Exception {
        mockMvc.perform(get("/api/test/citizen"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));

        mockMvc.perform(get("/api/test/admin"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("Citizen role should access citizen endpoint successfully")
    void testCitizenCanAccessCitizenEndpoint() throws Exception {
        mockMvc.perform(get("/api/test/citizen")
                        .header("Authorization", "Bearer " + citizenToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value("CITIZEN_DATA"));
    }

    @Test
    @DisplayName("Citizen role should be FORBIDDEN (403) from accessing admin endpoint")
    void testCitizenCannotAccessAdminEndpoint() throws Exception {
        mockMvc.perform(get("/api/test/admin")
                        .header("Authorization", "Bearer " + citizenToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Forbidden: You do not have permission to access this resource"));
    }

    @Test
    @DisplayName("Volunteer role should access volunteer endpoint successfully")
    void testVolunteerCanAccessVolunteerEndpoint() throws Exception {
        mockMvc.perform(get("/api/test/volunteer")
                        .header("Authorization", "Bearer " + volunteerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value("VOLUNTEER_DATA"));
    }

    @Test
    @DisplayName("Volunteer role should be FORBIDDEN (403) from accessing admin endpoint")
    void testVolunteerCannotAccessAdminEndpoint() throws Exception {
        mockMvc.perform(get("/api/test/admin")
                        .header("Authorization", "Bearer " + volunteerToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("Admin role should access admin endpoint successfully")
    void testAdminCanAccessAdminEndpoint() throws Exception {
        mockMvc.perform(get("/api/test/admin")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value("ADMIN_DATA"));
    }
}
