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
 *   <li>Buscar mesa por ID (lanza TableNotFoundException si no existe)</li>
 *   <li>Validar transición de estado (lanza InvalidTableTransitionException si es inválida)</li>
 *   <li>Si es OCCUPIED, validar que tenga pedido activo (lanza TableWithoutActiveOrderException)</li>
 *   <li>Si es AVAILABLE, desvincular pedido automáticamente</li>
 *   <li>Cambiar estado y actualizar lastStateChangeAt</li>
 *   <li>Persistir cambios en repositorio</li>
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
        // 1. Buscar mesa
        Table table = tableRepository.findById(tableId)
            .orElseThrow(() -> new TableNotFoundException(tableId));
        
        // 2. Validar transición
        try {
            lifecycleValidator.validateTransition(table.getStatus(), newStatus);
        } catch (IllegalArgumentException e) {
            throw new InvalidTableTransitionException(
                table.getStatus(),
                newStatus,
                table.getStatus().getValidTransitions()
            );
        }
        
        // 3. Validación especial para OCCUPIED: requiere pedido activo
        if (newStatus == TableStatus.OCCUPIED) {
            try {
                lifecycleValidator.validateCanBeOccupied(table);
            } catch (IllegalArgumentException e) {
                throw new TableWithoutActiveOrderException(table.getTableNumber());
            }
        }
        
        // 4. Lógica especial para AVAILABLE: desvincular pedido
        if (newStatus == TableStatus.AVAILABLE) {
            table.clearOrder();
        }
        
        // 5. Cambiar estado
        table.changeStatus(newStatus);
        
        // 6. Persistir
        tableRepository.update(table);
        
        return table;
    }
}
