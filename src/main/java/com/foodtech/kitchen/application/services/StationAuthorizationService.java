package com.foodtech.kitchen.application.services;

import com.foodtech.kitchen.application.exception.StationAuthorizationException;
import com.foodtech.kitchen.domain.model.Station;
import com.foodtech.kitchen.infrastructure.security.Permissions;

import java.util.Set;

/**
 * Service responsible for validating station-based authorization.
 * 
 * Ensures that kitchen staff can only update tasks for their assigned station,
 * while administrators have access to all stations.
 * 
 * Business Rules:
 * - Bar staff (update:tasks:bar) can ONLY update ESPRESSO_BAR station tasks
 * - Hot kitchen staff (update:tasks:hot-kitchen) can ONLY update PASTRY_STATION station tasks
 * - Cold kitchen staff (update:tasks:cold-kitchen) can ONLY update SANDWICH_STATION station tasks
 * - Admin (admin:all) can update ANY station tasks
 * 
 * Architecture Notes:
 * - Application layer service (no Spring annotations)
 * - Pure business logic for authorization validation
 * - Throws StationAuthorizationException when access is denied (results in 403 Forbidden)
 * - Used by infrastructure layer controllers to enforce authorization
 * 
 * Usage Example:
 * {@code
 * Set<String> userPermissions = securityHelper.getCurrentUserPermissions();
 * authService.validateUserCanUpdateTaskAtStation(userPermissions, Station.ESPRESSO_BAR);
 * // If execution reaches here, user is authorized
 * }
 */
public class StationAuthorizationService {

    /**
     * Validates that the user has permission to update tasks at the specified station.
     * 
     * @param permissions The set of permissions from the user's JWT token (cannot be null)
     * @param station The station where the task is located (cannot be null)
     * @throws StationAuthorizationException if the user is not authorized for this station
     * @throws IllegalArgumentException if permissions or station is null
     */
    public void validateUserCanUpdateTaskAtStation(Set<String> permissions, Station station) 
            throws StationAuthorizationException {
        
        if (permissions == null) {
            throw new IllegalArgumentException("Permissions cannot be null");
        }
        
        if (station == null) {
            throw new IllegalArgumentException("Station cannot be null");
        }
        
        // Admin has access to all stations
        if (permissions.contains(Permissions.ADMIN_ALL)) {
            return;
        }
        
        // Check if user has the specific permission for this station
        String requiredPermission = getRequiredPermissionForStation(station);
        
        if (!permissions.contains(requiredPermission)) {
            throw new StationAuthorizationException(station, permissions);
        }
    }

    /**
     * Maps a station to its corresponding update permission.
     * 
     * @param station The station
     * @return The required permission string for updating tasks at this station
     */
    private String getRequiredPermissionForStation(Station station) {
        return switch (station) {
            case ESPRESSO_BAR -> Permissions.UPDATE_TASKS_BAR;
            case PASTRY_STATION -> Permissions.UPDATE_TASKS_PASTRY_STATION;
            case SANDWICH_STATION -> Permissions.UPDATE_TASKS_SANDWICH_STATION;
        };
    }
}
