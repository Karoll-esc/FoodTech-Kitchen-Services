package com.foodtech.kitchen.domain.model;

import java.time.LocalDateTime;

/**
 * Value Object que representa una transición de estado de mesa.
 * 
 * <p>Este objeto inmutable encapsula una transición válida entre dos estados
 * de mesa, registrando el momento exacto en que ocurre la transición. Se utiliza
 * para auditoría y seguimiento del ciclo de vida de las mesas.</p>
 * 
 * <p><strong>Características:</strong></p>
 * <ul>
 *   <li>Inmutable - no puede modificarse después de su creación</li>
 *   <li>Value Object - la identidad está definida por sus valores</li>
 *   <li>Valida la transición en el constructor</li>
 *   <li>Registra automáticamente el timestamp de la transición</li>
 * </ul>
 * 
 * <p><strong>Reglas de negocio:</strong></p>
 * <ul>
 *   <li>Los estados origen y destino no pueden ser null</li>
 *   <li>La transición debe ser válida según {@link TableStatus#canTransitionTo(TableStatus)}</li>
 *   <li>El timestamp se establece automáticamente al momento de la creación</li>
 * </ul>
 * 
 * <p><strong>Uso en HU-007:</strong> Este Value Object se utiliza para representar
 * transiciones de estado en el ciclo de vida manual de las mesas.</p>
 * 
 * <p><strong>Ejemplo de uso:</strong></p>
 * <pre>{@code
 * TableStateTransition transition = new TableStateTransition(
 *     TableStatus.AVAILABLE,
 *     TableStatus.OCCUPIED
 * );
 * 
 * System.out.println("Transición de " + transition.getFromStatus() 
 *     + " a " + transition.getToStatus());
 * System.out.println("Timestamp: " + transition.getTransitionTime());
 * }</pre>
 * 
 * @see TableStatus
 * @see Table
 */
public final class TableStateTransition {
    
    private final TableStatus fromStatus;
    private final TableStatus toStatus;
    private final LocalDateTime transitionTime;
    
    /**
     * Crea una nueva transición de estado validando que sea permitida.
     * 
     * <p>El constructor valida que:</p>
     * <ul>
     *   <li>Ningún estado sea null</li>
     *   <li>La transición sea válida según las reglas de negocio</li>
     * </ul>
     * 
     * <p>El timestamp de la transición se establece automáticamente
     * con {@code LocalDateTime.now()}.</p>
     * 
     * @param fromStatus el estado origen de la transición (no puede ser null)
     * @param toStatus el estado destino de la transición (no puede ser null)
     * @throws IllegalArgumentException si algún estado es null o si la transición no es válida
     */
    public TableStateTransition(TableStatus fromStatus, TableStatus toStatus) {
        if (fromStatus == null || toStatus == null) {
            throw new IllegalArgumentException(
                "States cannot be null. fromStatus=" + fromStatus + ", toStatus=" + toStatus
            );
        }
        if (!fromStatus.canTransitionTo(toStatus)) {
            throw new IllegalArgumentException(
                String.format(
                    "Invalid transition from %s to %s. Valid transitions from %s are: %s",
                    fromStatus,
                    toStatus,
                    fromStatus,
                    fromStatus.getValidTransitions()
                )
            );
        }
        
        this.fromStatus = fromStatus;
        this.toStatus = toStatus;
        this.transitionTime = LocalDateTime.now();
    }
    
    /**
     * Obtiene el estado origen de la transición.
     * 
     * @return el estado desde el cual se transiciona (nunca null)
     */
    public TableStatus getFromStatus() { 
        return fromStatus; 
    }
    
    /**
     * Obtiene el estado destino de la transición.
     * 
     * @return el estado al cual se transiciona (nunca null)
     */
    public TableStatus getToStatus() { 
        return toStatus; 
    }
    
    /**
     * Obtiene el timestamp exacto en que se creó la transición.
     * 
     * @return la fecha y hora de la transición (nunca null)
     */
    public LocalDateTime getTransitionTime() { 
        return transitionTime; 
    }
}
