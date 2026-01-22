package com.foodtech.kitchen.application.exception;

/**
 * Excepción lanzada cuando se intenta marcar una mesa como OCCUPIED sin tener un pedido activo.
 * 
 * <p>Esta excepción se lanza cuando un mesero intenta cambiar el estado de una mesa
 * a OCCUPIED pero la mesa no tiene ningún pedido asociado (currentOrderId es null).</p>
 * 
 * <p><strong>Arquitectura:</strong></p>
 * <ul>
 *   <li>Capa: Application (excepción de caso de uso)</li>
 *   <li>No usa anotaciones de Spring</li>
 *   <li>Se lanza desde {@link com.foodtech.kitchen.application.usecases.UpdateTableStatusUseCase}</li>
 * </ul>
 * 
 * <p><strong>Regla de negocio:</strong> Una mesa solo puede marcarse como OCCUPIED
 * si previamente se le ha asignado un pedido activo. Esto asegura que el flujo
 * operativo sea: 1) Cliente llega, 2) Se registra pedido, 3) Se marca mesa como ocupada.</p>
 * 
 * <p><strong>Uso en HU-007:</strong> Validación al intentar ocupar una mesa manualmente.</p>
 * 
 * <p><strong>Ejemplo de uso:</strong></p>
 * <pre>{@code
 * if (table.getCurrentOrderId() == null) {
 *     throw new TableWithoutActiveOrderException("A1");
 * }
 * }</pre>
 * 
 * @see com.foodtech.kitchen.domain.model.Table
 * @see com.foodtech.kitchen.application.usecases.UpdateTableStatusUseCase
 */
public class TableWithoutActiveOrderException extends RuntimeException {
    
    private final String tableNumber;
    
    /**
     * Crea una excepción indicando que la mesa no tiene pedido activo.
     * 
     * @param tableNumber el número identificador de la mesa (no puede ser null o vacío)
     * @throws IllegalArgumentException si tableNumber es null o vacío
     */
    public TableWithoutActiveOrderException(final String tableNumber) {
        super(buildMessage(tableNumber));
        validateTableNumber(tableNumber);
        this.tableNumber = tableNumber;
    }
    
    /**
     * Obtiene el número de la mesa que no tiene pedido activo.
     * 
     * @return el número de mesa (nunca null)
     */
    public String getTableNumber() {
        return tableNumber;
    }
    
    private static String buildMessage(final String tableNumber) {
        return String.format(
            "Table %s cannot be marked as OCCUPIED without an active order. " +
            "Please assign an order to the table before marking it as occupied.",
            tableNumber
        );
    }
    
    private static void validateTableNumber(final String tableNumber) {
        if (tableNumber == null || tableNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Table number cannot be null or empty");
        }
    }
}
