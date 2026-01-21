package com.foodtech.kitchen.application.usecases;

import com.foodtech.kitchen.application.exception.ProductNotFoundException;
import com.foodtech.kitchen.application.ports.out.ProductRepository;
import com.foodtech.kitchen.domain.model.Product;

public class UpdateProductAvailabilityUseCase {

    private final ProductRepository productRepository;

    public UpdateProductAvailabilityUseCase(ProductRepository productRepository) {
        if (productRepository == null) {
            throw new IllegalArgumentException("ProductRepository cannot be null");
        }
        this.productRepository = productRepository;
    }

    public Product execute(Long id, boolean available) {
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new ProductNotFoundException(id));
        
        product.setAvailable(available);
        product.updateTimestamp();
        
        return productRepository.save(product);
    }
}
