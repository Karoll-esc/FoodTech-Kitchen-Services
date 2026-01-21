package com.foodtech.kitchen.infrastructure.persistence.jpa;

import com.foodtech.kitchen.domain.model.TableStatus;
import com.foodtech.kitchen.infrastructure.persistence.jpa.entities.TableEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface TableJpaRepository extends JpaRepository<TableEntity, Long> {
    
    Optional<TableEntity> findByTableNumber(String tableNumber);
    
    boolean existsByTableNumber(String tableNumber);
    
    List<TableEntity> findByStatus(TableStatus status);
}
