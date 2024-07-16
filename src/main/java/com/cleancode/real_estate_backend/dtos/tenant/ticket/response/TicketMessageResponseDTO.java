package com.cleancode.real_estate_backend.dtos.tenant.ticket.response;

import com.cleancode.real_estate_backend.dtos.user.AppUserResponseDTOLite;

import java.util.List;

public record TicketMessageResponseDTO(
        Long id,
        String message,
        Long createTs,
        List<byte[]> images,

        AppUserResponseDTOLite creator
) {
}
