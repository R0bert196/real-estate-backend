package com.cleancode.real_estate_backend.dtos.user;

public record AppUserRepresentativeRequestDTO(
        String email,
        String name,
        String phoneNumber
) {
}
