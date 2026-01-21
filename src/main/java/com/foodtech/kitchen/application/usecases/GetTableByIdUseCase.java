package com.foodtech.kitchen.application.usecases;

import com.foodtech.kitchen.application.exception.TableNotFoundException;
import com.foodtech.kitchen.application.ports.out.TableRepository;
import com.foodtech.kitchen.domain.model.Table;

/**
 * Caso de uso para obtener una mesa específica mediante su identificador único.
 * 
 * <p><strong>Arquitectura:</strong></p>
 * <ul>
 *   <li>Capa: Application (caso de uso)</li>
 *   <li>No usa anotaciones de Spring (se instancia desde capa Infrastructure)</li>
 *   <li>Depende del puerto {@link TableRepository} (Inversión de Dependencias)</li>
 *   <li>Caso de uso de solo lectura (query)</li>
 * </ul>
 * 
 * <p><strong>Responsabilidades:</strong></p>
 * <ul>
 *   <li>Validar que el ID proporcionado no sea null</li>
 *   <li>Buscar la mesa en el repositorio usando su ID</li>
 *   <li>Lanzar excepción si la mesa no existe</li>
 * </ul>
 * 
 * <p><strong>Casos de uso típicos:</strong></p>
 * <ul>
 *   <li>Consultar detalles de una mesa específica desde API REST</li>
 *   <li>Obtener mesa antes de cambiar su estado</li>
 *   <li>Verificar existencia de mesa en operaciones de actualización</li>
 * </ul>
 * 
 * <p><strong>Ejemplo de uso:</strong></p>
 * <pre>
 * TableRepository repository = new TableRepositoryAdapter(...);
 * GetTableByIdUseCase useCase = new GetTableByIdUseCase(repository);
 * 
 * try {
 *     Table table = useCase.execute(42L);
 *     System.out.printf("Mesa encontrada: %s (Capacidad: %d, Estado: %s)%n",
 *         table.getTableNumber(), 
 *         table.getCapacity(), 
 *         table.getStatus());
 * } catch (TableNotFoundException e) {
 *     System.err.println("Error: " + e.getMessage());
 * } catch (IllegalArgumentException e) {
 *     System.err.println("ID inválido: " + e.getMessage());
 * }
 * </pre>
 * 
 * @author FoodTech Kitchen Team
 * @version 1.0
 * @since 2026-01-21
 */
public class GetTableByIdUseCase {
    
    private final TableRepository tableRepository;
    
    /**
     * Construye el caso de uso con su dependencia.
     * 
     * @param tableRepository el repositorio para consulta de mesas (no puede ser null)
     * @throws IllegalArgumentException si tableRepository es null
     */
    public GetTableByIdUseCase(final TableRepository tableRepository) {
        if (tableRepository == null) {
            throw new IllegalArgumentException("TableRepository cannot be null");
        }
        this.tableRepository = tableRepository;
    }
    
    /**
     * Ejecuta el caso de uso para buscar una mesa por su identificador único.
     * 
     * <p><strong>Flujo de ejecución:</strong></p>
     * <ol>
     *   <li>Valida que el ID no sea null</li>
     *   <li>Busca la mesa en el repositorio</li>
     *   <li>Si existe, retorna la mesa encontrada</li>
     *   <li>Si no existe, lanza {@link TableNotFoundException}</li>
     * </ol>
     * 
     * @param id el ID único de la mesa a buscar (no puede ser null)
     * @return la mesa encontrada con todos sus datos
     * @throws IllegalArgumentException si el ID es null
     * @throws TableNotFoundException si no existe una mesa con ese ID
     */
    public Table execute(final Long id) {
        validateId(id);
        return findTableOrThrow(id);
    }
    
    private void validateId(final Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Table ID cannot be null");
        }
    }
    
    private Table findTableOrThrow(final Long id) {
        return tableRepository.findById(id)
            .orElseThrow(() -> new TableNotFoundException(id));
    }
}
