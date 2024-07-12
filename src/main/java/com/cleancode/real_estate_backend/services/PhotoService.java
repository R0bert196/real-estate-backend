package com.cleancode.real_estate_backend.services;

import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Service
public class PhotoService {

    private static final String UPLOAD_DIR = "/photos/tickets/";

    public String savePhoto(Long creatorId, Long ticketId, byte[] photoBytes) throws IOException {

        Path uploadPath = Paths.get(UPLOAD_DIR);
        Files.createDirectories(uploadPath);

        String photoFileName = "creator_" + creatorId + "_ticket_" + ticketId + "_photo.jpg";
        Path filePath = uploadPath.resolve(photoFileName);

        Files.copy(new ByteArrayInputStream(photoBytes), filePath, StandardCopyOption.REPLACE_EXISTING);

        return filePath.toString();
    }
}
