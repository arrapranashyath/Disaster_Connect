package com.disasterconnect.controller;

import com.disasterconnect.dto.*;
import com.disasterconnect.entity.NGOProfile;
import com.disasterconnect.entity.User;
import com.disasterconnect.entity.VolunteerProfile;
import com.disasterconnect.entity.enums.*;
import com.disasterconnect.repository.*;
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
class CoordinationControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AssistanceRequestRepository assistanceRepository;

    @Autowired
    private RequestStatusLogRepository statusLogRepository;

    @Autowired
    private VolunteerProfileRepository volunteerProfileRepository;

    @Autowired
    private NGOProfileRepository ngoProfileRepository;

    @Autowired
    private IncidentReportRepository incidentReportRepository;

    @Autowired
    private DisasterAlertRepository disasterAlertRepository;

        @Autowired
    private com.disasterconnect.repository.ResourceUpdateLogRepository resourceUpdateLogRepository;

    @Autowired
    private com.disasterconnect.repository.ReliefResourceRepository reliefResourceRepository;

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
    private com.disasterconnect.repository.ShelterRepository shelterRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

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
        statusLogRepository.deleteAll();
        assistanceRepository.deleteAll();
        incidentReportRepository.deleteAll();
        disasterAlertRepository.deleteAll();
        userRepository.deleteAll();
    }

    private User createNGO(String name, String email, VerificationStatus status) {
        User user = new User(name, email, passwordEncoder.encode("pass1234"), "+919333333333", Role.NGO, "Pune");
        user = userRepository.save(user);
        NGOProfile profile = new NGOProfile(user, name + " Foundation", "REG-NGO-1234");
        profile.setVerificationStatus(status);
        ngoProfileRepository.save(profile);
        return user;
    }

    private User createVolunteer(String name, String email, VerificationStatus status) {
        User user = new User(name, email, passwordEncoder.encode("pass1234"), "+919444444444", Role.VOLUNTEER, "Pune");
        user = userRepository.save(user);
        VolunteerProfile profile = new VolunteerProfile(user, "Rescue, Driving", "AVAILABLE");
        profile.setVerificationStatus(status);
        volunteerProfileRepository.save(profile);
        return user;
    }

    private String loginUser(String email) throws Exception {
        LoginRequest login = new LoginRequest(email, "pass1234");
        MvcResult res = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andReturn();

        return objectMapper.readTree(res.getResponse().getContentAsString())
                .path("data").path("token").asText();
    }

    private Long createPendingRequest(String citizenEmail) throws Exception {
        RegisterRequest reg = new RegisterRequest("Test Citizen", citizenEmail, "pass1234", "+919555555555", Role.CITIZEN, "Pune");
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reg)))
                .andExpect(status().isCreated());

        String token = loginUser(citizenEmail);

        AssistanceCreateRequest req = new AssistanceCreateRequest();
        req.setAssistanceType(AssistanceType.MEDICAL);
        req.setDescription("Need emergency first aid supplies");
        req.setLocationName("Aundh, Pune");
        req.setPeopleCount(2);
        req.setPriority(RequestPriority.CRITICAL);

        MvcResult res = mockMvc.perform(post("/api/assistance-requests")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andReturn();

        return objectMapper.readTree(res.getResponse().getContentAsString())
                .path("data").path("requestId").asLong();
    }

    // -----------------------------------------------------------------------
    // Coordination Dashboard Tests (REQ-COOR-1)
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("GET /api/coordination/dashboard should return metrics and active requests for verified NGO")
    void testVerifiedNgoCanViewDashboard() throws Exception {
        createNGO("Seva Relief", "seva@test.com", VerificationStatus.VERIFIED);
        String ngoToken = loginUser("seva@test.com");

        // create two requests
        createPendingRequest("cit10@test.com");
        createPendingRequest("cit11@test.com");

        mockMvc.perform(get("/api/coordination/dashboard")
                        .header("Authorization", "Bearer " + ngoToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalPendingRequests").value(2))
                .andExpect(jsonPath("$.data.activeRequests.length()").value(2));
    }

    @Test
    @DisplayName("GET /api/coordination/dashboard should return 400 for unverified NGO")
    void testUnverifiedNgoCannotViewDashboard() throws Exception {
        createNGO("Pending NGO", "pending@ngo.com", VerificationStatus.PENDING);
        String ngoToken = loginUser("pending@ngo.com");

        mockMvc.perform(get("/api/coordination/dashboard")
                        .header("Authorization", "Bearer " + ngoToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("NGO account is pending verification. Only verified NGOs can coordinate requests."));
    }

    @Test
    @DisplayName("GET /api/coordination/dashboard should return 403 for CITIZEN")
    void testCitizenCannotViewCoordinationDashboard() throws Exception {
        RegisterRequest reg = new RegisterRequest("Normal Cit", "cit_dash@test.com", "pass1234", "+919666666666", Role.CITIZEN, "Pune");
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reg)))
                .andExpect(status().isCreated());

        String citizenToken = loginUser("cit_dash@test.com");

        mockMvc.perform(get("/api/coordination/dashboard")
                        .header("Authorization", "Bearer " + citizenToken))
                .andExpect(status().isForbidden());
    }

    // -----------------------------------------------------------------------
    // Volunteer Discovery and Assignment (REQ-COOR-2)
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("GET /api/coordination/volunteers should return list of verified volunteers")
    void testGetAvailableVolunteers() throws Exception {
        createNGO("Relief Trust", "trust@ngo.com", VerificationStatus.VERIFIED);
        String ngoToken = loginUser("trust@ngo.com");

        createVolunteer("Verified Vol 1", "vol_v1@test.com", VerificationStatus.VERIFIED);
        createVolunteer("Unverified Vol", "vol_un@test.com", VerificationStatus.PENDING);

        mockMvc.perform(get("/api/coordination/volunteers")
                        .header("Authorization", "Bearer " + ngoToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].email").value("vol_v1@test.com"));
    }

    @Test
    @DisplayName("POST /api/assistance-requests/{id}/assign should assign verified volunteer to pending request")
    void testAssignVerifiedVolunteerSuccess() throws Exception {
        createNGO("Helping Hands", "hands@ngo.com", VerificationStatus.VERIFIED);
        String ngoToken = loginUser("hands@ngo.com");

        User vol = createVolunteer("Task Force Vol", "taskforce@vol.com", VerificationStatus.VERIFIED);
        Long reqId = createPendingRequest("requester@test.com");

        AssignVolunteerRequest assignReq = new AssignVolunteerRequest(vol.getUserId(), "Assigned to medical response team");

        mockMvc.perform(post("/api/assistance-requests/" + reqId + "/assign")
                        .header("Authorization", "Bearer " + ngoToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(assignReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("ASSIGNED"))
                .andExpect(jsonPath("$.data.assignedTo.name").value("Task Force Vol"))
                .andExpect(jsonPath("$.data.assignedBy.name").value("Helping Hands"));
    }

    @Test
    @DisplayName("POST /api/assistance-requests/{id}/assign should return 400 when assigning unverified volunteer")
    void testCannotAssignUnverifiedVolunteer() throws Exception {
        createNGO("Care Corp", "care@ngo.com", VerificationStatus.VERIFIED);
        String ngoToken = loginUser("care@ngo.com");

        User unverifiedVol = createVolunteer("Newbie Vol", "newbie@vol.com", VerificationStatus.PENDING);
        Long reqId = createPendingRequest("requester2@test.com");

        AssignVolunteerRequest assignReq = new AssignVolunteerRequest(unverifiedVol.getUserId(), "Assign attempt");

        mockMvc.perform(post("/api/assistance-requests/" + reqId + "/assign")
                        .header("Authorization", "Bearer " + ngoToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(assignReq)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Volunteer account is pending verification. Only verified volunteers can accept or coordinate requests."));
    }

    // -----------------------------------------------------------------------
    // Volunteer Task List (REQ-COOR-3)
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("GET /api/coordination/my-tasks should return tasks assigned to calling volunteer")
    void testVolunteerCanViewAssignedTasks() throws Exception {
        createNGO("Hope NGO", "hope@ngo.com", VerificationStatus.VERIFIED);
        String ngoToken = loginUser("hope@ngo.com");

        User vol = createVolunteer("Field Worker", "field@vol.com", VerificationStatus.VERIFIED);
        String volToken = loginUser("field@vol.com");

        Long reqId = createPendingRequest("needy@test.com");

        // NGO assigns field worker
        AssignVolunteerRequest assignReq = new AssignVolunteerRequest(vol.getUserId(), "Go immediately");
        mockMvc.perform(post("/api/assistance-requests/" + reqId + "/assign")
                        .header("Authorization", "Bearer " + ngoToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(assignReq)))
                .andExpect(status().isOk());

        // Field worker views tasks
        mockMvc.perform(get("/api/coordination/my-tasks")
                        .header("Authorization", "Bearer " + volToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.content[0].requestId").value(reqId))
                .andExpect(jsonPath("$.data.content[0].status").value("ASSIGNED"));
    }
}
