package com.foodtech.kitchen.domain.services;

import com.foodtech.kitchen.domain.commands.Command;
import com.foodtech.kitchen.domain.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TaskDecomposerTest {

    private TaskDecomposer decomposer;
    private OrderValidator orderValidator;
    private TaskFactory taskFactory;

    @BeforeEach
    void setUp() {
        orderValidator = new OrderValidator();
        taskFactory = new TaskFactory();
        decomposer = new TaskDecomposer(orderValidator, taskFactory);
    }

    @Test
    @DisplayName("Debe crear una tarea para un pedido con una sola bebida")
    void shouldCreateOneTaskForSingleDrink() {
        // Given
        Product cocaCola = new Product("Coca Cola", ProductType.DRINK);
        Order order = new Order("A1", List.of(cocaCola));

        // When
        List<Task> tasks = decomposer.decompose(order);

        // Then
        assertEquals(1, tasks.size(), "Debe crear exactamente una tarea");
        assertEquals(Station.ESPRESSO_BAR, tasks.get(0).getStation(), "La bebida debe ir a BARRA");
        assertEquals(1, tasks.get(0).getProducts().size(), "La tarea debe contener un producto");
    }

    @Test
    @DisplayName("Debe crear una tarea para un pedido con un solo plato caliente")
    void shouldCreateOneTaskForSingleHotDish() {
        // Given
        Product pizza = new Product("Pizza Margarita", ProductType.PASTRY);
        Order order = new Order("B2", List.of(pizza));

        // When
        List<Task> tasks = decomposer.decompose(order);

        // Then
        assertEquals(1, tasks.size());
        assertEquals(Station.PASTRY_STATION, tasks.get(0).getStation());
    }

    @Test
    @DisplayName("Debe crear una tarea para un pedido con un solo plato frío")
    void shouldCreateOneTaskForSingleColdDish() {
        // Given
        Product salad = new Product("Caesar Salad", ProductType.SANDWICH);
        Order order = new Order("C3", List.of(salad));

        // When
        List<Task> tasks = decomposer.decompose(order);

        // Then
        assertEquals(1, tasks.size());
        assertEquals(Station.SANDWICH_STATION, tasks.get(0).getStation());
    }

    @Test
    @DisplayName("Debe crear tareas separadas para distintos tipos de producto")
    void shouldCreateSeparateTasksForMixedOrder() {
        // Given
        Product cocaCola = new Product("Coca Cola", ProductType.DRINK);
        Product pizza = new Product("Pizza", ProductType.PASTRY);
        Order order = new Order("D4", List.of(cocaCola, pizza));

        // When
        List<Task> tasks = decomposer.decompose(order);

        // Then
        assertEquals(2, tasks.size(), "Debe crear dos tareas separadas");

        boolean hasDrinkTask = tasks.stream()
                .anyMatch(task -> task.getStation() == Station.ESPRESSO_BAR);
        boolean hasHotDishTask = tasks.stream()
                .anyMatch(task -> task.getStation() == Station.PASTRY_STATION);

        assertTrue(hasDrinkTask, "Debe existir una tarea para ESPRESSO_BAR");
        assertTrue(hasHotDishTask, "Debe existir una tarea para la cocina caliente");
    }

    @Test
    @DisplayName("Debe agrupar productos del mismo tipo en una sola tarea")
    void shouldGroupProductsOfSameTypeInSingleTask() {
        // Given
        Product cocaCola = new Product("Coca Cola", ProductType.DRINK);
        Product sprite = new Product("Sprite", ProductType.DRINK);
        Order order = new Order("E5", List.of(cocaCola, sprite));

        // When
        List<Task> tasks = decomposer.decompose(order);

        // Then
        assertEquals(1, tasks.size(), "Debe crear solo UNA tarea para la misma estación");
        assertEquals(2, tasks.get(0).getProducts().size(), "La tarea debe contener ambos productos");
        assertEquals(Station.ESPRESSO_BAR, tasks.get(0).getStation());
    }

    @Test
    @DisplayName("Debe rechazar un pedido sin productos")
    void shouldRejectEmptyOrder() {
        // When & Then - la validación ahora ocurre en el constructor de Order
        assertThrows(
            IllegalArgumentException.class,
            () -> new Order("F6", List.of()),
            "Debe lanzar excepción para pedido vacío");
    }

    @Test
    @DisplayName("Debe rechazar un pedido nulo")
    void shouldRejectNullOrder() {
        // When & Then
        assertThrows(
                IllegalArgumentException.class,
                () -> decomposer.decompose(null),
                "Debe lanzar excepción para pedido nulo");
    }

    @Test
    @DisplayName("Debe rechazar un pedido con número de mesa nulo")
    void shouldRejectNullTableNumber() {
        // Given
        Product product = new Product("Coca Cola", ProductType.DRINK);

        // When & Then
        assertThrows(
                IllegalArgumentException.class,
                () -> new Order(null, List.of(product)),
                "Debe lanzar excepción para número de mesa nulo");
    }

    @Test
    @DisplayName("Debe crear tres tareas para un pedido con todos los tipos de producto")
    void shouldCreateThreeTasksForAllProductTypes() {
        // Given
        Product drink = new Product("Coca Cola", ProductType.DRINK);
        Product hotDish = new Product("Pizza", ProductType.PASTRY);
        Product coldDish = new Product("Caesar Salad", ProductType.SANDWICH);
        Order order = new Order("G7", List.of(drink, hotDish, coldDish));

        // When
        List<Task> tasks = decomposer.decompose(order);

        // Then
        assertEquals(3, tasks.size(), "Debe crear tres tareas");

        long barTasks = tasks.stream()
                .filter(task -> task.getStation() == Station.ESPRESSO_BAR)
                .count();
        long hotKitchenTasks = tasks.stream()
                .filter(task -> task.getStation() == Station.PASTRY_STATION)
                .count();
        long coldKitchenTasks = tasks.stream()
                .filter(task -> task.getStation() == Station.SANDWICH_STATION)
                .count();

        assertEquals(1, barTasks, "Debe tener una tarea para ESPRESSO_BAR");
        assertEquals(1, hotKitchenTasks, "Debe tener una tarea para PASTRY_STATION");
        assertEquals(1, coldKitchenTasks, "Debe tener una tarea para SANDWICH_STATION");
    }

    @Test
    @DisplayName("Debe crear comandos para cada tarea")
    void shouldCreateCommandsForEachTask() {
        // Given
        Product cocaCola = new Product("Coca Cola", ProductType.DRINK);
        Product pizza = new Product("Pizza", ProductType.PASTRY);
        Order order = new Order("H8", List.of(cocaCola, pizza));

        CommandFactory commandFactory = new CommandFactory();

        // When
        List<Task> tasks = decomposer.decompose(order);

        // Then
        assertEquals(2, tasks.size());
        // Verificar que cada tarea puede crear su comando
        for (Task task : tasks) {
            Command command = commandFactory.createCommand(task.getStation(), task.getProducts());
            assertNotNull(command);
            assertInstanceOf(Command.class, command);
        }
    }

}