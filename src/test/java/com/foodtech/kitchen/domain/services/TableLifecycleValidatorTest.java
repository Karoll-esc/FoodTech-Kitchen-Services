package com.foodtech.kitchen.domain.services;

import com.foodtech.kitchen.domain.model.Table;
import com.foodtech.kitchen.domain.model.TableStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios para TableLifecycleValidator.
 * Valida las reglas de negocio para el ciclo de vida de mesas.
 */
@DisplayName("TableLifecycleValidator - Validación de reglas de negocio del ciclo de vida")
class TableLifecycleValidatorTest {
    
    private TableLifecycleValidator validator;
    
    @BeforeEach
    void setUp() {
        validator = new TableLifecycleValidator();
    }
    
    @Test
    @DisplayName("Debe validar transiciones válidas sin lanzar excepción")
    void shouldValidateValidTransitions() {
        // Given & When & Then
        assertDoesNotThrow(() -> validator.validateTransition(TableStatus.AVAILABLE, TableStatus.OCCUPIED),
            "Debe permitir AVAILABLE → OCCUPIED");
        
        assertDoesNotThrow(() -> validator.validateTransition(TableStatus.OCCUPIED, TableStatus.SERVED),
            "Debe permitir OCCUPIED → SERVED");
        
        assertDoesNotThrow(() -> validator.validateTransition(TableStatus.SERVED, TableStatus.CLEANING),
            "Debe permitir SERVED → CLEANING");
        
        assertDoesNotThrow(() -> validator.validateTransition(TableStatus.CLEANING, TableStatus.AVAILABLE),
            "Debe permitir CLEANING → AVAILABLE");
    }
    
    @Test
    @DisplayName("Debe rechazar transición inválida de AVAILABLE a SERVED")
    void shouldRejectInvalidTransitionFromAvailableToServed() {
        // Given
        TableStatus currentStatus = TableStatus.AVAILABLE;
        TableStatus targetStatus = TableStatus.SERVED;
        
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> validator.validateTransition(currentStatus, targetStatus),
            "Debe lanzar excepción para transición inválida"
        );
        
        assertTrue(exception.getMessage().contains("Cannot transition from AVAILABLE to SERVED"),
            "El mensaje debe indicar la transición inválida");
        assertTrue(exception.getMessage().contains("Valid transitions from AVAILABLE are:"),
            "El mensaje debe mostrar las transiciones válidas");
    }
    
    @Test
    @DisplayName("Debe rechazar transición inválida de OCCUPIED a AVAILABLE")
    void shouldRejectInvalidTransitionFromOccupiedToAvailable() {
        // Given
        TableStatus currentStatus = TableStatus.OCCUPIED;
        TableStatus targetStatus = TableStatus.AVAILABLE;
        
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> validator.validateTransition(currentStatus, targetStatus)
        );
        
        assertTrue(exception.getMessage().contains("Cannot transition from OCCUPIED to AVAILABLE"));
    }
    
    @Test
    @DisplayName("Debe rechazar transición cuando estado actual es null")
    void shouldRejectTransitionWithNullCurrentStatus() {
        // Given
        TableStatus currentStatus = null;
        TableStatus targetStatus = TableStatus.OCCUPIED;
        
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> validator.validateTransition(currentStatus, targetStatus),
            "Debe lanzar excepción cuando currentStatus es null"
        );
        
        assertEquals("Status cannot be null", exception.getMessage());
    }
    
    @Test
    @DisplayName("Debe rechazar transición cuando estado destino es null")
    void shouldRejectTransitionWithNullTargetStatus() {
        // Given
        TableStatus currentStatus = TableStatus.AVAILABLE;
        TableStatus targetStatus = null;
        
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> validator.validateTransition(currentStatus, targetStatus),
            "Debe lanzar excepción cuando targetStatus es null"
        );
        
        assertEquals("Status cannot be null", exception.getMessage());
    }
    
    @Test
    @DisplayName("Debe validar que mesa puede ser ocupada cuando tiene pedido activo")
    void shouldValidateTableCanBeOccupiedWithOrder() {
        // Given
        Table table = new Table("A1", 4);
        table.assignOrder(123L); // Mesa tiene pedido pero está OCCUPIED
        
        // When & Then
        assertDoesNotThrow(() -> validator.validateCanBeOccupied(table),
            "Mesa con pedido activo debe poder ser validada");
    }
    
    @Test
    @DisplayName("Debe rechazar estado OCCUPIED sin pedido activo")
    void shouldRejectOccupiedStateWithoutOrder() {
        // Given
        Table table = new Table("B2", 2);
        // Mesa AVAILABLE sin pedido
        
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> validator.validateCanBeOccupied(table),
            "Debe lanzar excepción cuando mesa no tiene pedido activo"
        );
        
        assertTrue(exception.getMessage().contains("Table B2 cannot be marked as OCCUPIED without an active order"),
            "El mensaje debe indicar que falta el pedido activo");
    }
    
    @Test
    @DisplayName("Debe rechazar marcar como OCCUPIED una mesa sin currentOrderId")
    void shouldRejectMarkingOccupiedWithoutCurrentOrderId() {
        // Given
        Table table = new Table("C3", 6);
        // currentOrderId es null por defecto
        
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> validator.validateCanBeOccupied(table)
        );
        
        assertEquals(
            "Table C3 cannot be marked as OCCUPIED without an active order",
            exception.getMessage()
        );
    }
}
