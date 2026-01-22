package com.foodtech.kitchen.application.usecases;

import com.foodtech.kitchen.application.exception.InvalidTableTransitionException;
import com.foodtech.kitchen.application.exception.TableNotFoundException;
import com.foodtech.kitchen.application.exception.TableWithoutActiveOrderException;
import com.foodtech.kitchen.application.ports.in.UpdateTableStatusPort;
import com.foodtech.kitchen.application.ports.out.TableRepository;
import com.foodtech.kitchen.domain.model.Table;
import com.foodtech.kitchen.domain.model.TableStatus;
import com.foodtech.kitchen.domain.services.TableLifecycleValidator;

/**
 * Caso de uso para actualizar el estado de una mesa manualmente.
 * 
 * <p>Este caso de uso implementa la lógica de negocio para cambiar el estado
 * de una mesa en el flujo del ciclo de vida operativo, aplicando todas las
 * validaciones necesarias.</p>
 * 
 * <p><strong>Arquitectura:</strong></p>
 * <ul>
 *   <li>Capa: Application - Use Case</li>
 *   <li>Implementa: {@link UpdateTableStatusPort}</li>
 *   <li>Patrón: Hexagonal Architecture (Application Service)</li>
 * </ul>
 * 
 * <p><strong>Dependencias:</strong></p>
 * <ul>
 *   <li>{@link TableRepository} - Para buscar y actualizar mesas</li>
 *   <li>{@link TableLifecycleValidator} - Para validar reglas de negocio</li>
 * </ul>
 * 
 * <p><strong>Flujo de ejecución:</strong></p>
 * <ol>
 *   <li>{@link #findTableById(Long)} - Buscar mesa por ID (lanza TableNotFoundException si no existe)</li>
 *   <li>{@link #validateStateTransition(Table, TableStatus)} - Validar transición de estado (lanza InvalidTableTransitionException si es inválida)</li>
 *   <li>{@link #applyStatusSpecificRules(Table, TableStatus)} - Aplicar reglas específicas:
 *     <ul>
 *       <li>Si es OCCUPIED, validar que tenga pedido activo (lanza TableWithoutActiveOrderException)</li>
 *       <li>Si es AVAILABLE, desvincular pedido automáticamente</li>
 *     </ul>
 *   </li>
 *   <li>{@link #updateTableStatus(Table, TableStatus)} - Cambiar estado y actualizar lastStateChangeAt</li>
 *   <li>Persistir cambios en repositorio vía {@link TableRepository#update(Table)}</li>
 * </ol>
 * 
 * <p><strong>Uso en HU-007:</strong> Gestión manual del ciclo de vida de mesas.</p>
 * 
 * @see UpdateTableStatusPort
 * @see Table
 * @see TableStatus
 */
public class UpdateTableStatusUseCase implements UpdateTableStatusPort {
    
    private final TableRepository tableRepository;
    private final TableLifecycleValidator lifecycleValidator;
    
    /**
     * Constructor con inyección de dependencias.
     * 
     * @param tableRepository repositorio para persistencia de mesas
     * @param lifecycleValidator validador de reglas de negocio
     */
    public UpdateTableStatusUseCase(
        TableRepository tableRepository,
        TableLifecycleValidator lifecycleValidator
    ) {
        this.tableRepository = tableRepository;
        this.lifecycleValidator = lifecycleValidator;
    }
    
    @Override
    public Table execute(Long tableId, TableStatus newStatus) {
        Table table = findTableById(tableId);
        validateStateTransition(table, newStatus);
        applyStatusSpecificRules(table, newStatus);
        updateTableStatus(table, newStatus);
        return tableRepository.update(table);
    }
    
    /**
     * Busca una mesa por su identificador único.
     * 
     * @param tableId el ID de la mesa a buscar
     * @return la mesa encontrada (nunca null)
     * @throws TableNotFoundException si no existe una mesa con el ID especificado
     */
    private Table findTableById(Long tableId) {
        return tableRepository.findById(tableId)
            .orElseThrow(() -> new TableNotFoundException(tableId));
    }
    
    /**
     * Valida que la transición de estado sea permitida según las reglas del ciclo de vida.
     * 
     * @param table la mesa cuyo estado se validará
     * @param newStatus el nuevo estado al que se desea transicionar
     * @throws InvalidTableTransitionException si la transición no está permitida
     */
    private void validateStateTransition(Table table, TableStatus newStatus) {
        try {
            lifecycleValidator.validateTransition(table.getStatus(), newStatus);
        } catch (IllegalArgumentException e) {
            throw new InvalidTableTransitionException(
                table.getStatus(),
                newStatus,
                table.getStatus().getValidTransitions()
            );
        }
    }
    
    /**
     * Aplica reglas específicas según el estado objetivo.
     * 
     * <p>Reglas aplicadas:</p>
     * <ul>
     *   <li>OCCUPIED: Valida que la mesa tenga un pedido activo</li>
     *   <li>AVAILABLE: Desvincula automáticamente el pedido actual</li>
     * </ul>
     * 
     * @param table la mesa a la que aplicar las reglas
     * @param newStatus el estado objetivo que determina las reglas a aplicar
     * @throws TableWithoutActiveOrderException si se intenta marcar como OCCUPIED sin pedido
     */
    private void applyStatusSpecificRules(Table table, TableStatus newStatus) {
        if (newStatus == TableStatus.OCCUPIED) {
            validateTableHasActiveOrder(table);
        }
        if (newStatus == TableStatus.AVAILABLE) {
            table.clearOrder();
        }
    }
    
    /**
     * Valida que la mesa tenga un pedido activo asignado.
     * 
     * @param table la mesa a validar
     * @throws TableWithoutActiveOrderException si la mesa no tiene un pedido activo (currentOrderId es null)
     */
    private void validateTableHasActiveOrder(Table table) {
        try {
            lifecycleValidator.validateCanBeOccupied(table);
        } catch (IllegalArgumentException e) {
            throw new TableWithoutActiveOrderException(table.getTableNumber());
        }
    }
    
    /**
     * Actualiza el estado de la mesa y su timestamp de última modificación.
     * 
     * @param table la mesa cuyo estado se actualizará
     * @param newStatus el nuevo estado a aplicar
     */
    private void updateTableStatus(Table table, TableStatus newStatus) {
        table.changeStatus(newStatus);
    }
}
