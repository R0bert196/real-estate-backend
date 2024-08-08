package com.cleancode.real_estate_backend.security.services;

import com.cleancode.real_estate_backend.dtos.TestRequest;
import com.cleancode.real_estate_backend.dtos.auth.*;
import com.cleancode.real_estate_backend.dtos.kafka.EmailDTO;
import com.cleancode.real_estate_backend.dtos.kafka.KafkaMessage;
import com.cleancode.real_estate_backend.entities.AppUser;
import com.cleancode.real_estate_backend.enums.Role;
import com.cleancode.real_estate_backend.repositories.AppUserRepository;
import com.cleancode.real_estate_backend.utils.EmailComposer;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final AppUserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    private final KafkaTemplate<String, KafkaMessage> kafkaTemplate;

    public void register(RegisterRequest request, HttpServletRequest httpServletRequest, Role role) {

        Set<Role> roles = new HashSet<>();
        roles.add(role);

        AppUser user = AppUser.builder()
                .name(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(roles)
                .build();
        repository.save(user);
        KafkaMessage kafkaMessage = EmailComposer.createAccountConfirmationEmail(user, httpServletRequest);
        kafkaTemplate.send("mail", kafkaMessage);
//        sendVerificationEmail(user, httpServletRequest);
//        String jwtToken = jwtService.generateToken(user);
//        return AuthenticationResponse.builder()
//                .token(jwtToken)
//                .build();
    }


    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );
        AppUser user = repository.findByEmail(request.getEmail())
                .orElseThrow();
        String jwtToken = jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);
        return AuthenticationResponse.builder()
                .token(jwtToken)
                .refreshToken(refreshToken)
                .roles(user.getRole())
                .build();
    }

    public boolean verify(String verificationCode) {
        AppUser user = repository.findByVerificationCode(verificationCode)
                .orElse(null);

        if (user == null) {
            return false;
        }

        user.setVerificationCode(null);
        user.setEnabled(true);
        repository.save(user);

        return true;

    }


    public AuthenticationResponse refreshToken(
            HttpServletRequest request) {
        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        final String refreshToken;
        final String userEmail;
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }
        refreshToken = authHeader.substring(7);
        userEmail = jwtService.extractUsername(refreshToken);
        if (userEmail != null) {
            AppUser user = this.repository.findByEmail(userEmail)
                    .orElseThrow(() -> new UsernameNotFoundException("Invalid token"));
            if (jwtService.isTokenValid(refreshToken, user)) {
                String jwtToken = jwtService.generateToken(user);
                return AuthenticationResponse.builder()
                        .token(jwtToken)
                        .refreshToken(refreshToken)
                        .roles(user.getRole())
                        .build();
            } else {
                SecurityContextHolder.clearContext();
                request.getSession().invalidate();
                throw new ExpiredJwtException(null, null, "Token has expired");
            }
        } else {
            throw new UsernameNotFoundException("Invalid token");
        }
    }


    public void sendResetPasswordEmail(EmailRequest emailRequest, HttpServletRequest request) {

        AppUser user = repository.findByEmail(emailRequest.getEmail()).orElseThrow(EntityExistsException::new);
        user.setVerificationCode(RandomStringUtils.random(64, true, true));
        AppUser savedUser = repository.save(user);

        String origin = request.getHeader("Origin");
        String url = origin + "/reset-password?code=" + savedUser.getVerificationCode();

        String content = "Dear [[name]], You requested to change your password. <br>"
                + "Please follow the link to reset your password:<br>"
                + "<h3><a href=\"[[URL]]\" target=\"_self\">Reset password</a></h3>"
                + "Thank you,<br>"
                + "Our team";

        content = content.replace("[[name]]", savedUser.getName());
        content = content.replace("[[URL]]", url);

        KafkaMessage dto = new EmailDTO(user.getEmail(), "Verify your account", content);

        kafkaTemplate.send("mail", dto);
    }

    public void resetPassword(ResetPasswordRequest request) {

        AppUser user = repository.findByVerificationCode(request.getCode()).orElseThrow(EntityNotFoundException::new);

        user.setVerificationCode(null);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        repository.save(user);
    }


}
