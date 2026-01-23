package com.foodtech.kitchen.infrastructure.persistence.mappers;

import com.foodtech.kitchen.domain.model.Order;
import com.foodtech.kitchen.domain.model.Price;
import com.foodtech.kitchen.domain.model.Product;

import java.math.BigDecimal;
import com.foodtech.kitchen.infrastructure.persistence.jpa.entities.OrderEntity;
import com.foodtech.kitchen.infrastructure.persistence.jpa.entities.OrderItemEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class OrderEntityMapper {

    public OrderEntity toEntity(Order order) {
        List<OrderItemEntity> items = order.getProducts().stream()
                .map(this::toOrderItemEntity)
                .collect(Collectors.toList());

        return OrderEntity.builder()
                .id(order.getId())
                .tableNumber(order.getTableNumber())
                .items(items)
                .build();
    }

    public Order toDomain(OrderEntity entity) {
        List<Product> products = entity.getItems().stream()
                .map(this::toProductFromOrderItem)
                .collect(Collectors.toList());

        return Order.reconstruct(entity.getId(), entity.getTableNumber(), products);
    }

    private OrderItemEntity toOrderItemEntity(Product product) {
        return OrderItemEntity.builder()
                .productId(product.getId())
                .productName(product.getName())
                .productType(product.getType())
                .priceAtPurchase(product.getPrice() != null ? product.getPrice().getAmount() : null)
                .quantity(1)
                .build();
    }

    private Product toProductFromOrderItem(OrderItemEntity item) {
        return new Product(
                item.getProductId(),
                item.getProductName(),
                "",
                item.getProductType(),
                item.getPriceAtPurchase() != null ? new Price(item.getPriceAtPurchase()) : new Price(BigDecimal.ZERO),
                1,
                true
        );
    }
}