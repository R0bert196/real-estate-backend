package com.cleancode.real_estate_backend.services;

import com.cleancode.real_estate_backend.dtos.manager.tenants.response.TenantResponseDTO;
import com.cleancode.real_estate_backend.dtos.user.AppUserResponseDTOLite;
import com.cleancode.real_estate_backend.entities.AppUser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.cleancode.real_estate_backend.repositories.AppUserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AppUserService {

    private final AppUserRepository appUserRepository;

    @Autowired
    ObjectMapper objectMapper;

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

}
