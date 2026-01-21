package com.foodtech.kitchen.infrastructure.rest.mapper;

import com.foodtech.kitchen.domain.model.Table;
import com.foodtech.kitchen.infrastructure.rest.dto.TableResponse;

public class TableMapper {
    
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
