package com.cleancode.real_estate_backend.controllers;

import com.cleancode.real_estate_backend.dtos.administrator.building.BuildingRequestDTO;
import com.cleancode.real_estate_backend.dtos.administrator.building.BuildingResponseDTO;
import com.cleancode.real_estate_backend.services.BuildingService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequestMapping("/api/administrator")
@RequiredArgsConstructor
public class AdministratorController {

    private final BuildingService buildingService;

    @PostMapping("/building")
    public ResponseEntity<?> addBuilding(@RequestBody BuildingRequestDTO buildingRequestDTO) {

        try {
            return ResponseEntity.ok(buildingService.addBuilding(buildingRequestDTO));
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.badRequest().body(null);
        }

    }
}
