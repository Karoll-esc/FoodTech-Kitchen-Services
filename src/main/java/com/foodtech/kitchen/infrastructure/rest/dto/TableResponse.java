package com.foodtech.kitchen.infrastructure.rest.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.foodtech.kitchen.domain.model.TableStatus;
import java.time.LocalDateTime;

/**
 * DTO for table responses in REST API.
 * 
 * <p><strong>Architecture:</strong></p>
 * <ul>
 *   <li>Layer: Infrastructure (REST)</li>
 *   <li>Pattern: DTO (Data Transfer Object)</li>
 *   <li>Purpose: HTTP response body for table-related endpoints</li>
 * </ul>
 * 
 * <p><strong>Fields:</strong></p>
 * <ul>
 *   <li>id: Database primary key</li>
 *   <li>tableNumber: Unique identifier (e.g., "A1", "B2", "VIP-01")</li>
 *   <li>capacity: Number of diners the table can accommodate</li>
 *   <li>status: Operational state (AVAILABLE, OCCUPIED, SERVED, CLEANING)</li>
 *   <li>currentOrderId: FK to orders table (nullable, only set when OCCUPIED/SERVED)</li>
 *   <li>lastStateChangeAt: Timestamp of last status change (HU-007)</li>
 *   <li>createdAt: Timestamp when table was registered</li>
 *   <li>updatedAt: Timestamp of last modification</li>
 * </ul>
 * 
 * <p><strong>Usage:</strong></p>
 * <ul>
 *   <li>POST /api/tables response (201 Created)</li>
 *   <li>GET /api/tables response (200 OK, array of TableResponse)</li>
 *   <li>GET /api/tables/{id} response (200 OK)</li>
 *   <li>PATCH /api/tables/{id}/status response (200 OK) - HU-007</li>
 * </ul>
 * 
 * <p><strong>Example JSON:</strong></p>
 * <pre>
 * {
 *   "id": 1,
 *   "tableNumber": "A1",
 *   "capacity": 4,
 *   "status": "AVAILABLE",
 *   "currentOrderId": null,
 *   "lastStateChangeAt": "2026-01-21T16:00:00",
 *   "createdAt": "2026-01-21T16:00:00",
 *   "updatedAt": "2026-01-21T16:00:00"
 * }
 * </pre>
 * 
 * @see com.foodtech.kitchen.infrastructure.rest.TableController
 * @see com.foodtech.kitchen.infrastructure.rest.mapper.TableMapper
 * @author FoodTech Kitchen Team
 * @version 1.0
 * @since 2026-01-21
 */
public class TableResponse {
    
    @JsonProperty("id")
    private Long id;
    
    @JsonProperty("tableNumber")
    private String tableNumber;
    
    @JsonProperty("capacity")
    private Integer capacity;
    
    @JsonProperty("status")
    private TableStatus status;
    
    @JsonProperty("currentOrderId")
    private Long currentOrderId;
    
    @JsonProperty("lastStateChangeAt")
    private LocalDateTime lastStateChangeAt;
    
    @JsonProperty("createdAt")
    private LocalDateTime createdAt;
    
    @JsonProperty("updatedAt")
    private LocalDateTime updatedAt;
    
    /**
     * Default no-args constructor for Jackson serialization.
     */
    public TableResponse() {}
    
    /**
     * Constructor with all fields.
     * 
     * @param id database primary key
     * @param tableNumber unique table identifier
     * @param capacity number of diners
     * @param status operational state
     * @param currentOrderId nullable, ID of current order
     * @param lastStateChangeAt timestamp of last status change
     * @param createdAt registration timestamp
     * @param updatedAt last modification timestamp
     */
    public TableResponse(Long id, String tableNumber, Integer capacity, TableStatus status,
                        Long currentOrderId, LocalDateTime lastStateChangeAt,
                        LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.tableNumber = tableNumber;
        this.capacity = capacity;
        this.status = status;
        this.currentOrderId = currentOrderId;
        this.lastStateChangeAt = lastStateChangeAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
    
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
    
    public LocalDateTime getLastStateChangeAt() { return lastStateChangeAt; }
    public void setLastStateChangeAt(LocalDateTime lastStateChangeAt) { this.lastStateChangeAt = lastStateChangeAt; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
