package com.foodtech.kitchen.domain.services;

import com.foodtech.kitchen.domain.model.Table;

/**
 * Servicio de dominio para validar reglas de negocio de mesas.
 * 
 * <p>Este servicio centraliza las validaciones de mesas que van más allá
 * de las validaciones básicas del constructor de {@link Table}. Implementa
 * reglas de negocio adicionales como límites de longitud y capacidad.</p>
 * 
 * <p><strong>Arquitectura:</strong></p>
 * <ul>
 *   <li>Domain Layer - Domain Service</li>
 *   <li>Sin dependencias de framework (NO Spring annotations)</li>
 *   <li>Stateless - puede ser reutilizado de forma segura</li>
 *   <li>Implementa Single Responsibility Principle (solo validación)</li>
 * </ul>
 * 
 * <p><strong>Reglas de Validación:</strong></p>
 * <ul>
 *   <li>Mesa no puede ser null</li>
 *   <li>Número de mesa: no null, no vacío, máximo 10 caracteres</li>
 *   <li>Capacidad: mayor a 0, máximo 50 personas</li>
 * </ul>
 * 
 * <p><strong>Uso típico:</strong></p>
 * <pre>{@code
 * TableValidator validator = new TableValidator();
 * Table table = new Table("A1", 4);
 * validator.validate(table); // Valida todas las reglas
 * 
 * // O validación individual
 * validator.validateTableNumber("B3");
 * validator.validateCapacity(6);
 * }</pre>
 * 
 * <p><strong>Nota arquitectónica:</strong> Este servicio es instanciado
 * como @Bean en ApplicationConfig y utilizado por los casos de uso.</p>
 * 
 * @see Table
 */
public class TableValidator {
    
    /**
     * Valida que una mesa cumpla con todas las reglas de negocio.
     * 
     * <p>Este método ejecuta validaciones en cascada sobre todos los
     * campos críticos de la mesa. Si alguna validación falla, lanza
     * una excepción inmediatamente (fail-fast).</p>
     * 
     * <p><strong>Validaciones aplicadas:</strong></p>
     * <ol>
     *   <li>Verifica que la mesa no sea null</li>
     *   <li>Valida el número de mesa con {@link #validateTableNumber(String)}</li>
     *   <li>Valida la capacidad con {@link #validateCapacity(int)}</li>
     * </ol>
     * 
     * @param table la mesa a validar (no puede ser null)
     * @throws IllegalArgumentException si la mesa es null o alguna regla no se cumple
     */
    public void validate(Table table) {
        if (table == null) {
            throw new IllegalArgumentException("Table cannot be null");
        }
        
        validateTableNumber(table.getTableNumber());
        validateCapacity(table.getCapacity());
    }
    
    /**
     * Valida que el número de mesa cumpla con las reglas de negocio.
     * 
     * <p><strong>Reglas aplicadas:</strong></p>
     * <ul>
     *   <li>No puede ser null</li>
     *   <li>No puede estar vacío (después de trim)</li>
     *   <li>No puede exceder 10 caracteres</li>
     * </ul>
     * 
     * <p><strong>Ejemplos válidos:</strong> "A1", "B3", "VIP-1", "TABLE01"</p>
     * <p><strong>Ejemplos inválidos:</strong> null, "", "   ", "TABLEEXTRA123" (>10 chars)</p>
     * 
     * @param tableNumber el número a validar
     * @throws IllegalArgumentException si el número no cumple las reglas
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
     * Valida que la capacidad de la mesa cumpla con las reglas de negocio.
     * 
     * <p><strong>Reglas aplicadas:</strong></p>
     * <ul>
     *   <li>Debe ser mayor a 0 (al menos 1 persona)</li>
     *   <li>No puede exceder 50 personas (límite físico del restaurante)</li>
     * </ul>
     * 
     * <p><strong>Rango válido:</strong> 1 a 50 personas (inclusive)</p>
     * 
     * <p><strong>Justificación del límite:</strong> El límite de 50 personas
     * responde a restricciones físicas del establecimiento y regulaciones
     * de seguridad. Mesas más grandes requieren configuraciones especiales.</p>
     * 
     * @param capacity la capacidad a validar
     * @throws IllegalArgumentException si la capacidad está fuera del rango válido
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
