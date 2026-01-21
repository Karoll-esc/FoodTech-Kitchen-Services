package com.foodtech.kitchen.application.usecases;

import com.foodtech.kitchen.application.exception.TableAlreadyExistsException;
import com.foodtech.kitchen.application.ports.out.TableRepository;
import com.foodtech.kitchen.domain.model.Table;
import com.foodtech.kitchen.domain.model.TableStatus;
import com.foodtech.kitchen.domain.services.TableValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para CreateTableUseCase.
 * 
 * <p>Valida las reglas de negocio del caso de uso:</p>
 * <ul>
 *   <li>Validación de tableNumber y capacity antes de crear</li>
 *   <li>Verificación de unicidad del número de mesa</li>
 *   <li>Creación exitosa con ID asignado</li>
 *   <li>Estado inicial AVAILABLE</li>
 *   <li>Manejo de mesas duplicadas</li>
 * </ul>
 */
class CreateTableUseCaseTest {
    
    private TableRepository tableRepository;
    private TableValidator tableValidator;
    private CreateTableUseCase createTableUseCase;
    
    @BeforeEach
    void setUp() {
        tableRepository = mock(TableRepository.class);
        tableValidator = new TableValidator();
        createTableUseCase = new CreateTableUseCase(tableRepository, tableValidator);
    }
    
    @Test
    @DisplayName("Debe crear mesa válida y retornar con ID asignado")
    void shouldCreateValidTableAndReturnWithAssignedId() {
        // Given
        String tableNumber = "A1";
        int capacity = 4;
        
        Table savedTable = new Table(tableNumber, capacity);
        savedTable.setId(1L);
        
        when(tableRepository.existsByTableNumber(tableNumber)).thenReturn(false);
        when(tableRepository.save(any(Table.class))).thenReturn(savedTable);
        
        // When
        Table result = createTableUseCase.execute(tableNumber, capacity);
        
        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(tableNumber, result.getTableNumber());
        assertEquals(capacity, result.getCapacity());
        assertEquals(TableStatus.AVAILABLE, result.getStatus());
        assertNull(result.getCurrentOrderId());
        
        verify(tableRepository).existsByTableNumber(tableNumber);
        verify(tableRepository).save(any(Table.class));
    }
    
    @Test
    @DisplayName("Debe lanzar excepción cuando el número de mesa ya existe")
    void shouldThrowExceptionWhenTableNumberAlreadyExists() {
        // Given
        String tableNumber = "A1";
        int capacity = 4;
        
        when(tableRepository.existsByTableNumber(tableNumber)).thenReturn(true);
        
        // When & Then
        TableAlreadyExistsException exception = assertThrows(
            TableAlreadyExistsException.class,
            () -> createTableUseCase.execute(tableNumber, capacity)
        );
        
        assertTrue(exception.getMessage().contains("A1"));
        assertTrue(exception.getMessage().contains("already exists"));
        
        verify(tableRepository).existsByTableNumber(tableNumber);
        verify(tableRepository, never()).save(any(Table.class));
    }
    
    @Test
    @DisplayName("Debe lanzar excepción cuando tableNumber es null")
    void shouldThrowExceptionWhenTableNumberIsNull() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> createTableUseCase.execute(null, 4)
        );
        
        assertEquals("Table number cannot be null or empty", exception.getMessage());
        verify(tableRepository, never()).existsByTableNumber(any());
        verify(tableRepository, never()).save(any(Table.class));
    }
    
    @Test
    @DisplayName("Debe lanzar excepción cuando tableNumber está vacío")
    void shouldThrowExceptionWhenTableNumberIsEmpty() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> createTableUseCase.execute("", 4)
        );
        
        assertEquals("Table number cannot be null or empty", exception.getMessage());
        verify(tableRepository, never()).existsByTableNumber(any());
        verify(tableRepository, never()).save(any(Table.class));
    }
    
    @Test
    @DisplayName("Debe lanzar excepción cuando tableNumber excede 10 caracteres")
    void shouldThrowExceptionWhenTableNumberExceedsMaxLength() {
        // Given
        String longTableNumber = "VERYLONGTABLENUMBER";
        
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> createTableUseCase.execute(longTableNumber, 4)
        );
        
        assertEquals("Table number cannot exceed 10 characters", exception.getMessage());
        verify(tableRepository, never()).existsByTableNumber(any());
        verify(tableRepository, never()).save(any(Table.class));
    }
    
    @Test
    @DisplayName("Debe lanzar excepción cuando capacity es cero")
    void shouldThrowExceptionWhenCapacityIsZero() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> createTableUseCase.execute("A1", 0)
        );
        
        assertEquals("Table capacity must be greater than zero", exception.getMessage());
        verify(tableRepository, never()).existsByTableNumber(any());
        verify(tableRepository, never()).save(any(Table.class));
    }
    
    @Test
    @DisplayName("Debe lanzar excepción cuando capacity es negativo")
    void shouldThrowExceptionWhenCapacityIsNegative() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> createTableUseCase.execute("A1", -5)
        );
        
        assertEquals("Table capacity must be greater than zero", exception.getMessage());
        verify(tableRepository, never()).existsByTableNumber(any());
        verify(tableRepository, never()).save(any(Table.class));
    }
    
    @Test
    @DisplayName("Debe lanzar excepción cuando capacity excede 50")
    void shouldThrowExceptionWhenCapacityExceedsMaximum() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> createTableUseCase.execute("A1", 51)
        );
        
        assertEquals("Table capacity cannot exceed 50 people", exception.getMessage());
        verify(tableRepository, never()).existsByTableNumber(any());
        verify(tableRepository, never()).save(any(Table.class));
    }
    
    @Test
    @DisplayName("Debe crear mesa con capacidad mínima válida de 1 persona")
    void shouldCreateTableWithMinimumValidCapacity() {
        // Given
        String tableNumber = "A1";
        int capacity = 1;
        
        Table savedTable = new Table(tableNumber, capacity);
        savedTable.setId(1L);
        
        when(tableRepository.existsByTableNumber(tableNumber)).thenReturn(false);
        when(tableRepository.save(any(Table.class))).thenReturn(savedTable);
        
        // When
        Table result = createTableUseCase.execute(tableNumber, capacity);
        
        // Then
        assertNotNull(result);
        assertEquals(1, result.getCapacity());
        verify(tableRepository).save(any(Table.class));
    }
    
    @Test
    @DisplayName("Debe crear mesa con capacidad máxima válida de 50 personas")
    void shouldCreateTableWithMaximumValidCapacity() {
        // Given
        String tableNumber = "A1";
        int capacity = 50;
        
        Table savedTable = new Table(tableNumber, capacity);
        savedTable.setId(1L);
        
        when(tableRepository.existsByTableNumber(tableNumber)).thenReturn(false);
        when(tableRepository.save(any(Table.class))).thenReturn(savedTable);
        
        // When
        Table result = createTableUseCase.execute(tableNumber, capacity);
        
        // Then
        assertNotNull(result);
        assertEquals(50, result.getCapacity());
        verify(tableRepository).save(any(Table.class));
    }
}
