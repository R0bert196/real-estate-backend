package com.cleancode.real_estate_backend.dtos.auth;

import lombok.Data;

@Data
public class ResetPasswordRequest {

    private String password;
    private String code;

}
