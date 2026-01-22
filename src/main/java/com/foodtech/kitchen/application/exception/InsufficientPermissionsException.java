package com.foodtech.kitchen.application.exception;

/**
 * Excepción lanzada cuando un usuario sin permisos suficientes intenta realizar una operación restringida.
 * 
 * <p>Esta excepción se lanza cuando un usuario intenta ejecutar una operación
 * que requiere roles específicos (ej: WAITER, ADMIN) pero el usuario actual
 * no posee dichos roles.</p>
 * 
 * <p><strong>Arquitectura:</strong></p>
 * <ul>
 *   <li>Capa: Application (excepción de caso de uso)</li>
 *   <li>No usa anotaciones de Spring</li>
 *   <li>Se lanza desde casos de uso que requieren autorización</li>
 * </ul>
 * 
 * <p><strong>Uso en HU-007:</strong> Validación de que solo usuarios con permisos
 * update:tables o admin:all pueden cambiar el estado de las mesas. Personal de cocina
 * (KITCHEN_BAR, KITCHEN_HOT, KITCHEN_COLD) no puede modificar estados de mesa.</p>
 * 
 * <p><strong>Ejemplo de uso:</strong></p>
 * <pre>{@code
 * if (!user.hasAnyRole("update:tables", "admin:all")) {
 *     throw new InsufficientPermissionsException("update table status");
 * }
 * }</pre>
 * 
 * @see com.foodtech.kitchen.application.usecases.UpdateTableStatusUseCase
 */
public class InsufficientPermissionsException extends RuntimeException {
    
    private final String operation;
    
    /**
     * Crea una excepción de permisos insuficientes para una operación específica.
     * 
     * @param operation descripción de la operación que requiere permisos (no puede ser null o vacía)
     * @throws IllegalArgumentException si operation es null o vacía
     */
    public InsufficientPermissionsException(final String operation) {
        super(buildMessage(operation));
        validateOperation(operation);
        this.operation = operation;
    }
    
    /**
     * Obtiene la descripción de la operación que requería permisos.
     * 
     * @return la operación intentada (nunca null)
     */
    public String getOperation() {
        return operation;
    }
    
    private static String buildMessage(final String operation) {
        return String.format(
            "Insufficient permissions to perform operation: %s. " +
            "Only users with update:tables or admin:all permissions are allowed to perform this action.",
            operation
        );
    }
    
    private static void validateOperation(final String operation) {
        if (operation == null || operation.trim().isEmpty()) {
            throw new IllegalArgumentException("Operation description cannot be null or empty");
        }
    }
}
