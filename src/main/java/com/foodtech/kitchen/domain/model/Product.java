package com.foodtech.kitchen.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Product {

    private Long id;
    private final String name;
    private final String description;
    private final ProductType type;
    private final Price price;
    private final int preparationTimeSeconds;
    private boolean available;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Product(String name, String description, ProductType type, Price price, int preparationTimeSeconds) {
        validate(name, description, type, price, preparationTimeSeconds);
        this.name = name;
        this.description = description;
        this.type = type;
        this.price = price;
        this.preparationTimeSeconds = preparationTimeSeconds;
        this.available = true;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // Compatibility constructor for existing tests
    public Product(String name, ProductType type) {
        this(name, "", type, new Price(BigDecimal.ZERO), 1);
    }

    private void validate(String name, String description, ProductType type, Price price, int preparationTimeSeconds) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Product name cannot be null or empty");
        }
        if (description == null) {
            throw new IllegalArgumentException("Product description cannot be null");
        }
        if (type == null) {
            throw new IllegalArgumentException("Product type cannot be null");
        }
        if (price == null) {
            throw new IllegalArgumentException("Product price cannot be null");
        }
        if (preparationTimeSeconds <= 0) {
            throw new IllegalArgumentException("Preparation time must be greater than zero");
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public ProductType getType() {
        return type;
    }

    public Price getPrice() {
        return price;
    }

    public int getPreparationTimeSeconds() {
        return preparationTimeSeconds;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void updateTimestamp() {
        this.updatedAt = LocalDateTime.now();
    }
}

