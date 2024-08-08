package com.cleancode.real_estate_backend.dtos.user;

public record AppUserRepresentantRequestDTO(
        String email,
        String name,
        String phoneNumber
) {
}
