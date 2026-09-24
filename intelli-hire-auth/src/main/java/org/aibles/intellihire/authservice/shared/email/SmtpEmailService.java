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
        message.setSubject("IntelliHire - Yêu cầu đặt lại mật khẩu");
        message.setText(
                "Bạn đã yêu cầu đặt lại mật khẩu.\n\n" +
                "Nhấp vào liên kết bên dưới để đặt lại mật khẩu của bạn:\n" +
                resetUrl + "\n\n" +
                "Liên kết này sẽ hết hạn sau 1 giờ.\n\n" +
                "Nếu bạn không yêu cầu điều này, vui lòng bỏ qua email này."
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
        message.setSubject("IntelliHire - Xác thực email của bạn");
        message.setText(
                "Chào mừng bạn đến với IntelliHire!\n\n" +
                "Vui lòng xác thực email của bạn bằng cách nhấp vào liên kết bên dưới:\n" +
                verifyUrl + "\n\n" +
                "Liên kết này sẽ hết hạn sau 24 giờ.\n\n" +
                "Nếu bạn không tạo tài khoản này, vui lòng bỏ qua email này."
        );

        mailSender.send(message);
        log.info("Email verification sent to: {}", to);
    }
}
