package com.foodtech.kitchen.domain.services;

import com.foodtech.kitchen.domain.commands.Command;
import com.foodtech.kitchen.domain.model.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

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
        Product cocaCola = new Product("Coca Cola", ProductType.BEVERAGE);
        Order order = new Order("T-001", "Alice", List.of(cocaCola));

        // When
        List<Task> tasks = decomposer.decompose(order);

        // Then
        assertEquals(1, tasks.size(), "Debe crear exactamente una tarea");
        assertEquals(Station.BEVERAGE, tasks.get(0).getStation(), "La bebida debe ir a la estación de bebidas");
        assertEquals(1, tasks.get(0).getProducts().size(), "La tarea debe contener un producto");
    }

    @Test
    @DisplayName("Debe crear una tarea para un pedido con un solo plato caliente")
    void shouldCreateOneTaskForSingleHotDish() {
        // Given
        Product cheesecake = new Product("Cheesecake", ProductType.DESSERT);
        Order order = new Order("T-002", "Bob", List.of(cheesecake));

        // When
        List<Task> tasks = decomposer.decompose(order);

        // Then
        assertEquals(1, tasks.size());
        assertEquals(Station.DESSERT, tasks.get(0).getStation());
    }

    @Test
    @DisplayName("Debe crear una tarea para un pedido con un solo plato frío")
    void shouldCreateOneTaskForSingleColdDish() {
        // Given
        Product croissant = new Product("Croissant", ProductType.BAKERY_ITEM);
        Order order = new Order("T-003", "Carla", List.of(croissant));

        // When
        List<Task> tasks = decomposer.decompose(order);

        // Then
        assertEquals(1, tasks.size());
        assertEquals(Station.BAKERY, tasks.get(0).getStation());
    }

    @Test
    @DisplayName("Debe crear tareas separadas para distintos tipos de producto")
    void shouldCreateSeparateTasksForMixedOrder() {
        // Given
        Product coffee = new Product("Latte", ProductType.BEVERAGE);
        Product tiramisu = new Product("Tiramisu", ProductType.DESSERT);
        Order order = new Order("T-004", "Diana", List.of(coffee, tiramisu));

        // When
        List<Task> tasks = decomposer.decompose(order);

        // Then
        assertEquals(2, tasks.size(), "Debe crear dos tareas separadas");

        boolean hasBeverageTask = tasks.stream()
                .anyMatch(task -> task.getStation() == Station.BEVERAGE);
        boolean hasDessertTask = tasks.stream()
                .anyMatch(task -> task.getStation() == Station.DESSERT);

        assertTrue(hasBeverageTask, "Debe existir una tarea para la estación de bebidas");
        assertTrue(hasDessertTask, "Debe existir una tarea para la estación de postres");
    }

    @Test
    @DisplayName("Debe agrupar productos del mismo tipo en una sola tarea")
    void shouldGroupProductsOfSameTypeInSingleTask() {
        // Given
        Product espresso = new Product("Espresso", ProductType.BEVERAGE);
        Product cappuccino = new Product("Cappuccino", ProductType.BEVERAGE);
        Order order = new Order("T-005", "Eva", List.of(espresso, cappuccino));

        // When
        List<Task> tasks = decomposer.decompose(order);

        // Then
        assertEquals(1, tasks.size(), "Debe crear solo UNA tarea para la misma estación");
        assertEquals(2, tasks.get(0).getProducts().size(), "La tarea debe contener ambos productos");
        assertEquals(Station.BEVERAGE, tasks.get(0).getStation());
    }

    @Test
    @DisplayName("Debe rechazar un pedido sin productos")
    void shouldRejectEmptyOrder() {
        // When & Then - la validación ahora ocurre en el constructor de Order
        assertThrows(
            IllegalArgumentException.class,
            () -> new Order("T-006", "Felix", List.of()),
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
    @DisplayName("Debe rechazar un pedido con ticket nulo")
    void shouldRejectNullTicketNumber() {
        // Given
        Product product = new Product("Latte", ProductType.BEVERAGE);

        // When & Then
        assertThrows(
                IllegalArgumentException.class,
                () -> new Order(null, "Gloria", List.of(product)),
                "Debe lanzar excepción para ticket nulo");
    }

    @Test
    @DisplayName("Debe rechazar un pedido con nombre de cliente nulo")
    void shouldRejectNullCustomerName() {
        Product product = new Product("Latte", ProductType.BEVERAGE);

        assertThrows(
                IllegalArgumentException.class,
                () -> new Order("T-007", null, List.of(product)),
                "Debe lanzar excepción para nombre de cliente nulo");
    }

    @Test
    @DisplayName("Debe crear tres tareas para un pedido con todos los tipos de producto")
    void shouldCreateThreeTasksForAllProductTypes() {
        // Given
        Product drink = new Product("Iced Coffee", ProductType.BEVERAGE);
        Product dessert = new Product("Brownie", ProductType.DESSERT);
        Product bakery = new Product("Bagel", ProductType.BAKERY_ITEM);
        Order order = new Order("T-008", "Hector", List.of(drink, dessert, bakery));

        // When
        List<Task> tasks = decomposer.decompose(order);

        // Then
        assertEquals(3, tasks.size(), "Debe crear tres tareas");

        long beverageTasks = tasks.stream()
                .filter(task -> task.getStation() == Station.BEVERAGE)
                .count();
        long dessertTasks = tasks.stream()
                .filter(task -> task.getStation() == Station.DESSERT)
                .count();
        long bakeryTasks = tasks.stream()
                .filter(task -> task.getStation() == Station.BAKERY)
                .count();

        assertEquals(1, beverageTasks, "Debe tener una tarea para BEVERAGE");
        assertEquals(1, dessertTasks, "Debe tener una tarea para DESSERT");
        assertEquals(1, bakeryTasks, "Debe tener una tarea para BAKERY");
    }

    @Test
    @DisplayName("Debe crear comandos para cada tarea")
    void shouldCreateCommandsForEachTask() {
        // Given
        Product coldBrew = new Product("Cold Brew", ProductType.BEVERAGE);
        Product muffin = new Product("Blueberry Muffin", ProductType.BAKERY_ITEM);
        Order order = new Order("T-009", "Iris", List.of(coldBrew, muffin));

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
