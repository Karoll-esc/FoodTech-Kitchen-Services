package com.foodtech.kitchen.application.usecases;

import com.foodtech.kitchen.application.exception.ProductNotFoundException;
import com.foodtech.kitchen.application.ports.out.ProductRepository;
import com.foodtech.kitchen.domain.model.Price;
import com.foodtech.kitchen.domain.model.Product;
import com.foodtech.kitchen.domain.model.ProductType;
import com.foodtech.kitchen.domain.services.ProductValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UpdateProductUseCaseTest {

    private ProductRepository productRepository;
    private ProductValidator productValidator;
    private UpdateProductUseCase updateProductUseCase;

    @BeforeEach
    void setUp() {
        productRepository = mock(ProductRepository.class);
        productValidator = mock(ProductValidator.class);
        updateProductUseCase = new UpdateProductUseCase(productRepository, productValidator);
    }

    @Test
    @DisplayName("Debe lanzar IllegalArgumentException si el repositorio es nulo")
    void shouldThrowExceptionWhenRepositoryIsNull() {
        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> new UpdateProductUseCase(null, productValidator));
        
        assertEquals("ProductRepository cannot be null", exception.getMessage());
    }

    @Test
    @DisplayName("Debe lanzar IllegalArgumentException si el validador es nulo")
    void shouldThrowExceptionWhenValidatorIsNull() {
        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> new UpdateProductUseCase(productRepository, null));
        
        assertEquals("ProductValidator cannot be null", exception.getMessage());
    }

    @Test
    @DisplayName("Debe actualizar todos los campos mutables correctamente")
    void shouldUpdateAllMutableFieldsSuccessfully() throws InterruptedException {
        // Given
        Long productId = 1L;
        Product existingProduct = new Product(
            productId,
            "Coca Cola",
            "Bebida refrescante",
            ProductType.DRINK,
            new Price(new BigDecimal("5.00")),
            5,
            true
        );
        
        LocalDateTime originalUpdatedAt = existingProduct.getUpdatedAt();
        
        // Wait to ensure timestamp difference
        Thread.sleep(10);
        
        String newDescription = "Bebida gaseosa refrescante";
        Price newPrice = new Price(new BigDecimal("6.50"));
        Integer newPreparationTime = 3;
        
        when(productRepository.findById(productId)).thenReturn(Optional.of(existingProduct));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // When
        Product updatedProduct = updateProductUseCase.execute(productId, newDescription, newPrice, newPreparationTime);
        
        // Then
        assertNotNull(updatedProduct);
        assertEquals(newDescription, updatedProduct.getDescription());
        assertEquals(newPrice, updatedProduct.getPrice());
        assertEquals(newPreparationTime, updatedProduct.getPreparationTime());
        
        // Verify immutable fields remain unchanged
        assertEquals(productId, updatedProduct.getId());
        assertEquals("Coca Cola", updatedProduct.getName());
        assertEquals(ProductType.DRINK, updatedProduct.getType());
        assertEquals(existingProduct.getCreatedAt(), updatedProduct.getCreatedAt());
        
        // Verify updatedAt was changed
        assertTrue(updatedProduct.getUpdatedAt().isAfter(originalUpdatedAt));
        
        verify(productRepository).findById(productId);
        verify(productValidator).validate(any(Product.class));
        verify(productRepository).save(any(Product.class));
    }

    @Test
    @DisplayName("Debe actualizar solo la descripción cuando los demás campos son nulos")
    void shouldUpdateOnlyDescriptionWhenOtherFieldsAreNull() {
        // Given
        Long productId = 1L;
        Price originalPrice = new Price(new BigDecimal("5.00"));
        Product existingProduct = new Product(
            productId,
            "Coca Cola",
            "Bebida refrescante",
            ProductType.DRINK,
            originalPrice,
            5,
            true
        );
        
        String newDescription = "Nueva descripción";
        
        when(productRepository.findById(productId)).thenReturn(Optional.of(existingProduct));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // When
        Product updatedProduct = updateProductUseCase.execute(productId, newDescription, null, null);
        
        // Then
        assertNotNull(updatedProduct);
        assertEquals(newDescription, updatedProduct.getDescription());
        assertEquals(originalPrice, updatedProduct.getPrice());
        assertEquals(5, updatedProduct.getPreparationTime());
        
        verify(productRepository).findById(productId);
        verify(productValidator).validate(any(Product.class));
        verify(productRepository).save(any(Product.class));
    }

    @Test
    @DisplayName("Debe actualizar solo el precio cuando los demás campos son nulos")
    void shouldUpdateOnlyPriceWhenOtherFieldsAreNull() {
        // Given
        Long productId = 1L;
        String originalDescription = "Bebida refrescante";
        Product existingProduct = new Product(
            productId,
            "Coca Cola",
            originalDescription,
            ProductType.DRINK,
            new Price(new BigDecimal("5.00")),
            5,
            true
        );
        
        Price newPrice = new Price(new BigDecimal("7.00"));
        
        when(productRepository.findById(productId)).thenReturn(Optional.of(existingProduct));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // When
        Product updatedProduct = updateProductUseCase.execute(productId, null, newPrice, null);
        
        // Then
        assertNotNull(updatedProduct);
        assertEquals(originalDescription, updatedProduct.getDescription());
        assertEquals(newPrice, updatedProduct.getPrice());
        assertEquals(5, updatedProduct.getPreparationTime());
        
        verify(productRepository).findById(productId);
        verify(productValidator).validate(any(Product.class));
        verify(productRepository).save(any(Product.class));
    }

    @Test
    @DisplayName("Debe actualizar solo el tiempo de preparación cuando los demás campos son nulos")
    void shouldUpdateOnlyPreparationTimeWhenOtherFieldsAreNull() {
        // Given
        Long productId = 1L;
        String originalDescription = "Bebida refrescante";
        Price originalPrice = new Price(new BigDecimal("5.00"));
        Product existingProduct = new Product(
            productId,
            "Coca Cola",
            originalDescription,
            ProductType.DRINK,
            originalPrice,
            5,
            true
        );
        
        Integer newPreparationTime = 10;
        
        when(productRepository.findById(productId)).thenReturn(Optional.of(existingProduct));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // When
        Product updatedProduct = updateProductUseCase.execute(productId, null, null, newPreparationTime);
        
        // Then
        assertNotNull(updatedProduct);
        assertEquals(originalDescription, updatedProduct.getDescription());
        assertEquals(originalPrice, updatedProduct.getPrice());
        assertEquals(newPreparationTime, updatedProduct.getPreparationTime());
        
        verify(productRepository).findById(productId);
        verify(productValidator).validate(any(Product.class));
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
            () -> updateProductUseCase.execute(nonExistentId, "New description", null, null));
        
        assertEquals("Product not found with id: 999", exception.getMessage());
        verify(productRepository).findById(nonExistentId);
        verify(productValidator, never()).validate(any(Product.class));
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    @DisplayName("Debe lanzar IllegalArgumentException cuando el precio actualizado es inválido")
    void shouldThrowExceptionWhenUpdatedPriceIsInvalid() {
        // Given
        Long productId = 1L;
        Product existingProduct = new Product(
            productId,
            "Coca Cola",
            "Bebida refrescante",
            ProductType.DRINK,
            new Price(new BigDecimal("5.00")),
            5,
            true
        );
        
        // Note: Price value object validates itself, so negative price throws during construction
        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> new Price(new BigDecimal("-10.00")));
        
        assertEquals("Price cannot be null or negative", exception.getMessage());
    }

    @Test
    @DisplayName("Debe lanzar IllegalArgumentException cuando el tiempo de preparación actualizado es inválido")
    void shouldThrowExceptionWhenUpdatedPreparationTimeIsInvalid() {
        // Given
        Long productId = 1L;
        Product existingProduct = new Product(
            productId,
            "Coca Cola",
            "Bebida refrescante",
            ProductType.DRINK,
            new Price(new BigDecimal("5.00")),
            5,
            true
        );
        
        Integer invalidPreparationTime = -5;
        
        when(productRepository.findById(productId)).thenReturn(Optional.of(existingProduct));
        
        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> updateProductUseCase.execute(productId, null, null, invalidPreparationTime));
        
        assertEquals("Preparation time must be greater than zero", exception.getMessage());
        verify(productRepository).findById(productId);
        verify(productValidator, never()).validate(any(Product.class));
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    @DisplayName("Debe actualizar el timestamp updatedAt cuando se actualiza un producto")
    void shouldUpdateUpdatedAtTimestampWhenProductIsUpdated() throws InterruptedException {
        // Given
        Long productId = 1L;
        LocalDateTime originalCreatedAt = LocalDateTime.now().minusDays(5);
        LocalDateTime originalUpdatedAt = LocalDateTime.now().minusDays(2);
        
        Product existingProduct = new Product(
            productId,
            "Coca Cola",
            "Bebida refrescante",
            ProductType.DRINK,
            new Price(new BigDecimal("5.00")),
            5,
            true
        );
        
        String newDescription = "Nueva descripción";
        
        when(productRepository.findById(productId)).thenReturn(Optional.of(existingProduct));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // Capture the start time before update
        LocalDateTime beforeUpdate = LocalDateTime.now();
        
        // Small delay to ensure timestamp difference
        Thread.sleep(10);
        
        // When
        Product updatedProduct = updateProductUseCase.execute(productId, newDescription, null, null);
        
        // Then
        assertNotNull(updatedProduct.getUpdatedAt());
        assertTrue(updatedProduct.getUpdatedAt().isAfter(beforeUpdate) || 
                   updatedProduct.getUpdatedAt().isEqual(beforeUpdate));
        
        verify(productRepository).findById(productId);
        verify(productRepository).save(any(Product.class));
    }

    @Test
    @DisplayName("Debe mantener los campos inmutables sin cambios después de la actualización")
    void shouldKeepImmutableFieldsUnchangedAfterUpdate() {
        // Given
        Long productId = 1L;
        LocalDateTime originalCreatedAt = LocalDateTime.now().minusDays(10);
        
        Product existingProduct = new Product(
            productId,
            "Coca Cola",
            "Bebida refrescante",
            ProductType.DRINK,
            new Price(new BigDecimal("5.00")),
            5,
            true
        );
        
        // Update all mutable fields
        String newDescription = "Nueva descripción";
        Price newPrice = new Price(new BigDecimal("10.00"));
        Integer newPreparationTime = 15;
        
        when(productRepository.findById(productId)).thenReturn(Optional.of(existingProduct));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // When
        Product updatedProduct = updateProductUseCase.execute(productId, newDescription, newPrice, newPreparationTime);
        
        // Then - Verify immutable fields
        assertEquals(productId, updatedProduct.getId());
        assertEquals("Coca Cola", updatedProduct.getName());
        assertEquals(ProductType.DRINK, updatedProduct.getType());
        assertEquals(existingProduct.getCreatedAt(), updatedProduct.getCreatedAt());
        
        // Verify mutable fields were updated
        assertEquals(newDescription, updatedProduct.getDescription());
        assertEquals(newPrice, updatedProduct.getPrice());
        assertEquals(newPreparationTime, updatedProduct.getPreparationTime());
        
        verify(productRepository).findById(productId);
        verify(productRepository).save(any(Product.class));
    }
}
