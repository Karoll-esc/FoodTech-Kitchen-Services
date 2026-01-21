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
 * - Bar staff (update:tasks:bar) can ONLY update BAR station tasks
 * - Hot kitchen staff (update:tasks:hot-kitchen) can ONLY update HOT_KITCHEN station tasks
 * - Cold kitchen staff (update:tasks:cold-kitchen) can ONLY update COLD_KITCHEN station tasks
 * - Admin (admin:all) can update ANY station tasks
 */
public class StationAuthorizationService {

    /**
     * Validates that the user has permission to update tasks at the specified station.
     * 
     * @param permissions The set of permissions from the user's JWT token
     * @param station The station where the task is located
     * @throws StationAuthorizationException if the user is not authorized for this station
     */
    public void validateUserCanUpdateTaskAtStation(Set<String> permissions, Station station) 
            throws StationAuthorizationException {
        
        // Admin has access to all stations
        if (permissions.contains(Permissions.ADMIN_ALL)) {
            return;
        }
        
        // Check if user has the specific permission for this station
        String requiredPermission = getRequiredPermissionForStation(station);
        
        if (!permissions.contains(requiredPermission)) {
            throw new StationAuthorizationException(station);
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
            case BAR -> Permissions.UPDATE_TASKS_BAR;
            case HOT_KITCHEN -> Permissions.UPDATE_TASKS_HOT_KITCHEN;
            case COLD_KITCHEN -> Permissions.UPDATE_TASKS_COLD_KITCHEN;
        };
    }
}
