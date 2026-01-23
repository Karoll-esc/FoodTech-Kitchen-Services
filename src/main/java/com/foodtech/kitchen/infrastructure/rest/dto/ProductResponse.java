package com.foodtech.kitchen.infrastructure.rest.dto;

import java.math.BigDecimal;

/**
 * DTO for product responses in REST API.
 * 
 * Used by all product endpoints to return product data.
 * Includes all product fields including timestamps.
 * 
 * @param id product ID
 * @param name product name
 * @param description product description
 * @param type product type (DRINK, PASTRY, SANDWICH)
 * @param price product price
 * @param preparationTimeSeconds preparation time in seconds
 * @param available availability status
 * @param createdAt creation timestamp (ISO-8601 format)
 * @param updatedAt last update timestamp (ISO-8601 format)
 */
public record ProductResponse(
    Long id,
    String name,
    String description,
    String type,
    BigDecimal price,
    Integer preparationTimeSeconds,
    Boolean available,
    String imageUrl,
    String createdAt,
    String updatedAt
) {}
