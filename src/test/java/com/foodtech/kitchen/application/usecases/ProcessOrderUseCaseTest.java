package com.foodtech.kitchen.application.usecases;

import com.foodtech.kitchen.application.ports.out.OrderRepository;
import com.foodtech.kitchen.application.ports.out.TableRepository;
import com.foodtech.kitchen.domain.model.*;
import com.foodtech.kitchen.domain.services.*;
import com.foodtech.kitchen.application.ports.out.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProcessOrderUseCaseTest {

    private ProcessOrderUseCase useCase;
    private OrderRepository orderRepository;
    private TaskRepository taskRepository;
    private TableRepository tableRepository;
    private TaskDecomposer taskDecomposer;

    @BeforeEach
    void setUp() {
        orderRepository = mock(OrderRepository.class);
        taskRepository = mock(TaskRepository.class);
        tableRepository = mock(TableRepository.class);
        
        OrderValidator orderValidator = new OrderValidator();
        TaskFactory taskFactory = new TaskFactory();
        taskDecomposer = new TaskDecomposer(orderValidator, taskFactory);
        
        useCase = new ProcessOrderUseCase(orderRepository, taskDecomposer, taskRepository, tableRepository);
    }

    @Test
    @DisplayName("Should process order and save tasks")
    void shouldProcessOrderAndSaveTasks() {
        // Given
        Product cocaCola = new Product("Coca Cola", ProductType.DRINK);
        Order order = new Order("A1", List.of(cocaCola));
        Order savedOrder = Order.reconstruct(1L, "A1", List.of(cocaCola));
        
        when(orderRepository.save(order)).thenReturn(savedOrder);

        // When
        List<Task> tasks = useCase.execute(order);

        // Then
        assertEquals(1, tasks.size());
        verify(orderRepository, times(1)).save(order);
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
        
        when(orderRepository.save(order)).thenReturn(savedOrder);

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
        
        when(orderRepository.save(order)).thenReturn(savedOrder);
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
        
        when(orderRepository.save(order)).thenReturn(savedOrder);
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
        
        when(orderRepository.save(order)).thenReturn(savedOrder);
        when(tableRepository.findByTableNumber("B2")).thenReturn(Optional.of(occupiedTable));

        // When & Then - should throw IllegalStateException
        assertThrows(IllegalStateException.class, () -> useCase.execute(order));
        
        verify(tableRepository, times(1)).findByTableNumber("B2");
        verify(tableRepository, never()).update(any());
    }
}