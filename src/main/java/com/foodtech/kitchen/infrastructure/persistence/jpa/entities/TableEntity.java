package com.foodtech.kitchen.infrastructure.persistence.jpa.entities;

import com.foodtech.kitchen.domain.model.TableStatus;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * JPA entity representing a restaurant table in the persistence layer.
 * 
 * <p><strong>Architecture:</strong></p>
 * <ul>
 *   <li>Layer: Infrastructure (Persistence)</li>
 *   <li>Pattern: Entity (JPA ORM)</li>
 *   <li>Maps to: {@link com.foodtech.kitchen.domain.model.Table} domain entity</li>
 *   <li>Table: tables</li>
 * </ul>
 * 
 * <p><strong>Database Schema:</strong></p>
 * <ul>
 *   <li>id: Primary key (auto-generated)</li>
 *   <li>table_number: Unique identifier for the table (e.g., "A1", "B2", "VIP-01")</li>
 *   <li>capacity: Number of diners the table can accommodate</li>
 *   <li>status: Current operational status (AVAILABLE, OCCUPIED, SERVED, CLEANING)</li>
 *   <li>current_order_id: FK to orders table (nullable, only set when OCCUPIED/SERVED)</li>
 *   <li>created_at: Timestamp when table was registered</li>
 *   <li>updated_at: Timestamp of last modification</li>
 * </ul>
 * 
 * <p><strong>Lifecycle Callbacks:</strong></p>
 * <ul>
 *   <li>{@link #onCreate()}: Sets createdAt, updatedAt, and default status (AVAILABLE) on insert</li>
 *   <li>{@link #onUpdate()}: Updates updatedAt timestamp on every update</li>
 * </ul>
 * 
 * <p><strong>Constraints:</strong></p>
 * <ul>
 *   <li>table_number must be unique (enforced by database constraint)</li>
 *   <li>table_number max length: 10 characters</li>
 *   <li>capacity, table_number, status, created_at, updated_at are NOT NULL</li>
 * </ul>
 * 
 * <p><strong>Usage:</strong></p>
 * <pre>
 * // Mapped by TableEntityMapper between domain and JPA entity
 * TableEntity entity = new TableEntity();
 * entity.setTableNumber("A1");
 * entity.setCapacity(4);
 * entity.setStatus(TableStatus.AVAILABLE);
 * tableJpaRepository.save(entity); // Triggers @PrePersist
 * </pre>
 * 
 * @see com.foodtech.kitchen.domain.model.Table
 * @see com.foodtech.kitchen.infrastructure.persistence.mappers.TableEntityMapper
 * @see com.foodtech.kitchen.infrastructure.persistence.jpa.TableJpaRepository
 * @author FoodTech Kitchen Team
 * @version 1.0
 * @since 2026-01-21
 */
@Entity
@jakarta.persistence.Table(name = "tables")
public class TableEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "table_number", nullable = false, unique = true, length = 10)
    private String tableNumber;
    
    @Column(name = "capacity", nullable = false)
    private Integer capacity;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private TableStatus status;
    
    @Column(name = "current_order_id")
    private Long currentOrderId;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    /**
     * JPA lifecycle callback executed before persisting a new TableEntity.
     * 
     * <p>Automatically sets:</p>
     * <ul>
     *   <li>createdAt = current timestamp</li>
     *   <li>updatedAt = current timestamp</li>
     *   <li>status = AVAILABLE (if not explicitly set)</li>
     * </ul>
     * 
     * <p>This ensures every new table starts in AVAILABLE status
     * and has proper audit timestamps.</p>
     * 
     * @see TableStatus#AVAILABLE
     */
    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (this.status == null) {
            this.status = TableStatus.AVAILABLE;
        }
    }
    
    /**
     * JPA lifecycle callback executed before updating an existing TableEntity.
     * 
     * <p>Automatically updates:</p>
     * <ul>
     *   <li>updatedAt = current timestamp</li>
     * </ul>
     * 
     * <p>This provides automatic audit trail for all table modifications.</p>
     */
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Default no-args constructor required by JPA.
     * Creates an empty TableEntity instance.
     */
    public TableEntity() {}
    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getTableNumber() { return tableNumber; }
    public void setTableNumber(String tableNumber) { this.tableNumber = tableNumber; }
    
    public Integer getCapacity() { return capacity; }
    public void setCapacity(Integer capacity) { this.capacity = capacity; }
    
    public TableStatus getStatus() { return status; }
    public void setStatus(TableStatus status) { this.status = status; }
    
    public Long getCurrentOrderId() { return currentOrderId; }
    public void setCurrentOrderId(Long currentOrderId) { this.currentOrderId = currentOrderId; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
