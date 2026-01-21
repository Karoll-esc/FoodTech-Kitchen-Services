package com.foodtech.kitchen.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

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
}
