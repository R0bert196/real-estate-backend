package com.cleancode.real_estate_backend.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class PhotoService {

    @Value("${photo.upload.dir}")
    private String uploadDir;

    public Set<String> savePhotos(Long creatorId, Long ticketId, MultipartFile[] photos) throws IOException {
        Path uploadPath = Paths.get(uploadDir);
        Files.createDirectories(uploadPath);

        return Arrays.stream(photos)
                .map(photo -> savePhoto(creatorId, ticketId, photo))
                .collect(Collectors.toSet());
    }

    private String savePhoto(Long creatorId, Long ticketId, MultipartFile photo) {
        try {
            String photoFileName = "creator_" + creatorId + "_ticket_" + ticketId + "_" + photo.getOriginalFilename();
            Path filePath = Paths.get(uploadDir, photoFileName);

            Files.copy(photo.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            return filePath.toString();
        } catch (IOException e) {
            throw new RuntimeException("Failed to store photo " + photo.getOriginalFilename(), e);
        }
    }
}
