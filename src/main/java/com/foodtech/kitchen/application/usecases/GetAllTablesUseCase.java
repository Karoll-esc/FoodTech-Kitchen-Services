package com.foodtech.kitchen.application.usecases;

import com.foodtech.kitchen.application.ports.out.TableRepository;
import com.foodtech.kitchen.domain.model.Table;
import java.util.List;

/**
 * Caso de uso para obtener todas las mesas del sistema.
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
 *   <li>Coordinar la obtención de todas las mesas del repositorio</li>
 *   <li>Retornar la lista completa sin filtros ni transformaciones</li>
 * </ul>
 * 
 * <p><strong>Casos de uso típicos:</strong></p>
 * <ul>
 *   <li>Panel administrativo para visualizar todas las mesas</li>
 *   <li>Reportes del inventario completo de mesas</li>
 *   <li>Dashboard con estadísticas del restaurante</li>
 * </ul>
 * 
 * <p><strong>Ejemplo de uso:</strong></p>
 * <pre>
 * TableRepository repository = new TableRepositoryAdapter(...);
 * GetAllTablesUseCase useCase = new GetAllTablesUseCase(repository);
 * 
 * List&lt;Table&gt; allTables = useCase.execute();
 * System.out.println("Total de mesas: " + allTables.size());
 * 
 * for (Table table : allTables) {
 *     System.out.printf("Mesa %s - Capacidad: %d - Estado: %s%n",
 *         table.getTableNumber(), 
 *         table.getCapacity(), 
 *         table.getStatus());
 * }
 * </pre>
 * 
 * <p><strong>Nota:</strong> Si necesitas filtrar mesas por estado específico,
 * considera usar un caso de uso especializado como GetTablesByStatusUseCase.</p>
 * 
 * @author FoodTech Kitchen Team
 * @version 1.0
 * @since 2026-01-21
 */
public class GetAllTablesUseCase {
    
    private final TableRepository tableRepository;
    
    /**
     * Construye el caso de uso con su dependencia.
     * 
     * @param tableRepository el repositorio para consulta de mesas (no puede ser null)
     * @throws IllegalArgumentException si tableRepository es null
     */
    public GetAllTablesUseCase(final TableRepository tableRepository) {
        if (tableRepository == null) {
            throw new IllegalArgumentException("TableRepository cannot be null");
        }
        this.tableRepository = tableRepository;
    }
    
    /**
     * Ejecuta el caso de uso para obtener todas las mesas del sistema.
     * 
     * <p>Este método no aplica filtros ni transformaciones, retorna todas las
     * mesas tal como están almacenadas en el repositorio.</p>
     * 
     * @return lista de todas las mesas registradas (nunca null, puede estar vacía)
     */
    public List<Table> execute() {
        return tableRepository.findAll();
    }
}
