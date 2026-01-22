package com.foodtech.kitchen.domain.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Task {
    private final Long id;
    private final Long orderId;
    private final Station station;
    private final String ticketNumber;
    private final String customerName;
    private final List<Product> products;
    private final LocalDateTime createdAt;
    private TaskStatus status;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;

    // Constructor público para CREAR nuevas tasks (sin ID)
    public Task(Long orderId, Station station, String ticketNumber, String customerName,
                List<Product> products, LocalDateTime createdAt) {
        validate(orderId, station, ticketNumber, customerName, products, createdAt);
        this.id = null;
        this.orderId = orderId;
        this.station = station;
        this.ticketNumber = ticketNumber;
        this.customerName = customerName;
        this.products = new ArrayList<>(products);
        this.createdAt = createdAt;
        this.status = TaskStatus.PENDING;
        this.startedAt = null;
        this.completedAt = null;
    }

    // Constructor PRIVADO con ID (para reconstrucción)
    private Task(Long id, Long orderId, Station station, String ticketNumber, String customerName,
                 List<Product> products, LocalDateTime createdAt) {
        validate(orderId, station, ticketNumber, customerName, products, createdAt);
        this.id = id;
        this.orderId = orderId;
        this.station = station;
        this.ticketNumber = ticketNumber;
        this.customerName = customerName;
        this.products = new ArrayList<>(products);
        this.createdAt = createdAt;
        this.status = TaskStatus.PENDING;
        this.startedAt = null;
        this.completedAt = null;
    }


    public static Task reconstruct(Long id, Long orderId, Station station, String ticketNumber, String customerName,
                                    List<Product> products, LocalDateTime createdAt,
                                    TaskStatus status, LocalDateTime startedAt,
                                    LocalDateTime completedAt) {
        validateId(id);
        Task task = new Task(id, orderId, station, ticketNumber, customerName, products, createdAt);
        task.status = status;
        task.startedAt = startedAt;
        task.completedAt = completedAt;
        return task;
    }

    private static void validateId(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID cannot be null when reconstructing Task");
        }
    }

    private void validate(Long orderId, Station station, String ticketNumber, String customerName,
                          List<Product> products, LocalDateTime createdAt) {

        if (orderId == null) {
            throw new IllegalArgumentException("Order ID cannot be null");
        }
        if (station == null) {
            throw new IllegalArgumentException("Station cannot be null");
        }
        if (ticketNumber == null || ticketNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Ticket number cannot be null or empty");
        }
        if (customerName == null || customerName.trim().isEmpty()) {
            throw new IllegalArgumentException("Customer name cannot be null or empty");
        }
        if (products == null || products.isEmpty()) {
            throw new IllegalArgumentException("Products list cannot be null or empty");
        }
        if (createdAt == null) {
            throw new IllegalArgumentException("Created at timestamp cannot be null");
        }
    }

    public void start() {
        if (this.status != TaskStatus.PENDING) {
            throw new IllegalStateException("Task must be in PENDING status to start");
        }
        this.status = TaskStatus.IN_PREPARATION;
        this.startedAt = LocalDateTime.now();
    }

    public void complete() {
        if (this.status != TaskStatus.IN_PREPARATION) {
            throw new IllegalStateException("Task must be in IN_PREPARATION status to complete");
        }
        this.status = TaskStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
    }


    public Long getId() {
        return id;
    }

    public Long getOrderId() {
        return orderId;
    }

    public Station getStation() {
        return station;
    }

    public String getTicketNumber() {
        return ticketNumber;
    }

    public String getCustomerName() {
        return customerName;
    }

    public List<Product> getProducts() {
        return new ArrayList<>(products);
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }
}
