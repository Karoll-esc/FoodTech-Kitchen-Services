package com.foodtech.kitchen.infrastructure.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.foodtech.kitchen.application.ports.in.*;
import com.foodtech.kitchen.application.ports.out.CommandExecutor;
import com.foodtech.kitchen.application.ports.out.OrderRepository;
import com.foodtech.kitchen.application.ports.out.TaskRepository;
import com.foodtech.kitchen.application.services.StationAuthorizationService;
import com.foodtech.kitchen.application.usecases.*;
import com.foodtech.kitchen.domain.services.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationConfig {

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }

    @Bean
    public OrderValidator orderValidator() {
        return new OrderValidator();
    }

    @Bean
    public TaskFactory taskFactory() {
        return new TaskFactory();
    }

    @Bean
    public CommandFactory commandFactory() {
        return new CommandFactory();
    }

    @Bean
    public TaskDecomposer taskDecomposer(
            OrderValidator orderValidator,
            TaskFactory taskFactory
    ) {
        return new TaskDecomposer(orderValidator, taskFactory);
    }

    @Bean
    public OrderStatusCalculator orderStatusCalculator() {
        return new OrderStatusCalculator();
    }

    @Bean
    public ProcessOrderPort processOrderPort(
            OrderRepository orderRepository,
            TaskDecomposer taskDecomposer,
            TaskRepository taskRepository
    ) {
        return new ProcessOrderUseCase(orderRepository, taskDecomposer, taskRepository);
    }

    @Bean
    public StartTaskPreparationPort startTaskPreparationPort(
            TaskRepository taskRepository,
            CommandFactory commandFactory,
            CommandExecutor commandExecutor
    ) {
        return new StartTaskPreparationUseCase(taskRepository, commandFactory, commandExecutor);
    }

    @Bean
    public GetTasksByStationPort getTasksByStationPort(
            TaskRepository taskRepository
    ) {
        return new GetTasksByStationUseCase(taskRepository);
    }

    @Bean
    public GetOrderStatusPort getOrderStatusPort(
            TaskRepository taskRepository,
            OrderStatusCalculator orderStatusCalculator
    ) {
        return new GetOrderStatusUseCase(taskRepository, orderStatusCalculator);
    }

    @Bean
    public StationAuthorizationService stationAuthorizationService() {
        return new StationAuthorizationService();
    }
}