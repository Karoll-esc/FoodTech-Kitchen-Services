package com.foodtech.kitchen.domain.model;

import java.time.LocalDateTime;

/**
 * Value Object que representa una transición de estado de mesa.
 * Inmutable.
 */
public final class TableStateTransition {
    private final TableStatus fromStatus;
    private final TableStatus toStatus;
    private final LocalDateTime transitionTime;
    
    public TableStateTransition(TableStatus fromStatus, TableStatus toStatus) {
        if (fromStatus == null || toStatus == null) {
            throw new IllegalArgumentException("States cannot be null");
        }
        if (!fromStatus.canTransitionTo(toStatus)) {
            throw new IllegalArgumentException(
                String.format("Invalid transition from %s to %s", fromStatus, toStatus)
            );
        }
        
        this.fromStatus = fromStatus;
        this.toStatus = toStatus;
        this.transitionTime = LocalDateTime.now();
    }
    
    public TableStatus getFromStatus() { 
        return fromStatus; 
    }
    
    public TableStatus getToStatus() { 
        return toStatus; 
    }
    
    public LocalDateTime getTransitionTime() { 
        return transitionTime; 
    }
}
