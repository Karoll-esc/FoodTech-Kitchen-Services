package com.foodtech.kitchen.infrastructure.persistence.adapters;

import com.foodtech.kitchen.application.ports.out.ProductRepository;
import com.foodtech.kitchen.domain.model.Product;
import com.foodtech.kitchen.domain.model.ProductType;
import com.foodtech.kitchen.infrastructure.persistence.jpa.ProductJpaRepository;
import com.foodtech.kitchen.infrastructure.persistence.jpa.entities.ProductEntity;
import com.foodtech.kitchen.infrastructure.persistence.mappers.ProductEntityMapper;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Adapter que implementa el puerto ProductRepository usando JPA.
 * 
 * <p>Este adapter es parte de la capa de infraestructura y conecta
 * la capa de aplicación con la base de datos relacional a través de
 * Spring Data JPA. Traduce operaciones de dominio en operaciones de
 * persistencia.</p>
 * 
 * <p><strong>Arquitectura:</strong> Infrastructure Layer - Repository Adapter (Hexagonal Architecture)</p>
 * 
 * <p><strong>Responsabilidades:</strong></p>
 * <ul>
 *   <li>Implementar el puerto ProductRepository definido en application layer</li>
 *   <li>Delegar operaciones CRUD a ProductJpaRepository (Spring Data JPA)</li>
 *   <li>Convertir entidades JPA (ProductEntity) a entidades de dominio (Product)</li>
 *   <li>Convertir entidades de dominio a entidades JPA para persistencia</li>
 *   <li>Manejar la lógica de actualización vs creación de productos</li>
 * </ul>
 * 
 * <p><strong>Patrones de Diseño:</strong></p>
 * <ul>
 *   <li>Adapter Pattern: Adapta ProductJpaRepository a ProductRepository port</li>
 *   <li>Repository Pattern: Abstrae la persistencia de datos</li>
 *   <li>Dependency Inversion: Implementa puerto definido en capa superior</li>
 * </ul>
 * 
 * <p><strong>Detalles de Implementación:</strong></p>
 * <ul>
 *   <li>Usa saveAndFlush() para garantizar persistencia inmediata en tests</li>
 *   <li>Detecta updates vs creates basándose en la presencia del ID</li>
 *   <li>Para updates, carga la entidad existente y actualiza sus campos</li>
 *   <li>Mantiene entidades gestionadas por JPA para aprovechar dirty checking</li>
 * </ul>
 * 
 * <p><strong>HU Relacionadas:</strong> HU-006 Gestión del Catálogo de Productos</p>
 * 
 * @see ProductRepository Puerto implementado por este adapter
 * @see ProductJpaRepository Repositorio JPA delegado
 * @see ProductEntityMapper Mapper entre entidades JPA y dominio
 * @see Product Entidad de dominio
 * @see ProductEntity Entidad JPA
 */
@Component
public class ProductRepositoryAdapter implements ProductRepository {

    private final ProductJpaRepository jpaRepository;
    private final ProductEntityMapper mapper;

    /**
     * Constructor con inyección de dependencias.
     * 
     * @param jpaRepository repositorio JPA para operaciones de base de datos
     * @param mapper mapper para conversión entre Product y ProductEntity
     */
    public ProductRepositoryAdapter(ProductJpaRepository jpaRepository, ProductEntityMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    /**
     * Guarda un producto en la base de datos (crear o actualizar).
     * 
     * <p>Esta operación maneja tanto la creación de nuevos productos como
     * la actualización de productos existentes. La distinción se hace basándose
     * en si el producto tiene un ID y si ese ID existe en la base de datos.</p>
     * 
     * <p><strong>Lógica de Decisión:</strong></p>
     * <ul>
     *   <li>Si product.getId() != null AND existe en BD → UPDATE (carga entidad existente)</li>
     *   <li>Si product.getId() == null OR no existe en BD → CREATE (nueva entidad)</li>
     * </ul>
     * 
     * <p><strong>Update Strategy:</strong> Para actualizaciones, se carga la entidad
     * existente y se actualizan sus campos manualmente. Esto mantiene la entidad
     * gestionada por JPA y preserva campos como createdAt. Se usa saveAndFlush()
     * para garantizar persistencia inmediata.</p>
     * 
     * <p><strong>Create Strategy:</strong> Para creaciones, se convierte el Product
     * de dominio a ProductEntity usando el mapper y se persiste.</p>
     * 
     * @param product el producto a guardar (con o sin ID)
     * @return el producto guardado con ID asignado y timestamps actualizados
     * @throws IllegalArgumentException si el producto no cumple validaciones de dominio
     */
    @Override
    public Product save(Product product) {
        if (isUpdate(product)) {
            return updateExistingProduct(product);
        }
        
        return createNewProduct(product);
    }

    /**
     * Verifica si la operación es una actualización de producto existente.
     * 
     * @param product el producto a verificar
     * @return true si es actualización, false si es creación
     */
    private boolean isUpdate(Product product) {
        return product.getId() != null && jpaRepository.existsById(product.getId());
    }

    /**
     * Actualiza un producto existente en la base de datos.
     * 
     * <p>Carga la entidad existente y actualiza sus campos manualmente para
     * mantenerla gestionada por JPA. Esto preserva campos no modificables
     * como createdAt y aprovecha el dirty checking de JPA.</p>
     * 
     * @param product el producto con los datos actualizados
     * @return el producto actualizado desde la base de datos
     */
    private Product updateExistingProduct(Product product) {
        ProductEntity existingEntity = jpaRepository.findById(product.getId()).orElseThrow();
        
        // Actualizar solo campos mutables
        existingEntity.setName(product.getName());
        existingEntity.setDescription(product.getDescription());
        existingEntity.setImageUrl(product.getImageUrl());
        existingEntity.setType(product.getType());
        existingEntity.setPrice(product.getPrice().getAmount());
        existingEntity.setPreparationTimeSeconds(product.getPreparationTimeSeconds());
        existingEntity.setAvailable(product.isAvailable());
        
        ProductEntity savedEntity = jpaRepository.saveAndFlush(existingEntity);
        return mapper.toDomain(savedEntity);
    }

    /**
     * Crea un nuevo producto en la base de datos.
     * 
     * <p>Convierte el producto de dominio a entidad JPA y lo persiste.
     * El ID será asignado automáticamente por la base de datos.</p>
     * 
     * @param product el producto a crear
     * @return el producto creado con ID asignado
     */
    private Product createNewProduct(Product product) {
        ProductEntity entity = mapper.toEntity(product);
        ProductEntity savedEntity = jpaRepository.saveAndFlush(entity);
        return mapper.toDomain(savedEntity);
    }

    /**
     * Busca un producto por su ID.
     * 
     * @param id el ID del producto a buscar
     * @return Optional con el producto si existe, Optional.empty() si no existe
     */
    @Override
    public Optional<Product> findById(Long id) {
        return jpaRepository.findById(id)
            .map(mapper::toDomain);
    }

    /**
     * Obtiene todos los productos del catálogo.
     * 
     * <p>Incluye tanto productos disponibles como no disponibles.
     * Para filtrar solo disponibles, usar findByAvailable(true).</p>
     * 
     * @return lista de todos los productos (puede estar vacía)
     */
    @Override
    public List<Product> findAll() {
        return jpaRepository.findAll().stream()
            .map(mapper::toDomain)
            .collect(Collectors.toList());
    }

    /**
     * Busca productos por su estado de disponibilidad.
     * 
     * @param available true para productos disponibles, false para no disponibles
     * @return lista de productos que coinciden con el estado (puede estar vacía)
     */
    @Override
    public List<Product> findByAvailable(boolean available) {
        return jpaRepository.findByAvailable(available).stream()
            .map(mapper::toDomain)
            .collect(Collectors.toList());
    }

    /**
     * Busca productos por su tipo.
     * 
     * @param type el tipo de producto (DRINK, PASTRY, SANDWICH)
     * @return lista de productos del tipo especificado (puede estar vacía)
     */
    @Override
    public List<Product> findByType(ProductType type) {
        return jpaRepository.findByType(type).stream()
            .map(mapper::toDomain)
            .collect(Collectors.toList());
    }

    /**
     * Busca un producto por su nombre exacto.
     * 
     * <p>La búsqueda es case-sensitive y debe coincidir exactamente.</p>
     * 
     * @param name el nombre exacto del producto
     * @return Optional con el producto si existe, Optional.empty() si no existe
     */
    @Override
    public Optional<Product> findByName(String name) {
        return jpaRepository.findByName(name)
            .map(mapper::toDomain);
    }

    /**
     * Elimina un producto por su ID.
     * 
     * <p>Si el ID no existe, la operación no falla (idempotente).
     * Spring Data JPA maneja silenciosamente IDs no existentes.</p>
     * 
     * @param id el ID del producto a eliminar
     */
    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }

    /**
     * Verifica si existe un producto con el nombre dado.
     * 
     * <p>Útil para validar unicidad de nombres antes de crear/actualizar.</p>
     * 
     * @param name el nombre a verificar
     * @return true si existe un producto con ese nombre, false si no existe
     */
    @Override
    public boolean existsByName(String name) {
        return jpaRepository.existsByName(name);
    }
}
