package com.foodtech.kitchen.domain.model;

//HUMAN REVIEW: Agregué campo station a ProductType para eliminar violación OCP.
//Ahora cada ProductType conoce su Station, eliminando necesidad de ProductStationMapper.
//Cumple OCP: agregar nuevo tipo no requiere modificar otras clases.
public enum ProductType {
    DRINK(Station.ESPRESSO_BAR),
    PASTRY(Station.PASTRY_STATION),
    SANDWICH(Station.SANDWICH_STATION);

    private final Station station;

    ProductType(Station station) {
        this.station = station;
    }

    public Station getStation() {
        return station;
    }
}
