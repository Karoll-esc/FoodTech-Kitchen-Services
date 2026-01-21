package com.foodtech.kitchen.domain.services;

import com.foodtech.kitchen.domain.model.Table;

/**
 * Servicio de dominio para validar mesas.
 * 
 * <p>NO contiene dependencias de Spring (puro dominio).</p>
 */
public class TableValidator {
    
    /**
     * Valida que una mesa cumpla con todas las reglas de negocio.
     * 
     * @param table la mesa a validar
     * @throws IllegalArgumentException si la mesa no es válida
     */
    public void validate(Table table) {
        if (table == null) {
            throw new IllegalArgumentException("Table cannot be null");
        }
        
        validateTableNumber(table.getTableNumber());
        validateCapacity(table.getCapacity());
    }
    
    /**
     * Valida que el número de mesa sea válido.
     * 
     * @param tableNumber el número a validar
     * @throws IllegalArgumentException si no es válido
     */
    public void validateTableNumber(String tableNumber) {
        if (tableNumber == null || tableNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Table number cannot be null or empty");
        }
        
        if (tableNumber.length() > 10) {
            throw new IllegalArgumentException("Table number cannot exceed 10 characters");
        }
    }
    
    /**
     * Valida que la capacidad sea válida.
     * 
     * @param capacity la capacidad a validar
     * @throws IllegalArgumentException si no es válida
     */
    public void validateCapacity(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("Table capacity must be greater than zero");
        }
        
        if (capacity > 50) {
            throw new IllegalArgumentException("Table capacity cannot exceed 50 people");
        }
    }
}
