package com.foodtech.kitchen.application.ports.in;

import com.foodtech.kitchen.application.exception.InvalidTableTransitionException;
import com.foodtech.kitchen.application.exception.TableNotFoundException;
import com.foodtech.kitchen.application.exception.TableWithoutActiveOrderException;
import com.foodtech.kitchen.domain.model.Table;
import com.foodtech.kitchen.domain.model.TableStatus;

/**
 * Puerto de entrada para actualizar el estado de una mesa manualmente.
 * 
 * <p>Este puerto define la interfaz del caso de uso para cambiar el estado
 * de una mesa en el flujo del ciclo de vida operativo. Es utilizado por
 * los adaptadores de infraestructura (controllers) para invocar la lógica
 * de negocio.</p>
 * 
 * <p><strong>Arquitectura:</strong></p>
 * <ul>
 *   <li>Capa: Application - Input Port</li>
 *   <li>Patrón: Hexagonal Architecture (Port)</li>
 *   <li>Implementado por: {@link com.foodtech.kitchen.application.usecases.UpdateTableStatusUseCase}</li>
 *   <li>Usado por: TableController (REST adapter)</li>
 * </ul>
 * 
 * <p><strong>Flujo de transiciones válidas:</strong></p>
 * <pre>
 * AVAILABLE → OCCUPIED (requiere pedido activo)
 * OCCUPIED → SERVED
 * SERVED → CLEANING
 * CLEANING → AVAILABLE (desvincula pedido)
 * </pre>
 * 
 * <p><strong>Reglas de negocio aplicadas:</strong></p>
 * <ol>
 *   <li>La transición debe ser válida según el flujo del ciclo de vida</li>
 *   <li>Para marcar como OCCUPIED, la mesa debe tener un pedido activo</li>
 *   <li>Al regresar a AVAILABLE, se desvincula el pedido automáticamente</li>
 *   <li>Solo usuarios con rol WAITER o ADMIN pueden ejecutar esta operación</li>
 * </ol>
 * 
 * <p><strong>Uso en HU-007:</strong> Gestión manual del ciclo de vida de mesas
 * por parte del personal de servicio.</p>
 * 
 * <p><strong>Ejemplo de uso:</strong></p>
 * <pre>{@code
 * // Inyectar el port en el controller
 * private final UpdateTableStatusPort updateTableStatusPort;
 * 
 * // Ejecutar cambio de estado
 * Table updatedTable = updateTableStatusPort.execute(tableId, TableStatus.OCCUPIED);
 * }</pre>
 * 
 * @see Table
 * @see TableStatus
 * @see com.foodtech.kitchen.application.usecases.UpdateTableStatusUseCase
 */
public interface UpdateTableStatusPort {
    
    /**
     * Actualiza el estado de una mesa validando las reglas de negocio.
     * 
     * <p>Este método ejecuta el caso de uso completo:</p>
     * <ol>
     *   <li>Busca la mesa por ID</li>
     *   <li>Valida que la transición de estado sea permitida</li>
     *   <li>Si es OCCUPIED, valida que tenga pedido activo</li>
     *   <li>Si es AVAILABLE, desvincula el pedido</li>
     *   <li>Cambia el estado y actualiza lastStateChangeAt</li>
     *   <li>Persiste los cambios</li>
     * </ol>
     * 
     * <p><strong>Precondiciones:</strong></p>
     * <ul>
     *   <li>tableId no puede ser null</li>
     *   <li>newStatus no puede ser null</li>
     *   <li>La mesa debe existir en el sistema</li>
     *   <li>El usuario debe tener permisos (WAITER o ADMIN)</li>
     * </ul>
     * 
     * <p><strong>Postcondiciones:</strong></p>
     * <ul>
     *   <li>El estado de la mesa se actualiza</li>
     *   <li>El campo lastStateChangeAt se actualiza con timestamp actual</li>
     *   <li>Si newStatus es AVAILABLE, currentOrderId se establece a null</li>
     *   <li>Los cambios se persisten en la base de datos</li>
     * </ul>
     * 
     * @param tableId el ID de la mesa a actualizar (no puede ser null)
     * @param newStatus el nuevo estado objetivo (no puede ser null)
     * @return la mesa actualizada con el nuevo estado
     * @throws TableNotFoundException si la mesa con el ID especificado no existe
     * @throws InvalidTableTransitionException si la transición de estado no es válida
     * @throws TableWithoutActiveOrderException si se intenta marcar como OCCUPIED sin pedido activo
     * @throws IllegalArgumentException si tableId o newStatus son null
     */
    Table execute(Long tableId, TableStatus newStatus);
}
