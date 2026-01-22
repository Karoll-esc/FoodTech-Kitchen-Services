package com.foodtech.kitchen.domain.model;

import java.util.ArrayList;
import java.util.List;


public class Order {

    private static final int MAX_CUSTOMER_NAME_LENGTH = 100;

    private final Long id;
    private final String ticketNumber;
    private final String customerName;
    private final List<Product> products;

    public Order(String ticketNumber, String customerName, List<Product> products) {
        validate(ticketNumber, customerName, products);
        this.id = null;
        this.ticketNumber = ticketNumber;
        this.customerName = customerName;
        this.products = new ArrayList<>(products);
    }

    private Order(Long id, String ticketNumber, String customerName, List<Product> products) {
        validate(ticketNumber, customerName, products);
        this.id = id;
        this.ticketNumber = ticketNumber;
        this.customerName = customerName;
        this.products = new ArrayList<>(products);
    }

    public static Order reconstruct(Long id, String ticketNumber, String customerName, List<Product> products) {
        validateId(id);
        return new Order(id, ticketNumber, customerName, products);
    }

    private void validate(String ticketNumber, String customerName, List<Product> products) {
        validateTicketNumber(ticketNumber);
        validateCustomerName(customerName);
        if (products == null || products.isEmpty()) {
            throw new IllegalArgumentException("Products list cannot be null or empty");
        }
    }

    private void validateTicketNumber(String ticketNumber) {
        if (ticketNumber == null || ticketNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Ticket number cannot be null or empty");
        }
    }

    private void validateCustomerName(String customerName) {
        if (customerName == null || customerName.trim().isEmpty()) {
            throw new IllegalArgumentException("Customer name cannot be null or empty");
        }
        if (customerName.length() > MAX_CUSTOMER_NAME_LENGTH) {
            throw new IllegalArgumentException("Customer name cannot exceed " + MAX_CUSTOMER_NAME_LENGTH + " characters");
        }
    }

    private static void validateId(Long id){
        if (id == null) {
            throw new IllegalArgumentException("ID cannot be null when reconstructing Order");
        }
    }

    public Long getId() {
        return id;
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
}
