package com.foodtech.kitchen.infrastructure.rest.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CreateTableRequest {
    
    @JsonProperty("tableNumber")
    private String tableNumber;
    
    @JsonProperty("capacity")
    private Integer capacity;
    
    public CreateTableRequest() {}
    
    public CreateTableRequest(String tableNumber, Integer capacity) {
        this.tableNumber = tableNumber;
        this.capacity = capacity;
    }
    
    public String getTableNumber() { return tableNumber; }
    public void setTableNumber(String tableNumber) { this.tableNumber = tableNumber; }
    
    public Integer getCapacity() { return capacity; }
    public void setCapacity(Integer capacity) { this.capacity = capacity; }
}
