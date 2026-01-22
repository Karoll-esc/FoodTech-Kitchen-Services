package com.foodtech.kitchen.application.ports.out;

import com.foodtech.kitchen.domain.model.Table;
import com.foodtech.kitchen.domain.model.TableStatus;
import java.util.List;
import java.util.Optional;

/**
 * Puerto de salida para persistencia de mesas.
 * 
 * <p>Esta interfaz define el contrato que debe implementar
 * el adaptador de persistencia en la capa de infraestructura.</p>
 */
public interface TableRepository {
    
    /**
     * Guarda una nueva mesa o actualiza una existente.
     * 
     * @param table la mesa a guardar
     * @return la mesa guardada con su ID asignado
     */
    Table save(Table table);
    
    /**
     * Busca una mesa por su ID.
     * 
     * @param id el ID de la mesa
     * @return Optional con la mesa si existe
     */
    Optional<Table> findById(Long id);
    
    /**
     * Busca una mesa por su número.
     * 
     * @param tableNumber el número de la mesa
     * @return Optional con la mesa si existe
     */
    Optional<Table> findByTableNumber(String tableNumber);
    
    /**
     * Obtiene todas las mesas registradas.
     * 
     * @return lista de todas las mesas
     */
    List<Table> findAll();
    
    /**
     * Obtiene todas las mesas con un estado específico.
     * 
     * @param status el estado a filtrar
     * @return lista de mesas con ese estado
     */
    List<Table> findByStatus(TableStatus status);
    
    /**
     * Verifica si existe una mesa con el número dado.
     * 
     * @param tableNumber el número a verificar
     * @return true si existe una mesa con ese número
     */
    boolean existsByTableNumber(String tableNumber);
    
    /**
     * Actualiza una mesa existente.
     * 
     * <p>Este método actualiza los campos de una mesa ya persistida.
     * La mesa debe tener un ID no nulo.</p>
     * 
     * @param table la mesa con los datos actualizados (debe tener ID)
     * @return la mesa actualizada
     * @throws IllegalArgumentException si la mesa no tiene ID
     */
    Table update(Table table);
    
    /**
     * Elimina una mesa por su ID.
     * 
     * @param id el ID de la mesa a eliminar
     */
    void deleteById(Long id);
}
