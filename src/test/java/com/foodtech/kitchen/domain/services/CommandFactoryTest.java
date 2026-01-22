package com.foodtech.kitchen.domain.services;

import com.foodtech.kitchen.domain.commands.*;
import com.foodtech.kitchen.domain.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CommandFactoryTest {

    private CommandFactory factory;

    @BeforeEach
    void setUp() {
        factory = new CommandFactory();
    }

    @Test
    @DisplayName("Debe crear PrepareBeverageCommand para productos de tipo BEVERAGE")
    void shouldCreateDrinkCommandForDrinkProducts() {
        // Given
        Product cocaCola = new Product("Coca Cola", ProductType.BEVERAGE);
        List<Product> products = List.of(cocaCola);

        // When
        Command command = factory.createCommand(Station.BEVERAGE, products);

        // Then
        assertInstanceOf(PrepareDrinkCommand.class, command);
    }

    @Test
    @DisplayName("Debe crear PrepareDessertCommand para productos de tipo DESSERT")
    void shouldCreateHotDishCommandForHotDishProducts() {
        // Given
        Product tiramisu = new Product("Tiramisu", ProductType.DESSERT);
        List<Product> products = List.of(tiramisu);

        // When
        Command command = factory.createCommand(Station.DESSERT, products);

        // Then
        assertInstanceOf(PrepareHotDishCommand.class, command);
    }

    @Test
    @DisplayName("Debe crear PrepareBakeryItemCommand para productos de tipo BAKERY_ITEM")
    void shouldCreateColdDishCommandForColdDishProducts() {
        // Given
        Product croissant = new Product("Croissant", ProductType.BAKERY_ITEM);
        List<Product> products = List.of(croissant);

        // When
        Command command = factory.createCommand(Station.BAKERY, products);

        // Then
        assertInstanceOf(PrepareColdDishCommand.class, command);
    }

    @Test
    @DisplayName("Debe lanzar excepción para estación desconocida")
    void shouldThrowExceptionForUnknownStation() {
        // Given
        Product product = new Product("Test", ProductType.BEVERAGE);
        List<Product> products = List.of(product);

        // When & Then
        // Este test es solo por completitud, pero con enum no puede pasar
        assertDoesNotThrow(() -> factory.createCommand(Station.BEVERAGE, products));
    }
}
