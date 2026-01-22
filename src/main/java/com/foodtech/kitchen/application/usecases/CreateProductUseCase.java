package com.foodtech.kitchen.application.usecases;

import com.foodtech.kitchen.application.exception.ProductAlreadyExistsException;
import com.foodtech.kitchen.application.ports.out.ProductRepository;
import com.foodtech.kitchen.domain.model.Product;
import com.foodtech.kitchen.domain.services.ProductValidator;

/**
 * Use case for creating new products in the catalog.
 * <p>
 * This use case orchestrates the creation of a new product, ensuring that:
 * <ul>
 *   <li>The product passes all domain validations</li>
 *   <li>No duplicate product names exist in the catalog</li>
 *   <li>The product is persisted with default availability status (true)</li>
 * </ul>
 * </p>
 *
 * <h2>Architecture Notes</h2>
 * <ul>
 *   <li><strong>Layer:</strong> Application Layer (Use Cases)</li>
 *   <li><strong>Pattern:</strong> Use Case / Command Pattern</li>
 *   <li><strong>Dependencies:</strong> Domain services and output ports (NO Spring annotations)</li>
 *   <li><strong>SOLID:</strong> Single Responsibility - only handles product creation logic</li>
 * </ul>
 *
 * <h2>Business Rules</h2>
 * <ul>
 *   <li>Product names must be unique across the catalog</li>
 *   <li>All product attributes must pass domain validation</li>
 *   <li>New products are created with available=true by default</li>
 *   <li>Timestamps (createdAt, updatedAt) are set automatically</li>
 * </ul>
 *
 * <h2>Usage Example</h2>
 * <pre>{@code
 * // Setup dependencies
 * ProductRepository productRepository = new ProductRepositoryAdapter(...);
 * ProductValidator productValidator = new ProductValidator();
 * CreateProductUseCase useCase = new CreateProductUseCase(productRepository, productValidator);
 * 
 * // Create new product
 * Product newProduct = new Product(
 *     "Hamburguesa Clásica",
 *     "Hamburguesa de carne con lechuga y tomate",
 *     ProductType.PASTRY,
 *     new Price(new BigDecimal("15.50")),
 *     300
 * );
 * 
 * try {
 *     Product savedProduct = useCase.execute(newProduct);
 *     // Product created successfully with ID assigned
 * } catch (ProductAlreadyExistsException e) {
 *     // Handle duplicate product name
 * } catch (IllegalArgumentException e) {
 *     // Handle validation errors
 * }
 * }</pre>
 *
 * @see Product
 * @see ProductValidator
 * @see ProductRepository
 * @see ProductAlreadyExistsException
 */
public class CreateProductUseCase {

    private final ProductRepository productRepository;
    private final ProductValidator productValidator;

    /**
     * Constructs a new CreateProductUseCase with required dependencies.
     * <p>
     * This constructor validates that all dependencies are provided,
     * following defensive programming practices.
     * </p>
     *
     * @param productRepository repository for product persistence operations
     * @param productValidator validator for product business rules
     * @throws IllegalArgumentException if any parameter is null
     */
    public CreateProductUseCase(ProductRepository productRepository, ProductValidator productValidator) {
        if (productRepository == null) {
            throw new IllegalArgumentException("ProductRepository cannot be null");
        }
        if (productValidator == null) {
            throw new IllegalArgumentException("ProductValidator cannot be null");
        }
        this.productRepository = productRepository;
        this.productValidator = productValidator;
    }

    /**
     * Executes the product creation use case.
     * <p>
     * This method performs the following steps in order:
     * <ol>
     *   <li>Validates the product is not null</li>
     *   <li>Validates product attributes using ProductValidator</li>
     *   <li>Checks for duplicate product names in the catalog</li>
     *   <li>Persists the product with assigned ID and timestamps</li>
     * </ol>
     * </p>
     *
     * @param product the product to create (must not be null)
     * @return the created product with ID assigned and timestamps set
     * @throws IllegalArgumentException if product is null or fails validation
     * @throws ProductAlreadyExistsException if a product with the same name already exists
     */
    public Product execute(Product product) {
        validateProductNotNull(product);
        validateProductAttributes(product);
        checkForDuplicateName(product.getName());
        return saveProduct(product);
    }

    private void validateProductNotNull(Product product) {
        if (product == null) {
            throw new IllegalArgumentException("Product cannot be null");
        }
    }

    private void validateProductAttributes(Product product) {
        productValidator.validate(product);
    }

    private void checkForDuplicateName(String productName) {
        if (productRepository.existsByName(productName)) {
            throw new ProductAlreadyExistsException(productName);
        }
    }

    private Product saveProduct(Product product) {
        return productRepository.save(product);
    }
}
