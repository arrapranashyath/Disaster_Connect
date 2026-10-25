package com.disasterconnect.controller;

import com.disasterconnect.dto.*;
import com.disasterconnect.entity.NGOProfile;
import com.disasterconnect.entity.User;
import com.disasterconnect.entity.VolunteerProfile;
import com.disasterconnect.entity.enums.*;
import com.disasterconnect.repository.*;
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
class AssistanceRequestControllerIntegrationTest {

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

    private String createCitizenToken(String name, String email) throws Exception {
        RegisterRequest reg = new RegisterRequest(name, email, "pass1234", "+919111111111", Role.CITIZEN, "Pune");
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reg)))
                .andExpect(status().isCreated());

        LoginRequest login = new LoginRequest(email, "pass1234");
        MvcResult res = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andReturn();

        return objectMapper.readTree(res.getResponse().getContentAsString())
                .path("data").path("token").asText();
    }

    private User createVolunteer(String name, String email, VerificationStatus status) {
        User user = new User(name, email, passwordEncoder.encode("pass1234"), "+919222222222", Role.VOLUNTEER, "Pune");
        user = userRepository.save(user);
        VolunteerProfile profile = new VolunteerProfile(user, "First Aid, Rescue", "AVAILABLE");
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

    private AssistanceCreateRequest buildRequest() {
        AssistanceCreateRequest req = new AssistanceCreateRequest();
        req.setAssistanceType(AssistanceType.FOOD);
        req.setDescription("Need food and drinking water for a family stranded by flood.");
        req.setLocationName("Shivaji Nagar, Pune");
        req.setLatitude(18.5314);
        req.setLongitude(73.8446);
        req.setPeopleCount(4);
        req.setPriority(RequestPriority.HIGH);
        return req;
    }

    // -----------------------------------------------------------------------
    // Creation Tests (REQ-HELP-1)
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("POST /api/assistance-requests should create request for authenticated citizen")
    void testCreateAssistanceRequestSuccess() throws Exception {
        String citizenToken = createCitizenToken("Ramesh Citizen", "ramesh@test.com");

        mockMvc.perform(post("/api/assistance-requests")
                        .header("Authorization", "Bearer " + citizenToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.assistanceType").value("FOOD"))
                .andExpect(jsonPath("$.data.status").value("PENDING"))
                .andExpect(jsonPath("$.data.priority").value("HIGH"))
                .andExpect(jsonPath("$.data.peopleCount").value(4))
                .andExpect(jsonPath("$.data.citizen.name").value("Ramesh Citizen"));
    }

    @Test
    @DisplayName("POST /api/assistance-requests should return 400 when required fields are missing")
    void testCreateAssistanceRequestValidationFailure() throws Exception {
        String citizenToken = createCitizenToken("Suresh Citizen", "suresh@test.com");

        AssistanceCreateRequest bad = new AssistanceCreateRequest();
        // missing assistanceType, description, locationName

        mockMvc.perform(post("/api/assistance-requests")
                        .header("Authorization", "Bearer " + citizenToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bad)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/assistance-requests should return 401 when unauthenticated")
    void testCreateAssistanceRequestUnauthenticated() throws Exception {
        mockMvc.perform(post("/api/assistance-requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildRequest())))
                .andExpect(status().isUnauthorized());
    }

    // -----------------------------------------------------------------------
    // Retrieval & Privacy Tests (REQ-HELP-2)
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("GET /api/assistance-requests/my should return only the calling citizen's requests")
    void testGetMyRequests() throws Exception {
        String citizenToken = createCitizenToken("Pooja Citizen", "pooja@test.com");

        mockMvc.perform(post("/api/assistance-requests")
                        .header("Authorization", "Bearer " + citizenToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildRequest())))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/assistance-requests/my")
                        .header("Authorization", "Bearer " + citizenToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.content[0].locationName").value("Shivaji Nagar, Pune"));
    }

    @Test
    @DisplayName("GET /api/assistance-requests/{id} should return 403 when citizen views another citizen's request")
    void testCitizenCannotViewOtherCitizenRequest() throws Exception {
        String citizen1Token = createCitizenToken("Cit A", "cita@test.com");
        String citizen2Token = createCitizenToken("Cit B", "citb@test.com");

        MvcResult res = mockMvc.perform(post("/api/assistance-requests")
                        .header("Authorization", "Bearer " + citizen1Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildRequest())))
                .andExpect(status().isCreated())
                .andReturn();

        long reqId = objectMapper.readTree(res.getResponse().getContentAsString())
                .path("data").path("requestId").asLong();

        mockMvc.perform(get("/api/assistance-requests/" + reqId)
                        .header("Authorization", "Bearer " + citizen2Token))
                .andExpect(status().isForbidden());
    }

    // -----------------------------------------------------------------------
    // Acceptance & Mutual Exclusion Tests (REQ-HELP-3, REQ-COOR-4)
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("POST /api/assistance-requests/{id}/accept should succeed for verified volunteer")
    void testVerifiedVolunteerCanAcceptRequest() throws Exception {
        String citizenToken = createCitizenToken("Neha Citizen", "neha@test.com");
        User vol = createVolunteer("Anil Volunteer", "anil@test.com", VerificationStatus.VERIFIED);
        String volToken = loginUser("anil@test.com");

        MvcResult res = mockMvc.perform(post("/api/assistance-requests")
                        .header("Authorization", "Bearer " + citizenToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildRequest())))
                .andExpect(status().isCreated())
                .andReturn();

        long reqId = objectMapper.readTree(res.getResponse().getContentAsString())
                .path("data").path("requestId").asLong();

        // Volunteer accepts request
        mockMvc.perform(post("/api/assistance-requests/" + reqId + "/accept")
                        .header("Authorization", "Bearer " + volToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("ACCEPTED"))
                .andExpect(jsonPath("$.data.assignedTo.name").value("Anil Volunteer"));
    }

    @Test
    @DisplayName("POST /api/assistance-requests/{id}/accept should return 400 for unverified volunteer")
    void testUnverifiedVolunteerCannotAcceptRequest() throws Exception {
        String citizenToken = createCitizenToken("Kavita Citizen", "kavita@test.com");
        createVolunteer("Unverified Vol", "unverified@test.com", VerificationStatus.PENDING);
        String volToken = loginUser("unverified@test.com");

        MvcResult res = mockMvc.perform(post("/api/assistance-requests")
                        .header("Authorization", "Bearer " + citizenToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildRequest())))
                .andExpect(status().isCreated())
                .andReturn();

        long reqId = objectMapper.readTree(res.getResponse().getContentAsString())
                .path("data").path("requestId").asLong();

        mockMvc.perform(post("/api/assistance-requests/" + reqId + "/accept")
                        .header("Authorization", "Bearer " + volToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Volunteer account is pending verification. Only verified volunteers can accept or coordinate requests."));
    }

    @Test
    @DisplayName("REQ-COOR-4: Second responder cannot accept already accepted request")
    void testMutualExclusionOnAcceptRequest() throws Exception {
        String citizenToken = createCitizenToken("Deepak Citizen", "deepak@test.com");
        createVolunteer("Vol One", "vol1@test.com", VerificationStatus.VERIFIED);
        createVolunteer("Vol Two", "vol2@test.com", VerificationStatus.VERIFIED);
        String vol1Token = loginUser("vol1@test.com");
        String vol2Token = loginUser("vol2@test.com");

        MvcResult res = mockMvc.perform(post("/api/assistance-requests")
                        .header("Authorization", "Bearer " + citizenToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildRequest())))
                .andExpect(status().isCreated())
                .andReturn();

        long reqId = objectMapper.readTree(res.getResponse().getContentAsString())
                .path("data").path("requestId").asLong();

        // Vol 1 accepts
        mockMvc.perform(post("/api/assistance-requests/" + reqId + "/accept")
                        .header("Authorization", "Bearer " + vol1Token))
                .andExpect(status().isOk());

        // Vol 2 attempts to accept same request -> rejected per REQ-COOR-4
        mockMvc.perform(post("/api/assistance-requests/" + reqId + "/accept")
                        .header("Authorization", "Bearer " + vol2Token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("This request has already been accepted or assigned to another responder."));
    }

    // -----------------------------------------------------------------------
    // Status Transitions & Audit History (REQ-HELP-4, REQ-HELP-6)
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("PATCH /api/assistance-requests/{id}/status should update status and track audit history")
    void testUpdateStatusAndAuditTrail() throws Exception {
        String citizenToken = createCitizenToken("Amit Citizen", "amit@test.com");
        createVolunteer("Vijay Volunteer", "vijay@test.com", VerificationStatus.VERIFIED);
        String volToken = loginUser("vijay@test.com");

        MvcResult res = mockMvc.perform(post("/api/assistance-requests")
                        .header("Authorization", "Bearer " + citizenToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildRequest())))
                .andExpect(status().isCreated())
                .andReturn();

        long reqId = objectMapper.readTree(res.getResponse().getContentAsString())
                .path("data").path("requestId").asLong();

        // Accept -> ACCEPTED
        mockMvc.perform(post("/api/assistance-requests/" + reqId + "/accept")
                        .header("Authorization", "Bearer " + volToken))
                .andExpect(status().isOk());

        // Update -> IN_PROGRESS
        AssistanceStatusUpdateRequest inProgressReq = new AssistanceStatusUpdateRequest(RequestStatus.IN_PROGRESS, "On the way with food supplies");
        mockMvc.perform(patch("/api/assistance-requests/" + reqId + "/status")
                        .header("Authorization", "Bearer " + volToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inProgressReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("IN_PROGRESS"));

        // Update -> COMPLETED
        AssistanceStatusUpdateRequest completedReq = new AssistanceStatusUpdateRequest(RequestStatus.COMPLETED, "Food delivered successfully");
        mockMvc.perform(patch("/api/assistance-requests/" + reqId + "/status")
                        .header("Authorization", "Bearer " + volToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(completedReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("COMPLETED"));

        // Verify audit history per REQ-HELP-6
        mockMvc.perform(get("/api/assistance-requests/" + reqId)
                        .header("Authorization", "Bearer " + volToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.statusHistory.length()").value(4)); // Created, Accepted, InProgress, Completed
    }

    // -----------------------------------------------------------------------
    // Cancellation Tests
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("PATCH /api/assistance-requests/{id}/cancel should allow citizen to cancel pending request")
    void testCancelPendingRequest() throws Exception {
        String citizenToken = createCitizenToken("Rohan Citizen", "rohan@test.com");

        MvcResult res = mockMvc.perform(post("/api/assistance-requests")
                        .header("Authorization", "Bearer " + citizenToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildRequest())))
                .andExpect(status().isCreated())
                .andReturn();

        long reqId = objectMapper.readTree(res.getResponse().getContentAsString())
                .path("data").path("requestId").asLong();

        mockMvc.perform(patch("/api/assistance-requests/" + reqId + "/cancel")
                        .param("reason", "Neighbour helped us")
                        .header("Authorization", "Bearer " + citizenToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("CANCELLED"));
    }
}
