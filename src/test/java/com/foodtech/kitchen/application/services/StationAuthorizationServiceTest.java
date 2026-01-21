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
 * - Bar staff (update:tasks:bar) can ONLY update BAR station tasks
 * - Hot kitchen staff (update:tasks:hot-kitchen) can ONLY update HOT_KITCHEN station tasks
 * - Cold kitchen staff (update:tasks:cold-kitchen) can ONLY update COLD_KITCHEN station tasks
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
    @DisplayName("Personal de bar debe estar autorizado para actualizar tareas de BAR")
    void barStaffShouldBeAuthorizedToUpdateBarTasks() {
        // Given: User with bar update permission
        Set<String> permissions = Set.of(Permissions.UPDATE_TASKS_BAR);
        Station station = Station.BAR;

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
        Station station = Station.HOT_KITCHEN;

        // When/Then: Should throw StationAuthorizationException
        assertThrows(StationAuthorizationException.class, () -> 
            authorizationService.validateUserCanUpdateTaskAtStation(permissions, station)
        );
    }

    @Test
    @DisplayName("Personal de cocina caliente debe estar autorizado para actualizar tareas de cocina caliente")
    void hotKitchenStaffShouldBeAuthorizedToUpdateHotKitchenTasks() {
        // Given: User with hot kitchen update permission
        Set<String> permissions = Set.of(Permissions.UPDATE_TASKS_HOT_KITCHEN);
        Station station = Station.HOT_KITCHEN;

        // When/Then: Should NOT throw exception
        assertDoesNotThrow(() -> 
            authorizationService.validateUserCanUpdateTaskAtStation(permissions, station)
        );
    }

    @Test
    @DisplayName("Personal de cocina caliente NO debe estar autorizado para actualizar tareas de bar")
    void hotKitchenStaffShouldNotBeAuthorizedToUpdateBarTasks() {
        // Given: User with hot kitchen update permission trying to update bar task
        Set<String> permissions = Set.of(Permissions.UPDATE_TASKS_HOT_KITCHEN);
        Station station = Station.BAR;

        // When/Then: Should throw StationAuthorizationException
        assertThrows(StationAuthorizationException.class, () -> 
            authorizationService.validateUserCanUpdateTaskAtStation(permissions, station)
        );
    }

    @Test
    @DisplayName("Personal de cocina fría debe estar autorizado para actualizar tareas de cocina fría")
    void coldKitchenStaffShouldBeAuthorizedToUpdateColdKitchenTasks() {
        // Given: User with cold kitchen update permission
        Set<String> permissions = Set.of(Permissions.UPDATE_TASKS_COLD_KITCHEN);
        Station station = Station.COLD_KITCHEN;

        // When/Then: Should NOT throw exception
        assertDoesNotThrow(() -> 
            authorizationService.validateUserCanUpdateTaskAtStation(permissions, station)
        );
    }

    @Test
    @DisplayName("Personal de cocina fría NO debe estar autorizado para actualizar tareas de cocina caliente")
    void coldKitchenStaffShouldNotBeAuthorizedToUpdateHotKitchenTasks() {
        // Given: User with cold kitchen update permission trying to update hot kitchen task
        Set<String> permissions = Set.of(Permissions.UPDATE_TASKS_COLD_KITCHEN);
        Station station = Station.HOT_KITCHEN;

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
            authorizationService.validateUserCanUpdateTaskAtStation(permissions, Station.BAR)
        );
        assertDoesNotThrow(() -> 
            authorizationService.validateUserCanUpdateTaskAtStation(permissions, Station.HOT_KITCHEN)
        );
        assertDoesNotThrow(() -> 
            authorizationService.validateUserCanUpdateTaskAtStation(permissions, Station.COLD_KITCHEN)
        );
    }

    @Test
    @DisplayName("Usuario sin permisos de actualización NO debe estar autorizado para actualizar tareas")
    void unauthorizedUserShouldNotBeAuthorizedToUpdateTasks() {
        // Given: User with only read permissions (no update permissions)
        Set<String> permissions = Set.of(Permissions.READ_TASKS, Permissions.READ_ORDERS);

        // When/Then: Should throw StationAuthorizationException for any station
        assertThrows(StationAuthorizationException.class, () -> 
            authorizationService.validateUserCanUpdateTaskAtStation(permissions, Station.BAR)
        );
        assertThrows(StationAuthorizationException.class, () -> 
            authorizationService.validateUserCanUpdateTaskAtStation(permissions, Station.HOT_KITCHEN)
        );
        assertThrows(StationAuthorizationException.class, () -> 
            authorizationService.validateUserCanUpdateTaskAtStation(permissions, Station.COLD_KITCHEN)
        );
    }
}
