package com.cleancode.real_estate_backend.dtos.kafka;


public record EmailDTO(
        String userEmail,
        String subject,
        String content) implements KafkaMessage {
}

