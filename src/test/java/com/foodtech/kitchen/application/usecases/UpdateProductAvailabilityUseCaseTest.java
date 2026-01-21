package com.foodtech.kitchen.application.usecases;

import com.foodtech.kitchen.application.exception.ProductNotFoundException;
import com.foodtech.kitchen.application.ports.out.ProductRepository;
import com.foodtech.kitchen.domain.model.Price;
import com.foodtech.kitchen.domain.model.Product;
import com.foodtech.kitchen.domain.model.ProductType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UpdateProductAvailabilityUseCaseTest {

    private ProductRepository productRepository;
    private UpdateProductAvailabilityUseCase updateProductAvailabilityUseCase;

    @BeforeEach
    void setUp() {
        productRepository = mock(ProductRepository.class);
        updateProductAvailabilityUseCase = new UpdateProductAvailabilityUseCase(productRepository);
    }

    @Test
    @DisplayName("Debe lanzar IllegalArgumentException si el repositorio es nulo")
    void shouldThrowExceptionWhenRepositoryIsNull() {
        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> new UpdateProductAvailabilityUseCase(null));
        
        assertEquals("ProductRepository cannot be null", exception.getMessage());
    }

    @Test
    @DisplayName("Debe habilitar un producto que estaba deshabilitado")
    void shouldEnableUnavailableProduct() throws InterruptedException {
        // Given
        Long productId = 1L;
        Product unavailableProduct = new Product(
            productId,
            "Coca Cola",
            "Bebida refrescante",
            ProductType.DRINK,
            new Price(new BigDecimal("5.00")),
            5,
            false  // Initially unavailable
        );
        
        LocalDateTime originalUpdatedAt = unavailableProduct.getUpdatedAt();
        
        // Wait to ensure timestamp difference
        Thread.sleep(10);
        
        when(productRepository.findById(productId)).thenReturn(Optional.of(unavailableProduct));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // When
        Product updatedProduct = updateProductAvailabilityUseCase.execute(productId, true);
        
        // Then
        assertNotNull(updatedProduct);
        assertTrue(updatedProduct.isAvailable());
        
        // Verify timestamp was updated
        assertTrue(updatedProduct.getUpdatedAt().isAfter(originalUpdatedAt));
        
        // Verify other fields remain unchanged
        assertEquals(productId, updatedProduct.getId());
        assertEquals("Coca Cola", updatedProduct.getName());
        assertEquals("Bebida refrescante", updatedProduct.getDescription());
        assertEquals(ProductType.DRINK, updatedProduct.getType());
        assertEquals(new BigDecimal("5.00"), updatedProduct.getPrice().getAmount());
        assertEquals(5, updatedProduct.getPreparationTime());
        
        verify(productRepository).findById(productId);
        verify(productRepository).save(any(Product.class));
    }

    @Test
    @DisplayName("Debe deshabilitar un producto que estaba habilitado")
    void shouldDisableAvailableProduct() throws InterruptedException {
        // Given
        Long productId = 2L;
        Product availableProduct = new Product(
            productId,
            "Pizza Margherita",
            "Pizza con tomate y mozzarella",
            ProductType.HOT_DISH,
            new Price(new BigDecimal("12.50")),
            15,
            true  // Initially available
        );
        
        LocalDateTime originalUpdatedAt = availableProduct.getUpdatedAt();
        
        // Wait to ensure timestamp difference
        Thread.sleep(10);
        
        when(productRepository.findById(productId)).thenReturn(Optional.of(availableProduct));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // When
        Product updatedProduct = updateProductAvailabilityUseCase.execute(productId, false);
        
        // Then
        assertNotNull(updatedProduct);
        assertFalse(updatedProduct.isAvailable());
        
        // Verify timestamp was updated
        assertTrue(updatedProduct.getUpdatedAt().isAfter(originalUpdatedAt));
        
        // Verify other fields remain unchanged
        assertEquals(productId, updatedProduct.getId());
        assertEquals("Pizza Margherita", updatedProduct.getName());
        assertEquals("Pizza con tomate y mozzarella", updatedProduct.getDescription());
        assertEquals(ProductType.HOT_DISH, updatedProduct.getType());
        assertEquals(new BigDecimal("12.50"), updatedProduct.getPrice().getAmount());
        assertEquals(15, updatedProduct.getPreparationTime());
        
        verify(productRepository).findById(productId);
        verify(productRepository).save(any(Product.class));
    }

    @Test
    @DisplayName("Debe actualizar el timestamp aunque el valor de disponibilidad sea el mismo")
    void shouldUpdateTimestampEvenWhenAvailabilityIsSame() throws InterruptedException {
        // Given
        Long productId = 3L;
        Product availableProduct = new Product(
            productId,
            "Ensalada César",
            "Ensalada fresca",
            ProductType.COLD_DISH,
            new Price(new BigDecimal("8.00")),
            10,
            true  // Already available
        );
        
        LocalDateTime originalUpdatedAt = availableProduct.getUpdatedAt();
        
        // Wait to ensure timestamp difference
        Thread.sleep(10);
        
        when(productRepository.findById(productId)).thenReturn(Optional.of(availableProduct));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // When - Set to same value (true -> true)
        Product updatedProduct = updateProductAvailabilityUseCase.execute(productId, true);
        
        // Then
        assertNotNull(updatedProduct);
        assertTrue(updatedProduct.isAvailable());
        
        // Verify timestamp was updated even though value is the same
        assertTrue(updatedProduct.getUpdatedAt().isAfter(originalUpdatedAt));
        
        verify(productRepository).findById(productId);
        verify(productRepository).save(any(Product.class));
    }

    @Test
    @DisplayName("Debe lanzar ProductNotFoundException cuando el producto no existe")
    void shouldThrowProductNotFoundExceptionWhenProductDoesNotExist() {
        // Given
        Long nonExistentId = 999L;
        when(productRepository.findById(nonExistentId)).thenReturn(Optional.empty());
        
        // When & Then
        ProductNotFoundException exception = assertThrows(ProductNotFoundException.class,
            () -> updateProductAvailabilityUseCase.execute(nonExistentId, true));
        
        assertEquals("Product not found with id: 999", exception.getMessage());
        verify(productRepository).findById(nonExistentId);
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    @DisplayName("Debe mantener todos los campos inmutables sin cambios al cambiar disponibilidad")
    void shouldKeepAllImmutableFieldsUnchangedWhenChangingAvailability() {
        // Given
        Long productId = 4L;
        String originalName = "Hamburguesa Clásica";
        String originalDescription = "Hamburguesa con queso";
        ProductType originalType = ProductType.HOT_DISH;
        Price originalPrice = new Price(new BigDecimal("10.99"));
        Integer originalPreparationTime = 12;
        LocalDateTime originalCreatedAt = LocalDateTime.now().minusDays(30);
        
        Product product = new Product(
            productId,
            originalName,
            originalDescription,
            originalType,
            originalPrice,
            originalPreparationTime,
            true
        );
        
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // When - Disable the product
        Product updatedProduct = updateProductAvailabilityUseCase.execute(productId, false);
        
        // Then - Verify all immutable fields remain unchanged
        assertEquals(productId, updatedProduct.getId());
        assertEquals(originalName, updatedProduct.getName());
        assertEquals(originalDescription, updatedProduct.getDescription());
        assertEquals(originalType, updatedProduct.getType());
        assertEquals(originalPrice.getAmount(), updatedProduct.getPrice().getAmount());
        assertEquals(originalPreparationTime, updatedProduct.getPreparationTime());
        assertEquals(product.getCreatedAt(), updatedProduct.getCreatedAt());
        
        // Only availability should change
        assertFalse(updatedProduct.isAvailable());
        
        verify(productRepository).findById(productId);
        verify(productRepository).save(any(Product.class));
    }

    @Test
    @DisplayName("Debe cambiar disponibilidad múltiples veces correctamente")
    void shouldToggleAvailabilityMultipleTimes() throws InterruptedException {
        // Given
        Long productId = 5L;
        Product product = new Product(
            productId,
            "Sprite",
            "Bebida refrescante",
            ProductType.DRINK,
            new Price(new BigDecimal("4.50")),
            3,
            true  // Start as available
        );
        
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // When & Then - Toggle multiple times
        
        // First toggle: available -> unavailable
        Thread.sleep(10);
        Product firstUpdate = updateProductAvailabilityUseCase.execute(productId, false);
        assertFalse(firstUpdate.isAvailable());
        
        // Second toggle: unavailable -> available
        Thread.sleep(10);
        Product secondUpdate = updateProductAvailabilityUseCase.execute(productId, true);
        assertTrue(secondUpdate.isAvailable());
        
        // Third toggle: available -> unavailable
        Thread.sleep(10);
        Product thirdUpdate = updateProductAvailabilityUseCase.execute(productId, false);
        assertFalse(thirdUpdate.isAvailable());
        
        // Verify repository was called 3 times for each toggle
        verify(productRepository, times(3)).findById(productId);
        verify(productRepository, times(3)).save(any(Product.class));
    }
}
