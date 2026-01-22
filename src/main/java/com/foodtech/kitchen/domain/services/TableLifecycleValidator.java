package com.foodtech.kitchen.domain.services;

import com.foodtech.kitchen.domain.model.Table;
import com.foodtech.kitchen.domain.model.TableStatus;

/**
 * Validador de reglas de negocio para el ciclo de vida de mesas.
 * 
 * <p>Este servicio de dominio encapsula las validaciones complejas relacionadas
 * con las transiciones de estado de las mesas, asegurando que se cumplan todas
 * las reglas de negocio antes de permitir cambios de estado.</p>
 * 
 * <p><strong>Arquitectura:</strong></p>
 * <ul>
 *   <li>Domain Layer - Domain Service</li>
 *   <li>NO usa anotaciones de Spring (pure Java)</li>
 *   <li>Sin estado (stateless)</li>
 *   <li>Puede ser compartido entre múltiples casos de uso</li>
 * </ul>
 * 
 * <p><strong>Responsabilidades:</strong></p>
 * <ul>
 *   <li>Validar que las transiciones de estado sean permitidas</li>
 *   <li>Validar precondiciones específicas para estados (ej: pedido activo para OCCUPIED)</li>
 *   <li>Proveer mensajes de error descriptivos con contexto de negocio</li>
 * </ul>
 * 
 * <p><strong>Reglas de negocio implementadas:</strong></p>
 * <ol>
 *   <li>Solo se permiten transiciones en el flujo: AVAILABLE → OCCUPIED → SERVED → CLEANING → AVAILABLE</li>
 *   <li>Una mesa solo puede marcarse como OCCUPIED si tiene un pedido activo asociado</li>
 *   <li>No se permiten saltos de estado ni retrocesos (excepto el ciclo completo)</li>
 * </ol>
 * 
 * <p><strong>Uso en HU-007:</strong> Este validador es utilizado por el caso de uso
 * {@code UpdateTableStatusUseCase} para asegurar que los meseros solo realicen
 * cambios de estado válidos.</p>
 * 
 * <p><strong>Ejemplo de uso:</strong></p>
 * <pre>{@code
 * TableLifecycleValidator validator = new TableLifecycleValidator();
 * 
 * // Validar transición
 * validator.validateTransition(TableStatus.AVAILABLE, TableStatus.OCCUPIED);
 * 
 * // Validar que mesa puede ser ocupada
 * Table table = tableRepository.findById(tableId).orElseThrow();
 * validator.validateCanBeOccupied(table);
 * }</pre>
 * 
 * @see Table
 * @see TableStatus
 */
public class TableLifecycleValidator {
    
    /**
     * Valida que la transición de estado sea válida según las reglas de negocio.
     * 
     * <p>Este método verifica que:</p>
     * <ul>
     *   <li>Ambos estados no sean null</li>
     *   <li>La transición esté permitida según {@link TableStatus#canTransitionTo(TableStatus)}</li>
     * </ul>
     * 
     * <p><strong>Mensajes de error:</strong> Los mensajes incluyen el contexto completo
     * de la validación fallida, mostrando los estados involucrados y las transiciones válidas.</p>
     * 
     * @param currentStatus el estado actual de la mesa (no puede ser null)
     * @param targetStatus el estado objetivo al que se desea transicionar (no puede ser null)
     * @throws IllegalArgumentException si algún estado es null o si la transición no es permitida
     */
    public void validateTransition(TableStatus currentStatus, TableStatus targetStatus) {
        if (currentStatus == null || targetStatus == null) {
            throw new IllegalArgumentException(
                String.format(
                    "Status cannot be null. currentStatus=%s, targetStatus=%s",
                    currentStatus,
                    targetStatus
                )
            );
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
     * 
     * <p><strong>Regla de negocio:</strong> Una mesa solo puede marcarse como OCCUPIED
     * si tiene un pedido activo asociado (currentOrderId no es null).</p>
     * 
     * <p>Esta validación asegura que el flujo operativo sea correcto: primero se
     * registra el pedido, y luego se marca la mesa como ocupada.</p>
     * 
     * <p><strong>Nota:</strong> Este método NO valida el estado actual de la mesa,
     * solo verifica que tenga un pedido asociado. La validación del estado debe
     * hacerse con {@link #validateTransition(TableStatus, TableStatus)}.</p>
     * 
     * @param table la mesa a validar (no puede ser null)
     * @throws IllegalArgumentException si la mesa no tiene pedido activo
     * @throws NullPointerException si table es null
     */
    public void validateCanBeOccupied(Table table) {
        if (table == null) {
            throw new NullPointerException("Table cannot be null");
        }
        
        if (table.getCurrentOrderId() == null) {
            throw new IllegalArgumentException(
                String.format(
                    "Table %s cannot be marked as OCCUPIED without an active order. " +
                    "Please assign an order to the table first.",
                    table.getTableNumber()
                )
            );
        }
    }
}
