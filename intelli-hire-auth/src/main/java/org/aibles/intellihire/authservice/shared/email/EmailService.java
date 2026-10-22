package org.aibles.intellihire.authservice.shared.email;

public interface EmailService {
    void sendPasswordResetEmail(String to, String token);
    void sendEmailVerificationEmail(String to, String token);
}
