package com.foodtech.kitchen.application.usecases;

import com.foodtech.kitchen.application.exception.TableAlreadyExistsException;
import com.foodtech.kitchen.application.ports.out.TableRepository;
import com.foodtech.kitchen.domain.model.Table;
import com.foodtech.kitchen.domain.services.TableValidator;

/**
 * Caso de uso para crear una nueva mesa en el sistema.
 * 
 * <p><strong>Arquitectura:</strong></p>
 * <ul>
 *   <li>Capa: Application (caso de uso)</li>
 *   <li>No usa anotaciones de Spring (se instancia desde capa Infrastructure)</li>
 *   <li>Depende de puertos (interfaces): {@link TableRepository}, {@link TableValidator}</li>
 *   <li>Sigue el principio de Inversión de Dependencias (DIP)</li>
 * </ul>
 * 
 * <p><strong>Responsabilidades:</strong></p>
 * <ul>
 *   <li>Validar los datos de entrada (tableNumber, capacity)</li>
 *   <li>Verificar que no exista una mesa con el mismo número</li>
 *   <li>Crear la mesa con estado inicial AVAILABLE</li>
 *   <li>Persistir la mesa usando el repositorio</li>
 * </ul>
 * 
 * <p><strong>Reglas de negocio aplicadas:</strong></p>
 * <ul>
 *   <li>El número de mesa debe ser único en el sistema</li>
 *   <li>El número de mesa no puede tener más de 10 caracteres</li>
 *   <li>La capacidad debe estar entre 1 y 50 comensales</li>
 *   <li>Toda mesa nueva se crea en estado AVAILABLE</li>
 * </ul>
 * 
 * <p><strong>Uso típico:</strong></p>
 * <pre>
 * TableRepository repository = new TableRepositoryAdapter(...);
 * TableValidator validator = new TableValidator();
 * CreateTableUseCase useCase = new CreateTableUseCase(repository, validator);
 * 
 * try {
 *     Table table = useCase.execute("A1", 4);
 *     System.out.println("Mesa creada con ID: " + table.getId());
 * } catch (TableAlreadyExistsException e) {
 *     System.err.println("Error: " + e.getMessage());
 * } catch (IllegalArgumentException e) {
 *     System.err.println("Validación fallida: " + e.getMessage());
 * }
 * </pre>
 * 
 * @author FoodTech Kitchen Team
 * @version 1.0
 * @since 2026-01-21
 */
public class CreateTableUseCase {
    
    private final TableRepository tableRepository;
    private final TableValidator tableValidator;
    
    /**
     * Construye el caso de uso con sus dependencias.
     * 
     * @param tableRepository el repositorio para persistencia de mesas (no puede ser null)
     * @param tableValidator el validador de dominio (no puede ser null)
     * @throws IllegalArgumentException si alguna dependencia es null
     */
    public CreateTableUseCase(final TableRepository tableRepository, final TableValidator tableValidator) {
        if (tableRepository == null) {
            throw new IllegalArgumentException("TableRepository cannot be null");
        }
        if (tableValidator == null) {
            throw new IllegalArgumentException("TableValidator cannot be null");
        }
        this.tableRepository = tableRepository;
        this.tableValidator = tableValidator;
    }
    
    /**
     * Ejecuta el caso de uso de creación de mesa.
     * 
     * <p><strong>Flujo de ejecución:</strong></p>
     * <ol>
     *   <li>Valida el formato del número de mesa (max 10 caracteres)</li>
     *   <li>Valida la capacidad (1-50 comensales)</li>
     *   <li>Verifica que no exista una mesa con ese número</li>
     *   <li>Crea la instancia de Table con estado AVAILABLE</li>
     *   <li>Valida la mesa completa usando TableValidator</li>
     *   <li>Persiste la mesa usando el repositorio</li>
     *   <li>Retorna la mesa con su ID asignado</li>
     * </ol>
     * 
     * @param tableNumber el número identificador de la mesa (no puede ser null ni vacío, max 10 caracteres)
     * @param capacity la capacidad de comensales (debe estar entre 1 y 50)
     * @return la mesa creada con su ID asignado y estado AVAILABLE
     * @throws IllegalArgumentException si tableNumber es null/vacío, excede 10 caracteres, 
     *         o si capacity no está en el rango 1-50
     * @throws TableAlreadyExistsException si ya existe una mesa con ese número
     */
    public Table execute(final String tableNumber, final int capacity) {
        validateInputParameters(tableNumber, capacity);
        
        tableValidator.validateTableNumber(tableNumber);
        tableValidator.validateCapacity(capacity);
        
        verifyTableNumberDoesNotExist(tableNumber);
        
        Table table = createNewTable(tableNumber, capacity);
        tableValidator.validate(table);
        
        return tableRepository.save(table);
    }
    
    private void validateInputParameters(final String tableNumber, final int capacity) {
        if (tableNumber == null || tableNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Table number cannot be null or empty");
        }
    }
    
    private void verifyTableNumberDoesNotExist(final String tableNumber) {
        if (tableRepository.existsByTableNumber(tableNumber)) {
            throw new TableAlreadyExistsException(tableNumber);
        }
    }
    
    private Table createNewTable(final String tableNumber, final int capacity) {
        return new Table(tableNumber, capacity);
    }
}
