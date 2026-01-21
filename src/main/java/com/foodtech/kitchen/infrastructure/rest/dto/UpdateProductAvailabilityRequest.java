package com.foodtech.kitchen.infrastructure.rest.dto;

/**
 * DTO for updating product availability status.
 * 
 * Used by PATCH /api/products/{id}/availability endpoint.
 * 
 * @param available true to enable product, false to disable
 */
public record UpdateProductAvailabilityRequest(
    Boolean available
) {}
