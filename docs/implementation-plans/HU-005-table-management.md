# 📋 Plan de Implementación - HU-005: Gestión administrativa de mesas

**Historia de Usuario:** Gestión administrativa de mesas  
**Prioridad:** Alta  
**Estimación:** 2-3 días  
**Fecha de Creación:** 2026-01-21

---

## 📖 Resumen de la Historia de Usuario

**Como** administrador del restaurante  
**Quiero** registrar y consultar las mesas físicas del establecimiento  
**Para** configurar la infraestructura del salón antes de iniciar la operación diaria

> **Nota:** Esta historia cubre exclusivamente la **configuración inicial** y el inventario de mesas. El flujo operativo de servicio (cambios de estado por pedidos, limpieza y ocupación) se detalla en la **HU-007**.

---

## 🎯 Objetivos de Implementación

1. Permitir el registro de mesas físicas con identificador único y capacidad
2. Implementar validación de unicidad de números de mesa
3. Gestionar el estado inicial de mesas (siempre `AVAILABLE` al crear)
4. Implementar control de acceso basado en roles (solo ADMIN puede gestionar mesas)
5. Permitir consulta del catálogo completo de mesas
6. Validar que la capacidad de mesas sea un número entero positivo

---

## 🏗️ Arquitectura y Componentes

### Capas Involucradas (Hexagonal Architecture)

```
Domain Layer (Lógica de Negocio)
├── Table (Entity)
├── TableStatus (Enum)
└── TableValidator (Service)

Application Layer (Casos de Uso)
├── CreateTableUseCase
├── GetAllTablesUseCase
├── GetTableByIdUseCase
└── Exceptions
    ├── TableAlreadyExistsException
    └── TableNotFoundException

Infrastructure Layer (Adaptadores)
├── TableController (REST API)
├── TableRepositoryAdapter (JPA)
├── TableEntity (JPA)
├── TableJpaRepository (Spring Data)
├── TableMapper (DTO ↔ Domain)
└── TableEntityMapper (Domain ↔ JPA)
```

---

## 📦 Tareas de Implementación

### ✅ Fase 1: Domain Layer (Sin dependencias de framework)

#### Task 1.1: Crear TableStatus Enum
**Archivo:** `src/main/java/com/foodtech/kitchen/domain/model/TableStatus.java`

```java
package com.foodtech.kitchen.domain.model;

/**
 * Estados válidos para una mesa en el sistema.
 * 
 * Flujo de transición (para HU-007):
 * AVAILABLE → OCCUPIED → SERVED → CLEANING → AVAILABLE
 * 
 * Para HU-005 solo se usa AVAILABLE como estado inicial.
 */
public enum TableStatus {
    /**
     * Mesa disponible para nuevos clientes
     */
    AVAILABLE,
    
    /**
     * Mesa ocupada con clientes y pedido activo
     */
    OCCUPIED,
    
    /**
     * Mesa servida, esperando que los clientes se retiren
     */
    SERVED,
    
    /**
     * Mesa en proceso de limpieza
     */
    CLEANING;
    
    /**
     * Valida si la transición a otro estado es válida (para HU-007).
     * 
     * @param newStatus el estado destino
     * @return true si la transición es válida
     */
    public boolean canTransitionTo(TableStatus newStatus) {
        return switch (this) {
            case AVAILABLE -> newStatus == OCCUPIED;
            case OCCUPIED -> newStatus == SERVED;
            case SERVED -> newStatus == CLEANING;
            case CLEANING -> newStatus == AVAILABLE;
        };
    }
}
```

**Criterios de Aceptación:**
- Define los 4 estados: AVAILABLE, OCCUPIED, SERVED, CLEANING
- Incluye método `canTransitionTo()` para futuras validaciones (HU-007)
- Tests unitarios para validar transiciones válidas e inválidas

**Tests:** `src/test/java/com/foodtech/kitchen/domain/model/TableStatusTest.java`

---

#### Task 1.2: Crear Table Entity
**Archivo:** `src/main/java/com/foodtech/kitchen/domain/model/Table.java`

```java
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
```

**Criterios de Aceptación:**
- Constructor valida tableNumber (no null, no vacío)
- Constructor valida capacity (> 0)
- Mesa se crea con status AVAILABLE
- Mesa se crea con currentOrderId null
- Timestamps se establecen automáticamente
- Tests unitarios para todas las validaciones

**Tests:** `src/test/java/com/foodtech/kitchen/domain/model/TableTest.java`

---

#### Task 1.3: Crear TableValidator Service
**Archivo:** `src/main/java/com/foodtech/kitchen/domain/services/TableValidator.java`

```java
package com.foodtech.kitchen.domain.services;

import com.foodtech.kitchen.domain.model.Table;

/**
 * Servicio de dominio para validar mesas.
 * 
 * <p>NO contiene dependencias de Spring (puro dominio).</p>
 */
public class TableValidator {
    
    /**
     * Valida que una mesa cumpla con todas las reglas de negocio.
     * 
     * @param table la mesa a validar
     * @throws IllegalArgumentException si la mesa no es válida
     */
    public void validate(Table table) {
        if (table == null) {
            throw new IllegalArgumentException("Table cannot be null");
        }
        
        validateTableNumber(table.getTableNumber());
        validateCapacity(table.getCapacity());
    }
    
    /**
     * Valida que el número de mesa sea válido.
     * 
     * @param tableNumber el número a validar
     * @throws IllegalArgumentException si no es válido
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
     * Valida que la capacidad sea válida.
     * 
     * @param capacity la capacidad a validar
     * @throws IllegalArgumentException si no es válida
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
```

**Criterios de Aceptación:**
- Valida que la mesa no sea null
- Valida número de mesa (no null, no vacío, máx 10 caracteres)
- Valida capacidad (> 0, máx 50)
- Lanza IllegalArgumentException con mensajes descriptivos
- Tests unitarios para cada validación

**Tests:** `src/test/java/com/foodtech/kitchen/domain/services/TableValidatorTest.java`

---

### ✅ Fase 2: Application Layer (Puertos y Casos de Uso)

#### Task 2.1: Definir TableRepository Port
**Archivo:** `src/main/java/com/foodtech/kitchen/application/ports/out/TableRepository.java`

```java
package com.foodtech.kitchen.application.ports.out;

import com.foodtech.kitchen.domain.model.Table;
import com.foodtech.kitchen.domain.model.TableStatus;
import java.util.List;
import java.util.Optional;

/**
 * Puerto de salida para persistencia de mesas.
 * 
 * <p>Esta interfaz define el contrato que debe implementar
 * el adaptador de persistencia en la capa de infraestructura.</p>
 */
public interface TableRepository {
    
    /**
     * Guarda una nueva mesa o actualiza una existente.
     * 
     * @param table la mesa a guardar
     * @return la mesa guardada con su ID asignado
     */
    Table save(Table table);
    
    /**
     * Busca una mesa por su ID.
     * 
     * @param id el ID de la mesa
     * @return Optional con la mesa si existe
     */
    Optional<Table> findById(Long id);
    
    /**
     * Busca una mesa por su número.
     * 
     * @param tableNumber el número de la mesa
     * @return Optional con la mesa si existe
     */
    Optional<Table> findByTableNumber(String tableNumber);
    
    /**
     * Obtiene todas las mesas registradas.
     * 
     * @return lista de todas las mesas
     */
    List<Table> findAll();
    
    /**
     * Obtiene todas las mesas con un estado específico.
     * 
     * @param status el estado a filtrar
     * @return lista de mesas con ese estado
     */
    List<Table> findByStatus(TableStatus status);
    
    /**
     * Verifica si existe una mesa con el número dado.
     * 
     * @param tableNumber el número a verificar
     * @return true si existe una mesa con ese número
     */
    boolean existsByTableNumber(String tableNumber);
    
    /**
     * Elimina una mesa por su ID.
     * 
     * @param id el ID de la mesa a eliminar
     */
    void deleteById(Long id);
}
```

**Criterios de Aceptación:**
- Define todos los métodos necesarios para operaciones CRUD
- Incluye métodos para búsqueda por número y estado
- Documentación clara de cada método

---

#### Task 2.2: Crear Excepciones de Aplicación
**Archivo:** `src/main/java/com/foodtech/kitchen/application/exception/TableAlreadyExistsException.java`

```java
package com.foodtech.kitchen.application.exception;

/**
 * Excepción lanzada cuando se intenta crear una mesa con un número
 * que ya existe en el sistema.
 */
public class TableAlreadyExistsException extends RuntimeException {
    
    public TableAlreadyExistsException(String tableNumber) {
        super(String.format("Table with number '%s' already exists", tableNumber));
    }
}
```

**Archivo:** `src/main/java/com/foodtech/kitchen/application/exception/TableNotFoundException.java`

```java
package com.foodtech.kitchen.application.exception;

/**
 * Excepción lanzada cuando no se encuentra una mesa solicitada.
 */
public class TableNotFoundException extends RuntimeException {
    
    public TableNotFoundException(Long id) {
        super(String.format("Table not found with id: %d", id));
    }
    
    public TableNotFoundException(String tableNumber) {
        super(String.format("Table not found with number: %s", tableNumber));
    }
}
```

**Criterios de Aceptación:**
- Excepciones con mensajes descriptivos
- Constructores sobrecargados para diferentes tipos de búsqueda

---

#### Task 2.3: Crear CreateTableUseCase
**Archivo:** `src/main/java/com/foodtech/kitchen/application/usecases/CreateTableUseCase.java`

```java
package com.foodtech.kitchen.application.usecases;

import com.foodtech.kitchen.application.exception.TableAlreadyExistsException;
import com.foodtech.kitchen.application.ports.out.TableRepository;
import com.foodtech.kitchen.domain.model.Table;
import com.foodtech.kitchen.domain.services.TableValidator;

/**
 * Caso de uso para crear una nueva mesa en el sistema.
 * 
 * <p><strong>Responsabilidades:</strong></p>
 * <ul>
 *   <li>Validar los datos de la mesa</li>
 *   <li>Verificar que no exista una mesa con el mismo número</li>
 *   <li>Crear la mesa con estado AVAILABLE</li>
 *   <li>Persistir la mesa</li>
 * </ul>
 * 
 * <p><strong>Precondiciones:</strong></p>
 * <ul>
 *   <li>El número de mesa no debe existir en el sistema</li>
 *   <li>La capacidad debe ser mayor a 0</li>
 * </ul>
 */
public class CreateTableUseCase {
    
    private final TableRepository tableRepository;
    private final TableValidator tableValidator;
    
    public CreateTableUseCase(TableRepository tableRepository, TableValidator tableValidator) {
        this.tableRepository = tableRepository;
        this.tableValidator = tableValidator;
    }
    
    /**
     * Ejecuta el caso de uso de creación de mesa.
     * 
     * @param tableNumber el número identificador de la mesa
     * @param capacity la capacidad de comensales
     * @return la mesa creada con su ID asignado
     * @throws IllegalArgumentException si los datos no son válidos
     * @throws TableAlreadyExistsException si ya existe una mesa con ese número
     */
    public Table execute(String tableNumber, int capacity) {
        // Validar número de mesa y capacidad
        tableValidator.validateTableNumber(tableNumber);
        tableValidator.validateCapacity(capacity);
        
        // Verificar unicidad del número de mesa
        if (tableRepository.existsByTableNumber(tableNumber)) {
            throw new TableAlreadyExistsException(tableNumber);
        }
        
        // Crear nueva mesa (estado AVAILABLE por defecto)
        Table table = new Table(tableNumber, capacity);
        
        // Validar la mesa completa
        tableValidator.validate(table);
        
        // Persistir y retornar
        return tableRepository.save(table);
    }
}
```

**Criterios de Aceptación:**
- Valida tableNumber y capacity antes de crear
- Verifica que no exista mesa duplicada
- Lanza TableAlreadyExistsException si existe
- Retorna mesa con ID asignado
- Tests unitarios para caso exitoso y duplicado

**Tests:** `src/test/java/com/foodtech/kitchen/application/usecases/CreateTableUseCaseTest.java`

---

#### Task 2.4: Crear GetAllTablesUseCase
**Archivo:** `src/main/java/com/foodtech/kitchen/application/usecases/GetAllTablesUseCase.java`

```java
package com.foodtech.kitchen.application.usecases;

import com.foodtech.kitchen.application.ports.out.TableRepository;
import com.foodtech.kitchen.domain.model.Table;
import java.util.List;

/**
 * Caso de uso para obtener todas las mesas del sistema.
 * 
 * <p>Este caso de uso está destinado principalmente para uso administrativo,
 * permitiendo visualizar el inventario completo de mesas.</p>
 */
public class GetAllTablesUseCase {
    
    private final TableRepository tableRepository;
    
    public GetAllTablesUseCase(TableRepository tableRepository) {
        this.tableRepository = tableRepository;
    }
    
    /**
     * Ejecuta el caso de uso para obtener todas las mesas.
     * 
     * @return lista de todas las mesas registradas
     */
    public List<Table> execute() {
        return tableRepository.findAll();
    }
}
```

**Criterios de Aceptación:**
- Retorna todas las mesas del sistema
- Lista vacía si no hay mesas
- Tests unitarios para casos con y sin mesas

**Tests:** `src/test/java/com/foodtech/kitchen/application/usecases/GetAllTablesUseCaseTest.java`

---

#### Task 2.5: Crear GetTableByIdUseCase
**Archivo:** `src/main/java/com/foodtech/kitchen/application/usecases/GetTableByIdUseCase.java`

```java
package com.foodtech.kitchen.application.usecases;

import com.foodtech.kitchen.application.exception.TableNotFoundException;
import com.foodtech.kitchen.application.ports.out.TableRepository;
import com.foodtech.kitchen.domain.model.Table;

/**
 * Caso de uso para obtener una mesa por su ID.
 */
public class GetTableByIdUseCase {
    
    private final TableRepository tableRepository;
    
    public GetTableByIdUseCase(TableRepository tableRepository) {
        this.tableRepository = tableRepository;
    }
    
    /**
     * Ejecuta el caso de uso para buscar una mesa por ID.
     * 
     * @param id el ID de la mesa
     * @return la mesa encontrada
     * @throws TableNotFoundException si no existe la mesa
     */
    public Table execute(Long id) {
        return tableRepository.findById(id)
            .orElseThrow(() -> new TableNotFoundException(id));
    }
}
```

**Criterios de Aceptación:**
- Retorna la mesa si existe
- Lanza TableNotFoundException si no existe
- Tests unitarios para ambos casos

**Tests:** `src/test/java/com/foodtech/kitchen/application/usecases/GetTableByIdUseCaseTest.java`

---

### ✅ Fase 3: Infrastructure Layer (Adaptadores y REST API)

#### Task 3.1: Crear TableEntity (JPA)
**Archivo:** `src/main/java/com/foodtech/kitchen/infrastructure/persistence/jpa/entities/TableEntity.java`

```java
package com.foodtech.kitchen.infrastructure.persistence.jpa.entities;

import com.foodtech.kitchen.domain.model.TableStatus;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entidad JPA que representa una mesa en la base de datos.
 */
@Entity
@jakarta.persistence.Table(name = "tables")
public class TableEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "table_number", nullable = false, unique = true, length = 10)
    private String tableNumber;
    
    @Column(name = "capacity", nullable = false)
    private Integer capacity;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private TableStatus status;
    
    @Column(name = "current_order_id")
    private Long currentOrderId;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (this.status == null) {
            this.status = TableStatus.AVAILABLE;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
    
    // Constructor vacío para JPA
    public TableEntity() {}
    
    // Getters y setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getTableNumber() { return tableNumber; }
    public void setTableNumber(String tableNumber) { this.tableNumber = tableNumber; }
    
    public Integer getCapacity() { return capacity; }
    public void setCapacity(Integer capacity) { this.capacity = capacity; }
    
    public TableStatus getStatus() { return status; }
    public void setStatus(TableStatus status) { this.status = status; }
    
    public Long getCurrentOrderId() { return currentOrderId; }
    public void setCurrentOrderId(Long currentOrderId) { this.currentOrderId = currentOrderId; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
```

**Criterios de Aceptación:**
- Anotaciones JPA correctas
- tableNumber con constraint UNIQUE
- Status con valor por defecto AVAILABLE
- Callbacks @PrePersist y @PreUpdate para timestamps
- Tests de persistencia

**Tests:** `src/test/java/com/foodtech/kitchen/infrastructure/persistence/jpa/entities/TableEntityTest.java`

---

#### Task 3.2: Crear TableJpaRepository
**Archivo:** `src/main/java/com/foodtech/kitchen/infrastructure/persistence/jpa/TableJpaRepository.java`

```java
package com.foodtech.kitchen.infrastructure.persistence.jpa;

import com.foodtech.kitchen.domain.model.TableStatus;
import com.foodtech.kitchen.infrastructure.persistence.jpa.entities.TableEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio JPA para operaciones de persistencia de mesas.
 */
@Repository
public interface TableJpaRepository extends JpaRepository<TableEntity, Long> {
    
    /**
     * Busca una mesa por su número.
     * 
     * @param tableNumber el número de la mesa
     * @return Optional con la entidad si existe
     */
    Optional<TableEntity> findByTableNumber(String tableNumber);
    
    /**
     * Verifica si existe una mesa con el número dado.
     * 
     * @param tableNumber el número a verificar
     * @return true si existe
     */
    boolean existsByTableNumber(String tableNumber);
    
    /**
     * Obtiene todas las mesas con un estado específico.
     * 
     * @param status el estado a filtrar
     * @return lista de entidades con ese estado
     */
    List<TableEntity> findByStatus(TableStatus status);
}
```

**Criterios de Aceptación:**
- Extiende JpaRepository
- Query methods correctos
- Tests de integración con base de datos en memoria

**Tests:** `src/test/java/com/foodtech/kitchen/infrastructure/persistence/jpa/TableJpaRepositoryTest.java`

---

#### Task 3.3: Crear TableEntityMapper
**Archivo:** `src/main/java/com/foodtech/kitchen/infrastructure/persistence/mappers/TableEntityMapper.java`

```java
package com.foodtech.kitchen.infrastructure.persistence.mappers;

import com.foodtech.kitchen.domain.model.Table;
import com.foodtech.kitchen.infrastructure.persistence.jpa.entities.TableEntity;

/**
 * Mapper para convertir entre Table (dominio) y TableEntity (JPA).
 * 
 * <p>NO contiene lógica de negocio, solo transformación de datos.</p>
 */
public class TableEntityMapper {
    
    /**
     * Convierte una Table de dominio a TableEntity de JPA.
     * 
     * @param table la mesa de dominio
     * @return la entidad JPA correspondiente
     */
    public TableEntity toEntity(Table table) {
        if (table == null) {
            return null;
        }
        
        TableEntity entity = new TableEntity();
        entity.setId(table.getId());
        entity.setTableNumber(table.getTableNumber());
        entity.setCapacity(table.getCapacity());
        entity.setStatus(table.getStatus());
        entity.setCurrentOrderId(table.getCurrentOrderId());
        entity.setCreatedAt(table.getCreatedAt());
        entity.setUpdatedAt(table.getUpdatedAt());
        
        return entity;
    }
    
    /**
     * Convierte una TableEntity de JPA a Table de dominio.
     * 
     * @param entity la entidad JPA
     * @return la mesa de dominio correspondiente
     */
    public Table toDomain(TableEntity entity) {
        if (entity == null) {
            return null;
        }
        
        return new Table(
            entity.getId(),
            entity.getTableNumber(),
            entity.getCapacity(),
            entity.getStatus(),
            entity.getCurrentOrderId(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }
}
```

**Criterios de Aceptación:**
- Conversión bidireccional completa
- Manejo de nulls
- Tests unitarios para ambas direcciones

**Tests:** `src/test/java/com/foodtech/kitchen/infrastructure/persistence/mappers/TableEntityMapperTest.java`

---

#### Task 3.4: Crear TableRepositoryAdapter
**Archivo:** `src/main/java/com/foodtech/kitchen/infrastructure/persistence/adapters/TableRepositoryAdapter.java`

```java
package com.foodtech.kitchen.infrastructure.persistence.adapters;

import com.foodtech.kitchen.application.ports.out.TableRepository;
import com.foodtech.kitchen.domain.model.Table;
import com.foodtech.kitchen.domain.model.TableStatus;
import com.foodtech.kitchen.infrastructure.persistence.jpa.TableJpaRepository;
import com.foodtech.kitchen.infrastructure.persistence.jpa.entities.TableEntity;
import com.foodtech.kitchen.infrastructure.persistence.mappers.TableEntityMapper;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Adaptador que implementa el puerto TableRepository usando Spring Data JPA.
 */
@Component
public class TableRepositoryAdapter implements TableRepository {
    
    private final TableJpaRepository jpaRepository;
    private final TableEntityMapper mapper;
    
    public TableRepositoryAdapter(TableJpaRepository jpaRepository, TableEntityMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }
    
    @Override
    public Table save(Table table) {
        TableEntity entity = mapper.toEntity(table);
        TableEntity savedEntity = jpaRepository.save(entity);
        return mapper.toDomain(savedEntity);
    }
    
    @Override
    public Optional<Table> findById(Long id) {
        return jpaRepository.findById(id)
            .map(mapper::toDomain);
    }
    
    @Override
    public Optional<Table> findByTableNumber(String tableNumber) {
        return jpaRepository.findByTableNumber(tableNumber)
            .map(mapper::toDomain);
    }
    
    @Override
    public List<Table> findAll() {
        return jpaRepository.findAll().stream()
            .map(mapper::toDomain)
            .collect(Collectors.toList());
    }
    
    @Override
    public List<Table> findByStatus(TableStatus status) {
        return jpaRepository.findByStatus(status).stream()
            .map(mapper::toDomain)
            .collect(Collectors.toList());
    }
    
    @Override
    public boolean existsByTableNumber(String tableNumber) {
        return jpaRepository.existsByTableNumber(tableNumber);
    }
    
    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }
}
```

**Criterios de Aceptación:**
- Implementa todos los métodos del puerto
- Usa mapper para conversiones
- Tests de integración con base de datos

**Tests:** `src/test/java/com/foodtech/kitchen/infrastructure/persistence/adapters/TableRepositoryAdapterTest.java`

---

#### Task 3.5: Crear DTOs para REST API
**Archivo:** `src/main/java/com/foodtech/kitchen/infrastructure/rest/dto/CreateTableRequest.java`

```java
package com.foodtech.kitchen.infrastructure.rest.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO para solicitud de creación de mesa.
 */
public class CreateTableRequest {
    
    @JsonProperty("tableNumber")
    private String tableNumber;
    
    @JsonProperty("capacity")
    private Integer capacity;
    
    // Constructor vacío para Jackson
    public CreateTableRequest() {}
    
    public CreateTableRequest(String tableNumber, Integer capacity) {
        this.tableNumber = tableNumber;
        this.capacity = capacity;
    }
    
    // Getters y setters
    public String getTableNumber() { return tableNumber; }
    public void setTableNumber(String tableNumber) { this.tableNumber = tableNumber; }
    
    public Integer getCapacity() { return capacity; }
    public void setCapacity(Integer capacity) { this.capacity = capacity; }
}
```

**Archivo:** `src/main/java/com/foodtech/kitchen/infrastructure/rest/dto/TableResponse.java`

```java
package com.foodtech.kitchen.infrastructure.rest.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.foodtech.kitchen.domain.model.TableStatus;
import java.time.LocalDateTime;

/**
 * DTO para respuesta de mesa.
 */
public class TableResponse {
    
    @JsonProperty("id")
    private Long id;
    
    @JsonProperty("tableNumber")
    private String tableNumber;
    
    @JsonProperty("capacity")
    private Integer capacity;
    
    @JsonProperty("status")
    private TableStatus status;
    
    @JsonProperty("currentOrderId")
    private Long currentOrderId;
    
    @JsonProperty("createdAt")
    private LocalDateTime createdAt;
    
    @JsonProperty("updatedAt")
    private LocalDateTime updatedAt;
    
    // Constructor vacío
    public TableResponse() {}
    
    // Constructor completo
    public TableResponse(Long id, String tableNumber, Integer capacity, TableStatus status,
                        Long currentOrderId, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.tableNumber = tableNumber;
        this.capacity = capacity;
        this.status = status;
        this.currentOrderId = currentOrderId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
    
    // Getters y setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getTableNumber() { return tableNumber; }
    public void setTableNumber(String tableNumber) { this.tableNumber = tableNumber; }
    
    public Integer getCapacity() { return capacity; }
    public void setCapacity(Integer capacity) { this.capacity = capacity; }
    
    public TableStatus getStatus() { return status; }
    public void setStatus(TableStatus status) { this.status = status; }
    
    public Long getCurrentOrderId() { return currentOrderId; }
    public void setCurrentOrderId(Long currentOrderId) { this.currentOrderId = currentOrderId; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
```

**Criterios de Aceptación:**
- Anotaciones Jackson para serialización JSON
- Campos con nombres en camelCase
- Constructores vacíos para deserialización

---

#### Task 3.6: Crear TableMapper (DTO ↔ Domain)
**Archivo:** `src/main/java/com/foodtech/kitchen/infrastructure/rest/mapper/TableMapper.java`

```java
package com.foodtech.kitchen.infrastructure.rest.mapper;

import com.foodtech.kitchen.domain.model.Table;
import com.foodtech.kitchen.infrastructure.rest.dto.TableResponse;

/**
 * Mapper para convertir entre objetos de dominio y DTOs REST.
 */
public class TableMapper {
    
    /**
     * Convierte una Table de dominio a TableResponse DTO.
     * 
     * @param table la mesa de dominio
     * @return el DTO de respuesta
     */
    public static TableResponse toResponse(Table table) {
        if (table == null) {
            return null;
        }
        
        return new TableResponse(
            table.getId(),
            table.getTableNumber(),
            table.getCapacity(),
            table.getStatus(),
            table.getCurrentOrderId(),
            table.getCreatedAt(),
            table.getUpdatedAt()
        );
    }
}
```

**Criterios de Aceptación:**
- Conversión completa de dominio a DTO
- Manejo de nulls
- Tests unitarios

**Tests:** `src/test/java/com/foodtech/kitchen/infrastructure/rest/mapper/TableMapperTest.java`

---

#### Task 3.7: Crear TableController
**Archivo:** `src/main/java/com/foodtech/kitchen/infrastructure/rest/TableController.java`

```java
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
 * Controlador REST para gestión administrativa de mesas.
 * 
 * <p><strong>Endpoints:</strong></p>
 * <ul>
 *   <li>POST /api/tables - Crear nueva mesa (ADMIN only)</li>
 *   <li>GET /api/tables - Listar todas las mesas (ADMIN only)</li>
 *   <li>GET /api/tables/{id} - Obtener mesa por ID (ADMIN only)</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/tables")
public class TableController {
    
    private final CreateTableUseCase createTableUseCase;
    private final GetAllTablesUseCase getAllTablesUseCase;
    private final GetTableByIdUseCase getTableByIdUseCase;
    
    public TableController(CreateTableUseCase createTableUseCase,
                          GetAllTablesUseCase getAllTablesUseCase,
                          GetTableByIdUseCase getTableByIdUseCase) {
        this.createTableUseCase = createTableUseCase;
        this.getAllTablesUseCase = getAllTablesUseCase;
        this.getTableByIdUseCase = getTableByIdUseCase;
    }
    
    /**
     * Crea una nueva mesa en el sistema.
     * 
     * <p><strong>Requiere rol:</strong> ADMIN</p>
     * 
     * @param request datos de la mesa a crear
     * @return 201 Created con la mesa creada
     */
    @PostMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<TableResponse> createTable(@RequestBody CreateTableRequest request) {
        Table table = createTableUseCase.execute(request.getTableNumber(), request.getCapacity());
        TableResponse response = TableMapper.toResponse(table);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    /**
     * Obtiene todas las mesas del sistema.
     * 
     * <p><strong>Requiere rol:</strong> ADMIN</p>
     * 
     * @return 200 OK con lista de mesas
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
     * Obtiene una mesa por su ID.
     * 
     * <p><strong>Requiere rol:</strong> ADMIN</p>
     * 
     * @param id el ID de la mesa
     * @return 200 OK con la mesa encontrada
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<TableResponse> getTableById(@PathVariable Long id) {
        Table table = getTableByIdUseCase.execute(id);
        TableResponse response = TableMapper.toResponse(table);
        return ResponseEntity.ok(response);
    }
}
```

**Criterios de Aceptación:**
- Endpoints REST correctos (POST, GET)
- Anotación @PreAuthorize con rol ADMIN
- Códigos de estado HTTP apropiados (201, 200)
- Validación de request body
- Tests de integración

**Tests:** `src/test/java/com/foodtech/kitchen/infrastructure/rest/TableControllerIntegrationTest.java`

---

#### Task 3.8: Actualizar GlobalExceptionHandler
**Archivo:** `src/main/java/com/foodtech/kitchen/infrastructure/rest/exception/GlobalExceptionHandler.java`

Agregar manejadores para las nuevas excepciones:

```java
@ExceptionHandler(TableAlreadyExistsException.class)
public ResponseEntity<ErrorResponse> handleTableAlreadyExists(TableAlreadyExistsException ex) {
    ErrorResponse error = new ErrorResponse(
        ex.getMessage(),
        "Table number already exists",
        HttpStatus.CONFLICT.value()
    );
    return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
}

@ExceptionHandler(TableNotFoundException.class)
public ResponseEntity<ErrorResponse> handleTableNotFound(TableNotFoundException ex) {
    ErrorResponse error = new ErrorResponse(
        ex.getMessage(),
        "Table not found",
        HttpStatus.NOT_FOUND.value()
    );
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
}
```

**Criterios de Aceptación:**
- HTTP 409 Conflict para mesa duplicada
- HTTP 404 Not Found para mesa no encontrada
- Mensajes descriptivos en ErrorResponse

---

#### Task 3.9: Configurar Beans de Aplicación
**Archivo:** `src/main/java/com/foodtech/kitchen/infrastructure/config/ApplicationConfig.java`

Agregar beans para casos de uso de mesas:

```java
@Bean
public TableValidator tableValidator() {
    return new TableValidator();
}

@Bean
public TableEntityMapper tableEntityMapper() {
    return new TableEntityMapper();
}

@Bean
public CreateTableUseCase createTableUseCase(TableRepository tableRepository, TableValidator tableValidator) {
    return new CreateTableUseCase(tableRepository, tableValidator);
}

@Bean
public GetAllTablesUseCase getAllTablesUseCase(TableRepository tableRepository) {
    return new GetAllTablesUseCase(tableRepository);
}

@Bean
public GetTableByIdUseCase getTableByIdUseCase(TableRepository tableRepository) {
    return new GetTableByIdUseCase(tableRepository);
}
```

**Criterios de Aceptación:**
- Beans correctamente configurados
- Inyección de dependencias funcional

---

### ✅ Fase 4: Testing

#### Task 4.1: Tests Unitarios de Domain Layer

**Tests a crear:**
- `TableStatusTest.java` - Validar transiciones de estado
- `TableTest.java` - Validar constructores y reglas de negocio
- `TableValidatorTest.java` - Validar todas las reglas de validación

**Escenarios de prueba:**
- ✅ Mesa se crea con estado AVAILABLE
- ✅ Mesa se crea con currentOrderId null
- ✅ Timestamps se establecen automáticamente
- ✅ Validación de tableNumber (null, vacío, muy largo)
- ✅ Validación de capacity (0, negativo, muy grande)
- ✅ Transiciones de estado válidas e inválidas

---

#### Task 4.2: Tests Unitarios de Application Layer

**Tests a crear:**
- `CreateTableUseCaseTest.java` - Caso exitoso y mesa duplicada
- `GetAllTablesUseCaseTest.java` - Con y sin mesas
- `GetTableByIdUseCaseTest.java` - Mesa encontrada y no encontrada

**Escenarios de prueba:**
- ✅ Crear mesa válida retorna mesa con ID
- ✅ Crear mesa duplicada lanza TableAlreadyExistsException
- ✅ Obtener todas las mesas retorna lista correcta
- ✅ Obtener mesa por ID existente retorna mesa
- ✅ Obtener mesa por ID inexistente lanza TableNotFoundException

---

#### Task 4.3: Tests de Infrastructure Layer

**Tests a crear:**
- `TableEntityTest.java` - Callbacks JPA funcionan
- `TableJpaRepositoryTest.java` - Query methods con H2
- `TableRepositoryAdapterTest.java` - Integración repositorio
- `TableEntityMapperTest.java` - Conversiones bidireccionales
- `TableMapperTest.java` - Conversión a DTO

---

#### Task 4.4: Tests de Integración REST

**Archivo:** `src/test/java/com/foodtech/kitchen/infrastructure/rest/TableControllerIntegrationTest.java`

**Escenarios de prueba (según HU-005):**

1. **Escenario 1: Registro exitoso de una nueva mesa**
   - POST /api/tables con rol ADMIN
   - Body: `{"tableNumber": "A1", "capacity": 4}`
   - Expect: 201 Created
   - Verify: status = AVAILABLE, currentOrderId = null

2. **Escenario 2: Restricción de números duplicados**
   - POST /api/tables con "B3" dos veces
   - Expect: segunda llamada retorna 409 Conflict

3. **Escenario 3: Validación de capacidad inválida**
   - POST /api/tables con capacity = 0
   - Expect: 400 Bad Request

4. **Escenario 4: Consulta del catálogo de mesas**
   - GET /api/tables con rol ADMIN
   - Expect: 200 OK con lista de 5 mesas
   - Verify: cada mesa tiene número, capacidad, estado, fecha creación

**Seguridad:**
- Verificar que sin autenticación retorna 401
- Verificar que WAITER no puede acceder (403)
- Verificar que KITCHEN_* no puede acceder (403)

---

### ✅ Fase 5: Documentación

#### Task 5.1: Actualizar README
- Agregar sección sobre gestión de mesas
- Documentar endpoints REST
- Incluir ejemplos de requests/responses

#### Task 5.2: Crear Colección Postman
**Archivo:** `docs/postman_collections/HU-005-table-management.postman_collection.json`

**Requests a incluir:**
- POST Create Table (ADMIN)
- GET All Tables (ADMIN)
- GET Table by ID (ADMIN)
- POST Create Duplicate Table (error case)
- POST Create Invalid Table (error case)

#### Task 5.3: Crear Migration SQL
**Archivo:** `src/main/resources/db/migration/V3__create_tables_table.sql`

```sql
CREATE TABLE tables (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    table_number VARCHAR(10) NOT NULL UNIQUE,
    capacity INT NOT NULL CHECK (capacity > 0),
    status VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE',
    current_order_id BIGINT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_tables_current_order FOREIGN KEY (current_order_id) REFERENCES orders(id) ON DELETE SET NULL
);

CREATE INDEX idx_tables_status ON tables(status);
CREATE INDEX idx_tables_table_number ON tables(table_number);
```

---

## 📊 Criterios de Aceptación Globales

### Funcionalidad
- ✅ Administrador puede crear mesas con número único
- ✅ Capacidad debe ser > 0
- ✅ Mesa se crea con estado AVAILABLE
- ✅ Mesa se crea sin pedido asociado
- ✅ No se permiten números duplicados
- ✅ Solo ADMIN puede gestionar mesas
- ✅ Se puede consultar listado completo de mesas

### Arquitectura
- ✅ Hexagonal Architecture respetada
- ✅ Domain sin dependencias de framework
- ✅ SRP respetado en todos los componentes
- ✅ Dependency Inversion aplicada

### Testing
- ✅ Cobertura mínima 70%
- ✅ Tests unitarios para domain y application
- ✅ Tests de integración para infrastructure
- ✅ Tests de seguridad para endpoints

### Calidad de Código
- ✅ Nombres descriptivos
- ✅ Métodos < 20 líneas
- ✅ Excepciones con mensajes claros
- ✅ Documentación Javadoc completa

---

## 🚀 Orden de Implementación Recomendado

### Día 1 (4-5 horas)
1. Fase 1: Domain Layer (Tasks 1.1 - 1.3)
2. Fase 2: Application Layer (Tasks 2.1 - 2.5)

### Día 2 (4-5 horas)
3. Fase 3: Infrastructure Layer (Tasks 3.1 - 3.9)
4. Configuración de seguridad y beans

### Día 3 (3-4 horas)
5. Fase 4: Testing completo (Tasks 4.1 - 4.4)
6. Fase 5: Documentación y migraciones

---

## 🔗 Dependencias con Otras Historias

### Dependencias Directas
- **HU-004** (Autenticación) - Requerida para control de acceso por roles

### Historias Relacionadas
- **HU-007** (Ciclo de vida de mesas) - Extiende esta funcionalidad con cambios de estado
- **HU-001** (Procesar pedido) - Futura integración con assignOrder()

---

## 📝 Notas Adicionales

### Consideraciones de Seguridad
- Solo rol ADMIN puede crear y consultar mesas
- Validar autenticación en todos los endpoints
- No exponer información sensible en errores

### Consideraciones de Performance
- Índice en table_number para búsquedas rápidas
- Índice en status para futuras consultas (HU-007)

### Extensibilidad
- Campo currentOrderId preparado para HU-007
- Métodos de cambio de estado listos para HU-007
- Validación de transiciones implementada

---

## ✅ Checklist Final

Antes de marcar la historia como completa, verificar:

- [ ] Todos los tests pasan (./gradlew test)
- [ ] Cobertura >= 70% (./gradlew jacocoTestReport)
- [ ] Build exitoso sin warnings (./gradlew clean build)
- [ ] Endpoints REST funcionan con Postman
- [ ] Autenticación y autorización funcionan correctamente
- [ ] Excepciones manejan todos los casos de error
- [ ] Documentación actualizada (README, Postman)
- [ ] Migraciones SQL ejecutan correctamente
- [ ] Código revisado según AGENTS.md
- [ ] Pre-commit checklist completado

---

**Fin del Plan de Implementación HU-005**
