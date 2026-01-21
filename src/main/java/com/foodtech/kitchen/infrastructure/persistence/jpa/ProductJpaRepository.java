package com.foodtech.kitchen.infrastructure.persistence.jpa;

import com.foodtech.kitchen.domain.model.ProductType;
import com.foodtech.kitchen.infrastructure.persistence.jpa.entities.ProductEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductJpaRepository extends JpaRepository<ProductEntity, Long> {
    
    List<ProductEntity> findByAvailable(Boolean available);
    
    List<ProductEntity> findByType(ProductType type);
    
    Optional<ProductEntity> findByName(String name);
    
    boolean existsByName(String name);
}
