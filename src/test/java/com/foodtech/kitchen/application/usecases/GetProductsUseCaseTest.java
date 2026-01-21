package com.foodtech.kitchen.application.usecases;

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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetProductsUseCaseTest {

    @Mock
    private ProductRepository productRepository;

    private GetProductsUseCase getProductsUseCase;

    @BeforeEach
    void setUp() {
        getProductsUseCase = new GetProductsUseCase(productRepository);
    }

    @Test
    @DisplayName("Debe retornar todos los productos cuando no hay filtros")
    void shouldReturnAllProductsWhenNoFiltersApplied() {
        // Given
        Product product1 = createProduct(1L, "Hamburguesa", ProductType.HOT_DISH, true);
        Product product2 = createProduct(2L, "Coca Cola", ProductType.DRINK, true);
        Product product3 = createProduct(3L, "Ensalada", ProductType.COLD_DISH, false);

        List<Product> allProducts = Arrays.asList(product1, product2, product3);
        when(productRepository.findAll()).thenReturn(allProducts);

        // When
        List<Product> result = getProductsUseCase.execute(null, null);

        // Then
        assertNotNull(result);
        assertEquals(3, result.size());
        verify(productRepository, times(1)).findAll();
        verify(productRepository, never()).findByAvailable(anyBoolean());
        verify(productRepository, never()).findByType(any());
    }

    @Test
    @DisplayName("Debe retornar solo productos disponibles cuando se filtra por disponibilidad")
    void shouldReturnOnlyAvailableProductsWhenFilteredByAvailability() {
        // Given
        Product product1 = createProduct(1L, "Hamburguesa", ProductType.HOT_DISH, true);
        Product product2 = createProduct(2L, "Coca Cola", ProductType.DRINK, true);

        List<Product> availableProducts = Arrays.asList(product1, product2);
        when(productRepository.findByAvailable(true)).thenReturn(availableProducts);

        // When
        List<Product> result = getProductsUseCase.execute(true, null);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(Product::isAvailable));
        verify(productRepository, times(1)).findByAvailable(true);
        verify(productRepository, never()).findAll();
        verify(productRepository, never()).findByType(any());
    }

    @Test
    @DisplayName("Debe retornar solo productos no disponibles cuando se filtra por no disponible")
    void shouldReturnOnlyUnavailableProductsWhenFilteredByUnavailable() {
        // Given
        Product product = createProduct(3L, "Ensalada", ProductType.COLD_DISH, false);

        List<Product> unavailableProducts = Collections.singletonList(product);
        when(productRepository.findByAvailable(false)).thenReturn(unavailableProducts);

        // When
        List<Product> result = getProductsUseCase.execute(false, null);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertFalse(result.get(0).isAvailable());
        verify(productRepository, times(1)).findByAvailable(false);
    }

    @Test
    @DisplayName("Debe retornar solo productos de tipo específico cuando se filtra por tipo")
    void shouldReturnOnlyProductsOfSpecificTypeWhenFilteredByType() {
        // Given
        Product product1 = createProduct(1L, "Coca Cola", ProductType.DRINK, true);
        Product product2 = createProduct(2L, "Sprite", ProductType.DRINK, true);

        List<Product> drinks = Arrays.asList(product1, product2);
        when(productRepository.findByType(ProductType.DRINK)).thenReturn(drinks);

        // When
        List<Product> result = getProductsUseCase.execute(null, ProductType.DRINK);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(p -> p.getType() == ProductType.DRINK));
        verify(productRepository, times(1)).findByType(ProductType.DRINK);
        verify(productRepository, never()).findAll();
    }

    @Test
    @DisplayName("Debe retornar productos filtrados por disponibilidad y tipo")
    void shouldReturnProductsFilteredByBothAvailabilityAndType() {
        // Given
        Product product1 = createProduct(1L, "Hamburguesa", ProductType.HOT_DISH, true);
        Product product2 = createProduct(2L, "Pizza", ProductType.HOT_DISH, true);

        List<Product> allHotDishes = Arrays.asList(product1, product2);
        when(productRepository.findByType(ProductType.HOT_DISH)).thenReturn(allHotDishes);

        // When
        List<Product> result = getProductsUseCase.execute(true, ProductType.HOT_DISH);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(p -> p.getType() == ProductType.HOT_DISH && p.isAvailable()));
        verify(productRepository, times(1)).findByType(ProductType.HOT_DISH);
    }

    @Test
    @DisplayName("Debe retornar lista vacía cuando no hay productos que coincidan con los filtros")
    void shouldReturnEmptyListWhenNoProductsMatchFilters() {
        // Given
        when(productRepository.findByType(ProductType.COLD_DISH)).thenReturn(Collections.emptyList());

        // When
        List<Product> result = getProductsUseCase.execute(null, ProductType.COLD_DISH);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(productRepository, times(1)).findByType(ProductType.COLD_DISH);
    }

    @Test
    @DisplayName("Debe retornar lista vacía cuando no hay productos en el catálogo")
    void shouldReturnEmptyListWhenNoProductsInCatalog() {
        // Given
        when(productRepository.findAll()).thenReturn(Collections.emptyList());

        // When
        List<Product> result = getProductsUseCase.execute(null, null);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(productRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Debe lanzar IllegalArgumentException cuando ProductRepository es null en constructor")
    void shouldThrowIllegalArgumentExceptionWhenRepositoryIsNull() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new GetProductsUseCase(null)
        );

        assertEquals("ProductRepository cannot be null", exception.getMessage());
    }

    private Product createProduct(Long id, String name, ProductType type, boolean available) {
        Product product = new Product(
            name,
            "Descripción de " + name,
            type,
            new Price(new BigDecimal("10.00")),
            180
        );
        product.setId(id);
        product.setAvailable(available);
        return product;
    }
}
