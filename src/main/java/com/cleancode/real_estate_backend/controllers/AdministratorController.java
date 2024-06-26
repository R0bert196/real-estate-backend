package com.cleancode.real_estate_backend.controllers;

import com.cleancode.real_estate_backend.dtos.administrator.building.BuildingRequestDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequestMapping("/api/administrator")
@RequiredArgsConstructor
public class AdministratorController {

    @PostMapping("/building")
    public void addBuilding(@RequestBody BuildingRequestDTO buildingRequestDTO) {

        System.out.println(buildingRequestDTO);

    }
}
