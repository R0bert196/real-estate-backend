package com.cleancode.real_estate_backend.dtos.tenant.ticket.request;

public record TicketUpdateRequestDTO(
        String severity,
        String department,
        String status
) {
}
