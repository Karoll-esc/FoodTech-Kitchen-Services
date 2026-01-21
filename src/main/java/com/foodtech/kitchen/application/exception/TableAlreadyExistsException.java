package com.foodtech.kitchen.application.exception;

/**
 * Excepción lanzada cuando se intenta crear una mesa con un número
 * que ya existe en el sistema.
 */
public class TableAlreadyExistsException extends RuntimeException {
    
    public TableAlreadyExistsException(String tableNumber) {
        super(String.format("Table with number '%s' already exists", tableNumber));
    }
}
