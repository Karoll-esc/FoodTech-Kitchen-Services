package com.foodtech.kitchen.infrastructure.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.foodtech.kitchen.application.ports.in.*;
import com.foodtech.kitchen.application.ports.out.CommandExecutor;
import com.foodtech.kitchen.application.ports.out.OrderRepository;
import com.foodtech.kitchen.application.ports.out.ProductRepository;
import com.foodtech.kitchen.application.ports.out.TableRepository;
import com.foodtech.kitchen.application.ports.out.TaskRepository;
import com.foodtech.kitchen.application.services.StationAuthorizationService;
import com.foodtech.kitchen.application.usecases.*;
import com.foodtech.kitchen.domain.services.*;
import com.foodtech.kitchen.infrastructure.persistence.mappers.TableEntityMapper;
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
            TaskRepository taskRepository,
            TableRepository tableRepository
    ) {
        return new ProcessOrderUseCase(orderRepository, taskDecomposer, taskRepository, tableRepository);
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

    // ============================================================================
    // Product Catalog Use Cases (HU-006)
    // ============================================================================

    @Bean
    public ProductValidator productValidator() {
        return new ProductValidator();
    }

    @Bean
    public CreateProductUseCase createProductUseCase(
            ProductRepository productRepository,
            ProductValidator productValidator
    ) {
        return new CreateProductUseCase(productRepository, productValidator);
    }

    @Bean
    public GetProductByIdUseCase getProductByIdUseCase(
            ProductRepository productRepository
    ) {
        return new GetProductByIdUseCase(productRepository);
    }

    @Bean
    public GetProductsUseCase getProductsUseCase(
            ProductRepository productRepository
    ) {
        return new GetProductsUseCase(productRepository);
    }

    @Bean
    public UpdateProductUseCase updateProductUseCase(
            ProductRepository productRepository,
            ProductValidator productValidator
    ) {
        return new UpdateProductUseCase(productRepository, productValidator);
    }

    @Bean
    public UpdateProductAvailabilityUseCase updateProductAvailabilityUseCase(
            ProductRepository productRepository
    ) {
        return new UpdateProductAvailabilityUseCase(productRepository);
    }

    @Bean
    public DeleteProductUseCase deleteProductUseCase(
            ProductRepository productRepository
    ) {
        return new DeleteProductUseCase(productRepository);
    }

    // ============================================================================
    // Table Management Use Cases (HU-005)
    // ============================================================================

    @Bean
    public TableValidator tableValidator() {
        return new TableValidator();
    }

    @Bean
    public TableEntityMapper tableEntityMapper() {
        return new TableEntityMapper();
    }

    @Bean
    public CreateTableUseCase createTableUseCase(
            com.foodtech.kitchen.application.ports.out.TableRepository tableRepository,
            TableValidator tableValidator
    ) {
        return new CreateTableUseCase(tableRepository, tableValidator);
    }

    @Bean
    public GetAllTablesUseCase getAllTablesUseCase(
            com.foodtech.kitchen.application.ports.out.TableRepository tableRepository
    ) {
        return new GetAllTablesUseCase(tableRepository);
    }

    @Bean
    public GetTableByIdUseCase getTableByIdUseCase(
            com.foodtech.kitchen.application.ports.out.TableRepository tableRepository
    ) {
        return new GetTableByIdUseCase(tableRepository);
    }

    // ============================================================================
    // Table Lifecycle Management Use Cases (HU-007)
    // ============================================================================

    @Bean
    public TableLifecycleValidator tableLifecycleValidator() {
        return new TableLifecycleValidator();
    }

    @Bean
    public UpdateTableStatusPort updateTableStatusPort(
            com.foodtech.kitchen.application.ports.out.TableRepository tableRepository,
            TableLifecycleValidator tableLifecycleValidator
    ) {
        return new UpdateTableStatusUseCase(tableRepository, tableLifecycleValidator);
    }
}
