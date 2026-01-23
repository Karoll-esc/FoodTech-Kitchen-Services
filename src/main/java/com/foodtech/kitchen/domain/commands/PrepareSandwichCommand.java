package com.foodtech.kitchen.domain.commands;

import com.foodtech.kitchen.domain.model.Product;

import java.util.ArrayList;
import java.util.List;

public class PrepareSandwichCommand implements Command {

    private static final int MIN_PREPARATION_SECONDS = 45;

    private final List<Product> products;

    public PrepareSandwichCommand(List<Product> products) {
        this.products = new ArrayList<>(products);
    }

    @Override
    public void execute() {
        System.out.println("\n[SANDWICH_STATION] 🥗 Starting preparation of " + products.size() + " sandwich/salad item(s)");

        int totalTime = 0;
        for (int i = 0; i < products.size(); i++) {
            Product product = products.get(i);
            int prepSeconds = resolvePreparationTime(product);
            System.out.println("[SANDWICH_STATION] Preparing item " + (i + 1) + "/" + products.size() + ": "
                + product.getName() + " (" + prepSeconds + "s)");

            simulatePreparation(prepSeconds);
            totalTime += prepSeconds;

            System.out.println("[SANDWICH_STATION] ✓ " + product.getName() + " ready!");
        }

        System.out.println("[SANDWICH_STATION] ✅ All sandwich/salad items completed in " + totalTime + " seconds\n");
    }

    private int resolvePreparationTime(Product product) {
        Integer prepSeconds = product.getPreparationTime();
        if (prepSeconds == null || prepSeconds <= 0) {
            return MIN_PREPARATION_SECONDS;
        }
        return prepSeconds;
    }

    private void simulatePreparation(int seconds) {
        try {
            Thread.sleep(seconds * 1000L);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Cold dish preparation interrupted", e);
        }
    }
}
