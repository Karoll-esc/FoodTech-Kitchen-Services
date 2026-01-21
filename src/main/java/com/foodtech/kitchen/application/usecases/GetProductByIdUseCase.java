package com.foodtech.kitchen.application.usecases;

import com.foodtech.kitchen.application.exception.ProductNotFoundException;
import com.foodtech.kitchen.application.ports.out.ProductRepository;
import com.foodtech.kitchen.domain.model.Product;

/**
 * Use case for retrieving a product by its unique identifier.
 * <p>
 * This use case provides read-only access to product details from the catalog.
 * It follows the query pattern, returning product information without modifying state.
 * </p>
 *
 * <h2>Architecture Notes</h2>
 * <ul>
 *   <li><strong>Layer:</strong> Application Layer (Use Cases)</li>
 *   <li><strong>Pattern:</strong> Query Use Case / Read Operation</li>
 *   <li><strong>Dependencies:</strong> Output ports only (NO Spring annotations)</li>
 *   <li><strong>SOLID:</strong> Single Responsibility - only retrieves product by ID</li>
 * </ul>
 *
 * <h2>Business Rules</h2>
 * <ul>
 *   <li>Product ID must not be null</li>
 *   <li>Product must exist in the catalog</li>
 *   <li>Returns complete product with all attributes</li>
 *   <li>Read-only operation - no state modifications</li>
 * </ul>
 *
 * <h2>Usage Example</h2>
 * <pre>{@code
 * // Setup dependencies
 * ProductRepository productRepository = new ProductRepositoryAdapter(...);
 * GetProductByIdUseCase useCase = new GetProductByIdUseCase(productRepository);
 * 
 * // Retrieve product by ID
 * Long productId = 1L;
 * 
 * try {
 *     Product product = useCase.execute(productId);
 *     // Process product details
 *     System.out.println("Product: " + product.getName());
 *     System.out.println("Price: " + product.getPrice().getAmount());
 *     System.out.println("Available: " + product.isAvailable());
 * } catch (ProductNotFoundException e) {
 *     // Handle product not found
 *     System.err.println("Product with ID " + e.getProductId() + " not found");
 * } catch (IllegalArgumentException e) {
 *     // Handle validation errors
 *     System.err.println("Invalid input: " + e.getMessage());
 * }
 * }</pre>
 *
 * @see Product
 * @see ProductRepository
 * @see ProductNotFoundException
 */
public class GetProductByIdUseCase {

    private final ProductRepository productRepository;

    /**
     * Constructs a new GetProductByIdUseCase with required dependencies.
     * <p>
     * This constructor validates that the repository dependency is provided,
     * following defensive programming practices to fail fast at construction time.
     * </p>
     *
     * @param productRepository repository for product persistence operations
     * @throws IllegalArgumentException if productRepository is null
     */
    public GetProductByIdUseCase(ProductRepository productRepository) {
        validateRepositoryNotNull(productRepository);
        this.productRepository = productRepository;
    }

    /**
     * Executes the product retrieval use case.
     * <p>
     * This method performs the following steps in order:
     * <ol>
     *   <li>Validates the product ID is not null</li>
     *   <li>Queries the repository for the product</li>
     *   <li>Returns the product if found, or throws exception if not found</li>
     * </ol>
     * </p>
     *
     * @param productId the unique identifier of the product to retrieve (must not be null)
     * @return the product with the specified ID, including all attributes
     * @throws IllegalArgumentException if productId is null
     * @throws ProductNotFoundException if no product exists with the given ID
     */
    public Product execute(Long productId) {
        validateProductIdNotNull(productId);
        return findProductById(productId);
    }

    private void validateRepositoryNotNull(ProductRepository productRepository) {
        if (productRepository == null) {
            throw new IllegalArgumentException("ProductRepository cannot be null");
        }
    }

    private void validateProductIdNotNull(Long productId) {
        if (productId == null) {
            throw new IllegalArgumentException("Product ID cannot be null");
        }
    }

    private Product findProductById(Long productId) {
        return productRepository.findById(productId)
            .orElseThrow(() -> new ProductNotFoundException(productId));
    }
}
