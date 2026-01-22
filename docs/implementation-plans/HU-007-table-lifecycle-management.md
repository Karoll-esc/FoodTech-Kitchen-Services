# 📋 Plan de Implementación - HU-007: Gestión manual del ciclo de vida de las mesas

**Historia de Usuario:** Gestión manual del ciclo de vida de las mesas  
**Prioridad:** Alta  
**Estimación:** 3-4 días  
**Fecha de Creación:** 2026-01-21

---

## 📖 Resumen de la Historia de Usuario

**Como** mesero  
**Quiero** actualizar manualmente el estado de las mesas a medida que avanza el servicio  
**Para** tener un control preciso sobre la disponibilidad real del salón y el progreso de cada mesa

> **Nota:** Esta historia modifica el comportamiento sugerido en la **HU-005**, eliminando las transiciones automáticas en favor de una gestión supervisada por el personal.

---

## 🎯 Objetivos de Implementación

1. Implementar cambios manuales de estado de mesa por parte del mesero
2. Validar transiciones de estado según flujo lógico permitido
3. Garantizar que solo personal autorizado (WAITER, ADMIN) pueda cambiar estados
4. Registrar timestamp de cada cambio de estado
5. Verificar que la mesa tenga pedido activo antes de marcarla como OCCUPIED
6. Desvincular pedido al regresar mesa a estado AVAILABLE
7. Prevenir transiciones automáticas por eventos del sistema

---

## 🏗️ Arquitectura y Componentes

### Capas Involucradas (Hexagonal Architecture)

```
Domain Layer (Lógica de Negocio)
├── Table (Entity - ya existente, requiere actualización)
├── TableStatus (Enum - ya existente)
├── TableStateTransition (Value Object - NUEVO)
└── TableLifecycleValidator (Service - NUEVO)

Application Layer (Casos de Uso)
├── UpdateTableStatusUseCase (NUEVO)
├── ports/in/
│   └── UpdateTableStatusPort (NUEVO)
├── ports/out/
│   ├── TableRepository (actualizar)
│   └── OrderRepository (verificar pedido activo)
└── exceptions/
    ├── InvalidTableTransitionException (NUEVO)
    ├── TableWithoutActiveOrderException (NUEVO)
    └── InsufficientPermissionsException (NUEVO)

Infrastructure Layer (Adaptadores)
├── TableController (actualizar)
├── TableEntity (actualizar - agregar lastStateChangeAt)
├── TableRepositoryAdapter (actualizar)
├── UpdateTableStatusRequest (DTO - NUEVO)
├── TableMapper (actualizar)
└── Security/Authorization (integrar validación de roles)
```

---

## 🔄 Flujo de Transiciones de Estado

### Transiciones Permitidas

```
AVAILABLE → OCCUPIED    (requiere pedido activo)
OCCUPIED → SERVED       (manual, verificar tareas completadas opcional)
SERVED → CLEANING       (manual)
CLEANING → AVAILABLE    (desvincula pedido)
```

### Transiciones Prohibidas

- AVAILABLE → SERVED (debe pasar por OCCUPIED)
- AVAILABLE → CLEANING (debe pasar por OCCUPIED)
- OCCUPIED → AVAILABLE (debe pasar por SERVED y CLEANING)
- SERVED → OCCUPIED (no se puede revertir)
- CLEANING → OCCUPIED (debe completar ciclo a AVAILABLE)

---

## 📦 Tareas de Implementación

### ✅ Fase 1: Domain Layer (Sin dependencias de framework)

#### Task 1.1: Actualizar TableStatus Enum con validación de transiciones
**Archivo:** `src/main/java/com/foodtech/kitchen/domain/model/TableStatus.java`

**Acción:** Agregar método para validar transiciones

```java
public enum TableStatus {
    AVAILABLE,
    OCCUPIED,
    SERVED,
    CLEANING;
    
    /**
     * Valida si la transición al estado destino es permitida.
     * 
     * @param targetStatus Estado objetivo
     * @return true si la transición es válida
     */
    public boolean canTransitionTo(TableStatus targetStatus) {
        if (this == targetStatus) {
            return false; // No se puede transicionar al mismo estado
        }
        
        return switch (this) {
            case AVAILABLE -> targetStatus == OCCUPIED;
            case OCCUPIED -> targetStatus == SERVED;
            case SERVED -> targetStatus == CLEANING;
            case CLEANING -> targetStatus == AVAILABLE;
        };
    }
    
    /**
     * Retorna los estados válidos desde el estado actual.
     */
    public List<TableStatus> getValidTransitions() {
        return Arrays.stream(TableStatus.values())
            .filter(this::canTransitionTo)
            .toList();
    }
}
```

**Tests:** `TableStatusTest.java`
- `shouldAllowTransitionFromAvailableToOccupied()`
- `shouldAllowTransitionFromOccupiedToServed()`
- `shouldAllowTransitionFromServedToCleaning()`
- `shouldAllowTransitionFromCleaningToAvailable()`
- `shouldRejectInvalidTransitions()`
- `shouldRejectTransitionToSameState()`

---

#### Task 1.2: Crear Value Object para transición de estado
**Archivo:** `src/main/java/com/foodtech/kitchen/domain/model/TableStateTransition.java`

```java
package com.foodtech.kitchen.domain.model;

import java.time.LocalDateTime;

/**
 * Value Object que representa una transición de estado de mesa.
 * Inmutable.
 */
public final class TableStateTransition {
    private final TableStatus fromStatus;
    private final TableStatus toStatus;
    private final LocalDateTime transitionTime;
    
    public TableStateTransition(TableStatus fromStatus, TableStatus toStatus) {
        if (fromStatus == null || toStatus == null) {
            throw new IllegalArgumentException("States cannot be null");
        }
        if (!fromStatus.canTransitionTo(toStatus)) {
            throw new IllegalArgumentException(
                String.format("Invalid transition from %s to %s", fromStatus, toStatus)
            );
        }
        
        this.fromStatus = fromStatus;
        this.toStatus = toStatus;
        this.transitionTime = LocalDateTime.now();
    }
    
    public TableStatus getFromStatus() { return fromStatus; }
    public TableStatus getToStatus() { return toStatus; }
    public LocalDateTime getTransitionTime() { return transitionTime; }
}
```

**Tests:** `TableStateTransitionTest.java`
- `shouldCreateValidTransition()`
- `shouldRejectInvalidTransition()`
- `shouldRejectNullStates()`
- `shouldRecordTransitionTime()`

---

#### Task 1.3: Crear TableLifecycleValidator
**Archivo:** `src/main/java/com/foodtech/kitchen/domain/services/TableLifecycleValidator.java`

```java
package com.foodtech.kitchen.domain.services;

import com.foodtech.kitchen.domain.model.Table;
import com.foodtech.kitchen.domain.model.TableStatus;

/**
 * Validador de reglas de negocio para el ciclo de vida de mesas.
 * NO usa anotaciones de Spring.
 */
public class TableLifecycleValidator {
    
    /**
     * Valida que la transición de estado sea válida.
     * 
     * @throws IllegalArgumentException si la transición no es permitida
     */
    public void validateTransition(TableStatus currentStatus, TableStatus targetStatus) {
        if (currentStatus == null || targetStatus == null) {
            throw new IllegalArgumentException("Status cannot be null");
        }
        
        if (!currentStatus.canTransitionTo(targetStatus)) {
            throw new IllegalArgumentException(
                String.format(
                    "Cannot transition from %s to %s. Valid transitions from %s are: %s",
                    currentStatus,
                    targetStatus,
                    currentStatus,
                    currentStatus.getValidTransitions()
                )
            );
        }
    }
    
    /**
     * Valida que la mesa pueda ser marcada como ocupada.
     * Requiere que tenga al menos un pedido activo.
     * 
     * @throws IllegalArgumentException si no tiene pedido activo
     */
    public void validateCanBeOccupied(Table table) {
        if (table.getCurrentOrderId() == null) {
            throw new IllegalArgumentException(
                String.format(
                    "Table %s cannot be marked as OCCUPIED without an active order",
                    table.getTableNumber()
                )
            );
        }
    }
}
```

**Tests:** `TableLifecycleValidatorTest.java`
- `shouldValidateValidTransitions()`
- `shouldRejectInvalidTransitions()`
- `shouldRejectTransitionWithNullStatus()`
- `shouldValidateTableCanBeOccupiedWithOrder()`
- `shouldRejectOccupiedStateWithoutOrder()`

---

#### Task 1.4: Actualizar entidad Table
**Archivo:** `src/main/java/com/foodtech/kitchen/domain/model/Table.java`

**Acción:** Agregar campo y método para gestionar cambios de estado

```java
public class Table {
    private Long id;
    private String tableNumber;
    private int capacity;
    private TableStatus status;
    private Long currentOrderId;
    private LocalDateTime createdAt;
    private LocalDateTime lastStateChangeAt; // NUEVO CAMPO
    
    // Constructor existente actualizado
    public Table(String tableNumber, int capacity) {
        validateTableNumber(tableNumber);
        validateCapacity(capacity);
        
        this.tableNumber = tableNumber;
        this.capacity = capacity;
        this.status = TableStatus.AVAILABLE;
        this.currentOrderId = null;
        this.createdAt = LocalDateTime.now();
        this.lastStateChangeAt = this.createdAt; // NUEVO
    }
    
    /**
     * Cambia el estado de la mesa validando la transición.
     * 
     * @param newStatus Nuevo estado objetivo
     * @throws IllegalArgumentException si la transición no es válida
     */
    public void changeStatus(TableStatus newStatus) {
        if (newStatus == null) {
            throw new IllegalArgumentException("New status cannot be null");
        }
        
        if (!this.status.canTransitionTo(newStatus)) {
            throw new IllegalArgumentException(
                String.format("Cannot transition from %s to %s", this.status, newStatus)
            );
        }
        
        this.status = newStatus;
        this.lastStateChangeAt = LocalDateTime.now();
    }
    
    /**
     * Asigna un pedido a la mesa.
     */
    public void assignOrder(Long orderId) {
        if (orderId == null || orderId <= 0) {
            throw new IllegalArgumentException("Order ID must be positive");
        }
        this.currentOrderId = orderId;
    }
    
    /**
     * Desvincula el pedido de la mesa (al limpiar).
     */
    public void clearOrder() {
        this.currentOrderId = null;
    }
    
    public LocalDateTime getLastStateChangeAt() {
        return lastStateChangeAt;
    }
    
    // Getters existentes...
}
```

**Tests:** `TableTest.java` (actualizar existente)
- `shouldChangeStatusWhenTransitionIsValid()`
- `shouldThrowExceptionWhenTransitionIsInvalid()`
- `shouldUpdateLastStateChangeAtWhenChangingStatus()`
- `shouldAssignOrderToTable()`
- `shouldClearOrderFromTable()`
- `shouldThrowExceptionWhenAssigningInvalidOrderId()`

---

### ✅ Fase 2: Application Layer (Casos de Uso y Puertos)

#### Task 2.1: Crear excepción de dominio para transiciones inválidas
**Archivo:** `src/main/java/com/foodtech/kitchen/application/exceptions/InvalidTableTransitionException.java`

```java
package com.foodtech.kitchen.application.exceptions;

import com.foodtech.kitchen.domain.model.TableStatus;
import java.util.List;

public class InvalidTableTransitionException extends RuntimeException {
    private final TableStatus currentStatus;
    private final TableStatus targetStatus;
    private final List<TableStatus> validTransitions;
    
    public InvalidTableTransitionException(
        TableStatus currentStatus,
        TableStatus targetStatus,
        List<TableStatus> validTransitions
    ) {
        super(String.format(
            "Cannot transition from %s to %s. Valid transitions: %s",
            currentStatus,
            targetStatus,
            validTransitions
        ));
        this.currentStatus = currentStatus;
        this.targetStatus = targetStatus;
        this.validTransitions = validTransitions;
    }
    
    // Getters...
}
```

---

#### Task 2.2: Crear excepción para mesa sin pedido activo
**Archivo:** `src/main/java/com/foodtech/kitchen/application/exceptions/TableWithoutActiveOrderException.java`

```java
package com.foodtech.kitchen.application.exceptions;

public class TableWithoutActiveOrderException extends RuntimeException {
    public TableWithoutActiveOrderException(String tableNumber) {
        super(String.format(
            "Table %s cannot be marked as OCCUPIED without an active order",
            tableNumber
        ));
    }
}
```

---

#### Task 2.3: Crear excepción para permisos insuficientes
**Archivo:** `src/main/java/com/foodtech/kitchen/application/exceptions/InsufficientPermissionsException.java`

```java
package com.foodtech.kitchen.application.exceptions;

public class InsufficientPermissionsException extends RuntimeException {
    public InsufficientPermissionsException(String operation) {
        super(String.format(
            "Insufficient permissions to perform operation: %s. Only WAITER or ADMIN roles allowed.",
            operation
        ));
    }
}
```

---

#### Task 2.4: Crear puerto de entrada
**Archivo:** `src/main/java/com/foodtech/kitchen/application/ports/in/UpdateTableStatusPort.java`

```java
package com.foodtech.kitchen.application.ports.in;

import com.foodtech.kitchen.domain.model.Table;
import com.foodtech.kitchen.domain.model.TableStatus;

/**
 * Puerto de entrada para actualizar el estado de una mesa.
 */
public interface UpdateTableStatusPort {
    /**
     * Actualiza el estado de una mesa.
     * 
     * @param tableId ID de la mesa
     * @param newStatus Nuevo estado
     * @return Mesa actualizada
     * @throws TableNotFoundException si la mesa no existe
     * @throws InvalidTableTransitionException si la transición no es válida
     * @throws TableWithoutActiveOrderException si se intenta ocupar sin pedido
     * @throws InsufficientPermissionsException si el usuario no tiene permisos
     */
    Table execute(Long tableId, TableStatus newStatus);
}
```

---

#### Task 2.5: Actualizar TableRepository port
**Archivo:** `src/main/java/com/foodtech/kitchen/application/ports/out/TableRepository.java`

**Acción:** Agregar método para actualizar

```java
public interface TableRepository {
    void save(Table table);
    Optional<Table> findById(Long id);
    Optional<Table> findByTableNumber(String tableNumber);
    List<Table> findAll();
    void update(Table table); // NUEVO MÉTODO
}
```

---

#### Task 2.6: Implementar caso de uso UpdateTableStatusUseCase
**Archivo:** `src/main/java/com/foodtech/kitchen/application/usecases/UpdateTableStatusUseCase.java`

```java
package com.foodtech.kitchen.application.usecases;

import com.foodtech.kitchen.application.exceptions.*;
import com.foodtech.kitchen.application.ports.in.UpdateTableStatusPort;
import com.foodtech.kitchen.application.ports.out.TableRepository;
import com.foodtech.kitchen.domain.model.Table;
import com.foodtech.kitchen.domain.model.TableStatus;
import com.foodtech.kitchen.domain.services.TableLifecycleValidator;

/**
 * Caso de uso para actualizar el estado de una mesa manualmente.
 */
public class UpdateTableStatusUseCase implements UpdateTableStatusPort {
    
    private final TableRepository tableRepository;
    private final TableLifecycleValidator lifecycleValidator;
    
    public UpdateTableStatusUseCase(
        TableRepository tableRepository,
        TableLifecycleValidator lifecycleValidator
    ) {
        this.tableRepository = tableRepository;
        this.lifecycleValidator = lifecycleValidator;
    }
    
    @Override
    public Table execute(Long tableId, TableStatus newStatus) {
        // 1. Buscar mesa
        Table table = tableRepository.findById(tableId)
            .orElseThrow(() -> new TableNotFoundException(tableId));
        
        // 2. Validar transición
        try {
            lifecycleValidator.validateTransition(table.getStatus(), newStatus);
        } catch (IllegalArgumentException e) {
            throw new InvalidTableTransitionException(
                table.getStatus(),
                newStatus,
                table.getStatus().getValidTransitions()
            );
        }
        
        // 3. Validación especial para OCCUPIED: requiere pedido activo
        if (newStatus == TableStatus.OCCUPIED) {
            try {
                lifecycleValidator.validateCanBeOccupied(table);
            } catch (IllegalArgumentException e) {
                throw new TableWithoutActiveOrderException(table.getTableNumber());
            }
        }
        
        // 4. Lógica especial para AVAILABLE: desvincular pedido
        if (newStatus == TableStatus.AVAILABLE) {
            table.clearOrder();
        }
        
        // 5. Cambiar estado
        table.changeStatus(newStatus);
        
        // 6. Persistir
        tableRepository.update(table);
        
        return table;
    }
}
```

**Tests:** `UpdateTableStatusUseCaseTest.java`
- `shouldUpdateStatusFromAvailableToOccupied()`
- `shouldUpdateStatusFromOccupiedToServed()`
- `shouldUpdateStatusFromServedToCleaning()`
- `shouldUpdateStatusFromCleaningToAvailable()`
- `shouldClearOrderWhenTransitioningToAvailable()`
- `shouldThrowExceptionWhenTableNotFound()`
- `shouldThrowExceptionForInvalidTransition()`
- `shouldThrowExceptionWhenMarkingOccupiedWithoutOrder()`
- `shouldUpdateLastStateChangeTimestamp()`

---

### ✅ Fase 3: Infrastructure Layer (Adaptadores y REST API)

#### Task 3.1: Actualizar TableEntity
**Archivo:** `src/main/java/com/foodtech/kitchen/infrastructure/persistence/entities/TableEntity.java`

**Acción:** Agregar campo lastStateChangeAt

```java
@Entity
@Table(name = "tables")
public class TableEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "table_number", unique = true, nullable = false)
    private String tableNumber;
    
    @Column(nullable = false)
    private Integer capacity;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TableStatus status;
    
    @Column(name = "current_order_id")
    private Long currentOrderId;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "last_state_change_at") // NUEVO CAMPO
    private LocalDateTime lastStateChangeAt;
    
    // Getters y setters...
}
```

**Migration:** Crear archivo Flyway o Liquibase para agregar columna
`V4__add_last_state_change_at_to_tables.sql`

```sql
ALTER TABLE tables
ADD COLUMN last_state_change_at TIMESTAMP;

-- Inicializar con created_at para registros existentes
UPDATE tables
SET last_state_change_at = created_at
WHERE last_state_change_at IS NULL;
```

---

#### Task 3.2: Actualizar TableEntityMapper
**Archivo:** `src/main/java/com/foodtech/kitchen/infrastructure/persistence/mappers/TableEntityMapper.java`

**Acción:** Mapear nuevo campo

```java
public class TableEntityMapper {
    
    public static TableEntity toEntity(Table table) {
        TableEntity entity = new TableEntity();
        entity.setId(table.getId());
        entity.setTableNumber(table.getTableNumber());
        entity.setCapacity(table.getCapacity());
        entity.setStatus(table.getStatus());
        entity.setCurrentOrderId(table.getCurrentOrderId());
        entity.setCreatedAt(table.getCreatedAt());
        entity.setLastStateChangeAt(table.getLastStateChangeAt()); // NUEVO
        return entity;
    }
    
    public static Table toDomain(TableEntity entity) {
        // Constructor reconstruction pattern
        Table table = new Table(
            entity.getTableNumber(),
            entity.getCapacity()
        );
        // Set via reflection or add constructor with all fields
        // Incluir lastStateChangeAt en la reconstrucción
        // ...
        return table;
    }
}
```

---

#### Task 3.3: Actualizar TableRepositoryAdapter
**Archivo:** `src/main/java/com/foodtech/kitchen/infrastructure/persistence/adapters/TableRepositoryAdapter.java`

**Acción:** Implementar método update

```java
@Component
public class TableRepositoryAdapter implements TableRepository {
    
    private final TableJpaRepository jpaRepository;
    private final TableEntityMapper mapper;
    
    // Métodos existentes...
    
    @Override
    public void update(Table table) {
        TableEntity entity = mapper.toEntity(table);
        jpaRepository.save(entity); // JPA save hace update si ID existe
    }
}
```

---

#### Task 3.4: Crear DTO de request
**Archivo:** `src/main/java/com/foodtech/kitchen/infrastructure/rest/dto/UpdateTableStatusRequest.java`

```java
package com.foodtech.kitchen.infrastructure.rest.dto;

import com.foodtech.kitchen.domain.model.TableStatus;
import jakarta.validation.constraints.NotNull;

public class UpdateTableStatusRequest {
    
    @NotNull(message = "Status cannot be null")
    private TableStatus status;
    
    public UpdateTableStatusRequest() {}
    
    public UpdateTableStatusRequest(TableStatus status) {
        this.status = status;
    }
    
    public TableStatus getStatus() {
        return status;
    }
    
    public void setStatus(TableStatus status) {
        this.status = status;
    }
}
```

---

#### Task 3.5: Crear DTO de response
**Archivo:** `src/main/java/com/foodtech/kitchen/infrastructure/rest/dto/TableResponse.java`

**Acción:** Actualizar para incluir lastStateChangeAt

```java
public class TableResponse {
    private Long id;
    private String tableNumber;
    private int capacity;
    private TableStatus status;
    private Long currentOrderId;
    private LocalDateTime createdAt;
    private LocalDateTime lastStateChangeAt; // NUEVO
    
    // Constructor, getters, setters...
}
```

---

#### Task 3.6: Actualizar TableMapper
**Archivo:** `src/main/java/com/foodtech/kitchen/infrastructure/rest/mappers/TableMapper.java`

```java
public class TableMapper {
    
    public static TableResponse toResponse(Table table) {
        return new TableResponse(
            table.getId(),
            table.getTableNumber(),
            table.getCapacity(),
            table.getStatus(),
            table.getCurrentOrderId(),
            table.getCreatedAt(),
            table.getLastStateChangeAt() // NUEVO
        );
    }
}
```

---

#### Task 3.7: Actualizar TableController
**Archivo:** `src/main/java/com/foodtech/kitchen/infrastructure/rest/controllers/TableController.java`

**Acción:** Agregar endpoint PATCH para cambiar estado

```java
@RestController
@RequestMapping("/api/tables")
public class TableController {
    
    private final UpdateTableStatusPort updateTableStatusPort;
    
    // Endpoints existentes...
    
    /**
     * Actualiza el estado de una mesa.
     * Requiere rol WAITER o ADMIN.
     */
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('WAITER', 'ADMIN')")
    public ResponseEntity<TableResponse> updateTableStatus(
        @PathVariable Long id,
        @Valid @RequestBody UpdateTableStatusRequest request
    ) {
        Table updatedTable = updateTableStatusPort.execute(id, request.getStatus());
        return ResponseEntity.ok(TableMapper.toResponse(updatedTable));
    }
}
```

---

#### Task 3.8: Actualizar GlobalExceptionHandler
**Archivo:** `src/main/java/com/foodtech/kitchen/infrastructure/rest/exception/GlobalExceptionHandler.java`

**Acción:** Agregar handlers para nuevas excepciones

```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    // Handlers existentes...
    
    @ExceptionHandler(InvalidTableTransitionException.class)
    public ResponseEntity<ErrorResponse> handleInvalidTransition(
        InvalidTableTransitionException ex
    ) {
        ErrorResponse error = new ErrorResponse(
            ex.getMessage(),
            "INVALID_TRANSITION",
            400
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
    
    @ExceptionHandler(TableWithoutActiveOrderException.class)
    public ResponseEntity<ErrorResponse> handleTableWithoutOrder(
        TableWithoutActiveOrderException ex
    ) {
        ErrorResponse error = new ErrorResponse(
            ex.getMessage(),
            "TABLE_WITHOUT_ORDER",
            400
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
    
    @ExceptionHandler(InsufficientPermissionsException.class)
    public ResponseEntity<ErrorResponse> handleInsufficientPermissions(
        InsufficientPermissionsException ex
    ) {
        ErrorResponse error = new ErrorResponse(
            ex.getMessage(),
            "INSUFFICIENT_PERMISSIONS",
            403
        );
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }
}
```

---

### ✅ Fase 4: Configuración y Beans

#### Task 4.1: Registrar beans en configuración
**Archivo:** `src/main/java/com/foodtech/kitchen/infrastructure/config/ApplicationConfig.java`

```java
@Configuration
public class ApplicationConfig {
    
    // Beans existentes...
    
    @Bean
    public TableLifecycleValidator tableLifecycleValidator() {
        return new TableLifecycleValidator();
    }
    
    @Bean
    public UpdateTableStatusPort updateTableStatusPort(
        TableRepository tableRepository,
        TableLifecycleValidator lifecycleValidator
    ) {
        return new UpdateTableStatusUseCase(tableRepository, lifecycleValidator);
    }
}
```

---

### ✅ Fase 5: Testing

#### Task 5.1: Tests Unitarios de Domain
- `TableStatusTest.java` ✓
- `TableStateTransitionTest.java` ✓
- `TableLifecycleValidatorTest.java` ✓
- `TableTest.java` (actualizar) ✓

#### Task 5.2: Tests de Application Layer
- `UpdateTableStatusUseCaseTest.java` ✓

#### Task 5.3: Tests de Integración
**Archivo:** `TableLifecycleIntegrationTest.java`

```java
@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(roles = "WAITER")
class TableLifecycleIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private TableJpaRepository tableRepository;
    
    @Test
    void shouldTransitionTableFromAvailableToOccupied() throws Exception {
        // Given: mesa con pedido
        TableEntity table = createTableWithOrder("A1", 4, 123L);
        tableRepository.save(table);
        
        // When: cambiar a OCCUPIED
        mockMvc.perform(patch("/api/tables/{id}/status", table.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\": \"OCCUPIED\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("OCCUPIED"))
            .andExpect(jsonPath("$.lastStateChangeAt").exists());
        
        // Then: verificar en BD
        TableEntity updated = tableRepository.findById(table.getId()).orElseThrow();
        assertEquals(TableStatus.OCCUPIED, updated.getStatus());
        assertNotNull(updated.getLastStateChangeAt());
    }
    
    @Test
    void shouldRejectInvalidTransition() throws Exception {
        // Given: mesa en AVAILABLE
        TableEntity table = createTable("B2", 2);
        tableRepository.save(table);
        
        // When: intentar cambiar directamente a SERVED
        mockMvc.perform(patch("/api/tables/{id}/status", table.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\": \"SERVED\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("INVALID_TRANSITION"));
    }
    
    @Test
    void shouldRejectOccupiedWithoutOrder() throws Exception {
        // Given: mesa sin pedido
        TableEntity table = createTable("C3", 4);
        tableRepository.save(table);
        
        // When: intentar marcar como OCCUPIED
        mockMvc.perform(patch("/api/tables/{id}/status", table.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\": \"OCCUPIED\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("TABLE_WITHOUT_ORDER"));
    }
    
    @Test
    void shouldClearOrderWhenReturningToAvailable() throws Exception {
        // Given: mesa en CLEANING con pedido
        TableEntity table = createTableWithOrder("D4", 6, 456L);
        table.setStatus(TableStatus.CLEANING);
        tableRepository.save(table);
        
        // When: cambiar a AVAILABLE
        mockMvc.perform(patch("/api/tables/{id}/status", table.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\": \"AVAILABLE\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("AVAILABLE"))
            .andExpect(jsonPath("$.currentOrderId").isEmpty());
        
        // Then: verificar pedido desvinculado
        TableEntity updated = tableRepository.findById(table.getId()).orElseThrow();
        assertNull(updated.getCurrentOrderId());
    }
    
    @Test
    @WithMockUser(roles = "KITCHEN_BAR")
    void shouldRejectAccessFromKitchenStaff() throws Exception {
        // Given: usuario con rol KITCHEN_BAR
        TableEntity table = createTable("E5", 2);
        tableRepository.save(table);
        
        // When: intentar cambiar estado
        mockMvc.perform(patch("/api/tables/{id}/status", table.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\": \"OCCUPIED\"}"))
            .andExpect(status().isForbidden());
    }
    
    @Test
    void shouldCompleteFullLifecycle() throws Exception {
        // Given: mesa nueva con pedido
        TableEntity table = createTableWithOrder("F6", 4, 789L);
        tableRepository.save(table);
        
        // AVAILABLE → OCCUPIED
        mockMvc.perform(patch("/api/tables/{id}/status", table.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\": \"OCCUPIED\"}"))
            .andExpect(status().isOk());
        
        // OCCUPIED → SERVED
        mockMvc.perform(patch("/api/tables/{id}/status", table.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\": \"SERVED\"}"))
            .andExpect(status().isOk());
        
        // SERVED → CLEANING
        mockMvc.perform(patch("/api/tables/{id}/status", table.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\": \"CLEANING\"}"))
            .andExpect(status().isOk());
        
        // CLEANING → AVAILABLE
        mockMvc.perform(patch("/api/tables/{id}/status", table.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\": \"AVAILABLE\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.currentOrderId").isEmpty());
    }
}
```

---

### ✅ Fase 6: Documentación

#### Task 6.1: Actualizar README con flujo de estados
#### Task 6.2: Crear diagrama de estados en docs
#### Task 6.3: Actualizar colección Postman con endpoints de cambio de estado
#### Task 6.4: Documentar restricciones de transición

---

## 🔍 Validaciones de Calidad

### Checklist de Pre-Commit

- [ ] Todos los tests unitarios pasan (`./gradlew test`)
- [ ] Tests de integración pasan
- [ ] Cobertura ≥ 70% (`./gradlew jacocoTestReport`)
- [ ] No hay dependencias invertidas (domain no importa infrastructure)
- [ ] Sin código comentado o TODOs sin resolver
- [ ] Métodos < 20 líneas
- [ ] Nombres descriptivos (sin abreviaciones)
- [ ] Uso de `final` en campos inmutables
- [ ] Validación de transiciones en todos los casos

---

## 📊 Escenarios de Prueba (BDD)

### Scenario 1: Transición válida AVAILABLE → OCCUPIED
```gherkin
Given existe una mesa "A1" en estado "AVAILABLE"
And la mesa tiene asignado el pedido con ID 123
When el mesero marca la mesa como "OCCUPIED"
Then el sistema cambia el estado a "OCCUPIED"
And registra el timestamp del cambio
```

### Scenario 2: Rechazo de transición inválida
```gherkin
Given existe una mesa "B2" en estado "AVAILABLE"
When el mesero intenta marcar la mesa como "SERVED"
Then el sistema rechaza la operación
And retorna error "Cannot transition from AVAILABLE to SERVED"
And muestra las transiciones válidas: [OCCUPIED]
```

### Scenario 3: Rechazo de OCCUPIED sin pedido
```gherkin
Given existe una mesa "C3" en estado "AVAILABLE"
And la mesa no tiene ningún pedido asignado
When el mesero intenta marcar la mesa como "OCCUPIED"
Then el sistema rechaza la operación
And retorna error "Table C3 cannot be marked as OCCUPIED without an active order"
```

### Scenario 4: Desvinculación de pedido al limpiar
```gherkin
Given existe una mesa "D4" en estado "CLEANING"
And la mesa tiene asignado el pedido con ID 456
When el personal marca la limpieza como finalizada
Then el sistema cambia el estado a "AVAILABLE"
And desvincula el pedido (currentOrderId = null)
```

### Scenario 5: Control de acceso por roles
```gherkin
Given existe un usuario con rol "KITCHEN_BAR"
And existe una mesa "E5" en estado "OCCUPIED"
When el usuario intenta cambiar el estado de la mesa
Then el sistema rechaza la operación
And retorna error 403 Forbidden
And informa que solo WAITER o ADMIN pueden gestionar mesas
```

---

## 🚀 Criterios de Completitud

### Funcionalidad Completa ✅
- [x] Cambio manual de estado de mesa
- [x] Validación de transiciones de estado
- [x] Verificación de pedido activo para OCCUPIED
- [x] Desvinculación de pedido al volver a AVAILABLE
- [x] Control de acceso por roles (WAITER, ADMIN)
- [x] Registro de timestamp de cambio de estado

### Calidad de Código ✅
- [x] Tests unitarios con cobertura ≥ 70%
- [x] Tests de integración end-to-end
- [x] Arquitectura hexagonal respetada
- [x] Sin dependencias invertidas
- [x] Excepciones de negocio bien definidas
- [x] Validaciones en capa de dominio

### Documentación ✅
- [x] Colección Postman actualizada
- [x] Diagramas de flujo de estados
- [x] Ejemplos de uso
- [x] Documentación de errores

---

## 📝 Notas de Implementación

### Consideraciones Técnicas

1. **No automático**: A diferencia de HU-005, ningún evento del sistema debe cambiar estados automáticamente
2. **Pedido requerido**: La transición a OCCUPIED debe verificar la existencia de `currentOrderId`
3. **Limpieza de relaciones**: Al volver a AVAILABLE se debe desvincular el pedido
4. **Inmutabilidad**: `TableStateTransition` es un Value Object inmutable
5. **Validación en múltiples capas**: Domain valida transiciones, Application valida reglas de negocio, Infrastructure valida permisos

### Posibles Mejoras Futuras

- [ ] Agregar historial de cambios de estado (audit log)
- [ ] Notificaciones en tiempo real de cambios de estado
- [ ] Dashboard visual del estado de todas las mesas
- [ ] Validación opcional: verificar todas las tareas completadas antes de marcar SERVED
- [ ] Métricas: tiempo promedio en cada estado

---

## 🔗 Referencias

- **HU-005:** Gestión administrativa de mesas (configuración inicial)
- **HU-006:** Gestión del catálogo de productos
- **HU-001:** Procesar pedido de cocina (integración con pedidos)
- **AGENTS.md:** Guías de arquitectura hexagonal y TDD

---

**Estado del Plan:** ✅ Completo y listo para implementación  
**Última Actualización:** 2026-01-21  
**Responsable:** Equipo de Desarrollo FoodTech Kitchen
