package com.foodtech.kitchen.infrastructure.rest.exception;

import com.foodtech.kitchen.application.exception.OrderNotFoundException;
import com.foodtech.kitchen.application.exception.ProductAlreadyExistsException;
import com.foodtech.kitchen.application.exception.ProductNotFoundException;
import com.foodtech.kitchen.application.exception.StationAuthorizationException;
import com.foodtech.kitchen.application.exception.TableAlreadyExistsException;
import com.foodtech.kitchen.application.exception.TableNotFoundException;
import com.foodtech.kitchen.application.exception.TaskNotFoundException;
import com.foodtech.kitchen.infrastructure.rest.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

//HUMAN REVIEW: Manejo centralizado de excepciones. Cumple SRP: controller solo coordina, este handler maneja errores.
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(OrderNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleOrderNotFoundException(OrderNotFoundException ex) {
        ErrorResponse error = new ErrorResponse(
            ex.getMessage(),
            "Order not found",
            HttpStatus.NOT_FOUND.value()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(TaskNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleTaskNotFoundException(TaskNotFoundException ex) {
        ErrorResponse error = new ErrorResponse(
            ex.getMessage(),
            "Task not found",
            HttpStatus.NOT_FOUND.value()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    /**
     * Handles ProductNotFoundException thrown when attempting to retrieve or modify a non-existent product.
     * 
     * <p>This exception is thrown by product use cases when an operation references a product ID
     * that does not exist in the catalog database. Common scenarios include:</p>
     * <ul>
     *   <li>GET /api/products/{id} - Retrieving a product that doesn't exist</li>
     *   <li>PUT /api/products/{id} - Updating a product that doesn't exist</li>
     *   <li>PATCH /api/products/{id}/availability - Changing availability of non-existent product</li>
     *   <li>DELETE /api/products/{id} - Deleting a product that doesn't exist (caught by controller)</li>
     * </ul>
     * 
     * <p><strong>HTTP Response:</strong></p>
     * <pre>
     * Status: 404 Not Found
     * Body: {
     *   "error": "Product not found",
     *   "message": "Product not found with id: 123",
     *   "status": 404
     * }
     * </pre>
     * 
     * <p><strong>Note:</strong> DELETE endpoints may catch this exception internally to ensure
     * idempotent behavior (returning 204 regardless of existence).</p>
     * 
     * @param ex the ProductNotFoundException containing the product ID that was not found
     * @return ResponseEntity with 404 status and error details in response body
     * @see ProductNotFoundException
     * @see com.foodtech.kitchen.application.usecases.GetProductByIdUseCase
     * @see com.foodtech.kitchen.application.usecases.UpdateProductUseCase
     * @see com.foodtech.kitchen.application.usecases.UpdateProductAvailabilityUseCase
     * @see com.foodtech.kitchen.application.usecases.DeleteProductUseCase
     */
    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleProductNotFoundException(ProductNotFoundException ex) {
        ErrorResponse error = new ErrorResponse(
            "Product not found",
            ex.getMessage(),
            HttpStatus.NOT_FOUND.value()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    /**
     * Handles ProductAlreadyExistsException thrown when attempting to create a product with a duplicate name.
     * 
     * <p>This exception is thrown by CreateProductUseCase when a product creation request
     * contains a name that already exists in the catalog. Product names must be unique to
     * prevent confusion and ensure proper catalog management.</p>
     * 
     * <p><strong>Scenario:</strong></p>
     * <ul>
     *   <li>POST /api/products - Creating a product with a name that already exists</li>
     * </ul>
     * 
     * <p><strong>Business Rule:</strong></p>
     * <p>Product names must be unique (case-sensitive). This constraint ensures that:</p>
     * <ul>
     *   <li>Each product has a distinct identity in the catalog</li>
     *   <li>Kitchen staff can unambiguously identify products</li>
     *   <li>Reporting and analytics are accurate</li>
     *   <li>UI/UX displays remain clear without duplicates</li>
     * </ul>
     * 
     * <p><strong>HTTP Response:</strong></p>
     * <pre>
     * Status: 409 Conflict
     * Body: {
     *   "error": "Product already exists",
     *   "message": "Product with name 'Coca Cola' already exists",
     *   "status": 409
     * }
     * </pre>
     * 
     * <p><strong>Resolution:</strong></p>
     * <ul>
     *   <li>Client should use a different product name</li>
     *   <li>Or retrieve existing product with GET /api/products and update it with PUT</li>
     *   <li>Or check if existing product should be updated instead of creating a new one</li>
     * </ul>
     * 
     * @param ex the ProductAlreadyExistsException containing the duplicate product name
     * @return ResponseEntity with 409 Conflict status and error details in response body
     * @see ProductAlreadyExistsException
     * @see com.foodtech.kitchen.application.usecases.CreateProductUseCase
     * @see com.foodtech.kitchen.domain.services.ProductValidator
     */
    @ExceptionHandler(ProductAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleProductAlreadyExistsException(ProductAlreadyExistsException ex) {
        ErrorResponse error = new ErrorResponse(
            "Product already exists",
            ex.getMessage(),
            HttpStatus.CONFLICT.value()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    /**
     * Handles StationAuthorizationException thrown when a user attempts to update a task
     * at a station for which they don't have the required permission.
     * 
     * <p>This exception is thrown by StationAuthorizationService when a kitchen staff member
     * attempts to modify tasks assigned to a station they're not authorized for. The FoodTech
     * Kitchen system enforces strict station-based access control:</p>
     * <ul>
     *   <li><strong>Bar Staff:</strong> Can only update BAR station tasks (requires {@code update:tasks:bar})</li>
     *   <li><strong>Hot Kitchen Staff:</strong> Can only update HOT_KITCHEN station tasks (requires {@code update:tasks:hot-kitchen})</li>
     *   <li><strong>Cold Kitchen Staff:</strong> Can only update COLD_KITCHEN station tasks (requires {@code update:tasks:cold-kitchen})</li>
     *   <li><strong>Admin:</strong> Can update ALL station tasks (has {@code admin:all} permission)</li>
     * </ul>
     * 
     * <p><strong>Common Scenarios:</strong></p>
     * <ul>
     *   <li>Bar staff trying to start a HOT_KITCHEN task → 403 Forbidden</li>
     *   <li>Hot kitchen staff trying to complete a COLD_KITCHEN task → 403 Forbidden</li>
     *   <li>Cold kitchen staff trying to update a BAR task → 403 Forbidden</li>
     * </ul>
     * 
     * <p><strong>HTTP Response Example:</strong></p>
     * <pre>
     * Status: 403 Forbidden
     * Body: {
     *   "error": "Forbidden",
     *   "message": "User is not authorized to update tasks at station: HOT_KITCHEN. 
     *               User permissions: update:tasks:bar, read:tasks. 
     *               Required permission: update:tasks:hot-kitchen or admin:all",
     *   "status": 403
     * }
     * </pre>
     * 
     * <p><strong>Security Implications:</strong></p>
     * <p>This authorization mechanism ensures that kitchen staff can only work on tasks
     * within their designated station, preventing cross-station interference and maintaining
     * proper workflow separation. Admins bypass all station restrictions for management purposes.</p>
     * 
     * <p><strong>Resolution:</strong></p>
     * <ul>
     *   <li>User must request access to the required station permission from administrator</li>
     *   <li>Administrator must update user's role/permissions in Auth0 tenant</li>
     *   <li>User must obtain a new JWT token with updated permissions</li>
     *   <li>Alternatively, admin user with {@code admin:all} permission can perform the operation</li>
     * </ul>
     * 
     * @param ex the StationAuthorizationException containing station and user permission details
     * @return ResponseEntity with 403 Forbidden status and detailed error message
     * @see StationAuthorizationException
     * @see com.foodtech.kitchen.application.services.StationAuthorizationService
     * @see com.foodtech.kitchen.infrastructure.security.Permissions
     */
    @ExceptionHandler(StationAuthorizationException.class)
    public ResponseEntity<ErrorResponse> handleStationAuthorizationException(StationAuthorizationException ex) {
        ErrorResponse error = new ErrorResponse(
            "Forbidden",
            ex.getMessage(),
            HttpStatus.FORBIDDEN.value()
        );
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    @ExceptionHandler(TableAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleTableAlreadyExists(TableAlreadyExistsException ex) {
        ErrorResponse error = new ErrorResponse(
            "Table number already exists",
            ex.getMessage(),
            HttpStatus.CONFLICT.value()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(TableNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleTableNotFound(TableNotFoundException ex) {
        ErrorResponse error = new ErrorResponse(
            "Table not found",
            ex.getMessage(),
            HttpStatus.NOT_FOUND.value()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(IllegalArgumentException ex) {
        ErrorResponse error = new ErrorResponse(
            "Validation failed",
            ex.getMessage(),
            HttpStatus.BAD_REQUEST.value()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalStateException(IllegalStateException ex) {
        ErrorResponse error = new ErrorResponse(
            ex.getMessage(),
            "Invalid state transition",
            HttpStatus.BAD_REQUEST.value()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatchException(MethodArgumentTypeMismatchException ex) {
        String message = String.format("Invalid value '%s' for parameter '%s'. Expected one of: BAR, HOT_KITCHEN, COLD_KITCHEN", 
            ex.getValue(), ex.getName());
        ErrorResponse error = new ErrorResponse(
            message,
            "Invalid parameter type",
            HttpStatus.BAD_REQUEST.value()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        ErrorResponse error = new ErrorResponse(
            "Internal server error",
            ex.getMessage(),
            HttpStatus.INTERNAL_SERVER_ERROR.value()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
