package com.foodtech.kitchen.domain.model;

import java.time.LocalDateTime;

/**
 * Entity que representa una mesa física del restaurante.
 * 
 * <p>Una mesa tiene un identificador único (número), una capacidad
 * de comensales y un estado que determina su disponibilidad.</p>
 * 
 * <p><strong>Arquitectura:</strong> Domain Layer - Entity (sin dependencias de framework)</p>
 * 
 * <p><strong>Reglas de Negocio:</strong></p>
 * <ul>
 *   <li>El número de mesa es obligatorio y único en el sistema</li>
 *   <li>La capacidad debe ser un número entero mayor a 0</li>
 *   <li>Toda mesa nueva inicia con estado AVAILABLE</li>
 *   <li>Al crearse, no tiene pedido asociado (currentOrderId es null)</li>
 *   <li>Los timestamps se gestionan automáticamente</li>
 * </ul>
 * 
 * <p><strong>Campos Inmutables:</strong></p>
 * <ul>
 *   <li>tableNumber - identificador único, no se puede cambiar</li>
 *   <li>capacity - no se modifica después de creación</li>
 *   <li>createdAt - establecido en construcción</li>
 * </ul>
 * 
 * <p><strong>Campos Mutables (para HU-007):</strong></p>
 * <ul>
 *   <li>status - cambia según el flujo operativo</li>
 *   <li>currentOrderId - se asigna al recibir un pedido</li>
 *   <li>updatedAt - se actualiza con cada cambio de estado</li>
 * </ul>
 */
public class Table {
    
    private Long id;
    private final String tableNumber;
    private final int capacity;
    private TableStatus status;
    private Long currentOrderId;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    /**
     * Crea una nueva mesa con número y capacidad especificados.
     * 
     * <p>La mesa se crea con:</p>
     * <ul>
     *   <li>status = AVAILABLE (por defecto)</li>
     *   <li>currentOrderId = null</li>
     *   <li>createdAt = LocalDateTime.now()</li>
     *   <li>updatedAt = LocalDateTime.now()</li>
     * </ul>
     * 
     * @param tableNumber identificador único de la mesa (ej: "A1", "B3")
     * @param capacity número de comensales que puede albergar (> 0)
     * @throws IllegalArgumentException si los parámetros no son válidos
     */
    public Table(String tableNumber, int capacity) {
        validateTableNumber(tableNumber);
        validateCapacity(capacity);
        
        this.tableNumber = tableNumber;
        this.capacity = capacity;
        this.status = TableStatus.AVAILABLE;
        this.currentOrderId = null;
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }
    
    /**
     * Constructor para reconstruir una mesa desde la base de datos.
     * 
     * @param id el ID de la mesa
     * @param tableNumber el número de la mesa
     * @param capacity la capacidad de comensales
     * @param status el estado actual
     * @param currentOrderId el ID del pedido actual (puede ser null)
     * @param createdAt fecha de creación
     * @param updatedAt fecha de última actualización
     */
    public Table(Long id, String tableNumber, int capacity, TableStatus status, 
                 Long currentOrderId, LocalDateTime createdAt, LocalDateTime updatedAt) {
        validateTableNumber(tableNumber);
        validateCapacity(capacity);
        
        this.id = id;
        this.tableNumber = tableNumber;
        this.capacity = capacity;
        this.status = status != null ? status : TableStatus.AVAILABLE;
        this.currentOrderId = currentOrderId;
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
        this.updatedAt = updatedAt != null ? updatedAt : LocalDateTime.now();
    }
    
    private void validateTableNumber(String tableNumber) {
        if (tableNumber == null || tableNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Table number cannot be null or empty");
        }
    }
    
    private void validateCapacity(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("Table capacity must be greater than zero");
        }
    }
    
    /**
     * Cambia el estado de la mesa (para HU-007).
     * 
     * @param newStatus el nuevo estado
     * @throws IllegalStateException si la transición no es válida
     */
    public void changeStatus(TableStatus newStatus) {
        if (!this.status.canTransitionTo(newStatus)) {
            throw new IllegalStateException(
                String.format("Cannot transition from %s to %s", this.status, newStatus)
            );
        }
        this.status = newStatus;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Asigna un pedido a la mesa (para HU-007).
     * 
     * @param orderId el ID del pedido
     */
    public void assignOrder(Long orderId) {
        if (this.status != TableStatus.AVAILABLE) {
            throw new IllegalStateException("Cannot assign order to non-available table");
        }
        this.currentOrderId = orderId;
        this.status = TableStatus.OCCUPIED;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Limpia el pedido asociado (para HU-007).
     */
    public void clearOrder() {
        this.currentOrderId = null;
        this.updatedAt = LocalDateTime.now();
    }
    
    // Getters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTableNumber() { return tableNumber; }
    public int getCapacity() { return capacity; }
    public TableStatus getStatus() { return status; }
    public Long getCurrentOrderId() { return currentOrderId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
