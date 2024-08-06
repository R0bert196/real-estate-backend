package com.cleancode.real_estate_backend.controllers;

import com.cleancode.real_estate_backend.dtos.auth.*;
import com.cleancode.real_estate_backend.enums.Role;
import com.cleancode.real_estate_backend.security.services.AuthenticationService;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService service;

    @PostMapping("/register")
    public ResponseEntity<?> register(
            @RequestBody RegisterRequest request,
            HttpServletRequest httpServletRequest
    ) {

        try {
            //TODO FIX!!!!
            service.register(request, httpServletRequest, Role.ROLE_MANAGER);

            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Exception occurred while creating new user");

            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/authenticate")
    public ResponseEntity<AuthenticationResponse> authenticate(
            @RequestBody AuthenticationRequest request
    ) {

        return ResponseEntity.ok(service.authenticate(request));
    }

    @PostMapping("register-creator")
    public ResponseEntity<?> registerCreator(
            @RequestBody RegisterRequest request,
            HttpServletRequest httpServletRequest
    ) {

        try {
            service.register(request, httpServletRequest, Role.ROLE_CREATOR);

            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Exception occurred while creating new user");

            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/verify")
    public ResponseEntity<?> verifyUser(@RequestBody VerificationCode code) {

        if (service.verify(code.code())) {
            return new ResponseEntity<>(HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/request-reset-password")
    public ResponseEntity<?> requestResetPassword(
            @RequestBody EmailRequest emailRequest,
           HttpServletRequest request) {

        try{
            service.sendResetPasswordEmail(emailRequest, request);
            return ResponseEntity.ok().build();

        }catch (EntityNotFoundException e) {
            log.error(e.toString(), "No account associated with this email");
            return ResponseEntity.ok().build();
        }

    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest body) {

        try {
            service.resetPassword(body);
            return ResponseEntity.ok().build();
        } catch (EntityNotFoundException e) {
            log.error(e.toString(), "Invalid verification code");

            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(HttpServletRequest request)  {

        try {
            return ResponseEntity.ok(service.refreshToken(request));

        } catch (UsernameNotFoundException e) {
            log.error(e.toString());

            return  ResponseEntity.badRequest().build();
        } catch (ExpiredJwtException expiredJwtException) {
            log.error(expiredJwtException.toString());

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }
    }

}
