package com.foodtech.kitchen.application.exception;

import com.foodtech.kitchen.domain.model.Station;

import java.util.Set;

/**
 * Exception thrown when a user attempts to update a task at a station
 * for which they don't have the required permission.
 * 
 * This exception results in a 403 Forbidden HTTP response when thrown
 * from a controller method.
 */
public class StationAuthorizationException extends RuntimeException {

    private final Station station;
    private final Set<String> userPermissions;

    public StationAuthorizationException(Station station) {
        super(String.format(
            "User is not authorized to update tasks at station: %s. " +
            "Required permission: update:tasks:%s or admin:all",
            station,
            station.name().toLowerCase().replace("_", "-")
        ));
        this.station = station;
        this.userPermissions = Set.of();
    }

    public StationAuthorizationException(Station station, Set<String> userPermissions) {
        super(String.format(
            "User is not authorized to update tasks at station: %s. " +
            "User permissions: %s. Required permission: update:tasks:%s or admin:all",
            station,
            userPermissions.isEmpty() ? "none" : String.join(", ", userPermissions),
            station.name().toLowerCase().replace("_", "-")
        ));
        this.station = station;
        this.userPermissions = userPermissions;
    }

    public Station getStation() {
        return station;
    }

    public Set<String> getUserPermissions() {
        return userPermissions;
    }

}
