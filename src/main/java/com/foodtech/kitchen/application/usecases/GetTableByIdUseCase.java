package com.foodtech.kitchen.application.usecases;

import com.foodtech.kitchen.application.exception.TableNotFoundException;
import com.foodtech.kitchen.application.ports.out.TableRepository;
import com.foodtech.kitchen.domain.model.Table;

/**
 * Caso de uso para obtener una mesa por su ID.
 */
public class GetTableByIdUseCase {
    
    private final TableRepository tableRepository;
    
    public GetTableByIdUseCase(TableRepository tableRepository) {
        this.tableRepository = tableRepository;
    }
    
    /**
     * Ejecuta el caso de uso para buscar una mesa por ID.
     * 
     * @param id el ID de la mesa
     * @return la mesa encontrada
     * @throws TableNotFoundException si no existe la mesa
     */
    public Table execute(Long id) {
        return tableRepository.findById(id)
            .orElseThrow(() -> new TableNotFoundException(id));
    }
}
