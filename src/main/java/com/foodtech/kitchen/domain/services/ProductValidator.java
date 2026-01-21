package com.foodtech.kitchen.domain.services;

import com.foodtech.kitchen.domain.model.Price;
import com.foodtech.kitchen.domain.model.Product;
import com.foodtech.kitchen.domain.model.ProductType;

public class ProductValidator {

    public void validate(Product product) {
        if (product == null) {
            throw new IllegalArgumentException("Product cannot be null");
        }
        validateName(product.getName());
        validateDescription(product.getDescription());
        validateType(product.getType());
        validatePrice(product.getPrice());
        validatePreparationTime(product.getPreparationTimeSeconds());
    }

    public void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Product name cannot be null or empty");
        }
        if (name.length() > 100) {
            throw new IllegalArgumentException("Product name cannot exceed 100 characters");
        }
    }

    public void validateDescription(String description) {
        if (description == null) {
            throw new IllegalArgumentException("Product description cannot be null");
        }
        if (description.length() > 500) {
            throw new IllegalArgumentException("Product description cannot exceed 500 characters");
        }
    }

    public void validateType(ProductType type) {
        if (type == null) {
            throw new IllegalArgumentException("Product type cannot be null");
        }
    }

    public void validatePrice(Price price) {
        if (price == null) {
            throw new IllegalArgumentException("Product price cannot be null");
        }
    }

    public void validatePreparationTime(int preparationTimeSeconds) {
        if (preparationTimeSeconds <= 0) {
            throw new IllegalArgumentException("Preparation time must be greater than zero");
        }
    }
}
