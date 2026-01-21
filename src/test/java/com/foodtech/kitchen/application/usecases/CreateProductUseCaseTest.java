package com.foodtech.kitchen.application.usecases;

import com.foodtech.kitchen.application.exception.ProductAlreadyExistsException;
import com.foodtech.kitchen.application.ports.out.ProductRepository;
import com.foodtech.kitchen.domain.model.Price;
import com.foodtech.kitchen.domain.model.Product;
import com.foodtech.kitchen.domain.model.ProductType;
import com.foodtech.kitchen.domain.services.ProductValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateProductUseCaseTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductValidator productValidator;

    private CreateProductUseCase createProductUseCase;

    @BeforeEach
    void setUp() {
        createProductUseCase = new CreateProductUseCase(productRepository, productValidator);
    }

    @Test
    @DisplayName("Debe crear un producto exitosamente cuando el nombre no existe")
    void shouldCreateProductSuccessfullyWhenNameDoesNotExist() {
        // Given
        Product inputProduct = new Product(
            "Hamburguesa Clásica",
            "Hamburguesa de carne con lechuga y tomate",
            ProductType.HOT_DISH,
            new Price(new BigDecimal("15.50")),
            300
        );

        Product savedProduct = new Product(
            "Hamburguesa Clásica",
            "Hamburguesa de carne con lechuga y tomate",
            ProductType.HOT_DISH,
            new Price(new BigDecimal("15.50")),
            300
        );
        savedProduct.setId(1L);

        when(productRepository.existsByName(inputProduct.getName())).thenReturn(false);
        when(productRepository.save(inputProduct)).thenReturn(savedProduct);

        // When
        Product result = createProductUseCase.execute(inputProduct);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Hamburguesa Clásica", result.getName());
        verify(productValidator, times(1)).validate(inputProduct);
        verify(productRepository, times(1)).existsByName(inputProduct.getName());
        verify(productRepository, times(1)).save(inputProduct);
    }

    @Test
    @DisplayName("Debe lanzar ProductAlreadyExistsException cuando el nombre ya existe")
    void shouldThrowProductAlreadyExistsExceptionWhenNameExists() {
        // Given
        Product product = new Product(
            "Coca Cola",
            "Bebida gaseosa",
            ProductType.DRINK,
            new Price(new BigDecimal("5.00")),
            30
        );

        when(productRepository.existsByName(product.getName())).thenReturn(true);

        // When & Then
        ProductAlreadyExistsException exception = assertThrows(
            ProductAlreadyExistsException.class,
            () -> createProductUseCase.execute(product)
        );

        assertEquals("Product with name 'Coca Cola' already exists", exception.getMessage());
        assertEquals("Coca Cola", exception.getProductName());
        verify(productValidator, times(1)).validate(product);
        verify(productRepository, times(1)).existsByName(product.getName());
        verify(productRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe validar el producto antes de verificar duplicados")
    void shouldValidateProductBeforeCheckingDuplicates() {
        // Given
        Product product = new Product(
            "Coca Cola",
            "Bebida gaseosa",
            ProductType.DRINK,
            new Price(new BigDecimal("5.00")),
            30
        );

        doThrow(new IllegalArgumentException("Product name cannot be null or empty"))
            .when(productValidator).validate(product);

        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> createProductUseCase.execute(product)
        );

        assertEquals("Product name cannot be null or empty", exception.getMessage());
        verify(productValidator, times(1)).validate(product);
        verify(productRepository, never()).existsByName(anyString());
        verify(productRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe crear producto con available=true por defecto")
    void shouldCreateProductWithAvailableTrueByDefault() {
        // Given
        Product product = new Product(
            "Ensalada César",
            "Ensalada con pollo y aderezo césar",
            ProductType.COLD_DISH,
            new Price(new BigDecimal("12.00")),
            180
        );

        Product savedProduct = new Product(
            "Ensalada César",
            "Ensalada con pollo y aderezo césar",
            ProductType.COLD_DISH,
            new Price(new BigDecimal("12.00")),
            180
        );
        savedProduct.setId(5L);

        when(productRepository.existsByName(product.getName())).thenReturn(false);
        when(productRepository.save(product)).thenReturn(savedProduct);

        // When
        Product result = createProductUseCase.execute(product);

        // Then
        assertTrue(result.isAvailable());
        verify(productRepository, times(1)).save(product);
    }

    @Test
    @DisplayName("Debe lanzar IllegalArgumentException cuando el producto es null")
    void shouldThrowIllegalArgumentExceptionWhenProductIsNull() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> createProductUseCase.execute(null)
        );

        assertEquals("Product cannot be null", exception.getMessage());
        verify(productValidator, never()).validate(any());
        verify(productRepository, never()).existsByName(anyString());
        verify(productRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe lanzar IllegalArgumentException cuando ProductRepository es null en constructor")
    void shouldThrowIllegalArgumentExceptionWhenRepositoryIsNull() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new CreateProductUseCase(null, productValidator)
        );

        assertEquals("ProductRepository cannot be null", exception.getMessage());
    }

    @Test
    @DisplayName("Debe lanzar IllegalArgumentException cuando ProductValidator es null en constructor")
    void shouldThrowIllegalArgumentExceptionWhenValidatorIsNull() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new CreateProductUseCase(productRepository, null)
        );

        assertEquals("ProductValidator cannot be null", exception.getMessage());
    }
}
