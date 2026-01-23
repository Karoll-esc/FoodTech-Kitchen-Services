package com.foodtech.kitchen.infrastructure.persistence.adapters;

import com.foodtech.kitchen.domain.model.*;
import com.foodtech.kitchen.infrastructure.persistence.jpa.OrderJpaRepository;
import com.foodtech.kitchen.infrastructure.persistence.jpa.entities.OrderEntity;
import com.foodtech.kitchen.infrastructure.persistence.jpa.entities.OrderItemEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class OrderRepositoryAdapterTest {

    private OrderRepositoryAdapter adapter;
    private OrderJpaRepository jpaRepository;

    @BeforeEach
    void setUp() {
        jpaRepository = mock(OrderJpaRepository.class);
        com.foodtech.kitchen.infrastructure.persistence.mappers.OrderEntityMapper mapper =
            new com.foodtech.kitchen.infrastructure.persistence.mappers.OrderEntityMapper();
        adapter = new OrderRepositoryAdapter(jpaRepository, mapper);
    }

    @Test
    @DisplayName("Should save order using JPA repository")
    void shouldSaveOrder() {
        // Given
        Product cocaCola = new Product("Coca Cola", ProductType.DRINK);
        Product pizza = new Product("Pizza", ProductType.PASTRY);
        Order order = new Order("A1", List.of(cocaCola, pizza));

        OrderItemEntity item1 = OrderItemEntity.builder()
            .productName("Coca Cola").productType(ProductType.DRINK).build();
        OrderItemEntity item2 = OrderItemEntity.builder()
            .productName("Pizza").productType(ProductType.PASTRY).build();

        OrderEntity savedEntity = OrderEntity.builder()
            .id(1L)
            .tableNumber("A1")
            .items(List.of(item1, item2))
            .build();

        when(jpaRepository.save(any(OrderEntity.class))).thenReturn(savedEntity);

        // When
        adapter.save(order);

        // Then
        verify(jpaRepository, times(1)).save(any(OrderEntity.class));
    }

    @Test
    @DisplayName("Should convert Order to OrderEntity correctly")
    void shouldConvertOrderToEntity() {
        // Given
        Product product = new Product("Coca Cola", ProductType.DRINK);
        Order order = new Order("B2", List.of(product));

        OrderItemEntity item = OrderItemEntity.builder()
            .productName("Coca Cola").productType(ProductType.DRINK).build();

        OrderEntity savedEntity = OrderEntity.builder()
            .id(2L)
            .tableNumber("B2")
            .items(List.of(item))
            .build();

        when(jpaRepository.save(any(OrderEntity.class))).thenReturn(savedEntity);

        // When
        Order result = adapter.save(order);

        // Then
        assertNotNull(result);
        assertEquals(2L, result.getId());
        assertEquals("B2", result.getTableNumber());
        verify(jpaRepository, times(1)).save(any(OrderEntity.class));
    }

    @Test
    @DisplayName("Should handle order with multiple products")
    void shouldHandleMultipleProducts() {
        // Given
        Product cocaCola = new Product("Coca Cola", ProductType.DRINK);
        Product sprite = new Product("Sprite", ProductType.DRINK);
        Product pizza = new Product("Pizza", ProductType.PASTRY);
        Order order = new Order("C3", List.of(cocaCola, sprite, pizza));

        OrderEntity savedEntity = OrderEntity.builder()
            .id(3L)
            .tableNumber("C3")
            .items(List.of(
                OrderItemEntity.builder()
                    .productName("Coca Cola").productType(ProductType.DRINK).build(),
                OrderItemEntity.builder()
                    .productName("Sprite").productType(ProductType.DRINK).build(),
                OrderItemEntity.builder()
                    .productName("Pizza").productType(ProductType.PASTRY).build()))
            .build();

        when(jpaRepository.save(any(OrderEntity.class))).thenReturn(savedEntity);

        // When
        Order result = adapter.save(order);

        // Then
        assertNotNull(result);
        assertEquals(3L, result.getId());
        assertEquals("C3", result.getTableNumber());
        assertEquals(3, result.getProducts().size());
        verify(jpaRepository, times(1)).save(any(OrderEntity.class));
    }
}