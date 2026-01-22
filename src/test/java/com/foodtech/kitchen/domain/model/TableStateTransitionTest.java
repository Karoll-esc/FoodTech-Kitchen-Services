package com.foodtech.kitchen.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios para TableStateTransition Value Object.
 * Valida la creación de transiciones de estado y sus reglas de negocio.
 */
@DisplayName("TableStateTransition - Value Object para transiciones de estado")
class TableStateTransitionTest {
    
    @Test
    @DisplayName("Debe crear transición válida de AVAILABLE a OCCUPIED")
    void shouldCreateValidTransition() {
        // Given
        TableStatus fromStatus = TableStatus.AVAILABLE;
        TableStatus toStatus = TableStatus.OCCUPIED;
        
        // When
        TableStateTransition transition = new TableStateTransition(fromStatus, toStatus);
        
        // Then
        assertNotNull(transition, "La transición no debe ser null");
        assertEquals(fromStatus, transition.getFromStatus(), "El estado origen debe ser AVAILABLE");
        assertEquals(toStatus, transition.getToStatus(), "El estado destino debe ser OCCUPIED");
        assertNotNull(transition.getTransitionTime(), "Debe registrar el tiempo de transición");
    }
    
    @Test
    @DisplayName("Debe rechazar transición inválida de AVAILABLE a SERVED")
    void shouldRejectInvalidTransition() {
        // Given
        TableStatus fromStatus = TableStatus.AVAILABLE;
        TableStatus toStatus = TableStatus.SERVED;
        
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new TableStateTransition(fromStatus, toStatus),
            "Debe lanzar excepción para transición inválida"
        );
        
        assertTrue(exception.getMessage().contains("Invalid transition from AVAILABLE to SERVED"),
            "El mensaje debe indicar la transición inválida");
    }
    
    @Test
    @DisplayName("Debe rechazar transición con estado origen null")
    void shouldRejectNullFromState() {
        // Given
        TableStatus fromStatus = null;
        TableStatus toStatus = TableStatus.OCCUPIED;
        
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new TableStateTransition(fromStatus, toStatus),
            "Debe lanzar excepción cuando fromStatus es null"
        );
        
        assertTrue(exception.getMessage().contains("States cannot be null"),
            "El mensaje debe indicar que los estados no pueden ser null");
    }
    
    @Test
    @DisplayName("Debe rechazar transición con estado destino null")
    void shouldRejectNullToState() {
        // Given
        TableStatus fromStatus = TableStatus.AVAILABLE;
        TableStatus toStatus = null;
        
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new TableStateTransition(fromStatus, toStatus),
            "Debe lanzar excepción cuando toStatus es null"
        );
        
        assertTrue(exception.getMessage().contains("States cannot be null"),
            "El mensaje debe indicar que los estados no pueden ser null");
    }
    
    @Test
    @DisplayName("Debe registrar timestamp de transición")
    void shouldRecordTransitionTime() {
        // Given
        TableStatus fromStatus = TableStatus.OCCUPIED;
        TableStatus toStatus = TableStatus.SERVED;
        LocalDateTime before = LocalDateTime.now();
        
        // When
        TableStateTransition transition = new TableStateTransition(fromStatus, toStatus);
        LocalDateTime after = LocalDateTime.now();
        
        // Then
        assertNotNull(transition.getTransitionTime(), "Debe tener timestamp");
        assertTrue(
            !transition.getTransitionTime().isBefore(before) && 
            !transition.getTransitionTime().isAfter(after),
            "El timestamp debe estar entre el antes y después de la creación"
        );
    }
    
    @Test
    @DisplayName("Debe crear transición válida para cada paso del ciclo de vida")
    void shouldCreateValidTransitionForFullLifecycle() {
        // Given & When & Then
        // AVAILABLE → OCCUPIED
        assertDoesNotThrow(() -> new TableStateTransition(TableStatus.AVAILABLE, TableStatus.OCCUPIED));
        
        // OCCUPIED → SERVED
        assertDoesNotThrow(() -> new TableStateTransition(TableStatus.OCCUPIED, TableStatus.SERVED));
        
        // SERVED → CLEANING
        assertDoesNotThrow(() -> new TableStateTransition(TableStatus.SERVED, TableStatus.CLEANING));
        
        // CLEANING → AVAILABLE
        assertDoesNotThrow(() -> new TableStateTransition(TableStatus.CLEANING, TableStatus.AVAILABLE));
    }
}
