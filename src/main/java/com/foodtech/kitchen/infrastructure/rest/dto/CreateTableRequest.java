package com.foodtech.kitchen.infrastructure.rest.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO for table creation requests via REST API.
 * 
 * <p><strong>Architecture:</strong></p>
 * <ul>
 *   <li>Layer: Infrastructure (REST)</li>
 *   <li>Pattern: DTO (Data Transfer Object)</li>
 *   <li>Purpose: HTTP request body for POST /api/tables</li>
 * </ul>
 * 
 * <p><strong>Fields:</strong></p>
 * <ul>
 *   <li>tableNumber: Unique identifier for the table (e.g., "A1", "B2", "VIP-01")</li>
 *   <li>capacity: Number of diners the table can accommodate (must be > 0)</li>
 * </ul>
 * 
 * <p><strong>Validation:</strong></p>
 * <ul>
 *   <li>Validations are performed in the domain layer by {@link com.foodtech.kitchen.domain.model.Table}</li>
 *   <li>Controller delegates validation to use case</li>
 * </ul>
 * 
 * <p><strong>Example JSON:</strong></p>
 * <pre>
 * {
 *   "tableNumber": "A1",
 *   "capacity": 4
 * }
 * </pre>
 * 
 * @see com.foodtech.kitchen.infrastructure.rest.TableController#createTable(CreateTableRequest)
 * @author FoodTech Kitchen Team
 * @version 1.0
 * @since 2026-01-21
 */
public class CreateTableRequest {
    
    @JsonProperty("tableNumber")
    private String tableNumber;
    
    @JsonProperty("capacity")
    private Integer capacity;
    
    /**
     * Default no-args constructor for Jackson deserialization.
     */
    public CreateTableRequest() {}
    
    /**
     * Constructor with all fields.
     * 
     * @param tableNumber the unique table identifier
     * @param capacity the number of diners the table can accommodate
     */
    public CreateTableRequest(String tableNumber, Integer capacity) {
        this.tableNumber = tableNumber;
        this.capacity = capacity;
    }
    
    public String getTableNumber() { return tableNumber; }
    public void setTableNumber(String tableNumber) { this.tableNumber = tableNumber; }
    
    public Integer getCapacity() { return capacity; }
    public void setCapacity(Integer capacity) { this.capacity = capacity; }
}
