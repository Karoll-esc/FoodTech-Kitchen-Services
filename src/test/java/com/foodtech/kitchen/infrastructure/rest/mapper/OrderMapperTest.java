package com.foodtech.kitchen.infrastructure.rest.mapper;

import com.foodtech.kitchen.domain.model.Order;
import com.foodtech.kitchen.domain.model.ProductType;
import com.foodtech.kitchen.infrastructure.rest.dto.CreateOrderRequest;
import com.foodtech.kitchen.infrastructure.rest.dto.ProductRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OrderMapperTest {

    @Test
    @DisplayName("Should map CreateOrderRequest to Order domain")
    void shouldMapRequestToOrder() {
        // Given
        CreateOrderRequest request = new CreateOrderRequest(
            "A1",
            List.of(
                new ProductRequest("Coca Cola", "DRINK"),
                new ProductRequest("Pizza", "PASTRY")
            )
        );

        // When
        Order order = OrderMapper.toDomain(request);

        // Then
        assertEquals("A1", order.getTableNumber());
        assertEquals(2, order.getProducts().size());
        assertEquals("Coca Cola", order.getProducts().get(0).getName());
        assertEquals(ProductType.DRINK, order.getProducts().get(0).getType());
    }

    @Test
    @DisplayName("Should handle single product")
    void shouldHandleSingleProduct() {
        // Given
        CreateOrderRequest request = new CreateOrderRequest(
            "B2",
            List.of(
                new ProductRequest("Sprite", "DRINK")
            )
        );

        // When
        Order order = OrderMapper.toDomain(request);

        // Then
        assertEquals(1, order.getProducts().size());
        assertEquals("Sprite", order.getProducts().get(0).getName());
    }

    @Test
    @DisplayName("Should throw exception for invalid product type")
    void shouldThrowExceptionForInvalidProductType() {
        // Given
        CreateOrderRequest request = new CreateOrderRequest(
            "C3",
            List.of(
                new ProductRequest("Invalid", "INVALID_TYPE")
            )
        );

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> OrderMapper.toDomain(request));
    }
}