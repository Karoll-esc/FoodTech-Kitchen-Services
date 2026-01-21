package com.foodtech.kitchen.infrastructure.persistence.mappers;

import com.foodtech.kitchen.domain.model.Table;
import com.foodtech.kitchen.infrastructure.persistence.jpa.entities.TableEntity;

public class TableEntityMapper {
    
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
        entity.setCreatedAt(table.getCreatedAt());
        entity.setUpdatedAt(table.getUpdatedAt());
        
        return entity;
    }
    
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
            entity.getUpdatedAt()
        );
    }
}
