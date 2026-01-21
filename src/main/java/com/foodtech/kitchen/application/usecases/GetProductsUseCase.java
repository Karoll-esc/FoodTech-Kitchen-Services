package com.foodtech.kitchen.application.usecases;

import com.foodtech.kitchen.application.ports.out.ProductRepository;
import com.foodtech.kitchen.domain.model.Product;
import com.foodtech.kitchen.domain.model.ProductType;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Query use case for retrieving products with optional filtering.
 * 
 * <p>This use case implements the Query Pattern to fetch products from the catalog
 * with optional filters for availability status and product type. Filters can be
 * applied individually or in combination.</p>
 * 
 * <h2>Filter Behavior:</h2>
 * <ul>
 *   <li><b>No filters (both null):</b> Returns all products in the catalog</li>
 *   <li><b>Type only:</b> Returns all products of the specified type (regardless of availability)</li>
 *   <li><b>Available only:</b> Returns all products matching the availability status (regardless of type)</li>
 *   <li><b>Both filters:</b> Returns products matching BOTH type AND availability status</li>
 * </ul>
 * 
 * <h2>Filter Priority Strategy:</h2>
 * <p>When both filters are provided, the implementation uses a two-step approach:</p>
 * <ol>
 *   <li>Query by type first (more selective, typically fewer results)</li>
 *   <li>Filter by availability in-memory (stream processing)</li>
 * </ol>
 * <p>This strategy minimizes database load by reducing result set size early.</p>
 * 
 * <h2>Usage Examples:</h2>
 * <pre>{@code
 * // Example 1: Get all products
 * List<Product> allProducts = useCase.execute(null, null);
 * 
 * // Example 2: Get only available products
 * List<Product> availableProducts = useCase.execute(true, null);
 * 
 * // Example 3: Get all drinks (available and unavailable)
 * List<Product> allDrinks = useCase.execute(null, ProductType.DRINK);
 * 
 * // Example 4: Get only available drinks (combined filter)
 * List<Product> availableDrinks = useCase.execute(true, ProductType.DRINK);
 * 
 * // Example 5: Get unavailable hot dishes
 * List<Product> unavailableHotDishes = useCase.execute(false, ProductType.HOT_DISH);
 * }</pre>
 * 
 * <h2>Return Value:</h2>
 * <ul>
 *   <li>Always returns a non-null List (empty list if no matches)</li>
 *   <li>Read-only operation (does not modify product state)</li>
 * </ul>
 * 
 * <h2>Architecture Notes:</h2>
 * <ul>
 *   <li><b>Layer:</b> Application Layer (Use Case)</li>
 *   <li><b>Pattern:</b> Query Pattern (read-only operation)</li>
 *   <li><b>Dependencies:</b> ProductRepository port (output port)</li>
 *   <li><b>Framework:</b> NO Spring annotations (Pure Java)</li>
 *   <li><b>Testing:</b> Unit tests with Mockito (no Spring context required)</li>
 * </ul>
 * 
 * @see ProductRepository
 * @see Product
 * @see ProductType
 */
public class GetProductsUseCase {

    private final ProductRepository productRepository;

    /**
     * Constructs the use case with required dependencies.
     * 
     * @param productRepository the repository for accessing product data (must not be null)
     * @throws IllegalArgumentException if productRepository is null
     */
    public GetProductsUseCase(ProductRepository productRepository) {
        validateRepositoryNotNull(productRepository);
        this.productRepository = productRepository;
    }

    /**
     * Executes the query to retrieve products with optional filters.
     * 
     * @param available filter by availability status (null = no filter)
     * @param type filter by product type (null = no filter)
     * @return list of products matching the filters (never null, empty if no matches)
     */
    public List<Product> execute(Boolean available, ProductType type) {
        List<Product> products = fetchProductsByFilters(available, type);
        
        // Apply availability filter if both filters are provided
        if (hasBothFilters(available, type)) {
            products = applyAvailabilityFilter(products, available);
        }
        
        return products;
    }

    /**
     * Validates that the repository dependency is not null.
     * 
     * @param productRepository the repository to validate
     * @throws IllegalArgumentException if repository is null
     */
    private void validateRepositoryNotNull(ProductRepository productRepository) {
        if (productRepository == null) {
            throw new IllegalArgumentException("ProductRepository cannot be null");
        }
    }

    /**
     * Fetches products from repository based on filter priority.
     * 
     * <p>Filter priority: type > available > all</p>
     * <p>Type filter is prioritized as it typically returns fewer results.</p>
     * 
     * @param available availability filter (can be null)
     * @param type product type filter (can be null)
     * @return list of products matching the primary filter
     */
    private List<Product> fetchProductsByFilters(Boolean available, ProductType type) {
        if (type != null) {
            return productRepository.findByType(type);
        } else if (available != null) {
            return productRepository.findByAvailable(available);
        } else {
            return productRepository.findAll();
        }
    }

    /**
     * Applies availability filter to the product list using stream processing.
     * 
     * <p>Used when both type and availability filters are provided.</p>
     * 
     * @param products the list to filter
     * @param available the availability status to match
     * @return filtered list containing only products with matching availability
     */
    private List<Product> applyAvailabilityFilter(List<Product> products, Boolean available) {
        return products.stream()
            .filter(p -> p.isAvailable() == available)
            .collect(Collectors.toList());
    }

    /**
     * Checks if both filters are provided (non-null).
     * 
     * @param available availability filter
     * @param type product type filter
     * @return true if both filters are non-null
     */
    private boolean hasBothFilters(Boolean available, ProductType type) {
        return available != null && type != null;
    }
}
