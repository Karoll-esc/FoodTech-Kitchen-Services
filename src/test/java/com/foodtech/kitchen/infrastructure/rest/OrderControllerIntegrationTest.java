package com.foodtech.kitchen.infrastructure.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class OrderControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Should create order and return 201 with task count")
    void shouldCreateOrderAndReturn201() throws Exception {
        // Given
        Map<String, Object> request = Map.of(
            "tableNumber", "A1",
            "products", List.of(
                Map.of("name", "Coca Cola", "type", "DRINK")
            )
        );

        // When & Then
        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tableNumber").value("A1"))
                .andExpect(jsonPath("$.tasksCreated").value(1))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("Should create order with mixed products")
    void shouldCreateOrderWithMixedProducts() throws Exception {
        // Given
        Map<String, Object> request = Map.of(
            "tableNumber", "B2",
            "products", List.of(
                Map.of("name", "Coca Cola", "type", "DRINK"),
                Map.of("name", "Pizza", "type", "HOT_DISH")
            )
        );

        // When & Then
        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tableNumber").value("B2"))
                .andExpect(jsonPath("$.tasksCreated").value(2));
    }

    @Test
    @DisplayName("Should reject order without products")
    void shouldRejectOrderWithoutProducts() throws Exception {
        // Given
        Map<String, Object> request = Map.of(
            "tableNumber", "C3",
            "products", List.of()
        );

        // When & Then
        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    @DisplayName("Should reject order without table number")
    void shouldRejectOrderWithoutTableNumber() throws Exception {
        // Given
        Map<String, Object> request = Map.of(
            "tableNumber", "",
            "products", List.of(
                Map.of("name", "Coca Cola", "type", "DRINK")
            )
        );

        // When & Then
        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }
}