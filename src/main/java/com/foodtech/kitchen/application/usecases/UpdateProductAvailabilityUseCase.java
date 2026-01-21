package com.foodtech.kitchen.application.usecases;

import com.foodtech.kitchen.application.exception.ProductNotFoundException;
import com.foodtech.kitchen.application.ports.out.ProductRepository;
import com.foodtech.kitchen.domain.model.Product;

/**
 * Command use case for updating product availability status in the catalog.
 * 
 * <p>This use case provides a focused operation for enabling or disabling products
 * without modifying any other product attributes. It's designed for operational
 * scenarios where products need to be temporarily unavailable (out of stock,
 * ingredients missing, etc.) without being deleted from the catalog.</p>
 * 
 * <h2>What It Does:</h2>
 * <ul>
 *   <li>Updates ONLY the <code>available</code> field (boolean)</li>
 *   <li>Automatically updates the <code>updatedAt</code> timestamp</li>
 *   <li>Preserves ALL other product fields (name, description, price, etc.)</li>
 * </ul>
 * 
 * <h2>Fields Modified:</h2>
 * <ul>
 *   <li><b>available:</b> Boolean flag indicating product availability</li>
 *   <li><b>updatedAt:</b> Timestamp of last modification (updated automatically)</li>
 * </ul>
 * 
 * <h2>Fields Preserved (never modified):</h2>
 * <ul>
 *   <li><b>id:</b> Database primary key</li>
 *   <li><b>name:</b> Product name</li>
 *   <li><b>description:</b> Product description</li>
 *   <li><b>type:</b> Product category (DRINK, HOT_DISH, COLD_DISH)</li>
 *   <li><b>price:</b> Product price</li>
 *   <li><b>preparationTime:</b> Preparation time in seconds</li>
 *   <li><b>createdAt:</b> Original creation timestamp</li>
 * </ul>
 * 
 * <h2>Behavioral Notes:</h2>
 * <ul>
 *   <li><b>Idempotent:</b> Setting availability to the same value updates timestamp</li>
 *   <li><b>No validation required:</b> Boolean value is always valid (true/false)</li>
 *   <li><b>Simple operation:</b> No complex business rules to validate</li>
 * </ul>
 * 
 * <h2>Usage Examples:</h2>
 * <pre>{@code
 * // Example 1: Disable a product (temporarily out of stock)
 * Product disabled = useCase.execute(1L, false);
 * assertFalse(disabled.isAvailable());
 * 
 * // Example 2: Re-enable a product (back in stock)
 * Product enabled = useCase.execute(1L, true);
 * assertTrue(enabled.isAvailable());
 * 
 * // Example 3: Handle inventory shortage
 * if (inventoryService.isOutOfStock(productId)) {
 *     useCase.execute(productId, false);  // Disable product
 * }
 * 
 * // Example 4: Bulk disable products by type
 * List<Product> hotDishes = getProductsByType(ProductType.HOT_DISH);
 * for (Product product : hotDishes) {
 *     if (kitchenClosed) {
 *         useCase.execute(product.getId(), false);
 *     }
 * }
 * 
 * // Example 5: Toggle availability
 * Product product = getProductById(1L);
 * boolean newAvailability = !product.isAvailable();
 * useCase.execute(1L, newAvailability);
 * }</pre>
 * 
 * <h2>Common Use Cases:</h2>
 * <ul>
 *   <li><b>Out of Stock:</b> Disable product when ingredients run out</li>
 *   <li><b>Kitchen Station Offline:</b> Disable all products for that station</li>
 *   <li><b>Temporary Menu Changes:</b> Disable seasonal items off-season</li>
 *   <li><b>Quality Issues:</b> Disable product during quality problems</li>
 *   <li><b>Restock:</b> Re-enable product when inventory is replenished</li>
 * </ul>
 * 
 * <h2>Error Handling:</h2>
 * <ul>
 *   <li><b>ProductNotFoundException:</b> Thrown if product ID doesn't exist</li>
 *   <li><b>IllegalArgumentException:</b> Thrown if repository is null (constructor)</li>
 * </ul>
 * 
 * <h2>Architecture Notes:</h2>
 * <ul>
 *   <li><b>Layer:</b> Application Layer (Use Case)</li>
 *   <li><b>Pattern:</b> Command Pattern (state-changing operation)</li>
 *   <li><b>Dependencies:</b> ProductRepository (output port) - NO validator needed</li>
 *   <li><b>Framework:</b> NO Spring annotations (Pure Java)</li>
 *   <li><b>Testing:</b> Unit tests with Mockito (no Spring context required)</li>
 *   <li><b>Transaction:</b> Caller responsible for transaction boundaries</li>
 * </ul>
 * 
 * <h2>Design Rationale:</h2>
 * <p><b>Why separate from UpdateProductUseCase?</b></p>
 * <ul>
 *   <li><b>Single Responsibility:</b> Availability changes are operationally different from content updates</li>
 *   <li><b>Simpler Contract:</b> No validation needed, just a boolean toggle</li>
 *   <li><b>Frequent Operation:</b> Availability changes happen more often than price/description updates</li>
 *   <li><b>Different Actors:</b> Kitchen staff toggle availability; managers update prices/descriptions</li>
 *   <li><b>Performance:</b> Lightweight operation without validation overhead</li>
 * </ul>
 * 
 * <p><b>Why does timestamp update even when value doesn't change?</b></p>
 * <p>This is intentional behavior that documents operational attempts. If a user
 * tries to enable an already-enabled product, the timestamp update records that
 * the operation was performed, which can be valuable for audit trails and
 * understanding system usage patterns.</p>
 * 
 * @see Product
 * @see ProductRepository
 * @see ProductNotFoundException
 * @see UpdateProductUseCase
 */
public class UpdateProductAvailabilityUseCase {

    private final ProductRepository productRepository;

    /**
     * Constructs the use case with required dependencies.
     * 
     * @param productRepository repository for accessing product data (must not be null)
     * @throws IllegalArgumentException if productRepository is null
     */
    public UpdateProductAvailabilityUseCase(ProductRepository productRepository) {
        validateRepository(productRepository);
        this.productRepository = productRepository;
    }

    /**
     * Executes the availability update operation for a product.
     * 
     * <p>Updates the product's availability status and automatically updates
     * the <code>updatedAt</code> timestamp. The timestamp is updated even if
     * the availability value doesn't change (idempotent behavior).</p>
     * 
     * @param id the ID of the product to update (must exist)
     * @param available the new availability status (true = available, false = unavailable)
     * @return the updated product with new availability and timestamp
     * @throws ProductNotFoundException if product with given ID doesn't exist
     */
    public Product execute(Long id, boolean available) {
        Product product = findExistingProduct(id);
        updateAvailability(product, available);
        return saveProduct(product);
    }

    /**
     * Validates that the repository dependency is not null.
     * 
     * @param productRepository the repository to validate
     * @throws IllegalArgumentException if repository is null
     */
    private void validateRepository(ProductRepository productRepository) {
        if (productRepository == null) {
            throw new IllegalArgumentException("ProductRepository cannot be null");
        }
    }

    /**
     * Finds an existing product by ID.
     * 
     * @param id the product ID to search for
     * @return the found product
     * @throws ProductNotFoundException if product doesn't exist
     */
    private Product findExistingProduct(Long id) {
        return productRepository.findById(id)
            .orElseThrow(() -> new ProductNotFoundException(id));
    }

    /**
     * Updates the availability status of a product and its timestamp.
     * 
     * <p>This method updates both the availability flag and the updatedAt
     * timestamp, ensuring that any availability change is properly tracked.</p>
     * 
     * @param product the product to update
     * @param available the new availability status
     */
    private void updateAvailability(Product product, boolean available) {
        product.setAvailable(available);
        product.updateTimestamp();
    }

    /**
     * Persists the updated product to the repository.
     * 
     * @param product the product to save
     * @return the saved product
     */
    private Product saveProduct(Product product) {
        return productRepository.save(product);
    }
}
