package com.cleancode.real_estate_backend.dtos.tenant.ticket.response;

import org.springframework.core.io.Resource;

import java.util.List;

public record TicketMessageResponseDTO(
        Long id,
        String message,
        Long createTs,
        List<Resource> images

) {
}
