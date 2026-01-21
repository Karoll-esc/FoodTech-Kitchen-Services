package com.foodtech.kitchen.infrastructure.persistence.adapters;

import com.foodtech.kitchen.domain.model.Price;
import com.foodtech.kitchen.domain.model.Product;
import com.foodtech.kitchen.domain.model.ProductType;
import com.foodtech.kitchen.infrastructure.persistence.jpa.entities.ProductEntity;
import com.foodtech.kitchen.infrastructure.persistence.jpa.ProductJpaRepository;
import com.foodtech.kitchen.infrastructure.persistence.mappers.ProductEntityMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import({ProductRepositoryAdapter.class, ProductEntityMapper.class})
class ProductRepositoryAdapterTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ProductJpaRepository jpaRepository;

    @Autowired
    private ProductRepositoryAdapter repositoryAdapter;

    @BeforeEach
    void setUp() {
        // Clear database before each test
        jpaRepository.deleteAll();
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    @DisplayName("Debe guardar un producto nuevo correctamente")
    void shouldSaveNewProductSuccessfully() {
        // Given
        Product newProduct = new Product(
            "Coca Cola",
            "Bebida refrescante",
            ProductType.DRINK,
            new Price(new BigDecimal("5.00")),
            300
        );

        // When
        Product savedProduct = repositoryAdapter.save(newProduct);

        // Then
        assertNotNull(savedProduct);
        assertNotNull(savedProduct.getId());
        assertEquals("Coca Cola", savedProduct.getName());
        assertEquals("Bebida refrescante", savedProduct.getDescription());
        assertEquals(ProductType.DRINK, savedProduct.getType());
        assertEquals(new BigDecimal("5.00"), savedProduct.getPrice().getAmount());
        assertEquals(300, savedProduct.getPreparationTime());
        assertTrue(savedProduct.isAvailable());
        assertNotNull(savedProduct.getCreatedAt());
        assertNotNull(savedProduct.getUpdatedAt());
    }

    @Test
    @DisplayName("Debe actualizar un producto existente correctamente")
    void shouldUpdateExistingProductSuccessfully() {
        // Given - Create initial product
        ProductEntity initialEntity = ProductEntity.builder()
            .name("Pizza Original")
            .description("Pizza clásica")
            .type(ProductType.HOT_DISH)
            .price(new BigDecimal("10.00"))
            .preparationTimeSeconds(600)
            .available(true)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
        
        ProductEntity savedEntity = entityManager.persistAndFlush(initialEntity);
        Long productId = savedEntity.getId();

        // Create updated product with same ID
        Product updatedProduct = new Product(
            productId,
            "Pizza Original",
            "Pizza clásica mejorada",
            ProductType.HOT_DISH,
            new Price(new BigDecimal("12.00")),
            700,
            true
        );

        // When
        Product result = repositoryAdapter.save(updatedProduct);

        // Then
        entityManager.clear();
        ProductEntity updatedEntity = entityManager.find(ProductEntity.class, productId);
        
        assertNotNull(result);
        assertEquals(productId, result.getId());
        assertEquals("Pizza clásica mejorada", updatedEntity.getDescription());
        assertEquals(0, new BigDecimal("12.00").compareTo(updatedEntity.getPrice()));
        assertEquals(700, updatedEntity.getPreparationTimeSeconds());
    }

    @Test
    @DisplayName("Debe encontrar un producto por ID")
    void shouldFindProductById() {
        // Given
        ProductEntity entity = createAndPersistProductEntity(
            "Sprite",
            "Bebida de limón",
            ProductType.DRINK,
            new BigDecimal("4.50"),
            180,
            true
        );

        // When
        Optional<Product> result = repositoryAdapter.findById(entity.getId());

        // Then
        assertTrue(result.isPresent());
        Product product = result.get();
        assertEquals(entity.getId(), product.getId());
        assertEquals("Sprite", product.getName());
        assertEquals("Bebida de limón", product.getDescription());
        assertEquals(ProductType.DRINK, product.getType());
        assertEquals(new BigDecimal("4.50"), product.getPrice().getAmount());
        assertEquals(180, product.getPreparationTime());
        assertTrue(product.isAvailable());
    }

    @Test
    @DisplayName("Debe retornar Optional vacío cuando el producto no existe")
    void shouldReturnEmptyOptionalWhenProductNotFound() {
        // When
        Optional<Product> result = repositoryAdapter.findById(999L);

        // Then
        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("Debe encontrar todos los productos")
    void shouldFindAllProducts() {
        // Given
        createAndPersistProductEntity("Coca Cola", "Bebida", ProductType.DRINK, new BigDecimal("5.00"), 300, true);
        createAndPersistProductEntity("Pizza", "Pizza clásica", ProductType.HOT_DISH, new BigDecimal("12.00"), 900, true);
        createAndPersistProductEntity("Ensalada", "Fresca", ProductType.COLD_DISH, new BigDecimal("8.00"), 600, false);

        // When
        List<Product> result = repositoryAdapter.findAll();

        // Then
        assertNotNull(result);
        assertEquals(3, result.size());
    }

    @Test
    @DisplayName("Debe retornar lista vacía cuando no hay productos")
    void shouldReturnEmptyListWhenNoProducts() {
        // When
        List<Product> result = repositoryAdapter.findAll();

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Debe encontrar productos por disponibilidad (disponibles)")
    void shouldFindAvailableProducts() {
        // Given
        createAndPersistProductEntity("Coca Cola", "Bebida", ProductType.DRINK, new BigDecimal("5.00"), 300, true);
        createAndPersistProductEntity("Pizza", "Pizza", ProductType.HOT_DISH, new BigDecimal("12.00"), 900, true);
        createAndPersistProductEntity("Ensalada", "Fresca", ProductType.COLD_DISH, new BigDecimal("8.00"), 600, false);

        // When
        List<Product> result = repositoryAdapter.findByAvailable(true);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(Product::isAvailable));
    }

    @Test
    @DisplayName("Debe encontrar productos por disponibilidad (no disponibles)")
    void shouldFindUnavailableProducts() {
        // Given
        createAndPersistProductEntity("Coca Cola", "Bebida", ProductType.DRINK, new BigDecimal("5.00"), 300, true);
        createAndPersistProductEntity("Pizza", "Pizza", ProductType.HOT_DISH, new BigDecimal("12.00"), 900, false);
        createAndPersistProductEntity("Ensalada", "Fresca", ProductType.COLD_DISH, new BigDecimal("8.00"), 600, false);

        // When
        List<Product> result = repositoryAdapter.findByAvailable(false);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.stream().noneMatch(Product::isAvailable));
    }

    @Test
    @DisplayName("Debe encontrar productos por tipo (DRINK)")
    void shouldFindProductsByTypeDrink() {
        // Given
        createAndPersistProductEntity("Coca Cola", "Bebida", ProductType.DRINK, new BigDecimal("5.00"), 300, true);
        createAndPersistProductEntity("Sprite", "Bebida", ProductType.DRINK, new BigDecimal("4.50"), 300, true);
        createAndPersistProductEntity("Pizza", "Pizza", ProductType.HOT_DISH, new BigDecimal("12.00"), 900, true);

        // When
        List<Product> result = repositoryAdapter.findByType(ProductType.DRINK);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(p -> p.getType() == ProductType.DRINK));
    }

    @Test
    @DisplayName("Debe encontrar productos por tipo (HOT_DISH)")
    void shouldFindProductsByTypeHotDish() {
        // Given
        createAndPersistProductEntity("Pizza", "Pizza", ProductType.HOT_DISH, new BigDecimal("12.00"), 900, true);
        createAndPersistProductEntity("Pasta", "Pasta", ProductType.HOT_DISH, new BigDecimal("10.00"), 800, true);
        createAndPersistProductEntity("Ensalada", "Fresca", ProductType.COLD_DISH, new BigDecimal("8.00"), 600, true);

        // When
        List<Product> result = repositoryAdapter.findByType(ProductType.HOT_DISH);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(p -> p.getType() == ProductType.HOT_DISH));
    }

    @Test
    @DisplayName("Debe encontrar productos por tipo (COLD_DISH)")
    void shouldFindProductsByTypeColdDish() {
        // Given
        createAndPersistProductEntity("Ensalada César", "Fresca", ProductType.COLD_DISH, new BigDecimal("8.00"), 600, true);
        createAndPersistProductEntity("Ensalada Griega", "Mediterránea", ProductType.COLD_DISH, new BigDecimal("9.00"), 500, true);
        createAndPersistProductEntity("Pizza", "Pizza", ProductType.HOT_DISH, new BigDecimal("12.00"), 900, true);

        // When
        List<Product> result = repositoryAdapter.findByType(ProductType.COLD_DISH);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(p -> p.getType() == ProductType.COLD_DISH));
    }

    @Test
    @DisplayName("Debe retornar lista vacía cuando no hay productos del tipo especificado")
    void shouldReturnEmptyListWhenNoProductsOfType() {
        // Given
        createAndPersistProductEntity("Pizza", "Pizza", ProductType.HOT_DISH, new BigDecimal("12.00"), 900, true);

        // When
        List<Product> result = repositoryAdapter.findByType(ProductType.DRINK);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Debe encontrar producto por nombre exacto")
    void shouldFindProductByName() {
        // Given
        createAndPersistProductEntity("Coca Cola", "Bebida", ProductType.DRINK, new BigDecimal("5.00"), 300, true);
        createAndPersistProductEntity("Sprite", "Bebida", ProductType.DRINK, new BigDecimal("4.50"), 300, true);

        // When
        Optional<Product> result = repositoryAdapter.findByName("Coca Cola");

        // Then
        assertTrue(result.isPresent());
        assertEquals("Coca Cola", result.get().getName());
    }

    @Test
    @DisplayName("Debe retornar Optional vacío cuando no existe producto con ese nombre")
    void shouldReturnEmptyOptionalWhenProductNameNotFound() {
        // Given
        createAndPersistProductEntity("Coca Cola", "Bebida", ProductType.DRINK, new BigDecimal("5.00"), 300, true);

        // When
        Optional<Product> result = repositoryAdapter.findByName("Pepsi");

        // Then
        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("Debe eliminar un producto por ID")
    void shouldDeleteProductById() {
        // Given
        ProductEntity entity = createAndPersistProductEntity(
            "Coca Cola",
            "Bebida",
            ProductType.DRINK,
            new BigDecimal("5.00"),
            300,
            true
        );
        Long productId = entity.getId();

        // When
        repositoryAdapter.deleteById(productId);
        entityManager.flush();
        entityManager.clear();

        // Then
        ProductEntity deletedEntity = entityManager.find(ProductEntity.class, productId);
        assertNull(deletedEntity);
    }

    @Test
    @DisplayName("Debe verificar si existe un producto con el nombre dado (existe)")
    void shouldReturnTrueWhenProductExistsByName() {
        // Given
        createAndPersistProductEntity("Coca Cola", "Bebida", ProductType.DRINK, new BigDecimal("5.00"), 300, true);

        // When
        boolean exists = repositoryAdapter.existsByName("Coca Cola");

        // Then
        assertTrue(exists);
    }

    @Test
    @DisplayName("Debe verificar si existe un producto con el nombre dado (no existe)")
    void shouldReturnFalseWhenProductDoesNotExistByName() {
        // Given
        createAndPersistProductEntity("Coca Cola", "Bebida", ProductType.DRINK, new BigDecimal("5.00"), 300, true);

        // When
        boolean exists = repositoryAdapter.existsByName("Pepsi");

        // Then
        assertFalse(exists);
    }

    @Test
    @DisplayName("Debe manejar correctamente la búsqueda con nombre case-sensitive")
    void shouldHandleCaseSensitiveNameSearch() {
        // Given
        createAndPersistProductEntity("Coca Cola", "Bebida", ProductType.DRINK, new BigDecimal("5.00"), 300, true);

        // When
        Optional<Product> exact = repositoryAdapter.findByName("Coca Cola");
        Optional<Product> lowercase = repositoryAdapter.findByName("coca cola");
        Optional<Product> uppercase = repositoryAdapter.findByName("COCA COLA");

        // Then
        assertTrue(exact.isPresent());
        assertFalse(lowercase.isPresent());
        assertFalse(uppercase.isPresent());
    }

    @Test
    @DisplayName("Debe preservar los timestamps al actualizar")
    void shouldPreserveTimestampsOnUpdate() throws InterruptedException {
        // Given - Create initial product
        ProductEntity initialEntity = ProductEntity.builder()
            .name("Pizza")
            .description("Pizza clásica")
            .type(ProductType.HOT_DISH)
            .price(new BigDecimal("10.00"))
            .preparationTimeSeconds(600)
            .available(true)
            .createdAt(LocalDateTime.now().minusDays(5))
            .updatedAt(LocalDateTime.now().minusDays(5))
            .build();
        
        ProductEntity savedEntity = entityManager.persistAndFlush(initialEntity);
        LocalDateTime originalCreatedAt = savedEntity.getCreatedAt();
        LocalDateTime originalUpdatedAt = savedEntity.getUpdatedAt();
        
        entityManager.clear();
        Thread.sleep(10);

        // Create updated product
        Product updatedProduct = new Product(
            savedEntity.getId(),
            "Pizza",
            "Pizza mejorada",
            ProductType.HOT_DISH,
            new Price(new BigDecimal("12.00")),
            700,
            true
        );
        updatedProduct.updateDetails("Pizza mejorada", null, null);

        // When
        repositoryAdapter.save(updatedProduct);
        entityManager.flush();
        entityManager.clear();

        // Then
        ProductEntity resultEntity = entityManager.find(ProductEntity.class, savedEntity.getId());
        assertEquals(originalCreatedAt.withNano(0), resultEntity.getCreatedAt().withNano(0));
        assertTrue(resultEntity.getUpdatedAt().isAfter(originalUpdatedAt) || 
                   resultEntity.getUpdatedAt().isEqual(originalUpdatedAt));
    }

    // Helper method to create and persist product entities
    private ProductEntity createAndPersistProductEntity(
        String name,
        String description,
        ProductType type,
        BigDecimal price,
        Integer preparationTime,
        Boolean available
    ) {
        ProductEntity entity = ProductEntity.builder()
            .name(name)
            .description(description)
            .type(type)
            .price(price)
            .preparationTimeSeconds(preparationTime)
            .available(available)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
        
        return entityManager.persistAndFlush(entity);
    }
}
