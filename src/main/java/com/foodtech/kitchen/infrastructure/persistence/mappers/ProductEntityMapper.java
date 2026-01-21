package com.foodtech.kitchen.infrastructure.persistence.mappers;

import com.foodtech.kitchen.domain.model.Price;
import com.foodtech.kitchen.domain.model.Product;
import com.foodtech.kitchen.infrastructure.persistence.jpa.entities.ProductEntity;
import com.foodtech.kitchen.infrastructure.persistence.jpa.entities.TaskProductEntity;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class ProductEntityMapper {

    public Product toDomain(ProductEntity entity) {
        if (entity == null) {
            return null;
        }

        // Check if this is a catalog product (has price/description) or order/task product (legacy)
        if (entity.getPrice() == null || entity.getDescription() == null || entity.getPreparationTimeSeconds() == null) {
            // Legacy order/task product - use deprecated constructor
            return new Product(entity.getName(), entity.getType());
        }

        // Catalog product - use full constructor
        Product product = new Product(
            entity.getId(),
            entity.getName(),
            entity.getDescription(),
            entity.getType(),
            new Price(entity.getPrice()),
            entity.getPreparationTimeSeconds(),
            entity.getAvailable() != null ? entity.getAvailable() : true
        );

        return product;
    }

    public ProductEntity toEntity(Product product) {
        if (product == null) {
            return null;
        }

        return ProductEntity.builder()
            .id(product.getId())
            .name(product.getName())
            .description(product.getDescription())
            .type(product.getType())
            .price(product.getPrice().getAmount())
            .preparationTimeSeconds(product.getPreparationTimeSeconds())
            .available(product.isAvailable())
            .createdAt(product.getCreatedAt())
            .updatedAt(product.getUpdatedAt())
            .build();
    }

    // Backward compatibility methods for existing order/task system

    public ProductEntity toProductEntity(Product product) {
        if (product == null) {
            return null;
        }

        return ProductEntity.builder()
            .id(product.getId())
            .name(product.getName())
            .type(product.getType())
            .build();
    }

    public TaskProductEntity toTaskProductEntity(Product product) {
        if (product == null) {
            return null;
        }

        return TaskProductEntity.builder()
            .name(product.getName())
            .type(product.getType())
            .build();
    }

    public Product toDomain(TaskProductEntity entity) {
        if (entity == null) {
            return null;
        }

        return new Product(entity.getName(), entity.getType());
    }
}
