package com.foodtech.kitchen.infrastructure.rest.dto;

import java.math.BigDecimal;

/**
 * DTO for updating an existing product's details.
 * 
 * Used by PUT /api/products/{id} endpoint.
 * All fields are optional - only provided fields will be updated.
 * Name and type cannot be changed after creation (not included here).
 * 
 * @param description new description (null = no change)
 * @param price new price (null = no change)
 * @param preparationTimeSeconds new preparation time (null = no change)
 */
public record UpdateProductRequest(
    String description,
    BigDecimal price,
    Integer preparationTimeSeconds
) {}
