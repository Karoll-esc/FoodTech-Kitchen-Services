package com.foodtech.kitchen.infrastructure.rest;

import com.foodtech.kitchen.application.usecases.CreateTableUseCase;
import com.foodtech.kitchen.application.usecases.GetAllTablesUseCase;
import com.foodtech.kitchen.application.usecases.GetTableByIdUseCase;
import com.foodtech.kitchen.application.ports.in.UpdateTableStatusPort;
import com.foodtech.kitchen.domain.model.Table;
import com.foodtech.kitchen.infrastructure.rest.dto.CreateTableRequest;
import com.foodtech.kitchen.infrastructure.rest.dto.TableResponse;
import com.foodtech.kitchen.infrastructure.rest.dto.UpdateTableStatusRequest;
import com.foodtech.kitchen.infrastructure.rest.mapper.TableMapper;
import com.foodtech.kitchen.infrastructure.security.Permissions;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * REST controller for table management operations (HU-005, HU-007).
 * 
 * <p><strong>Architecture:</strong></p>
 * <ul>
 *   <li>Layer: Infrastructure (REST)</li>
 *   <li>Pattern: Controller (coordinates use cases)</li>
 *   <li>Base Path: /api/tables</li>
 *   <li>Authorization: Mixed (POST requires admin:all, PATCH requires update:tables, GET requires authentication)</li>
 * </ul>
 * 
 * <p><strong>Endpoints:</strong></p>
 * <ul>
 *   <li>POST /api/tables - Create new table (ADMIN only) - HU-005</li>
 *   <li>GET /api/tables - List all tables (Any authenticated user) - HU-005</li>
 *   <li>GET /api/tables/{id} - Get table by ID (Any authenticated user) - HU-005</li>
 *   <li>PATCH /api/tables/{id}/status - Update table status manually (WAITER/ADMIN) - HU-007</li>
 * </ul>
 * 
 * <p><strong>Security:</strong></p>
 * <ul>
 *   <li>All endpoints require authentication (JWT token)</li>
 *   <li>POST endpoints require admin:all permission</li>
 *   <li>PATCH endpoints require update:tables or admin:all permission (HU-007)</li>
 *   <li>GET endpoints accessible to any authenticated user</li>
 *   <li>401 Unauthorized if no valid JWT token</li>
 *   <li>403 Forbidden if user lacks required permission</li>
 * </ul>
 * 
 * <p><strong>Responsibilities:</strong></p>
 * <ul>
 *   <li>Validate HTTP request format (delegated to Spring)</li>
 *   <li>Coordinate use case execution</li>
 *   <li>Map domain entities to DTOs</li>
 *   <li>Return appropriate HTTP status codes</li>
 *   <li>NO business logic (SRP compliance)</li>
 * </ul>
 * 
 * <p><strong>Error Handling:</strong></p>
 * <ul>
 *   <li>400 Bad Request: Validation errors (handled by GlobalExceptionHandler)</li>
 *   <li>404 Not Found: Table not found (TableNotFoundException)</li>
 *   <li>409 Conflict: Duplicate table number (TableAlreadyExistsException) or invalid transition (InvalidTableTransitionException)</li>
 * </ul>
 * 
 * <p><strong>Example Requests:</strong></p>
 * <pre>
 * // Create table
 * POST /api/tables
 * Authorization: Bearer {jwt-token}
 * Content-Type: application/json
 * {
 *   "tableNumber": "A1",
 *   "capacity": 4
 * }
 * 
 * // Response: 201 Created
 * {
 *   "id": 1,
 *   "tableNumber": "A1",
 *   "capacity": 4,
 *   "status": "AVAILABLE",
 *   "currentOrderId": null,
 *   "createdAt": "2026-01-21T16:00:00",
 *   "updatedAt": "2026-01-21T16:00:00"
 * }
 * </pre>
 * 
 * @see CreateTableUseCase
 * @see GetAllTablesUseCase
 * @see GetTableByIdUseCase
 * @see UpdateTableStatusPort
 * @see TableResponse
 * @see CreateTableRequest
 * @see UpdateTableStatusRequest
 * @author FoodTech Kitchen Team
 * @version 1.0
 * @since 2026-01-21
 */
@RestController
@RequestMapping("/api/tables")
public class TableController {
    
    private final CreateTableUseCase createTableUseCase;
    private final GetAllTablesUseCase getAllTablesUseCase;
    private final GetTableByIdUseCase getTableByIdUseCase;
    private final UpdateTableStatusPort updateTableStatusPort;
    
    /**
     * Constructs the controller with required use case dependencies.
     * 
     * @param createTableUseCase use case for creating new tables
     * @param getAllTablesUseCase use case for listing all tables
     * @param getTableByIdUseCase use case for retrieving table by ID
     * @param updateTableStatusPort port for updating table status (HU-007)
     */
    public TableController(CreateTableUseCase createTableUseCase,
                          GetAllTablesUseCase getAllTablesUseCase,
                          GetTableByIdUseCase getTableByIdUseCase,
                          UpdateTableStatusPort updateTableStatusPort) {
        this.createTableUseCase = createTableUseCase;
        this.getAllTablesUseCase = getAllTablesUseCase;
        this.getTableByIdUseCase = getTableByIdUseCase;
        this.updateTableStatusPort = updateTableStatusPort;
    }
    
    /**
     * Creates a new restaurant table.
     * 
     * <p><strong>HTTP Method:</strong> POST</p>
     * <p><strong>Path:</strong> /api/tables</p>
     * <p><strong>Authorization:</strong> Requires ADMIN authority</p>
     * 
     * <p><strong>Validations:</strong></p>
     * <ul>
     *   <li>tableNumber must not be null or empty</li>
     *   <li>tableNumber must be unique (409 Conflict if duplicate)</li>
     *   <li>capacity must be greater than zero</li>
     * </ul>
     * 
     * <p><strong>Status Codes:</strong></p>
     * <ul>
     *   <li>201 Created: Table created successfully</li>
     *   <li>400 Bad Request: Invalid input (null/empty tableNumber, capacity ≤ 0)</li>
     *   <li>401 Unauthorized: No valid JWT token</li>
     *   <li>403 Forbidden: User lacks ADMIN authority</li>
     *   <li>409 Conflict: Table number already exists</li>
     * </ul>
     * 
     * @param request the table creation request containing tableNumber and capacity
     * @return ResponseEntity with 201 Created and the created TableResponse
     * @see CreateTableUseCase
     * @see CreateTableRequest
     * @see TableResponse
     */
    @PostMapping
    @PreAuthorize("hasAuthority('" + Permissions.ADMIN_ALL + "')")
    public ResponseEntity<TableResponse> createTable(@RequestBody CreateTableRequest request) {
        Table table = createTableUseCase.execute(request.getTableNumber(), request.getCapacity());
        TableResponse response = TableMapper.toResponse(table);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    /**
     * Retrieves all registered tables.
     * 
     * <p><strong>HTTP Method:</strong> GET</p>
     * <p><strong>Path:</strong> /api/tables</p>
     * <p><strong>Authorization:</strong> Requires authentication (any valid JWT token)</p>
     * 
     * <p><strong>Status Codes:</strong></p>
     * <ul>
     *   <li>200 OK: Returns list of tables (empty list if none exist)</li>
     *   <li>401 Unauthorized: No valid JWT token</li>
     * </ul>
     * 
     * <p><strong>Access Control:</strong></p>
     * <p>This endpoint is accessible to all authenticated users (kitchen staff, waiters, admins)
     * as they need to view available tables for order management and table assignment.</p>
     * 
     * @return ResponseEntity with 200 OK and list of TableResponse objects
     * @see GetAllTablesUseCase
     * @see TableResponse
     */
    @GetMapping
    public ResponseEntity<List<TableResponse>> getAllTables() {
        List<Table> tables = getAllTablesUseCase.execute();
        List<TableResponse> responses = tables.stream()
            .map(TableMapper::toResponse)
            .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }
    
    /**
     * Retrieves a specific table by its database ID.
     * 
     * <p><strong>HTTP Method:</strong> GET</p>
     * <p><strong>Path:</strong> /api/tables/{id}</p>
     * <p><strong>Authorization:</strong> Requires authentication (any valid JWT token)</p>
     * 
     * <p><strong>Status Codes:</strong></p>
     * <ul>
     *   <li>200 OK: Table found and returned</li>
     *   <li>401 Unauthorized: No valid JWT token</li>
     *   <li>404 Not Found: Table with specified ID does not exist</li>
     * </ul>
     * 
     * <p><strong>Access Control:</strong></p>
     * <p>This endpoint is accessible to all authenticated users (kitchen staff, waiters, admins)
     * as they need to view table details for order processing and service.</p>
     * 
     * @param id the database ID of the table to retrieve
     * @return ResponseEntity with 200 OK and the TableResponse
     * @see GetTableByIdUseCase
     * @see TableResponse
     */
    @GetMapping("/{id}")
    public ResponseEntity<TableResponse> getTableById(@PathVariable Long id) {
        Table table = getTableByIdUseCase.execute(id);
        TableResponse response = TableMapper.toResponse(table);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Updates the status of a table manually (HU-007).
     * 
     * <p><strong>HTTP Method:</strong> PATCH</p>
     * <p><strong>Path:</strong> /api/tables/{id}/status</p>
     * <p><strong>Authorization:</strong> Requires update:tables or admin:all permission</p>
     * 
     * <p><strong>Business Rules:</strong></p>
     * <ul>
     *   <li>State transitions must follow valid flow: AVAILABLE → OCCUPIED → SERVED → CLEANING → AVAILABLE</li>
     *   <li>OCCUPIED requires active order (currentOrderId not null)</li>
     *   <li>AVAILABLE automatically clears order (sets currentOrderId to null)</li>
     *   <li>lastStateChangeAt is updated automatically</li>
     * </ul>
     * 
     * <p><strong>Validations:</strong></p>
     * <ul>
     *   <li>newStatus must not be null (400 Bad Request)</li>
     *   <li>table must exist (404 Not Found)</li>
     *   <li>transition must be valid (409 Conflict - InvalidTableTransitionException)</li>
     *   <li>OCCUPIED requires active order (400 Bad Request - TableWithoutActiveOrderException)</li>
     * </ul>
     * 
     * <p><strong>Status Codes:</strong></p>
     * <ul>
     *   <li>200 OK: Status updated successfully</li>
     *   <li>400 Bad Request: Invalid input or table has no active order for OCCUPIED status</li>
     *   <li>401 Unauthorized: No valid JWT token</li>
     *   <li>403 Forbidden: User lacks update:tables or admin:all permission</li>
     *   <li>404 Not Found: Table with specified ID does not exist</li>
     *   <li>409 Conflict: Invalid state transition</li>
     * </ul>
     * 
     * <p><strong>Example Request:</strong></p>
     * <pre>
     * PATCH /api/tables/1/status
     * Authorization: Bearer {jwt-token}
     * Content-Type: application/json
     * {
     *   "newStatus": "OCCUPIED"
     * }
     * 
     * // Response: 200 OK
     * {
     *   "id": 1,
     *   "tableNumber": "A1",
     *   "capacity": 4,
     *   "status": "OCCUPIED",
     *   "currentOrderId": 123,
     *   "lastStateChangeAt": "2026-01-21T16:30:00",
     *   "createdAt": "2026-01-21T16:00:00",
     *   "updatedAt": "2026-01-21T16:30:00"
     * }
     * </pre>
     * 
     * @param id the database ID of the table to update
     * @param request the status update request containing the new status
     * @return ResponseEntity with 200 OK and the updated TableResponse
     * @see UpdateTableStatusPort
     * @see UpdateTableStatusRequest
     * @see TableResponse
     */
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyAuthority('" + Permissions.ADMIN_ALL + "', '" + Permissions.UPDATE_TABLES + "')")
    public ResponseEntity<TableResponse> updateTableStatus(
        @PathVariable Long id,
        @RequestBody UpdateTableStatusRequest request
    ) {
        if (request.newStatus() == null) {
            throw new IllegalArgumentException("New status cannot be null");
        }
        Table table = updateTableStatusPort.execute(id, request.newStatus());
        TableResponse response = TableMapper.toResponse(table);
        return ResponseEntity.ok(response);
    }
}

