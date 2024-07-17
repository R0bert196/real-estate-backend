package com.cleancode.real_estate_backend.dtos.tenant.ticket.request;

import java.util.Set;

public record TicketRequestDTO(
        String subject,
        String message,
        String severity,
        String department,
        Long rentedFloorId
) {
}
