package com.disasterconnect.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class HealthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testHealthEndpointReturnsSanitizedStatus() throws Exception {
        mockMvc.perform(get("/api/health")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.application").value("DisasterConnect"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.database").value("CONNECTED"))
                // Ensure internal database details are NOT exposed
                .andExpect(jsonPath("$.databaseUrl").doesNotExist())
                .andExpect(jsonPath("$.userName").doesNotExist())
                .andExpect(jsonPath("$.databaseVersion").doesNotExist())
                .andExpect(jsonPath("$.databaseProduct").doesNotExist());
    }
}
