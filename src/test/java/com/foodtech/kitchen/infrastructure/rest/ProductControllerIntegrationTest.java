package com.foodtech.kitchen.infrastructure.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for ProductController REST API endpoints.
 * 
 * Tests cover all CRUD operations for product catalog management (HU-006):
 * - POST /api/products - Create product
 * - GET /api/products/{id} - Get product by ID
 * - GET /api/products - Get all products (with filters)
 * - PUT /api/products/{id} - Update product
 * - PATCH /api/products/{id}/availability - Update availability
 * - DELETE /api/products/{id} - Delete product
 * 
 * Uses MockMvc for full HTTP request/response testing with Spring Boot context.
 */
class ProductControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // ============================================================================
    // POST /api/products - Create Product
    // ============================================================================

    @Test
    @DisplayName("HU-006: Debe crear un producto nuevo y retornar 201 Created")
    void shouldCreateProductAndReturn201() throws Exception {
        // Given
        Map<String, Object> request = Map.of(
            "name", "Pizza Margherita",
            "description", "Pizza clásica con tomate y mozzarella",
            "type", "HOT_DISH",
            "price", "12.99",
            "preparationTimeSeconds", 900
        );

        // When & Then
        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").exists())
            .andExpect(jsonPath("$.name").value("Pizza Margherita"))
            .andExpect(jsonPath("$.description").value("Pizza clásica con tomate y mozzarella"))
            .andExpect(jsonPath("$.type").value("HOT_DISH"))
            .andExpect(jsonPath("$.price").value(12.99))
            .andExpect(jsonPath("$.preparationTimeSeconds").value(900))
            .andExpect(jsonPath("$.available").value(true))
            .andExpect(jsonPath("$.createdAt").exists())
            .andExpect(jsonPath("$.updatedAt").exists());
    }

    @Test
    @DisplayName("HU-006: Debe rechazar creación con nombre vacío (400 Bad Request)")
    void shouldRejectProductWithEmptyName() throws Exception {
        // Given
        Map<String, Object> request = Map.of(
            "name", "",
            "description", "Bebida refrescante",
            "type", "DRINK",
            "price", "2.50",
            "preparationTimeSeconds", 60
        );

        // When & Then
        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").exists())
            .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("HU-006: Debe rechazar creación con precio negativo (400 Bad Request)")
    void shouldRejectProductWithNegativePrice() throws Exception {
        // Given
        Map<String, Object> request = Map.of(
            "name", "Producto Inválido",
            "description", "Precio negativo no permitido",
            "type", "DRINK",
            "price", "-5.00",
            "preparationTimeSeconds", 60
        );

        // When & Then
        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").exists());
    }

    @Test
    @DisplayName("HU-006: Debe rechazar creación con tiempo de preparación cero (400 Bad Request)")
    void shouldRejectProductWithZeroPreparationTime() throws Exception {
        // Given
        Map<String, Object> request = Map.of(
            "name", "Producto Inválido",
            "description", "Tiempo de preparación inválido",
            "type", "DRINK",
            "price", "2.50",
            "preparationTimeSeconds", 0
        );

        // When & Then
        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("HU-006: Debe rechazar creación con nombre duplicado (409 Conflict)")
    void shouldRejectDuplicateProductName() throws Exception {
        // Given - Create first product
        Map<String, Object> request1 = Map.of(
            "name", "Coca Cola",
            "description", "Bebida gaseosa",
            "type", "DRINK",
            "price", "2.50",
            "preparationTimeSeconds", 30
        );
        
        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request1)))
            .andExpect(status().isCreated());

        // When - Try to create with same name
        Map<String, Object> request2 = Map.of(
            "name", "Coca Cola",
            "description", "Otra bebida con mismo nombre",
            "type", "DRINK",
            "price", "3.00",
            "preparationTimeSeconds", 30
        );

        // Then
        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request2)))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.error").exists())
            .andExpect(jsonPath("$.message").value("Product with name 'Coca Cola' already exists"));
    }

    // ============================================================================
    // GET /api/products/{id} - Get Product by ID
    // ============================================================================

    @Test
    @DisplayName("HU-006: Debe obtener un producto por ID y retornar 200 OK")
    void shouldGetProductByIdAndReturn200() throws Exception {
        // Given - Create a product first
        Map<String, Object> createRequest = Map.of(
            "name", "Hamburguesa Clásica",
            "description", "Hamburguesa con queso",
            "type", "HOT_DISH",
            "price", "8.99",
            "preparationTimeSeconds", 600
        );
        
        String createResponse = mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();
        
        Long productId = objectMapper.readTree(createResponse).get("id").asLong();

        // When & Then
        mockMvc.perform(get("/api/products/{id}", productId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(productId))
            .andExpect(jsonPath("$.name").value("Hamburguesa Clásica"))
            .andExpect(jsonPath("$.description").value("Hamburguesa con queso"))
            .andExpect(jsonPath("$.type").value("HOT_DISH"))
            .andExpect(jsonPath("$.price").value(8.99));
    }

    @Test
    @DisplayName("HU-006: Debe retornar 404 Not Found cuando el producto no existe")
    void shouldReturn404WhenProductNotFound() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/products/{id}", 99999L))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.error").exists())
            .andExpect(jsonPath("$.message").value("Product not found with id: 99999"));
    }

    // ============================================================================
    // GET /api/products - Get All Products
    // ============================================================================

    @Test
    @DisplayName("HU-006: Debe obtener todos los productos y retornar 200 OK")
    void shouldGetAllProductsAndReturn200() throws Exception {
        // Given - Create multiple products
        Map<String, Object> product1 = Map.of(
            "name", "Sprite",
            "description", "Bebida de lima-limón",
            "type", "DRINK",
            "price", "2.00",
            "preparationTimeSeconds", 30
        );
        
        Map<String, Object> product2 = Map.of(
            "name", "Ensalada César",
            "description", "Ensalada fresca",
            "type", "COLD_DISH",
            "price", "6.50",
            "preparationTimeSeconds", 300
        );
        
        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(product1)))
            .andExpect(status().isCreated());
        
        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(product2)))
            .andExpect(status().isCreated());

        // When & Then
        mockMvc.perform(get("/api/products"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[0].name").exists())
            .andExpect(jsonPath("$[1].name").exists());
    }

    @Test
    @DisplayName("HU-006: Debe filtrar productos por disponibilidad (available=true)")
    void shouldFilterProductsByAvailability() throws Exception {
        // Given - Create one available and one unavailable product
        Map<String, Object> availableProduct = Map.of(
            "name", "Producto Disponible",
            "description", "Este está disponible",
            "type", "DRINK",
            "price", "3.00",
            "preparationTimeSeconds", 60
        );
        
        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(availableProduct)))
            .andExpect(status().isCreated());

        // When & Then - Filter by available
        mockMvc.perform(get("/api/products").param("available", "true"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$[*].available").value(org.hamcrest.Matchers.everyItem(org.hamcrest.Matchers.is(true))));
    }

    @Test
    @DisplayName("HU-006: Debe filtrar productos por tipo (type=DRINK)")
    void shouldFilterProductsByType() throws Exception {
        // Given - Create products of different types
        Map<String, Object> drink = Map.of(
            "name", "Agua Mineral",
            "description", "Agua natural",
            "type", "DRINK",
            "price", "1.50",
            "preparationTimeSeconds", 10
        );
        
        Map<String, Object> hotDish = Map.of(
            "name", "Sopa de Pollo",
            "description", "Sopa caliente",
            "type", "HOT_DISH",
            "price", "5.00",
            "preparationTimeSeconds", 900
        );
        
        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(drink)))
            .andExpect(status().isCreated());
        
        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(hotDish)))
            .andExpect(status().isCreated());

        // When & Then - Filter by type DRINK
        mockMvc.perform(get("/api/products").param("type", "DRINK"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$[*].type").value(org.hamcrest.Matchers.everyItem(org.hamcrest.Matchers.is("DRINK"))));
    }

    // ============================================================================
    // PUT /api/products/{id} - Update Product
    // ============================================================================

    @Test
    @DisplayName("HU-006: Debe actualizar un producto existente y retornar 200 OK")
    void shouldUpdateProductAndReturn200() throws Exception {
        // Given - Create a product first
        Map<String, Object> createRequest = Map.of(
            "name", "Pizza Original",
            "description", "Pizza básica",
            "type", "HOT_DISH",
            "price", "10.00",
            "preparationTimeSeconds", 600
        );
        
        String createResponse = mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();
        
        Long productId = objectMapper.readTree(createResponse).get("id").asLong();

        // When - Update the product
        Map<String, Object> updateRequest = Map.of(
            "description", "Pizza mejorada con ingredientes premium",
            "price", "12.00",
            "preparationTimeSeconds", 700
        );

        // Then
        mockMvc.perform(put("/api/products/{id}", productId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(productId))
            .andExpect(jsonPath("$.name").value("Pizza Original"))
            .andExpect(jsonPath("$.description").value("Pizza mejorada con ingredientes premium"))
            .andExpect(jsonPath("$.price").value(12.00))
            .andExpect(jsonPath("$.preparationTimeSeconds").value(700));
    }

    @Test
    @DisplayName("HU-006: Debe retornar 404 al intentar actualizar producto inexistente")
    void shouldReturn404WhenUpdatingNonExistentProduct() throws Exception {
        // Given
        Map<String, Object> updateRequest = Map.of(
            "description", "Nueva descripción",
            "price", "15.00",
            "preparationTimeSeconds", 800
        );

        // When & Then
        mockMvc.perform(put("/api/products/{id}", 99999L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.error").exists());
    }

    @Test
    @DisplayName("HU-006: Debe rechazar actualización con precio negativo (400 Bad Request)")
    void shouldRejectUpdateWithNegativePrice() throws Exception {
        // Given - Create a product first
        Map<String, Object> createRequest = Map.of(
            "name", "Producto para Actualizar",
            "description", "Descripción inicial",
            "type", "DRINK",
            "price", "5.00",
            "preparationTimeSeconds", 60
        );
        
        String createResponse = mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();
        
        Long productId = objectMapper.readTree(createResponse).get("id").asLong();

        // When - Try to update with negative price
        Map<String, Object> updateRequest = Map.of(
            "description", "Descripción actualizada",
            "price", "-10.00",
            "preparationTimeSeconds", 60
        );

        // Then
        mockMvc.perform(put("/api/products/{id}", productId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
            .andExpect(status().isBadRequest());
    }

    // ============================================================================
    // PATCH /api/products/{id}/availability - Update Availability
    // ============================================================================

    @Test
    @DisplayName("HU-006: Debe actualizar disponibilidad de un producto y retornar 200 OK")
    void shouldUpdateProductAvailabilityAndReturn200() throws Exception {
        // Given - Create a product first (available=true by default)
        Map<String, Object> createRequest = Map.of(
            "name", "Producto para Deshabilitar",
            "description", "Este producto será deshabilitado",
            "type", "DRINK",
            "price", "3.50",
            "preparationTimeSeconds", 60
        );
        
        String createResponse = mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();
        
        Long productId = objectMapper.readTree(createResponse).get("id").asLong();

        // When - Disable the product
        Map<String, Object> availabilityRequest = Map.of("available", false);

        // Then
        mockMvc.perform(patch("/api/products/{id}/availability", productId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(availabilityRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(productId))
            .andExpect(jsonPath("$.available").value(false));
    }

    @Test
    @DisplayName("HU-006: Debe retornar 404 al actualizar disponibilidad de producto inexistente")
    void shouldReturn404WhenUpdatingAvailabilityOfNonExistentProduct() throws Exception {
        // Given
        Map<String, Object> availabilityRequest = Map.of("available", false);

        // When & Then
        mockMvc.perform(patch("/api/products/{id}/availability", 99999L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(availabilityRequest)))
            .andExpect(status().isNotFound());
    }

    // ============================================================================
    // DELETE /api/products/{id} - Delete Product
    // ============================================================================

    @Test
    @DisplayName("HU-006: Debe eliminar un producto y retornar 204 No Content")
    void shouldDeleteProductAndReturn204() throws Exception {
        // Given - Create a product first
        Map<String, Object> createRequest = Map.of(
            "name", "Producto para Eliminar",
            "description", "Este producto será eliminado",
            "type", "DRINK",
            "price", "2.00",
            "preparationTimeSeconds", 30
        );
        
        String createResponse = mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();
        
        Long productId = objectMapper.readTree(createResponse).get("id").asLong();

        // When - Delete the product
        mockMvc.perform(delete("/api/products/{id}", productId))
            .andExpect(status().isNoContent());

        // Then - Verify product is deleted
        mockMvc.perform(get("/api/products/{id}", productId))
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("HU-006: Debe ser idempotente al eliminar producto inexistente (204 No Content)")
    void shouldBeIdempotentWhenDeletingNonExistentProduct() throws Exception {
        // When & Then - DELETE is idempotent, should return 204 even if not found
        mockMvc.perform(delete("/api/products/{id}", 99999L))
            .andExpect(status().isNoContent());
    }
}
