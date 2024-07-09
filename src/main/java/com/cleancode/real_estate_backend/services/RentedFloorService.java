package com.cleancode.real_estate_backend.services;

import com.cleancode.real_estate_backend.dtos.administrator.tenants.response.RentedFloorResponseDTO;
import com.cleancode.real_estate_backend.entities.RentedFloor;
import com.cleancode.real_estate_backend.repositories.RentedFloorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class RentedFloorService {

    private final RentedFloorRepository rentedFloorRepository;
    private final FloorService floorService;


    RentedFloorResponseDTO convertToDTO(RentedFloor entity) {

        return new RentedFloorResponseDTO(
                entity.getRentedSize(),
                entity.getSquareMeterPrice(),
                entity.getMaintenanceSquareMeterPrice(),
                floorService.convertToDTO(entity.getFloor())
        );
    }
}
