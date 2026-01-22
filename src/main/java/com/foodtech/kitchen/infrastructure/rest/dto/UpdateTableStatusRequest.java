package com.foodtech.kitchen.infrastructure.rest.dto;

import com.foodtech.kitchen.domain.model.TableStatus;

/**
 * DTO para actualizar el estado de una mesa manualmente.
 * 
 * <p>Este DTO se utiliza en el endpoint PATCH /api/tables/{id}/status
 * para cambiar el estado de una mesa siguiendo el flujo del ciclo de vida
 * operativo (HU-007).</p>
 * 
 * <p><strong>Arquitectura:</strong></p>
 * <ul>
 *   <li>Capa: Infrastructure (REST)</li>
 *   <li>Patrón: Data Transfer Object (DTO)</li>
 *   <li>Validaciones: Manual validation in controller/use case</li>
 * </ul>
 * 
 * <p><strong>Flujo de estados permitidos:</strong></p>
 * <ul>
 *   <li>AVAILABLE → OCCUPIED (requiere pedido activo)</li>
 *   <li>OCCUPIED → SERVED</li>
 *   <li>SERVED → CLEANING</li>
 *   <li>CLEANING → AVAILABLE (limpia pedido automáticamente)</li>
 * </ul>
 * 
 * <p><strong>Uso en HU-007:</strong> Gestión manual del ciclo de vida de mesas
 * por usuarios con permisos update:tables (meseros) o admin:all (administradores).</p>
 * 
 * <p><strong>Ejemplo JSON:</strong></p>
 * <pre>
 * {
 *   "newStatus": "OCCUPIED"
 * }
 * </pre>
 * 
 * @see TableStatus
 * @see com.foodtech.kitchen.infrastructure.rest.TableController
 * @see com.foodtech.kitchen.application.usecases.UpdateTableStatusUseCase
 */
public record UpdateTableStatusRequest(
    
    /**
     * Nuevo estado al que se desea transicionar la mesa.
     * 
     * <p>Debe ser uno de los valores del enum {@link TableStatus}:</p>
     * <ul>
     *   <li>AVAILABLE - Mesa disponible para asignar</li>
     *   <li>OCCUPIED - Mesa ocupada con clientes y pedido activo</li>
     *   <li>SERVED - Mesa con pedido servido, clientes consumiendo</li>
     *   <li>CLEANING - Mesa en proceso de limpieza</li>
     * </ul>
     * 
     * <p><strong>Validaciones:</strong></p>
     * <ul>
     *   <li>No puede ser null (validado en capa aplicación)</li>
     *   <li>Debe ser una transición válida desde el estado actual (validado en capa aplicación)</li>
     * </ul>
     * 
     * @return el nuevo estado de la mesa (puede ser null, se valida en use case)
     */
    TableStatus newStatus
) {}
