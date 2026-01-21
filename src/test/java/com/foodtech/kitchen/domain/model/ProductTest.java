package com.foodtech.kitchen.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ProductTest {

    // ==================== HAPPY PATH TESTS ====================

    @Test
    @DisplayName("Debe crear un producto válido con todos los campos requeridos")
    void shouldCreateValidProductWithAllRequiredFields() {
        String name = "Pizza Margherita";
        String description = "Pizza con tomate y mozzarella";
        ProductType type = ProductType.HOT_DISH;
        Price price = new Price(new BigDecimal("12.99"));
        int preparationTime = 900;

        Product product = new Product(name, description, type, price, preparationTime);

        assertEquals(name, product.getName());
        assertEquals(description, product.getDescription());
        assertEquals(type, product.getType());
        assertEquals(price, product.getPrice());
        assertEquals(preparationTime, product.getPreparationTimeSeconds());
    }

    @Test
    @DisplayName("Debe crear producto con disponibilidad true por defecto")
    void shouldCreateProductWithDefaultAvailabilityTrue() {
        Product product = createValidProduct();

        assertTrue(product.isAvailable());
    }

    @Test
    @DisplayName("Debe establecer createdAt automáticamente al crear producto")
    void shouldSetCreatedAtAutomatically() {
        LocalDateTime before = LocalDateTime.now();
        Product product = createValidProduct();
        LocalDateTime after = LocalDateTime.now();

        assertNotNull(product.getCreatedAt());
        assertTrue(product.getCreatedAt().isAfter(before.minusSeconds(1)));
        assertTrue(product.getCreatedAt().isBefore(after.plusSeconds(1)));
    }

    @Test
    @DisplayName("Debe establecer updatedAt igual a createdAt al crear producto")
    void shouldSetUpdatedAtEqualToCreatedAtOnCreation() {
        Product product = createValidProduct();

        assertNotNull(product.getUpdatedAt());
        assertEquals(product.getCreatedAt(), product.getUpdatedAt());
    }

    @Test
    @DisplayName("Debe crear producto con descripción vacía pero no null")
    void shouldCreateProductWithEmptyDescription() {
        Product product = new Product(
            "Coca Cola",
            "",
            ProductType.DRINK,
            new Price(new BigDecimal("2.50")),
            180
        );

        assertEquals("", product.getDescription());
    }

    @Test
    @DisplayName("Debe crear producto de tipo DRINK")
    void shouldCreateDrinkProduct() {
        Product product = new Product(
            "Mojito",
            "Cóctel de ron blanco",
            ProductType.DRINK,
            new Price(new BigDecimal("8.50")),
            300
        );

        assertEquals(ProductType.DRINK, product.getType());
        assertEquals(Station.BAR, product.getType().getStation());
    }

    @Test
    @DisplayName("Debe crear producto de tipo COLD_DISH")
    void shouldCreateColdDishProduct() {
        Product product = new Product(
            "Ensalada César",
            "Ensalada con pollo",
            ProductType.COLD_DISH,
            new Price(new BigDecimal("8.99")),
            480
        );

        assertEquals(ProductType.COLD_DISH, product.getType());
        assertEquals(Station.COLD_KITCHEN, product.getType().getStation());
    }

    // ==================== VALIDATION TESTS - NAME ====================

    @Test
    @DisplayName("Debe lanzar excepción cuando el nombre es null")
    void shouldThrowExceptionWhenNameIsNull() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new Product(null, "Description", ProductType.DRINK, 
                new Price(new BigDecimal("2.50")), 180)
        );

        assertEquals("Product name cannot be null or empty", exception.getMessage());
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando el nombre está vacío")
    void shouldThrowExceptionWhenNameIsEmpty() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new Product("", "Description", ProductType.DRINK, 
                new Price(new BigDecimal("2.50")), 180)
        );

        assertEquals("Product name cannot be null or empty", exception.getMessage());
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando el nombre solo tiene espacios")
    void shouldThrowExceptionWhenNameIsBlank() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new Product("   ", "Description", ProductType.DRINK, 
                new Price(new BigDecimal("2.50")), 180)
        );

        assertEquals("Product name cannot be null or empty", exception.getMessage());
    }

    // ==================== VALIDATION TESTS - DESCRIPTION ====================

    @Test
    @DisplayName("Debe lanzar excepción cuando la descripción es null")
    void shouldThrowExceptionWhenDescriptionIsNull() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new Product("Product", null, ProductType.DRINK, 
                new Price(new BigDecimal("2.50")), 180)
        );

        assertEquals("Product description cannot be null", exception.getMessage());
    }

    // ==================== VALIDATION TESTS - TYPE ====================

    @Test
    @DisplayName("Debe lanzar excepción cuando el tipo es null")
    void shouldThrowExceptionWhenTypeIsNull() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new Product("Product", "Description", null, 
                new Price(new BigDecimal("2.50")), 180)
        );

        assertEquals("Product type cannot be null", exception.getMessage());
    }

    // ==================== VALIDATION TESTS - PRICE ====================

    @Test
    @DisplayName("Debe lanzar excepción cuando el precio es null")
    void shouldThrowExceptionWhenPriceIsNull() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new Product("Product", "Description", ProductType.DRINK, null, 180)
        );

        assertEquals("Product price cannot be null", exception.getMessage());
    }

    // ==================== VALIDATION TESTS - PREPARATION TIME ====================

    @Test
    @DisplayName("Debe lanzar excepción cuando el tiempo de preparación es cero")
    void shouldThrowExceptionWhenPreparationTimeIsZero() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new Product("Product", "Description", ProductType.DRINK, 
                new Price(new BigDecimal("2.50")), 0)
        );

        assertEquals("Preparation time must be greater than zero", exception.getMessage());
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando el tiempo de preparación es negativo")
    void shouldThrowExceptionWhenPreparationTimeIsNegative() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new Product("Product", "Description", ProductType.DRINK, 
                new Price(new BigDecimal("2.50")), -10)
        );

        assertEquals("Preparation time must be greater than zero", exception.getMessage());
    }

    @Test
    @DisplayName("Debe aceptar tiempo de preparación de 1 segundo")
    void shouldAcceptPreparationTimeOfOneSecond() {
        Product product = new Product(
            "Water",
            "Bottled water",
            ProductType.DRINK,
            new Price(new BigDecimal("1.00")),
            1
        );

        assertEquals(1, product.getPreparationTimeSeconds());
    }

    // ==================== SETTERS WITH ID TESTS ====================

    @Test
    @DisplayName("Debe permitir establecer ID para productos nuevos")
    void shouldAllowSettingIdForNewProducts() {
        Product product = createValidProduct();
        assertNull(product.getId());

        product.setId(1L);

        assertEquals(1L, product.getId());
    }

    @Test
    @DisplayName("Debe permitir actualizar updatedAt")
    void shouldAllowUpdatingUpdatedAt() throws InterruptedException {
        Product product = createValidProduct();
        LocalDateTime originalUpdatedAt = product.getUpdatedAt();

        Thread.sleep(10); // Small delay to ensure time difference
        product.updateTimestamp();

        assertNotNull(product.getUpdatedAt());
        assertTrue(product.getUpdatedAt().isAfter(originalUpdatedAt));
    }

    @Test
    @DisplayName("Debe permitir cambiar disponibilidad a false")
    void shouldAllowChangingAvailabilityToFalse() {
        Product product = createValidProduct();
        assertTrue(product.isAvailable());

        product.setAvailable(false);

        assertFalse(product.isAvailable());
    }

    @Test
    @DisplayName("Debe permitir cambiar disponibilidad a true")
    void shouldAllowChangingAvailabilityToTrue() {
        Product product = createValidProduct();
        product.setAvailable(false);

        product.setAvailable(true);

        assertTrue(product.isAvailable());
    }

    // ==================== EDGE CASES ====================

    @Test
    @DisplayName("Debe crear producto con nombre muy largo")
    void shouldCreateProductWithLongName() {
        String longName = "A".repeat(100);
        Product product = new Product(
            longName,
            "Description",
            ProductType.DRINK,
            new Price(new BigDecimal("2.50")),
            180
        );

        assertEquals(longName, product.getName());
        assertEquals(100, product.getName().length());
    }

    @Test
    @DisplayName("Debe crear producto con tiempo de preparación muy grande")
    void shouldCreateProductWithLargePreparationTime() {
        int largeTime = 3600; // 1 hour
        Product product = new Product(
            "Slow Roast",
            "Takes a long time",
            ProductType.HOT_DISH,
            new Price(new BigDecimal("25.00")),
            largeTime
        );

        assertEquals(largeTime, product.getPreparationTimeSeconds());
    }

    @Test
    @DisplayName("Debe mantener el precio inmutable")
    void shouldKeepPriceImmutable() {
        Price originalPrice = new Price(new BigDecimal("10.00"));
        Product product = new Product(
            "Product",
            "Description",
            ProductType.DRINK,
            originalPrice,
            180
        );

        assertEquals(originalPrice, product.getPrice());
        assertSame(originalPrice, product.getPrice());
    }

    // ==================== HELPER METHOD ====================

    private Product createValidProduct() {
        return new Product(
            "Test Product",
            "Test Description",
            ProductType.DRINK,
            new Price(new BigDecimal("5.00")),
            300
        );
    }
}
