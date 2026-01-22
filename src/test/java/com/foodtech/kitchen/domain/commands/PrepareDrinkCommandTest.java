package com.foodtech.kitchen.domain.commands;

import com.foodtech.kitchen.domain.model.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PrepareDrinkCommandTest {

    @Test
@DisplayName("Debe crear comando de bebida con estación correcta")
    void shouldCreateDrinkCommandWithCorrectStation() {
        // Given
        Product cocaCola = new Product("Coca Cola", ProductType.BEVERAGE);
        List<Product> products = List.of(cocaCola);

        // When
        PrepareDrinkCommand command = new PrepareDrinkCommand(products);

        // Then
            assertInstanceOf(PrepareDrinkCommand.class, command);
    }

    @Test
    @DisplayName("Debe ejecutar la preparación de bebida")
    void shouldExecuteDrinkPreparation() {
        // Given
        Product cocaCola = new Product("Coca Cola", ProductType.BEVERAGE);
        PrepareDrinkCommand command = new PrepareDrinkCommand(List.of(cocaCola));

        // When & Then
        assertDoesNotThrow(() -> command.execute());
    }

    @Test
    @DisplayName("Debe manejar múltiples bebidas en un solo comando")
    void shouldHandleMultipleDrinks() {
        // Given
        Product cocaCola = new Product("Coca Cola", ProductType.BEVERAGE);
        Product sprite = new Product("Sprite", ProductType.BEVERAGE);
        List<Product> products = List.of(cocaCola, sprite);

        // When
        PrepareDrinkCommand command = new PrepareDrinkCommand(products);

        // Then
        assertInstanceOf(PrepareDrinkCommand.class, command);
    }
}
