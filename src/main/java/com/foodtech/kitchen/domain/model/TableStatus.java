package com.foodtech.kitchen.domain.model;

/**
 * Estados válidos para una mesa en el sistema.
 * 
 * Flujo de transición (para HU-007):
 * AVAILABLE → OCCUPIED → SERVED → CLEANING → AVAILABLE
 * 
 * Para HU-005 solo se usa AVAILABLE como estado inicial.
 */
public enum TableStatus {
    /**
     * Mesa disponible para nuevos clientes
     */
    AVAILABLE,
    
    /**
     * Mesa ocupada con clientes y pedido activo
     */
    OCCUPIED,
    
    /**
     * Mesa servida, esperando que los clientes se retiren
     */
    SERVED,
    
    /**
     * Mesa en proceso de limpieza
     */
    CLEANING;
    
    /**
     * Valida si la transición a otro estado es válida (para HU-007).
     * 
     * @param newStatus el estado destino
     * @return true si la transición es válida
     */
    public boolean canTransitionTo(TableStatus newStatus) {
        return switch (this) {
            case AVAILABLE -> newStatus == OCCUPIED;
            case OCCUPIED -> newStatus == SERVED;
            case SERVED -> newStatus == CLEANING;
            case CLEANING -> newStatus == AVAILABLE;
        };
    }
}
