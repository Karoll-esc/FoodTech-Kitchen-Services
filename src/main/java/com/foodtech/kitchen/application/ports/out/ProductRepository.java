package com.foodtech.kitchen.application.ports.out;

import com.foodtech.kitchen.domain.model.Product;
import com.foodtech.kitchen.domain.model.ProductType;

import java.util.List;
import java.util.Optional;

/**
 * Output port for Product persistence operations.
 * <p>
 * This interface defines the contract for product data access operations,
 * following the Hexagonal Architecture pattern. It acts as a port that will
 * be implemented by adapters in the infrastructure layer (e.g., JPA, MongoDB).
 * </p>
 *
 * <h2>Architecture Notes</h2>
 * <ul>
 *   <li><strong>Layer:</strong> Application Layer (Ports & Adapters)</li>
 *   <li><strong>Pattern:</strong> Repository Pattern / Output Port</li>
 *   <li><strong>Dependencies:</strong> Only Domain entities (NO framework dependencies)</li>
 *   <li><strong>SOLID:</strong> Interface Segregation - focused repository operations</li>
 * </ul>
 *
 * <h2>Implementation Guidelines</h2>
 * <ul>
 *   <li>Implementations must be in the infrastructure layer</li>
 *   <li>Use domain entities (Product), not persistence entities</li>
 *   <li>Return Optional for single-result queries that may not find data</li>
 *   <li>Return empty List (not null) for multi-result queries with no data</li>
 * </ul>
 *
 * <h2>Usage Example</h2>
 * <pre>{@code
 * public class CreateProductUseCase {
 *     private final ProductRepository productRepository;
 *     
 *     public Product execute(Product product) {
 *         if (productRepository.existsByName(product.getName())) {
 *             throw new ProductAlreadyExistsException(product.getName());
 *         }
 *         return productRepository.save(product);
 *     }
 * }
 * }</pre>
 *
 * @see Product
 * @see ProductType
 */
public interface ProductRepository {

    /**
     * Persists a product entity (create or update).
     * <p>
     * If the product has no ID, it will be created and assigned a new ID.
     * If the product has an ID, it will be updated.
     * </p>
     *
     * @param product the product to save
     * @return the saved product with ID assigned
     * @throws IllegalArgumentException if product is null
     */
    Product save(Product product);

    /**
     * Finds a product by its unique identifier.
     *
     * @param id the product ID
     * @return Optional containing the product if found, empty otherwise
     * @throws IllegalArgumentException if id is null
     */
    Optional<Product> findById(Long id);

    /**
     * Retrieves all products in the catalog.
     *
     * @return list of all products (empty list if none exist)
     */
    List<Product> findAll();

    /**
     * Finds products filtered by availability status.
     *
     * @param available true to find available products, false for unavailable
     * @return list of products matching the availability filter (empty list if none found)
     */
    List<Product> findByAvailable(boolean available);

    /**
     * Finds products filtered by product type.
     *
     * @param type the product type (DRINK, HOT_DISH, COLD_DISH)
     * @return list of products of the specified type (empty list if none found)
     * @throws IllegalArgumentException if type is null
     */
    List<Product> findByType(ProductType type);

    /**
     * Finds a product by its exact name.
     * <p>
     * This query is case-sensitive and must match the name exactly.
     * Used primarily for duplicate name validation.
     * </p>
     *
     * @param name the exact product name to search
     * @return Optional containing the product if found, empty otherwise
     * @throws IllegalArgumentException if name is null or blank
     */
    Optional<Product> findByName(String name);

    /**
     * Deletes a product by its ID.
     * <p>
     * This operation is idempotent - deleting a non-existent product
     * does not throw an exception.
     * </p>
     *
     * @param id the product ID to delete
     * @throws IllegalArgumentException if id is null
     */
    void deleteById(Long id);

    /**
     * Checks if a product with the given name already exists.
     * <p>
     * This is used for validation before creating new products
     * to enforce name uniqueness.
     * </p>
     *
     * @param name the product name to check
     * @return true if a product with this name exists, false otherwise
     * @throws IllegalArgumentException if name is null or blank
     */
    boolean existsByName(String name);
}
