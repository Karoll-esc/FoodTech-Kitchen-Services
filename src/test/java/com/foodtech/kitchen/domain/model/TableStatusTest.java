package com.foodtech.kitchen.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios para TableStatus enum.
 * 
 * <p>Valida las transiciones de estado según las reglas de negocio:
 * AVAILABLE → OCCUPIED → SERVED → CLEANING → AVAILABLE</p>
 */
class TableStatusTest {
    
    @Test
    @DisplayName("Debe permitir transición de AVAILABLE a OCCUPIED")
    void shouldAllowTransitionFromAvailableToOccupied() {
        // Given
        TableStatus status = TableStatus.AVAILABLE;
        
        // When
        boolean canTransition = status.canTransitionTo(TableStatus.OCCUPIED);
        
        // Then
        assertTrue(canTransition, "Should allow transition from AVAILABLE to OCCUPIED");
    }
    
    @Test
    @DisplayName("Debe permitir transición de OCCUPIED a SERVED")
    void shouldAllowTransitionFromOccupiedToServed() {
        // Given
        TableStatus status = TableStatus.OCCUPIED;
        
        // When
        boolean canTransition = status.canTransitionTo(TableStatus.SERVED);
        
        // Then
        assertTrue(canTransition, "Should allow transition from OCCUPIED to SERVED");
    }
    
    @Test
    @DisplayName("Debe permitir transición de SERVED a CLEANING")
    void shouldAllowTransitionFromServedToCleaning() {
        // Given
        TableStatus status = TableStatus.SERVED;
        
        // When
        boolean canTransition = status.canTransitionTo(TableStatus.CLEANING);
        
        // Then
        assertTrue(canTransition, "Should allow transition from SERVED to CLEANING");
    }
    
    @Test
    @DisplayName("Debe permitir transición de CLEANING a AVAILABLE")
    void shouldAllowTransitionFromCleaningToAvailable() {
        // Given
        TableStatus status = TableStatus.CLEANING;
        
        // When
        boolean canTransition = status.canTransitionTo(TableStatus.AVAILABLE);
        
        // Then
        assertTrue(canTransition, "Should allow transition from CLEANING to AVAILABLE");
    }
    
    @Test
    @DisplayName("No debe permitir transición de AVAILABLE a SERVED")
    void shouldNotAllowTransitionFromAvailableToServed() {
        // Given
        TableStatus status = TableStatus.AVAILABLE;
        
        // When
        boolean canTransition = status.canTransitionTo(TableStatus.SERVED);
        
        // Then
        assertFalse(canTransition, "Should not allow transition from AVAILABLE to SERVED");
    }
    
    @Test
    @DisplayName("No debe permitir transición de AVAILABLE a CLEANING")
    void shouldNotAllowTransitionFromAvailableToCleaning() {
        // Given
        TableStatus status = TableStatus.AVAILABLE;
        
        // When
        boolean canTransition = status.canTransitionTo(TableStatus.CLEANING);
        
        // Then
        assertFalse(canTransition, "Should not allow transition from AVAILABLE to CLEANING");
    }
    
    @Test
    @DisplayName("No debe permitir transición de OCCUPIED a AVAILABLE")
    void shouldNotAllowTransitionFromOccupiedToAvailable() {
        // Given
        TableStatus status = TableStatus.OCCUPIED;
        
        // When
        boolean canTransition = status.canTransitionTo(TableStatus.AVAILABLE);
        
        // Then
        assertFalse(canTransition, "Should not allow transition from OCCUPIED to AVAILABLE");
    }
    
    @Test
    @DisplayName("No debe permitir transición de OCCUPIED a CLEANING")
    void shouldNotAllowTransitionFromOccupiedToCleaning() {
        // Given
        TableStatus status = TableStatus.OCCUPIED;
        
        // When
        boolean canTransition = status.canTransitionTo(TableStatus.CLEANING);
        
        // Then
        assertFalse(canTransition, "Should not allow transition from OCCUPIED to CLEANING");
    }
    
    @Test
    @DisplayName("No debe permitir transición de SERVED a AVAILABLE")
    void shouldNotAllowTransitionFromServedToAvailable() {
        // Given
        TableStatus status = TableStatus.SERVED;
        
        // When
        boolean canTransition = status.canTransitionTo(TableStatus.AVAILABLE);
        
        // Then
        assertFalse(canTransition, "Should not allow transition from SERVED to AVAILABLE");
    }
    
    @Test
    @DisplayName("No debe permitir transición de SERVED a OCCUPIED")
    void shouldNotAllowTransitionFromServedToOccupied() {
        // Given
        TableStatus status = TableStatus.SERVED;
        
        // When
        boolean canTransition = status.canTransitionTo(TableStatus.OCCUPIED);
        
        // Then
        assertFalse(canTransition, "Should not allow transition from SERVED to OCCUPIED");
    }
    
    @Test
    @DisplayName("No debe permitir transición de CLEANING a OCCUPIED")
    void shouldNotAllowTransitionFromCleaningToOccupied() {
        // Given
        TableStatus status = TableStatus.CLEANING;
        
        // When
        boolean canTransition = status.canTransitionTo(TableStatus.OCCUPIED);
        
        // Then
        assertFalse(canTransition, "Should not allow transition from CLEANING to OCCUPIED");
    }
    
    @Test
    @DisplayName("No debe permitir transición de CLEANING a SERVED")
    void shouldNotAllowTransitionFromCleaningToServed() {
        // Given
        TableStatus status = TableStatus.CLEANING;
        
        // When
        boolean canTransition = status.canTransitionTo(TableStatus.SERVED);
        
        // Then
        assertFalse(canTransition, "Should not allow transition from CLEANING to SERVED");
    }
    
    @Test
    @DisplayName("Debe lanzar excepción cuando newStatus es null")
    void shouldThrowExceptionWhenNewStatusIsNull() {
        // Given
        TableStatus status = TableStatus.AVAILABLE;
        
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> status.canTransitionTo(null)
        );
        
        assertEquals("New status cannot be null", exception.getMessage());
    }
    
    @Test
    @DisplayName("No debe permitir transición al mismo estado")
    void shouldRejectTransitionToSameState() {
        // Given & When & Then
        assertFalse(TableStatus.AVAILABLE.canTransitionTo(TableStatus.AVAILABLE),
            "No se puede transicionar al mismo estado AVAILABLE");
        assertFalse(TableStatus.OCCUPIED.canTransitionTo(TableStatus.OCCUPIED),
            "No se puede transicionar al mismo estado OCCUPIED");
        assertFalse(TableStatus.SERVED.canTransitionTo(TableStatus.SERVED),
            "No se puede transicionar al mismo estado SERVED");
        assertFalse(TableStatus.CLEANING.canTransitionTo(TableStatus.CLEANING),
            "No se puede transicionar al mismo estado CLEANING");
    }
    
    @Test
    @DisplayName("Debe retornar lista de transiciones válidas desde AVAILABLE")
    void shouldReturnValidTransitionsFromAvailable() {
        // Given
        TableStatus status = TableStatus.AVAILABLE;
        
        // When
        List<TableStatus> validTransitions = status.getValidTransitions();
        
        // Then
        assertEquals(1, validTransitions.size(), "AVAILABLE debe tener una sola transición válida");
        assertTrue(validTransitions.contains(TableStatus.OCCUPIED), 
            "AVAILABLE debe poder transicionar a OCCUPIED");
    }
    
    @Test
    @DisplayName("Debe retornar lista de transiciones válidas desde OCCUPIED")
    void shouldReturnValidTransitionsFromOccupied() {
        // Given
        TableStatus status = TableStatus.OCCUPIED;
        
        // When
        List<TableStatus> validTransitions = status.getValidTransitions();
        
        // Then
        assertEquals(1, validTransitions.size(), "OCCUPIED debe tener una sola transición válida");
        assertTrue(validTransitions.contains(TableStatus.SERVED), 
            "OCCUPIED debe poder transicionar a SERVED");
    }
    
    @Test
    @DisplayName("Debe retornar lista de transiciones válidas desde SERVED")
    void shouldReturnValidTransitionsFromServed() {
        // Given
        TableStatus status = TableStatus.SERVED;
        
        // When
        List<TableStatus> validTransitions = status.getValidTransitions();
        
        // Then
        assertEquals(1, validTransitions.size(), "SERVED debe tener una sola transición válida");
        assertTrue(validTransitions.contains(TableStatus.CLEANING), 
            "SERVED debe poder transicionar a CLEANING");
    }
    
    @Test
    @DisplayName("Debe retornar lista de transiciones válidas desde CLEANING")
    void shouldReturnValidTransitionsFromCleaning() {
        // Given
        TableStatus status = TableStatus.CLEANING;
        
        // When
        List<TableStatus> validTransitions = status.getValidTransitions();
        
        // Then
        assertEquals(1, validTransitions.size(), "CLEANING debe tener una sola transición válida");
        assertTrue(validTransitions.contains(TableStatus.AVAILABLE), 
            "CLEANING debe poder transicionar a AVAILABLE");
    }
}
