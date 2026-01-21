package com.foodtech.kitchen.infrastructure.persistence.mappers;

import com.foodtech.kitchen.domain.model.Table;
import com.foodtech.kitchen.domain.model.TableStatus;
import com.foodtech.kitchen.infrastructure.persistence.jpa.entities.TableEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("TableEntityMapper - Tests de conversión Domain ↔ JPA")
class TableEntityMapperTest {

    private TableEntityMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new TableEntityMapper();
    }

    @Test
    @DisplayName("Debe convertir Table de dominio a TableEntity de JPA")
    void shouldConvertDomainTableToJpaEntity() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        Table domainTable = new Table(
            1L,
            "A1",
            4,
            TableStatus.AVAILABLE,
            null,
            now,
            now
        );

        // When
        TableEntity entity = mapper.toEntity(domainTable);

        // Then
        assertNotNull(entity);
        assertEquals(1L, entity.getId());
        assertEquals("A1", entity.getTableNumber());
        assertEquals(4, entity.getCapacity());
        assertEquals(TableStatus.AVAILABLE, entity.getStatus());
        assertNull(entity.getCurrentOrderId());
        assertEquals(now, entity.getCreatedAt());
        assertEquals(now, entity.getUpdatedAt());
    }

    @Test
    @DisplayName("Debe convertir TableEntity de JPA a Table de dominio")
    void shouldConvertJpaEntityToDomainTable() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        TableEntity entity = new TableEntity();
        entity.setId(2L);
        entity.setTableNumber("B2");
        entity.setCapacity(6);
        entity.setStatus(TableStatus.OCCUPIED);
        entity.setCurrentOrderId(123L);
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);

        // When
        Table domainTable = mapper.toDomain(entity);

        // Then
        assertNotNull(domainTable);
        assertEquals(2L, domainTable.getId());
        assertEquals("B2", domainTable.getTableNumber());
        assertEquals(6, domainTable.getCapacity());
        assertEquals(TableStatus.OCCUPIED, domainTable.getStatus());
        assertEquals(123L, domainTable.getCurrentOrderId());
        assertEquals(now, domainTable.getCreatedAt());
        assertEquals(now, domainTable.getUpdatedAt());
    }

    @Test
    @DisplayName("Debe manejar conversión de Table null a TableEntity null")
    void shouldHandleNullDomainTableToEntity() {
        // When
        TableEntity entity = mapper.toEntity(null);

        // Then
        assertNull(entity);
    }

    @Test
    @DisplayName("Debe manejar conversión de TableEntity null a Table null")
    void shouldHandleNullEntityToDomainTable() {
        // When
        Table domainTable = mapper.toDomain(null);

        // Then
        assertNull(domainTable);
    }

    @Test
    @DisplayName("Debe convertir mesa con todos los estados posibles")
    void shouldConvertTableWithAllStatuses() {
        LocalDateTime now = LocalDateTime.now();

        // AVAILABLE
        Table availableTable = new Table(1L, "S1", 4, TableStatus.AVAILABLE, null, now, now);
        TableEntity availableEntity = mapper.toEntity(availableTable);
        assertEquals(TableStatus.AVAILABLE, availableEntity.getStatus());

        // OCCUPIED
        Table occupiedTable = new Table(2L, "S2", 4, TableStatus.OCCUPIED, 456L, now, now);
        TableEntity occupiedEntity = mapper.toEntity(occupiedTable);
        assertEquals(TableStatus.OCCUPIED, occupiedEntity.getStatus());

        // SERVED
        Table servedTable = new Table(3L, "S3", 4, TableStatus.SERVED, 789L, now, now);
        TableEntity servedEntity = mapper.toEntity(servedTable);
        assertEquals(TableStatus.SERVED, servedEntity.getStatus());

        // CLEANING
        Table cleaningTable = new Table(4L, "S4", 4, TableStatus.CLEANING, null, now, now);
        TableEntity cleaningEntity = mapper.toEntity(cleaningTable);
        assertEquals(TableStatus.CLEANING, cleaningEntity.getStatus());
    }

    @Test
    @DisplayName("Debe mantener integridad en conversión bidireccional")
    void shouldMaintainIntegrityInBidirectionalConversion() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        Table original = new Table(5L, "C3", 8, TableStatus.SERVED, 999L, now, now);

        // When
        TableEntity entity = mapper.toEntity(original);
        Table converted = mapper.toDomain(entity);

        // Then
        assertEquals(original.getId(), converted.getId());
        assertEquals(original.getTableNumber(), converted.getTableNumber());
        assertEquals(original.getCapacity(), converted.getCapacity());
        assertEquals(original.getStatus(), converted.getStatus());
        assertEquals(original.getCurrentOrderId(), converted.getCurrentOrderId());
        assertEquals(original.getCreatedAt(), converted.getCreatedAt());
        assertEquals(original.getUpdatedAt(), converted.getUpdatedAt());
    }

    @Test
    @DisplayName("Debe convertir mesa sin ID (nueva mesa)")
    void shouldConvertTableWithoutId() {
        // Given
        Table newTable = new Table("D4", 2);

        // When
        TableEntity entity = mapper.toEntity(newTable);

        // Then
        assertNotNull(entity);
        assertNull(entity.getId());
        assertEquals("D4", entity.getTableNumber());
        assertEquals(2, entity.getCapacity());
        assertEquals(TableStatus.AVAILABLE, entity.getStatus());
        assertNull(entity.getCurrentOrderId());
    }

    @Test
    @DisplayName("Debe convertir mesa con currentOrderId null")
    void shouldConvertTableWithNullCurrentOrderId() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        Table table = new Table(6L, "E5", 4, TableStatus.AVAILABLE, null, now, now);

        // When
        TableEntity entity = mapper.toEntity(table);

        // Then
        assertNull(entity.getCurrentOrderId());
    }

    @Test
    @DisplayName("Debe preservar currentOrderId en conversión")
    void shouldPreserveCurrentOrderIdInConversion() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        TableEntity entity = new TableEntity();
        entity.setId(7L);
        entity.setTableNumber("F6");
        entity.setCapacity(4);
        entity.setStatus(TableStatus.OCCUPIED);
        entity.setCurrentOrderId(555L);
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);

        // When
        Table domainTable = mapper.toDomain(entity);

        // Then
        assertEquals(555L, domainTable.getCurrentOrderId());
    }
}
