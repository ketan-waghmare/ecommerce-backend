package com.ecommerce.ecommerce_backend.controller;

import com.ecommerce.ecommerce_backend.entity.Product;
import com.ecommerce.ecommerce_backend.services.ImageUploadService;
import com.ecommerce.ecommerce_backend.services.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@Slf4j
@RequestMapping("/api/products")
public class ProductController {

    @Autowired
    ProductService productService;

    @Autowired
    private ImageUploadService imageUploadService;

//    @PostMapping
//    public Product addProduct(@RequestBody Product product) {
//        return productService.addProduct(product);
//    }
//
//    @GetMapping
//    public List<Product> getAllProducts() {
//        return productService.getAllProducts();
//    }
//
    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts() {
        return ResponseEntity.ok(productService.getAllActiveProducts());
    }

    @GetMapping("/{id}")
    public Product getProductById(@PathVariable("id") Long id) {
        return productService.getProductById(id);
    }

    @GetMapping("/test")
    public String test() {
        return "testing done";
    }

    // UPDATE
//    @PutMapping("/{id}")
//    public Product updateProduct(
//            @PathVariable Long id,
//            @RequestBody Product product) {
//        return productService.updateProduct(id, product);
//    }

//    @DeleteMapping("/{id}")
//    public ResponseEntity<String> deleteProduct(@PathVariable Long id) {
//        productService.deleteProduct(id);
//        return ResponseEntity.ok("Product deleted successfully");
//    }

    /**
     * Add new product WITH IMAGE
     * Use @RequestParam for multipart/form-data
     */
    @PostMapping
    public ResponseEntity<?> addProduct(
            @RequestParam("name") String name,
            @RequestParam("description") String description,
            @RequestParam("price") Double price,
            @RequestParam("stock") Integer stock,
            @RequestParam(value = "image", required = false) MultipartFile image) {

        try {
            log.info("📦 Adding product: {}", name);

            Product product = new Product();
            product.setName(name);
            product.setDescription(description);
            product.setPrice(price);
            product.setStock(stock);
            product.setIsActive(true);

            // Upload image if provided
            if (image != null && !image.isEmpty()) {
                log.info("📸 Uploading image: {}", image.getOriginalFilename());
                String imageUrl = imageUploadService.uploadImage(image);
                product.setImageUrl(imageUrl);
                log.info("✅ Image uploaded: {}", imageUrl);
            }

            Product savedProduct = productService.addProduct(product);
            log.info("✅ Product added with ID: {}", savedProduct.getId());

            return ResponseEntity.ok(savedProduct);

        } catch (IOException e) {
            log.error("❌ Image upload failed: {}", e.getMessage());
            return ResponseEntity.status(500).body("Image upload failed: " + e.getMessage());
        } catch (Exception e) {
            log.error("❌ Failed to add product: {}", e.getMessage());
            return ResponseEntity.status(500).body("Failed to add product: " + e.getMessage());
        }
    }

    /**
     * Update product WITH IMAGE
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateProduct(
            @PathVariable Long id,
            @RequestParam("name") String name,
            @RequestParam("description") String description,
            @RequestParam("price") Double price,
            @RequestParam("stock") Integer stock,
            @RequestParam(value = "image", required = false) MultipartFile image) {

        try {
            log.info("✏️ Updating product: {}", id);

            Product product = productService.getProductById(id);
            product.setName(name);
            product.setDescription(description);
            product.setPrice(price);
            product.setStock(stock);

            // Upload new image if provided
            if (image != null && !image.isEmpty()) {
                log.info("📸 Uploading new image");

                // Delete old image
                if (product.getImageUrl() != null) {
                    imageUploadService.deleteImage(product.getImageUrl());
                }

                // Upload new image
                String imageUrl = imageUploadService.uploadImage(image);
                product.setImageUrl(imageUrl);
                log.info("✅ New image uploaded: {}", imageUrl);
            }

            Product updatedProduct = productService.updateProduct(id, product);
            log.info("✅ Product updated: {}", id);

            return ResponseEntity.ok(updatedProduct);

        } catch (IOException e) {
            log.error("❌ Image upload failed: {}", e.getMessage());
            return ResponseEntity.status(500).body("Image upload failed: " + e.getMessage());
        } catch (Exception e) {
            log.error("❌ Failed to update product: {}", e.getMessage());
            return ResponseEntity.status(500).body("Failed to update product: " + e.getMessage());
        }
    }

    /**
     * Delete product (also deletes image)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProduct(@PathVariable Long id) {
        try {
            log.info("🗑️ Deleting product: {}", id);

            Product product = productService.getProductById(id);

            // Delete image if exists
            if (product.getImageUrl() != null) {
                imageUploadService.deleteImage(product.getImageUrl());
            }

//            productService.deleteProduct(id);
            product.setIsActive(false);
            productService.addProduct(product);
            log.info("✅ Product deleted: {}", id);

            return ResponseEntity.ok("Product deleted successfully");

        } catch (Exception e) {
            log.error("❌ Failed to delete product: {}", e.getMessage());
            return ResponseEntity.status(500).body("Failed to delete product: " + e.getMessage());
        }
    }

}
