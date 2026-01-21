package com.foodtech.kitchen.infrastructure.rest;

import com.foodtech.kitchen.application.exception.TaskNotFoundException;
import com.foodtech.kitchen.application.ports.in.GetTasksByStationPort;
import com.foodtech.kitchen.application.ports.in.StartTaskPreparationPort;
import com.foodtech.kitchen.application.ports.out.TaskRepository;
import com.foodtech.kitchen.application.services.StationAuthorizationService;
import com.foodtech.kitchen.domain.model.Station;
import com.foodtech.kitchen.domain.model.Task;
import com.foodtech.kitchen.domain.model.TaskStatus;
import com.foodtech.kitchen.infrastructure.rest.dto.TaskResponse;
import com.foodtech.kitchen.infrastructure.rest.mapper.TaskMapper;
import com.foodtech.kitchen.infrastructure.security.Permissions;
import com.foodtech.kitchen.infrastructure.security.SecurityContextHelper;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final GetTasksByStationPort getTasksByStationPort;
    private final StartTaskPreparationPort startTaskPreparationPort;
    private final TaskRepository taskRepository;
    private final StationAuthorizationService authorizationService;
    private final SecurityContextHelper securityContextHelper;

    public TaskController(GetTasksByStationPort getTasksByStationPort, 
                         StartTaskPreparationPort startTaskPreparationPort,
                         TaskRepository taskRepository,
                         StationAuthorizationService authorizationService,
                         SecurityContextHelper securityContextHelper) {
        this.getTasksByStationPort = getTasksByStationPort;
        this.startTaskPreparationPort = startTaskPreparationPort;
        this.taskRepository = taskRepository;
        this.authorizationService = authorizationService;
        this.securityContextHelper = securityContextHelper;
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
        // Get task to determine its station
        Task task = taskRepository.findById(id)
            .orElseThrow(() -> new TaskNotFoundException(id));
        
        // Validate user has permission for this station
        Set<String> userPermissions = securityContextHelper.getCurrentUserPermissions();
        authorizationService.validateUserCanUpdateTaskAtStation(userPermissions, task.getStation());
        
        // Proceed with task preparation
        Task updatedTask = startTaskPreparationPort.execute(id);
        TaskResponse response = TaskMapper.toResponse(updatedTask);
        return ResponseEntity.ok(response);
    }
}
