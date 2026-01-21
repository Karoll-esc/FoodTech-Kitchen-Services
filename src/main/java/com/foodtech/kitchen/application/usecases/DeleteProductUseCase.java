package com.foodtech.kitchen.application.usecases;

import com.foodtech.kitchen.application.exception.ProductNotFoundException;
import com.foodtech.kitchen.application.ports.out.ProductRepository;

/**
 * Use case for deleting a product from the catalog.
 * <p>
 * This use case handles the permanent removal of products from the catalog.
 * It follows a strict verification-before-deletion pattern to ensure that
 * only existing products are deleted, providing clear error messages when
 * attempting to delete non-existent products.
 * </p>
 *
 * <h2>Architecture Notes</h2>
 * <ul>
 *   <li><strong>Layer:</strong> Application Layer (Use Cases)</li>
 *   <li><strong>Pattern:</strong> Command Use Case / Delete Operation</li>
 *   <li><strong>Dependencies:</strong> Output ports only (NO Spring annotations)</li>
 *   <li><strong>SOLID:</strong> Single Responsibility - only handles product deletion</li>
 * </ul>
 *
 * <h2>Business Rules</h2>
 * <ul>
 *   <li>Product ID must not be null</li>
 *   <li>Product must exist before deletion</li>
 *   <li>Deletion is permanent (no soft delete)</li>
 *   <li>Must verify existence before attempting deletion</li>
 *   <li>Throws exception if product not found (not idempotent)</li>
 * </ul>
 *
 * <h2>Design Decisions</h2>
 * <p>
 * <strong>Why not idempotent?</strong> This use case explicitly checks for product
 * existence before deletion and throws ProductNotFoundException if the product
 * doesn't exist. This design choice ensures:
 * </p>
 * <ul>
 *   <li>Clear error reporting when attempting to delete non-existent products</li>
 *   <li>Prevents silent failures that could mask bugs in client code</li>
 *   <li>Maintains consistency with other use cases (GetById, Update)</li>
 * </ul>
 *
 * <h2>Usage Example</h2>
 * <pre>{@code
 * // Setup dependencies
 * ProductRepository productRepository = new ProductRepositoryAdapter(...);
 * DeleteProductUseCase useCase = new DeleteProductUseCase(productRepository);
 * 
 * // Delete product by ID
 * Long productId = 1L;
 * 
 * try {
 *     useCase.execute(productId);
 *     System.out.println("Product deleted successfully");
 * } catch (ProductNotFoundException e) {
 *     // Handle product not found
 *     System.err.println("Cannot delete: Product with ID " + e.getProductId() + " not found");
 * } catch (IllegalArgumentException e) {
 *     // Handle validation errors
 *     System.err.println("Invalid input: " + e.getMessage());
 * }
 * }</pre>
 *
 * @see ProductRepository
 * @see ProductNotFoundException
 */
public class DeleteProductUseCase {

    private final ProductRepository productRepository;

    /**
     * Constructs a new DeleteProductUseCase with required dependencies.
     * <p>
     * This constructor validates that the repository dependency is provided,
     * following defensive programming practices to fail fast at construction time.
     * </p>
     *
     * @param productRepository repository for product persistence operations
     * @throws IllegalArgumentException if productRepository is null
     */
    public DeleteProductUseCase(ProductRepository productRepository) {
        validateRepositoryNotNull(productRepository);
        this.productRepository = productRepository;
    }

    /**
     * Executes the product deletion use case.
     * <p>
     * This method performs the following steps in order:
     * <ol>
     *   <li>Validates the product ID is not null</li>
     *   <li>Verifies the product exists in the catalog</li>
     *   <li>Permanently deletes the product from the repository</li>
     * </ol>
     * </p>
     * <p>
     * <strong>Important:</strong> This operation is NOT idempotent. Calling this method
     * multiple times with the same ID will fail on the second call because the product
     * will no longer exist. This is intentional to provide clear error reporting.
     * </p>
     *
     * @param productId the unique identifier of the product to delete (must not be null)
     * @throws IllegalArgumentException if productId is null
     * @throws ProductNotFoundException if no product exists with the given ID
     */
    public void execute(Long productId) {
        validateProductIdNotNull(productId);
        verifyProductExists(productId);
        deleteProduct(productId);
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

    private void verifyProductExists(Long productId) {
        productRepository.findById(productId)
            .orElseThrow(() -> new ProductNotFoundException(productId));
    }

    private void deleteProduct(Long productId) {
        productRepository.deleteById(productId);
    }
}
