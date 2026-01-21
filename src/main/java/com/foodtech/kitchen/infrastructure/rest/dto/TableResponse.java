package com.foodtech.kitchen.infrastructure.rest.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.foodtech.kitchen.domain.model.TableStatus;
import java.time.LocalDateTime;

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
    
    @JsonProperty("createdAt")
    private LocalDateTime createdAt;
    
    @JsonProperty("updatedAt")
    private LocalDateTime updatedAt;
    
    public TableResponse() {}
    
    public TableResponse(Long id, String tableNumber, Integer capacity, TableStatus status,
                        Long currentOrderId, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.tableNumber = tableNumber;
        this.capacity = capacity;
        this.status = status;
        this.currentOrderId = currentOrderId;
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
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
