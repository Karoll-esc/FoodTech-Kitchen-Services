package com.foodtech.kitchen.application.usecases;

import com.foodtech.kitchen.application.exception.TaskNotFoundException;
import com.foodtech.kitchen.application.ports.in.StartTaskPreparationPort;
import com.foodtech.kitchen.application.ports.out.CommandExecutor;
import com.foodtech.kitchen.application.ports.out.TaskRepository;
import com.foodtech.kitchen.domain.commands.Command;
import com.foodtech.kitchen.domain.model.Task;
import com.foodtech.kitchen.domain.services.CommandFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
public class StartTaskPreparationUseCase implements StartTaskPreparationPort {

    private final TaskRepository taskRepository;
    private final CommandFactory commandFactory;
    private final CommandExecutor commandExecutor;

    public StartTaskPreparationUseCase(
            TaskRepository taskRepository,
            CommandFactory commandFactory,
            CommandExecutor commandExecutor
    ) {
        this.taskRepository = taskRepository;
        this.commandFactory = commandFactory;
        this.commandExecutor = commandExecutor;
    }

    @Override
    public Task execute(Long taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException(taskId));

        task.start();
        Task savedTask = taskRepository.save(task);

        // Ejecutar comando asíncronamente
        Mono.fromRunnable(() -> {
                    Command command = commandFactory.createCommand(
                            savedTask.getStation(),
                            savedTask.getProducts()
                    );
                    commandExecutor.execute(command);
                })
                .subscribeOn(Schedulers.boundedElastic())
                .doOnSuccess(unused -> {
                    Task completedTask = taskRepository.findById(taskId)
                            .orElseThrow(() -> new TaskNotFoundException(taskId));
                    completedTask.complete();
                    taskRepository.save(completedTask);
                    System.out.println("✅ [REACTOR] Task " + taskId + " completed");
                })
                .doOnError(error -> {
                    System.err.println("❌ [REACTOR] Error in task " + taskId);
                    error.printStackTrace();
                })
                .subscribe();

        return savedTask;
    }
}