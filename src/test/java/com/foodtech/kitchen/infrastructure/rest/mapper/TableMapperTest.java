package com.foodtech.kitchen.infrastructure.rest.mapper;

import com.foodtech.kitchen.domain.model.Table;
import com.foodtech.kitchen.domain.model.TableStatus;
import com.foodtech.kitchen.infrastructure.rest.dto.TableResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("TableMapper - Tests de conversión Domain → DTO")
class TableMapperTest {

    @Test
    @DisplayName("Debe convertir Table de dominio a TableResponse DTO")
    void shouldConvertDomainTableToResponseDTO() {
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
        TableResponse response = TableMapper.toResponse(domainTable);

        // Then
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("A1", response.getTableNumber());
        assertEquals(4, response.getCapacity());
        assertEquals(TableStatus.AVAILABLE, response.getStatus());
        assertNull(response.getCurrentOrderId());
        assertEquals(now, response.getCreatedAt());
        assertEquals(now, response.getUpdatedAt());
    }

    @Test
    @DisplayName("Debe manejar conversión de Table null a TableResponse null")
    void shouldHandleNullTableConversion() {
        // When
        TableResponse response = TableMapper.toResponse(null);

        // Then
        assertNull(response);
    }

    @Test
    @DisplayName("Debe convertir mesa con todos los campos poblados")
    void shouldConvertTableWithAllFieldsPopulated() {
        // Given
        LocalDateTime createdAt = LocalDateTime.of(2026, 1, 20, 10, 0);
        LocalDateTime updatedAt = LocalDateTime.of(2026, 1, 21, 14, 30);
        
        Table table = new Table(
            5L,
            "B2",
            6,
            TableStatus.OCCUPIED,
            123L,
            createdAt,
            updatedAt
        );

        // When
        TableResponse response = TableMapper.toResponse(table);

        // Then
        assertEquals(5L, response.getId());
        assertEquals("B2", response.getTableNumber());
        assertEquals(6, response.getCapacity());
        assertEquals(TableStatus.OCCUPIED, response.getStatus());
        assertEquals(123L, response.getCurrentOrderId());
        assertEquals(createdAt, response.getCreatedAt());
        assertEquals(updatedAt, response.getUpdatedAt());
    }

    @Test
    @DisplayName("Debe convertir mesa con currentOrderId null")
    void shouldConvertTableWithNullCurrentOrderId() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        Table table = new Table(2L, "C3", 4, TableStatus.AVAILABLE, null, now, now);

        // When
        TableResponse response = TableMapper.toResponse(table);

        // Then
        assertNull(response.getCurrentOrderId());
    }

    @Test
    @DisplayName("Debe convertir mesa con todos los estados posibles")
    void shouldConvertTableWithAllStatuses() {
        LocalDateTime now = LocalDateTime.now();

        // AVAILABLE
        Table availableTable = new Table(1L, "S1", 4, TableStatus.AVAILABLE, null, now, now);
        TableResponse availableResponse = TableMapper.toResponse(availableTable);
        assertEquals(TableStatus.AVAILABLE, availableResponse.getStatus());

        // OCCUPIED
        Table occupiedTable = new Table(2L, "S2", 4, TableStatus.OCCUPIED, 456L, now, now);
        TableResponse occupiedResponse = TableMapper.toResponse(occupiedTable);
        assertEquals(TableStatus.OCCUPIED, occupiedResponse.getStatus());

        // SERVED
        Table servedTable = new Table(3L, "S3", 4, TableStatus.SERVED, 789L, now, now);
        TableResponse servedResponse = TableMapper.toResponse(servedTable);
        assertEquals(TableStatus.SERVED, servedResponse.getStatus());

        // CLEANING
        Table cleaningTable = new Table(4L, "S4", 4, TableStatus.CLEANING, null, now, now);
        TableResponse cleaningResponse = TableMapper.toResponse(cleaningTable);
        assertEquals(TableStatus.CLEANING, cleaningResponse.getStatus());
    }

    @Test
    @DisplayName("Debe mantener precisión de timestamps en conversión")
    void shouldMaintainTimestampPrecisionInConversion() {
        // Given
        LocalDateTime createdAt = LocalDateTime.of(2026, 1, 21, 12, 34, 56, 123456789);
        LocalDateTime updatedAt = LocalDateTime.of(2026, 1, 21, 15, 45, 12, 987654321);
        
        Table table = new Table(10L, "D4", 2, TableStatus.AVAILABLE, null, createdAt, updatedAt);

        // When
        TableResponse response = TableMapper.toResponse(table);

        // Then
        assertEquals(createdAt, response.getCreatedAt());
        assertEquals(updatedAt, response.getUpdatedAt());
        assertEquals(createdAt.getNano(), response.getCreatedAt().getNano());
        assertEquals(updatedAt.getNano(), response.getUpdatedAt().getNano());
    }

    @Test
    @DisplayName("Debe convertir mesa recién creada sin ID")
    void shouldConvertNewlyCreatedTableWithoutId() {
        // Given
        Table newTable = new Table("E5", 4);

        // When
        TableResponse response = TableMapper.toResponse(newTable);

        // Then
        assertNotNull(response);
        assertNull(response.getId());
        assertEquals("E5", response.getTableNumber());
        assertEquals(4, response.getCapacity());
        assertEquals(TableStatus.AVAILABLE, response.getStatus());
    }

    @Test
    @DisplayName("Debe preservar números de mesa alfanuméricos")
    void shouldPreserveAlphanumericTableNumbers() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        Table table1 = new Table(1L, "A1", 4, TableStatus.AVAILABLE, null, now, now);
        Table table2 = new Table(2L, "VIP-01", 8, TableStatus.AVAILABLE, null, now, now);
        Table table3 = new Table(3L, "T123", 2, TableStatus.AVAILABLE, null, now, now);

        // When
        TableResponse response1 = TableMapper.toResponse(table1);
        TableResponse response2 = TableMapper.toResponse(table2);
        TableResponse response3 = TableMapper.toResponse(table3);

        // Then
        assertEquals("A1", response1.getTableNumber());
        assertEquals("VIP-01", response2.getTableNumber());
        assertEquals("T123", response3.getTableNumber());
    }
}
