package com.cleancode.real_estate_backend.dtos.tenant.ticket.response;


import java.util.List;

public record TicketResponseDTO(
        Long id,
        String severity,
        String status,
        String department,
        String subject,

        List<TicketMessageResponseDTO> ticketMessageResponseDTO
) {
}
