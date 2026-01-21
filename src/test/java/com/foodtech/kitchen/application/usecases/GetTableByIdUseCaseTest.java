package com.foodtech.kitchen.application.usecases;

import com.foodtech.kitchen.application.exception.TableNotFoundException;
import com.foodtech.kitchen.application.ports.out.TableRepository;
import com.foodtech.kitchen.domain.model.Table;
import com.foodtech.kitchen.domain.model.TableStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para GetTableByIdUseCase.
 * 
 * <p>Valida el comportamiento del caso de uso:</p>
 * <ul>
 *   <li>Retorna la mesa si existe con el ID especificado</li>
 *   <li>Lanza TableNotFoundException si no existe</li>
 *   <li>Mensaje de error descriptivo con el ID buscado</li>
 * </ul>
 */
class GetTableByIdUseCaseTest {
    
    private TableRepository tableRepository;
    private GetTableByIdUseCase getTableByIdUseCase;
    
    @BeforeEach
    void setUp() {
        tableRepository = mock(TableRepository.class);
        getTableByIdUseCase = new GetTableByIdUseCase(tableRepository);
    }
    
    @Test
    @DisplayName("Debe retornar mesa cuando existe con el ID especificado")
    void shouldReturnTableWhenExistsWithGivenId() {
        // Given
        Long tableId = 1L;
        Table expectedTable = new Table("A1", 4);
        expectedTable.setId(tableId);
        
        when(tableRepository.findById(tableId)).thenReturn(Optional.of(expectedTable));
        
        // When
        Table result = getTableByIdUseCase.execute(tableId);
        
        // Then
        assertNotNull(result);
        assertEquals(tableId, result.getId());
        assertEquals("A1", result.getTableNumber());
        assertEquals(4, result.getCapacity());
        assertEquals(TableStatus.AVAILABLE, result.getStatus());
        
        verify(tableRepository).findById(tableId);
    }
    
    @Test
    @DisplayName("Debe lanzar excepción cuando mesa no existe")
    void shouldThrowExceptionWhenTableDoesNotExist() {
        // Given
        Long nonExistentId = 999L;
        
        when(tableRepository.findById(nonExistentId)).thenReturn(Optional.empty());
        
        // When & Then
        TableNotFoundException exception = assertThrows(
            TableNotFoundException.class,
            () -> getTableByIdUseCase.execute(nonExistentId)
        );
        
        assertTrue(exception.getMessage().contains("999"));
        assertTrue(exception.getMessage().contains("not found"));
        
        verify(tableRepository).findById(nonExistentId);
    }
    
    @Test
    @DisplayName("Debe retornar mesa con todos sus atributos correctamente")
    void shouldReturnTableWithAllAttributesCorrectly() {
        // Given
        Long tableId = 5L;
        Table expectedTable = new Table("VIP-1", 8);
        expectedTable.setId(tableId);
        expectedTable.assignOrder(100L);
        
        when(tableRepository.findById(tableId)).thenReturn(Optional.of(expectedTable));
        
        // When
        Table result = getTableByIdUseCase.execute(tableId);
        
        // Then
        assertNotNull(result);
        assertEquals(tableId, result.getId());
        assertEquals("VIP-1", result.getTableNumber());
        assertEquals(8, result.getCapacity());
        assertEquals(TableStatus.OCCUPIED, result.getStatus());
        assertEquals(100L, result.getCurrentOrderId());
        assertNotNull(result.getCreatedAt());
        assertNotNull(result.getUpdatedAt());
        
        verify(tableRepository).findById(tableId);
    }
}
