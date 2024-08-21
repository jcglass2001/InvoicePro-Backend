package com.jcg.bankingapi.service.implementation;

import com.jcg.bankingapi.domain.enums.VerificationType;
import com.jcg.bankingapi.exception.ApiException;
import com.jcg.bankingapi.service.EmailService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {
    private final JavaMailSender mailSender;
    @Override
    public void sendVerificationEmail(String firstName, String email, String verificationUrl, VerificationType verificationType) {
        try{
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("jcglass2001@gmail.com");
            message.setTo(email);
            message.setText(getEmailMessage(firstName, verificationUrl, verificationType));
            message.setSubject(String.format("InvoicePro - %s Verification Email", StringUtils.capitalize(verificationType.getType())));
            mailSender.send(message);
            log.info("Email sent to user {}", firstName);
        } catch (Exception exception) {
            log.error(exception.getMessage());
        }

    }

    private String getEmailMessage(String firstName, String verificationUrl, VerificationType verificationType) {
        switch (verificationType){
            case PASSWORD -> { return String.format("Hello %s,\n\nReset password request. Please click the link below to reset your password.\n\n%s\n\n - InvoicePro Support Team", firstName, verificationUrl);}
            case ACCOUNT -> { return String.format("Hello %s,\n\nYour new account has been created. Please click the link below to verify your account.\n\n%s\n\n - InvoicePro Support Team", firstName, verificationUrl); }
            default -> throw new ApiException("Unable to send email. Email type unknown.");
        }
    }
}
