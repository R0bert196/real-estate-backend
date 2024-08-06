package com.cleancode.real_estate_backend.services;

import com.cleancode.real_estate_backend.dtos.administrator.tenants.response.RentedFloorResponseDTO;
import com.cleancode.real_estate_backend.entities.AppUser;
import com.cleancode.real_estate_backend.entities.RentedFloor;
import com.cleancode.real_estate_backend.entities.Tenant;
import com.cleancode.real_estate_backend.repositories.AppUserRepository;
import com.cleancode.real_estate_backend.repositories.RentedFloorRepository;
import com.cleancode.real_estate_backend.utils.IAuthenticationFacade;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class RentedFloorService {

    private final RentedFloorRepository rentedFloorRepository;
    private final FloorService floorService;
    private final IAuthenticationFacade authenticationFacade;
    private final AppUserRepository appUserRepository;


    RentedFloorResponseDTO convertToDTO(RentedFloor entity) {

        return new RentedFloorResponseDTO(
                entity.getId(),
                entity.getRentedSize(),
                entity.getSquareMeterPrice(),
                entity.getMaintenanceSquareMeterPrice(),
                floorService.convertToDTO(entity.getFloor())
        );
    }

    public List<RentedFloorResponseDTO> getTenantRentedFloors() {

        AppUser appUser = appUserRepository.findByEmail(authenticationFacade.getAuthentication().getName()).orElseThrow(EntityNotFoundException::new);

        Tenant tenant = appUser.getTenantRepresentant();

        return rentedFloorRepository.findAllWithFloorAndBuildingByTenantId(tenant.getId()).stream().map(this::convertToDTO).toList();
    }
}
