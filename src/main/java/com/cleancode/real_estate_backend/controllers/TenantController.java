package com.cleancode.real_estate_backend.controllers;

import com.cleancode.real_estate_backend.dtos.administrator.tenants.response.RentedFloorResponseDTO;
import com.cleancode.real_estate_backend.services.RentedFloorService;
import com.cleancode.real_estate_backend.services.TenantService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/tenant")
public class TenantController {

    private final TenantService tenantService;
    private final RentedFloorService rentedFloorService;

    @GetMapping("/rented-floors")
    public ResponseEntity<?> getRentedFloors() {

        //todo get rented floors by tenant id
        List<RentedFloorResponseDTO> rentedFloors =  rentedFloorService.getRentedFloors();
        return ResponseEntity.ok(rentedFloors);
    }
}
