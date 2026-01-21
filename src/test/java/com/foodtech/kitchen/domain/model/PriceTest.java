package com.foodtech.kitchen.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class PriceTest {

    @Test
    @DisplayName("Debe crear un precio válido con dos decimales")
    void shouldCreateValidPriceWithTwoDecimals() {
        BigDecimal amount = new BigDecimal("10.99");
        Price price = new Price(amount);
        
        assertEquals(0, new BigDecimal("10.99").compareTo(price.getAmount()));
        assertEquals(2, price.getAmount().scale());
    }

    @Test
    @DisplayName("Debe redondear el precio a dos decimales")
    void shouldRoundPriceToTwoDecimals() {
        BigDecimal amount = new BigDecimal("10.995");
        Price price = new Price(amount);
        
        assertEquals(0, new BigDecimal("11.00").compareTo(price.getAmount()));
        assertEquals(2, price.getAmount().scale());
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando el precio es null")
    void shouldThrowExceptionWhenPriceIsNull() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new Price(null)
        );
        
        assertEquals("Price cannot be null or negative", exception.getMessage());
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando el precio es negativo")
    void shouldThrowExceptionWhenPriceIsNegative() {
        BigDecimal negativeAmount = new BigDecimal("-5.00");
        
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new Price(negativeAmount)
        );
        
        assertEquals("Price cannot be null or negative", exception.getMessage());
    }

    @Test
    @DisplayName("Debe aceptar precio cero")
    void shouldAcceptZeroPrice() {
        BigDecimal zeroAmount = BigDecimal.ZERO;
        Price price = new Price(zeroAmount);
        
        assertEquals(0, BigDecimal.ZERO.compareTo(price.getAmount()));
    }

    @Test
    @DisplayName("Debe implementar equals correctamente")
    void shouldImplementEqualsCorrectly() {
        Price price1 = new Price(new BigDecimal("10.99"));
        Price price2 = new Price(new BigDecimal("10.99"));
        Price price3 = new Price(new BigDecimal("20.00"));
        
        assertEquals(price1, price2);
        assertNotEquals(price1, price3);
        assertNotEquals(price1, null);
        assertNotEquals(price1, new Object());
    }

    @Test
    @DisplayName("Debe implementar hashCode correctamente")
    void shouldImplementHashCodeCorrectly() {
        Price price1 = new Price(new BigDecimal("10.99"));
        Price price2 = new Price(new BigDecimal("10.99"));
        
        assertEquals(price1.hashCode(), price2.hashCode());
    }

    @Test
    @DisplayName("Debe crear precio con valor muy grande")
    void shouldCreatePriceWithLargeValue() {
        BigDecimal largeAmount = new BigDecimal("999999.99");
        Price price = new Price(largeAmount);
        
        assertEquals(0, largeAmount.compareTo(price.getAmount()));
    }
}
