package com.foodtech.kitchen.application.usecases;

import com.foodtech.kitchen.application.ports.out.OrderRepository;
import com.foodtech.kitchen.application.ports.out.ProductRepository;
import com.foodtech.kitchen.application.ports.out.TableRepository;
import com.foodtech.kitchen.domain.model.*;
import com.foodtech.kitchen.domain.services.*;
import com.foodtech.kitchen.application.ports.out.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProcessOrderUseCaseTest {

    private ProcessOrderUseCase useCase;
    private OrderRepository orderRepository;
    private ProductRepository productRepository;
    private TaskRepository taskRepository;
    private TableRepository tableRepository;
    private TaskDecomposer taskDecomposer;

    @BeforeEach
    void setUp() {
        orderRepository = mock(OrderRepository.class);
        productRepository = mock(ProductRepository.class);
        taskRepository = mock(TaskRepository.class);
        tableRepository = mock(TableRepository.class);

        OrderValidator orderValidator = new OrderValidator();
        TaskFactory taskFactory = new TaskFactory();
        taskDecomposer = new TaskDecomposer(orderValidator, taskFactory);

        useCase = new ProcessOrderUseCase(orderRepository, productRepository, taskDecomposer, taskRepository, tableRepository);

        // Default behavior: return empty for product lookups (use original product)
        when(productRepository.findByName(anyString())).thenReturn(Optional.empty());
    }

    @Test
    @DisplayName("Should process order and save tasks")
    void shouldProcessOrderAndSaveTasks() {
        // Given
        Product cocaCola = new Product("Coca Cola", ProductType.DRINK);
        Order order = new Order("A1", List.of(cocaCola));
        Order savedOrder = Order.reconstruct(1L, "A1", List.of(cocaCola));

        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);

        // When
        List<Task> tasks = useCase.execute(order);

        // Then
        assertEquals(1, tasks.size());
        verify(orderRepository, times(1)).save(any(Order.class));
        verify(taskRepository, times(1)).saveAll(anyList());
    }

    @Test
    @DisplayName("Should process mixed order and save multiple tasks")
    void shouldProcessMixedOrderAndSaveMultipleTasks() {
        // Given
        Product cocaCola = new Product("Coca Cola", ProductType.DRINK);
        Product pizza = new Product("Pizza", ProductType.PASTRY);
        Order order = new Order("B2", List.of(cocaCola, pizza));
        Order savedOrder = Order.reconstruct(2L, "B2", List.of(cocaCola, pizza));

        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);

        // When
        List<Task> tasks = useCase.execute(order);

        // Then
        assertEquals(2, tasks.size());
        verify(taskRepository, times(1)).saveAll(argThat(list -> list.size() == 2));
    }

    @Test
    @DisplayName("Should propagate validation exception from TaskDecomposer")
    void shouldPropagateValidationException() {
        // When & Then - la validación ya se lanza al crear el Order
        assertThrows(
            IllegalArgumentException.class,
            () -> useCase.execute(new Order("C3", List.of()))
        );
        verify(taskRepository, never()).saveAll(anyList());
    }

    @Test
    @DisplayName("Should automatically update table status to OCCUPIED when order is created for existing table")
    void shouldAutomaticallyUpdateTableStatusWhenOrderIsCreated() {
        // Given
        Product cocaCola = new Product("Coca Cola", ProductType.DRINK);
        Order order = new Order("A1", List.of(cocaCola));
        Order savedOrder = Order.reconstruct(1L, "A1", List.of(cocaCola));

        // Create an AVAILABLE table
        Table availableTable = new Table("A1", 4);
        availableTable.setId(1L);

        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);
        when(tableRepository.findByTableNumber("A1")).thenReturn(Optional.of(availableTable));

        // When
        List<Task> tasks = useCase.execute(order);

        // Then
        verify(tableRepository, times(1)).findByTableNumber("A1");
        verify(tableRepository, times(1)).update(argThat(table ->
            table.getStatus() == TableStatus.OCCUPIED &&
            table.getCurrentOrderId().equals(1L)
        ));
        assertEquals(1, tasks.size());
    }

    @Test
    @DisplayName("Should process order successfully even if table does not exist")
    void shouldProcessOrderEvenIfTableDoesNotExist() {
        // Given
        Product cocaCola = new Product("Coca Cola", ProductType.DRINK);
        Order order = new Order("NONEXISTENT", List.of(cocaCola));
        Order savedOrder = Order.reconstruct(1L, "NONEXISTENT", List.of(cocaCola));

        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);
        when(tableRepository.findByTableNumber("NONEXISTENT")).thenReturn(Optional.empty());

        // When
        List<Task> tasks = useCase.execute(order);

        // Then
        verify(tableRepository, times(1)).findByTableNumber("NONEXISTENT");
        verify(tableRepository, never()).update(any());
        assertEquals(1, tasks.size());
    }

    @Test
    @DisplayName("Should not update table status if table is already OCCUPIED")
    void shouldNotUpdateTableStatusIfAlreadyOccupied() {
        // Given
        Product cocaCola = new Product("Coca Cola", ProductType.DRINK);
        Order order = new Order("B2", List.of(cocaCola));
        Order savedOrder = Order.reconstruct(2L, "B2", List.of(cocaCola));

        // Create an OCCUPIED table
        Table occupiedTable = new Table("B2", 4);
        occupiedTable.setId(2L);
        occupiedTable.assignOrder(100L); // Already has an order

        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);
        when(tableRepository.findByTableNumber("B2")).thenReturn(Optional.of(occupiedTable));

        // When & Then - should throw IllegalStateException
        assertThrows(IllegalStateException.class, () -> useCase.execute(order));

        verify(tableRepository, times(1)).findByTableNumber("B2");
        verify(tableRepository, never()).update(any());
    }

    @Test
    @DisplayName("Should use catalog product preparation time when product exists in catalog")
    void shouldUseCatalogProductPreparationTime() {
        // Given
        Product orderProduct = new Product("Coca Cola", ProductType.DRINK); // Has default prep time of 1
        Product catalogProduct = new Product(
            1L, "Coca Cola", "Refreshing drink", ProductType.DRINK,
            new Price(new BigDecimal("5.99")), 300, true // 300 seconds = 5 minutes prep time
        );
        Order order = new Order("A1", List.of(orderProduct));
        Order savedOrder = Order.reconstruct(1L, "A1", List.of(catalogProduct));

        when(productRepository.findByName("Coca Cola")).thenReturn(Optional.of(catalogProduct));
        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);

        // When
        List<Task> tasks = useCase.execute(order);

        // Then
        assertEquals(1, tasks.size());
        verify(productRepository, times(1)).findByName("Coca Cola");
        // The enriched order should use the catalog product with 300 seconds prep time
        verify(orderRepository, times(1)).save(argThat(enrichedOrder ->
            enrichedOrder.getProducts().get(0).getPreparationTimeSeconds() == 300
        ));
    }
}