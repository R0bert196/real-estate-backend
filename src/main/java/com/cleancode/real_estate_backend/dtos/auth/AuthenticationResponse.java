package com.cleancode.real_estate_backend.dtos.auth;

import com.cleancode.real_estate_backend.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthenticationResponse {

    private String token;
    private String refreshToken;
    private Set<Role> roles;

}
