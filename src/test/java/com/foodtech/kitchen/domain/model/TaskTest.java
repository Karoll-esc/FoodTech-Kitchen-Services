package com.foodtech.kitchen.domain.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TaskTest {

    @Test
    void shouldTransitionFromPendingToInPreparation() {
        // Given
        Product product = new Product("Cerveza", ProductType.BEVERAGE);
        Task task = new Task(
                1L,
                Station.BEVERAGE,
                "T-200",
                "Cliente",
                List.of(product),
                LocalDateTime.now()
        );

        // When
        task.start();

        // Then
        assertEquals(TaskStatus.IN_PREPARATION, task.getStatus());
        assertNotNull(task.getStartedAt());
    }

    @Test
    void shouldTransitionFromInPreparationToCompleted() {
        // Given
        Product product = new Product("Cerveza", ProductType.BEVERAGE);
        Task task = new Task(
                1L,
                Station.BEVERAGE,
                "T-201",
                "Cliente",
                List.of(product),
                LocalDateTime.now()
        );
        task.start();

        // When
        task.complete();

        // Then
        assertEquals(TaskStatus.COMPLETED, task.getStatus());
        assertNotNull(task.getCompletedAt());
    }

    @Test
    void shouldNotCompleteTaskWhenNotInPreparation() {
        // Given
        Product product = new Product("Cerveza", ProductType.BEVERAGE);
        Task task = new Task(
                1L,
                Station.BEVERAGE,
                "T-202",
                "Cliente",
                List.of(product),
                LocalDateTime.now()
        );
        // Task is still PENDING, not started

        // When & Then
        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> task.complete()
        );
        assertEquals("Task must be in IN_PREPARATION status to complete", exception.getMessage());
        assertEquals(TaskStatus.PENDING, task.getStatus());
    }
}
