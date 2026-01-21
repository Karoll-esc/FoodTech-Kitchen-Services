package com.foodtech.kitchen.infrastructure.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.foodtech.kitchen.domain.model.Table;
import com.foodtech.kitchen.domain.model.TableStatus;
import com.foodtech.kitchen.application.usecases.CreateTableUseCase;
import com.foodtech.kitchen.application.usecases.GetAllTablesUseCase;
import com.foodtech.kitchen.application.usecases.GetTableByIdUseCase;
import com.foodtech.kitchen.application.exception.TableAlreadyExistsException;
import com.foodtech.kitchen.application.exception.TableNotFoundException;
import com.foodtech.kitchen.infrastructure.rest.dto.CreateTableRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TableController.class)
@DisplayName("TableController - Tests de integración REST")
class TableControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CreateTableUseCase createTableUseCase;

    @MockBean
    private GetAllTablesUseCase getAllTablesUseCase;

    @MockBean
    private GetTableByIdUseCase getTableByIdUseCase;

    @Test
    @WithMockUser(authorities = "ADMIN")
    @DisplayName("Escenario 1: Debe crear mesa exitosamente con rol ADMIN")
    void shouldCreateTableSuccessfullyWithAdminRole() throws Exception {
        // Given
        CreateTableRequest request = new CreateTableRequest("A1", 4);
        LocalDateTime now = LocalDateTime.now();
        Table createdTable = new Table(1L, "A1", 4, TableStatus.AVAILABLE, null, now, now);

        when(createTableUseCase.execute("A1", 4)).thenReturn(createdTable);

        // When & Then
        mockMvc.perform(post("/api/tables")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.tableNumber").value("A1"))
            .andExpect(jsonPath("$.capacity").value(4))
            .andExpect(jsonPath("$.status").value("AVAILABLE"))
            .andExpect(jsonPath("$.currentOrderId").isEmpty())
            .andExpect(jsonPath("$.createdAt").exists())
            .andExpect(jsonPath("$.updatedAt").exists());
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    @DisplayName("Escenario 2: Debe retornar 409 Conflict al crear mesa duplicada")
    void shouldReturn409ConflictWhenCreatingDuplicateTable() throws Exception {
        // Given
        CreateTableRequest request = new CreateTableRequest("B3", 6);

        when(createTableUseCase.execute("B3", 6))
            .thenThrow(new TableAlreadyExistsException("B3"));

        // When & Then
        mockMvc.perform(post("/api/tables")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.message").value("Cannot create table 'B3': number already exists in system"))
            .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    @DisplayName("Escenario 3: Debe retornar 400 Bad Request con capacidad inválida (0)")
    void shouldReturn400BadRequestWithInvalidCapacityZero() throws Exception {
        // Given
        CreateTableRequest request = new CreateTableRequest("C1", 0);

        when(createTableUseCase.execute("C1", 0))
            .thenThrow(new IllegalArgumentException("Table capacity must be greater than zero"));

        // When & Then
        mockMvc.perform(post("/api/tables")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("Table capacity must be greater than zero"))
            .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    @DisplayName("Escenario 3b: Debe retornar 400 Bad Request con capacidad inválida (negativa)")
    void shouldReturn400BadRequestWithNegativeCapacity() throws Exception {
        // Given
        CreateTableRequest request = new CreateTableRequest("C2", -5);

        when(createTableUseCase.execute("C2", -5))
            .thenThrow(new IllegalArgumentException("Table capacity must be greater than zero"));

        // When & Then
        mockMvc.perform(post("/api/tables")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    @DisplayName("Escenario 4: Debe listar todas las mesas con rol ADMIN")
    void shouldGetAllTablesWithAdminRole() throws Exception {
        // Given
        LocalDateTime now = LocalDateTime.now();
        Table table1 = new Table(1L, "A1", 4, TableStatus.AVAILABLE, null, now, now);
        Table table2 = new Table(2L, "A2", 6, TableStatus.OCCUPIED, 123L, now, now);
        Table table3 = new Table(3L, "A3", 2, TableStatus.AVAILABLE, null, now, now);
        Table table4 = new Table(4L, "B1", 8, TableStatus.SERVED, 456L, now, now);
        Table table5 = new Table(5L, "B2", 4, TableStatus.CLEANING, null, now, now);

        when(getAllTablesUseCase.execute()).thenReturn(Arrays.asList(table1, table2, table3, table4, table5));

        // When & Then
        mockMvc.perform(get("/api/tables")
                .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$", hasSize(5)))
            .andExpect(jsonPath("$[0].tableNumber").value("A1"))
            .andExpect(jsonPath("$[0].capacity").value(4))
            .andExpect(jsonPath("$[0].status").value("AVAILABLE"))
            .andExpect(jsonPath("$[0].createdAt").exists())
            .andExpect(jsonPath("$[1].tableNumber").value("A2"))
            .andExpect(jsonPath("$[1].status").value("OCCUPIED"))
            .andExpect(jsonPath("$[1].currentOrderId").value(123))
            .andExpect(jsonPath("$[4].status").value("CLEANING"));
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    @DisplayName("Debe retornar lista vacía cuando no hay mesas")
    void shouldReturnEmptyListWhenNoTables() throws Exception {
        // Given
        when(getAllTablesUseCase.execute()).thenReturn(Collections.emptyList());

        // When & Then
        mockMvc.perform(get("/api/tables")
                .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    @DisplayName("Debe obtener mesa por ID con rol ADMIN")
    void shouldGetTableByIdWithAdminRole() throws Exception {
        // Given
        LocalDateTime now = LocalDateTime.now();
        Table table = new Table(5L, "D4", 6, TableStatus.OCCUPIED, 789L, now, now);

        when(getTableByIdUseCase.execute(5L)).thenReturn(table);

        // When & Then
        mockMvc.perform(get("/api/tables/5")
                .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(5))
            .andExpect(jsonPath("$.tableNumber").value("D4"))
            .andExpect(jsonPath("$.capacity").value(6))
            .andExpect(jsonPath("$.status").value("OCCUPIED"))
            .andExpect(jsonPath("$.currentOrderId").value(789));
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    @DisplayName("Debe retornar 404 Not Found cuando mesa no existe por ID")
    void shouldReturn404NotFoundWhenTableNotFoundById() throws Exception {
        // Given
        when(getTableByIdUseCase.execute(999L))
            .thenThrow(new TableNotFoundException(999L));

        // When & Then
        mockMvc.perform(get("/api/tables/999")
                .with(csrf()))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message").value("Table not found with ID: 999"))
            .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    @DisplayName("Debe validar tableNumber vacío al crear mesa")
    void shouldValidateEmptyTableNumberWhenCreatingTable() throws Exception {
        // Given
        CreateTableRequest request = new CreateTableRequest("", 4);

        when(createTableUseCase.execute("", 4))
            .thenThrow(new IllegalArgumentException("Table number cannot be null or empty"));

        // When & Then
        mockMvc.perform(post("/api/tables")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("Table number cannot be null or empty"));
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    @DisplayName("Debe crear mesa con capacidades variadas")
    void shouldCreateTableWithVariousCapacities() throws Exception {
        // Capacidad 2
        CreateTableRequest request2 = new CreateTableRequest("H1", 2);
        LocalDateTime now = LocalDateTime.now();
        Table table2 = new Table(1L, "H1", 2, TableStatus.AVAILABLE, null, now, now);
        when(createTableUseCase.execute("H1", 2)).thenReturn(table2);

        mockMvc.perform(post("/api/tables")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request2)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.capacity").value(2));

        // Capacidad 8
        CreateTableRequest request8 = new CreateTableRequest("H2", 8);
        Table table8 = new Table(2L, "H2", 8, TableStatus.AVAILABLE, null, now, now);
        when(createTableUseCase.execute("H2", 8)).thenReturn(table8);

        mockMvc.perform(post("/api/tables")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request8)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.capacity").value(8));
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    @DisplayName("Debe crear mesa con números alfanuméricos")
    void shouldCreateTableWithAlphanumericNumbers() throws Exception {
        // Given
        CreateTableRequest request = new CreateTableRequest("VIP-01", 10);
        LocalDateTime now = LocalDateTime.now();
        Table table = new Table(1L, "VIP-01", 10, TableStatus.AVAILABLE, null, now, now);

        when(createTableUseCase.execute("VIP-01", 10)).thenReturn(table);

        // When & Then
        mockMvc.perform(post("/api/tables")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.tableNumber").value("VIP-01"));
    }
}
