package com.foodtech.kitchen.infrastructure.persistence.jpa;

import com.foodtech.kitchen.domain.model.TableStatus;
import com.foodtech.kitchen.infrastructure.persistence.jpa.entities.TableEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@DisplayName("TableJpaRepository - Tests de integración con base de datos")
class TableJpaRepositoryTest {

    @Autowired
    private TableJpaRepository tableJpaRepository;

    @Autowired
    private TestEntityManager entityManager;

    @BeforeEach
    void setUp() {
        tableJpaRepository.deleteAll();
    }

    @Test
    @DisplayName("Debe guardar una mesa en la base de datos")
    void shouldSaveTableToDatabase() {
        // Given
        TableEntity table = new TableEntity();
        table.setTableNumber("A1");
        table.setCapacity(4);
        table.setStatus(TableStatus.AVAILABLE);
        table.setCreatedAt(LocalDateTime.now());
        table.setUpdatedAt(LocalDateTime.now());

        // When
        TableEntity savedTable = tableJpaRepository.save(table);

        // Then
        assertNotNull(savedTable.getId());
        assertEquals("A1", savedTable.getTableNumber());
        assertEquals(4, savedTable.getCapacity());
        assertEquals(TableStatus.AVAILABLE, savedTable.getStatus());
    }

    @Test
    @DisplayName("Debe buscar mesa por su número")
    void shouldFindTableByTableNumber() {
        // Given
        TableEntity table = new TableEntity();
        table.setTableNumber("B2");
        table.setCapacity(6);
        table.setStatus(TableStatus.AVAILABLE);
        table.setCreatedAt(LocalDateTime.now());
        table.setUpdatedAt(LocalDateTime.now());
        entityManager.persist(table);
        entityManager.flush();

        // When
        Optional<TableEntity> found = tableJpaRepository.findByTableNumber("B2");

        // Then
        assertTrue(found.isPresent());
        assertEquals("B2", found.get().getTableNumber());
        assertEquals(6, found.get().getCapacity());
    }

    @Test
    @DisplayName("Debe retornar Optional vacío cuando no existe mesa con ese número")
    void shouldReturnEmptyOptionalWhenTableNumberNotFound() {
        // When
        Optional<TableEntity> found = tableJpaRepository.findByTableNumber("NONEXISTENT");

        // Then
        assertFalse(found.isPresent());
    }

    @Test
    @DisplayName("Debe verificar si existe mesa por número")
    void shouldCheckIfTableExistsByNumber() {
        // Given
        TableEntity table = new TableEntity();
        table.setTableNumber("C3");
        table.setCapacity(2);
        table.setStatus(TableStatus.AVAILABLE);
        table.setCreatedAt(LocalDateTime.now());
        table.setUpdatedAt(LocalDateTime.now());
        entityManager.persist(table);
        entityManager.flush();

        // When
        boolean exists = tableJpaRepository.existsByTableNumber("C3");
        boolean notExists = tableJpaRepository.existsByTableNumber("Z99");

        // Then
        assertTrue(exists);
        assertFalse(notExists);
    }

    @Test
    @DisplayName("Debe buscar todas las mesas por estado")
    void shouldFindAllTablesByStatus() {
        // Given
        TableEntity table1 = createTable("D1", 4, TableStatus.AVAILABLE);
        TableEntity table2 = createTable("D2", 4, TableStatus.AVAILABLE);
        TableEntity table3 = createTable("D3", 4, TableStatus.OCCUPIED);
        
        entityManager.persist(table1);
        entityManager.persist(table2);
        entityManager.persist(table3);
        entityManager.flush();

        // When
        List<TableEntity> availableTables = tableJpaRepository.findByStatus(TableStatus.AVAILABLE);
        List<TableEntity> occupiedTables = tableJpaRepository.findByStatus(TableStatus.OCCUPIED);

        // Then
        assertEquals(2, availableTables.size());
        assertEquals(1, occupiedTables.size());
        assertTrue(availableTables.stream().allMatch(t -> t.getStatus() == TableStatus.AVAILABLE));
        assertTrue(occupiedTables.stream().allMatch(t -> t.getStatus() == TableStatus.OCCUPIED));
    }

    @Test
    @DisplayName("Debe retornar lista vacía cuando no hay mesas con ese estado")
    void shouldReturnEmptyListWhenNoTablesWithStatus() {
        // Given
        TableEntity table = createTable("E1", 4, TableStatus.AVAILABLE);
        entityManager.persist(table);
        entityManager.flush();

        // When
        List<TableEntity> cleaningTables = tableJpaRepository.findByStatus(TableStatus.CLEANING);

        // Then
        assertTrue(cleaningTables.isEmpty());
    }

    @Test
    @DisplayName("Debe respetar restricción UNIQUE en table_number")
    void shouldEnforceUniqueConstraintOnTableNumber() {
        // Given
        TableEntity table1 = createTable("F1", 4, TableStatus.AVAILABLE);
        entityManager.persist(table1);
        entityManager.flush();

        TableEntity table2 = createTable("F1", 6, TableStatus.AVAILABLE);

        // When & Then
        assertThrows(Exception.class, () -> {
            entityManager.persist(table2);
            entityManager.flush();
        });
    }

    @Test
    @DisplayName("Debe buscar mesa por ID")
    void shouldFindTableById() {
        // Given
        TableEntity table = createTable("G1", 4, TableStatus.AVAILABLE);
        entityManager.persist(table);
        entityManager.flush();

        Long tableId = table.getId();

        // When
        Optional<TableEntity> found = tableJpaRepository.findById(tableId);

        // Then
        assertTrue(found.isPresent());
        assertEquals(tableId, found.get().getId());
        assertEquals("G1", found.get().getTableNumber());
    }

    @Test
    @DisplayName("Debe obtener todas las mesas")
    void shouldFindAllTables() {
        // Given
        entityManager.persist(createTable("H1", 4, TableStatus.AVAILABLE));
        entityManager.persist(createTable("H2", 6, TableStatus.OCCUPIED));
        entityManager.persist(createTable("H3", 2, TableStatus.SERVED));
        entityManager.flush();

        // When
        List<TableEntity> allTables = tableJpaRepository.findAll();

        // Then
        assertEquals(3, allTables.size());
    }

    @Test
    @DisplayName("Debe eliminar mesa por ID")
    void shouldDeleteTableById() {
        // Given
        TableEntity table = createTable("I1", 4, TableStatus.AVAILABLE);
        entityManager.persist(table);
        entityManager.flush();

        Long tableId = table.getId();

        // When
        tableJpaRepository.deleteById(tableId);

        // Then
        Optional<TableEntity> found = tableJpaRepository.findById(tableId);
        assertFalse(found.isPresent());
    }

    @Test
    @DisplayName("Debe actualizar mesa existente")
    void shouldUpdateExistingTable() {
        // Given
        TableEntity table = createTable("J1", 4, TableStatus.AVAILABLE);
        entityManager.persist(table);
        entityManager.flush();

        // When
        table.setStatus(TableStatus.OCCUPIED);
        table.setCurrentOrderId(123L);
        TableEntity updated = tableJpaRepository.save(table);

        // Then
        assertEquals(TableStatus.OCCUPIED, updated.getStatus());
        assertEquals(123L, updated.getCurrentOrderId());
    }

    @Test
    @DisplayName("Debe persistir currentOrderId como null")
    void shouldPersistNullCurrentOrderId() {
        // Given
        TableEntity table = createTable("K1", 4, TableStatus.AVAILABLE);
        table.setCurrentOrderId(null);

        // When
        entityManager.persist(table);
        entityManager.flush();

        // Then
        Optional<TableEntity> found = tableJpaRepository.findByTableNumber("K1");
        assertTrue(found.isPresent());
        assertNull(found.get().getCurrentOrderId());
    }

    // Helper method
    private TableEntity createTable(String tableNumber, int capacity, TableStatus status) {
        TableEntity table = new TableEntity();
        table.setTableNumber(tableNumber);
        table.setCapacity(capacity);
        table.setStatus(status);
        table.setCreatedAt(LocalDateTime.now());
        table.setUpdatedAt(LocalDateTime.now());
        return table;
    }
}
