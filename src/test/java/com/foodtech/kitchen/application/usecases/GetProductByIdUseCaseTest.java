package com.foodtech.kitchen.application.usecases;

import com.foodtech.kitchen.application.exception.ProductNotFoundException;
import com.foodtech.kitchen.application.ports.out.ProductRepository;
import com.foodtech.kitchen.domain.model.Price;
import com.foodtech.kitchen.domain.model.Product;
import com.foodtech.kitchen.domain.model.ProductType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetProductByIdUseCaseTest {

    @Mock
    private ProductRepository productRepository;

    private GetProductByIdUseCase getProductByIdUseCase;

    @BeforeEach
    void setUp() {
        getProductByIdUseCase = new GetProductByIdUseCase(productRepository);
    }

    @Test
    @DisplayName("Debe retornar un producto cuando existe con el ID dado")
    void shouldReturnProductWhenProductExistsWithGivenId() {
        // Given
        Long productId = 1L;
        Product expectedProduct = new Product(
            "Hamburguesa Clásica",
            "Hamburguesa de carne con lechuga y tomate",
            ProductType.PASTRY,
            new Price(new BigDecimal("15.50")),
            300
        );
        expectedProduct.setId(productId);

        when(productRepository.findById(productId)).thenReturn(Optional.of(expectedProduct));

        // When
        Product result = getProductByIdUseCase.execute(productId);

        // Then
        assertNotNull(result);
        assertEquals(productId, result.getId());
        assertEquals("Hamburguesa Clásica", result.getName());
        assertEquals(ProductType.PASTRY, result.getType());
        verify(productRepository, times(1)).findById(productId);
    }

    @Test
    @DisplayName("Debe lanzar ProductNotFoundException cuando el producto no existe")
    void shouldThrowProductNotFoundExceptionWhenProductDoesNotExist() {
        // Given
        Long productId = 999L;
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        // When & Then
        ProductNotFoundException exception = assertThrows(
            ProductNotFoundException.class,
            () -> getProductByIdUseCase.execute(productId)
        );

        assertEquals("Product not found with id: 999", exception.getMessage());
        assertEquals(999L, exception.getProductId());
        verify(productRepository, times(1)).findById(productId);
    }

    @Test
    @DisplayName("Debe lanzar IllegalArgumentException cuando el ID es null")
    void shouldThrowIllegalArgumentExceptionWhenIdIsNull() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> getProductByIdUseCase.execute(null)
        );

        assertEquals("Product ID cannot be null", exception.getMessage());
        verify(productRepository, never()).findById(any());
    }

    @Test
    @DisplayName("Debe retornar producto con todos sus atributos correctamente")
    void shouldReturnProductWithAllAttributesCorrectly() {
        // Given
        Long productId = 5L;
        Product product = new Product(
            "Ensalada César",
            "Ensalada con pollo y aderezo césar",
            ProductType.SANDWICH,
            new Price(new BigDecimal("12.00")),
            180
        );
        product.setId(productId);
        product.setAvailable(false);

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        // When
        Product result = getProductByIdUseCase.execute(productId);

        // Then
        assertEquals(productId, result.getId());
        assertEquals("Ensalada César", result.getName());
        assertEquals("Ensalada con pollo y aderezo césar", result.getDescription());
        assertEquals(ProductType.SANDWICH, result.getType());
        assertEquals(new BigDecimal("12.00"), result.getPrice().getAmount());
        assertEquals(180, result.getPreparationTimeSeconds());
        assertFalse(result.isAvailable());
        assertNotNull(result.getCreatedAt());
        assertNotNull(result.getUpdatedAt());
    }

    @Test
    @DisplayName("Debe lanzar IllegalArgumentException cuando ProductRepository es null en constructor")
    void shouldThrowIllegalArgumentExceptionWhenRepositoryIsNull() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new GetProductByIdUseCase(null)
        );

        assertEquals("ProductRepository cannot be null", exception.getMessage());
    }
}
