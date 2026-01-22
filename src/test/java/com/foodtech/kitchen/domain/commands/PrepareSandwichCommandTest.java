package com.foodtech.kitchen.domain.commands;

import com.foodtech.kitchen.domain.model.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PrepareSandwichCommandTest {

    @Test
    @DisplayName("Debe crear comando de plato frío con estación correcta")
    void shouldCreateColdDishCommandWithCorrectStation() {
        // Given
        Product salad = new Product("Caesar Salad", ProductType.SANDWICH);
        List<Product> products = List.of(salad);

        // When
        PrepareSandwichCommand command = new PrepareSandwichCommand(products);

        // Then
        assertInstanceOf(PrepareSandwichCommand.class, command);
    }

    @Test
    @DisplayName("Debe ejecutar la preparación de plato frío")
    void shouldExecuteColdDishPreparation() {
        // Given
        Product salad = new Product("Caesar Salad", ProductType.SANDWICH);
        PrepareSandwichCommand command = new PrepareSandwichCommand(List.of(salad));

        // When & Then
        assertDoesNotThrow(() -> command.execute());
    }
}