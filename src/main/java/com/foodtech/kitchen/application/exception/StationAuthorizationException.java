package com.foodtech.kitchen.application.exception;

import com.foodtech.kitchen.domain.model.Station;

public class StationAuthorizationException extends RuntimeException {

    public StationAuthorizationException(Station station) {
        super("User is not authorized to update tasks at station: " + station);
    }

}
