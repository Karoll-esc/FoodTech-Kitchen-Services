package com.foodtech.kitchen.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity que representa un producto del catálogo del restaurante.
 * 
 * <p>Un producto es un ítem del menú (platillo o bebida) con información
 * detallada sobre su nombre, descripción, precio, tiempo de preparación
 * y disponibilidad. Los productos se utilizan para construir pedidos y
 * descomponer tareas para las estaciones de cocina.</p>
 * 
 * <p><strong>Arquitectura:</strong> Domain Layer - Entity (sin dependencias de framework)</p>
 * 
 * <p><strong>Reglas de Negocio:</strong></p>
 * <ul>
 *   <li>El nombre es obligatorio y único en el catálogo</li>
 *   <li>La descripción es obligatoria (puede estar vacía)</li>
 *   <li>El tipo determina la estación de cocina responsable</li>
 *   <li>El precio debe ser válido (>= 0, 2 decimales)</li>
 *   <li>El tiempo de preparación debe ser mayor a 0</li>
 *   <li>Los productos se crean como disponibles por defecto</li>
 *   <li>Los timestamps se gestionan automáticamente</li>
 * </ul>
 * 
 * <p><strong>Campos Inmutables:</strong></p>
 * <ul>
 *   <li>name - no se puede cambiar después de creación</li>
 *   <li>type - no se puede cambiar después de creación</li>
 *   <li>createdAt - establecido en construcción</li>
 * </ul>
 * 
 * <p><strong>Campos Mutables:</strong></p>
 * <ul>
 *   <li>id - establecido por el repositorio al persistir</li>
 *   <li>description - puede actualizarse con updateDetails()</li>
 *   <li>price - puede actualizarse con updateDetails()</li>
 *   <li>preparationTimeSeconds - puede actualizarse con updateDetails()</li>
 *   <li>available - puede cambiar para habilitar/deshabilitar producto</li>
 *   <li>updatedAt - actualizado automáticamente con updateDetails() y updateTimestamp()</li>
 * </ul>
 * 
 * <p><strong>Ejemplo de uso:</strong></p>
 * <pre>
 * Price price = new Price(new BigDecimal("12.99"));
 * Product pizza = new Product(
 *     "Pizza Margherita",
 *     "Pizza con tomate, mozzarella y albahaca",
 *     ProductType.PASTRY,
 *     price,
 *     900 // 15 minutos
 * );
 * 
 * // Producto creado con available=true, createdAt y updatedAt auto-set
 * assertTrue(pizza.isAvailable());
 * 
 * // Deshabilitar temporalmente
 * pizza.setAvailable(false);
 * pizza.updateTimestamp();
 * </pre>
 * 
 * @see ProductType
 * @see Price
 * @see Station
 */
public class Product {
    /**
     * Obtiene la URL de la imagen del producto.
     * @return la URL de la imagen o null si no tiene
     */
    public String getImageUrl() {
        return imageUrl;
    }

    /**
     * Establece la URL de la imagen del producto.
     * @param imageUrl la URL de la imagen
     */
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    private Long id;
    private final String name;
    private String description;
    private final ProductType type;
    private Price price;
    private int preparationTimeSeconds;
    private boolean available;
    private String imageUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Crea un nuevo producto con todos los campos requeridos.
     * 
     * <p>El producto se crea con:</p>
     * <ul>
     *   <li>available = true (por defecto)</li>
     *   <li>createdAt = LocalDateTime.now()</li>
     *   <li>updatedAt = LocalDateTime.now()</li>
     *   <li>id = null (será asignado al persistir)</li>
     * </ul>
     * 
     * @param name el nombre del producto (único, no vacío)
     * @param description descripción del producto (no null, puede estar vacía)
     * @param type tipo de producto (DRINK, PASTRY, SANDWICH)
     * @param price precio del producto (>= 0)
     * @param preparationTimeSeconds tiempo de preparación en segundos (> 0)
     * @throws IllegalArgumentException si algún parámetro no cumple las validaciones
     */
    public Product(String name, String description, ProductType type, Price price, int preparationTimeSeconds) {
        validateName(name);
        validateDescription(description);
        validateType(type);
        validatePrice(price);
        validatePreparationTime(preparationTimeSeconds);
        
        this.name = name;
        this.description = description;
        this.type = type;
        this.price = price;
        this.preparationTimeSeconds = preparationTimeSeconds;
        this.available = true;
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    /**
     * Constructor de compatibilidad para código existente.
     * 
     * <p>Crea un producto con valores por defecto:</p>
     * <ul>
     *   <li>description = ""</li>
     *   <li>price = 0.00</li>
     *   <li>preparationTimeSeconds = 1</li>
     * </ul>
     * 
     * @param name el nombre del producto
     * @param type el tipo de producto
     * @deprecated usar constructor completo para productos del catálogo
     */
    @Deprecated
    public Product(String name, ProductType type) {
        this(name, "", type, new Price(BigDecimal.ZERO), 1);
    }

    /**
     * Constructor para reconstruir un producto desde la base de datos.
     * 
     * <p>Este constructor incluye el ID y disponibilidad, utilizado por
     * el repositorio al cargar productos existentes.</p>
     * 
     * @param id el ID del producto
     * @param name el nombre del producto
     * @param description descripción del producto
     * @param type tipo de producto
     * @param price precio del producto
     * @param preparationTimeSeconds tiempo de preparación en segundos
     * @param available disponibilidad del producto
     */
    public Product(Long id, String name, String description, ProductType type, Price price, int preparationTimeSeconds, boolean available) {
        validateName(name);
        validateDescription(description);
        validateType(type);
        validatePrice(price);
        validatePreparationTime(preparationTimeSeconds);
        
        this.id = id;
        this.name = name;
        this.description = description;
        this.type = type;
        this.price = price;
        this.preparationTimeSeconds = preparationTimeSeconds;
        this.available = available;
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    /**
     * Valida que el nombre del producto sea válido.
     * 
     * @param name el nombre a validar
     * @throws IllegalArgumentException si name es null o está vacío
     */
    private void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Product name cannot be null or empty");
        }
    }

    /**
     * Valida que la descripción del producto sea válida.
     * 
     * @param description la descripción a validar
     * @throws IllegalArgumentException si description es null
     */
    private void validateDescription(String description) {
        if (description == null) {
            throw new IllegalArgumentException("Product description cannot be null");
        }
    }

    /**
     * Valida que el tipo de producto sea válido.
     * 
     * @param type el tipo a validar
     * @throws IllegalArgumentException si type es null
     */
    private void validateType(ProductType type) {
        if (type == null) {
            throw new IllegalArgumentException("Product type cannot be null");
        }
    }

    /**
     * Valida que el precio del producto sea válido.
     * 
     * @param price el precio a validar
     * @throws IllegalArgumentException si price es null
     */
    private void validatePrice(Price price) {
        if (price == null) {
            throw new IllegalArgumentException("Product price cannot be null");
        }
    }

    /**
     * Valida que el tiempo de preparación sea válido.
     * 
     * @param preparationTimeSeconds el tiempo a validar
     * @throws IllegalArgumentException si preparationTimeSeconds es <= 0
     */
    private void validatePreparationTime(int preparationTimeSeconds) {
        if (preparationTimeSeconds <= 0) {
            throw new IllegalArgumentException("Preparation time must be greater than zero");
        }
    }

    /**
     * Obtiene el ID del producto.
     * 
     * @return el ID del producto, null si no ha sido persistido
     */
    public Long getId() {
        return id;
    }

    /**
     * Establece el ID del producto.
     * 
     * <p>Este método es utilizado por el repositorio al persistir
     * el producto en la base de datos.</p>
     * 
     * @param id el ID asignado por la base de datos
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Obtiene el nombre del producto.
     * 
     * @return el nombre del producto
     */
    public String getName() {
        return name;
    }

    /**
     * Obtiene la descripción del producto.
     * 
     * @return la descripción del producto (puede estar vacía)
     */
    public String getDescription() {
        return description;
    }

    /**
     * Obtiene el tipo de producto.
     * 
     * @return el tipo (DRINK, PASTRY, SANDWICH)
     */
    public ProductType getType() {
        return type;
    }

    /**
     * Obtiene el precio del producto.
     * 
     * @return el precio como Value Object
     */
    public Price getPrice() {
        return price;
    }

    /**
     * Obtiene el tiempo de preparación en segundos.
     * 
     * @return el tiempo de preparación en segundos
     */
    public int getPreparationTimeSeconds() {
        return preparationTimeSeconds;
    }

    /**
     * Obtiene el tiempo de preparación.
     * 
     * <p>Alias de getPreparationTimeSeconds() que retorna Integer para compatibilidad.</p>
     * 
     * @return el tiempo de preparación en segundos como Integer
     */
    public Integer getPreparationTime() {
        return preparationTimeSeconds;
    }

    /**
     * Verifica si el producto está disponible.
     * 
     * @return true si está disponible, false si está deshabilitado
     */
    public boolean isAvailable() {
        return available;
    }

    /**
     * Establece la disponibilidad del producto.
     * 
     * <p>Utilizar este método para habilitar o deshabilitar
     * temporalmente un producto sin eliminarlo del catálogo.</p>
     * 
     * @param available true para habilitar, false para deshabilitar
     */
    public void setAvailable(boolean available) {
        this.available = available;
    }

    /**
     * Obtiene la fecha de creación del producto.
     * 
     * @return la fecha y hora de creación
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * Obtiene la fecha de última actualización del producto.
     * 
     * @return la fecha y hora de última actualización
     */
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    /**
     * Actualiza los campos mutables del producto.
     * 
     * <p>Solo actualiza los campos que no sean null. Si un campo es null,
     * se mantiene el valor actual.</p>
     * 
     * <p>Este método actualiza automáticamente el timestamp updatedAt.</p>
     * 
     * @param description nueva descripción (null para mantener actual)
     * @param price nuevo precio (null para mantener actual)
     * @param preparationTimeSeconds nuevo tiempo de preparación (null para mantener actual)
     */
    public void updateDetails(String description, Price price, Integer preparationTimeSeconds) {
        if (description != null) {
            validateDescription(description);
            this.description = description;
        }
        
        if (price != null) {
            validatePrice(price);
            this.price = price;
        }
        
        if (preparationTimeSeconds != null) {
            validatePreparationTime(preparationTimeSeconds);
            this.preparationTimeSeconds = preparationTimeSeconds;
        }
        
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Actualiza el timestamp de última modificación.
     * 
     * <p>Este método debe invocarse cada vez que se modifica
     * un campo mutable del producto (ej: available).</p>
     */
    public void updateTimestamp() {
        this.updatedAt = LocalDateTime.now();
    }
}

