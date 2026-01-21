package com.foodtech.kitchen.application.exception;

/**
 * Excepción lanzada cuando no se encuentra una mesa solicitada
 * en el sistema mediante ID o número de mesa.
 * 
 * <p><strong>Arquitectura:</strong></p>
 * <ul>
 *   <li>Capa: Application (excepción de caso de uso)</li>
 *   <li>No usa anotaciones de Spring</li>
 *   <li>Se lanza desde casos de uso como {@link com.foodtech.kitchen.application.usecases.GetTableByIdUseCase}</li>
 * </ul>
 * 
 * <p><strong>Uso:</strong></p>
 * <pre>
 * // Búsqueda por ID
 * throw new TableNotFoundException(42L);
 * 
 * // Búsqueda por número de mesa
 * throw new TableNotFoundException("A1");
 * 
 * // Con mensaje personalizado
 * throw new TableNotFoundException(42L, "Table was deleted during transaction");
 * </pre>
 * 
 * @author FoodTech Kitchen Team
 * @version 1.0
 * @since 2026-01-21
 */
public class TableNotFoundException extends RuntimeException {
    
    private final Long tableId;
    private final String tableNumber;
    
    /**
     * Constructor para búsqueda por ID.
     * 
     * @param id el ID de la mesa no encontrada
     * @throws IllegalArgumentException si id es null
     */
    public TableNotFoundException(final Long id) {
        super(buildMessageForId(id));
        validateId(id);
        this.tableId = id;
        this.tableNumber = null;
    }
    
    /**
     * Constructor para búsqueda por número de mesa.
     * 
     * @param tableNumber el número de mesa no encontrado
     * @throws IllegalArgumentException si tableNumber es null o vacío
     */
    public TableNotFoundException(final String tableNumber) {
        super(buildMessageForTableNumber(tableNumber));
        validateTableNumber(tableNumber);
        this.tableId = null;
        this.tableNumber = tableNumber;
    }
    
    /**
     * Constructor con ID y mensaje personalizado.
     * 
     * @param id el ID de la mesa no encontrada
     * @param customMessage mensaje adicional con contexto
     * @throws IllegalArgumentException si id es null
     */
    public TableNotFoundException(final Long id, final String customMessage) {
        super(buildCustomMessageForId(id, customMessage));
        validateId(id);
        this.tableId = id;
        this.tableNumber = null;
    }
    
    /**
     * Constructor con número de mesa y mensaje personalizado.
     * 
     * @param tableNumber el número de mesa no encontrado
     * @param customMessage mensaje adicional con contexto
     * @throws IllegalArgumentException si tableNumber es null o vacío
     */
    public TableNotFoundException(final String tableNumber, final String customMessage) {
        super(buildCustomMessageForTableNumber(tableNumber, customMessage));
        validateTableNumber(tableNumber);
        this.tableId = null;
        this.tableNumber = tableNumber;
    }
    
    /**
     * Obtiene el ID de la mesa buscada (si la búsqueda fue por ID).
     * 
     * @return el ID de la mesa, o null si la búsqueda fue por número
     */
    public Long getTableId() {
        return tableId;
    }
    
    /**
     * Obtiene el número de mesa buscado (si la búsqueda fue por número).
     * 
     * @return el número de mesa, o null si la búsqueda fue por ID
     */
    public String getTableNumber() {
        return tableNumber;
    }
    
    private static String buildMessageForId(final Long id) {
        return String.format("Table not found with ID: %d", id);
    }
    
    private static String buildMessageForTableNumber(final String tableNumber) {
        return String.format("Table not found with number: '%s'", tableNumber);
    }
    
    private static String buildCustomMessageForId(final Long id, final String customMessage) {
        return String.format("Table not found with ID: %d. %s", id, customMessage);
    }
    
    private static String buildCustomMessageForTableNumber(final String tableNumber, final String customMessage) {
        return String.format("Table not found with number: '%s'. %s", tableNumber, customMessage);
    }
    
    private static void validateId(final Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Table ID cannot be null");
        }
    }
    
    private static void validateTableNumber(final String tableNumber) {
        if (tableNumber == null || tableNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Table number cannot be null or empty");
        }
    }
}
