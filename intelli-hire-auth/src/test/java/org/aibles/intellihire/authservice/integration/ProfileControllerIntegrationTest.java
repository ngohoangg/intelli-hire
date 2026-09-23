package org.aibles.intellihire.authservice.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ProfileControllerIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    private String accessToken;

    @BeforeEach
    void setUp() throws Exception {
        // Register and login to get an access token
        String uniqueId = String.valueOf(System.nanoTime());
        String registerBody = String.format("""
                {
                    "email": "profile-%s@test.com",
                    "username": "profileuser%s",
                    "password": "Password1",
                    "firstName": "Profile",
                    "lastName": "Tester"
                }
                """, uniqueId, uniqueId);

        mockMvc.perform(post("/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerBody))
                .andExpect(status().isCreated());

        String loginBody = String.format("""
                {
                    "identifier": "profile-%s@test.com",
                    "password": "Password1"
                }
                """, uniqueId);

        MvcResult loginResult = mockMvc.perform(post("/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode json = objectMapper.readTree(loginResult.getResponse().getContentAsString());
        accessToken = json.at("/data/accessToken").asText();
    }

    @Test
    void getProfile_success() throws Exception {
        mockMvc.perform(get("/v1/profiles/me")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.firstName").value("Profile"));
    }

    @Test
    void updateProfile_success() throws Exception {
        String body = """
                {
                    "first_name": "Updated",
                    "last_name": "Name",
                    "phone": "1234567890"
                }
                """;

        mockMvc.perform(put("/v1/profiles/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + accessToken)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.firstName").value("Updated"));
    }

    @Test
    void getProfile_unauthorized() throws Exception {
        mockMvc.perform(get("/v1/profiles/me"))
                .andExpect(status().isUnauthorized());
    }
}
