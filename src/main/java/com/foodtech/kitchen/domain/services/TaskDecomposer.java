package com.foodtech.kitchen.domain.services;

import com.foodtech.kitchen.domain.model.*;

import java.util.*;

//HUMAN REVIEW: Eliminé ProductStationMapper porque ProductType ahora tiene getStation().
//Cumple OCP: extensible sin modificar código. Cumple SRP: orquesta validación y creación.
public class TaskDecomposer {

    private final OrderValidator orderValidator;
    private final TaskFactory taskFactory;

    public TaskDecomposer(OrderValidator orderValidator, TaskFactory taskFactory) {
        this.orderValidator = orderValidator;
        this.taskFactory = taskFactory;
    }

    public List<Task> decompose(Order order) {
        orderValidator.validate(order);

        Map<Station, List<Product>> productsByStation = groupProductsByStation(order);

        // Si el order tiene ID, lo usamos; si no, usamos un ID temporal para tests
        Long orderId = order.getId() != null ? order.getId() : 0L;
        return taskFactory.createTasks(orderId, order.getTicketNumber(), order.getCustomerName(), productsByStation);
    }

    private Map<Station, List<Product>> groupProductsByStation(Order order) {
        Map<Station, List<Product>> productsByStation = new HashMap<>();

        for (Product product : order.getProducts()) {
            Station station = product.getType().getStation();
            productsByStation
                    .computeIfAbsent(station, k -> new ArrayList<>())
                    .add(product);
        }

        return productsByStation;
    }
}
