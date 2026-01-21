package com.foodtech.kitchen.infrastructure.rest.mapper;

import com.foodtech.kitchen.domain.model.Price;
import com.foodtech.kitchen.domain.model.Product;
import com.foodtech.kitchen.domain.model.ProductType;
import com.foodtech.kitchen.infrastructure.rest.dto.CreateProductRequest;
import com.foodtech.kitchen.infrastructure.rest.dto.ProductResponse;

/**
 * Mapper para conversiones entre DTOs de REST y entidades de dominio Product.
 * 
 * <p>Este mapper es stateless y usa métodos estáticos para conversiones.
 * NO requiere instanciación.</p>
 * 
 * <p><strong>Responsabilidades:</strong></p>
 * <ul>
 *   <li>Convertir CreateProductRequest → Product (dominio)</li>
 *   <li>Convertir Product (dominio) → ProductResponse</li>
 *   <li>Manejar conversión de tipos (String → Enum, BigDecimal → Price)</li>
 *   <li>Formatear timestamps a ISO-8601 String</li>
 * </ul>
 * 
 * <p><strong>Arquitectura:</strong> Infrastructure Layer - REST Mapper</p>
 */
public class ProductDtoMapper {

    private ProductDtoMapper() {
        // Utility class - no instantiation
    }

    /**
     * Convierte CreateProductRequest (DTO) a Product (dominio).
     * 
     * <p>El producto se crea con available=true por defecto y
     * timestamps auto-generados por el constructor de Product.</p>
     * 
     * @param request DTO con datos del producto a crear
     * @return producto de dominio listo para persistir
     * @throws IllegalArgumentException si el tipo de producto no es válido
     */
    public static Product toDomain(CreateProductRequest request) {
        ProductType type = ProductType.valueOf(request.type());
        Price price = new Price(request.price());
        
        return new Product(
            request.name(),
            request.description(),
            type,
            price,
            request.preparationTimeSeconds()
        );
    }

    /**
     * Convierte Product (dominio) a ProductResponse (DTO).
     * 
     * <p>Los timestamps se convierten a String en formato ISO-8601
     * para serialización JSON consistente.</p>
     * 
     * @param product producto de dominio
     * @return DTO de respuesta con todos los campos del producto
     */
    public static ProductResponse toResponse(Product product) {
        return new ProductResponse(
            product.getId(),
            product.getName(),
            product.getDescription(),
            product.getType().name(),
            product.getPrice().getAmount(),
            product.getPreparationTimeSeconds(),
            product.isAvailable(),
            product.getCreatedAt().toString(),
            product.getUpdatedAt().toString()
        );
    }
}
