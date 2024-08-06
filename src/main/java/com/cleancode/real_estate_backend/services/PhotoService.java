package com.cleancode.real_estate_backend.services;

import com.cleancode.real_estate_backend.entities.AppUser;
import com.cleancode.real_estate_backend.repositories.AppUserRepository;
import com.cleancode.real_estate_backend.utils.IAuthenticationFacade;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
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
public class PhotoService {

    @Value("${photo.upload.dir}")
    private String uploadDir;

    IAuthenticationFacade authenticationFacade;
    private final AppUserRepository appUserRepository;

    public Set<String> savePhotos(Long messageId, MultipartFile[] photos) throws IOException {

        AppUser creator = appUserRepository.findByEmail(authenticationFacade.getAuthentication().getName()).orElseThrow(EntityNotFoundException::new);

        Path uploadPath = Paths.get(uploadDir, "creator_" + creator.getId(), "message_" + messageId);
        Files.createDirectories(uploadPath);

        return Arrays.stream(photos)
                .map(photo -> savePhoto(creator.getId(), messageId, photo))
                .collect(Collectors.toSet());
    }

    public List<byte[]> getPhotos(Set<String> imageUrls) throws IOException {
        return imageUrls.stream().map(imageUrl -> {
            Path imagePath = Paths.get(imageUrl);
            if (!Files.exists(imagePath) || !Files.isRegularFile(imagePath)) {
                throw new RuntimeException("Image file not found: " + imageUrl);
            }
            try {
                return Files.readAllBytes(imagePath);
            } catch (IOException e) {
                throw new RuntimeException("Error reading file " + imageUrl, e);
            }
        }).collect(Collectors.toList());
    }

    private String savePhoto(Long creatorId, Long messageId, MultipartFile photo) {
        try {
            String photoFileName = photo.getOriginalFilename();
            Path filePath = Paths.get(uploadDir, "creator_" + creatorId, "message_" + messageId, photoFileName);

            Files.copy(photo.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            return filePath.toString();
        } catch (IOException e) {
            throw new RuntimeException("Failed to store photo " + photo.getOriginalFilename(), e);
        }
    }
}
