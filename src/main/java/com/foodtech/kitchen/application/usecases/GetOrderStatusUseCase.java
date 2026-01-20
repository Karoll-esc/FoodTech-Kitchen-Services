package com.foodtech.kitchen.application.usecases;

import com.foodtech.kitchen.application.exception.OrderNotFoundException;
import com.foodtech.kitchen.application.ports.in.GetOrderStatusPort;
import com.foodtech.kitchen.application.ports.out.TaskRepository;
import com.foodtech.kitchen.domain.model.Task;
import com.foodtech.kitchen.domain.model.TaskStatus;
import com.foodtech.kitchen.domain.services.OrderStatusCalculator;

import java.util.List;

public class GetOrderStatusUseCase implements GetOrderStatusPort {

    private final TaskRepository taskRepository;
    private final OrderStatusCalculator orderStatusCalculator;

    public GetOrderStatusUseCase(TaskRepository taskRepository,
                                 OrderStatusCalculator orderStatusCalculator) {
        this.taskRepository = taskRepository;
        this.orderStatusCalculator = orderStatusCalculator;
    }

    @Override
    public TaskStatus execute(Long orderId) {
        List<Task> tasks = taskRepository.findByOrderId(orderId);

        // Validación de aplicación: el orderId debe existir
        if (tasks.isEmpty()) {
            throw new OrderNotFoundException(orderId);
        }

        // Delegar lógica de negocio al dominio
        return orderStatusCalculator.calculateOrderStatus(tasks);
    }
}