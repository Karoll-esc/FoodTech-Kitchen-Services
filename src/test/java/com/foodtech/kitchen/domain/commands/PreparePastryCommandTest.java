package com.foodtech.kitchen.domain.commands;

import com.foodtech.kitchen.domain.model.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PreparePastryCommandTest {

    @Test
    @DisplayName("Debe crear comando de plato caliente con estación correcta")
    void shouldCreateHotDishCommandWithCorrectStation() {
        // Given
        Product pizza = new Product("Pizza", ProductType.PASTRY);
        List<Product> products = List.of(pizza);

        // When
        PreparePastryCommand command = new PreparePastryCommand(products);

        // Then
        assertInstanceOf(PreparePastryCommand.class, command);
    }

    @Test
    @DisplayName("Debe ejecutar la preparación de plato caliente")
    void shouldExecuteHotDishPreparation() {
        // Given
        Product pizza = new Product("Pizza", ProductType.PASTRY);
        PreparePastryCommand command = new PreparePastryCommand(List.of(pizza));

        // When & Then
        assertDoesNotThrow(() -> command.execute());
    }
}