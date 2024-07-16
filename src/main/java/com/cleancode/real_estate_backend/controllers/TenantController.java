package com.cleancode.real_estate_backend.controllers;

import com.cleancode.real_estate_backend.dtos.administrator.tenants.response.RentedFloorResponseDTO;
import com.cleancode.real_estate_backend.dtos.tenant.ticket.request.TicketRequestDTO;
import com.cleancode.real_estate_backend.dtos.tenant.ticket.response.TicketResponseDTOLite;
import com.cleancode.real_estate_backend.enums.ticket.TicketSeverity;
import com.cleancode.real_estate_backend.services.PhotoService;
import com.cleancode.real_estate_backend.services.RentedFloorService;
import com.cleancode.real_estate_backend.services.TenantService;
import com.cleancode.real_estate_backend.services.TicketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/tenant")
public class TenantController {

    private final TenantService tenantService;
    private final RentedFloorService rentedFloorService;
    private final TicketService ticketService;
    private final PhotoService photoService;

    @GetMapping("/rented-floors")
    public ResponseEntity<?> getRentedFloors() {

        //todo get rented floors by tenant id
        List<RentedFloorResponseDTO> rentedFloors =  rentedFloorService.getRentedFloors();
        return ResponseEntity.ok(rentedFloors);
    }

    @PostMapping(path = "/ticket",  consumes = "multipart/form-data")
    public ResponseEntity<TicketResponseDTOLite> createTicket(
            @RequestParam("subject") String subject,
            @RequestParam("message") String message,
            @RequestParam("severity") String severity,
            @RequestParam("images") MultipartFile[] images) {

        // Replace these with actual IDs
        Long creatorId = 1L;
        Long ticketId = 1L;

        Set<String> imageUrls = null;
        try {
            imageUrls = photoService.savePhotos(creatorId, ticketId, images);
        } catch (IOException e) {
            log.error(e.toString());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        TicketRequestDTO ticketRequestDTO = new TicketRequestDTO(subject, message, severity, imageUrls);

        TicketResponseDTOLite ticketResponse = ticketService.addTicket(ticketRequestDTO);
        return new ResponseEntity<>(ticketResponse, HttpStatus.CREATED);
    }
}
