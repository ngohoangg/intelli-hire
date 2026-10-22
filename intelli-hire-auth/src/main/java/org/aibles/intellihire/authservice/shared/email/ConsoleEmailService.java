package org.aibles.intellihire.authservice.shared.email;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@Profile({"dev", "default"})
public class ConsoleEmailService implements EmailService {

    @Value("${application.email.base-url:http://localhost:4200}")
    private String baseUrl;

    @Override
    public void sendPasswordResetEmail(String to, String token) {
        String resetUrl = baseUrl + "/auth/reset-password?token=" + token;
        log.info("========== PASSWORD RESET EMAIL ==========");
        log.info("To: {}", to);
        log.info("Token: {}", token);
        log.info("Reset URL: {}", resetUrl);
        log.info("==========================================");
    }

    @Override
    public void sendEmailVerificationEmail(String to, String token) {
        String verifyUrl = baseUrl + "/auth/verify-email?token=" + token;
        log.info("========== EMAIL VERIFICATION ==========");
        log.info("To: {}", to);
        log.info("Token: {}", token);
        log.info("Verify URL: {}", verifyUrl);
        log.info("========================================");
    }
}
