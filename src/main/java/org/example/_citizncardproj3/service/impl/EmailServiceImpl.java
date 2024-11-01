package org.example._citizncardproj3.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example._citizncardproj3.service.EmailService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.url}")
    private String appUrl;

    @Async
    @Override
    public void sendVerificationEmail(String email, String token) {
        String subject = "請驗證您的電子郵件";
        String template = "verification";
        Map<String, Object> variables = Map.of(
                "verificationLink", appUrl + "/verify?token=" + token,
                "expiryHours", 168,
                "currentTime", LocalDateTime.now()
        );
        sendEmail(email, subject, template, variables);
    }

    @Async
    @Override
    public void sendPasswordResetEmail(String email, String token) {
        String subject = "密碼重設請求";
        String template = "password-reset";
        Map<String, Object> variables = Map.of(
                "resetLink", appUrl + "/reset-password?token=" + token,
                "expiryHours", 24,
                "currentTime", LocalDateTime.now()
        );
        sendEmail(email, subject, template, variables);
    }

    @Async
    @Override
    public void sendPasswordChangeNotification(String email) {
        String subject = "密碼已變更通知";
        String template = "password-changed";
        Map<String, Object> variables = Map.of(
                "supportEmail", "support@citizencard.com",
                "changeTime", LocalDateTime.now(),
                "securityLink", appUrl + "/security-settings"
        );
        sendEmail(email, subject, template, variables);
    }

    @Async
    @Override
    public void sendBookingConfirmation(String email, String bookingNumber) {
        String subject = "訂票成功通知";
        String template = "booking-confirmation";
        Map<String, Object> variables = Map.of(
                "bookingNumber", bookingNumber,
                "bookingLink", appUrl + "/bookings/" + bookingNumber,
                "bookingTime", LocalDateTime.now()
        );
        sendEmail(email, subject, template, variables);
    }

    @Async
    @Override
    public void sendBookingCancellation(String email, String bookingNumber, String reason) {
        String subject = "訂票取消通知";
        String template = "booking-cancellation";
        Map<String, Object> variables = Map.of(
                "bookingNumber", bookingNumber,
                "reason", reason,
                "cancelTime", LocalDateTime.now(),
                "supportEmail", "support@citizencard.com"
        );
        sendEmail(email, subject, template, variables);
    }

    @Async
    @Override
    public void sendBookingReminder(String email, String bookingNumber,
                                    String movieName, String showTime) {
        String subject = "觀影提醒";
        String template = "booking-reminder";
        Map<String, Object> variables = Map.of(
                "bookingNumber", bookingNumber,
                "movieName", movieName,
                "showTime", showTime,
                "bookingLink", appUrl + "/bookings/" + bookingNumber,
                "currentTime", LocalDateTime.now()
        );
        sendEmail(email, subject, template, variables);
    }

    @Async
    @Override
    public void sendLoginNotification(String email, String ipAddress, String deviceInfo) {
        String subject = "新登入通知";
        String template = "login-notification";
        Map<String, Object> variables = Map.of(
                "ipAddress", ipAddress,
                "deviceInfo", deviceInfo,
                "loginTime", LocalDateTime.now(),
                "securityLink", appUrl + "/security-settings"
        );
        sendEmail(email, subject, template, variables);
    }

    @Async
    @Override
    public void sendAccountLockNotification(String email, String reason) {
        String subject = "帳號已被鎖定";
        String template = "account-locked";
        Map<String, Object> variables = Map.of(
                "reason", reason,
                "supportEmail", "support@citizencard.com",
                "lockTime", LocalDateTime.now(),
                "unlockLink", appUrl + "/unlock-account"
        );
        sendEmail(email, subject, template, variables);
    }

    @Async
    @Override
    public void sendAccountUnlockNotification(String email) {
        String subject = "帳號已解鎖";
        String template = "account-unlocked";
        Map<String, Object> variables = Map.of(
                "unlockTime", LocalDateTime.now(),
                "supportEmail", "support@citizencard.com",
                "securityLink", appUrl + "/security-settings"
        );
        sendEmail(email, subject, template, variables);
    }

    @Async
    @Override
    public void sendWelcomeEmail(String email, String name) {
        String subject = "歡迎加入市民卡";
        String template = "welcome";
        Map<String, Object> variables = Map.of(
                "name", name,
                "supportEmail", "support@citizencard.com",
                "profileLink", appUrl + "/profile",
                "currentTime", LocalDateTime.now()
        );
        sendEmail(email, subject, template, variables);
    }

    // 私有輔助方法
    private void sendEmail(String to, String subject, String template,
                           Map<String, Object> variables) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            Context context = new Context();
            variables.forEach(context::setVariable);

            String htmlContent = templateEngine.process(template, context);

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Email sent successfully to: {}, template: {}", to, template);
        } catch (MessagingException e) {
            log.error("Failed to send email to: {}, template: {}", to, template, e);
            throw new RuntimeException("Failed to send email", e);
        }
    }
}