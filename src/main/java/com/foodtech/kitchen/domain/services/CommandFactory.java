package com.foodtech.kitchen.domain.services;

import com.foodtech.kitchen.domain.commands.*;
import com.foodtech.kitchen.domain.model.Product;
import com.foodtech.kitchen.domain.model.Station;

import java.util.List;

public class CommandFactory {

    public Command createCommand(Station station, List<Product> products) {
        return switch (station) {
            case ESPRESSO_BAR -> new PrepareDrinkCommand(products);
            case PASTRY_STATION -> new PreparePastryCommand(products);
            case SANDWICH_STATION -> new PrepareSandwichCommand(products);
        };
    }

    
}