package com.foodtech.kitchen.infrastructure.persistence.mappers;

import com.foodtech.kitchen.domain.model.Table;
import com.foodtech.kitchen.infrastructure.persistence.jpa.entities.TableEntity;

/**
 * Mapper for bidirectional conversion between {@link Table} domain entity
 * and {@link TableEntity} JPA persistence entity.
 * 
 * <p><strong>Architecture:</strong></p>
 * <ul>
 *   <li>Layer: Infrastructure (Persistence)</li>
 *   <li>Pattern: Mapper (Anti-Corruption Layer)</li>
 *   <li>Purpose: Isolates domain from JPA framework concerns</li>
 * </ul>
 * 
 * <p><strong>Responsibilities:</strong></p>
 * <ul>
 *   <li>Convert domain Table to JPA TableEntity for persistence</li>
 *   <li>Convert JPA TableEntity to domain Table after retrieval</li>
 *   <li>Handle null-safe mappings</li>
 *   <li>Preserve all entity state during conversion</li>
 * </ul>
 * 
 * <p><strong>Design Notes:</strong></p>
 * <ul>
 *   <li>Stateless: Can be registered as Spring singleton bean</li>
 *   <li>Null-safe: Returns null if input is null</li>
 *   <li>No business logic: Pure data transformation</li>
 *   <li>Bidirectional: Supports both directions of conversion</li>
 * </ul>
 * 
 * <p><strong>Usage Example:</strong></p>
 * <pre>
 * // Domain to JPA (for saving)
 * Table domainTable = new Table("A1", 4);
 * TableEntity entity = mapper.toEntity(domainTable);
 * tableJpaRepository.save(entity);
 * 
 * // JPA to Domain (after retrieval)
 * TableEntity entity = tableJpaRepository.findById(1L).orElseThrow();
 * Table domainTable = mapper.toDomain(entity);
 * </pre>
 * 
 * @see Table
 * @see TableEntity
 * @see com.foodtech.kitchen.infrastructure.persistence.adapters.TableRepositoryAdapter
 * @author FoodTech Kitchen Team
 * @version 1.0
 * @since 2026-01-21
 */
public class TableEntityMapper {
    
    /**
     * Converts a domain {@link Table} entity to a JPA {@link TableEntity}.
     * 
     * <p>Maps all fields from domain to JPA entity, including:</p>
     * <ul>
     *   <li>id (may be null for new tables)</li>
     *   <li>tableNumber (unique identifier)</li>
     *   <li>capacity (number of diners)</li>
     *   <li>status (operational state)</li>
     *   <li>currentOrderId (nullable, for occupied tables)</li>
     *   <li>lastStateChangeAt (timestamp of last status change)</li>
     *   <li>createdAt, updatedAt (audit timestamps)</li>
     * </ul>
     * 
     * @param table the domain Table entity to convert
     * @return the JPA TableEntity, or null if input is null
     */
    public TableEntity toEntity(Table table) {
        if (table == null) {
            return null;
        }
        
        TableEntity entity = new TableEntity();
        entity.setId(table.getId());
        entity.setTableNumber(table.getTableNumber());
        entity.setCapacity(table.getCapacity());
        entity.setStatus(table.getStatus());
        entity.setCurrentOrderId(table.getCurrentOrderId());
        entity.setLastStateChangeAt(table.getLastStateChangeAt());
        entity.setCreatedAt(table.getCreatedAt());
        entity.setUpdatedAt(table.getUpdatedAt());
        
        return entity;
    }
    
    /**
     * Converts a JPA {@link TableEntity} to a domain {@link Table} entity.
     * 
     * <p>Maps all fields from JPA entity to domain, preserving:</p>
     * <ul>
     *   <li>id (database primary key)</li>
     *   <li>tableNumber (unique identifier)</li>
     *   <li>capacity (number of diners)</li>
     *   <li>status (operational state)</li>
     *   <li>currentOrderId (nullable, for occupied tables)</li>
     *   <li>lastStateChangeAt (timestamp of last status change)</li>
     *   <li>createdAt, updatedAt (audit timestamps)</li>
     * </ul>
     * 
     * @param entity the JPA TableEntity to convert
     * @return the domain Table entity, or null if input is null
     */
    public Table toDomain(TableEntity entity) {
        if (entity == null) {
            return null;
        }
        
        return new Table(
            entity.getId(),
            entity.getTableNumber(),
            entity.getCapacity(),
            entity.getStatus(),
            entity.getCurrentOrderId(),
            entity.getCreatedAt(),
            entity.getUpdatedAt(),
            entity.getLastStateChangeAt()
        );
    }
}
