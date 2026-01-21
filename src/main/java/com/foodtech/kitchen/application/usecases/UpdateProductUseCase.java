package com.foodtech.kitchen.application.usecases;

import com.foodtech.kitchen.application.exception.ProductNotFoundException;
import com.foodtech.kitchen.application.ports.out.ProductRepository;
import com.foodtech.kitchen.domain.model.Price;
import com.foodtech.kitchen.domain.model.Product;
import com.foodtech.kitchen.domain.services.ProductValidator;

public class UpdateProductUseCase {

    private final ProductRepository productRepository;
    private final ProductValidator productValidator;

    public UpdateProductUseCase(ProductRepository productRepository, ProductValidator productValidator) {
        if (productRepository == null) {
            throw new IllegalArgumentException("ProductRepository cannot be null");
        }
        if (productValidator == null) {
            throw new IllegalArgumentException("ProductValidator cannot be null");
        }
        this.productRepository = productRepository;
        this.productValidator = productValidator;
    }

    public Product execute(Long id, String description, Price price, Integer preparationTime) {
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new ProductNotFoundException(id));
        
        product.updateDetails(description, price, preparationTime);
        productValidator.validate(product);
        
        return productRepository.save(product);
    }
}
