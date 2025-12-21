package com.ecommerce.ecommerce_backend.services;

import com.ecommerce.ecommerce_backend.entity.Product;

import java.util.List;

public interface ProductService {
    Product addProduct(Product product);

    List<Product> getAllProducts();

    Product getProductById(Long Id);

    Product updateProduct(Long id, Product product);

    void deleteProduct(Long id);
}
