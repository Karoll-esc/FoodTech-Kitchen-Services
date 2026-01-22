package com.foodtech.kitchen.application.usecases;

import com.foodtech.kitchen.application.ports.out.TaskRepository;
import com.foodtech.kitchen.domain.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GetTasksByStationUseCaseTest {

    private GetTasksByStationUseCase useCase;
    private TaskRepository taskRepository;

    @BeforeEach
    void setUp() {
        taskRepository = mock(TaskRepository.class);
        useCase = new GetTasksByStationUseCase(taskRepository);
    }

    @Test
    @DisplayName("Should return only tasks for specified station")
    void shouldReturnOnlyTasksForSpecifiedStation() {
        // Given - 3 tareas pendientes: 2 ESPRESSO_BAR, 1 PASTRY_STATION
        Product cocaCola = new Product("Coca Cola", ProductType.DRINK);
        Product sprite = new Product("Sprite", ProductType.DRINK);
        
        LocalDateTime now = LocalDateTime.now();
        Task barTask1 = new Task(1L, Station.ESPRESSO_BAR, "A1", List.of(cocaCola), now);
        Task barTask2 = new Task(1L, Station.ESPRESSO_BAR, "A2", List.of(sprite), now);
        
        when(taskRepository.findByStation(Station.ESPRESSO_BAR))
            .thenReturn(List.of(barTask1, barTask2));

        // When - el encargado de barra consulta sus tareas
        List<Task> tasks = useCase.execute(Station.ESPRESSO_BAR);

        // Then - el sistema muestra únicamente las 2 tareas de barra
        assertEquals(2, tasks.size());
        assertTrue(tasks.stream().allMatch(task -> task.getStation() == Station.ESPRESSO_BAR));
        verify(taskRepository, times(1)).findByStation(Station.ESPRESSO_BAR);
    }

    @Test
    @DisplayName("Should return empty list when no tasks for station")
    void shouldReturnEmptyListWhenNoTasksForStation() {
        // Given - no hay tareas asignadas a la estación de barra
        when(taskRepository.findByStation(Station.ESPRESSO_BAR))
            .thenReturn(List.of());

        // When - el encargado de barra consulta sus tareas
        List<Task> tasks = useCase.execute(Station.ESPRESSO_BAR);

        // Then - el sistema muestra que no hay tareas pendientes
        assertNotNull(tasks);
        assertTrue(tasks.isEmpty());
        verify(taskRepository, times(1)).findByStation(Station.ESPRESSO_BAR);
    }
}
