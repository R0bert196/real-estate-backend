package com.cleancode.real_estate_backend.services;

import com.cleancode.real_estate_backend.dtos.administrator.building.FloorResponseDTO;
import com.cleancode.real_estate_backend.entities.Floor;
import com.cleancode.real_estate_backend.repositories.FloorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class FloorService {

    private final FloorRepository floorRepository;


    public FloorResponseDTO convertToDTO(Floor entity) {
        return new FloorResponseDTO(entity.getSize(), entity.getFloorNumber());
    }
}
