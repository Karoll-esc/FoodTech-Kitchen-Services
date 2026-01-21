package com.foodtech.kitchen.domain.services;

import com.foodtech.kitchen.domain.model.Table;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios para TableValidator.
 * 
 * <p>Valida las reglas de negocio de validación de mesas:</p>
 * <ul>
 *   <li>Mesa no puede ser null</li>
 *   <li>TableNumber no puede ser null o vacío</li>
 *   <li>TableNumber no puede exceder 10 caracteres</li>
 *   <li>Capacity debe ser mayor a 0</li>
 *   <li>Capacity no puede exceder 50 personas</li>
 * </ul>
 */
class TableValidatorTest {
    
    private TableValidator validator;
    
    @BeforeEach
    void setUp() {
        validator = new TableValidator();
    }
    
    @Test
    @DisplayName("Debe validar mesa correctamente cuando todos los datos son válidos")
    void shouldValidateTableSuccessfullyWhenAllDataIsValid() {
        // Given
        Table table = new Table("A1", 4);
        
        // When & Then
        assertDoesNotThrow(() -> validator.validate(table));
    }
    
    @Test
    @DisplayName("Debe lanzar excepción cuando mesa es null")
    void shouldThrowExceptionWhenTableIsNull() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> validator.validate(null)
        );
        
        assertEquals("Table cannot be null", exception.getMessage());
    }
    
    @Test
    @DisplayName("Debe lanzar excepción cuando tableNumber es null")
    void shouldThrowExceptionWhenTableNumberIsNull() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> validator.validateTableNumber(null)
        );
        
        assertEquals("Table number cannot be null or empty", exception.getMessage());
    }
    
    @Test
    @DisplayName("Debe lanzar excepción cuando tableNumber está vacío")
    void shouldThrowExceptionWhenTableNumberIsEmpty() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> validator.validateTableNumber("")
        );
        
        assertEquals("Table number cannot be null or empty", exception.getMessage());
    }
    
    @Test
    @DisplayName("Debe lanzar excepción cuando tableNumber solo contiene espacios")
    void shouldThrowExceptionWhenTableNumberIsBlank() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> validator.validateTableNumber("   ")
        );
        
        assertEquals("Table number cannot be null or empty", exception.getMessage());
    }
    
    @Test
    @DisplayName("Debe lanzar excepción cuando tableNumber excede 10 caracteres")
    void shouldThrowExceptionWhenTableNumberExceedsMaxLength() {
        // Given
        String longTableNumber = "TABLEVERYLONGNUMBER"; // 19 caracteres
        
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> validator.validateTableNumber(longTableNumber)
        );
        
        assertEquals("Table number cannot exceed 10 characters", exception.getMessage());
    }
    
    @Test
    @DisplayName("Debe validar correctamente tableNumber de 10 caracteres")
    void shouldValidateTableNumberWithExactlyMaxLength() {
        // Given
        String tableNumber = "TABLE12345"; // Exactamente 10 caracteres
        
        // When & Then
        assertDoesNotThrow(() -> validator.validateTableNumber(tableNumber));
    }
    
    @Test
    @DisplayName("Debe validar correctamente tableNumber de 1 carácter")
    void shouldValidateTableNumberWithSingleCharacter() {
        // Given
        String tableNumber = "A";
        
        // When & Then
        assertDoesNotThrow(() -> validator.validateTableNumber(tableNumber));
    }
    
    @Test
    @DisplayName("Debe lanzar excepción cuando capacity es cero")
    void shouldThrowExceptionWhenCapacityIsZero() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> validator.validateCapacity(0)
        );
        
        assertEquals("Table capacity must be greater than zero", exception.getMessage());
    }
    
    @Test
    @DisplayName("Debe lanzar excepción cuando capacity es negativo")
    void shouldThrowExceptionWhenCapacityIsNegative() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> validator.validateCapacity(-1)
        );
        
        assertEquals("Table capacity must be greater than zero", exception.getMessage());
    }
    
    @Test
    @DisplayName("Debe lanzar excepción cuando capacity excede 50 personas")
    void shouldThrowExceptionWhenCapacityExceedsMaximum() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> validator.validateCapacity(51)
        );
        
        assertEquals("Table capacity cannot exceed 50 people", exception.getMessage());
    }
    
    @Test
    @DisplayName("Debe validar correctamente capacity de 50 personas")
    void shouldValidateCapacityWithExactlyMaximum() {
        // When & Then
        assertDoesNotThrow(() -> validator.validateCapacity(50));
    }
    
    @Test
    @DisplayName("Debe validar correctamente capacity de 1 persona")
    void shouldValidateCapacityWithMinimumValid() {
        // When & Then
        assertDoesNotThrow(() -> validator.validateCapacity(1));
    }
    
    @Test
    @DisplayName("Debe validar correctamente capacity típica de 4 personas")
    void shouldValidateTypicalCapacityOfFourPeople() {
        // When & Then
        assertDoesNotThrow(() -> validator.validateCapacity(4));
    }
}
