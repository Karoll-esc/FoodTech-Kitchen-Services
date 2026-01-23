package com.foodtech.kitchen.domain.commands;

import com.foodtech.kitchen.domain.model.Product;

import java.util.ArrayList;
import java.util.List;

public class PreparePastryCommand implements Command {

    private static final int MIN_PREPARATION_SECONDS = 30;

    private final List<Product> products;

    public PreparePastryCommand(List<Product> products) {
        this.products = new ArrayList<>(products);
    }

    @Override
    public void execute() {
        System.out.println("\n[PASTRY_STATION] 🔥 Starting preparation of " + products.size() + " pastry item(s)");

        int totalTime = 0;
        for (int i = 0; i < products.size(); i++) {
            Product product = products.get(i);
            int prepSeconds = resolvePreparationTime(product);
            System.out.println("[PASTRY_STATION] Baking item " + (i + 1) + "/" + products.size() + ": "
                + product.getName() + " (" + prepSeconds + "s)");

            simulatePreparation(prepSeconds);
            totalTime += prepSeconds;

            System.out.println("[PASTRY_STATION] ✓ " + product.getName() + " ready!");
        }

        System.out.println("[PASTRY_STATION] ✅ All pastry items completed in " + totalTime + " seconds\n");
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
            throw new RuntimeException("Hot dish preparation interrupted", e);
        }
    }
}
