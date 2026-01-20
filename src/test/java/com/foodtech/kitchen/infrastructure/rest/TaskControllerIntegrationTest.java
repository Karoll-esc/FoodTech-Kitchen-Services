package com.foodtech.kitchen.infrastructure.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.foodtech.kitchen.application.ports.out.TaskRepository;
import com.foodtech.kitchen.domain.model.Station;
import com.foodtech.kitchen.domain.model.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class TaskControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TaskRepository taskRepository;

    @BeforeEach
    void setUp() throws Exception {
        // Given - Preparar datos: 3 tareas (2 BAR, 1 HOT_KITCHEN)
        
        // Crear pedido con bebidas para BAR
        Map<String, Object> orderBar1 = Map.of(
            "tableNumber", "A1",
            "products", List.of(
                Map.of("name", "Coca Cola", "type", "DRINK")
            )
        );
        mockMvc.perform(post("/api/orders")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(orderBar1)));

        // Crear otro pedido con bebida para BAR
        Map<String, Object> orderBar2 = Map.of(
            "tableNumber", "A2",
            "products", List.of(
                Map.of("name", "Sprite", "type", "DRINK")
            )
        );
        mockMvc.perform(post("/api/orders")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(orderBar2)));

        // Crear pedido con plato caliente para HOT_KITCHEN
        Map<String, Object> orderHotKitchen = Map.of(
            "tableNumber", "B1",
            "products", List.of(
                Map.of("name", "Pizza", "type", "HOT_DISH")
            )
        );
        mockMvc.perform(post("/api/orders")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(orderHotKitchen)));
    }

    @Test
    @DisplayName("Scenario 1: Should return only tasks for specified station")
    void shouldReturnOnlyTasksForSpecifiedStation() throws Exception {
        // When - el encargado de barra consulta sus tareas
        // Then - el sistema muestra únicamente tareas de barra (verifica filtrado)
        mockMvc.perform(get("/api/tasks/station/BAR"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$[0]").exists())  // Al menos una tarea
            .andExpect(jsonPath("$[0].station").value("BAR"))
            .andExpect(jsonPath("$[0].tableNumber").exists())
            .andExpect(jsonPath("$[0].products").isArray())
            .andExpect(jsonPath("$[0].createdAt").exists());
    }

    @Test
    @DisplayName("Scenario 2: Should return empty list when no tasks for station")
    void shouldReturnEmptyListWhenNoTasksForStation() throws Exception {
        // Given - Todas las tareas son de HOT_KITCHEN, ninguna para COLD_KITCHEN
        // When - el encargado de cocina fría consulta sus tareas
        // Then - el sistema muestra que no hay tareas pendientes
        mockMvc.perform(get("/api/tasks/station/COLD_KITCHEN"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    @DisplayName("Scenario 3: Should include complete task information with createdAt timestamp")
    void shouldIncludeCompleteTaskInformationWithTimestamp() throws Exception {
        // Given - tareas ya creadas en setUp
        // When - el encargado consulta las tareas
        // Then - el sistema muestra información completa incluyendo createdAt
        mockMvc.perform(get("/api/tasks/station/BAR"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].tableNumber").exists())
            .andExpect(jsonPath("$[0].station").value("BAR"))
            .andExpect(jsonPath("$[0].products").isArray())
            .andExpect(jsonPath("$[0].products[0].name").exists())
            .andExpect(jsonPath("$[0].products[0].type").exists())
            .andExpect(jsonPath("$[0].createdAt").exists());
    }

    @Test
    @DisplayName("Scenario 4: Should return 400 when invalid station")
    void shouldReturn400WhenInvalidStation() throws Exception {
        // Given - el sistema solo reconoce BAR, HOT_KITCHEN, COLD_KITCHEN
        // When - se consultan tareas para una estación no reconocida
        // Then - el sistema informa que la estación no existe
        mockMvc.perform(get("/api/tasks/station/INVALID_STATION"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").exists())
            .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("HU-003 Scenario 1: Should start task preparation and update status")
    @org.springframework.transaction.annotation.Transactional
    void shouldStartTaskPreparation() throws Exception {
        // Given - existe una tarea pendiente con ID
        List<Task> allTasks = taskRepository.findAll();
        Long taskId = allTasks.get(0).getId();

        // When - el cocinero inicia la preparación de la tarea
        mockMvc.perform(patch("/api/tasks/" + taskId + "/start"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(taskId))
            .andExpect(jsonPath("$.status").value("IN_PREPARATION"))
            .andExpect(jsonPath("$.startedAt").exists());
    }

    @Test
    @DisplayName("HU-003 Scenario 3: Should return only completed tasks when filtering by status")
    @org.springframework.transaction.annotation.Transactional
    void shouldReturnOnlyCompletedTasksForStation() throws Exception {
        // Given - la estación de barra tiene tareas en diferentes estados
        List<Task> barTasks = taskRepository.findByStation(Station.BAR);
        
        // Start tasks (without completing them through the API)
        Task task1 = barTasks.get(0);
        Task task2 = barTasks.get(1);
        
        // Start and manually complete task1
        task1.start();
        task1.complete();
        taskRepository.save(task1);
        
        // Start and manually complete task2
        task2.start();
        task2.complete();
        taskRepository.save(task2);

        // When - se consulta el historial de tareas completadas de barra
        mockMvc.perform(get("/api/tasks/station/BAR?status=COMPLETED"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(2)))
            .andExpect(jsonPath("$[0].status").value("COMPLETED"))
            .andExpect(jsonPath("$[0].startedAt").exists())
            .andExpect(jsonPath("$[0].completedAt").exists())
            .andExpect(jsonPath("$[1].status").value("COMPLETED"))
            .andExpect(jsonPath("$[1].startedAt").exists())
            .andExpect(jsonPath("$[1].completedAt").exists());
    }

    @Test
    @DisplayName("HU-003 Scenario 5: Should return order status based on task states")
    @org.springframework.transaction.annotation.Transactional
    void shouldReturnOrderStatusBasedOnTaskStates() throws Exception {
        // Given - crear un pedido con múltiples productos que generen 3 tareas (diferentes estaciones)
        String orderRequest = objectMapper.writeValueAsString(Map.of(
            "products", List.of(
                Map.of("name", "Coca Cola", "type", "DRINK"),
                Map.of("name", "Sprite", "type", "DRINK"),  // Same station as Coca Cola
                Map.of("name", "Pizza", "type", "HOT_DISH"),
                Map.of("name", "Ensalada", "type", "COLD_DISH")
            ),
            "tableNumber", "A1"
        ));

        mockMvc.perform(post("/api/orders")
            .contentType(MediaType.APPLICATION_JSON)
            .content(orderRequest))
            .andExpect(status().isCreated());

        // Get the order ID from the created tasks
        List<Task> allTasks = taskRepository.findAll();
        Long orderId = allTasks.get(allTasks.size() - 1).getOrderId();
        List<Task> orderTasks = taskRepository.findByOrderId(orderId);
        
        // Should have created 3 tasks (BAR, HOT_KITCHEN, COLD_KITCHEN)
        assertEquals(3, orderTasks.size());
        
        // Initially all tasks are PENDING
        mockMvc.perform(get("/api/orders/" + orderId + "/status"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.orderId").value(orderId.toString()))
            .andExpect(jsonPath("$.status").value("PENDING"));

        // Start one task - order should be IN_PREPARATION
        mockMvc.perform(patch("/api/tasks/" + orderTasks.get(0).getId() + "/start"))
            .andExpect(status().isOk());

        mockMvc.perform(get("/api/orders/" + orderId + "/status"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("IN_PREPARATION"));

        // Complete first task manually (refresh from DB first), start and complete second
        Task task0 = taskRepository.findById(orderTasks.get(0).getId()).get();
        task0.complete();
        taskRepository.save(task0);
        
        Task task1 = taskRepository.findById(orderTasks.get(1).getId()).get();
        task1.start();
        task1.complete();
        taskRepository.save(task1);

        // With 2 completed and 1 pending, status is still IN_PREPARATION because at least one task was started
        mockMvc.perform(get("/api/orders/" + orderId + "/status"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("IN_PREPARATION"));

        // Start last task - order should be IN_PREPARATION
        mockMvc.perform(patch("/api/tasks/" + orderTasks.get(2).getId() + "/start"))
            .andExpect(status().isOk());

        mockMvc.perform(get("/api/orders/" + orderId + "/status"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("IN_PREPARATION"));

        // Complete last task manually - order should be COMPLETED
        Task task2 = taskRepository.findById(orderTasks.get(2).getId()).get();
        task2.complete();
        taskRepository.save(task2);

        mockMvc.perform(get("/api/orders/" + orderId + "/status"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("COMPLETED"));
    }
}
