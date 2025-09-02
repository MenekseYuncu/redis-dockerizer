package com.redisdockerizer.pubsub.pubsub.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.redisdockerizer.pubsub.pubsub.model.PublishMessageRequest;
import com.redisdockerizer.pubsub.pubsub.subscriber.MetricsSubscriber;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.shaded.org.awaitility.Awaitility;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicLong;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith(MockitoExtension.class)
class ChatControllerEndToEndTest {

    private static final String BASE_URL = "/api/pubsub";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MetricsSubscriber metrics;


    @Test
    void givenValidRequest_whenPublishMessage_thenReturnsConfirmation() throws Exception {
        PublishMessageRequest request = new PublishMessageRequest(
                "demo.chat.general",
                "alice",
                "Hello from integration test!"
        );

        mockMvc.perform(post(BASE_URL + "/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("Message published to demo.chat.general"));
    }


    @Test
    void givenValidRequests_whenPublishBatch_thenReturnsBatchConfirmation() throws Exception {
        PublishMessageRequest msg1 = new PublishMessageRequest(
                "demo.chat.general",
                "alice",
                "Batch message 1"
        );

        PublishMessageRequest msg2 = new PublishMessageRequest(
                "demo.chat.random",
                "charlie",
                "Batch message 2"
        );

        mockMvc.perform(post(BASE_URL + "/messages/batch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new PublishMessageRequest[]{msg1, msg2})))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("2 messages published."));
    }

    @Test
    void replayStartupMessages_replaysMessagesSuccessfully() throws Exception {

        mockMvc.perform(post(BASE_URL + "/messages/replay"))
                .andExpect(status().isOk())
                .andExpect(content().string("100 startup messages replayed to demo.chat.general."));
    }

    @Test
    void givenRoom_whenSubscribe_thenReturnsConfirmation() throws Exception {
        String roomName = "devops";

        mockMvc.perform(post(BASE_URL + "/channels/" + roomName + "/subscribe"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("Subscribed to channel: devops"));
    }

    @Test
    void givenRoom_whenUnsubscribe_thenReturnsConfirmation() throws Exception {
        String roomName = "devops";

        mockMvc.perform(post(BASE_URL + "/channels/" + roomName + "/unsubscribe"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("Unsubscribed from channel: devops"));
    }

    @Test
    void whenGetSubscriptions_thenReturnsJsonArray() throws Exception {

        mockMvc.perform(get(BASE_URL + "/channels/subscriptions"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content().string(containsString("demo.chat.")));
    }


    @Test
    void metricsShouldIncreaseAfterSinglePublish() throws Exception {

        String roomName = "backend";

        mockMvc.perform(post(BASE_URL + "/channels/" + roomName + "/subscribe"))
                .andExpect(status().isOk());

        long before = metrics.getMessageCounts()
                .getOrDefault("backend", new AtomicLong(0))
                .get();

        PublishMessageRequest request = new PublishMessageRequest(
                "demo.chat.backend",
                "alice",
                "Hello from integration test!"
        );

        mockMvc.perform(post(BASE_URL + "/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        Awaitility.await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
            long after = metrics.getMessageCounts()
                    .getOrDefault("backend", new AtomicLong(0))
                    .get();
            org.assertj.core.api.Assertions.assertThat(after).isGreaterThan(before);
        });

        mockMvc.perform(get(BASE_URL + "/metrics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalMessageCountsByRoom.backend")
                        .value(org.hamcrest.Matchers.greaterThan((int) before)));
    }


    @Test
    void whenGetHealth_thenContainsHealthy() throws Exception {
        mockMvc.perform(get(BASE_URL + "/health"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("healthy")));
    }

}