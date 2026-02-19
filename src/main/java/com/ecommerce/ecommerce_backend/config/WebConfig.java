package com.ecommerce.ecommerce_backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${upload.dir:uploads/products}")
    private String uploadDir;

    /**
     * Configure Spring to serve uploaded images as static files
     * This makes images accessible at: http://localhost:8080/uploads/products/image.jpg
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Get absolute path
        String absolutePath = Paths.get(uploadDir).toAbsolutePath().toString();

        // Map /uploads/products/** URLs to the upload directory
        registry.addResourceHandler("/uploads/products/**")
                .addResourceLocations("file:" + absolutePath + "/");

        System.out.println("✅ Serving product images from: " + absolutePath);
    }
}