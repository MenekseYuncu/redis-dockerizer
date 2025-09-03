package com.redisdockerizer.sessionmanagement.session.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.redisdockerizer.sessionmanagement.session.repository.UserRepository;
import com.redisdockerizer.sessionmanagement.session.user.User;
import org.hamcrest.Matchers;
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

import java.time.Instant;

@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith(MockitoExtension.class)
class SessionControllerTest {

    private static final String BASE_URL = "/api";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void seedData() {
        userRepository.deleteAll();

        userRepository.save(new User("u20000", "john_doe", "password123", "user", Instant.now(), true));
        userRepository.save(new User("u20001", "jane_smith", "password456", "user", Instant.now(), true));
        userRepository.save(new User("u20003", "alice", "passwor789", "user", Instant.now(), true));
    }

    @Test
    void givenUserId_whenSetOnline_thenReturnsSessionResponse() throws Exception {
        MockHttpServletRequestBuilder req = MockMvcRequestBuilders
                .post(BASE_URL + "/sessions/{userId}/online", "u20000")
                .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(req)
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").isString())
                .andExpect(MockMvcResultMatchers.jsonPath("$.userId").value("u20000"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.username").isString())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value("online"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.ttlSeconds").isNumber());
    }

    @Test
    void givenOnlineUser_whenRefreshTtl_thenReturnsUpdatedTtl() throws Exception {
        // online
        mockMvc.perform(MockMvcRequestBuilders
                        .post(BASE_URL + "/sessions/{userId}/online", "u20000")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk());

        // refresh ttl
        MockHttpServletRequestBuilder req = MockMvcRequestBuilders
                .post(BASE_URL + "/sessions/{userId}/refresh-ttl", "u20000")
                .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(req)
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").isString())
                .andExpect(MockMvcResultMatchers.jsonPath("$.userId").value("u20000"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value("refreshed"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.ttlSeconds", Matchers.greaterThan(0)));
    }


    @Test
    void givenExistingSession_whenSetOffline_thenReturnsSessionResponse() throws Exception {
        // online
        mockMvc.perform(MockMvcRequestBuilders
                        .post(BASE_URL + "/sessions/{userId}/online", "u20001")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk());

        // offline
        MockHttpServletRequestBuilder req = MockMvcRequestBuilders
                .post(BASE_URL + "/sessions/{userId}/offline", "u20001")
                .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(req)
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").isString())
                .andExpect(MockMvcResultMatchers.jsonPath("$.userId").value("u20001"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.username").isString())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value("offline"));
    }


    @Test
    void whenGetAllUsers_thenReturnsArray() throws Exception {
        // online
        mockMvc.perform(MockMvcRequestBuilders.post(BASE_URL + "/sessions/{id}/online", "u20000")
                .contentType(MediaType.APPLICATION_JSON));
        mockMvc.perform(MockMvcRequestBuilders.post(BASE_URL + "/sessions/{id}/online", "u20001")
                .contentType(MediaType.APPLICATION_JSON));

        MockHttpServletRequestBuilder req = MockMvcRequestBuilders
                .get(BASE_URL + "/users")
                .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(req)
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$").isArray())
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].username").isString())
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].email").isString())
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].is_online").isBoolean());
    }


    @Test
    void whenGetOnlineUsers_thenReturnsList() throws Exception {
        // onlind
        mockMvc.perform(MockMvcRequestBuilders.post(BASE_URL + "/sessions/{id}/online", "u20001")
                .contentType(MediaType.APPLICATION_JSON));
        mockMvc.perform(MockMvcRequestBuilders.post(BASE_URL + "/sessions/{id}/online", "u20000")
                .contentType(MediaType.APPLICATION_JSON));


        MockHttpServletRequestBuilder req = MockMvcRequestBuilders
                .get(BASE_URL + "/sessions/online")
                .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(req)
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$").isArray())
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].is_online").value(true));
    }


    @Test
    void whenGetOfflineUsers_thenReturnsList() throws Exception {
        // online
        mockMvc.perform(MockMvcRequestBuilders.post(BASE_URL + "/sessions/{id}/online", "u20001")
                .contentType(MediaType.APPLICATION_JSON));
        mockMvc.perform(MockMvcRequestBuilders.post(BASE_URL + "/sessions/{id}/offline", "u20000")
                .contentType(MediaType.APPLICATION_JSON));

        MockHttpServletRequestBuilder req = MockMvcRequestBuilders
                .get(BASE_URL + "/sessions/offline")
                .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(req)
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$").isArray())
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].is_online").value(false));
    }


    @Test
    void whenGetSessionStats_thenReturnsAggregates() throws Exception {
        // online
        mockMvc.perform(MockMvcRequestBuilders.post(BASE_URL + "/sessions/{id}/online", "u20001")
                .contentType(MediaType.APPLICATION_JSON));
        mockMvc.perform(MockMvcRequestBuilders.post(BASE_URL + "/sessions/{id}/online", "u20000")
                .contentType(MediaType.APPLICATION_JSON));
        mockMvc.perform(MockMvcRequestBuilders.post(BASE_URL + "/sessions/{id}/offline", "u20003")
                .contentType(MediaType.APPLICATION_JSON));

        MockHttpServletRequestBuilder req = MockMvcRequestBuilders
                .get(BASE_URL + "/sessions/stats")
                .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(req)
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.totalUsers").isNumber())
                .andExpect(MockMvcResultMatchers.jsonPath("$.onlineUsers").isNumber())
                .andExpect(MockMvcResultMatchers.jsonPath("$.offlineUsers").isNumber())
                .andExpect(MockMvcResultMatchers.jsonPath("$.onlinePercentage").isNumber());
    }


    @Test
    void givenUserId_whenRemoveUser_thenReturnsRemovalResponse() throws Exception {
        //online
        mockMvc.perform(MockMvcRequestBuilders.post(BASE_URL + "/sessions/{id}/online", "u20003")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk());

        //delete
        MockHttpServletRequestBuilder req = MockMvcRequestBuilders
                .delete(BASE_URL + "/users/{userId}", "u20003")
                .contentType(MediaType.APPLICATION_JSON);

        String resp = mockMvc.perform(req)
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").isString())
                .andExpect(MockMvcResultMatchers.jsonPath("$.userId").value("u20003"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.username").isString())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value("removed"))
                .andReturn().getResponse().getContentAsString();

        JsonNode node = objectMapper.readTree(resp);
        assert !node.get("message").asText().isEmpty();
    }
}