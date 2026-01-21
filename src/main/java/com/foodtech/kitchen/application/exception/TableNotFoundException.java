package com.foodtech.kitchen.application.exception;

/**
 * Excepción lanzada cuando no se encuentra una mesa solicitada.
 */
public class TableNotFoundException extends RuntimeException {
    
    public TableNotFoundException(Long id) {
        super(String.format("Table not found with id: %d", id));
    }
    
    public TableNotFoundException(String tableNumber) {
        super(String.format("Table not found with number: %s", tableNumber));
    }
}
