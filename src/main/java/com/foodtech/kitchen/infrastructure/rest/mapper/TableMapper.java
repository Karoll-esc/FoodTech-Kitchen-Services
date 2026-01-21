package com.foodtech.kitchen.infrastructure.rest.mapper;

import com.foodtech.kitchen.domain.model.Table;
import com.foodtech.kitchen.infrastructure.rest.dto.TableResponse;

/**
 * Mapper for converting domain {@link Table} entities to REST {@link TableResponse} DTOs.
 * 
 * <p><strong>Architecture:</strong></p>
 * <ul>
 *   <li>Layer: Infrastructure (REST)</li>
 *   <li>Pattern: Mapper (Anti-Corruption Layer)</li>
 *   <li>Purpose: Isolates domain from REST API concerns</li>
 * </ul>
 * 
 * <p><strong>Responsibilities:</strong></p>
 * <ul>
 *   <li>Convert domain Table to TableResponse DTO for HTTP responses</li>
 *   <li>Handle null-safe mappings</li>
 *   <li>Ensure API clients receive clean, framework-independent data</li>
 * </ul>
 * 
 * <p><strong>Design Notes:</strong></p>
 * <ul>
 *   <li>Static utility class (no state)</li>
 *   <li>One-way mapping: Domain → DTO only (no DTO → Domain needed)</li>
 *   <li>Null-safe: Returns null if input is null</li>
 *   <li>No business logic: Pure data transformation</li>
 * </ul>
 * 
 * @see Table
 * @see TableResponse
 * @see com.foodtech.kitchen.infrastructure.rest.TableController
 * @author FoodTech Kitchen Team
 * @version 1.0
 * @since 2026-01-21
 */
public class TableMapper {
    
    /**
     * Converts a domain {@link Table} entity to a {@link TableResponse} DTO.
     * 
     * <p>Maps all domain fields to DTO fields for JSON serialization.</p>
     * 
     * @param table the domain Table entity to convert
     * @return the TableResponse DTO, or null if input is null
     */
    public static TableResponse toResponse(Table table) {
        if (table == null) {
            return null;
        }
        
        return new TableResponse(
            table.getId(),
            table.getTableNumber(),
            table.getCapacity(),
            table.getStatus(),
            table.getCurrentOrderId(),
            table.getCreatedAt(),
            table.getUpdatedAt()
        );
    }
}
