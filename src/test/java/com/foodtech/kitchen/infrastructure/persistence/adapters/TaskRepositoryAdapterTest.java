package com.foodtech.kitchen.infrastructure.persistence.adapters;

import com.foodtech.kitchen.domain.model.*;
import com.foodtech.kitchen.infrastructure.persistence.jpa.TaskJpaRepository;
import com.foodtech.kitchen.infrastructure.persistence.jpa.entities.TaskEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TaskRepositoryAdapterTest {

    private TaskRepositoryAdapter adapter;
    private TaskJpaRepository jpaRepository;

    @BeforeEach
    void setUp() {
        jpaRepository = mock(TaskJpaRepository.class);
        com.foodtech.kitchen.infrastructure.persistence.mappers.ProductEntityMapper productMapper =
            new com.foodtech.kitchen.infrastructure.persistence.mappers.ProductEntityMapper();
        com.foodtech.kitchen.infrastructure.persistence.mappers.TaskEntityMapper mapper = 
            new com.foodtech.kitchen.infrastructure.persistence.mappers.TaskEntityMapper(productMapper);
        adapter = new TaskRepositoryAdapter(jpaRepository, mapper);
    }

    @Test
    @DisplayName("Should save tasks using JPA repository")
    void shouldSaveTasks() {
        // Given
        Product product = new Product("Coca Cola", ProductType.DRINK);
        Task task = new Task(1L, Station.ESPRESSO_BAR, "A1", List.of(product), LocalDateTime.now());

        // When
        adapter.saveAll(List.of(task));

        // Then
        verify(jpaRepository, times(1)).saveAll(anyList());
    }

    @Test
    @DisplayName("Should find tasks by station")
    void shouldFindTasksByStation() {
        // Given
        com.foodtech.kitchen.infrastructure.persistence.jpa.entities.TaskProductEntity p =
            com.foodtech.kitchen.infrastructure.persistence.jpa.entities.TaskProductEntity.builder()
                .name("Coca Cola").type(ProductType.DRINK).build();

        TaskEntity entity = TaskEntity.builder()
            .id(1L)
            .orderId(1L)
            .station(Station.ESPRESSO_BAR)
            .tableNumber("A1")
            .products(List.of(p))
            .createdAt(LocalDateTime.now())
            .build();
        
        when(jpaRepository.findByStation(Station.ESPRESSO_BAR))
            .thenReturn(List.of(entity));

        // When
        List<Task> tasks = adapter.findByStation(Station.ESPRESSO_BAR);

        // Then
        assertEquals(1, tasks.size());
        assertEquals(Station.ESPRESSO_BAR, tasks.get(0).getStation());
        verify(jpaRepository, times(1)).findByStation(Station.ESPRESSO_BAR);
    }

    @Test
    @DisplayName("Should find all tasks")
    void shouldFindAllTasks() {
        // Given
        com.foodtech.kitchen.infrastructure.persistence.jpa.entities.TaskProductEntity p =
            com.foodtech.kitchen.infrastructure.persistence.jpa.entities.TaskProductEntity.builder()
                .name("Coca Cola").type(ProductType.DRINK).build();

        TaskEntity entity = TaskEntity.builder()
            .id(1L)
            .orderId(1L)
            .station(Station.ESPRESSO_BAR)
            .tableNumber("A1")
            .products(List.of(p))
            .createdAt(LocalDateTime.now())
            .build();
        
        when(jpaRepository.findAll()).thenReturn(List.of(entity));

        // When
        List<Task> tasks = adapter.findAll();

        // Then
        assertEquals(1, tasks.size());
        verify(jpaRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should find tasks by station and status")
    void shouldFindTasksByStationAndStatus() {
        // Given
        com.foodtech.kitchen.infrastructure.persistence.jpa.entities.TaskProductEntity p =
            com.foodtech.kitchen.infrastructure.persistence.jpa.entities.TaskProductEntity.builder()
                .name("Coca Cola").type(ProductType.DRINK).build();

        TaskEntity completedEntity = TaskEntity.builder()
            .id(1L)
            .orderId(1L)
            .station(Station.ESPRESSO_BAR)
            .tableNumber("A1")
            .products(List.of(p))
            .status(TaskStatus.COMPLETED)
            .createdAt(LocalDateTime.now())
            .startedAt(LocalDateTime.now())
            .completedAt(LocalDateTime.now())
            .build();
        
        when(jpaRepository.findByStationAndStatus(Station.ESPRESSO_BAR, TaskStatus.COMPLETED))
            .thenReturn(List.of(completedEntity));

        // When
        List<Task> tasks = adapter.findByStationAndStatus(Station.ESPRESSO_BAR, TaskStatus.COMPLETED);

        // Then
        assertEquals(1, tasks.size());
        assertEquals(Station.ESPRESSO_BAR, tasks.get(0).getStation());
        assertEquals(TaskStatus.COMPLETED, tasks.get(0).getStatus());
        verify(jpaRepository, times(1)).findByStationAndStatus(Station.ESPRESSO_BAR, TaskStatus.COMPLETED);
    }
}