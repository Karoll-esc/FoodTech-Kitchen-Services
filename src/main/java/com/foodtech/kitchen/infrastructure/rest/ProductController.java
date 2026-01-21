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
 * Provides HTTP endpoints for CRUD operations on products.
 * Coordinates between REST layer and application use cases.
 * 
 * Endpoints:
 * - POST /api/products - Create product
 * - GET /api/products/{id} - Get product by ID
 * - GET /api/products - Get all products (with optional filters)
 * - PUT /api/products/{id} - Update product
 * - PATCH /api/products/{id}/availability - Update availability
 * - DELETE /api/products/{id} - Delete product
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
     * POST /api/products - Create a new product.
     */
    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(@RequestBody CreateProductRequest request) {
        Product product = ProductDtoMapper.toDomain(request);
        Product createdProduct = createProductUseCase.execute(product);
        ProductResponse response = ProductDtoMapper.toResponse(createdProduct);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * GET /api/products/{id} - Get product by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable Long id) {
        Product product = getProductByIdUseCase.execute(id);
        ProductResponse response = ProductDtoMapper.toResponse(product);
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/products - Get all products with optional filters.
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
     * PUT /api/products/{id} - Update product details.
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
     * PATCH /api/products/{id}/availability - Update product availability.
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
     * DELETE /api/products/{id} - Delete product.
     * 
     * Idempotent operation - returns 204 whether product exists or not.
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
