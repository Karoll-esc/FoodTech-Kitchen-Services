package com.foodtech.kitchen.infrastructure.rest;

import com.foodtech.kitchen.application.usecases.*;
import com.foodtech.kitchen.domain.model.Price;
import com.foodtech.kitchen.domain.model.Product;
import com.foodtech.kitchen.domain.model.ProductType;
import com.foodtech.kitchen.infrastructure.rest.dto.*;
import com.foodtech.kitchen.infrastructure.rest.mapper.ProductDtoMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * REST Controller for Product Catalog Management (HU-006).
 * 
 * <p>This controller provides HTTP endpoints for complete CRUD operations on the
 * product catalog. It acts as the REST adapter in the hexagonal architecture,
 * coordinating between HTTP requests and application use cases.</p>
 * 
 * <p><strong>Architecture:</strong> Infrastructure Layer - REST Adapter</p>
 * 
 * <p><strong>Responsibilities:</strong></p>
 * <ul>
 *   <li>Expose HTTP endpoints for product catalog operations</li>
 *   <li>Convert HTTP requests (DTOs) to domain objects</li>
 *   <li>Delegate business logic to application use cases</li>
 *   <li>Convert domain objects to HTTP responses (DTOs)</li>
 *   <li>Return appropriate HTTP status codes</li>
 * </ul>
 * 
 * <p><strong>RESTful Endpoints:</strong></p>
 * <table border="1">
 *   <tr>
 *     <th>Method</th>
 *     <th>Path</th>
 *     <th>Description</th>
 *     <th>Status Codes</th>
 *   </tr>
 *   <tr>
 *     <td>POST</td>
 *     <td>/api/products</td>
 *     <td>Create new product</td>
 *     <td>201 Created, 400 Bad Request, 409 Conflict</td>
 *   </tr>
 *   <tr>
 *     <td>GET</td>
 *     <td>/api/products/{id}</td>
 *     <td>Get product by ID</td>
 *     <td>200 OK, 404 Not Found</td>
 *   </tr>
 *   <tr>
 *     <td>GET</td>
 *     <td>/api/products</td>
 *     <td>Get all products (with filters)</td>
 *     <td>200 OK</td>
 *   </tr>
 *   <tr>
 *     <td>PUT</td>
 *     <td>/api/products/{id}</td>
 *     <td>Update product details</td>
 *     <td>200 OK, 400 Bad Request, 404 Not Found</td>
 *   </tr>
 *   <tr>
 *     <td>PATCH</td>
 *     <td>/api/products/{id}/availability</td>
 *     <td>Update availability status</td>
 *     <td>200 OK, 404 Not Found</td>
 *   </tr>
 *   <tr>
 *     <td>DELETE</td>
 *     <td>/api/products/{id}</td>
 *     <td>Delete product (idempotent)</td>
 *     <td>204 No Content</td>
 *   </tr>
 * </table>
 * 
 * <p><strong>Design Patterns:</strong></p>
 * <ul>
 *   <li>Controller Pattern: Coordinates HTTP requests with business logic</li>
 *   <li>Adapter Pattern: Adapts HTTP layer to application layer</li>
 *   <li>Dependency Injection: Use cases injected via constructor</li>
 *   <li>SRP: Only coordinates requests, delegates all logic to use cases</li>
 * </ul>
 * 
 * <p><strong>Error Handling:</strong></p>
 * <p>Exceptions thrown by use cases are handled by GlobalExceptionHandler:</p>
 * <ul>
 *   <li>ProductNotFoundException → 404 Not Found</li>
 *   <li>ProductAlreadyExistsException → 409 Conflict</li>
 *   <li>IllegalArgumentException → 400 Bad Request</li>
 * </ul>
 * 
 * <p><strong>HU Relacionada:</strong> HU-006 Gestión del Catálogo de Productos</p>
 * 
 * @see CreateProductUseCase
 * @see GetProductByIdUseCase
 * @see GetProductsUseCase
 * @see UpdateProductUseCase
 * @see UpdateProductAvailabilityUseCase
 * @see DeleteProductUseCase
 * @see ProductDtoMapper
 * @see GlobalExceptionHandler
 */
@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final CreateProductUseCase createProductUseCase;
    private final GetProductByIdUseCase getProductByIdUseCase;
    private final GetProductsUseCase getProductsUseCase;
    private final UpdateProductUseCase updateProductUseCase;
    private final UpdateProductAvailabilityUseCase updateProductAvailabilityUseCase;
    private final DeleteProductUseCase deleteProductUseCase;

    public ProductController(
            CreateProductUseCase createProductUseCase,
            GetProductByIdUseCase getProductByIdUseCase,
            GetProductsUseCase getProductsUseCase,
            UpdateProductUseCase updateProductUseCase,
            UpdateProductAvailabilityUseCase updateProductAvailabilityUseCase,
            DeleteProductUseCase deleteProductUseCase) {
        this.createProductUseCase = createProductUseCase;
        this.getProductByIdUseCase = getProductByIdUseCase;
        this.getProductsUseCase = getProductsUseCase;
        this.updateProductUseCase = updateProductUseCase;
        this.updateProductAvailabilityUseCase = updateProductAvailabilityUseCase;
        this.deleteProductUseCase = deleteProductUseCase;
    }

    /**
     * Creates a new product in the catalog.
     * 
     * <p>Receives a product creation request, validates business rules (name uniqueness,
     * valid type, positive price, positive preparation time), and persists the new product
     * with default availability status (true) and timestamps.</p>
     * 
     * <p><strong>Endpoint:</strong> POST /api/products</p>
     * 
     * <p><strong>Request Body:</strong></p>
     * <pre>
     * {
     *   "name": "Coca Cola",
     *   "description": "Refreshing carbonated drink",
     *   "type": "DRINK",
     *   "price": 2.50,
     *   "preparationTimeSeconds": 30
     * }
     * </pre>
     * 
     * <p><strong>Response Example (201 Created):</strong></p>
     * <pre>
     * {
     *   "id": 1,
     *   "name": "Coca Cola",
     *   "description": "Refreshing carbonated drink",
     *   "type": "DRINK",
     *   "price": 2.50,
     *   "preparationTimeSeconds": 30,
     *   "available": true,
     *   "createdAt": "2026-01-21T10:30:00",
     *   "updatedAt": "2026-01-21T10:30:00"
     * }
     * </pre>
     * 
     * <p><strong>HTTP Status Codes:</strong></p>
     * <ul>
     *   <li>201 Created - Product successfully created</li>
     *   <li>400 Bad Request - Invalid input (null/empty name, invalid type, negative price, etc.)</li>
     *   <li>409 Conflict - Product with same name already exists</li>
     * </ul>
     * 
     * <p><strong>Business Rules Enforced:</strong></p>
     * <ul>
     *   <li>Product name must be unique (case-sensitive)</li>
     *   <li>Name cannot be null or blank</li>
     *   <li>Type must be valid ProductType enum (DRINK, HOT_DISH, COLD_DISH)</li>
     *   <li>Price must be positive (> 0)</li>
     *   <li>Preparation time must be positive (> 0 seconds)</li>
     * </ul>
     * 
     * @param request the product creation request containing name, description, type, price, and preparation time
     * @return ResponseEntity with 201 status and created product details in response body
     * @throws ProductAlreadyExistsException if a product with the same name already exists (handled by GlobalExceptionHandler → 409)
     * @throws IllegalArgumentException if validation fails (handled by GlobalExceptionHandler → 400)
     * @see CreateProductUseCase
     * @see ProductDtoMapper#toDomain(CreateProductRequest)
     */
    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(@RequestBody CreateProductRequest request) {
        Product product = ProductDtoMapper.toDomain(request);
        Product createdProduct = createProductUseCase.execute(product);
        ProductResponse response = ProductDtoMapper.toResponse(createdProduct);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Retrieves a product by its unique identifier.
     * 
     * <p>Fetches a single product from the catalog using its database ID.
     * Returns complete product details including metadata (timestamps, availability).</p>
     * 
     * <p><strong>Endpoint:</strong> GET /api/products/{id}</p>
     * 
     * <p><strong>Path Parameter:</strong></p>
     * <ul>
     *   <li><code>id</code> (Long) - Unique product identifier</li>
     * </ul>
     * 
     * <p><strong>Response Example (200 OK):</strong></p>
     * <pre>
     * {
     *   "id": 1,
     *   "name": "Coca Cola",
     *   "description": "Refreshing carbonated drink",
     *   "type": "DRINK",
     *   "price": 2.50,
     *   "preparationTimeSeconds": 30,
     *   "available": true,
     *   "createdAt": "2026-01-21T10:30:00",
     *   "updatedAt": "2026-01-21T10:30:00"
     * }
     * </pre>
     * 
     * <p><strong>HTTP Status Codes:</strong></p>
     * <ul>
     *   <li>200 OK - Product found and returned</li>
     *   <li>404 Not Found - No product exists with the given ID</li>
     * </ul>
     * 
     * @param id the unique identifier of the product to retrieve
     * @return ResponseEntity with 200 status and product details in response body
     * @throws ProductNotFoundException if no product exists with the given ID (handled by GlobalExceptionHandler → 404)
     * @see GetProductByIdUseCase
     * @see ProductDtoMapper#toResponse(Product)
     */
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable Long id) {
        Product product = getProductByIdUseCase.execute(id);
        ProductResponse response = ProductDtoMapper.toResponse(product);
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves all products from the catalog with optional filtering.
     * 
     * <p>Returns a list of all products, optionally filtered by availability status
     * and/or product type. If no filters are provided, returns all products in the catalog.
     * Results are ordered by ID (default database ordering).</p>
     * 
     * <p><strong>Endpoint:</strong> GET /api/products</p>
     * 
     * <p><strong>Query Parameters (optional):</strong></p>
     * <ul>
     *   <li><code>available</code> (Boolean) - Filter by availability status (true/false)</li>
     *   <li><code>type</code> (String) - Filter by product type (DRINK, HOT_DISH, COLD_DISH)</li>
     * </ul>
     * 
     * <p><strong>Request Examples:</strong></p>
     * <ul>
     *   <li>GET /api/products - All products</li>
     *   <li>GET /api/products?available=true - Only available products</li>
     *   <li>GET /api/products?type=DRINK - Only drinks</li>
     *   <li>GET /api/products?available=true&type=HOT_DISH - Available hot dishes</li>
     * </ul>
     * 
     * <p><strong>Response Example (200 OK):</strong></p>
     * <pre>
     * [
     *   {
     *     "id": 1,
     *     "name": "Coca Cola",
     *     "type": "DRINK",
     *     "price": 2.50,
     *     "available": true,
     *     ...
     *   },
     *   {
     *     "id": 2,
     *     "name": "Hamburger",
     *     "type": "HOT_DISH",
     *     "price": 8.50,
     *     "available": false,
     *     ...
     *   }
     * ]
     * </pre>
     * 
     * <p><strong>HTTP Status Codes:</strong></p>
     * <ul>
     *   <li>200 OK - Products retrieved successfully (empty list if no matches)</li>
     *   <li>400 Bad Request - Invalid type value (if not DRINK, HOT_DISH, or COLD_DISH)</li>
     * </ul>
     * 
     * <p><strong>Note:</strong> Returns empty list if no products match the filters.</p>
     * 
     * @param available optional filter for product availability status (true/false/null for all)
     * @param type optional filter for product type (DRINK/HOT_DISH/COLD_DISH, null for all)
     * @return ResponseEntity with 200 status and list of matching products (empty if none match)
     * @throws IllegalArgumentException if type parameter is not a valid ProductType enum value (handled by Spring → 400)
     * @see GetProductsUseCase
     * @see ProductType
     */
    @GetMapping
    public ResponseEntity<List<ProductResponse>> getProducts(
            @RequestParam(required = false) Boolean available,
            @RequestParam(required = false) String type) {
        
        ProductType productType = type != null ? ProductType.valueOf(type) : null;
        List<Product> products = getProductsUseCase.execute(available, productType);
        
        List<ProductResponse> response = products.stream()
            .map(ProductDtoMapper::toResponse)
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(response);
    }

    /**
     * Updates mutable fields of an existing product.
     * 
     * <p>Allows partial updates to a product's description, price, and preparation time.
     * Immutable fields (id, name, type, available, timestamps) cannot be changed via this endpoint.
     * All fields in the request are optional - only provided fields will be updated.</p>
     * 
     * <p><strong>Endpoint:</strong> PUT /api/products/{id}</p>
     * 
     * <p><strong>Path Parameter:</strong></p>
     * <ul>
     *   <li><code>id</code> (Long) - Unique identifier of the product to update</li>
     * </ul>
     * 
     * <p><strong>Request Body (all fields optional):</strong></p>
     * <pre>
     * {
     *   "description": "Updated description",
     *   "price": 3.50,
     *   "preparationTimeSeconds": 45
     * }
     * </pre>
     * 
     * <p><strong>Response Example (200 OK):</strong></p>
     * <pre>
     * {
     *   "id": 1,
     *   "name": "Coca Cola",
     *   "description": "Updated description",
     *   "type": "DRINK",
     *   "price": 3.50,
     *   "preparationTimeSeconds": 45,
     *   "available": true,
     *   "createdAt": "2026-01-21T10:30:00",
     *   "updatedAt": "2026-01-21T11:15:00"
     * }
     * </pre>
     * 
     * <p><strong>HTTP Status Codes:</strong></p>
     * <ul>
     *   <li>200 OK - Product successfully updated</li>
     *   <li>400 Bad Request - Invalid input (negative price, negative preparation time)</li>
     *   <li>404 Not Found - No product exists with the given ID</li>
     * </ul>
     * 
     * <p><strong>Business Rules Enforced:</strong></p>
     * <ul>
     *   <li>Price must be positive if provided (> 0)</li>
     *   <li>Preparation time must be positive if provided (> 0 seconds)</li>
     *   <li>Product must exist (throws ProductNotFoundException if not found)</li>
     *   <li>updatedAt timestamp is automatically refreshed</li>
     * </ul>
     * 
     * <p><strong>Immutable Fields:</strong></p>
     * <ul>
     *   <li>id - Cannot be changed</li>
     *   <li>name - Cannot be changed (would violate uniqueness constraint)</li>
     *   <li>type - Cannot be changed (use DELETE + CREATE instead)</li>
     *   <li>available - Use PATCH /api/products/{id}/availability instead</li>
     *   <li>createdAt - System-managed timestamp</li>
     * </ul>
     * 
     * @param id the unique identifier of the product to update
     * @param request the update request containing optional new values for description, price, and/or preparation time
     * @return ResponseEntity with 200 status and updated product details in response body
     * @throws ProductNotFoundException if no product exists with the given ID (handled by GlobalExceptionHandler → 404)
     * @throws IllegalArgumentException if validation fails (negative values) (handled by GlobalExceptionHandler → 400)
     * @see UpdateProductUseCase
     * @see Price
     */
    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> updateProduct(
            @PathVariable Long id,
            @RequestBody UpdateProductRequest request) {
        
        Price price = request.price() != null ? new Price(request.price()) : null;
        
        Product updatedProduct = updateProductUseCase.execute(
            id,
            request.description(),
            price,
            request.preparationTimeSeconds()
        );
        
        ProductResponse response = ProductDtoMapper.toResponse(updatedProduct);
        return ResponseEntity.ok(response);
    }

    /**
     * Updates the availability status of a product.
     * 
     * <p>Toggles whether a product is available for ordering. This is a dedicated endpoint
     * for availability management, separate from general product updates (PUT). Commonly used
     * to temporarily disable products that are out of stock without removing them from the catalog.</p>
     * 
     * <p><strong>Endpoint:</strong> PATCH /api/products/{id}/availability</p>
     * 
     * <p><strong>Path Parameter:</strong></p>
     * <ul>
     *   <li><code>id</code> (Long) - Unique identifier of the product to update</li>
     * </ul>
     * 
     * <p><strong>Request Body:</strong></p>
     * <pre>
     * {
     *   "available": false
     * }
     * </pre>
     * 
     * <p><strong>Response Example (200 OK):</strong></p>
     * <pre>
     * {
     *   "id": 1,
     *   "name": "Coca Cola",
     *   "description": "Refreshing carbonated drink",
     *   "type": "DRINK",
     *   "price": 2.50,
     *   "preparationTimeSeconds": 30,
     *   "available": false,
     *   "createdAt": "2026-01-21T10:30:00",
     *   "updatedAt": "2026-01-21T12:00:00"
     * }
     * </pre>
     * 
     * <p><strong>HTTP Status Codes:</strong></p>
     * <ul>
     *   <li>200 OK - Availability status successfully updated</li>
     *   <li>404 Not Found - No product exists with the given ID</li>
     * </ul>
     * 
     * <p><strong>Use Cases:</strong></p>
     * <ul>
     *   <li>Mark product as unavailable when out of stock</li>
     *   <li>Re-enable product when stock is replenished</li>
     *   <li>Temporarily disable product during kitchen issues</li>
     *   <li>Seasonal products (enable/disable based on season)</li>
     * </ul>
     * 
     * <p><strong>Note:</strong> updatedAt timestamp is automatically refreshed when availability changes.</p>
     * 
     * @param id the unique identifier of the product to update
     * @param request the availability update request containing the new availability status (true/false)
     * @return ResponseEntity with 200 status and updated product details in response body
     * @throws ProductNotFoundException if no product exists with the given ID (handled by GlobalExceptionHandler → 404)
     * @see UpdateProductAvailabilityUseCase
     */
    @PatchMapping("/{id}/availability")
    public ResponseEntity<ProductResponse> updateProductAvailability(
            @PathVariable Long id,
            @RequestBody UpdateProductAvailabilityRequest request) {
        
        Product updatedProduct = updateProductAvailabilityUseCase.execute(id, request.available());
        ProductResponse response = ProductDtoMapper.toResponse(updatedProduct);
        return ResponseEntity.ok(response);
    }

    /**
     * Deletes a product from the catalog.
     * 
     * <p>Permanently removes a product from the catalog database. This operation is
     * <strong>idempotent</strong> - calling DELETE on a non-existent product ID returns
     * 204 No Content (same as successful deletion), following REST best practices.</p>
     * 
     * <p><strong>Endpoint:</strong> DELETE /api/products/{id}</p>
     * 
     * <p><strong>Path Parameter:</strong></p>
     * <ul>
     *   <li><code>id</code> (Long) - Unique identifier of the product to delete</li>
     * </ul>
     * 
     * <p><strong>HTTP Status Codes:</strong></p>
     * <ul>
     *   <li>204 No Content - Product successfully deleted (or already didn't exist)</li>
     * </ul>
     * 
     * <p><strong>Idempotency:</strong></p>
     * <p>This endpoint is idempotent, meaning multiple DELETE requests for the same product ID
     * will always return 204 No Content, regardless of whether the product exists or not.
     * This design follows HTTP specification RFC 7231 for DELETE operations:</p>
     * <ul>
     *   <li>First DELETE call: Product exists → deleted → 204 No Content</li>
     *   <li>Second DELETE call: Product doesn't exist → still 204 No Content</li>
     * </ul>
     * 
     * <p><strong>Behavior:</strong></p>
     * <ul>
     *   <li>If product exists: Removes from database and returns 204</li>
     *   <li>If product doesn't exist: Catches ProductNotFoundException and still returns 204</li>
     *   <li>No response body in either case (DELETE successful = no content)</li>
     * </ul>
     * 
     * <p><strong>Warning:</strong> This operation is permanent and cannot be undone.
     * Consider using PATCH /api/products/{id}/availability to disable products instead
     * of deleting them if you need to preserve historical data.</p>
     * 
     * <p><strong>Use Cases:</strong></p>
     * <ul>
     *   <li>Remove discontinued products from catalog</li>
     *   <li>Clean up test/demo products</li>
     *   <li>Remove duplicate entries</li>
     * </ul>
     * 
     * @param id the unique identifier of the product to delete
     * @return ResponseEntity with 204 No Content status and empty body
     * @see DeleteProductUseCase
     * @see <a href="https://www.rfc-editor.org/rfc/rfc7231#section-4.3.5">RFC 7231 - DELETE Method</a>
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        try {
            deleteProductUseCase.execute(id);
        } catch (com.foodtech.kitchen.application.exception.ProductNotFoundException e) {
            // DELETE is idempotent - return 204 even if product doesn't exist
        }
        return ResponseEntity.noContent().build();
    }
}
