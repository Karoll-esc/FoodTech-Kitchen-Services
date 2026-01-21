package com.foodtech.kitchen.infrastructure.persistence.adapters;

import com.foodtech.kitchen.domain.model.Table;
import com.foodtech.kitchen.domain.model.TableStatus;
import com.foodtech.kitchen.infrastructure.persistence.jpa.TableJpaRepository;
import com.foodtech.kitchen.infrastructure.persistence.jpa.entities.TableEntity;
import com.foodtech.kitchen.infrastructure.persistence.mappers.TableEntityMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TableRepositoryAdapter - Tests del adaptador de repositorio")
class TableRepositoryAdapterTest {

    @Mock
    private TableJpaRepository jpaRepository;

    @Mock
    private TableEntityMapper mapper;

    @InjectMocks
    private TableRepositoryAdapter adapter;

    private Table domainTable;
    private TableEntity tableEntity;

    @BeforeEach
    void setUp() {
        LocalDateTime now = LocalDateTime.now();
        
        domainTable = new Table(1L, "A1", 4, TableStatus.AVAILABLE, null, now, now);
        
        tableEntity = new TableEntity();
        tableEntity.setId(1L);
        tableEntity.setTableNumber("A1");
        tableEntity.setCapacity(4);
        tableEntity.setStatus(TableStatus.AVAILABLE);
        tableEntity.setCurrentOrderId(null);
        tableEntity.setCreatedAt(now);
        tableEntity.setUpdatedAt(now);
    }

    @Test
    @DisplayName("Debe guardar una mesa en el repositorio")
    void shouldSaveTable() {
        // Given
        when(mapper.toEntity(domainTable)).thenReturn(tableEntity);
        when(jpaRepository.save(tableEntity)).thenReturn(tableEntity);
        when(mapper.toDomain(tableEntity)).thenReturn(domainTable);

        // When
        Table saved = adapter.save(domainTable);

        // Then
        assertNotNull(saved);
        assertEquals("A1", saved.getTableNumber());
        verify(mapper).toEntity(domainTable);
        verify(jpaRepository).save(tableEntity);
        verify(mapper).toDomain(tableEntity);
    }

    @Test
    @DisplayName("Debe buscar mesa por ID")
    void shouldFindTableById() {
        // Given
        Long tableId = 1L;
        when(jpaRepository.findById(tableId)).thenReturn(Optional.of(tableEntity));
        when(mapper.toDomain(tableEntity)).thenReturn(domainTable);

        // When
        Optional<Table> found = adapter.findById(tableId);

        // Then
        assertTrue(found.isPresent());
        assertEquals("A1", found.get().getTableNumber());
        verify(jpaRepository).findById(tableId);
        verify(mapper).toDomain(tableEntity);
    }

    @Test
    @DisplayName("Debe retornar Optional vacío cuando no se encuentra mesa por ID")
    void shouldReturnEmptyOptionalWhenTableNotFoundById() {
        // Given
        Long tableId = 999L;
        when(jpaRepository.findById(tableId)).thenReturn(Optional.empty());

        // When
        Optional<Table> found = adapter.findById(tableId);

        // Then
        assertFalse(found.isPresent());
        verify(jpaRepository).findById(tableId);
        verify(mapper, never()).toDomain(any());
    }

    @Test
    @DisplayName("Debe buscar mesa por número")
    void shouldFindTableByTableNumber() {
        // Given
        String tableNumber = "A1";
        when(jpaRepository.findByTableNumber(tableNumber)).thenReturn(Optional.of(tableEntity));
        when(mapper.toDomain(tableEntity)).thenReturn(domainTable);

        // When
        Optional<Table> found = adapter.findByTableNumber(tableNumber);

        // Then
        assertTrue(found.isPresent());
        assertEquals("A1", found.get().getTableNumber());
        verify(jpaRepository).findByTableNumber(tableNumber);
        verify(mapper).toDomain(tableEntity);
    }

    @Test
    @DisplayName("Debe retornar Optional vacío cuando no se encuentra mesa por número")
    void shouldReturnEmptyOptionalWhenTableNotFoundByNumber() {
        // Given
        String tableNumber = "Z99";
        when(jpaRepository.findByTableNumber(tableNumber)).thenReturn(Optional.empty());

        // When
        Optional<Table> found = adapter.findByTableNumber(tableNumber);

        // Then
        assertFalse(found.isPresent());
        verify(jpaRepository).findByTableNumber(tableNumber);
        verify(mapper, never()).toDomain(any());
    }

    @Test
    @DisplayName("Debe obtener todas las mesas")
    void shouldFindAllTables() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        
        TableEntity entity1 = createTableEntity(1L, "B1", 4, TableStatus.AVAILABLE, now);
        TableEntity entity2 = createTableEntity(2L, "B2", 6, TableStatus.OCCUPIED, now);
        
        Table table1 = new Table(1L, "B1", 4, TableStatus.AVAILABLE, null, now, now);
        Table table2 = new Table(2L, "B2", 6, TableStatus.OCCUPIED, 123L, now, now);
        
        when(jpaRepository.findAll()).thenReturn(Arrays.asList(entity1, entity2));
        when(mapper.toDomain(entity1)).thenReturn(table1);
        when(mapper.toDomain(entity2)).thenReturn(table2);

        // When
        List<Table> tables = adapter.findAll();

        // Then
        assertEquals(2, tables.size());
        verify(jpaRepository).findAll();
        verify(mapper, times(2)).toDomain(any(TableEntity.class));
    }

    @Test
    @DisplayName("Debe obtener todas las mesas por estado")
    void shouldFindTablesByStatus() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        
        TableEntity entity1 = createTableEntity(1L, "C1", 4, TableStatus.AVAILABLE, now);
        TableEntity entity2 = createTableEntity(2L, "C2", 4, TableStatus.AVAILABLE, now);
        
        Table table1 = new Table(1L, "C1", 4, TableStatus.AVAILABLE, null, now, now);
        Table table2 = new Table(2L, "C2", 4, TableStatus.AVAILABLE, null, now, now);
        
        when(jpaRepository.findByStatus(TableStatus.AVAILABLE)).thenReturn(Arrays.asList(entity1, entity2));
        when(mapper.toDomain(entity1)).thenReturn(table1);
        when(mapper.toDomain(entity2)).thenReturn(table2);

        // When
        List<Table> availableTables = adapter.findByStatus(TableStatus.AVAILABLE);

        // Then
        assertEquals(2, availableTables.size());
        assertTrue(availableTables.stream().allMatch(t -> t.getStatus() == TableStatus.AVAILABLE));
        verify(jpaRepository).findByStatus(TableStatus.AVAILABLE);
        verify(mapper, times(2)).toDomain(any(TableEntity.class));
    }

    @Test
    @DisplayName("Debe verificar si existe mesa por número")
    void shouldCheckIfTableExistsByNumber() {
        // Given
        String tableNumber = "D1";
        when(jpaRepository.existsByTableNumber(tableNumber)).thenReturn(true);

        // When
        boolean exists = adapter.existsByTableNumber(tableNumber);

        // Then
        assertTrue(exists);
        verify(jpaRepository).existsByTableNumber(tableNumber);
    }

    @Test
    @DisplayName("Debe retornar false cuando no existe mesa con ese número")
    void shouldReturnFalseWhenTableDoesNotExistByNumber() {
        // Given
        String tableNumber = "Z99";
        when(jpaRepository.existsByTableNumber(tableNumber)).thenReturn(false);

        // When
        boolean exists = adapter.existsByTableNumber(tableNumber);

        // Then
        assertFalse(exists);
        verify(jpaRepository).existsByTableNumber(tableNumber);
    }

    @Test
    @DisplayName("Debe eliminar mesa por ID")
    void shouldDeleteTableById() {
        // Given
        Long tableId = 1L;
        doNothing().when(jpaRepository).deleteById(tableId);

        // When
        adapter.deleteById(tableId);

        // Then
        verify(jpaRepository).deleteById(tableId);
    }

    @Test
    @DisplayName("Debe retornar lista vacía cuando no hay mesas")
    void shouldReturnEmptyListWhenNoTables() {
        // Given
        when(jpaRepository.findAll()).thenReturn(Arrays.asList());

        // When
        List<Table> tables = adapter.findAll();

        // Then
        assertTrue(tables.isEmpty());
        verify(jpaRepository).findAll();
    }

    @Test
    @DisplayName("Debe retornar lista vacía cuando no hay mesas con estado específico")
    void shouldReturnEmptyListWhenNoTablesWithStatus() {
        // Given
        when(jpaRepository.findByStatus(TableStatus.CLEANING)).thenReturn(Arrays.asList());

        // When
        List<Table> cleaningTables = adapter.findByStatus(TableStatus.CLEANING);

        // Then
        assertTrue(cleaningTables.isEmpty());
        verify(jpaRepository).findByStatus(TableStatus.CLEANING);
    }

    // Helper method
    private TableEntity createTableEntity(Long id, String tableNumber, int capacity, TableStatus status, LocalDateTime now) {
        TableEntity entity = new TableEntity();
        entity.setId(id);
        entity.setTableNumber(tableNumber);
        entity.setCapacity(capacity);
        entity.setStatus(status);
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
        return entity;
    }
}
