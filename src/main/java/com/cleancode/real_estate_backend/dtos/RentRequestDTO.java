package com.cleancode.real_estate_backend.dtos;

import java.time.LocalDate;

public record RentRequestDTO(int size, LocalDate startDate, LocalDate endDate) {
}
