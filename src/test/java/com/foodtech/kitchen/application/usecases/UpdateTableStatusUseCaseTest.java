package com.foodtech.kitchen.application.usecases;

import com.foodtech.kitchen.application.exception.InvalidTableTransitionException;
import com.foodtech.kitchen.application.exception.TableNotFoundException;
import com.foodtech.kitchen.application.exception.TableWithoutActiveOrderException;
import com.foodtech.kitchen.application.ports.out.TableRepository;
import com.foodtech.kitchen.domain.model.Table;
import com.foodtech.kitchen.domain.model.TableStatus;
import com.foodtech.kitchen.domain.services.TableLifecycleValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para UpdateTableStatusUseCase.
 * Valida las reglas de negocio para actualización manual de estado de mesas.
 */
@DisplayName("UpdateTableStatusUseCase - Actualización manual de estado de mesas")
class UpdateTableStatusUseCaseTest {
    
    @Mock
    private TableRepository tableRepository;
    
    @Mock
    private TableLifecycleValidator lifecycleValidator;
    
    private UpdateTableStatusUseCase useCase;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        useCase = new UpdateTableStatusUseCase(tableRepository, lifecycleValidator);
    }
    
    @Test
    @DisplayName("Debe actualizar estado de AVAILABLE a OCCUPIED cuando la mesa tiene pedido activo")
    void shouldUpdateStatusFromAvailableToOccupied() {
        // Given
        Long tableId = 1L;
        Table table = createTableWithOrder("A1", 4, TableStatus.AVAILABLE, 123L);
        table.setId(tableId);
        
        when(tableRepository.findById(tableId)).thenReturn(Optional.of(table));
        doNothing().when(lifecycleValidator).validateTransition(TableStatus.AVAILABLE, TableStatus.OCCUPIED);
        doNothing().when(lifecycleValidator).validateCanBeOccupied(table);
        when(tableRepository.update(any(Table.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // When
        Table result = useCase.execute(tableId, TableStatus.OCCUPIED);
        
        // Then
        assertNotNull(result, "El resultado no debe ser null");
        assertEquals(TableStatus.OCCUPIED, result.getStatus(), "El estado debe ser OCCUPIED");
        assertEquals(123L, result.getCurrentOrderId(), "Debe mantener el pedido activo");
        verify(tableRepository).findById(tableId);
        verify(lifecycleValidator).validateTransition(TableStatus.AVAILABLE, TableStatus.OCCUPIED);
        verify(lifecycleValidator).validateCanBeOccupied(table);
        verify(tableRepository).update(table);
    }
    
    @Test
    @DisplayName("Debe actualizar estado de OCCUPIED a SERVED")
    void shouldUpdateStatusFromOccupiedToServed() {
        // Given
        Long tableId = 2L;
        Table table = createTableWithOrder("B2", 2, TableStatus.OCCUPIED, 456L);
        table.setId(tableId);
        
        when(tableRepository.findById(tableId)).thenReturn(Optional.of(table));
        doNothing().when(lifecycleValidator).validateTransition(TableStatus.OCCUPIED, TableStatus.SERVED);
        when(tableRepository.update(any(Table.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // When
        Table result = useCase.execute(tableId, TableStatus.SERVED);
        
        // Then
        assertEquals(TableStatus.SERVED, result.getStatus());
        assertEquals(456L, result.getCurrentOrderId());
        verify(lifecycleValidator).validateTransition(TableStatus.OCCUPIED, TableStatus.SERVED);
        verify(lifecycleValidator, never()).validateCanBeOccupied(any());
    }
    
    @Test
    @DisplayName("Debe actualizar estado de SERVED a CLEANING")
    void shouldUpdateStatusFromServedToCleaning() {
        // Given
        Long tableId = 3L;
        Table table = createTableWithOrder("C3", 6, TableStatus.SERVED, 789L);
        table.setId(tableId);
        
        when(tableRepository.findById(tableId)).thenReturn(Optional.of(table));
        doNothing().when(lifecycleValidator).validateTransition(TableStatus.SERVED, TableStatus.CLEANING);
        when(tableRepository.update(any(Table.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // When
        Table result = useCase.execute(tableId, TableStatus.CLEANING);
        
        // Then
        assertEquals(TableStatus.CLEANING, result.getStatus());
        verify(lifecycleValidator).validateTransition(TableStatus.SERVED, TableStatus.CLEANING);
    }
    
    @Test
    @DisplayName("Debe actualizar estado de CLEANING a AVAILABLE y desvincular pedido")
    void shouldUpdateStatusFromCleaningToAvailable() {
        // Given
        Long tableId = 4L;
        Table table = createTableWithOrder("D4", 4, TableStatus.CLEANING, 999L);
        table.setId(tableId);
        
        when(tableRepository.findById(tableId)).thenReturn(Optional.of(table));
        doNothing().when(lifecycleValidator).validateTransition(TableStatus.CLEANING, TableStatus.AVAILABLE);
        when(tableRepository.update(any(Table.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // When
        Table result = useCase.execute(tableId, TableStatus.AVAILABLE);
        
        // Then
        assertEquals(TableStatus.AVAILABLE, result.getStatus());
        assertNull(result.getCurrentOrderId(), "El pedido debe ser desvinculado al regresar a AVAILABLE");
        verify(lifecycleValidator).validateTransition(TableStatus.CLEANING, TableStatus.AVAILABLE);
    }
    
    @Test
    @DisplayName("Debe limpiar pedido cuando se transiciona a AVAILABLE")
    void shouldClearOrderWhenTransitioningToAvailable() {
        // Given
        Long tableId = 5L;
        Table table = createTableWithOrder("E5", 2, TableStatus.CLEANING, 111L);
        table.setId(tableId);
        
        when(tableRepository.findById(tableId)).thenReturn(Optional.of(table));
        when(tableRepository.update(any(Table.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // When
        Table result = useCase.execute(tableId, TableStatus.AVAILABLE);
        
        // Then
        assertNull(result.getCurrentOrderId(), "El currentOrderId debe ser null");
    }
    
    @Test
    @DisplayName("Debe lanzar excepción cuando la mesa no existe")
    void shouldThrowExceptionWhenTableNotFound() {
        // Given
        Long tableId = 999L;
        
        when(tableRepository.findById(tableId)).thenReturn(Optional.empty());
        
        // When & Then
        TableNotFoundException exception = assertThrows(
            TableNotFoundException.class,
            () -> useCase.execute(tableId, TableStatus.OCCUPIED),
            "Debe lanzar TableNotFoundException cuando la mesa no existe"
        );
        
        assertTrue(exception.getMessage().contains("999"));
        verify(tableRepository).findById(tableId);
        verify(tableRepository, never()).update(any());
    }
    
    @Test
    @DisplayName("Debe lanzar excepción para transición inválida")
    void shouldThrowExceptionForInvalidTransition() {
        // Given
        Long tableId = 6L;
        Table table = createTable("F6", 4, TableStatus.AVAILABLE);
        table.setId(tableId);
        
        when(tableRepository.findById(tableId)).thenReturn(Optional.of(table));
        doThrow(new IllegalArgumentException("Cannot transition from AVAILABLE to SERVED"))
            .when(lifecycleValidator).validateTransition(TableStatus.AVAILABLE, TableStatus.SERVED);
        
        // When & Then
        InvalidTableTransitionException exception = assertThrows(
            InvalidTableTransitionException.class,
            () -> useCase.execute(tableId, TableStatus.SERVED),
            "Debe lanzar InvalidTableTransitionException para transición inválida"
        );
        
        assertEquals(TableStatus.AVAILABLE, exception.getCurrentStatus());
        assertEquals(TableStatus.SERVED, exception.getTargetStatus());
        verify(tableRepository, never()).update(any());
    }
    
    @Test
    @DisplayName("Debe lanzar excepción cuando se intenta marcar OCCUPIED sin pedido activo")
    void shouldThrowExceptionWhenMarkingOccupiedWithoutOrder() {
        // Given
        Long tableId = 7L;
        Table table = createTable("G7", 4, TableStatus.AVAILABLE);
        table.setId(tableId);
        
        when(tableRepository.findById(tableId)).thenReturn(Optional.of(table));
        doNothing().when(lifecycleValidator).validateTransition(TableStatus.AVAILABLE, TableStatus.OCCUPIED);
        doThrow(new IllegalArgumentException("Table G7 cannot be marked as OCCUPIED without an active order"))
            .when(lifecycleValidator).validateCanBeOccupied(table);
        
        // When & Then
        TableWithoutActiveOrderException exception = assertThrows(
            TableWithoutActiveOrderException.class,
            () -> useCase.execute(tableId, TableStatus.OCCUPIED),
            "Debe lanzar TableWithoutActiveOrderException cuando no hay pedido activo"
        );
        
        assertTrue(exception.getMessage().contains("G7"));
        assertTrue(exception.getMessage().contains("OCCUPIED"));
        assertTrue(exception.getMessage().contains("active order"));
        verify(tableRepository, never()).update(any());
    }
    
    @Test
    @DisplayName("Debe actualizar el timestamp lastStateChangeAt")
    void shouldUpdateLastStateChangeTimestamp() {
        // Given
        Long tableId = 8L;
        Table table = createTableWithOrder("H8", 6, TableStatus.AVAILABLE, 222L);
        table.setId(tableId);
        LocalDateTime originalTimestamp = table.getLastStateChangeAt();
        
        when(tableRepository.findById(tableId)).thenReturn(Optional.of(table));
        when(tableRepository.update(any(Table.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // Wait a bit to ensure time difference
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // When
        Table result = useCase.execute(tableId, TableStatus.OCCUPIED);
        
        // Then
        assertNotNull(result.getLastStateChangeAt());
        assertTrue(result.getLastStateChangeAt().isAfter(originalTimestamp) || 
                   result.getLastStateChangeAt().isEqual(originalTimestamp),
            "El timestamp debe actualizarse con el cambio de estado");
    }
    
    // Helper methods
    
    private Table createTable(String tableNumber, int capacity, TableStatus status) {
        return new Table(
            null,
            tableNumber,
            capacity,
            status,
            null,
            LocalDateTime.now(),
            LocalDateTime.now()
        );
    }
    
    private Table createTableWithOrder(String tableNumber, int capacity, TableStatus status, Long orderId) {
        Table table = new Table(tableNumber, capacity);
        if (orderId != null && status == TableStatus.AVAILABLE) {
            table.assignOrder(orderId);
            // Reset to AVAILABLE for test setup
            return new Table(
                null,
                tableNumber,
                capacity,
                TableStatus.AVAILABLE,
                orderId,
                LocalDateTime.now(),
                LocalDateTime.now()
            );
        }
        return new Table(
            null,
            tableNumber,
            capacity,
            status,
            orderId,
            LocalDateTime.now(),
            LocalDateTime.now()
        );
    }
}
