package com.cleancode.real_estate_backend.utils;

import com.cleancode.real_estate_backend.dtos.kafka.EmailDTO;
import com.cleancode.real_estate_backend.dtos.kafka.KafkaMessage;
import com.cleancode.real_estate_backend.entities.AppUser;
import jakarta.servlet.http.HttpServletRequest;

public class EmailComposer {


    public static KafkaMessage createAccountConfirmationEmail(AppUser user, HttpServletRequest httpServletRequest) {
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

        return new EmailDTO(user.getEmail(), "Verify your account", content);

    }

    public static KafkaMessage createRepresentativeAccountConfirmationEmail(AppUser user, String tenantName, String generatedPassword, HttpServletRequest httpServletRequest) {
        String origin = httpServletRequest.getHeader("Origin");
        String code = user.getVerificationCode();
        String verifyURL = origin + "/auth/signup" + "?code=" + code;

        String content = "<!DOCTYPE html>"
                + "<html lang='en'>"
                + "<head>"
                + "<meta charset='UTF-8'>"
                + "<meta name='viewport' content='width=device-width, initial-scale=1.0'>"
                + "<title>Account Confirmation</title>"
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
                + "<div class='header'>Account Confirmation for [[tenantName]]</div>"
                + "<div class='content'>"
                + "Dear [[name]],<br><br>"
                + "We are pleased to inform you that your account has been created as a representative for [[tenantName]].<br><br>"
                + "Please click the button below to verify your email address and confirm your account:<br><br>"
                + "<a href='[[URL]]' class='button'>Verify Your Email</a><br><br>"
                + "Or use the following verification code:<br>"
                + "<h3>[[CODE]]</h3><br>"
                + "Your temporary password is: <strong>[[PASSWORD]]</strong><br><br>"
                + "<strong>Please note:</strong> After logging in for the first time, you will be required to reset your password.<br><br>"
                + "Thank you for being a part of [[tenantName]].<br><br>"
                + "Best regards,<br>"
                + "Our team"
                + "</div>"
                + "<div class='footer'>If you did not expect to receive this email, please contact us immediately.</div>"
                + "</div>"
                + "</body>"
                + "</html>";

        content = content.replace("[[name]]", user.getName());
        content = content.replace("[[CODE]]", code);
        content = content.replace("[[URL]]", verifyURL);
        content = content.replace("[[PASSWORD]]", generatedPassword);
        content = content.replace("[[tenantName]]", tenantName);

        return new EmailDTO(user.getEmail(), "Account Confirmation for " + tenantName, content);
    }



}
