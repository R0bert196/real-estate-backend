package com.cleancode.real_estate_backend.dtos.tenant.ticket.response;


import com.cleancode.real_estate_backend.dtos.user.AppUserResponseDTOLite;

import java.util.List;

public record TicketResponseDTO(
        Long id,
        String severity,
        String status,
        String department,
        String subject,
        AppUserResponseDTOLite creator,

        List<TicketMessageResponseDTO> ticketMessageResponseDTO
) {
}
