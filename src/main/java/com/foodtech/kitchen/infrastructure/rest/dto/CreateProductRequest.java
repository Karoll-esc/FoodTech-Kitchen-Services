package com.foodtech.kitchen.infrastructure.rest.dto;

import java.math.BigDecimal;

/**
 * DTO for creating a new product in the catalog.
 * 
 * Used by POST /api/products endpoint.
 * All fields are required for product creation.
 * 
 * @param name product name (unique, not blank)
 * @param description product description (not null, can be empty)
 * @param type product type (DRINK, PASTRY, SANDWICH)
 * @param price product price (>= 0, 2 decimals)
 * @param preparationTimeSeconds preparation time in seconds (> 0)
 */
public record CreateProductRequest(
    String name,
    String description,
    String type,
    BigDecimal price,
    Integer preparationTimeSeconds
) {}
