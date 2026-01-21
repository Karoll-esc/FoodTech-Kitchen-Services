package com.foodtech.kitchen.application.usecases;

import com.foodtech.kitchen.application.ports.out.TableRepository;
import com.foodtech.kitchen.domain.model.Table;
import java.util.List;

/**
 * Caso de uso para obtener todas las mesas del sistema.
 * 
 * <p>Este caso de uso está destinado principalmente para uso administrativo,
 * permitiendo visualizar el inventario completo de mesas.</p>
 */
public class GetAllTablesUseCase {
    
    private final TableRepository tableRepository;
    
    public GetAllTablesUseCase(TableRepository tableRepository) {
        this.tableRepository = tableRepository;
    }
    
    /**
     * Ejecuta el caso de uso para obtener todas las mesas.
     * 
     * @return lista de todas las mesas registradas
     */
    public List<Table> execute() {
        return tableRepository.findAll();
    }
}
