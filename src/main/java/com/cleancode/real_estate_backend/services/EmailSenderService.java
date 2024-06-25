package com.cleancode.real_estate_backend.services;


import com.cleancode.real_estate_backend.entities.AppUser;
import com.cleancode.real_estate_backend.repositories.AppUserRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;

@Service
@RequiredArgsConstructor
public class EmailSenderService {

    private final JavaMailSender mailSender;

    private final AppUserRepository userRepository;

    public void sendEmailWithContent(AppUser user, String subject, String content) throws MessagingException, UnsupportedEncodingException {
        String toAddress = user.getEmail();
        String fromAddress = "sabiuta123@gmail.com";
        String senderName = "Influencers app";

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message);

        helper.setFrom(fromAddress, senderName);
        helper.setTo(toAddress);
        helper.setSubject(subject);

        helper.setText(content, true);

        mailSender.send(message);
    }






    public void sendPasswordResetEmail(AppUser user, String siteURL) throws MessagingException, UnsupportedEncodingException {
        String toAddress = user.getEmail();
        String fromAddress = "noreply@tactiqhrinsights.ro";
        String senderName = "Tactiq HR Insights";
        String subject = "Password reset";
        String content = "Dear [[name]],<br>"
                + "Please click the link below to reset your password:<br>"
                + "<h3><a href=\"[[URL]]\" target=\"_self\">VERIFY</a></h3>"
                + "Tactiq HR Insights.";

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message);

        helper.setFrom(fromAddress, senderName);
        helper.setTo(toAddress);
        helper.setSubject(subject);

        content = content.replace("[[name]]", user.getName());
        System.out.println(siteURL);

        String verifyURL = siteURL + "/reset?code=" + user.getVerificationCode();

        content = content.replace("[[URL]]", verifyURL);

        helper.setText(content, true);

        mailSender.send(message);
    }
}
