package com.foodtech.kitchen.infrastructure.persistence.jpa;

import com.foodtech.kitchen.domain.model.Station;
import com.foodtech.kitchen.infrastructure.persistence.jpa.entities.TaskEntity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class TaskJpaRepositoryTest {

    @Autowired
    private TaskJpaRepository repository;

    @Test
    @DisplayName("Should save and find task")
    void shouldSaveAndFindTask() {
        // Given
        com.foodtech.kitchen.infrastructure.persistence.jpa.entities.TaskProductEntity p =
            com.foodtech.kitchen.infrastructure.persistence.jpa.entities.TaskProductEntity.builder()
                .name("Coca Cola").type(com.foodtech.kitchen.domain.model.ProductType.DRINK).build();

        TaskEntity task = TaskEntity.builder()
            .orderId(1L)
            .station(Station.BAR)
            .tableNumber("A1")
            .products(List.of(p))
            .build();

        // When
        TaskEntity saved = repository.save(task);
        TaskEntity found = repository.findById(saved.getId()).orElse(null);

        // Then
        assertNotNull(found);
        assertEquals(Station.BAR, found.getStation());
        assertEquals("A1", found.getTableNumber());
    }

    @Test
    @DisplayName("Should find tasks by station")
    void shouldFindTasksByStation() {
        // Given
        TaskEntity barTask = TaskEntity.builder()
            .orderId(1L)
            .station(Station.BAR)
            .tableNumber("A1")
            .products(List.of())
            .build();
        
        TaskEntity kitchenTask = TaskEntity.builder()
            .orderId(1L)
            .station(Station.HOT_KITCHEN)
            .tableNumber("B2")
            .products(List.of())
            .build();

        repository.save(barTask);
        repository.save(kitchenTask);

        // When
        List<TaskEntity> barTasks = repository.findByStation(Station.BAR);

        // Then
        assertEquals(1, barTasks.size());
        assertEquals(Station.BAR, barTasks.get(0).getStation());
    }
}