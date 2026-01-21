package com.foodtech.kitchen.infrastructure.rest;

import com.foodtech.kitchen.application.ports.in.GetTasksByStationPort;
import com.foodtech.kitchen.application.ports.in.StartTaskPreparationPort;
import com.foodtech.kitchen.application.ports.out.TaskRepository;
import com.foodtech.kitchen.domain.model.Station;
import com.foodtech.kitchen.domain.model.Task;
import com.foodtech.kitchen.domain.model.TaskStatus;
import com.foodtech.kitchen.infrastructure.rest.dto.TaskResponse;
import com.foodtech.kitchen.infrastructure.rest.mapper.TaskMapper;
import com.foodtech.kitchen.infrastructure.security.Permissions;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final GetTasksByStationPort getTasksByStationPort;
    private final StartTaskPreparationPort startTaskPreparationPort;
    private final TaskRepository taskRepository;

    public TaskController(GetTasksByStationPort getTasksByStationPort, 
                         StartTaskPreparationPort startTaskPreparationPort,
                         TaskRepository taskRepository) {
        this.getTasksByStationPort = getTasksByStationPort;
        this.startTaskPreparationPort = startTaskPreparationPort;
        this.taskRepository = taskRepository;
    }

    @GetMapping("/station/{station}")
    @PreAuthorize("hasAuthority('" + Permissions.READ_TASKS + "')")
    public ResponseEntity<List<TaskResponse>> getTasksByStation(
            @PathVariable Station station,
            @RequestParam(required = false) TaskStatus status) {
        List<Task> tasks;
        if (status != null) {
            tasks = taskRepository.findByStationAndStatus(station, status);
        } else {
            tasks = getTasksByStationPort.execute(station);
        }
        List<TaskResponse> response = TaskMapper.toResponseList(tasks);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/start")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<TaskResponse> startTaskPreparation(@PathVariable Long id) {
        // Station-specific authorization will be implemented in GREEN phase
        Task task = startTaskPreparationPort.execute(id);
        TaskResponse response = TaskMapper.toResponse(task);
        return ResponseEntity.ok(response);
    }
}
