package com.foodtech.kitchen.domain.model;

import java.time.LocalDateTime;

/**
 * Entity que representa una mesa física del restaurante.
 * 
 * <p>Una mesa tiene un identificador único (número), una capacidad
 * de comensales y un estado que determina su disponibilidad. Es el
 * aggregate root para la gestión del ciclo de vida de las mesas.</p>
 * 
 * <p><strong>Arquitectura:</strong></p>
 * <ul>
 *   <li>Domain Layer - Aggregate Root Entity</li>
 *   <li>Sin dependencias de framework (pure Java)</li>
 *   <li>Implementa Single Responsibility Principle (gestión de mesa)</li>
 *   <li>Encapsula reglas de negocio y validaciones</li>
 * </ul>
 * 
 * <p><strong>Reglas de Negocio:</strong></p>
 * <ul>
 *   <li>El número de mesa es obligatorio y único en el sistema</li>
 *   <li>La capacidad debe ser un número entero mayor a 0</li>
 *   <li>Toda mesa nueva inicia con estado AVAILABLE</li>
 *   <li>Al crearse, no tiene pedido asociado (currentOrderId es null)</li>
 *   <li>Los timestamps se gestionan automáticamente</li>
 *   <li>Solo mesas AVAILABLE pueden recibir nuevos pedidos</li>
 *   <li>Las transiciones de estado deben seguir el flujo válido</li>
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
 * 
 * <p><strong>Ejemplo de uso:</strong></p>
 * <pre>{@code
 * // Crear nueva mesa
 * Table table = new Table("A1", 4);
 * 
 * // Asignar pedido (cambia a OCCUPIED automáticamente)
 * table.assignOrder(123L);
 * 
 * // Cambiar estado manualmente
 * table.changeStatus(TableStatus.SERVED);
 * }</pre>
 * 
 * @see TableStatus
 * @see TableValidator
 */
public class Table {
    
    private Long id;
    private final String tableNumber;
    private final int capacity;
    private TableStatus status;
    private Long currentOrderId;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime lastStateChangeAt;
    
    /**
     * Crea una nueva mesa con número y capacidad especificados.
     * 
     * <p>La mesa se crea con estado inicial AVAILABLE, sin pedido asociado,
     * y con timestamps automáticos para auditoría.</p>
     * 
     * <p><strong>Estado inicial:</strong></p>
     * <ul>
     *   <li>status = AVAILABLE (por defecto)</li>
     *   <li>currentOrderId = null (sin pedido)</li>
     *   <li>createdAt = LocalDateTime.now()</li>
     *   <li>updatedAt = LocalDateTime.now()</li>
     * </ul>
     * 
     * <p><strong>Validaciones aplicadas:</strong></p>
     * <ul>
     *   <li>tableNumber no puede ser null ni vacío</li>
     *   <li>capacity debe ser mayor a 0</li>
     * </ul>
     * 
     * @param tableNumber identificador único de la mesa (ej: "A1", "B3", "VIP-1")
     * @param capacity número de comensales que puede albergar (debe ser > 0)
     * @throws IllegalArgumentException si tableNumber es null/vacío o capacity es <= 0
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
        this.lastStateChangeAt = now;
    }
    
    /**
     * Constructor para reconstruir una mesa desde la base de datos.
     * 
     * <p>Este constructor es utilizado por el adaptador de persistencia
     * para reconstruir entidades de dominio desde registros de base de datos.</p>
     * 
     * <p><strong>Valores por defecto si son null:</strong></p>
     * <ul>
     *   <li>status: AVAILABLE</li>
     *   <li>createdAt: LocalDateTime.now()</li>
     *   <li>updatedAt: LocalDateTime.now()</li>
     *   <li>lastStateChangeAt: createdAt</li>
     * </ul>
     * 
     * @param id el ID de la mesa (generado por la base de datos)
     * @param tableNumber el número de la mesa
     * @param capacity la capacidad de comensales
     * @param status el estado actual (puede ser null, se usa AVAILABLE por defecto)
     * @param currentOrderId el ID del pedido actual (puede ser null)
     * @param createdAt fecha de creación (puede ser null, se usa now() por defecto)
     * @param updatedAt fecha de última actualización (puede ser null, se usa now() por defecto)
     * @throws IllegalArgumentException si tableNumber es null/vacío o capacity es <= 0
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
        this.lastStateChangeAt = this.createdAt;
    }
    
    /**
     * Valida que el número de mesa sea válido.
     * 
     * <p>Un número de mesa válido debe cumplir:</p>
     * <ul>
     *   <li>No ser null</li>
     *   <li>No estar vacío (después de trim)</li>
     * </ul>
     * 
     * @param tableNumber el número a validar
     * @throws IllegalArgumentException si el número no es válido
     */
    private void validateTableNumber(String tableNumber) {
        if (tableNumber == null || tableNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Table number cannot be null or empty");
        }
    }
    
    /**
     * Valida que la capacidad sea válida.
     * 
     * <p>Una capacidad válida debe ser mayor a 0 (al menos 1 persona).</p>
     * 
     * @param capacity la capacidad a validar
     * @throws IllegalArgumentException si la capacidad no es válida
     */
    private void validateCapacity(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("Table capacity must be greater than zero");
        }
    }
    
    /**
     * Cambia el estado de la mesa validando que la transición sea válida.
     * 
     * <p>Este método implementa el flujo de estados definido en {@link TableStatus}.
     * Solo se permiten transiciones válidas según las reglas de negocio.</p>
     * 
     * <p><strong>Efectos secundarios:</strong></p>
     * <ul>
     *   <li>Actualiza el campo status al nuevo estado</li>
     *   <li>Actualiza updatedAt con LocalDateTime.now()</li>
     *   <li>Actualiza lastStateChangeAt con LocalDateTime.now()</li>
     * </ul>
     * 
     * <p><strong>Uso en HU-007:</strong> Este método será utilizado para gestionar
     * el ciclo de vida operativo de las mesas (servicio, limpieza, etc.).</p>
     * 
     * @param newStatus el nuevo estado al que se desea cambiar
     * @throws IllegalStateException si la transición no es válida según las reglas de negocio
     * @throws IllegalArgumentException si newStatus es null (delegado a TableStatus)
     * @see TableStatus#canTransitionTo(TableStatus)
     */
    public void changeStatus(TableStatus newStatus) {
        if (!this.status.canTransitionTo(newStatus)) {
            throw new IllegalStateException(
                String.format("Cannot transition from %s to %s", this.status, newStatus)
            );
        }
        this.status = newStatus;
        LocalDateTime now = LocalDateTime.now();
        this.updatedAt = now;
        this.lastStateChangeAt = now;
    }
    
    /**
     * Asigna un pedido a la mesa y cambia su estado a OCCUPIED automáticamente.
     * 
     * <p>Esta operación representa el momento en que un cliente llega a la mesa
     * y realiza un pedido. Solo se puede asignar un pedido a una mesa AVAILABLE.</p>
     * 
     * <p><strong>Efectos secundarios:</strong></p>
     * <ul>
     *   <li>Establece currentOrderId con el ID del pedido</li>
     *   <li>Cambia status a OCCUPIED</li>
     *   <li>Actualiza updatedAt con LocalDateTime.now()</li>
     * </ul>
     * 
     * <p><strong>Uso en HU-007:</strong> Integración con el sistema de pedidos
     * para vincular una orden con una mesa específica.</p>
     * 
     * @param orderId el ID del pedido a asignar (no puede ser null)
     * @throws IllegalStateException si la mesa no está en estado AVAILABLE
     * @throws IllegalArgumentException si orderId es null
     */
    public void assignOrder(Long orderId) {
        if (orderId == null) {
            throw new IllegalArgumentException("Order ID cannot be null");
        }
        if (this.status != TableStatus.AVAILABLE) {
            throw new IllegalStateException(
                String.format("Cannot assign order to non-available table. Current status: %s", this.status)
            );
        }
        this.currentOrderId = orderId;
        this.status = TableStatus.OCCUPIED;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Limpia el pedido asociado a la mesa.
     * 
     * <p>Esta operación representa la desvinculación del pedido de la mesa,
     * típicamente cuando el ciclo del pedido ha finalizado.</p>
     * 
     * <p><strong>Efectos secundarios:</strong></p>
     * <ul>
     *   <li>Establece currentOrderId a null</li>
     *   <li>Actualiza updatedAt con LocalDateTime.now()</li>
     *   <li>NO cambia el estado automáticamente (debe hacerse manualmente)</li>
     * </ul>
     * 
     * <p><strong>Nota:</strong> Esta operación no valida el estado actual.
     * Es responsabilidad del caso de uso asegurar que se llama en el momento correcto.</p>
     * 
     * <p><strong>Uso en HU-007:</strong> Limpiar la referencia al pedido cuando
     * la mesa pasa a CLEANING después de que los clientes se retiran.</p>
     */
    public void clearOrder() {
        this.currentOrderId = null;
        this.updatedAt = LocalDateTime.now();
    }
    
    // Getters con documentación
    
    /**
     * Obtiene el ID de la mesa.
     * 
     * @return el ID de la mesa, o null si aún no ha sido persistida
     */
    public Long getId() { return id; }
    
    /**
     * Establece el ID de la mesa.
     * 
     * <p>Este método es utilizado por el adaptador de persistencia después
     * de insertar la mesa en la base de datos.</p>
     * 
     * @param id el ID generado por la base de datos
     */
    public void setId(Long id) { this.id = id; }
    
    /**
     * Obtiene el número identificador único de la mesa.
     * 
     * @return el número de la mesa (nunca null)
     */
    public String getTableNumber() { return tableNumber; }
    
    /**
     * Obtiene la capacidad de comensales de la mesa.
     * 
     * @return la capacidad en número de personas (siempre > 0)
     */
    public int getCapacity() { return capacity; }
    
    /**
     * Obtiene el estado actual de la mesa.
     * 
     * @return el estado actual (nunca null)
     * @see TableStatus
     */
    public TableStatus getStatus() { return status; }
    
    /**
     * Obtiene el ID del pedido actualmente asociado a la mesa.
     * 
     * @return el ID del pedido, o null si no hay pedido asociado
     */
    public Long getCurrentOrderId() { return currentOrderId; }
    
    /**
     * Obtiene la fecha y hora de creación de la mesa.
     * 
     * @return la fecha de creación (nunca null)
     */
    public LocalDateTime getCreatedAt() { return createdAt; }
    
    /**
     * Obtiene la fecha y hora de la última actualización de la mesa.
     * 
     * @return la fecha de última actualización (nunca null)
     */
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    
    /**
     * Obtiene la fecha y hora del último cambio de estado de la mesa.
     * 
     * @return la fecha del último cambio de estado (nunca null)
     */
    public LocalDateTime getLastStateChangeAt() { return lastStateChangeAt; }
}
