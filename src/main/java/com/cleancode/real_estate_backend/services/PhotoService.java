package com.cleancode.real_estate_backend.services;

import com.cleancode.real_estate_backend.entities.AppUser;
import com.cleancode.real_estate_backend.repositories.AppUserRepository;
import com.cleancode.real_estate_backend.utils.IAuthenticationFacade;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Log4j2
public class PhotoService {

    @Value("${photo.upload.dir}")
    private String uploadDir;

    private final IAuthenticationFacade authenticationFacade;
    private final AppUserRepository appUserRepository;

    public Set<String> savePhotos(Long messageId, MultipartFile[] photos) throws IOException {
        String userEmail = authenticationFacade.getAuthentication().getName();
        log.info("Saving photos for user: {} and message ID: {}", userEmail, messageId);

        AppUser creator = appUserRepository.findByEmail(userEmail)
                .orElseThrow(() -> {
                    log.error("User not found: {}", userEmail);
                    return new EntityNotFoundException("User not found: " + userEmail);
                });

        Path uploadPath = Paths.get(uploadDir, "creator_" + creator.getId(), "message_" + messageId);
        log.info("Creating directories for path: {}", uploadPath);
        Files.createDirectories(uploadPath);

        return Arrays.stream(photos)
                .map(photo -> savePhoto(creator.getId(), messageId, photo))
                .collect(Collectors.toSet());
    }

    public List<byte[]> getPhotos(Set<String> imageUrls) throws IOException {
        log.info("Fetching photos for URLs: {}", imageUrls);

        return imageUrls.stream().map(imageUrl -> {
            Path imagePath = Paths.get(imageUrl);
            if (!Files.exists(imagePath) || !Files.isRegularFile(imagePath)) {
                log.error("Image file not found: {}", imageUrl);
                throw new RuntimeException("Image file not found: " + imageUrl);
            }
            try {
                log.info("Reading bytes from image: {}", imageUrl);
                return Files.readAllBytes(imagePath);
            } catch (IOException e) {
                log.error("Error reading file: {}", imageUrl, e);
                throw new RuntimeException("Error reading file " + imageUrl, e);
            }
        }).collect(Collectors.toList());
    }

    private String savePhoto(Long creatorId, Long messageId, MultipartFile photo) {
        try {
            String photoFileName = photo.getOriginalFilename();
            Path filePath = Paths.get(uploadDir, "creator_" + creatorId, "message_" + messageId, photoFileName);

            log.info("Saving photo: {} for user ID: {} and message ID: {}", photoFileName, creatorId, messageId);
            Files.copy(photo.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            log.info("Photo saved successfully at: {}", filePath);
            return filePath.toString();
        } catch (IOException e) {
            log.error("Failed to store photo: {}", photo.getOriginalFilename(), e);
            throw new RuntimeException("Failed to store photo " + photo.getOriginalFilename(), e);
        }
    }
}
