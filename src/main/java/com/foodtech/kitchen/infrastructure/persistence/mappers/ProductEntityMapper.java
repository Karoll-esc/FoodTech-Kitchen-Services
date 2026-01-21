package com.foodtech.kitchen.infrastructure.persistence.mappers;

import com.foodtech.kitchen.domain.model.Price;
import com.foodtech.kitchen.domain.model.Product;
import com.foodtech.kitchen.infrastructure.persistence.jpa.entities.ProductEntity;
import com.foodtech.kitchen.infrastructure.persistence.jpa.entities.TaskProductEntity;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Mapper bidireccional entre Product (dominio) y ProductEntity (JPA).
 * 
 * <p>Este mapper es responsable de convertir entre la representación de dominio
 * de un producto (Product) y su representación de persistencia (ProductEntity).
 * Maneja tanto productos del catálogo (HU-006) como productos legacy del sistema
 * de órdenes/tareas existente.</p>
 * 
 * <p><strong>Arquitectura:</strong> Infrastructure Layer - Mapper</p>
 * 
 * <p><strong>Responsabilidades:</strong></p>
 * <ul>
 *   <li>Convertir ProductEntity a Product (toDomain)</li>
 *   <li>Convertir Product a ProductEntity (toEntity)</li>
 *   <li>Mantener compatibilidad con sistema orden/tarea legacy</li>
 *   <li>Manejar conversión de tipos (BigDecimal ↔ Price)</li>
 *   <li>Detectar y manejar productos catalog vs legacy</li>
 * </ul>
 * 
 * <p><strong>Compatibilidad Backward:</strong></p>
 * <p>El sistema existente de órdenes/tareas usa productos simples (solo name + type)
 * sin campos de catálogo (price, description, preparationTime). Este mapper detecta
 * automáticamente si una ProductEntity es del catálogo o legacy y aplica la conversión
 * apropiada:</p>
 * <ul>
 *   <li><strong>Productos Catálogo:</strong> Tienen price, description, preparationTime poblados</li>
 *   <li><strong>Productos Legacy:</strong> Solo tienen name y type (campos catalog son null)</li>
 * </ul>
 * 
 * <p><strong>Métodos de Conversión:</strong></p>
 * <ul>
 *   <li>toDomain(ProductEntity) - Detecta catalog vs legacy automáticamente</li>
 *   <li>toEntity(Product) - Para productos del catálogo (campos completos)</li>
 *   <li>toProductEntity(Product) - Para productos legacy (solo name + type)</li>
 *   <li>toTaskProductEntity(Product) - Para productos en tareas (TaskProductEntity)</li>
 *   <li>toDomain(TaskProductEntity) - De TaskProductEntity a Product</li>
 * </ul>
 * 
 * <p><strong>HU Relacionadas:</strong></p>
 * <ul>
 *   <li>HU-006 Gestión del Catálogo de Productos (productos con todos los campos)</li>
 *   <li>HU-001 Recepción de Órdenes (productos legacy, solo name + type)</li>
 * </ul>
 * 
 * @see Product Entidad de dominio
 * @see ProductEntity Entidad JPA para productos
 * @see TaskProductEntity Entidad JPA para productos en tareas
 * @see Price Value Object para precios
 */
@Component
public class ProductEntityMapper {

    /**
     * Convierte ProductEntity (JPA) a Product (dominio).
     * 
     * <p>Este método detecta automáticamente si la entidad representa un
     * producto del catálogo (HU-006) o un producto legacy del sistema de
     * órdenes/tareas basándose en la presencia de campos de catálogo.</p>
     * 
     * <p><strong>Lógica de Detección:</strong></p>
     * <ul>
     *   <li>Si price == null OR description == null OR preparationTime == null → Producto Legacy</li>
     *   <li>Si todos los campos están poblados → Producto Catálogo</li>
     * </ul>
     * 
     * <p><strong>Conversión Legacy:</strong> Usa constructor Product(name, type) que
     * establece valores por defecto: description="", price=0.00, preparationTime=1</p>
     * 
     * <p><strong>Conversión Catálogo:</strong> Usa constructor completo con todos los campos,
     * incluyendo ID, timestamps y disponibilidad.</p>
     * 
     * @param entity la entidad JPA a convertir (puede ser null)
     * @return el producto de dominio, o null si entity es null
     */
    public Product toDomain(ProductEntity entity) {
        if (entity == null) {
            return null;
        }

        // Detectar si es producto legacy (sin campos de catálogo) o producto del catálogo
        if (entity.getPrice() == null || entity.getDescription() == null || entity.getPreparationTimeSeconds() == null) {
            // Producto legacy del sistema orden/tarea - usar constructor deprecated
            return new Product(entity.getName(), entity.getType());
        }

        // Producto del catálogo - usar constructor completo
        Product product = new Product(
            entity.getId(),
            entity.getName(),
            entity.getDescription(),
            entity.getType(),
            new Price(entity.getPrice()),
            entity.getPreparationTimeSeconds(),
            entity.getAvailable() != null ? entity.getAvailable() : true
        );

        return product;
    }

    /**
     * Convierte Product (dominio) a ProductEntity (JPA) para catálogo.
     * 
     * <p>Este método crea una ProductEntity con todos los campos del catálogo
     * poblados. Debe usarse para productos de HU-006 (Gestión del Catálogo).</p>
     * 
     * <p><strong>IMPORTANTE:</strong> Para productos legacy del sistema orden/tarea,
     * usar toProductEntity() en su lugar, que solo mapea name y type.</p>
     * 
     * <p><strong>Campos Mapeados:</strong></p>
     * <ul>
     *   <li>id - Puede ser null para productos nuevos</li>
     *   <li>name - Obligatorio</li>
     *   <li>description - Obligatorio (puede estar vacío "")</li>
     *   <li>type - Obligatorio (DRINK, HOT_DISH, COLD_DISH)</li>
     *   <li>price - Extraído de Price value object</li>
     *   <li>preparationTimeSeconds - Tiempo en segundos</li>
     *   <li>available - Estado de disponibilidad</li>
     *   <li>createdAt/updatedAt - Timestamps del dominio</li>
     * </ul>
     * 
     * @param product el producto de dominio a convertir (puede ser null)
     * @return la entidad JPA, o null si product es null
     */
    public ProductEntity toEntity(Product product) {
        if (product == null) {
            return null;
        }

        return ProductEntity.builder()
            .id(product.getId())
            .name(product.getName())
            .description(product.getDescription())
            .type(product.getType())
            .price(product.getPrice().getAmount())
            .preparationTimeSeconds(product.getPreparationTimeSeconds())
            .available(product.isAvailable())
            .createdAt(product.getCreatedAt())
            .updatedAt(product.getUpdatedAt())
            .build();
    }

    /**
     * Convierte Product (dominio) a ProductEntity (JPA) para sistema legacy.
     * 
     * <p>Este método es para compatibilidad con el sistema existente de órdenes/tareas
     * que solo usa name y type. NO incluye campos del catálogo (price, description, etc.).</p>
     * 
     * <p><strong>Uso:</strong> Sistema de órdenes/tareas (HU-001, HU-002)</p>
     * 
     * <p><strong>Campos Mapeados:</strong></p>
     * <ul>
     *   <li>id - Del producto de dominio</li>
     *   <li>name - Nombre del producto</li>
     *   <li>type - Tipo de producto</li>
     *   <li>Campos de catálogo quedan null (price, description, etc.)</li>
     * </ul>
     * 
     * @param product el producto de dominio a convertir (puede ser null)
     * @return la entidad JPA simplificada, o null si product es null
     */
    public ProductEntity toProductEntity(Product product) {
        if (product == null) {
            return null;
        }

        return ProductEntity.builder()
            .id(product.getId())
            .name(product.getName())
            .type(product.getType())
            .build();
    }

    /**
     * Convierte Product (dominio) a TaskProductEntity (JPA).
     * 
     * <p>TaskProductEntity es una entidad embebida en Task que solo contiene
     * name y type. Se usa para almacenar productos dentro de tareas sin
     * duplicar toda la información del catálogo.</p>
     * 
     * <p><strong>Uso:</strong> Sistema de tareas (HU-002, HU-003)</p>
     * 
     * <p><strong>Campos Mapeados:</strong></p>
     * <ul>
     *   <li>name - Nombre del producto</li>
     *   <li>type - Tipo de producto</li>
     * </ul>
     * 
     * @param product el producto de dominio a convertir (puede ser null)
     * @return la entidad de tarea JPA, o null si product es null
     */
    public TaskProductEntity toTaskProductEntity(Product product) {
        if (product == null) {
            return null;
        }

        return TaskProductEntity.builder()
            .name(product.getName())
            .type(product.getType())
            .build();
    }

    /**
     * Convierte TaskProductEntity (JPA) a Product (dominio).
     * 
     * <p>Crea un producto de dominio usando el constructor legacy Product(name, type)
     * que establece valores por defecto para campos del catálogo.</p>
     * 
     * <p><strong>Valores por Defecto:</strong></p>
     * <ul>
     *   <li>description = ""</li>
     *   <li>price = 0.00</li>
     *   <li>preparationTimeSeconds = 1</li>
     *   <li>available = true</li>
     * </ul>
     * 
     * @param entity la entidad de tarea JPA a convertir (puede ser null)
     * @return el producto de dominio, o null si entity es null
     */
    public Product toDomain(TaskProductEntity entity) {
        if (entity == null) {
            return null;
        }

        return new Product(entity.getName(), entity.getType());
    }
}
