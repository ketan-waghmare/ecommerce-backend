package com.ecommerce.ecommerce_backend.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class ImageUploadService {

    // This will come from application.properties
    @Value("${upload.dir:uploads/products}")
    private String uploadDir;

    @Value("${server.url:http://localhost:8080}")
    private String serverUrl;

    /**
     * Upload image to local filesystem
     * Returns the URL to access the image
     */
    public String uploadImage(MultipartFile file) throws IOException {
        
        // Validate file
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        // Validate file type
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("File must be an image");
        }

        // Create upload directory if it doesn't exist
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Generate unique filename
        String originalFilename = file.getOriginalFilename();
        String fileExtension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String uniqueFilename = UUID.randomUUID().toString() + fileExtension;

        // Save file
        Path filePath = uploadPath.resolve(uniqueFilename);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        // Return URL
        // Example: http://localhost:8080/uploads/products/abc-123-def.jpg
        return serverUrl + "/uploads/products/" + uniqueFilename;
    }

    /**
     * Delete image from filesystem
     */
    public void deleteImage(String imageUrl) {
        try {
            // Extract filename from URL
            // Example: http://localhost:8080/uploads/products/abc-123.jpg -> abc-123.jpg
            String filename = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
            Path filePath = Paths.get(uploadDir, filename);
            
            if (Files.exists(filePath)) {
                Files.delete(filePath);
            }
        } catch (Exception e) {
            // Log but don't throw - image deletion is not critical
            System.err.println("Failed to delete image: " + e.getMessage());
        }
    }

    /**
     * TODO: For production, replace this class with CloudImageUploadService
     * that uploads to AWS S3, Cloudinary, or other cloud storage
     * 
     * The interface remains the same:
     * - uploadImage(MultipartFile) -> returns URL
     * - deleteImage(String url) -> deletes from cloud
     * 
     * This makes it easy to swap implementations later!
     */
}
