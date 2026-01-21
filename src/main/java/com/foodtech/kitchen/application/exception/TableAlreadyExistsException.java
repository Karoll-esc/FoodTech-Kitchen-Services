package com.foodtech.kitchen.application.exception;

/**
 * Excepción lanzada cuando se intenta crear una mesa con un número
 * que ya existe en el sistema.
 * 
 * <p><strong>Arquitectura:</strong></p>
 * <ul>
 *   <li>Capa: Application (excepción de caso de uso)</li>
 *   <li>No usa anotaciones de Spring</li>
 *   <li>Se lanza desde {@link com.foodtech.kitchen.application.usecases.CreateTableUseCase}</li>
 * </ul>
 * 
 * <p><strong>Uso:</strong></p>
 * <pre>
 * // Con solo el número de mesa
 * throw new TableAlreadyExistsException("A1");
 * 
 * // Con ID de la mesa existente para debugging
 * throw new TableAlreadyExistsException("A1", 42L);
 * 
 * // Con mensaje personalizado
 * throw new TableAlreadyExistsException("A1", "Custom reason");
 * </pre>
 * 
 * @author FoodTech Kitchen Team
 * @version 1.0
 * @since 2026-01-21
 */
public class TableAlreadyExistsException extends RuntimeException {
    
    private final String tableNumber;
    private final Long existingTableId;
    
    /**
     * Constructor básico con solo el número de mesa.
     * 
     * @param tableNumber el número de mesa que ya existe
     * @throws IllegalArgumentException si tableNumber es null o vacío
     */
    public TableAlreadyExistsException(final String tableNumber) {
        super(buildDefaultMessage(tableNumber));
        validateTableNumber(tableNumber);
        this.tableNumber = tableNumber;
        this.existingTableId = null;
    }
    
    /**
     * Constructor con número de mesa e ID de la mesa existente.
     * Útil para debugging y logging detallado.
     * 
     * @param tableNumber el número de mesa que ya existe
     * @param existingTableId el ID de la mesa existente en el sistema
     * @throws IllegalArgumentException si tableNumber es null o vacío
     */
    public TableAlreadyExistsException(final String tableNumber, final Long existingTableId) {
        super(buildDetailedMessage(tableNumber, existingTableId));
        validateTableNumber(tableNumber);
        this.tableNumber = tableNumber;
        this.existingTableId = existingTableId;
    }
    
    /**
     * Constructor con mensaje personalizado.
     * 
     * @param tableNumber el número de mesa que ya existe
     * @param customMessage mensaje personalizado adicional
     * @throws IllegalArgumentException si tableNumber es null o vacío
     */
    public TableAlreadyExistsException(final String tableNumber, final String customMessage) {
        super(buildCustomMessage(tableNumber, customMessage));
        validateTableNumber(tableNumber);
        this.tableNumber = tableNumber;
        this.existingTableId = null;
    }
    
    /**
     * Obtiene el número de mesa que causó el conflicto.
     * 
     * @return el número de mesa
     */
    public String getTableNumber() {
        return tableNumber;
    }
    
    /**
     * Obtiene el ID de la mesa existente (si está disponible).
     * 
     * @return el ID de la mesa existente, o null si no fue proporcionado
     */
    public Long getExistingTableId() {
        return existingTableId;
    }
    
    private static String buildDefaultMessage(final String tableNumber) {
        return String.format(
            "Cannot create table '%s': number already exists in system", 
            tableNumber
        );
    }
    
    private static String buildDetailedMessage(final String tableNumber, final Long existingTableId) {
        return String.format(
            "Cannot create table '%s': number already exists in system (existing table ID: %d)", 
            tableNumber, 
            existingTableId
        );
    }
    
    private static String buildCustomMessage(final String tableNumber, final String customMessage) {
        return String.format(
            "Cannot create table '%s': %s", 
            tableNumber, 
            customMessage
        );
    }
    
    private static void validateTableNumber(final String tableNumber) {
        if (tableNumber == null || tableNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Table number cannot be null or empty");
        }
    }
}
