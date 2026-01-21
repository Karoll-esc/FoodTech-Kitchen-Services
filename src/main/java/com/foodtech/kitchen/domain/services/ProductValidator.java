package com.foodtech.kitchen.domain.services;

import com.foodtech.kitchen.domain.model.Price;
import com.foodtech.kitchen.domain.model.Product;
import com.foodtech.kitchen.domain.model.ProductType;

/**
 * Domain service responsible for validating Product entities and their attributes.
 * <p>
 * This validator enforces business rules for product data integrity, ensuring that
 * all products meet the required constraints before being processed or persisted.
 * </p>
 *
 * <h2>Architecture Notes</h2>
 * <ul>
 *   <li><strong>Layer:</strong> Domain Layer (Hexagonal Architecture)</li>
 *   <li><strong>Dependencies:</strong> Pure Java - NO framework dependencies</li>
 *   <li><strong>Pattern:</strong> Domain Service (stateless validation logic)</li>
 *   <li><strong>SOLID:</strong> Single Responsibility - only validates products</li>
 * </ul>
 *
 * <h2>Validation Rules</h2>
 * <ul>
 *   <li><strong>Product Name:</strong> 1-100 characters, not null, not blank</li>
 *   <li><strong>Description:</strong> 0-500 characters, not null (empty string allowed)</li>
 *   <li><strong>Type:</strong> Must be valid ProductType enum value, not null</li>
 *   <li><strong>Price:</strong> Must be valid Price object (>= 0, 2 decimals), not null</li>
 *   <li><strong>Preparation Time:</strong> Must be > 0 seconds</li>
 * </ul>
 *
 * <h2>Usage Example</h2>
 * <pre>{@code
 * ProductValidator validator = new ProductValidator();
 * 
 * // Valid product
 * Product product = new Product(
 *     "Hamburguesa Clásica",
 *     "Hamburguesa de carne con lechuga y tomate",
 *     ProductType.HOT_DISH,
 *     new Price(new BigDecimal("15.50")),
 *     300
 * );
 * validator.validate(product); // No exception thrown
 * 
 * // Invalid product - throws IllegalArgumentException
 * Product invalidProduct = new Product("", "", ProductType.DRINK, null, -1);
 * validator.validate(invalidProduct); // Throws exception
 * }</pre>
 *
 * @see Product
 * @see Price
 * @see ProductType
 */
public class ProductValidator {

    /**
     * Validates all attributes of a product entity.
     * <p>
     * This method performs comprehensive validation of the product object,
     * checking all fields against business rules. Validation is performed
     * in a fail-fast manner - the first validation failure will throw an exception.
     * </p>
     *
     * @param product the product to validate
     * @throws IllegalArgumentException if product is null or any attribute violates business rules
     */
    public void validate(Product product) {
        if (product == null) {
            throw new IllegalArgumentException("Product cannot be null");
        }
        validateName(product.getName());
        validateDescription(product.getDescription());
        validateType(product.getType());
        validatePrice(product.getPrice());
        validatePreparationTime(product.getPreparationTimeSeconds());
    }

    /**
     * Validates product name according to business rules.
     * <p>
     * <strong>Business Rules:</strong>
     * <ul>
     *   <li>Name cannot be null</li>
     *   <li>Name cannot be blank (empty or only whitespace)</li>
     *   <li>Name cannot exceed 100 characters</li>
     * </ul>
     * </p>
     *
     * @param name the product name to validate
     * @throws IllegalArgumentException if name is null, blank, or exceeds 100 characters
     */
    public void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Product name cannot be null or empty");
        }
        if (name.length() > 100) {
            throw new IllegalArgumentException("Product name cannot exceed 100 characters");
        }
    }

    /**
     * Validates product description according to business rules.
     * <p>
     * <strong>Business Rules:</strong>
     * <ul>
     *   <li>Description cannot be null (empty string is allowed)</li>
     *   <li>Description cannot exceed 500 characters</li>
     * </ul>
     * </p>
     *
     * @param description the product description to validate
     * @throws IllegalArgumentException if description is null or exceeds 500 characters
     */
    public void validateDescription(String description) {
        if (description == null) {
            throw new IllegalArgumentException("Product description cannot be null");
        }
        if (description.length() > 500) {
            throw new IllegalArgumentException("Product description cannot exceed 500 characters");
        }
    }

    /**
     * Validates product type according to business rules.
     * <p>
     * <strong>Business Rules:</strong>
     * <ul>
     *   <li>Type cannot be null</li>
     *   <li>Type must be a valid ProductType enum value (DRINK, HOT_DISH, COLD_DISH)</li>
     * </ul>
     * </p>
     *
     * @param type the product type to validate
     * @throws IllegalArgumentException if type is null
     */
    public void validateType(ProductType type) {
        if (type == null) {
            throw new IllegalArgumentException("Product type cannot be null");
        }
    }

    /**
     * Validates product price according to business rules.
     * <p>
     * <strong>Business Rules:</strong>
     * <ul>
     *   <li>Price cannot be null</li>
     *   <li>Price must be a valid Price value object (>= 0, exactly 2 decimals)</li>
     * </ul>
     * </p>
     * <p>
     * Note: The Price value object itself enforces that the amount is non-negative
     * and rounded to 2 decimal places. This method only validates that the Price
     * object is not null.
     * </p>
     *
     * @param price the product price to validate
     * @throws IllegalArgumentException if price is null
     * @see Price
     */
    public void validatePrice(Price price) {
        if (price == null) {
            throw new IllegalArgumentException("Product price cannot be null");
        }
    }

    /**
     * Validates product preparation time according to business rules.
     * <p>
     * <strong>Business Rules:</strong>
     * <ul>
     *   <li>Preparation time must be greater than zero seconds</li>
     * </ul>
     * </p>
     * <p>
     * Rationale: A product cannot have zero or negative preparation time as
     * this would be meaningless for kitchen operations. Even instant items
     * require at least 1 second to process.
     * </p>
     *
     * @param preparationTimeSeconds the preparation time in seconds to validate
     * @throws IllegalArgumentException if preparation time is less than or equal to zero
     */
    public void validatePreparationTime(int preparationTimeSeconds) {
        if (preparationTimeSeconds <= 0) {
            throw new IllegalArgumentException("Preparation time must be greater than zero");
        }
    }
}
