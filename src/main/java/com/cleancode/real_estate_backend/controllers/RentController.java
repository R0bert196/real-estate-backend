package com.cleancode.real_estate_backend.controllers;

import com.cleancode.real_estate_backend.dtos.RentRequestDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/rent")
@CrossOrigin(origins = "http://localhost:3000")
public class RentController {


    @PostMapping
    public String rentRequest(@RequestBody RentRequestDTO rentRequestDTO) {

        System.out.println(rentRequestDTO.startDate());
        System.out.println(rentRequestDTO.endDate());
        System.out.println(rentRequestDTO.size());
        return "{\"success\":1}";
    }
}
