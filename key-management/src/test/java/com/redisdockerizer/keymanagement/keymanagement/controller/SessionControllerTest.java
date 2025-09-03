package com.redisdockerizer.keymanagement.keymanagement.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.redisdockerizer.keymanagement.keymanagement.dto.LoginRequest;
import com.redisdockerizer.keymanagement.keymanagement.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith(MockitoExtension.class)
class SessionControllerTest {

    private static final String BASE_URL = "/api/sessions";
    private final Map<Long, User> users = new HashMap<>();
    private final Map<String, User> usersByUsername = new HashMap<>();

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        initializeMockData();
    }

    private void initializeMockData() {
        User user1 = new User(2L,
                "john_doe",
                "password123",
                "john@example.com",
                List.of("USER"),
                true
        );
        User user2 = new User(3L,
                "jane_smith",
                "password456",
                "jane@example.com",
                Arrays.asList("USER", "MANAGER"),
                true
        );

        users.put(2L, user1);
        users.put(3L, user2);

        usersByUsername.put("john_doe", user1);
        usersByUsername.put("jane_smith", user2);
    }

    @Test
    void givenValidCredentials_whenLogin_thenReturnsLoginResponse() throws Exception {
        LoginRequest request = new LoginRequest("john_doe", "password123");

        MockHttpServletRequestBuilder mockRequest = MockMvcRequestBuilders
                .post(BASE_URL + "/login")
                .content(objectMapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-Forwarded-For", "203.0.113.10");

        mockMvc.perform(mockRequest)
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.sessionId").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$.username").value("john_doe"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.roles").isArray())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Login successful"));
    }

    @Test
    void givenExistingSession_whenGetById_thenReturnsSessionDetails() throws Exception {
        LoginRequest request = new LoginRequest("jane_smith", "password456");
        String response = mockMvc.perform(MockMvcRequestBuilders.post(BASE_URL + "/login")
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andReturn().getResponse().getContentAsString();

        String sessionId = objectMapper.readTree(response).get("sessionId").asText();

        MockHttpServletRequestBuilder mockRequest = MockMvcRequestBuilders
                .get(BASE_URL + "/" + sessionId)
                .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(mockRequest)
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.sessionId").value(sessionId));
    }

    @Test
    void givenExistingSession_whenLogout_thenReturnsMessage() throws Exception {
        // login
        LoginRequest request = new LoginRequest("jane_smith", "password456");
        String response = mockMvc.perform(MockMvcRequestBuilders.post(BASE_URL + "/login")
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andReturn().getResponse().getContentAsString();

        String sessionId = objectMapper.readTree(response).get("sessionId").asText();

        // logout
        MockHttpServletRequestBuilder mockRequest = MockMvcRequestBuilders
                .delete(BASE_URL + "/" + sessionId + "/logout")
                .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(mockRequest)
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Logout successful"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.sessionId").value(sessionId));
    }

    @Test
    void whenGetAllActiveSessions_thenReturnsSetAndCount() throws Exception {
        MockHttpServletRequestBuilder mockRequest = MockMvcRequestBuilders
                .get(BASE_URL + "/active")
                .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(mockRequest)
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.activeSessions").isArray())
                .andExpect(MockMvcResultMatchers.jsonPath("$.count").isNumber());
    }

    @Test
    void givenUserId_whenGetUserSessions_thenReturnsListAndCount() throws Exception {
        MockHttpServletRequestBuilder mockRequest = MockMvcRequestBuilders
                .get(BASE_URL + "/user/{userId}", 2L)
                .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(mockRequest)
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.userId").value(2))
                .andExpect(MockMvcResultMatchers.jsonPath("$.activeSessions").isArray())
                .andExpect(MockMvcResultMatchers.jsonPath("$.count").isNumber());
    }

    @Test
    void givenExistingSession_whenExtend_thenReturnsExtendSessionResponse() throws Exception {
        // login
        LoginRequest request = new LoginRequest("john_doe", "password123");
        String response = mockMvc.perform(MockMvcRequestBuilders.post(BASE_URL + "/login")
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andReturn().getResponse().getContentAsString();

        String sessionId = objectMapper.readTree(response).get("sessionId").asText();

        // extend
        MockHttpServletRequestBuilder mockRequest = MockMvcRequestBuilders
                .put(BASE_URL + "/" + sessionId + "/extend")
                .param("minutes", "45")
                .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(mockRequest)
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Session extended successfully"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.sessionId").value(sessionId))
                .andExpect(MockMvcResultMatchers.jsonPath("$.extendedByMinutes").value(45));
    }

    @Test
    void givenUserId_whenTerminateAll_thenReturnsTerminateAllResponse() throws Exception {
        MockHttpServletRequestBuilder mockRequest = MockMvcRequestBuilders
                .delete(BASE_URL + "/user/{userId}/terminate-all", 2L)
                .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(mockRequest)
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("User sessions terminated"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.userId").value(2))
                .andExpect(MockMvcResultMatchers.jsonPath("$.terminatedCount").isNumber());
    }

    @Test
    void givenValidSession_whenValidate_thenReturnsValidTrue() throws Exception {
        // login
        LoginRequest request = new LoginRequest("john_doe", "password123");
        String response = mockMvc.perform(MockMvcRequestBuilders.post(BASE_URL + "/login")
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andReturn().getResponse().getContentAsString();

        String sessionId = objectMapper.readTree(response).get("sessionId").asText();

        // validate
        MockHttpServletRequestBuilder mockRequest = MockMvcRequestBuilders
                .post(BASE_URL + "/validate")
                .param("sessionId", sessionId)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED);

        mockMvc.perform(mockRequest)
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.valid").value(true))
                .andExpect(MockMvcResultMatchers.jsonPath("$.userId").isNumber())
                .andExpect(MockMvcResultMatchers.jsonPath("$.username").isString())
                .andExpect(MockMvcResultMatchers.jsonPath("$.roles").isArray());
    }

}