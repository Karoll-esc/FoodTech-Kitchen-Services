package com.foodtech.kitchen.infrastructure.persistence.jpa.entities;

import com.foodtech.kitchen.domain.model.TableStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("TableEntity JPA - Tests de persistencia")
class TableEntityTest {

    @Test
    @DisplayName("Debe crear una TableEntity con todos los campos")
    void shouldCreateTableEntityWithAllFields() {
        // Given
        TableEntity entity = new TableEntity();
        entity.setTableNumber("A1");
        entity.setCapacity(4);
        entity.setStatus(TableStatus.AVAILABLE);
        entity.setCurrentOrderId(null);
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());

        // Then
        assertEquals("A1", entity.getTableNumber());
        assertEquals(4, entity.getCapacity());
        assertEquals(TableStatus.AVAILABLE, entity.getStatus());
        assertNull(entity.getCurrentOrderId());
        assertNotNull(entity.getCreatedAt());
        assertNotNull(entity.getUpdatedAt());
    }

    @Test
    @DisplayName("Debe establecer status AVAILABLE por defecto en @PrePersist")
    void shouldSetDefaultStatusOnPrePersist() {
        // Given
        TableEntity entity = new TableEntity();
        entity.setTableNumber("B2");
        entity.setCapacity(6);
        // Status no establecido intencionalmente

        // When
        entity.onCreate();

        // Then
        assertEquals(TableStatus.AVAILABLE, entity.getStatus());
    }

    @Test
    @DisplayName("Debe establecer createdAt y updatedAt en @PrePersist")
    void shouldSetTimestampsOnPrePersist() {
        // Given
        TableEntity entity = new TableEntity();
        entity.setTableNumber("C3");
        entity.setCapacity(2);

        LocalDateTime before = LocalDateTime.now();

        // When
        entity.onCreate();

        LocalDateTime after = LocalDateTime.now();

        // Then
        assertNotNull(entity.getCreatedAt());
        assertNotNull(entity.getUpdatedAt());
        assertTrue(entity.getCreatedAt().isAfter(before) || entity.getCreatedAt().isEqual(before));
        assertTrue(entity.getCreatedAt().isBefore(after) || entity.getCreatedAt().isEqual(after));
        assertEquals(entity.getCreatedAt(), entity.getUpdatedAt());
    }

    @Test
    @DisplayName("Debe actualizar updatedAt en @PreUpdate")
    void shouldUpdateTimestampOnPreUpdate() throws InterruptedException {
        // Given
        TableEntity entity = new TableEntity();
        entity.setTableNumber("D4");
        entity.setCapacity(4);
        entity.onCreate();

        LocalDateTime originalUpdatedAt = entity.getUpdatedAt();
        Thread.sleep(10); // Small delay to ensure timestamp difference

        // When
        entity.onUpdate();

        // Then
        assertNotNull(entity.getUpdatedAt());
        assertTrue(entity.getUpdatedAt().isAfter(originalUpdatedAt));
    }

    @Test
    @DisplayName("Debe permitir cambiar el estado de la mesa")
    void shouldAllowStatusChange() {
        // Given
        TableEntity entity = new TableEntity();
        entity.setTableNumber("E5");
        entity.setCapacity(8);
        entity.setStatus(TableStatus.AVAILABLE);

        // When
        entity.setStatus(TableStatus.OCCUPIED);

        // Then
        assertEquals(TableStatus.OCCUPIED, entity.getStatus());
    }

    @Test
    @DisplayName("Debe permitir asignar un pedido a la mesa")
    void shouldAllowOrderAssignment() {
        // Given
        TableEntity entity = new TableEntity();
        entity.setTableNumber("F6");
        entity.setCapacity(4);
        entity.setCurrentOrderId(null);

        // When
        entity.setCurrentOrderId(123L);

        // Then
        assertEquals(123L, entity.getCurrentOrderId());
    }

    @Test
    @DisplayName("Debe permitir limpiar el pedido asociado")
    void shouldAllowClearingOrder() {
        // Given
        TableEntity entity = new TableEntity();
        entity.setTableNumber("G7");
        entity.setCapacity(4);
        entity.setCurrentOrderId(456L);

        // When
        entity.setCurrentOrderId(null);

        // Then
        assertNull(entity.getCurrentOrderId());
    }

    @Test
    @DisplayName("Debe mantener createdAt inmutable después de @PreUpdate")
    void shouldKeepCreatedAtImmutableOnUpdate() {
        // Given
        TableEntity entity = new TableEntity();
        entity.setTableNumber("H8");
        entity.setCapacity(2);
        entity.onCreate();

        LocalDateTime originalCreatedAt = entity.getCreatedAt();

        // When
        entity.onUpdate();

        // Then
        assertEquals(originalCreatedAt, entity.getCreatedAt());
    }

    @Test
    @DisplayName("Debe persistir todos los estados de TableStatus")
    void shouldPersistAllTableStatuses() {
        // AVAILABLE
        TableEntity availableEntity = new TableEntity();
        availableEntity.setTableNumber("S1");
        availableEntity.setCapacity(4);
        availableEntity.setStatus(TableStatus.AVAILABLE);
        assertEquals(TableStatus.AVAILABLE, availableEntity.getStatus());

        // OCCUPIED
        TableEntity occupiedEntity = new TableEntity();
        occupiedEntity.setTableNumber("S2");
        occupiedEntity.setCapacity(4);
        occupiedEntity.setStatus(TableStatus.OCCUPIED);
        assertEquals(TableStatus.OCCUPIED, occupiedEntity.getStatus());

        // SERVED
        TableEntity servedEntity = new TableEntity();
        servedEntity.setTableNumber("S3");
        servedEntity.setCapacity(4);
        servedEntity.setStatus(TableStatus.SERVED);
        assertEquals(TableStatus.SERVED, servedEntity.getStatus());

        // CLEANING
        TableEntity cleaningEntity = new TableEntity();
        cleaningEntity.setTableNumber("S4");
        cleaningEntity.setCapacity(4);
        cleaningEntity.setStatus(TableStatus.CLEANING);
        assertEquals(TableStatus.CLEANING, cleaningEntity.getStatus());
    }

    @Test
    @DisplayName("Debe permitir establecer y obtener el ID")
    void shouldAllowIdSetterAndGetter() {
        // Given
        TableEntity entity = new TableEntity();
        entity.setId(999L);

        // Then
        assertEquals(999L, entity.getId());
    }
}
