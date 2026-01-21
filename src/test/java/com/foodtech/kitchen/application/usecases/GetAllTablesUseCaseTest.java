package com.foodtech.kitchen.application.usecases;

import com.foodtech.kitchen.application.ports.out.TableRepository;
import com.foodtech.kitchen.domain.model.Table;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para GetAllTablesUseCase.
 * 
 * <p>Valida el comportamiento del caso de uso:</p>
 * <ul>
 *   <li>Retorna todas las mesas del sistema</li>
 *   <li>Retorna lista vacía si no hay mesas</li>
 *   <li>Preserva el orden retornado por el repositorio</li>
 * </ul>
 */
class GetAllTablesUseCaseTest {
    
    private TableRepository tableRepository;
    private GetAllTablesUseCase getAllTablesUseCase;
    
    @BeforeEach
    void setUp() {
        tableRepository = mock(TableRepository.class);
        getAllTablesUseCase = new GetAllTablesUseCase(tableRepository);
    }
    
    @Test
    @DisplayName("Debe retornar todas las mesas del sistema")
    void shouldReturnAllTablesFromSystem() {
        // Given
        Table table1 = new Table("A1", 4);
        table1.setId(1L);
        
        Table table2 = new Table("B3", 6);
        table2.setId(2L);
        
        Table table3 = new Table("C5", 2);
        table3.setId(3L);
        
        List<Table> expectedTables = Arrays.asList(table1, table2, table3);
        when(tableRepository.findAll()).thenReturn(expectedTables);
        
        // When
        List<Table> result = getAllTablesUseCase.execute();
        
        // Then
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals("A1", result.get(0).getTableNumber());
        assertEquals("B3", result.get(1).getTableNumber());
        assertEquals("C5", result.get(2).getTableNumber());
        
        verify(tableRepository).findAll();
    }
    
    @Test
    @DisplayName("Debe retornar lista vacía cuando no hay mesas")
    void shouldReturnEmptyListWhenNoTablesExist() {
        // Given
        when(tableRepository.findAll()).thenReturn(Collections.emptyList());
        
        // When
        List<Table> result = getAllTablesUseCase.execute();
        
        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        
        verify(tableRepository).findAll();
    }
    
    @Test
    @DisplayName("Debe retornar una sola mesa cuando solo existe una")
    void shouldReturnSingleTableWhenOnlyOneExists() {
        // Given
        Table table = new Table("A1", 4);
        table.setId(1L);
        
        when(tableRepository.findAll()).thenReturn(Collections.singletonList(table));
        
        // When
        List<Table> result = getAllTablesUseCase.execute();
        
        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("A1", result.get(0).getTableNumber());
        assertEquals(4, result.get(0).getCapacity());
        
        verify(tableRepository).findAll();
    }
}
