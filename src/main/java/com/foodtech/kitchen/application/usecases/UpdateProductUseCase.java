package com.foodtech.kitchen.application.usecases;

import com.foodtech.kitchen.application.exception.ProductNotFoundException;
import com.foodtech.kitchen.application.ports.out.ProductRepository;
import com.foodtech.kitchen.domain.model.Price;
import com.foodtech.kitchen.domain.model.Product;
import com.foodtech.kitchen.domain.services.ProductValidator;

/**
 * Command use case for updating mutable product fields in the catalog.
 * 
 * <p>This use case allows updating product details while preserving business
 * identifiers and audit fields. It supports partial updates where only non-null
 * fields are modified, keeping existing values for null parameters.</p>
 * 
 * <h2>Mutable Fields (can be updated):</h2>
 * <ul>
 *   <li><b>description:</b> Product description text</li>
 *   <li><b>price:</b> Product price (Price value object)</li>
 *   <li><b>preparationTime:</b> Time required to prepare the product (in seconds)</li>
 * </ul>
 * 
 * <h2>Immutable Fields (never modified):</h2>
 * <ul>
 *   <li><b>id:</b> Database primary key</li>
 *   <li><b>name:</b> Business identifier (unique product name)</li>
 *   <li><b>type:</b> Product category (changing type would alter business meaning)</li>
 *   <li><b>createdAt:</b> Audit field - original creation timestamp</li>
 * </ul>
 * 
 * <h2>Automatic Behavior:</h2>
 * <ul>
 *   <li>The <code>updatedAt</code> timestamp is automatically updated</li>
 *   <li>Null parameters preserve existing field values (partial updates)</li>
 *   <li>Validation occurs after updates to ensure data integrity</li>
 * </ul>
 * 
 * <h2>Validation:</h2>
 * <p>Product validation is performed in two layers:</p>
 * <ol>
 *   <li><b>Domain Layer:</b> Product.updateDetails() validates business rules</li>
 *   <li><b>Application Layer:</b> ProductValidator performs additional checks</li>
 * </ol>
 * 
 * <h2>Usage Examples:</h2>
 * <pre>{@code
 * // Example 1: Update all mutable fields
 * Price newPrice = new Price(new BigDecimal("15.99"));
 * Product updated = useCase.execute(1L, "New description", newPrice, 600);
 * 
 * // Example 2: Update only description (partial update)
 * Product updated = useCase.execute(1L, "Updated description", null, null);
 * 
 * // Example 3: Update only price
 * Price newPrice = new Price(new BigDecimal("12.50"));
 * Product updated = useCase.execute(1L, null, newPrice, null);
 * 
 * // Example 4: Update only preparation time
 * Product updated = useCase.execute(1L, null, null, 900);
 * 
 * // Example 5: Update price and preparation time
 * Price newPrice = new Price(new BigDecimal("18.00"));
 * Product updated = useCase.execute(1L, null, newPrice, 1200);
 * }</pre>
 * 
 * <h2>Error Handling:</h2>
 * <ul>
 *   <li><b>ProductNotFoundException:</b> Thrown if product ID doesn't exist</li>
 *   <li><b>IllegalArgumentException:</b> Thrown for invalid field values (negative price, etc.)</li>
 * </ul>
 * 
 * <h2>Architecture Notes:</h2>
 * <ul>
 *   <li><b>Layer:</b> Application Layer (Use Case)</li>
 *   <li><b>Pattern:</b> Command Pattern (state-changing operation)</li>
 *   <li><b>Dependencies:</b> ProductRepository (output port), ProductValidator (domain service)</li>
 *   <li><b>Framework:</b> NO Spring annotations (Pure Java)</li>
 *   <li><b>Testing:</b> Unit tests with Mockito (no Spring context required)</li>
 *   <li><b>Transaction:</b> Caller responsible for transaction boundaries</li>
 * </ul>
 * 
 * <h2>Design Rationale:</h2>
 * <p><b>Why are name and type immutable?</b></p>
 * <ul>
 *   <li><b>name:</b> Acts as business identifier; changing it would effectively create a new product</li>
 *   <li><b>type:</b> Determines kitchen station assignment; changing it would affect operational workflows</li>
 * </ul>
 * <p>To change these fields, the recommended approach is to create a new product and
 * mark the old one as unavailable.</p>
 * 
 * @see Product
 * @see ProductRepository
 * @see ProductValidator
 * @see ProductNotFoundException
 * @see Price
 */
public class UpdateProductUseCase {

    private final ProductRepository productRepository;
    private final ProductValidator productValidator;

    /**
     * Constructs the use case with required dependencies.
     * 
     * @param productRepository repository for accessing product data (must not be null)
     * @param productValidator validator for product business rules (must not be null)
     * @throws IllegalArgumentException if any dependency is null
     */
    public UpdateProductUseCase(ProductRepository productRepository, ProductValidator productValidator) {
        validateDependencies(productRepository, productValidator);
        this.productRepository = productRepository;
        this.productValidator = productValidator;
    }

    /**
     * Executes the update operation for a product.
     * 
     * <p>Updates the specified mutable fields and automatically updates the
     * <code>updatedAt</code> timestamp. Null parameters preserve existing values.</p>
     * 
     * @param id the ID of the product to update (must exist)
     * @param description new description (null to keep existing)
     * @param price new price (null to keep existing)
     * @param preparationTime new preparation time in seconds (null to keep existing)
     * @return the updated product with new timestamp
     * @throws ProductNotFoundException if product with given ID doesn't exist
     * @throws IllegalArgumentException if updated values violate business rules
     */
    public Product execute(Long id, String description, Price price, Integer preparationTime) {
        Product product = findExistingProduct(id);
        updateProductFields(product, description, price, preparationTime);
        validateUpdatedProduct(product);
        return saveProduct(product);
    }

    /**
     * Validates that all required dependencies are not null.
     * 
     * @param productRepository the repository to validate
     * @param productValidator the validator to validate
     * @throws IllegalArgumentException if any dependency is null
     */
    private void validateDependencies(ProductRepository productRepository, ProductValidator productValidator) {
        if (productRepository == null) {
            throw new IllegalArgumentException("ProductRepository cannot be null");
        }
        if (productValidator == null) {
            throw new IllegalArgumentException("ProductValidator cannot be null");
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
     * Updates the mutable fields of a product.
     * 
     * <p>Delegates to Product.updateDetails() which handles validation
     * and timestamp updates automatically.</p>
     * 
     * @param product the product to update
     * @param description new description (null to skip)
     * @param price new price (null to skip)
     * @param preparationTime new preparation time (null to skip)
     */
    private void updateProductFields(Product product, String description, Price price, Integer preparationTime) {
        product.updateDetails(description, price, preparationTime);
    }

    /**
     * Validates the updated product using business rules.
     * 
     * @param product the product to validate
     * @throws IllegalArgumentException if validation fails
     */
    private void validateUpdatedProduct(Product product) {
        productValidator.validate(product);
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
