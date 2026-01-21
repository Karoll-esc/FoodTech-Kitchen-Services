package com.foodtech.kitchen.application.exception;

/**
 * Exception thrown when attempting to access a product that does not exist.
 * <p>
 * This exception is thrown when a product lookup by ID fails to find the
 * requested product. It indicates that the requested resource does not exist
 * in the product catalog.
 * </p>
 *
 * <h2>Architecture Notes</h2>
 * <ul>
 *   <li><strong>Layer:</strong> Application Layer (Use Case Exceptions)</li>
 *   <li><strong>Pattern:</strong> Domain Exception</li>
 *   <li><strong>HTTP Mapping:</strong> Should map to 404 NOT FOUND in REST controllers</li>
 * </ul>
 *
 * <h2>Usage Example</h2>
 * <pre>{@code
 * public class GetProductByIdUseCase {
 *     public Product execute(Long productId) {
 *         return productRepository.findById(productId)
 *             .orElseThrow(() -> new ProductNotFoundException(productId));
 *     }
 * }
 * }</pre>
 */
public class ProductNotFoundException extends RuntimeException {

    private final Long productId;

    /**
     * Constructs a new ProductNotFoundException with the missing product ID.
     *
     * @param productId the ID of the product that was not found
     */
    public ProductNotFoundException(Long productId) {
        super(String.format("Product not found with id: %d", productId));
        this.productId = productId;
    }

    /**
     * Gets the ID of the product that was not found.
     *
     * @return the product ID
     */
    public Long getProductId() {
        return productId;
    }
}
