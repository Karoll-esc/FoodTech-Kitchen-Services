package com.foodtech.kitchen.application.usecases;

import com.foodtech.kitchen.application.exception.ProductAlreadyExistsException;
import com.foodtech.kitchen.application.ports.out.ProductRepository;
import com.foodtech.kitchen.domain.model.Product;
import com.foodtech.kitchen.domain.services.ProductValidator;

public class CreateProductUseCase {

    private final ProductRepository productRepository;
    private final ProductValidator productValidator;

    public CreateProductUseCase(ProductRepository productRepository, ProductValidator productValidator) {
        this.productRepository = productRepository;
        this.productValidator = productValidator;
    }

    public Product execute(Product product) {
        if (product == null) {
            throw new IllegalArgumentException("Product cannot be null");
        }
        
        productValidator.validate(product);
        
        if (productRepository.existsByName(product.getName())) {
            throw new ProductAlreadyExistsException(product.getName());
        }
        
        return productRepository.save(product);
    }
}
