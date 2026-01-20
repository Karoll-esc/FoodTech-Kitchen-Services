# Test Plan and Test Cases for FoodTech Kitchen Services

Based on the refactored user stories (HU-004, HU-005, HU-006), this document outlines a comprehensive test plan and test cases.

---

## 📋 Test Plan Overview

### 1. Test Strategy

**Scope:**
- Authentication & Authorization (HU-004)
- Table Lifecycle Management (HU-005)
- Product Catalog Management (HU-006)

**Test Levels:**
- **Unit Tests**: Domain logic, validators, factories
- **Integration Tests**: API endpoints, database interactions, Auth0 integration
- **End-to-End Tests**: Complete user workflows

**Test Types:**
- Functional testing (business rules)
- Security testing (authentication, authorization)
- Negative testing (invalid inputs, unauthorized access)
- State transition testing (table lifecycle)

---

## 🧪 Test Cases Structure

### **HU-004: Authentication & Authorization**

#### **Test Suite 1: Authentication**

```gherkin
Test Case ID: AUTH-001
Title: Usuario sin token no puede acceder al sistema
Priority: Critical
Preconditions: Sistema configurado con Auth0

Given: Un usuario sin token de autenticación
When: Intenta acceder a GET /api/orders
Then: 
  - HTTP status: 401 Unauthorized
  - Response body contiene mensaje de error de autenticación
  - No se ejecuta la lógica de negocio
```

```gherkin
Test Case ID: AUTH-002
Title: Token expirado es rechazado
Priority: High
Preconditions: Usuario previamente autenticado

Given: Un token JWT válido que expiró hace 1 hora
When: Intenta acceder a GET /api/tasks
Then:
  - HTTP status: 401 Unauthorized
  - Response indica token expirado
```

```gherkin
Test Case ID: AUTH-003
Title: Token con firma inválida es rechazado
Priority: Critical
Preconditions: Sistema configurado con Auth0

Given: Un token JWT con firma adulterada
When: Intenta acceder a cualquier endpoint protegido
Then:
  - HTTP status: 401 Unauthorized
  - Response indica token inválido
```

---

#### **Test Suite 2: Authorization - Role-based Access Control**

```gherkin
Test Case ID: AUTHZ-001
Title: WAITER puede crear pedidos
Priority: High
Preconditions: Usuario autenticado con rol WAITER

Given: Token válido con rol "WAITER"
When: POST /api/orders con payload válido
Then:
  - HTTP status: 201 Created
  - Pedido creado en BD
  - Tareas generadas correctamente
```

```gherkin
Test Case ID: AUTHZ-002
Title: WAITER no puede crear productos
Priority: High
Preconditions: Usuario autenticado con rol WAITER

Given: Token válido con rol "WAITER"
When: POST /api/products con payload válido
Then:
  - HTTP status: 403 Forbidden
  - Response: "Insufficient permissions"
  - No se crea producto en BD
```

```gherkin
Test Case ID: AUTHZ-003
Title: KITCHEN_BAR puede iniciar tareas de BAR
Priority: High
Preconditions: 
  - Usuario autenticado con rol KITCHEN_BAR
  - Existe tarea PENDING de estación BAR

Given: Token válido con rol "KITCHEN_BAR"
When: POST /api/tasks/{taskId}/start (taskId de estación BAR)
Then:
  - HTTP status: 200 OK
  - Task.status cambia a IN_PREPARATION
  - Task.startedAt se registra
```

```gherkin
Test Case ID: AUTHZ-004
Title: KITCHEN_BAR no puede iniciar tareas de HOT_KITCHEN
Priority: High
Preconditions:
  - Usuario autenticado con rol KITCHEN_BAR
  - Existe tarea PENDING de estación HOT_KITCHEN

Given: Token válido con rol "KITCHEN_BAR"
When: POST /api/tasks/{taskId}/start (taskId de estación HOT_KITCHEN)
Then:
  - HTTP status: 403 Forbidden
  - Response: "Cannot modify tasks from station: HOT_KITCHEN"
  - Task.status permanece PENDING
```

```gherkin
Test Case ID: AUTHZ-005
Title: KITCHEN_BAR puede visualizar tareas de todas las estaciones
Priority: Medium
Preconditions:
  - Usuario autenticado con rol KITCHEN_BAR
  - Existen 5 tareas: 2 BAR, 2 HOT_KITCHEN, 1 COLD_KITCHEN

Given: Token válido con rol "KITCHEN_BAR"
When: GET /api/tasks
Then:
  - HTTP status: 200 OK
  - Response contiene 5 tareas
  - Tareas de BAR tienen "canModify": true
  - Tareas de HOT_KITCHEN y COLD_KITCHEN tienen "canModify": false
```

```gherkin
Test Case ID: AUTHZ-006
Title: ADMIN tiene acceso completo a todas las operaciones
Priority: High
Preconditions: Usuario autenticado con rol ADMIN

Given: Token válido con rol "ADMIN"
When: Ejecuta POST /api/products, POST /api/tables, POST /api/tasks/{id}/start
Then:
  - Todas las operaciones retornan éxito (201/200)
  - Todas las entidades se crean/modifican correctamente
```

---

#### **Test Suite 3: User Profile**

```gherkin
Test Case ID: PROFILE-001
Title: KITCHEN_BAR consulta su perfil con información de estación
Priority: Medium
Preconditions: Usuario autenticado con rol KITCHEN_BAR

Given: Token válido con rol "KITCHEN_BAR"
When: GET /api/users/me
Then:
  - HTTP status: 200 OK
  - Response contiene: userId, email, name, roles
  - Response.station = "BAR"
```

```gherkin
Test Case ID: PROFILE-002
Title: WAITER consulta su perfil sin estación asignada
Priority: Medium
Preconditions: Usuario autenticado con rol WAITER

Given: Token válido con rol "WAITER"
When: GET /api/users/me
Then:
  - HTTP status: 200 OK
  - Response contiene: userId, email, name, roles
  - Response.station = null
```

---

### **HU-005: Table Lifecycle Management**

#### **Test Suite 4: Table Creation & Validation**

```gherkin
Test Case ID: TABLE-001
Title: ADMIN crea nueva mesa exitosamente
Priority: High
Preconditions: 
  - Usuario autenticado con rol ADMIN
  - No existe mesa "A1"

Given: Token ADMIN válido
When: POST /api/tables
  {
    "tableNumber": "A1",
    "capacity": 4
  }
Then:
  - HTTP status: 201 Created
  - Response contiene mesa con id generado
  - Mesa.status = "AVAILABLE"
  - Mesa.createdAt está presente
  - Mesa existe en BD
```

```gherkin
Test Case ID: TABLE-002
Title: No se puede crear mesa con número duplicado
Priority: High
Preconditions:
  - Usuario autenticado con rol ADMIN
  - Ya existe mesa "B3"

Given: Token ADMIN válido
When: POST /api/tables con tableNumber "B3"
Then:
  - HTTP status: 400 Bad Request
  - Response: "Table with number B3 already exists"
  - No se crea duplicado en BD
```

```gherkin
Test Case ID: TABLE-003
Title: WAITER no puede crear mesas
Priority: Medium
Preconditions: Usuario autenticado con rol WAITER

Given: Token WAITER válido
When: POST /api/tables con payload válido
Then:
  - HTTP status: 403 Forbidden
  - No se crea mesa en BD
```

---

#### **Test Suite 5: Table State Transitions**

```gherkin
Test Case ID: TABLE-STATE-001
Title: Mesa cambia a OCCUPIED al crear pedido
Priority: Critical
Preconditions:
  - Existe mesa "C5" con status AVAILABLE
  - Usuario autenticado con rol WAITER

Given: Mesa "C5" en estado AVAILABLE
When: POST /api/orders para mesa "C5"
Then:
  - HTTP status: 201 Created
  - Mesa.status = "OCCUPIED"
  - Mesa.currentOrderId = {orderId creado}
  - Mesa.updatedAt actualizado
```

```gherkin
Test Case ID: TABLE-STATE-002
Title: Mesa cambia a SERVED cuando todas las tareas se completan
Priority: Critical
Preconditions:
  - Mesa "A2" en estado OCCUPIED con pedido asociado
  - Pedido tiene 3 tareas: 2 COMPLETED, 1 IN_PREPARATION

Given: Mesa "A2" con 2/3 tareas completadas
When: La última tarea cambia a COMPLETED
Then:
  - Mesa.status cambia automáticamente a "SERVED"
  - Mesa.currentOrderId permanece igual (para referencia)
  - Mesa.updatedAt actualizado
```

```gherkin
Test Case ID: TABLE-STATE-003
Title: WAITER cambia mesa SERVED a CLEANING
Priority: High
Preconditions:
  - Mesa "D1" en estado SERVED
  - Usuario autenticado con rol WAITER

Given: Mesa "D1" con status SERVED
When: PUT /api/tables/D1/status con status "CLEANING"
Then:
  - HTTP status: 200 OK
  - Mesa.status = "CLEANING"
  - Mesa.updatedAt actualizado
```

```gherkin
Test Case ID: TABLE-STATE-004
Title: WAITER cambia mesa CLEANING a AVAILABLE
Priority: High
Preconditions:
  - Mesa "E3" en estado CLEANING
  - Usuario autenticado con rol WAITER

Given: Mesa "E3" con status CLEANING
When: PUT /api/tables/E3/status con status "AVAILABLE"
Then:
  - HTTP status: 200 OK
  - Mesa.status = "AVAILABLE"
  - Mesa.currentOrderId = null
  - Mesa.updatedAt actualizado
```

```gherkin
Test Case ID: TABLE-STATE-005
Title: No se puede saltar estados (AVAILABLE → CLEANING inválido)
Priority: High
Preconditions:
  - Mesa existe con status AVAILABLE
  - Usuario autenticado con rol WAITER

Given: Mesa con status AVAILABLE
When: PUT /api/tables/{id}/status con status "CLEANING"
Then:
  - HTTP status: 400 Bad Request
  - Response: "Invalid state transition: AVAILABLE → CLEANING"
  - Mesa.status permanece AVAILABLE
```

```gherkin
Test Case ID: TABLE-STATE-006
Title: No se puede crear pedido en mesa OCCUPIED
Priority: High
Preconditions:
  - Mesa "B3" en estado OCCUPIED con orderId 42
  - Usuario autenticado con rol WAITER

Given: Mesa "B3" con status OCCUPIED
When: POST /api/orders para mesa "B3"
Then:
  - HTTP status: 400 Bad Request
  - Response: "Table B3 is not available"
  - No se crea nuevo pedido
```

---

#### **Test Suite 6: Table Queries**

```gherkin
Test Case ID: TABLE-QUERY-001
Title: Consultar solo mesas disponibles
Priority: Medium
Preconditions:
  - Existen 10 mesas: 3 AVAILABLE, 4 OCCUPIED, 2 SERVED, 1 CLEANING

Given: Usuario autenticado
When: GET /api/tables?status=AVAILABLE
Then:
  - HTTP status: 200 OK
  - Response contiene 3 mesas
  - Todas las mesas tienen status AVAILABLE
```

```gherkin
Test Case ID: TABLE-QUERY-002
Title: Consultar todas las mesas con información completa
Priority: Medium
Preconditions: Existen 15 mesas en diferentes estados

Given: Usuario autenticado
When: GET /api/tables
Then:
  - HTTP status: 200 OK
  - Response contiene 15 mesas
  - Cada mesa incluye: id, tableNumber, status, capacity, currentOrderId, updatedAt
```

---

### **HU-006: Product Catalog Management**

#### **Test Suite 7: Product CRUD Operations**

```gherkin
Test Case ID: PRODUCT-001
Title: ADMIN crea nuevo producto con precio
Priority: High
Preconditions:
  - Usuario autenticado con rol ADMIN
  - No existe producto "Mojito"

Given: Token ADMIN válido
When: POST /api/products
  {
    "name": "Mojito",
    "description": "Cóctel de ron blanco con menta",
    "type": "DRINK",
    "price": 8.50,
    "preparationTime": 4
  }
Then:
  - HTTP status: 201 Created
  - Response contiene producto con id generado
  - Product.available = true (default)
  - Product.price = 8.50
  - Producto existe en BD
```

```gherkin
Test Case ID: PRODUCT-002
Title: No se puede crear producto con nombre duplicado
Priority: High
Preconditions:
  - Usuario ADMIN autenticado
  - Ya existe producto "Pizza Margherita"

Given: Token ADMIN válido
When: POST /api/products con name "Pizza Margherita"
Then:
  - HTTP status: 400 Bad Request
  - Response: "Product with name 'Pizza Margherita' already exists"
  - No se crea duplicado en BD
```

```gherkin
Test Case ID: PRODUCT-003
Title: ADMIN actualiza precio y descripción de producto
Priority: High
Preconditions:
  - Usuario ADMIN autenticado
  - Existe producto "Ensalada César" con precio 8.99

Given: Producto existente con id conocido
When: PUT /api/products/{id}
  {
    "price": 9.50,
    "description": "Ensalada con aderezo césar casero",
    "preparationTime": 6
  }
Then:
  - HTTP status: 200 OK
  - Product.price = 9.50
  - Product.description actualizada
  - Product.updatedAt actualizado
```

```gherkin
Test Case ID: PRODUCT-004
Title: ADMIN marca producto como no disponible
Priority: Medium
Preconditions:
  - Usuario ADMIN autenticado
  - Existe producto "Salmón a la parrilla" con available=true

Given: Producto disponible
When: PATCH /api/products/{id}
  {
    "available": false
  }
Then:
  - HTTP status: 200 OK
  - Product.available = false
  - Producto no aparece en GET /api/products (filtro default: available=true)
  - Producto sigue existiendo en BD
```

```gherkin
Test Case ID: PRODUCT-005
Title: ADMIN reactiva producto deshabilitado
Priority: Medium
Preconditions:
  - Usuario ADMIN autenticado
  - Existe producto "Tiramisu" con available=false

Given: Producto no disponible
When: PATCH /api/products/{id}
  {
    "available": true
  }
Then:
  - HTTP status: 200 OK
  - Product.available = true
  - Producto aparece en GET /api/products
```

```gherkin
Test Case ID: PRODUCT-006
Title: ADMIN elimina producto del catálogo
Priority: Medium
Preconditions:
  - Usuario ADMIN autenticado
  - Existe producto "Sangría"

Given: Producto existente con id conocido
When: DELETE /api/products/{id}
Then:
  - HTTP status: 204 No Content
  - Producto eliminado de BD (hard delete)
  - GET /api/products/{id} retorna 404
```

---

#### **Test Suite 8: Product Authorization**

```gherkin
Test Case ID: PRODUCT-AUTHZ-001
Title: WAITER no puede crear productos
Priority: High
Preconditions: Usuario autenticado con rol WAITER

Given: Token WAITER válido
When: POST /api/products con payload válido
Then:
  - HTTP status: 403 Forbidden
  - Response: "Only administrators can manage products"
  - No se crea producto
```

```gherkin
Test Case ID: PRODUCT-AUTHZ-002
Title: KITCHEN_BAR no puede modificar productos
Priority: High
Preconditions: Usuario autenticado con rol KITCHEN_BAR

Given: Token KITCHEN_BAR válido
When: PUT /api/products/{id} con cambios válidos
Then:
  - HTTP status: 403 Forbidden
  - Producto no se modifica
```

---

#### **Test Suite 9: Product Queries**

```gherkin
Test Case ID: PRODUCT-QUERY-001
Title: WAITER consulta productos disponibles
Priority: High
Preconditions:
  - Existen 10 productos: 7 available=true, 3 available=false

Given: Usuario WAITER autenticado
When: GET /api/products
Then:
  - HTTP status: 200 OK
  - Response contiene 7 productos
  - Todos tienen available=true
  - Cada producto incluye: id, name, description, type, price
```

```gherkin
Test Case ID: PRODUCT-QUERY-002
Title: Filtrar productos por tipo DRINK
Priority: Medium
Preconditions: Existen productos de diferentes tipos

Given: Usuario autenticado
When: GET /api/products?type=DRINK
Then:
  - HTTP status: 200 OK
  - Response contiene solo productos con type=DRINK
  - Cada producto incluye precio
```

```gherkin
Test Case ID: PRODUCT-QUERY-003
Title: Precio siempre presente con 2 decimales
Priority: Medium
Preconditions: Existen productos con precios variados

Given: Usuario autenticado
When: GET /api/products
Then:
  - HTTP status: 200 OK
  - Todos los productos tienen campo "price"
  - Todos los precios tienen formato #.## (2 decimales)
  - Todos los precios >= 0.00
```

---

## 📊 Test Execution Matrix

| Test Suite | Unit Tests | Integration Tests | E2E Tests | Priority |
|------------|-----------|------------------|-----------|----------|
| Authentication | SecurityConfig, JWT validation | Auth0 integration | Login flow | Critical |
| Authorization | Role enum, SecurityUtils | @PreAuthorize endpoints | Role-based workflows | Critical |
| User Profile | N/A | UserController | Profile retrieval | Medium |
| Table Creation | TableValidator | TableController, Repository | Create table flow | High |
| Table State Transitions | Table state machine | State update endpoints | Full lifecycle | Critical |
| Table Queries | N/A | Query endpoints | Filter/search | Medium |
| Product CRUD | Product domain model | ProductController | Catalog management | High |
| Product Authorization | SecurityUtils | @PreAuthorize checks | Access denial | High |
| Product Queries | N/A | Query endpoints | Menu display | Medium |

---

## 🔧 Test Data Requirements

### Auth0 Test Users

```yaml
- user1:
    email: admin@foodtech.com
    roles: [ADMIN]
    
- user2:
    email: waiter@foodtech.com
    roles: [WAITER]
    
- user3:
    email: bar@foodtech.com
    roles: [KITCHEN_BAR]
    
- user4:
    email: hot@foodtech.com
    roles: [KITCHEN_HOT]
    
- user5:
    email: cold@foodtech.com
    roles: [KITCHEN_COLD]
```

### Test Tables

```yaml
tables:
  - { number: "A1", capacity: 4, status: AVAILABLE }
  - { number: "B3", capacity: 4, status: OCCUPIED, orderId: 42 }
  - { number: "C5", capacity: 4, status: AVAILABLE }
  - { number: "D1", capacity: 4, status: SERVED }
  - { number: "E3", capacity: 4, status: CLEANING }
```

### Test Products

```yaml
products:
  - { name: "Coca Cola", type: DRINK, price: 2.50, available: true, preparationTime: 2 }
  - { name: "Sprite", type: DRINK, price: 2.50, available: true, preparationTime: 2 }
  - { name: "Mojito", type: DRINK, price: 8.50, available: true, preparationTime: 4 }
  - { name: "Pizza Margherita", type: HOT_DISH, price: 12.99, available: true, preparationTime: 15 }
  - { name: "Pasta Carbonara", type: HOT_DISH, price: 14.99, available: true, preparationTime: 12 }
  - { name: "Salmón a la parrilla", type: HOT_DISH, price: 18.99, available: true, preparationTime: 20 }
  - { name: "Ensalada César", type: COLD_DISH, price: 8.99, available: true, preparationTime: 5 }
  - { name: "Tiramisu", type: COLD_DISH, price: 6.99, available: false, preparationTime: 3 }
  - { name: "Sangría", type: DRINK, price: 7.50, available: true, preparationTime: 3 }
```

### Test Orders & Tasks

```yaml
orders:
  - id: 42
    tableNumber: "B3"
    status: IN_PREPARATION
    tasks:
      - { id: 1, station: BAR, status: COMPLETED }
      - { id: 2, station: HOT_KITCHEN, status: IN_PREPARATION }
```

---

## 📈 Test Metrics & Coverage

### Target Coverage

- **Unit Tests**: 80%+ for domain and application layers
- **Integration Tests**: 70%+ for controllers and adapters
- **E2E Tests**: Critical user journeys (authentication, table lifecycle, product CRUD)

### Coverage Exclusions

```
- KitchenServiceApplication.class (main method)
- infrastructure/config/** (Spring configuration)
- *JpaRepository.class (Spring Data interfaces)
```

### Exit Criteria

- ✅ All critical tests pass (100%)
- ✅ All high-priority tests pass (100%)
- ✅ Medium-priority tests: 95%+ pass rate
- ✅ No security vulnerabilities in authentication/authorization
- ✅ Code coverage meets minimum thresholds (70% overall)
- ✅ No blocker or critical bugs in test environment

---

## 🚀 Test Execution Strategy

### Phase 1: Unit Testing (Week 1)
- Implement unit tests for domain models
- Test validators and business logic
- Mock external dependencies
- Target: 80% domain coverage

### Phase 2: Integration Testing (Week 2)
- Test REST controllers with MockMvc
- Verify database interactions with H2
- Test Auth0 integration with mock server
- Target: 70% infrastructure coverage

### Phase 3: E2E Testing (Week 3)
- Full authentication flow
- Complete table lifecycle
- Product catalog management
- Cross-role scenarios

### Phase 4: Regression & Performance (Week 4)
- Regression suite execution
- Load testing for critical endpoints
- Security penetration testing
- Documentation and handoff

---

## 🔐 Security Testing Checklist

- [ ] Authentication with invalid tokens
- [ ] Authentication with expired tokens
- [ ] Authorization for each role combination
- [ ] SQL injection attempts on all inputs
- [ ] XSS attacks on text fields
- [ ] CSRF protection verification
- [ ] Rate limiting on authentication endpoints
- [ ] Sensitive data exposure in responses
- [ ] JWT token tampering detection

---

## 🛠️ Tools & Frameworks

### Testing Frameworks
- **JUnit 5**: Unit testing framework
- **Mockito**: Mocking framework
- **MockMvc**: Spring MVC testing
- **Spring Boot Test**: Integration testing
- **AssertJ**: Fluent assertions
- **Testcontainers**: Database integration tests (optional)

### CI/CD Integration
- **GitHub Actions**: Automated test execution
- **JaCoCo**: Code coverage reporting
- **SonarQube**: Code quality analysis (optional)

### Test Data Management
- **H2 Database**: In-memory testing
- **@Sql scripts**: Test data setup
- **TestDataBuilder pattern**: Fluent test object creation

---

## 📝 Test Case Template

For future test cases, use this template:

```gherkin
Test Case ID: [SUITE]-[NUMBER]
Title: [Descriptive title in Spanish]
Priority: [Critical | High | Medium | Low]
Preconditions: [Setup requirements]

Given: [Initial state]
When: [Action performed]
Then:
  - [Expected outcome 1]
  - [Expected outcome 2]
  - [Expected outcome N]
```

---

## 📞 Contact & Support

**QA Team Lead**: [Name]  
**Test Environment**: `http://localhost:8080` (local), `https://test.foodtech.com` (staging)  
**Issue Tracking**: GitHub Issues  
**Documentation**: See `HISTORIAS_DE_USUARIO.md` for detailed acceptance criteria

---

**Last Updated**: 2026-01-19  
**Version**: 1.0  
**Status**: Ready for Implementation
