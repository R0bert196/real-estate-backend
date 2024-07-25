package com.cleancode.real_estate_backend.controllers;

import com.cleancode.real_estate_backend.dtos.TestRequest;
import com.cleancode.real_estate_backend.dtos.kafka.EmailDTO;
import com.cleancode.real_estate_backend.dtos.kafka.KafkaMessage;
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

    private final KafkaTemplate<String, KafkaMessage> kafkaTemplate;

    @PostMapping
    public void publish(@RequestBody TestRequest request) {
        KafkaMessage dto =  new EmailDTO("lala", "zzz", "cacao");
        kafkaTemplate.send("mail", dto);
    }

}
