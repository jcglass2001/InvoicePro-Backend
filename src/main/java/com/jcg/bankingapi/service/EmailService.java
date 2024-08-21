package com.jcg.bankingapi.service;

import com.jcg.bankingapi.domain.enums.VerificationType;

public interface EmailService {
    void sendVerificationEmail(String firstName, String email, String verificationUrl, VerificationType verificationType);
}
