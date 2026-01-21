package com.foodtech.kitchen.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios para la entidad Table del dominio.
 * 
 * <p>Valida las reglas de negocio definidas en HU-005:</p>
 * <ul>
 *   <li>Mesa se crea con estado AVAILABLE</li>
 *   <li>Mesa se crea sin pedido asociado (currentOrderId null)</li>
 *   <li>Validación de tableNumber (no null, no vacío)</li>
 *   <li>Validación de capacity (> 0)</li>
 *   <li>Timestamps se establecen automáticamente</li>
 * </ul>
 */
class TableTest {
    
    @Test
    @DisplayName("Debe crear mesa con estado AVAILABLE por defecto")
    void shouldCreateTableWithAvailableStatus() {
        // When
        Table table = new Table("A1", 4);
        
        // Then
        assertEquals(TableStatus.AVAILABLE, table.getStatus());
    }
    
    @Test
    @DisplayName("Debe crear mesa con currentOrderId null")
    void shouldCreateTableWithNullCurrentOrderId() {
        // When
        Table table = new Table("A1", 4);
        
        // Then
        assertNull(table.getCurrentOrderId());
    }
    
    @Test
    @DisplayName("Debe establecer createdAt automáticamente")
    void shouldSetCreatedAtAutomatically() {
        // Given
        LocalDateTime before = LocalDateTime.now().minusSeconds(1);
        
        // When
        Table table = new Table("A1", 4);
        
        // Then
        LocalDateTime after = LocalDateTime.now().plusSeconds(1);
        assertNotNull(table.getCreatedAt());
        assertTrue(table.getCreatedAt().isAfter(before));
        assertTrue(table.getCreatedAt().isBefore(after));
    }
    
    @Test
    @DisplayName("Debe establecer updatedAt automáticamente")
    void shouldSetUpdatedAtAutomatically() {
        // Given
        LocalDateTime before = LocalDateTime.now().minusSeconds(1);
        
        // When
        Table table = new Table("A1", 4);
        
        // Then
        LocalDateTime after = LocalDateTime.now().plusSeconds(1);
        assertNotNull(table.getUpdatedAt());
        assertTrue(table.getUpdatedAt().isAfter(before));
        assertTrue(table.getUpdatedAt().isBefore(after));
    }
    
    @Test
    @DisplayName("Debe lanzar excepción cuando tableNumber es null")
    void shouldThrowExceptionWhenTableNumberIsNull() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new Table(null, 4)
        );
        
        assertEquals("Table number cannot be null or empty", exception.getMessage());
    }
    
    @Test
    @DisplayName("Debe lanzar excepción cuando tableNumber está vacío")
    void shouldThrowExceptionWhenTableNumberIsEmpty() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new Table("", 4)
        );
        
        assertEquals("Table number cannot be null or empty", exception.getMessage());
    }
    
    @Test
    @DisplayName("Debe lanzar excepción cuando tableNumber solo contiene espacios")
    void shouldThrowExceptionWhenTableNumberIsBlank() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new Table("   ", 4)
        );
        
        assertEquals("Table number cannot be null or empty", exception.getMessage());
    }
    
    @Test
    @DisplayName("Debe lanzar excepción cuando capacity es cero")
    void shouldThrowExceptionWhenCapacityIsZero() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new Table("A1", 0)
        );
        
        assertEquals("Table capacity must be greater than zero", exception.getMessage());
    }
    
    @Test
    @DisplayName("Debe lanzar excepción cuando capacity es negativo")
    void shouldThrowExceptionWhenCapacityIsNegative() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new Table("A1", -5)
        );
        
        assertEquals("Table capacity must be greater than zero", exception.getMessage());
    }
    
    @Test
    @DisplayName("Debe crear mesa válida con tableNumber y capacity correctos")
    void shouldCreateValidTableWithCorrectParameters() {
        // When
        Table table = new Table("B3", 6);
        
        // Then
        assertEquals("B3", table.getTableNumber());
        assertEquals(6, table.getCapacity());
        assertEquals(TableStatus.AVAILABLE, table.getStatus());
        assertNull(table.getCurrentOrderId());
        assertNotNull(table.getCreatedAt());
        assertNotNull(table.getUpdatedAt());
    }
    
    @Test
    @DisplayName("Debe cambiar estado cuando la transición es válida")
    void shouldChangeStatusWhenTransitionIsValid() {
        // Given
        Table table = new Table("A1", 4);
        
        // When
        table.changeStatus(TableStatus.OCCUPIED);
        
        // Then
        assertEquals(TableStatus.OCCUPIED, table.getStatus());
    }
    
    @Test
    @DisplayName("Debe actualizar updatedAt al cambiar estado")
    void shouldUpdateUpdatedAtWhenChangingStatus() {
        // Given
        Table table = new Table("A1", 4);
        LocalDateTime originalUpdatedAt = table.getUpdatedAt();
        
        // Wait a bit to ensure time difference
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // When
        table.changeStatus(TableStatus.OCCUPIED);
        
        // Then
        assertTrue(table.getUpdatedAt().isAfter(originalUpdatedAt));
    }
    
    @Test
    @DisplayName("Debe lanzar excepción cuando la transición de estado no es válida")
    void shouldThrowExceptionWhenTransitionIsInvalid() {
        // Given
        Table table = new Table("A1", 4);
        
        // When & Then
        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> table.changeStatus(TableStatus.SERVED)
        );
        
        assertTrue(exception.getMessage().contains("Cannot transition from AVAILABLE to SERVED"));
    }
    
    @Test
    @DisplayName("Debe asignar pedido a mesa AVAILABLE y cambiar estado a OCCUPIED")
    void shouldAssignOrderToAvailableTableAndChangeToOccupied() {
        // Given
        Table table = new Table("A1", 4);
        
        // When
        table.assignOrder(123L);
        
        // Then
        assertEquals(123L, table.getCurrentOrderId());
        assertEquals(TableStatus.OCCUPIED, table.getStatus());
    }
    
    @Test
    @DisplayName("Debe lanzar excepción al asignar pedido a mesa no AVAILABLE")
    void shouldThrowExceptionWhenAssigningOrderToNonAvailableTable() {
        // Given
        Table table = new Table("A1", 4);
        table.changeStatus(TableStatus.OCCUPIED);
        
        // When & Then
        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> table.assignOrder(123L)
        );
        
        assertTrue(exception.getMessage().contains("Cannot assign order to non-available table"));
        assertTrue(exception.getMessage().contains("Current status: OCCUPIED"));
    }
    
    @Test
    @DisplayName("Debe lanzar excepción al asignar pedido null")
    void shouldThrowExceptionWhenAssigningNullOrderId() {
        // Given
        Table table = new Table("A1", 4);
        
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> table.assignOrder(null)
        );
        
        assertEquals("Order ID cannot be null", exception.getMessage());
    }
    
    @Test
    @DisplayName("Debe limpiar pedido asociado")
    void shouldClearAssociatedOrder() {
        // Given
        Table table = new Table("A1", 4);
        table.assignOrder(123L);
        
        // When
        table.clearOrder();
        
        // Then
        assertNull(table.getCurrentOrderId());
    }
    
    @Test
    @DisplayName("Debe actualizar updatedAt al limpiar pedido")
    void shouldUpdateUpdatedAtWhenClearingOrder() {
        // Given
        Table table = new Table("A1", 4);
        table.assignOrder(123L);
        LocalDateTime originalUpdatedAt = table.getUpdatedAt();
        
        // Wait a bit to ensure time difference
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // When
        table.clearOrder();
        
        // Then
        assertTrue(table.getUpdatedAt().isAfter(originalUpdatedAt));
    }
    
    @Test
    @DisplayName("Debe reconstruir mesa desde base de datos con todos los campos")
    void shouldReconstructTableFromDatabase() {
        // Given
        LocalDateTime createdAt = LocalDateTime.now().minusDays(1);
        LocalDateTime updatedAt = LocalDateTime.now();
        
        // When
        Table table = new Table(
            1L,
            "A1",
            4,
            TableStatus.OCCUPIED,
            100L,
            createdAt,
            updatedAt
        );
        
        // Then
        assertEquals(1L, table.getId());
        assertEquals("A1", table.getTableNumber());
        assertEquals(4, table.getCapacity());
        assertEquals(TableStatus.OCCUPIED, table.getStatus());
        assertEquals(100L, table.getCurrentOrderId());
        assertEquals(createdAt, table.getCreatedAt());
        assertEquals(updatedAt, table.getUpdatedAt());
    }
    
    @Test
    @DisplayName("Debe usar AVAILABLE como estado por defecto al reconstruir si status es null")
    void shouldUseAvailableAsDefaultStatusWhenReconstructingWithNullStatus() {
        // When
        Table table = new Table(
            1L,
            "A1",
            4,
            null,
            null,
            LocalDateTime.now(),
            LocalDateTime.now()
        );
        
        // Then
        assertEquals(TableStatus.AVAILABLE, table.getStatus());
    }
}
