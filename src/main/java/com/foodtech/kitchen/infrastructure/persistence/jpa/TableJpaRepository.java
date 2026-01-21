package com.foodtech.kitchen.infrastructure.persistence.jpa;

import com.foodtech.kitchen.domain.model.TableStatus;
import com.foodtech.kitchen.infrastructure.persistence.jpa.entities.TableEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for {@link TableEntity} persistence operations.
 * 
 * <p><strong>Architecture:</strong></p>
 * <ul>
 *   <li>Layer: Infrastructure (Persistence)</li>
 *   <li>Pattern: Repository (Spring Data JPA)</li>
 *   <li>Extends: {@link JpaRepository}</li>
 *   <li>Used by: {@link com.foodtech.kitchen.infrastructure.persistence.adapters.TableRepositoryAdapter}</li>
 * </ul>
 * 
 * <p><strong>Provided Operations:</strong></p>
 * <ul>
 *   <li>CRUD operations (inherited from JpaRepository)</li>
 *   <li>Custom queries for table number lookup and status filtering</li>
 *   <li>Existence checks to enforce unique table numbers</li>
 * </ul>
 * 
 * <p><strong>Query Methods:</strong></p>
 * <ul>
 *   <li>{@link #findByTableNumber(String)}: Find table by unique table number</li>
 *   <li>{@link #existsByTableNumber(String)}: Check if table number is already registered</li>
 *   <li>{@link #findByStatus(TableStatus)}: Filter tables by operational status</li>
 * </ul>
 * 
 * <p><strong>Usage Example:</strong></p>
 * <pre>
 * // Check if table number exists before creating
 * if (tableJpaRepository.existsByTableNumber("A1")) {
 *     throw new TableAlreadyExistsException("A1");
 * }
 * 
 * // Find all available tables
 * List&lt;TableEntity&gt; availableTables = tableJpaRepository.findByStatus(TableStatus.AVAILABLE);
 * </pre>
 * 
 * @see TableEntity
 * @see com.foodtech.kitchen.infrastructure.persistence.adapters.TableRepositoryAdapter
 * @see JpaRepository
 * @author FoodTech Kitchen Team
 * @version 1.0
 * @since 2026-01-21
 */
@Repository
public interface TableJpaRepository extends JpaRepository<TableEntity, Long> {
    
    /**
     * Finds a table by its unique table number.
     * 
     * <p>Table numbers are unique identifiers like "A1", "B2", "VIP-01".</p>
     * 
     * @param tableNumber the unique table number to search for
     * @return Optional containing the TableEntity if found, empty otherwise
     * @throws IllegalArgumentException if tableNumber is null (Spring Data behavior)
     */
    Optional<TableEntity> findByTableNumber(String tableNumber);
    
    /**
     * Checks if a table with the given table number already exists.
     * 
     * <p>Used to enforce unique table number constraint before creating new tables.</p>
     * 
     * @param tableNumber the table number to check
     * @return true if a table with this number exists, false otherwise
     * @throws IllegalArgumentException if tableNumber is null (Spring Data behavior)
     * @see com.foodtech.kitchen.application.usecases.CreateTableUseCase
     */
    boolean existsByTableNumber(String tableNumber);
    
    /**
     * Finds all tables with the specified operational status.
     * 
     * <p>Useful for filtering tables by status (e.g., finding all AVAILABLE tables).</p>
     * 
     * @param status the table status to filter by (AVAILABLE, OCCUPIED, SERVED, CLEANING)
     * @return List of tables with the specified status (empty list if none found)
     * @throws IllegalArgumentException if status is null (Spring Data behavior)
     * @see TableStatus
     */
    List<TableEntity> findByStatus(TableStatus status);
}
