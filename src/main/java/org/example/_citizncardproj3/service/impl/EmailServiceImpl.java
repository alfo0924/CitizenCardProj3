package org.example._citizncardproj3.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example._citizncardproj3.service.EmailService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.url}")
    private String appUrl;

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Async
    @Override
    public void sendVerificationEmail(String email, String token) {
        String subject = "請驗證您的電子郵件";
        String verificationLink = appUrl + "/verify?token=" + token;
        String content = String.format("""
                <h2>歡迎加入市民卡</h2>
                <p>請點擊以下連結驗證您的電子郵件：</p>
                <a href="%s">驗證電子郵件</a>
                <p>此連結將在 168 小時後失效</p>
                <p>如果您沒有註冊市民卡帳號，請忽略此郵件。</p>
                """, verificationLink);

        sendEmail(email, subject, content);
    }

    @Async
    @Override
    public void sendPasswordResetEmail(String email, String token) {
        String subject = "密碼重設請求";
        String resetLink = appUrl + "/reset-password?token=" + token;
        String content = String.format("""
                <h2>密碼重設請求</h2>
                <p>請點擊以下連結重設您的密碼：</p>
                <a href="%s">重設密碼</a>
                <p>此連結將在 24 小時後失效</p>
                <p>如果您沒有要求重設密碼，請忽略此郵件。</p>
                """, resetLink);

        sendEmail(email, subject, content);
    }

    @Async
    @Override
    public void sendPasswordChangeNotification(String email) {
        String subject = "密碼已變更通知";
        String securityLink = appUrl + "/security-settings";
        String content = String.format("""
                <h2>密碼已變更通知</h2>
                <p>您的密碼已於 %s 變更成功。</p>
                <p>如果這不是您本人操作，請立即：</p>
                <ol>
                    <li><a href="%s">檢查帳號安全設定</a></li>
                    <li>聯絡客服：support@citizencard.com</li>
                </ol>
                """,
                LocalDateTime.now().format(DATE_FORMATTER),
                securityLink);

        sendEmail(email, subject, content);
    }

    @Async
    @Override
    public void sendBookingConfirmation(String email, String bookingNumber) {
        String subject = "訂票成功通知";
        String bookingLink = appUrl + "/bookings/" + bookingNumber;
        String content = String.format("""
                <h2>訂票成功通知</h2>
                <p>您的訂票編號：%s</p>
                <p>訂票時間：%s</p>
                <p><a href="%s">查看訂票詳情</a></p>
                """,
                bookingNumber,
                LocalDateTime.now().format(DATE_FORMATTER),
                bookingLink);

        sendEmail(email, subject, content);
    }

    @Override
    public void sendBookingCancellation(String email, String bookingNumber, String reason) {

    }

    @Override
    public void sendBookingReminder(String email, String bookingNumber, String movieName, String showTime) {

    }

    @Override
    public void sendLoginNotification(String email, String ipAddress, String deviceInfo) {

    }

    @Override
    public void sendAccountLockNotification(String email, String reason) {

    }

    @Override
    public void sendAccountUnlockNotification(String email) {

    }

    @Override
    public void sendWelcomeEmail(String email, String name) {

    }

    // ... 其他郵件發送方法使用類似的模式 ...

    // 私有輔助方法
    private void sendEmail(String to, String subject, String htmlContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Email sent successfully to: {}, subject: {}", to, subject);
        } catch (MessagingException e) {
            log.error("Failed to send email to: {}, subject: {}", to, subject, e);
            throw new RuntimeException("Failed to send email", e);
        }
    }
}