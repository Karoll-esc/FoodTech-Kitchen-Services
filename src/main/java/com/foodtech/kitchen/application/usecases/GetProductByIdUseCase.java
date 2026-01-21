package com.foodtech.kitchen.application.usecases;

import com.foodtech.kitchen.application.exception.ProductNotFoundException;
import com.foodtech.kitchen.application.ports.out.ProductRepository;
import com.foodtech.kitchen.domain.model.Product;

public class GetProductByIdUseCase {

    private final ProductRepository productRepository;

    public GetProductByIdUseCase(ProductRepository productRepository) {
        if (productRepository == null) {
            throw new IllegalArgumentException("ProductRepository cannot be null");
        }
        this.productRepository = productRepository;
    }

    public Product execute(Long productId) {
        if (productId == null) {
            throw new IllegalArgumentException("Product ID cannot be null");
        }

        return productRepository.findById(productId)
            .orElseThrow(() -> new ProductNotFoundException(productId));
    }
}
