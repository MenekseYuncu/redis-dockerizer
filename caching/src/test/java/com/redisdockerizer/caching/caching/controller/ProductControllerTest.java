package com.redisdockerizer.caching.caching.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.redisdockerizer.caching.caching.model.Product;
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

import java.math.BigDecimal;
import java.util.UUID;

@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith(MockitoExtension.class)
class ProductControllerTest {

    private static final String BASE_ENDPOINT = "/api/products";
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void givenValidProduct_whenCreateProduct_thenReturnsCreatedProduct() throws Exception {
        Product request = new Product(
                null,
                "Keyboard",
                "electronics",
                BigDecimal.valueOf(490.99),
                "Mechanical keyboard"
        );

        MockHttpServletRequestBuilder mockRequest = MockMvcRequestBuilders
                .post(BASE_ENDPOINT)
                .content(objectMapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(mockRequest)
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").value("Keyboard"));
    }

    @Test
    void whenGetAllProducts_thenReturnsList() throws Exception {
        MockHttpServletRequestBuilder mockRequest = MockMvcRequestBuilders
                .get(BASE_ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(mockRequest)
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$").isArray());
    }

    @Test
    void givenCreatedProduct_whenGetById_thenReturnsProduct() throws Exception {
        // create
        Product request = new Product(
                null,
                "Mouse",
                "electronics",
                BigDecimal.valueOf(19.99),
                "Silent mouse"
        );
        String response = mockMvc.perform(MockMvcRequestBuilders.post(BASE_ENDPOINT)
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andReturn().getResponse().getContentAsString();

        Product created = objectMapper.readValue(response, Product.class);

        // get by id
        MockHttpServletRequestBuilder mockRequest = MockMvcRequestBuilders
                .get(BASE_ENDPOINT + "/" + created.getId())
                .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(mockRequest)
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(created.getId().toString()));
    }


    @Test
    void givenCreatedProduct_whenUpdate_thenReturnsUpdatedProduct() throws Exception {
        // Given
        Product request = new Product(
                null,
                "Headset",
                "electronics",
                BigDecimal.valueOf(79.90),
                "Gaming headset"
        );

        MockHttpServletRequestBuilder createReq = MockMvcRequestBuilders
                .post(BASE_ENDPOINT)
                .content(objectMapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON);

        String createBody = mockMvc.perform(createReq)
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").isNotEmpty())
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").value("Headset"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Product created = objectMapper.readValue(createBody, Product.class);
        UUID createdId = created.getId();

        mockMvc.perform(MockMvcRequestBuilders.get(BASE_ENDPOINT + "/" + createdId))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(createdId.toString()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").value("Headset"));

        // Then
        Product updateRequest = new Product(
                null,
                "Headset Pro",
                "electronics",
                BigDecimal.valueOf(99.90),
                "Studio headset"
        );

        MockHttpServletRequestBuilder updateReq = MockMvcRequestBuilders
                .put(BASE_ENDPOINT + "/" + createdId)
                .content(objectMapper.writeValueAsString(updateRequest))
                .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(updateReq)
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(createdId.toString()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").value("Headset Pro"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.category").value("electronics"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.price").value(99.9))
                .andExpect(MockMvcResultMatchers.jsonPath("$.description").value("Studio headset"));
    }


    @Test
    void givenCreatedProduct_whenDelete_thenReturnsNoContent() throws Exception {
        // create
        Product request = new Product(
                null,
                "Webcam",
                "electronics",
                BigDecimal.valueOf(39.50),
                "HD webcam"
        );
        String body = mockMvc.perform(MockMvcRequestBuilders.post(BASE_ENDPOINT)
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").isNotEmpty())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Product created = objectMapper.readValue(body, Product.class);

        // delete
        MockHttpServletRequestBuilder delReq = MockMvcRequestBuilders
                .delete(BASE_ENDPOINT + "/" + created.getId())
                .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(delReq)
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    void whenGetProductCount_thenReturnsNumber() throws Exception {
        MockHttpServletRequestBuilder mockRequest = MockMvcRequestBuilders
                .get(BASE_ENDPOINT + "/count")
                .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(mockRequest)
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk());
    }
}