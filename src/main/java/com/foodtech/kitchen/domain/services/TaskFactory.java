package com.foodtech.kitchen.domain.services;

import com.foodtech.kitchen.domain.model.Product;
import com.foodtech.kitchen.domain.model.Station;
import com.foodtech.kitchen.domain.model.Task;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

//HUMAN REVIEW: Extraje la creación de tareas a su propia clase.
//Cumple SRP: solo se encarga de construir objetos Task a partir de productos agrupados.
public class TaskFactory {

    public List<Task> createTasks(Long orderId, String ticketNumber, String customerName,
                                  Map<Station, List<Product>> productsByStation) {
        List<Task> tasks = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        
        for (Map.Entry<Station, List<Product>> entry : productsByStation.entrySet()) {
            Station station = entry.getKey();
            List<Product> products = entry.getValue();
            tasks.add(new Task(orderId, station, ticketNumber, customerName, products, now));
        }
        
        return tasks;
    }
}
