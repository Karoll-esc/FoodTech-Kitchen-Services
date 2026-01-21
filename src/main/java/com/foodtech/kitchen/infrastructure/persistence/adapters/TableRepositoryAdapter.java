package com.foodtech.kitchen.infrastructure.persistence.adapters;

import com.foodtech.kitchen.application.ports.out.TableRepository;
import com.foodtech.kitchen.domain.model.Table;
import com.foodtech.kitchen.domain.model.TableStatus;
import com.foodtech.kitchen.infrastructure.persistence.jpa.TableJpaRepository;
import com.foodtech.kitchen.infrastructure.persistence.jpa.entities.TableEntity;
import com.foodtech.kitchen.infrastructure.persistence.mappers.TableEntityMapper;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Infrastructure adapter that implements {@link TableRepository} port
 * using Spring Data JPA for persistence.
 * 
 * <p><strong>Architecture:</strong></p>
 * <ul>
 *   <li>Layer: Infrastructure (Persistence)</li>
 *   <li>Pattern: Adapter (Hexagonal Architecture)</li>
 *   <li>Implements: {@link TableRepository} (application port)</li>
 *   <li>Uses: {@link TableJpaRepository}, {@link TableEntityMapper}</li>
 * </ul>
 * 
 * <p><strong>Responsibilities:</strong></p>
 * <ul>
 *   <li>Adapts JPA repository to domain repository interface</li>
 *   <li>Converts between domain {@link Table} and JPA {@link TableEntity}</li>
 *   <li>Delegates persistence operations to {@link TableJpaRepository}</li>
 *   <li>Isolates domain layer from JPA framework concerns</li>
 * </ul>
 * 
 * <p><strong>Design Principles:</strong></p>
 * <ul>
 *   <li><strong>Dependency Inversion:</strong> Application depends on TableRepository port, not this adapter</li>
 *   <li><strong>Single Responsibility:</strong> Only handles persistence adaptation</li>
 *   <li><strong>Anti-Corruption Layer:</strong> Prevents JPA concerns from leaking into domain</li>
 * </ul>
 * 
 * <p><strong>Usage:</strong></p>
 * <pre>
 * // Injected into use cases via TableRepository interface
 * public class CreateTableUseCase {
 *     private final TableRepository tableRepository; // This adapter injected here
 *     
 *     public Table execute(String tableNumber, int capacity) {
 *         Table table = new Table(tableNumber, capacity);
 *         return tableRepository.save(table); // Calls this adapter
 *     }
 * }
 * </pre>
 * 
 * @see TableRepository
 * @see TableJpaRepository
 * @see TableEntityMapper
 * @see Table
 * @author FoodTech Kitchen Team
 * @version 1.0
 * @since 2026-01-21
 */
@Component
public class TableRepositoryAdapter implements TableRepository {
    
    private final TableJpaRepository jpaRepository;
    private final TableEntityMapper mapper;
    
    /**
     * Constructs the adapter with required dependencies.
     * 
     * @param jpaRepository Spring Data JPA repository for database operations
     * @param mapper mapper for domain ↔ JPA entity conversion
     */
    public TableRepositoryAdapter(TableJpaRepository jpaRepository, TableEntityMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }
    
    /**
     * Persists or updates a table in the database.
     * 
     * <p>Converts domain Table to JPA entity, saves via JPA repository,
     * and converts result back to domain.</p>
     * 
     * @param table the domain Table to save (with or without ID)
     * @return the saved Table with generated ID and audit timestamps
     */
    @Override
    public Table save(Table table) {
        TableEntity entity = mapper.toEntity(table);
        TableEntity savedEntity = jpaRepository.save(entity);
        return mapper.toDomain(savedEntity);
    }
    
    /**
     * Finds a table by its database ID.
     * 
     * @param id the table's primary key
     * @return Optional containing the Table if found, empty otherwise
     */
    @Override
    public Optional<Table> findById(Long id) {
        return jpaRepository.findById(id)
            .map(mapper::toDomain);
    }
    
    /**
     * Finds a table by its unique table number.
     * 
     * @param tableNumber the unique table identifier (e.g., "A1", "B2")
     * @return Optional containing the Table if found, empty otherwise
     */
    @Override
    public Optional<Table> findByTableNumber(String tableNumber) {
        return jpaRepository.findByTableNumber(tableNumber)
            .map(mapper::toDomain);
    }
    
    /**
     * Retrieves all tables from the database.
     * 
     * @return List of all tables (empty list if none exist)
     */
    @Override
    public List<Table> findAll() {
        return jpaRepository.findAll().stream()
            .map(mapper::toDomain)
            .collect(Collectors.toList());
    }
    
    /**
     * Finds all tables with a specific operational status.
     * 
     * @param status the status to filter by (AVAILABLE, OCCUPIED, SERVED, CLEANING)
     * @return List of tables with the specified status (empty list if none found)
     */
    @Override
    public List<Table> findByStatus(TableStatus status) {
        return jpaRepository.findByStatus(status).stream()
            .map(mapper::toDomain)
            .collect(Collectors.toList());
    }
    
    /**
     * Checks if a table with the given table number already exists.
     * 
     * @param tableNumber the table number to check
     * @return true if exists, false otherwise
     */
    @Override
    public boolean existsByTableNumber(String tableNumber) {
        return jpaRepository.existsByTableNumber(tableNumber);
    }
    
    /**
     * Deletes a table by its database ID.
     * 
     * <p>Note: This is a destructive operation with no undo.</p>
     * 
     * @param id the ID of the table to delete
     */
    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }
}
