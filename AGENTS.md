# AGENTS.md - Coding Agent Guidelines

This file provides coding agents with essential information about build commands, code style, and architectural patterns for the FoodTech Kitchen Service project.

---

## Build, Test & Run Commands

### Running Tests
```bash
# Run all tests
./gradlew test

# Run single test class
./gradlew test --tests "TaskDecomposerTest"

# Run single test method
./gradlew test --tests "TaskDecomposerTest.shouldCreateOneTaskForSingleDrink"

# Run tests with verbose output
./gradlew test --info

# Run tests with coverage report
./gradlew test jacocoTestReport
```

### Building & Running
```bash
# Clean build
./gradlew clean build

# Build without tests
./gradlew clean build -x test

# Run application
./gradlew bootRun

# Build and run JAR
./gradlew build
java -jar build/libs/kitchen-service-0.0.1-SNAPSHOT.jar
```

### Code Quality
```bash
# Run tests with coverage verification (minimum 70%)
./gradlew check

# Generate coverage reports (HTML at build/reports/jacoco/test/html/index.html)
./gradlew jacocoTestReport
```

---

## Project Architecture

### Hexagonal Architecture (Ports & Adapters)

**Directory Structure:**
- `domain/` - Core business logic (NO framework dependencies, NO Spring annotations)
- `application/` - Use cases and port interfaces (orchestration layer)
- `infrastructure/` - Adapters for REST, JPA, config (framework-specific code)

**Critical Rules:**
- Domain NEVER imports from application or infrastructure
- Domain uses NO Spring annotations (@Component, @Service, etc.)
- Dependencies point TOWARD domain (Dependency Inversion Principle)
- Application uses ports (interfaces) to interact with domain
- Infrastructure implements ports defined in application layer

---

## Code Style Guidelines

### Package Organization
```
com.foodtech.kitchen/
├── domain/
│   ├── model/           # Entities, value objects, enums
│   ├── commands/        # Command pattern implementations
│   └── services/        # Domain services (pure business logic)
├── application/
│   ├── usecases/        # Use case implementations
│   ├── ports/in/        # Input ports (interfaces)
│   ├── ports/out/       # Output ports (interfaces)
│   └── exepcions/       # Application-level exceptions
└── infrastructure/
    ├── rest/            # REST controllers, DTOs, mappers
    ├── persistence/     # JPA adapters, entities, repositories
    ├── config/          # Spring configuration
    └── execution/       # Command executors
```

### Import Organization
```java
// 1. Package imports (specific first, wildcard for 3+ from same package)
import com.foodtech.kitchen.domain.model.Order;
import com.foodtech.kitchen.domain.model.Product;
import com.foodtech.kitchen.domain.model.Task;
// OR if 3+ imports from same package:
import com.foodtech.kitchen.domain.model.*;

// 2. Java standard library
import java.util.List;
import java.util.Map;

// 3. Spring framework (only in application/infrastructure layers)
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;
```

### Naming Conventions

**Classes:**
- Entities: `Order`, `Task`, `Product` (nouns)
- Use Cases: `ProcessOrderUseCase`, `GetTasksByStationUseCase` (verb + UseCase suffix)
- Ports: `ProcessOrderPort`, `TaskRepository` (interface names)
- Adapters: `OrderRepositoryAdapter`, `TaskRepositoryAdapter` (Adapter suffix)
- Mappers: `OrderMapper`, `TaskEntityMapper` (Mapper suffix)
- Factories: `TaskFactory`, `CommandFactory` (Factory suffix)
- Controllers: `OrderController`, `TaskController` (Controller suffix)

**Methods:**
- Use descriptive verbs: `execute()`, `decompose()`, `validate()`, `save()`
- Keep methods short (max 15-20 lines)
- Test methods: `should[ExpectedBehavior]When[Condition]` or `should[ExpectedBehavior]For[Scenario]`

**Variables:**
- Explicit types (avoid `var` in domain layer)
- Descriptive names: `productsByStation`, `taskDecomposer` (NOT `td`, `map`)
- Constants: `UPPER_SNAKE_CASE` (e.g., `ORDER_SUCCESS_MESSAGE`)

### Type Declarations
```java
// PREFERRED - Explicit types in domain/application layers
List<Product> products = new ArrayList<>();
Map<Station, List<Product>> productsByStation = new HashMap<>();

// AVOID - var makes code less clear (only use in obvious cases)
var products = new ArrayList<>();  // Less clear
```

### Formatting & Structure

**Method Length:**
- Keep methods under 20 lines
- Extract complex logic into private helper methods
- One responsibility per method (SRP)

**Example:**
```java
public List<Task> decompose(Order order) {
    orderValidator.validate(order);
    Map<Station, List<Product>> productsByStation = groupProductsByStation(order);
    Long orderId = order.getId() != null ? order.getId() : 0L;
    return taskFactory.createTasks(orderId, order.getTableNumber(), productsByStation);
}

private Map<Station, List<Product>> groupProductsByStation(Order order) {
    Map<Station, List<Product>> productsByStation = new HashMap<>();
    for (Product product : order.getProducts()) {
        Station station = product.getType().getStation();
        productsByStation.computeIfAbsent(station, k -> new ArrayList<>()).add(product);
    }
    return productsByStation;
}
```

---

## SOLID Principles (NON-NEGOTIABLE)

### Single Responsibility Principle (SRP)
- Each class has ONE responsibility
- Controllers coordinate, don't contain business logic
- Separate concerns: validation, mapping, persistence, execution

```java
// GOOD - Controller only coordinates
@RestController
public class OrderController {
    private final ProcessOrderPort processOrderPort;
    
    @PostMapping
    public ResponseEntity<CreateOrderResponse> createOrder(@RequestBody CreateOrderRequest request) {
        Order order = OrderMapper.toDomain(request);
        List<Task> tasks = processOrderPort.execute(order);
        return ResponseEntity.status(HttpStatus.CREATED).body(
            new CreateOrderResponse(order.getTableNumber(), tasks.size(), ORDER_SUCCESS_MESSAGE)
        );
    }
}
```

### Open/Closed Principle (OCP)
- Use enums with behavior instead of switch statements
- Extend by adding new implementations, not modifying existing code

```java
// GOOD - Enum contains station mapping
public enum ProductType {
    DRINK(Station.BAR),
    HOT_DISH(Station.HOT_KITCHEN),
    COLD_DISH(Station.COLD_KITCHEN);
    
    private final Station station;
    public Station getStation() { return station; }
}
```

### Liskov Substitution Principle (LSP)
- NEVER add stub methods that throw UnsupportedOperationException
- All interface implementations must be complete and valid
- Interfaces should be fully implementable

### Interface Segregation Principle (ISP)
- Keep interfaces small and focused
- Only include methods that are actually used
- Don't force clients to depend on methods they don't need

### Dependency Inversion Principle (DIP)
- Depend on abstractions (interfaces), not implementations
- Use constructor injection
- No `new` keyword in domain/application for injected dependencies

---

## Exception Handling

### Domain Layer
Use standard Java exceptions for business rule validation:
```java
public class Order {
    public Order(String tableNumber, List<Product> products) {
        if (tableNumber == null || tableNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Table number cannot be null or empty");
        }
        if (products == null || products.isEmpty()) {
            throw new IllegalArgumentException("Products list cannot be null or empty");
        }
    }
}
```

### Application Layer
Create custom exceptions for use case failures:
```java
public class OrderNotFoundException extends RuntimeException {
    public OrderNotFoundException(Long orderId) {
        super("Order not found with id: " + orderId);
    }
}
```

### Infrastructure Layer
Use `@RestControllerAdvice` for centralized error handling:
```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(IllegalArgumentException ex) {
        ErrorResponse error = new ErrorResponse(ex.getMessage(), "Validation failed", 400);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
}
```

---

## Testing Strategy

### Test Categories

**Unit Tests (Domain/Services):**
- No Spring framework dependencies
- Test pure business logic
- Use direct instantiation

```java
@BeforeEach
void setUp() {
    orderValidator = new OrderValidator();
    taskFactory = new TaskFactory();
    decomposer = new TaskDecomposer(orderValidator, taskFactory);
}

@Test
@DisplayName("Debe agrupar productos del mismo tipo en una sola tarea")
void shouldGroupProductsOfSameTypeInSingleTask() {
    // Given
    Product cocaCola = new Product("Coca Cola", ProductType.DRINK);
    Product sprite = new Product("Sprite", ProductType.DRINK);
    Order order = new Order("E5", List.of(cocaCola, sprite));
    
    // When
    List<Task> tasks = decomposer.decompose(order);
    
    // Then
    assertEquals(1, tasks.size());
    assertEquals(2, tasks.get(0).getProducts().size());
}
```

**Integration Tests (Controllers/Repositories):**
- Use `@SpringBootTest` and `@AutoConfigureMockMvc`
- Test full HTTP request/response cycle
- Verify database interactions

```java
@SpringBootTest
@AutoConfigureMockMvc
class OrderControllerIntegrationTest {
    @Autowired
    private MockMvc mockMvc;
    
    @Test
    void shouldReturn201WhenCreatingValidOrder() throws Exception {
        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(validOrderJson))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.tasksCreated").value(3));
    }
}
```

### Test Naming
- Use `@DisplayName` for Spanish descriptions
- Method names: `should[ExpectedBehavior]When[Condition]`
- Example: `shouldCreateOneTaskForSingleDrink()` with `@DisplayName("Debe crear una tarea para un pedido con una sola bebida")`

---

## Key Design Patterns

### Command Pattern (Core Pattern)
```java
// Interface
public interface Command {
    void execute();
}

// Concrete implementation
public class PrepareDrinkCommand implements Command {
    private final List<Product> products;
    
    @Override
    public void execute() {
        // Preparation logic
    }
}

// Factory
public class CommandFactory {
    public Command createCommand(Station station, List<Product> products) {
        return switch (station) {
            case BAR -> new PrepareDrinkCommand(products);
            case HOT_KITCHEN -> new PrepareHotDishCommand(products);
            case COLD_KITCHEN -> new PrepareColdDishCommand(products);
        };
    }
}
```

### Repository Pattern
```java
// Port (in application layer)
public interface TaskRepository {
    void saveAll(List<Task> tasks);
    Optional<Task> findById(Long id);
}

// Adapter (in infrastructure layer)
@Component
public class TaskRepositoryAdapter implements TaskRepository {
    private final TaskJpaRepository jpaRepository;
    private final TaskEntityMapper mapper;
    // Implementation...
}
```

---

## Additional Guidelines from Copilot Instructions

**Reference:** `.github/workflows/copilot-instruccion.md`

1. **Read Documentation First:** Always review `readme.md` and `HISTORIAS_DE_USUARIO.md` before implementing features
2. **TDD Approach:** Write tests BEFORE implementation (RED-GREEN-REFACTOR cycle)
3. **Immutability:** Prefer `final` fields and defensive copies
4. **No Commented Code:** Remove commented code and TODOs before committing
5. **Explicit Types:** Use explicit type declarations in domain/application layers
6. **YAGNI Principle:** Don't implement features until they're actually needed
7. **No Stub Methods:** Never add methods to interfaces that aren't fully implemented
8. **Lombok Configuration:** `lombok.addLombokGeneratedAnnotation = true` (see lombok.config)

---

## Pre-Commit Checklist

Before committing code, verify:
- [ ] Code follows SRP (one responsibility per class/method)
- [ ] Tests exist and pass (TDD followed)
- [ ] No inverted dependencies (domain doesn't import infrastructure)
- [ ] Methods are under 20 lines
- [ ] Descriptive names (no abbreviations)
- [ ] Exceptions handled in appropriate layer
- [ ] Defensive copies for collection getters
- [ ] No commented code or unresolved TODOs
- [ ] Explicit type declarations (avoid `var` in domain)
- [ ] Coverage is at least 70% (`./gradlew check`)
