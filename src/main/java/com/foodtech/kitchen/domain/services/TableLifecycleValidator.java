package com.foodtech.kitchen.domain.services;

import com.foodtech.kitchen.domain.model.Table;
import com.foodtech.kitchen.domain.model.TableStatus;

/**
 * Validador de reglas de negocio para el ciclo de vida de mesas.
 * NO usa anotaciones de Spring.
 */
public class TableLifecycleValidator {
    
    /**
     * Valida que la transición de estado sea válida.
     * 
     * @throws IllegalArgumentException si la transición no es permitida
     */
    public void validateTransition(TableStatus currentStatus, TableStatus targetStatus) {
        if (currentStatus == null || targetStatus == null) {
            throw new IllegalArgumentException("Status cannot be null");
        }
        
        if (!currentStatus.canTransitionTo(targetStatus)) {
            throw new IllegalArgumentException(
                String.format(
                    "Cannot transition from %s to %s. Valid transitions from %s are: %s",
                    currentStatus,
                    targetStatus,
                    currentStatus,
                    currentStatus.getValidTransitions()
                )
            );
        }
    }
    
    /**
     * Valida que la mesa pueda ser marcada como ocupada.
     * Requiere que tenga al menos un pedido activo.
     * 
     * @throws IllegalArgumentException si no tiene pedido activo
     */
    public void validateCanBeOccupied(Table table) {
        if (table.getCurrentOrderId() == null) {
            throw new IllegalArgumentException(
                String.format(
                    "Table %s cannot be marked as OCCUPIED without an active order",
                    table.getTableNumber()
                )
            );
        }
    }
}
