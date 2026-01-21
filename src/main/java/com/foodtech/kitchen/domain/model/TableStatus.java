package com.foodtech.kitchen.domain.model;

/**
 * Estados válidos para una mesa en el sistema.
 * 
 * <p>Este enum representa el ciclo de vida completo de una mesa en el restaurante,
 * desde que está disponible hasta que vuelve a estar lista para nuevos clientes.</p>
 * 
 * <p><strong>Flujo de transición (para HU-007):</strong></p>
 * <pre>
 * AVAILABLE → OCCUPIED → SERVED → CLEANING → AVAILABLE
 * </pre>
 * 
 * <p><strong>Contexto arquitectónico:</strong></p>
 * <ul>
 *   <li>Domain Layer - Value Object (sin dependencias de framework)</li>
 *   <li>Implementa Open/Closed Principle mediante comportamiento en el enum</li>
 *   <li>Para HU-005 solo se usa AVAILABLE como estado inicial</li>
 * </ul>
 * 
 * <p><strong>Ejemplo de uso:</strong></p>
 * <pre>{@code
 * TableStatus status = TableStatus.AVAILABLE;
 * if (status.canTransitionTo(TableStatus.OCCUPIED)) {
 *     // Permitir ocupar la mesa
 * }
 * }</pre>
 * 
 * @see Table
 */
public enum TableStatus {
    /**
     * Mesa disponible para nuevos clientes.
     * 
     * <p>La mesa está limpia y lista para ser asignada a nuevos comensales.
     * Puede transicionar únicamente a {@link #OCCUPIED}.</p>
     */
    AVAILABLE,
    
    /**
     * Mesa ocupada con clientes y pedido activo.
     * 
     * <p>Hay clientes sentados y se ha registrado un pedido activo.
     * Puede transicionar únicamente a {@link #SERVED}.</p>
     */
    OCCUPIED,
    
    /**
     * Mesa servida, esperando que los clientes se retiren.
     * 
     * <p>El pedido ha sido completado y servido, pero los clientes aún no se han ido.
     * Puede transicionar únicamente a {@link #CLEANING}.</p>
     */
    SERVED,
    
    /**
     * Mesa en proceso de limpieza.
     * 
     * <p>Los clientes se han retirado y el personal está limpiando la mesa.
     * Puede transicionar únicamente a {@link #AVAILABLE}.</p>
     */
    CLEANING;
    
    /**
     * Valida si la transición a otro estado es válida según las reglas de negocio.
     * 
     * <p>Las transiciones válidas siguen el flujo lineal del ciclo de vida de la mesa.
     * No se permiten saltos de estado ni retrocesos excepto el cierre del ciclo.</p>
     * 
     * <p><strong>Transiciones válidas:</strong></p>
     * <ul>
     *   <li>AVAILABLE → OCCUPIED (cliente llega y ordena)</li>
     *   <li>OCCUPIED → SERVED (pedido completado)</li>
     *   <li>SERVED → CLEANING (cliente se retira)</li>
     *   <li>CLEANING → AVAILABLE (limpieza completada)</li>
     * </ul>
     * 
     * @param newStatus el estado destino al que se desea transicionar
     * @return {@code true} si la transición es válida, {@code false} en caso contrario
     * @throws IllegalArgumentException si newStatus es null
     */
    public boolean canTransitionTo(TableStatus newStatus) {
        if (newStatus == null) {
            throw new IllegalArgumentException("New status cannot be null");
        }
        
        return switch (this) {
            case AVAILABLE -> newStatus == OCCUPIED;
            case OCCUPIED -> newStatus == SERVED;
            case SERVED -> newStatus == CLEANING;
            case CLEANING -> newStatus == AVAILABLE;
        };
    }
}
