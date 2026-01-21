package com.foodtech.kitchen.domain.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Value Object que representa el precio de un producto en el catálogo.
 * 
 * <p>Este Value Object garantiza que los precios sean siempre válidos y estén
 * formateados consistentemente con exactamente 2 decimales. Es inmutable y
 * forma parte del Domain Layer (sin dependencias de framework).</p>
 * 
 * <p><strong>Arquitectura:</strong> Domain Layer - Value Object</p>
 * 
 * <p><strong>Reglas de Negocio:</strong></p>
 * <ul>
 *   <li>El precio no puede ser null</li>
 *   <li>El precio no puede ser negativo</li>
 *   <li>El precio siempre tiene exactamente 2 decimales</li>
 *   <li>El redondeo se realiza con HALF_UP (0.995 → 1.00)</li>
 * </ul>
 * 
 * <p><strong>Ejemplo de uso:</strong></p>
 * <pre>
 * Price price = new Price(new BigDecimal("10.99"));
 * BigDecimal amount = price.getAmount(); // 10.99 con scale=2
 * 
 * Price rounded = new Price(new BigDecimal("10.995")); // Se redondea a 11.00
 * </pre>
 * 
 * @see Product
 */
public class Price {

    private final BigDecimal amount;

    /**
     * Crea un nuevo precio con el monto especificado.
     * 
     * <p>El monto se redondea automáticamente a 2 decimales usando
     * el modo de redondeo HALF_UP (0.5 se redondea hacia arriba).</p>
     * 
     * @param amount el monto del precio, debe ser >= 0
     * @throws IllegalArgumentException si amount es null o negativo
     */
    public Price(BigDecimal amount) {
        validateAmount(amount);
        this.amount = amount.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Valida que el monto sea válido.
     * 
     * @param amount el monto a validar
     * @throws IllegalArgumentException si amount es null o negativo
     */
    private void validateAmount(BigDecimal amount) {
        if (amount == null) {
            throw new IllegalArgumentException(
                "Price cannot be null or negative"
            );
        }
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                "Price cannot be null or negative"
            );
        }
    }

    /**
     * Obtiene el monto del precio.
     * 
     * @return el monto con exactamente 2 decimales
     */
    public BigDecimal getAmount() {
        return amount;
    }

    /**
     * Compara este precio con otro objeto.
     * 
     * <p>Dos precios son iguales si sus montos son iguales
     * (usando BigDecimal.equals).</p>
     * 
     * @param o el objeto a comparar
     * @return true si los precios son iguales, false en caso contrario
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Price price = (Price) o;
        return Objects.equals(amount, price.amount);
    }

    /**
     * Genera el código hash para este precio.
     * 
     * @return el código hash basado en el monto
     */
    @Override
    public int hashCode() {
        return Objects.hash(amount);
    }

    /**
     * Retorna una representación en texto del precio.
     * 
     * @return el precio en formato "Price{amount=X.XX}"
     */
    @Override
    public String toString() {
        return "Price{amount=" + amount + "}";
    }
}
