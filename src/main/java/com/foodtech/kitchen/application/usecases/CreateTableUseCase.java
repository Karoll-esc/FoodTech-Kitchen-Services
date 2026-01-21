package com.foodtech.kitchen.application.usecases;

import com.foodtech.kitchen.application.exception.TableAlreadyExistsException;
import com.foodtech.kitchen.application.ports.out.TableRepository;
import com.foodtech.kitchen.domain.model.Table;
import com.foodtech.kitchen.domain.services.TableValidator;

/**
 * Caso de uso para crear una nueva mesa en el sistema.
 * 
 * <p><strong>Responsabilidades:</strong></p>
 * <ul>
 *   <li>Validar los datos de la mesa</li>
 *   <li>Verificar que no exista una mesa con el mismo número</li>
 *   <li>Crear la mesa con estado AVAILABLE</li>
 *   <li>Persistir la mesa</li>
 * </ul>
 * 
 * <p><strong>Precondiciones:</strong></p>
 * <ul>
 *   <li>El número de mesa no debe existir en el sistema</li>
 *   <li>La capacidad debe ser mayor a 0</li>
 * </ul>
 */
public class CreateTableUseCase {
    
    private final TableRepository tableRepository;
    private final TableValidator tableValidator;
    
    public CreateTableUseCase(TableRepository tableRepository, TableValidator tableValidator) {
        this.tableRepository = tableRepository;
        this.tableValidator = tableValidator;
    }
    
    /**
     * Ejecuta el caso de uso de creación de mesa.
     * 
     * @param tableNumber el número identificador de la mesa
     * @param capacity la capacidad de comensales
     * @return la mesa creada con su ID asignado
     * @throws IllegalArgumentException si los datos no son válidos
     * @throws TableAlreadyExistsException si ya existe una mesa con ese número
     */
    public Table execute(String tableNumber, int capacity) {
        tableValidator.validateTableNumber(tableNumber);
        tableValidator.validateCapacity(capacity);
        
        if (tableRepository.existsByTableNumber(tableNumber)) {
            throw new TableAlreadyExistsException(tableNumber);
        }
        
        Table table = new Table(tableNumber, capacity);
        tableValidator.validate(table);
        
        return tableRepository.save(table);
    }
}
