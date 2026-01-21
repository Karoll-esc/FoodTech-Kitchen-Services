package com.foodtech.kitchen.infrastructure.persistence.adapters;

import com.foodtech.kitchen.application.ports.out.TableRepository;
import com.foodtech.kitchen.domain.model.Table;
import com.foodtech.kitchen.domain.model.TableStatus;
import com.foodtech.kitchen.infrastructure.persistence.jpa.TableJpaRepository;
import com.foodtech.kitchen.infrastructure.persistence.jpa.entities.TableEntity;
import com.foodtech.kitchen.infrastructure.persistence.mappers.TableEntityMapper;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class TableRepositoryAdapter implements TableRepository {
    
    private final TableJpaRepository jpaRepository;
    private final TableEntityMapper mapper;
    
    public TableRepositoryAdapter(TableJpaRepository jpaRepository, TableEntityMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }
    
    @Override
    public Table save(Table table) {
        TableEntity entity = mapper.toEntity(table);
        TableEntity savedEntity = jpaRepository.save(entity);
        return mapper.toDomain(savedEntity);
    }
    
    @Override
    public Optional<Table> findById(Long id) {
        return jpaRepository.findById(id)
            .map(mapper::toDomain);
    }
    
    @Override
    public Optional<Table> findByTableNumber(String tableNumber) {
        return jpaRepository.findByTableNumber(tableNumber)
            .map(mapper::toDomain);
    }
    
    @Override
    public List<Table> findAll() {
        return jpaRepository.findAll().stream()
            .map(mapper::toDomain)
            .collect(Collectors.toList());
    }
    
    @Override
    public List<Table> findByStatus(TableStatus status) {
        return jpaRepository.findByStatus(status).stream()
            .map(mapper::toDomain)
            .collect(Collectors.toList());
    }
    
    @Override
    public boolean existsByTableNumber(String tableNumber) {
        return jpaRepository.existsByTableNumber(tableNumber);
    }
    
    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }
}
