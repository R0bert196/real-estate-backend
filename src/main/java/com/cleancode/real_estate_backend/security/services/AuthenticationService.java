package com.cleancode.real_estate_backend.security.services;

import com.cleancode.real_estate_backend.dtos.TestRequest;
import com.cleancode.real_estate_backend.dtos.auth.*;
import com.cleancode.real_estate_backend.dtos.kafka.EmailDTO;
import com.cleancode.real_estate_backend.dtos.kafka.KafkaMessage;
import com.cleancode.real_estate_backend.entities.AppUser;
import com.cleancode.real_estate_backend.enums.Role;
import com.cleancode.real_estate_backend.repositories.AppUserRepository;
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
        sendVerificationEmail(user, httpServletRequest);
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

    private void sendVerificationEmail(AppUser user, HttpServletRequest httpServletRequest) {
        String origin = httpServletRequest.getHeader("Origin");
        String code = user.getVerificationCode();
        String verifyURL = origin + "/auth/signup" + "?code=" + code;

        String content = "<!DOCTYPE html>"
                + "<html lang='en'>"
                + "<head>"
                + "<meta charset='UTF-8'>"
                + "<meta name='viewport' content='width=device-width, initial-scale=1.0'>"
                + "<title>Verify your account</title>"
                + "<style>"
                + "body { font-family: Arial, sans-serif; background-color: #f4f4f4; margin: 0; padding: 0; }"
                + ".container { max-width: 600px; margin: 20px auto; background-color: #ffffff; padding: 20px; border-radius: 8px; box-shadow: 0 0 10px rgba(0, 0, 0, 0.1); }"
                + ".header { font-size: 24px; font-weight: bold; color: #333333; margin-bottom: 20px; }"
                + ".content { font-size: 16px; color: #666666; line-height: 1.6; }"
                + ".button { display: inline-block; padding: 10px 20px; margin-top: 20px; background-color: #007bff; color: #ffffff; text-decoration: none; border-radius: 4px; }"
                + ".footer { margin-top: 30px; font-size: 14px; color: #999999; }"
                + "</style>"
                + "</head>"
                + "<body>"
                + "<div class='container'>"
                + "<div class='header'>Verify Your Account</div>"
                + "<div class='content'>"
                + "Dear [[name]],<br><br>"
                + "Your account is ready. Please click the button below to verify your email address:<br><br>"
                + "<a href='[[URL]]' class='button'>Verify Your Email</a><br><br>"
                + "Or use the following code:<br>"
                + "<h3>[[CODE]]</h3><br>"
                + "Thank you,<br>"
                + "Our team"
                + "</div>"
                + "<div class='footer'>If you did not create an account, please ignore this email.</div>"
                + "</div>"
                + "</body>"
                + "</html>";

        content = content.replace("[[name]]", user.getName());
        content = content.replace("[[CODE]]", code);
        content = content.replace("[[URL]]", verifyURL);

        KafkaMessage dto = new EmailDTO(user.getEmail(), "Verify your account", content);

        kafkaTemplate.send("mail", dto);
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
