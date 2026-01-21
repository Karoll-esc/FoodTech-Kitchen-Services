package com.foodtech.kitchen.infrastructure.rest;

import com.foodtech.kitchen.application.usecases.CreateTableUseCase;
import com.foodtech.kitchen.application.usecases.GetAllTablesUseCase;
import com.foodtech.kitchen.application.usecases.GetTableByIdUseCase;
import com.foodtech.kitchen.domain.model.Table;
import com.foodtech.kitchen.infrastructure.rest.dto.CreateTableRequest;
import com.foodtech.kitchen.infrastructure.rest.dto.TableResponse;
import com.foodtech.kitchen.infrastructure.rest.mapper.TableMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * REST controller for table management operations (HU-005).
 * 
 * <p><strong>Architecture:</strong></p>
 * <ul>
 *   <li>Layer: Infrastructure (REST)</li>
 *   <li>Pattern: Controller (coordinates use cases)</li>
 *   <li>Base Path: /api/tables</li>
 *   <li>Authorization: ADMIN only (enforced via @PreAuthorize)</li>
 * </ul>
 * 
 * <p><strong>Endpoints:</strong></p>
 * <ul>
 *   <li>POST /api/tables - Create new table</li>
 *   <li>GET /api/tables - List all tables</li>
 *   <li>GET /api/tables/{id} - Get table by ID</li>
 * </ul>
 * 
 * <p><strong>Security:</strong></p>
 * <ul>
 *   <li>All endpoints require authentication (JWT token)</li>
 *   <li>All endpoints require ADMIN authority</li>
 *   <li>Authorization enforced by @PreAuthorize annotation</li>
 *   <li>401 Unauthorized if no valid JWT token</li>
 *   <li>403 Forbidden if user lacks ADMIN authority</li>
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
 *   <li>409 Conflict: Duplicate table number (TableAlreadyExistsException)</li>
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
 * @see TableResponse
 * @see CreateTableRequest
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
    
    /**
     * Constructs the controller with required use case dependencies.
     * 
     * @param createTableUseCase use case for creating new tables
     * @param getAllTablesUseCase use case for listing all tables
     * @param getTableByIdUseCase use case for retrieving table by ID
     */
    public TableController(CreateTableUseCase createTableUseCase,
                          GetAllTablesUseCase getAllTablesUseCase,
                          GetTableByIdUseCase getTableByIdUseCase) {
        this.createTableUseCase = createTableUseCase;
        this.getAllTablesUseCase = getAllTablesUseCase;
        this.getTableByIdUseCase = getTableByIdUseCase;
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
    @PreAuthorize("hasAuthority('ADMIN')")
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
     * <p><strong>Authorization:</strong> Requires ADMIN authority</p>
     * 
     * <p><strong>Status Codes:</strong></p>
     * <ul>
     *   <li>200 OK: Returns list of tables (empty list if none exist)</li>
     *   <li>401 Unauthorized: No valid JWT token</li>
     *   <li>403 Forbidden: User lacks ADMIN authority</li>
     * </ul>
     * 
     * @return ResponseEntity with 200 OK and list of TableResponse objects
     * @see GetAllTablesUseCase
     * @see TableResponse
     */
    @GetMapping
    @PreAuthorize("hasAuthority('ADMIN')")
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
     * <p><strong>Authorization:</strong> Requires ADMIN authority</p>
     * 
     * <p><strong>Status Codes:</strong></p>
     * <ul>
     *   <li>200 OK: Table found and returned</li>
     *   <li>401 Unauthorized: No valid JWT token</li>
     *   <li>403 Forbidden: User lacks ADMIN authority</li>
     *   <li>404 Not Found: Table with specified ID does not exist</li>
     * </ul>
     * 
     * @param id the database ID of the table to retrieve
     * @return ResponseEntity with 200 OK and the TableResponse
     * @see GetTableByIdUseCase
     * @see TableResponse
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<TableResponse> getTableById(@PathVariable Long id) {
        Table table = getTableByIdUseCase.execute(id);
        TableResponse response = TableMapper.toResponse(table);
        return ResponseEntity.ok(response);
    }
}
