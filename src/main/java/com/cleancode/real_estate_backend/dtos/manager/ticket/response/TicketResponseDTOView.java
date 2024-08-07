package com.cleancode.real_estate_backend.dtos.manager.ticket.response;

public record TicketResponseDTOView(
        Long id,
        String subject,
        String severity,
        String status,
        Long creatorId,
        String creatorName,
        String department
) {
}
