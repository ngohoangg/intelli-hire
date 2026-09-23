package org.aibles.intellihire.authservice.shared.email;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aibles.intellihire.authservice.shared.config.EmailProperties;
import org.springframework.context.annotation.Profile;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@Profile("prod")
@RequiredArgsConstructor
public class SmtpEmailService implements EmailService {

    private final JavaMailSender mailSender;
    private final EmailProperties emailProperties;

    @Override
    public void sendPasswordResetEmail(String to, String token) {
        String resetUrl = emailProperties.getBaseUrl() + "/auth/reset-password?token=" + token;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(emailProperties.getFrom());
        message.setTo(to);
        message.setSubject("IntelliHire - Password Reset Request");
        message.setText(
                "You have requested to reset your password.\n\n" +
                "Click the link below to reset your password:\n" +
                resetUrl + "\n\n" +
                "This link will expire in 1 hour.\n\n" +
                "If you did not request this, please ignore this email."
        );

        mailSender.send(message);
        log.info("Password reset email sent to: {}", to);
    }

    @Override
    public void sendEmailVerificationEmail(String to, String token) {
        String verifyUrl = emailProperties.getBaseUrl() + "/auth/verify-email?token=" + token;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(emailProperties.getFrom());
        message.setTo(to);
        message.setSubject("IntelliHire - Verify Your Email");
        message.setText(
                "Welcome to IntelliHire!\n\n" +
                "Please verify your email by clicking the link below:\n" +
                verifyUrl + "\n\n" +
                "This link will expire in 24 hours.\n\n" +
                "If you did not create an account, please ignore this email."
        );

        mailSender.send(message);
        log.info("Email verification sent to: {}", to);
    }
}
