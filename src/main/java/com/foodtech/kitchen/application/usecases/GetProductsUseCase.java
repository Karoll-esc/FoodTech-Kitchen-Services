package com.foodtech.kitchen.application.usecases;

import com.foodtech.kitchen.application.ports.out.ProductRepository;
import com.foodtech.kitchen.domain.model.Product;
import com.foodtech.kitchen.domain.model.ProductType;

import java.util.List;
import java.util.stream.Collectors;

public class GetProductsUseCase {

    private final ProductRepository productRepository;

    public GetProductsUseCase(ProductRepository productRepository) {
        if (productRepository == null) {
            throw new IllegalArgumentException("ProductRepository cannot be null");
        }
        this.productRepository = productRepository;
    }

    public List<Product> execute(Boolean available, ProductType type) {
        List<Product> products;

        if (type != null) {
            products = productRepository.findByType(type);
        } else if (available != null) {
            products = productRepository.findByAvailable(available);
        } else {
            products = productRepository.findAll();
        }

        if (available != null && type != null) {
            products = products.stream()
                .filter(p -> p.isAvailable() == available)
                .collect(Collectors.toList());
        }

        return products;
    }
}
