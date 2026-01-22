package com.foodtech.kitchen.application.services;

import com.foodtech.kitchen.application.exception.StationAuthorizationException;
import com.foodtech.kitchen.domain.model.Station;
import com.foodtech.kitchen.infrastructure.security.Permissions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for station-based authorization logic.
 * 
 * Validates that the StationAuthorizationService correctly enforces business rules:
 * - Bar staff (update:tasks:bar) can ONLY update ESPRESSO_BAR station tasks
 * - Hot kitchen staff (update:tasks:hot-kitchen) can ONLY update PASTRY_STATION station tasks
 * - Cold kitchen staff (update:tasks:cold-kitchen) can ONLY update SANDWICH_STATION station tasks
 * - Admin (admin:all) can update ANY station tasks
 * - Users without appropriate permissions should be denied access
 * 
 * Test Strategy:
 * - Tests focus on authorization logic in isolation (unit tests)
 * - Each test validates a specific business rule
 * - Uses assertDoesNotThrow for authorized scenarios
 * - Uses assertThrows for unauthorized scenarios
 */
@DisplayName("StationAuthorizationService - Station-based authorization")
class StationAuthorizationServiceTest {

    private StationAuthorizationService authorizationService;

    @BeforeEach
    void setUp() {
        authorizationService = new StationAuthorizationService();
    }

    @Test
    @DisplayName("Personal de bar debe estar autorizado para actualizar tareas de ESPRESSO_BAR")
    void barStaffShouldBeAuthorizedToUpdateBarTasks() {
        // Given: User with bar update permission
        Set<String> permissions = Set.of(Permissions.UPDATE_TASKS_BAR);
        Station station = Station.ESPRESSO_BAR;

        // When/Then: Should NOT throw exception
        assertDoesNotThrow(() -> 
            authorizationService.validateUserCanUpdateTaskAtStation(permissions, station)
        );
    }

    @Test
    @DisplayName("Personal de bar NO debe estar autorizado para actualizar tareas de cocina caliente")
    void barStaffShouldNotBeAuthorizedToUpdateHotKitchenTasks() {
        // Given: User with bar update permission trying to update hot kitchen task
        Set<String> permissions = Set.of(Permissions.UPDATE_TASKS_BAR);
        Station station = Station.PASTRY_STATION;

        // When/Then: Should throw StationAuthorizationException
        assertThrows(StationAuthorizationException.class, () -> 
            authorizationService.validateUserCanUpdateTaskAtStation(permissions, station)
        );
    }

    @Test
    @DisplayName("Personal de cocina caliente debe estar autorizado para actualizar tareas de cocina caliente")
    void hotKitchenStaffShouldBeAuthorizedToUpdateHotKitchenTasks() {
        // Given: User with hot kitchen update permission
        Set<String> permissions = Set.of(Permissions.UPDATE_TASKS_PASTRY_STATION);
        Station station = Station.PASTRY_STATION;

        // When/Then: Should NOT throw exception
        assertDoesNotThrow(() -> 
            authorizationService.validateUserCanUpdateTaskAtStation(permissions, station)
        );
    }

    @Test
    @DisplayName("Personal de cocina caliente NO debe estar autorizado para actualizar tareas de bar")
    void hotKitchenStaffShouldNotBeAuthorizedToUpdateBarTasks() {
        // Given: User with hot kitchen update permission trying to update bar task
        Set<String> permissions = Set.of(Permissions.UPDATE_TASKS_PASTRY_STATION);
        Station station = Station.ESPRESSO_BAR;

        // When/Then: Should throw StationAuthorizationException
        assertThrows(StationAuthorizationException.class, () -> 
            authorizationService.validateUserCanUpdateTaskAtStation(permissions, station)
        );
    }

    @Test
    @DisplayName("Personal de cocina fría debe estar autorizado para actualizar tareas de cocina fría")
    void coldKitchenStaffShouldBeAuthorizedToUpdateColdKitchenTasks() {
        // Given: User with cold kitchen update permission
        Set<String> permissions = Set.of(Permissions.UPDATE_TASKS_SANDWICH_STATION);
        Station station = Station.SANDWICH_STATION;

        // When/Then: Should NOT throw exception
        assertDoesNotThrow(() -> 
            authorizationService.validateUserCanUpdateTaskAtStation(permissions, station)
        );
    }

    @Test
    @DisplayName("Personal de cocina fría NO debe estar autorizado para actualizar tareas de cocina caliente")
    void coldKitchenStaffShouldNotBeAuthorizedToUpdateHotKitchenTasks() {
        // Given: User with cold kitchen update permission trying to update hot kitchen task
        Set<String> permissions = Set.of(Permissions.UPDATE_TASKS_SANDWICH_STATION);
        Station station = Station.PASTRY_STATION;

        // When/Then: Should throw StationAuthorizationException
        assertThrows(StationAuthorizationException.class, () -> 
            authorizationService.validateUserCanUpdateTaskAtStation(permissions, station)
        );
    }

    @Test
    @DisplayName("Administrador debe estar autorizado para actualizar tareas de cualquier estación")
    void adminShouldBeAuthorizedToUpdateAnyStationTasks() {
        // Given: User with admin permission
        Set<String> permissions = Set.of(Permissions.ADMIN_ALL);

        // When/Then: Should NOT throw exception for any station
        assertDoesNotThrow(() -> 
            authorizationService.validateUserCanUpdateTaskAtStation(permissions, Station.ESPRESSO_BAR)
        );
        assertDoesNotThrow(() -> 
            authorizationService.validateUserCanUpdateTaskAtStation(permissions, Station.PASTRY_STATION)
        );
        assertDoesNotThrow(() -> 
            authorizationService.validateUserCanUpdateTaskAtStation(permissions, Station.SANDWICH_STATION)
        );
    }

    @Test
    @DisplayName("Usuario sin permisos de actualización NO debe estar autorizado para actualizar tareas")
    void unauthorizedUserShouldNotBeAuthorizedToUpdateTasks() {
        // Given: User with only read permissions (no update permissions)
        Set<String> permissions = Set.of(Permissions.READ_TASKS, Permissions.READ_ORDERS);

        // When/Then: Should throw StationAuthorizationException for any station
        assertThrows(StationAuthorizationException.class, () -> 
            authorizationService.validateUserCanUpdateTaskAtStation(permissions, Station.ESPRESSO_BAR)
        );
        assertThrows(StationAuthorizationException.class, () -> 
            authorizationService.validateUserCanUpdateTaskAtStation(permissions, Station.PASTRY_STATION)
        );
        assertThrows(StationAuthorizationException.class, () -> 
            authorizationService.validateUserCanUpdateTaskAtStation(permissions, Station.SANDWICH_STATION)
        );
    }
}
