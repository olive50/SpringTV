package com.tvboot.tivio.common.util;

import com.tvboot.tivio.common.exception.ResourceNotFoundException;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
@Service
public class FileStorageService {

    @Value("${file.upload-dir:./uploads}")
    private String uploadDir;

    private Path fileStorageLocation;

    @PostConstruct
    public void init() {
        this.fileStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new RuntimeException("Could not create upload directory!", ex);
        }
    }

    public String storeFile(MultipartFile file, String subDir) {
        // Validate file
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        // Clean filename
        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
        String fileExtension = getFileExtension(originalFilename);
        String newFilename = UUID.randomUUID().toString() + fileExtension;

        try {
            // Create subdirectory if needed
            Path targetLocation = this.fileStorageLocation.resolve(subDir);
            Files.createDirectories(targetLocation);

            // Copy file to target location
            Path targetFile = targetLocation.resolve(newFilename);
            Files.copy(file.getInputStream(), targetFile, StandardCopyOption.REPLACE_EXISTING);

            return subDir + "/" + newFilename;
        } catch (IOException ex) {
            throw new RuntimeException("Could not store file " + newFilename, ex);
        }
    }

    public Resource loadFileAsResource(String filePath) {
        try {
            Path file = fileStorageLocation.resolve(filePath).normalize();
            Resource resource = new UrlResource(file.toUri());

            if (resource.exists()) {
                return resource;
            } else {
                throw new ResourceNotFoundException("File not found: " + filePath);
            }
        } catch (MalformedURLException ex) {
            throw new ResourceNotFoundException("File not found: " + filePath, ex);
        }
    }

    public void deleteFile(String filePath) {
        try {
            Path file = fileStorageLocation.resolve(filePath).normalize();
            Files.deleteIfExists(file);
        } catch (IOException ex) {
            // Log error but don't throw exception
            System.err.println("Could not delete file: " + filePath);
        }
    }

    private String getFileExtension(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        return (dotIndex == -1) ? "" : filename.substring(dotIndex);
    }

    public String storeFileWithCustomName(MultipartFile file, String subDir, String customFilename) {
        // Validate file
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        // Validate custom filename
        if (!StringUtils.hasText(customFilename)) {
            throw new IllegalArgumentException("Custom filename cannot be empty");
        }

        try {
            // Create subdirectory if needed
            Path targetLocation = this.fileStorageLocation.resolve(subDir);
            Files.createDirectories(targetLocation);

            // Clean and use custom filename
            String cleanFilename = StringUtils.cleanPath(customFilename);
            Path targetFile = targetLocation.resolve(cleanFilename);

            // Copy file to target location
            Files.copy(file.getInputStream(), targetFile, StandardCopyOption.REPLACE_EXISTING);

            return subDir + "/" + cleanFilename;
        } catch (IOException ex) {
            throw new RuntimeException("Could not store file " + customFilename, ex);
        }
    }

    public static String generateChannelLogoFilename(String channelName, int channelNumber, String originalFilename) {
        // Get file extension
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        // Sanitize channel name: remove special characters and convert to lowercase
        String sanitizedName = channelName
                .toLowerCase()
                .replaceAll("[^a-zA-Z0-9\\s]", "") // Remove special chars
                .replaceAll("\\s+", "-")           // Replace spaces with hyphens
                .trim();

        // Format: channel-name_number.extension
        return String.format("%s_%d%s", sanitizedName, channelNumber, extension);
    }
}