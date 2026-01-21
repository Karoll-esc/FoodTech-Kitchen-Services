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

@Component
public class ProductRepositoryAdapter implements ProductRepository {

    private final ProductJpaRepository jpaRepository;
    private final ProductEntityMapper mapper;

    public ProductRepositoryAdapter(ProductJpaRepository jpaRepository, ProductEntityMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Product save(Product product) {
        if (product.getId() != null && jpaRepository.existsById(product.getId())) {
            ProductEntity existingEntity = jpaRepository.findById(product.getId()).orElseThrow();
            existingEntity.setName(product.getName());
            existingEntity.setDescription(product.getDescription());
            existingEntity.setType(product.getType());
            existingEntity.setPrice(product.getPrice().getAmount());
            existingEntity.setPreparationTimeSeconds(product.getPreparationTimeSeconds());
            existingEntity.setAvailable(product.isAvailable());
            
            ProductEntity savedEntity = jpaRepository.saveAndFlush(existingEntity);
            return mapper.toDomain(savedEntity);
        }
        
        ProductEntity entity = mapper.toEntity(product);
        ProductEntity savedEntity = jpaRepository.saveAndFlush(entity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Product> findById(Long id) {
        return jpaRepository.findById(id)
            .map(mapper::toDomain);
    }

    @Override
    public List<Product> findAll() {
        return jpaRepository.findAll().stream()
            .map(mapper::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    public List<Product> findByAvailable(boolean available) {
        return jpaRepository.findByAvailable(available).stream()
            .map(mapper::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    public List<Product> findByType(ProductType type) {
        return jpaRepository.findByType(type).stream()
            .map(mapper::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    public Optional<Product> findByName(String name) {
        return jpaRepository.findByName(name)
            .map(mapper::toDomain);
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public boolean existsByName(String name) {
        return jpaRepository.existsByName(name);
    }
}
