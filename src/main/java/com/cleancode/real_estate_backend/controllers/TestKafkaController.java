package com.cleancode.real_estate_backend.controllers;

import com.cleancode.real_estate_backend.dtos.TestRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
public class TestKafkaController {

    private final KafkaTemplate<String, String> kafkaTemplate;

    @PostMapping
    public void publish(@RequestBody TestRequest request) {
        kafkaTemplate.send("mail", request.message());
    }

}
