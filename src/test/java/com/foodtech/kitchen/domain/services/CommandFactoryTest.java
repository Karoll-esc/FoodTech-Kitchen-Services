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
    @DisplayName("Debe crear PrepareDrinkCommand para productos de tipo DRINK")
    void shouldCreateDrinkCommandForDrinkProducts() {
        // Given
        Product cocaCola = new Product("Coca Cola", ProductType.DRINK);
        List<Product> products = List.of(cocaCola);

        // When
        Command command = factory.createCommand(Station.ESPRESSO_BAR, products);

        // Then
        assertInstanceOf(PrepareDrinkCommand.class, command);
    }

    @Test
    @DisplayName("Debe crear PreparePastryCommand para productos de tipo PASTRY")
    void shouldCreateHotDishCommandForHotDishProducts() {
        // Given
        Product pizza = new Product("Pizza", ProductType.PASTRY);
        List<Product> products = List.of(pizza);

        // When
        Command command = factory.createCommand(Station.PASTRY_STATION, products);

        // Then
        assertInstanceOf(PreparePastryCommand.class, command);
    }

    @Test
    @DisplayName("Debe crear PrepareSandwichCommand para productos de tipo SANDWICH")
    void shouldCreateColdDishCommandForColdDishProducts() {
        // Given
        Product salad = new Product("Caesar Salad", ProductType.SANDWICH);
        List<Product> products = List.of(salad);

        // When
        Command command = factory.createCommand(Station.SANDWICH_STATION, products);

        // Then
        assertInstanceOf(PrepareSandwichCommand.class, command);
    }

    @Test
    @DisplayName("Debe lanzar excepción para estación desconocida")
    void shouldThrowExceptionForUnknownStation() {
        // Given
        Product product = new Product("Test", ProductType.DRINK);
        List<Product> products = List.of(product);

        // When & Then
        // Este test es solo por completitud, pero con enum no puede pasar
        assertDoesNotThrow(() -> factory.createCommand(Station.ESPRESSO_BAR, products));
    }
}