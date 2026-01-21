# 📋 Plan de Implementación - HU-006: Gestión del catálogo de productos

**Historia de Usuario:** Gestión del catálogo de productos  
**Prioridad:** Alta  
**Estimación:** 3-5 días  
**Fecha de Creación:** 2026-01-21

---

## 📖 Resumen de la Historia de Usuario

**Como** administrador del restaurante  
**Quiero** gestionar el catálogo de productos (platillos y bebidas) del menú  
**Para** mantener actualizada la oferta disponible, sus precios y disponibilidad

---

## 🎯 Objetivos de Implementación

1. Permitir operaciones CRUD completas sobre productos del catálogo
2. Implementar validación de unicidad de nombres de productos
3. Gestionar disponibilidad de productos (habilitación/deshabilitación)
4. Implementar control de acceso basado en roles (solo ADMIN puede modificar)
5. Permitir consultas filtradas por tipo de producto y disponibilidad
6. Incluir precio en todas las operaciones de productos

---

## 🏗️ Arquitectura y Componentes

### Capas Involucradas (Hexagonal Architecture)

```
Domain Layer (Lógica de Negocio)
├── Product (Entity)
├── ProductValidator (Service)
└── Price (Value Object)

Application Layer (Casos de Uso)
├── CreateProductUseCase
├── UpdateProductUseCase
├── DeleteProductUseCase
├── GetProductsUseCase
├── GetProductByIdUseCase
└── UpdateProductAvailabilityUseCase

Infrastructure Layer (Adaptadores)
├── ProductController (REST API)
├── ProductRepositoryAdapter (JPA)
├── ProductEntity (JPA)
├── ProductMapper (DTO ↔ Domain)
└── ProductEntityMapper (Domain ↔ JPA)
```

---

## 📦 Tareas de Implementación

### ✅ Fase 1: Domain Layer (Sin dependencias de framework)

#### Task 1.1: Crear Value Object Price
**Archivo:** `domain/model/Price.java`

```java
public class Price {
    private final BigDecimal amount;
    
    public Price(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Price cannot be null or negative");
        }
        this.amount = amount.setScale(2, RoundingMode.HALF_UP);
    }
    
    // Getters, equals, hashCode
}
```

**Criterios de Aceptación:**
- El precio no puede ser null
- El precio no puede ser negativo
- El precio siempre tiene 2 decimales
- Tests unitarios que validen las reglas

#### Task 1.2: Extender Product Entity con campos adicionales
**Archivo:** `domain/model/Product.java`

**Campos a Agregar:**
- `Long id` (para persistencia)
- `String description` (descripción del producto)
- `Price price` (precio usando el Value Object)
- `int preparationTimeSeconds` (tiempo de preparación)
- `boolean available` (disponibilidad)
- `LocalDateTime createdAt` (fecha de creación)
- `LocalDateTime updatedAt` (fecha de última actualización)

**Validaciones:**
- Nombre no puede ser null ni vacío
- Descripción no puede ser null (puede estar vacía)
- Tipo debe ser válido (DRINK, HOT_DISH, COLD_DISH)
- Tiempo de preparación debe ser mayor a 0

**Criterios de Aceptación:**
- Constructor valida todos los campos obligatorios
- Tests unitarios para validaciones
- Producto se crea con `available = true` por defecto
- `createdAt` se establece automáticamente en el constructor

#### Task 1.3: Crear ProductValidator Service
**Archivo:** `domain/services/ProductValidator.java`

```java
public class ProductValidator {
    
    public void validate(Product product) {
        validateName(product.getName());
        validatePrice(product.getPrice());
        validatePreparationTime(product.getPreparationTimeSeconds());
    }
    
    public void validateName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Product name cannot be null or empty");
        }
        if (name.length() > 100) {
            throw new IllegalArgumentException("Product name cannot exceed 100 characters");
        }
    }
    
    // Otras validaciones...
}
```

**Criterios de Aceptación:**
- Valida nombre, precio y tiempo de preparación
- Lanza IllegalArgumentException con mensajes descriptivos
- Tests unitarios para cada validación

---

### ✅ Fase 2: Application Layer (Puertos y Casos de Uso)

#### Task 2.1: Definir ProductRepository Port
**Archivo:** `application/ports/out/ProductRepository.java`

```java
public interface ProductRepository {
    Product save(Product product);
    Optional<Product> findById(Long id);
    List<Product> findAll();
    List<Product> findByAvailable(boolean available);
    List<Product> findByType(ProductType type);
    Optional<Product> findByName(String name);
    void deleteById(Long id);
    boolean existsByName(String name);
}
```

#### Task 2.2: Crear CreateProductUseCase
**Archivo:** `application/usecases/CreateProductUseCase.java`

**Responsabilidades:**
- Validar datos del producto
- Verificar que no exista un producto con el mismo nombre
- Guardar el producto con `available = true` por defecto
- Retornar el producto creado con su ID asignado

**Criterios de Aceptación:**
- Lanza `ProductAlreadyExistsException` si el nombre está duplicado
- Producto se guarda con estado disponible por defecto
- Tests unitarios que validen duplicados y guardado exitoso

#### Task 2.3: Crear UpdateProductUseCase
**Archivo:** `application/usecases/UpdateProductUseCase.java`

**Responsabilidades:**
- Verificar que el producto existe
- Validar nuevos datos
- Actualizar campos modificables (descripción, precio, tiempo de preparación)
- Actualizar `updatedAt`
- Retornar producto actualizado

**Campos No Modificables:**
- `id`
- `name` (para evitar duplicados, se maneja en caso de uso separado)
- `type` (no debe cambiar después de creación)
- `createdAt`

**Criterios de Aceptación:**
- Lanza `ProductNotFoundException` si no existe
- Actualiza `updatedAt` automáticamente
- Tests para actualización exitosa y producto no encontrado

#### Task 2.4: Crear UpdateProductAvailabilityUseCase
**Archivo:** `application/usecases/UpdateProductAvailabilityUseCase.java`

**Responsabilidades:**
- Cambiar disponibilidad del producto (true/false)
- Actualizar `updatedAt`

**Criterios de Aceptación:**
- Permite habilitar/deshabilitar productos
- Tests para ambos escenarios

#### Task 2.5: Crear DeleteProductUseCase
**Archivo:** `application/usecases/DeleteProductUseCase.java`

**Responsabilidades:**
- Verificar que el producto existe
- Eliminar el producto permanentemente

**Criterios de Aceptación:**
- Lanza `ProductNotFoundException` si no existe
- Tests para eliminación exitosa

#### Task 2.6: Crear GetProductsUseCase
**Archivo:** `application/usecases/GetProductsUseCase.java`

**Responsabilidades:**
- Obtener todos los productos (con filtros opcionales)
- Filtrar por disponibilidad
- Filtrar por tipo

**Criterios de Aceptación:**
- Sin filtros, retorna todos los productos
- Con filtro de disponibilidad, retorna solo disponibles/no disponibles
- Con filtro de tipo, retorna solo de ese tipo
- Tests para todos los escenarios de filtrado

#### Task 2.7: Crear GetProductByIdUseCase
**Archivo:** `application/usecases/GetProductByIdUseCase.java`

**Responsabilidades:**
- Buscar producto por ID
- Lanzar excepción si no existe

**Criterios de Aceptación:**
- Lanza `ProductNotFoundException` si no existe
- Retorna producto completo si existe

#### Task 2.8: Crear Excepciones Personalizadas
**Archivo:** `application/exceptions/`

```java
// ProductNotFoundException.java
public class ProductNotFoundException extends RuntimeException {
    public ProductNotFoundException(Long productId) {
        super("Product not found with id: " + productId);
    }
}

// ProductAlreadyExistsException.java
public class ProductAlreadyExistsException extends RuntimeException {
    public ProductAlreadyExistsException(String name) {
        super("Product already exists with name: " + name);
    }
}
```

---

### ✅ Fase 3: Infrastructure Layer (Adaptadores JPA y REST)

#### Task 3.1: Crear ProductEntity (JPA)
**Archivo:** `infrastructure/persistence/entity/ProductEntity.java`

```java
@Entity
@Table(name = "products")
public class ProductEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true, length = 100)
    private String name;
    
    @Column(nullable = false, length = 500)
    private String description;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductType type;
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;
    
    @Column(nullable = false)
    private Integer preparationTimeSeconds;
    
    @Column(nullable = false)
    private Boolean available;
    
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
```

#### Task 3.2: Crear ProductJpaRepository
**Archivo:** `infrastructure/persistence/repository/ProductJpaRepository.java`

```java
@Repository
public interface ProductJpaRepository extends JpaRepository<ProductEntity, Long> {
    Optional<ProductEntity> findByName(String name);
    List<ProductEntity> findByAvailable(Boolean available);
    List<ProductEntity> findByType(ProductType type);
    boolean existsByName(String name);
}
```

#### Task 3.3: Crear ProductEntityMapper
**Archivo:** `infrastructure/persistence/mapper/ProductEntityMapper.java`

```java
@Component
public class ProductEntityMapper {
    
    public ProductEntity toEntity(Product product) {
        // Domain → JPA Entity
    }
    
    public Product toDomain(ProductEntity entity) {
        // JPA Entity → Domain
    }
    
    public List<Product> toDomainList(List<ProductEntity> entities) {
        // Lista de entities → Lista de domain
    }
}
```

#### Task 3.4: Crear ProductRepositoryAdapter
**Archivo:** `infrastructure/persistence/adapter/ProductRepositoryAdapter.java`

```java
@Component
public class ProductRepositoryAdapter implements ProductRepository {
    
    private final ProductJpaRepository jpaRepository;
    private final ProductEntityMapper mapper;
    
    // Implementar todos los métodos del puerto
}
```

**Criterios de Aceptación:**
- Implementa todos los métodos de `ProductRepository`
- Usa mapper para convertir entre domain y JPA
- Tests de integración con base de datos H2

#### Task 3.5: Crear DTOs para REST API
**Archivo:** `infrastructure/rest/dto/product/`

```java
// CreateProductRequest.java
public record CreateProductRequest(
    String name,
    String description,
    ProductType type,
    BigDecimal price,
    Integer preparationTimeSeconds
) {}

// UpdateProductRequest.java
public record UpdateProductRequest(
    String description,
    BigDecimal price,
    Integer preparationTimeSeconds
) {}

// UpdateAvailabilityRequest.java
public record UpdateAvailabilityRequest(
    Boolean available
) {}

// ProductResponse.java
public record ProductResponse(
    Long id,
    String name,
    String description,
    ProductType type,
    BigDecimal price,
    Integer preparationTimeSeconds,
    Boolean available,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
```

#### Task 3.6: Crear ProductMapper (DTO ↔ Domain)
**Archivo:** `infrastructure/rest/mapper/ProductMapper.java`

```java
@Component
public class ProductMapper {
    
    public Product toDomain(CreateProductRequest request) {
        // DTO → Domain
    }
    
    public ProductResponse toResponse(Product product) {
        // Domain → Response DTO
    }
    
    public List<ProductResponse> toResponseList(List<Product> products) {
        // Lista de domain → Lista de responses
    }
}
```

#### Task 3.7: Crear ProductController
**Archivo:** `infrastructure/rest/controller/ProductController.java`

**Endpoints:**

```java
@RestController
@RequestMapping("/api/products")
public class ProductController {
    
    // POST /api/products - Crear producto (ADMIN)
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductResponse> createProduct(@Valid @RequestBody CreateProductRequest request)
    
    // GET /api/products - Listar productos (filtros opcionales)
    @GetMapping
    public ResponseEntity<List<ProductResponse>> getProducts(
        @RequestParam(required = false) Boolean available,
        @RequestParam(required = false) ProductType type
    )
    
    // GET /api/products/{id} - Obtener producto por ID
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable Long id)
    
    // PUT /api/products/{id} - Actualizar producto (ADMIN)
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductResponse> updateProduct(
        @PathVariable Long id,
        @Valid @RequestBody UpdateProductRequest request
    )
    
    // PATCH /api/products/{id}/availability - Cambiar disponibilidad (ADMIN)
    @PatchMapping("/{id}/availability")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductResponse> updateAvailability(
        @PathVariable Long id,
        @Valid @RequestBody UpdateAvailabilityRequest request
    )
    
    // DELETE /api/products/{id} - Eliminar producto (ADMIN)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id)
}
```

**Criterios de Aceptación:**
- Todos los endpoints de modificación requieren rol ADMIN
- Validación de DTOs con `@Valid`
- Códigos HTTP apropiados (201, 200, 204, 400, 404, 403)
- Respuestas consistentes en formato JSON

#### Task 3.8: Implementar GlobalExceptionHandler para Productos
**Archivo:** `infrastructure/rest/exception/GlobalExceptionHandler.java`

**Agregar Handlers:**
```java
@ExceptionHandler(ProductNotFoundException.class)
public ResponseEntity<ErrorResponse> handleProductNotFound(ProductNotFoundException ex) {
    ErrorResponse error = new ErrorResponse(ex.getMessage(), "Product not found", 404);
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
}

@ExceptionHandler(ProductAlreadyExistsException.class)
public ResponseEntity<ErrorResponse> handleProductAlreadyExists(ProductAlreadyExistsException ex) {
    ErrorResponse error = new ErrorResponse(ex.getMessage(), "Duplicate product", 409);
    return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
}
```

---

### ✅ Fase 4: Testing

#### Task 4.1: Tests Unitarios - Domain Layer
**Archivos:**
- `ProductTest.java` - Validación de campos y construcción
- `PriceTest.java` - Validación de precios (negativos, null, decimales)
- `ProductValidatorTest.java` - Validaciones de negocio

**Cobertura Mínima:** 100% en domain

#### Task 4.2: Tests Unitarios - Application Layer
**Archivos:**
- `CreateProductUseCaseTest.java` - Creación exitosa y duplicados
- `UpdateProductUseCaseTest.java` - Actualización exitosa y producto no encontrado
- `UpdateProductAvailabilityUseCaseTest.java` - Habilitar/deshabilitar
- `DeleteProductUseCaseTest.java` - Eliminación exitosa y no encontrado
- `GetProductsUseCaseTest.java` - Filtros y listado completo
- `GetProductByIdUseCaseTest.java` - Búsqueda exitosa y no encontrado

**Cobertura Mínima:** 90% en application

#### Task 4.3: Tests de Integración - Infrastructure Layer
**Archivos:**
- `ProductRepositoryAdapterTest.java` - Operaciones CRUD con H2
- `ProductControllerIntegrationTest.java` - Tests de endpoints completos

**Escenarios a Cubrir:**
- Crear producto exitosamente (201)
- Crear producto duplicado (409)
- Actualizar producto existente (200)
- Actualizar producto inexistente (404)
- Cambiar disponibilidad (200)
- Eliminar producto (204)
- Listar todos los productos (200)
- Filtrar productos disponibles (200)
- Filtrar productos por tipo (200)
- Mesero intenta crear producto (403)

**Cobertura Mínima:** 80% en infrastructure

#### Task 4.4: Tests de Seguridad
**Archivo:** `ProductSecurityTest.java`

**Escenarios:**
- Usuario sin autenticación no puede acceder (401)
- WAITER no puede crear productos (403)
- WAITER puede listar productos disponibles (200)
- ADMIN puede realizar todas las operaciones (200/201/204)
- KITCHEN_BAR puede listar productos (200)
- KITCHEN_BAR no puede modificar productos (403)

---

### ✅ Fase 5: Migración de Base de Datos

#### Task 5.1: Crear Script de Migración Flyway/Liquibase
**Archivo:** `resources/db/migration/V4__create_products_table.sql`

```sql
CREATE TABLE products (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(500) NOT NULL,
    type VARCHAR(50) NOT NULL,
    price DECIMAL(10, 2) NOT NULL CHECK (price >= 0),
    preparation_time_seconds INT NOT NULL CHECK (preparation_time_seconds > 0),
    available BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_products_available ON products(available);
CREATE INDEX idx_products_type ON products(type);
CREATE INDEX idx_products_name ON products(name);
```

#### Task 5.2: Script de Datos de Prueba
**Archivo:** `resources/db/migration/V5__insert_sample_products.sql`

```sql
INSERT INTO products (name, description, type, price, preparation_time_seconds, available, created_at, updated_at)
VALUES
('Coca Cola', 'Bebida refrescante carbonatada', 'DRINK', 2.50, 3, TRUE, NOW(), NOW()),
('Pizza Margherita', 'Pizza con tomate, mozzarella y albahaca', 'HOT_DISH', 12.99, 15, TRUE, NOW(), NOW()),
('Ensalada César', 'Ensalada con lechuga, pollo, queso parmesano', 'COLD_DISH', 8.99, 8, TRUE, NOW(), NOW()),
('Mojito', 'Cóctel de ron blanco con menta', 'DRINK', 8.50, 5, TRUE, NOW(), NOW()),
('Tiramisu', 'Postre italiano con café y mascarpone', 'COLD_DISH', 6.50, 6, FALSE, NOW(), NOW());
```

---

### ✅ Fase 6: Documentación

#### Task 6.1: Actualizar README con endpoints de productos

#### Task 6.2: Documentar ejemplos de peticiones con curl
```bash
# Crear producto
curl -X POST http://localhost:8080/api/products \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <admin_token>" \
  -d '{
    "name": "Mojito",
    "description": "Cóctel de ron blanco con menta",
    "type": "DRINK",
    "price": 8.50,
    "preparationTimeSeconds": 4
  }'

# Listar productos disponibles
curl -X GET "http://localhost:8080/api/products?available=true"

# Actualizar disponibilidad
curl -X PATCH http://localhost:8080/api/products/1/availability \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <admin_token>" \
  -d '{"available": false}'
```

#### Task 6.3: Agregar Swagger/OpenAPI annotations
- Documentar todos los endpoints con `@Operation`, `@ApiResponse`
- Incluir ejemplos en los DTOs

---

## 🔄 Flujo de Implementación Sugerido

### Día 1: Domain + Application (TDD)
1. Escribir tests para `Price` → Implementar `Price`
2. Escribir tests para `Product` actualizado → Actualizar `Product`
3. Escribir tests para `ProductValidator` → Implementar `ProductValidator`
4. Definir `ProductRepository` port
5. Escribir tests para `CreateProductUseCase` → Implementar caso de uso
6. Escribir tests para `GetProductsUseCase` → Implementar caso de uso

### Día 2: Application (Resto de Casos de Uso)
1. TDD: `UpdateProductUseCase`
2. TDD: `UpdateProductAvailabilityUseCase`
3. TDD: `DeleteProductUseCase`
4. TDD: `GetProductByIdUseCase`
5. Crear excepciones personalizadas

### Día 3: Infrastructure (Persistencia)
1. Crear migration scripts
2. Crear `ProductEntity`
3. Crear `ProductJpaRepository`
4. Crear `ProductEntityMapper`
5. Crear `ProductRepositoryAdapter`
6. Tests de integración con H2

### Día 4: Infrastructure (REST API)
1. Crear DTOs (Request/Response)
2. Crear `ProductMapper`
3. Implementar `ProductController` con seguridad
4. Actualizar `GlobalExceptionHandler`
5. Tests de integración de endpoints

### Día 5: Testing Completo + Documentación
1. Tests de seguridad (roles)
2. Tests end-to-end
3. Verificar cobertura (mínimo 70%)
4. Documentar API
5. Actualizar README
6. Code review y refactoring

---

## ✅ Criterios de Completitud

### Funcionales
- ✅ Administrador puede crear productos con todos los campos requeridos
- ✅ No se permiten nombres de productos duplicados (409 Conflict)
- ✅ Administrador puede actualizar precio, descripción y tiempo de preparación
- ✅ Administrador puede habilitar/deshabilitar productos
- ✅ Administrador puede eliminar productos permanentemente
- ✅ Meseros pueden listar productos disponibles (solo `available = true`)
- ✅ Administradores pueden listar todos los productos
- ✅ Se puede filtrar por tipo (DRINK, HOT_DISH, COLD_DISH)
- ✅ Precio siempre tiene 2 decimales y es >= 0
- ✅ Meseros NO pueden crear/modificar/eliminar productos (403 Forbidden)

### No Funcionales
- ✅ Cobertura de tests >= 70%
- ✅ Arquitectura hexagonal respetada (sin dependencias invertidas)
- ✅ No usar Spring annotations en domain
- ✅ Métodos < 20 líneas
- ✅ Nombres descriptivos (no abreviaturas)
- ✅ Tipos explícitos en domain/application
- ✅ Manejo centralizado de excepciones
- ✅ Códigos HTTP apropiados
- ✅ Validaciones en la capa correcta

### Seguridad
- ✅ Endpoints protegidos con `@PreAuthorize("hasRole('ADMIN')")`
- ✅ Tests de seguridad verifican roles
- ✅ 401 para usuarios no autenticados
- ✅ 403 para usuarios sin permisos suficientes

---

## 🧪 Comandos de Verificación

```bash
# Ejecutar todos los tests
./gradlew test

# Verificar cobertura (mínimo 70%)
./gradlew check

# Generar reporte de cobertura
./gradlew jacocoTestReport
# Ver en: build/reports/jacoco/test/html/index.html

# Build completo
./gradlew clean build

# Ejecutar aplicación
./gradlew bootRun
```

---

## 📊 Métricas de Éxito

- **Cobertura de Código:** >= 70%
- **Tests Unitarios:** >= 25 tests
- **Tests de Integración:** >= 10 tests
- **Endpoints Implementados:** 6 endpoints REST
- **Tiempo de Respuesta:** < 200ms para operaciones CRUD
- **Sin Violaciones SOLID:** 0 dependencias invertidas

---

## 🚨 Riesgos y Mitigaciones

| Riesgo | Probabilidad | Impacto | Mitigación |
|--------|--------------|---------|------------|
| Productos duplicados no detectados | Media | Alto | Validación de unicidad en BD + test específico |
| Cambios de precio afectan pedidos existentes | Baja | Alto | No afecta (pedidos ya creados usan precio histórico) |
| Performance con catálogo grande | Media | Medio | Indexar campos `name`, `available`, `type` |
| Eliminar producto en uso por pedidos activos | Alta | Alto | Verificar referencias antes de eliminar (HU futura) |

---

## 📝 Notas Adicionales

- Los productos eliminados se borran permanentemente (DELETE físico, no lógico)
- El campo `available = false` sirve para inhabilitar temporalmente sin eliminar
- El nombre del producto es inmutable después de creación (para evitar confusiones)
- Si se necesita cambiar el nombre, eliminar y crear uno nuevo
- Los precios históricos en pedidos NO se ven afectados por cambios en el catálogo
- El tiempo de preparación se usa para estimar tiempos de las tareas

---

## 🔗 Dependencias con Otras Historias de Usuario

- **HU-001 (Procesar pedido):** Los pedidos referencian productos del catálogo
- **HU-004 (Autenticación):** Requiere roles ADMIN para gestión completa
- **HU-007 (Futura):** Historial de cambios de precios para auditoría

---

**Última Actualización:** 2026-01-21  