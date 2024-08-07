package com.cleancode.real_estate_backend.controllers;

import com.cleancode.real_estate_backend.dtos.auth.*;
import com.cleancode.real_estate_backend.enums.Role;
import com.cleancode.real_estate_backend.security.services.AuthenticationService;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

@RestController
@Log4j2
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService service;

    @PostMapping("/register")
    public ResponseEntity<?> register(
            @RequestBody RegisterRequest request,
            HttpServletRequest httpServletRequest
    ) {
        log.info("Register request received: {}", request);

        try {
            service.register(request, httpServletRequest, Role.ROLE_MANAGER);
            log.info("User registered successfully with role MANAGER: {}", request.getEmail());
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Exception occurred while creating new user: {}", request.getEmail(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/authenticate")
    public ResponseEntity<AuthenticationResponse> authenticate(
            @RequestBody AuthenticationRequest request
    ) {
        log.info("Authentication request received for user: {}", request.getEmail());

        AuthenticationResponse response = service.authenticate(request);
        log.info("User authenticated successfully: {}", request.getEmail());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register-creator")
    public ResponseEntity<?> registerCreator(
            @RequestBody RegisterRequest request,
            HttpServletRequest httpServletRequest
    ) {
        log.info("Register creator request received: {}", request);

        try {
            service.register(request, httpServletRequest, Role.ROLE_CREATOR);
            log.info("User registered successfully with role CREATOR: {}", request.getEmail());
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Exception occurred while creating new user: {}", request.getEmail(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/verify")
    public ResponseEntity<?> verifyUser(@RequestBody VerificationCode code) {
        log.info("Verification request received for code: {}", code.code());

        if (service.verify(code.code())) {
            log.info("User verified successfully with code: {}", code.code());
            return new ResponseEntity<>(HttpStatus.OK);
        } else {
            log.warn("User verification failed for code: {}", code.code());
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/request-reset-password")
    public ResponseEntity<?> requestResetPassword(
            @RequestBody EmailRequest emailRequest,
            HttpServletRequest request) {
        log.info("Password reset request received for email: {}", emailRequest.getEmail());

        try {
            service.sendResetPasswordEmail(emailRequest, request);
            log.info("Password reset email sent successfully to: {}", emailRequest.getEmail());
            return ResponseEntity.ok().build();
        } catch (EntityNotFoundException e) {
            log.warn("No account associated with email: {}", emailRequest.getEmail(), e);
            return ResponseEntity.ok().build();
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest body) {
        log.info("Reset password request received for code: {}", body.getCode());

        try {
            service.resetPassword(body);
            log.info("Password reset successfully for code: {}", body.getCode());
            return ResponseEntity.ok().build();
        } catch (EntityNotFoundException e) {
            log.error("Invalid verification code for password reset: {}", body.getCode(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(HttpServletRequest request) {
        log.info("Token refresh request received");

        try {
            return ResponseEntity.ok(service.refreshToken(request));
        } catch (UsernameNotFoundException e) {
            log.error("Username not found during token refresh", e);
            return ResponseEntity.badRequest().build();
        } catch (ExpiredJwtException expiredJwtException) {
            log.error("Expired JWT during token refresh", expiredJwtException);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }
    }
}
