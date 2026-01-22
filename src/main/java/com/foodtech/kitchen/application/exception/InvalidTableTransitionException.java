package com.foodtech.kitchen.application.exception;

import com.foodtech.kitchen.domain.model.TableStatus;
import java.util.List;

/**
 * Excepción lanzada cuando se intenta realizar una transición de estado inválida en una mesa.
 * 
 * <p>Esta excepción se lanza cuando un usuario intenta cambiar el estado de una mesa
 * de forma que no cumple con las reglas del flujo de ciclo de vida:
 * AVAILABLE → OCCUPIED → SERVED → CLEANING → AVAILABLE</p>
 * 
 * <p><strong>Arquitectura:</strong></p>
 * <ul>
 *   <li>Capa: Application (excepción de caso de uso)</li>
 *   <li>No usa anotaciones de Spring</li>
 *   <li>Se lanza desde {@link com.foodtech.kitchen.application.usecases.UpdateTableStatusUseCase}</li>
 * </ul>
 * 
 * <p><strong>Reglas de transición:</strong></p>
 * <ul>
 *   <li>AVAILABLE solo puede ir a OCCUPIED</li>
 *   <li>OCCUPIED solo puede ir a SERVED</li>
 *   <li>SERVED solo puede ir a CLEANING</li>
 *   <li>CLEANING solo puede ir a AVAILABLE</li>
 * </ul>
 * 
 * <p><strong>Uso en HU-007:</strong> Validación de cambios manuales de estado de mesa.</p>
 * 
 * <p><strong>Ejemplo de uso:</strong></p>
 * <pre>{@code
 * throw new InvalidTableTransitionException(
 *     TableStatus.AVAILABLE,
 *     TableStatus.SERVED,
 *     List.of(TableStatus.OCCUPIED)
 * );
 * }</pre>
 * 
 * @see com.foodtech.kitchen.domain.model.TableStatus
 * @see com.foodtech.kitchen.application.usecases.UpdateTableStatusUseCase
 */
public class InvalidTableTransitionException extends RuntimeException {
    
    private final TableStatus currentStatus;
    private final TableStatus targetStatus;
    private final List<TableStatus> validTransitions;
    
    /**
     * Crea una excepción de transición inválida con contexto completo.
     * 
     * @param currentStatus el estado actual de la mesa (no puede ser null)
     * @param targetStatus el estado objetivo intentado (no puede ser null)
     * @param validTransitions lista de transiciones válidas desde el estado actual (no puede ser null)
     * @throws IllegalArgumentException si algún parámetro es null
     */
    public InvalidTableTransitionException(
        final TableStatus currentStatus,
        final TableStatus targetStatus,
        final List<TableStatus> validTransitions
    ) {
        super(buildMessage(currentStatus, targetStatus, validTransitions));
        validateParameters(currentStatus, targetStatus, validTransitions);
        this.currentStatus = currentStatus;
        this.targetStatus = targetStatus;
        this.validTransitions = List.copyOf(validTransitions);
    }
    
    /**
     * Obtiene el estado actual desde el cual se intentó transicionar.
     * 
     * @return el estado actual (nunca null)
     */
    public TableStatus getCurrentStatus() {
        return currentStatus;
    }
    
    /**
     * Obtiene el estado objetivo al que se intentó transicionar.
     * 
     * @return el estado objetivo (nunca null)
     */
    public TableStatus getTargetStatus() {
        return targetStatus;
    }
    
    /**
     * Obtiene la lista de transiciones válidas desde el estado actual.
     * 
     * @return lista inmutable de estados válidos (nunca null)
     */
    public List<TableStatus> getValidTransitions() {
        return validTransitions;
    }
    
    private static String buildMessage(
        final TableStatus currentStatus,
        final TableStatus targetStatus,
        final List<TableStatus> validTransitions
    ) {
        return String.format(
            "Cannot transition from %s to %s. Valid transitions from %s are: %s",
            currentStatus,
            targetStatus,
            currentStatus,
            validTransitions
        );
    }
    
    private static void validateParameters(
        final TableStatus currentStatus,
        final TableStatus targetStatus,
        final List<TableStatus> validTransitions
    ) {
        if (currentStatus == null) {
            throw new IllegalArgumentException("Current status cannot be null");
        }
        if (targetStatus == null) {
            throw new IllegalArgumentException("Target status cannot be null");
        }
        if (validTransitions == null) {
            throw new IllegalArgumentException("Valid transitions list cannot be null");
        }
    }
}
