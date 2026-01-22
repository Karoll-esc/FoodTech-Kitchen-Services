package com.foodtech.kitchen.application.usecases;

import com.foodtech.kitchen.application.ports.out.TaskRepository;
import com.foodtech.kitchen.domain.model.Station;
import com.foodtech.kitchen.domain.model.Product;
import com.foodtech.kitchen.domain.model.ProductType;
import com.foodtech.kitchen.domain.model.Task;
import com.foodtech.kitchen.domain.model.TaskStatus;
import com.foodtech.kitchen.domain.services.CommandFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StartTaskPreparationUseCaseTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private CommandFactory commandFactory;

    @Mock
    private com.foodtech.kitchen.application.ports.out.CommandExecutor commandExecutor;

    private StartTaskPreparationUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new StartTaskPreparationUseCase(taskRepository, commandFactory, commandExecutor);
    }

    @Test
    void shouldStartTaskPreparationWhenTaskExists() {
        // Given
        Long taskId = 1L;
        Product product = new Product("Cerveza", ProductType.DRINK);
        Task pendingTask = Task.reconstruct(
                taskId,
                1L,
                Station.ESPRESSO_BAR,
                "A1",
                List.of(product),
                LocalDateTime.now(),
                TaskStatus.PENDING,
                null,
                null
        );

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(pendingTask));
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Task result = useCase.execute(taskId);

        // Then
        assertNotNull(result);
        assertEquals(TaskStatus.IN_PREPARATION, result.getStatus());
        assertNotNull(result.getStartedAt());
        verify(taskRepository).findById(taskId);
        verify(taskRepository).save(any(Task.class));
    }
}
