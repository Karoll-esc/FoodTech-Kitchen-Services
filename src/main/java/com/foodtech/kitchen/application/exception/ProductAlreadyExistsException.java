package com.foodtech.kitchen.application.exception;

/**
 * Exception thrown when attempting to create a product with a name that already exists.
 * <p>
 * This exception enforces the business rule that product names must be unique
 * across the catalog. It should be thrown during product creation if a duplicate
 * name is detected.
 * </p>
 *
 * <h2>Architecture Notes</h2>
 * <ul>
 *   <li><strong>Layer:</strong> Application Layer (Use Case Exceptions)</li>
 *   <li><strong>Pattern:</strong> Domain Exception</li>
 *   <li><strong>HTTP Mapping:</strong> Should map to 409 CONFLICT in REST controllers</li>
 * </ul>
 *
 * <h2>Usage Example</h2>
 * <pre>{@code
 * public class CreateProductUseCase {
 *     public Product execute(Product product) {
 *         if (productRepository.existsByName(product.getName())) {
 *             throw new ProductAlreadyExistsException(product.getName());
 *         }
 *         return productRepository.save(product);
 *     }
 * }
 * }</pre>
 */
public class ProductAlreadyExistsException extends RuntimeException {

    private final String productName;

    /**
     * Constructs a new ProductAlreadyExistsException with the duplicate product name.
     *
     * @param productName the name of the product that already exists
     */
    public ProductAlreadyExistsException(String productName) {
        super(String.format("Product with name '%s' already exists", productName));
        this.productName = productName;
    }

    /**
     * Gets the name of the product that caused the duplicate error.
     *
     * @return the duplicate product name
     */
    public String getProductName() {
        return productName;
    }
}
