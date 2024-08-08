package com.cleancode.real_estate_backend.services;

import com.cleancode.real_estate_backend.dtos.kafka.EmailDTO;
import com.cleancode.real_estate_backend.dtos.kafka.KafkaMessage;
import com.cleancode.real_estate_backend.dtos.user.AppUserRepresentantRequestDTO;
import com.cleancode.real_estate_backend.dtos.user.AppUserResponseDTOLite;
import com.cleancode.real_estate_backend.entities.AppUser;
import com.cleancode.real_estate_backend.entities.Tenant;
import com.cleancode.real_estate_backend.enums.Role;
import com.cleancode.real_estate_backend.repositories.TenantRepository;
import com.cleancode.real_estate_backend.repositories.AppUserRepository;
import com.cleancode.real_estate_backend.utils.EmailComposer;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;

@Service
@Log4j2
@RequiredArgsConstructor
public class AppUserService {

    private final AppUserRepository appUserRepository;
    private final TenantRepository tenantRepository;

    private final PasswordEncoder passwordEncoder;
    private final KafkaTemplate<String, KafkaMessage> kafkaTemplate;

    public AppUserResponseDTOLite convertToDto(AppUser appUser) {
        if (appUser == null) {
            throw new EntityNotFoundException("User not found");
        }

        return new AppUserResponseDTOLite(
                appUser.getId(),
                appUser.getEmail(),
                appUser.getName(),
                appUser.getPhoneNumber()
        );
    }


    public List<AppUserResponseDTOLite> getTenantRepresentants(Long tenantId) {

        List<AppUser> representants = appUserRepository.findTicketTenantRepresentatnsByTenantId(tenantId);

        return representants.stream().map(this::convertToDto).toList();
    }

    public AppUserResponseDTOLite addTenantRepresentative(Long tenantId, AppUserRepresentantRequestDTO representativeRequestDTO, HttpServletRequest httpServletRequest) {

        Tenant tenant = tenantRepository.findById(tenantId).orElseThrow(() -> {
            log.error("Tenant not found for id: {}", tenantId);
            return new EntityNotFoundException("Tenant not found");
        });

        String generatedPassword = RandomStringUtils.random(10, true, true);

        AppUser representative = AppUser.builder()
                .name(representativeRequestDTO.name())
                .email(representativeRequestDTO.email())
                .phoneNumber(representativeRequestDTO.phoneNumber())
                .password(passwordEncoder.encode(generatedPassword))
                .tenantRepresentant(tenant)
                .role(new HashSet<>(Collections.singleton(Role.ROLE_REPRESENTANT)))
                .build();

        AppUser savedUser = appUserRepository.save(representative);

        KafkaMessage kafkaMessage = EmailComposer.createRepresentativeAccountConfirmationEmail(representative, tenant.getName(), generatedPassword, httpServletRequest);

        kafkaTemplate.send("mail", kafkaMessage);

        return new AppUserResponseDTOLite(savedUser.getId(), savedUser.getEmail(), savedUser.getName(), savedUser.getPhoneNumber());

    }



}
