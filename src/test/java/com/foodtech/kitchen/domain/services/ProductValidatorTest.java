package com.foodtech.kitchen.domain.services;

import com.foodtech.kitchen.domain.model.Price;
import com.foodtech.kitchen.domain.model.Product;
import com.foodtech.kitchen.domain.model.ProductType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class ProductValidatorTest {

    private ProductValidator validator;

    @BeforeEach
    void setUp() {
        validator = new ProductValidator();
    }

    // ==================== VALIDATE FULL PRODUCT TESTS ====================

    @Test
    @DisplayName("Debe validar correctamente un producto válido completo")
    void shouldValidateValidProduct() {
        Product validProduct = createValidProduct();

        assertDoesNotThrow(() -> validator.validate(validProduct));
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando el producto es null")
    void shouldThrowExceptionWhenProductIsNull() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> validator.validate(null)
        );

        assertEquals("Product cannot be null", exception.getMessage());
    }

    // ==================== VALIDATE NAME TESTS ====================

    @Test
    @DisplayName("Debe validar correctamente un nombre válido")
    void shouldValidateValidName() {
        assertDoesNotThrow(() -> validator.validateName("Pizza Margherita"));
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando el nombre es null")
    void shouldThrowExceptionWhenNameIsNull() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> validator.validateName(null)
        );

        assertEquals("Product name cannot be null or empty", exception.getMessage());
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando el nombre está vacío")
    void shouldThrowExceptionWhenNameIsEmpty() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> validator.validateName("")
        );

        assertEquals("Product name cannot be null or empty", exception.getMessage());
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando el nombre solo tiene espacios")
    void shouldThrowExceptionWhenNameIsBlank() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> validator.validateName("   ")
        );

        assertEquals("Product name cannot be null or empty", exception.getMessage());
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando el nombre excede 100 caracteres")
    void shouldThrowExceptionWhenNameExceeds100Characters() {
        String longName = "A".repeat(101);

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> validator.validateName(longName)
        );

        assertEquals("Product name cannot exceed 100 characters", exception.getMessage());
    }

    @Test
    @DisplayName("Debe aceptar nombre de exactamente 100 caracteres")
    void shouldAcceptNameOfExactly100Characters() {
        String nameWith100Chars = "A".repeat(100);

        assertDoesNotThrow(() -> validator.validateName(nameWith100Chars));
    }

    @Test
    @DisplayName("Debe aceptar nombre de 1 carácter")
    void shouldAcceptNameWithSingleCharacter() {
        assertDoesNotThrow(() -> validator.validateName("A"));
    }

    // ==================== VALIDATE DESCRIPTION TESTS ====================

    @Test
    @DisplayName("Debe validar correctamente una descripción válida")
    void shouldValidateValidDescription() {
        assertDoesNotThrow(() -> validator.validateDescription("Pizza con tomate y mozzarella"));
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando la descripción es null")
    void shouldThrowExceptionWhenDescriptionIsNull() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> validator.validateDescription(null)
        );

        assertEquals("Product description cannot be null", exception.getMessage());
    }

    @Test
    @DisplayName("Debe aceptar descripción vacía")
    void shouldAcceptEmptyDescription() {
        assertDoesNotThrow(() -> validator.validateDescription(""));
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando la descripción excede 500 caracteres")
    void shouldThrowExceptionWhenDescriptionExceeds500Characters() {
        String longDescription = "A".repeat(501);

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> validator.validateDescription(longDescription)
        );

        assertEquals("Product description cannot exceed 500 characters", exception.getMessage());
    }

    @Test
    @DisplayName("Debe aceptar descripción de exactamente 500 caracteres")
    void shouldAcceptDescriptionOfExactly500Characters() {
        String descriptionWith500Chars = "A".repeat(500);

        assertDoesNotThrow(() -> validator.validateDescription(descriptionWith500Chars));
    }

    // ==================== VALIDATE TYPE TESTS ====================

    @Test
    @DisplayName("Debe validar correctamente un tipo válido")
    void shouldValidateValidType() {
        assertDoesNotThrow(() -> validator.validateType(ProductType.DRINK));
        assertDoesNotThrow(() -> validator.validateType(ProductType.HOT_DISH));
        assertDoesNotThrow(() -> validator.validateType(ProductType.COLD_DISH));
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando el tipo es null")
    void shouldThrowExceptionWhenTypeIsNull() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> validator.validateType(null)
        );

        assertEquals("Product type cannot be null", exception.getMessage());
    }

    // ==================== VALIDATE PRICE TESTS ====================

    @Test
    @DisplayName("Debe validar correctamente un precio válido")
    void shouldValidateValidPrice() {
        Price validPrice = new Price(new BigDecimal("10.99"));

        assertDoesNotThrow(() -> validator.validatePrice(validPrice));
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando el precio es null")
    void shouldThrowExceptionWhenPriceIsNull() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> validator.validatePrice(null)
        );

        assertEquals("Product price cannot be null", exception.getMessage());
    }

    @Test
    @DisplayName("Debe aceptar precio cero")
    void shouldAcceptZeroPrice() {
        Price zeroPrice = new Price(BigDecimal.ZERO);

        assertDoesNotThrow(() -> validator.validatePrice(zeroPrice));
    }

    @Test
    @DisplayName("Debe aceptar precio muy grande")
    void shouldAcceptLargePrice() {
        Price largePrice = new Price(new BigDecimal("999999.99"));

        assertDoesNotThrow(() -> validator.validatePrice(largePrice));
    }

    // ==================== VALIDATE PREPARATION TIME TESTS ====================

    @Test
    @DisplayName("Debe validar correctamente un tiempo de preparación válido")
    void shouldValidateValidPreparationTime() {
        assertDoesNotThrow(() -> validator.validatePreparationTime(300));
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando el tiempo de preparación es cero")
    void shouldThrowExceptionWhenPreparationTimeIsZero() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> validator.validatePreparationTime(0)
        );

        assertEquals("Preparation time must be greater than zero", exception.getMessage());
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando el tiempo de preparación es negativo")
    void shouldThrowExceptionWhenPreparationTimeIsNegative() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> validator.validatePreparationTime(-10)
        );

        assertEquals("Preparation time must be greater than zero", exception.getMessage());
    }

    @Test
    @DisplayName("Debe aceptar tiempo de preparación de 1 segundo")
    void shouldAcceptPreparationTimeOfOneSecond() {
        assertDoesNotThrow(() -> validator.validatePreparationTime(1));
    }

    @Test
    @DisplayName("Debe aceptar tiempo de preparación muy grande")
    void shouldAcceptLargePreparationTime() {
        assertDoesNotThrow(() -> validator.validatePreparationTime(86400)); // 24 hours
    }

    // ==================== EDGE CASES ====================

    @Test
    @DisplayName("Debe validar producto con valores límite válidos")
    void shouldValidateProductWithBoundaryValues() {
        Product product = new Product(
            "A",                                    // Minimum name length (1)
            "",                                     // Minimum description length (0)
            ProductType.DRINK,
            new Price(BigDecimal.ZERO),            // Minimum price
            1                                       // Minimum preparation time
        );

        assertDoesNotThrow(() -> validator.validate(product));
    }

    @Test
    @DisplayName("Debe validar producto con valores máximos válidos")
    void shouldValidateProductWithMaximumValues() {
        Product product = new Product(
            "A".repeat(100),                        // Maximum name length
            "B".repeat(500),                        // Maximum description length
            ProductType.HOT_DISH,
            new Price(new BigDecimal("999999.99")),// Large price
            86400                                   // Large preparation time (24h)
        );

        assertDoesNotThrow(() -> validator.validate(product));
    }

    // ==================== HELPER METHODS ====================

    private Product createValidProduct() {
        return new Product(
            "Pizza Margherita",
            "Pizza con tomate, mozzarella y albahaca",
            ProductType.HOT_DISH,
            new Price(new BigDecimal("12.99")),
            900
        );
    }
}
