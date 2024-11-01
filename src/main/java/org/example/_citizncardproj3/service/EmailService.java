package org.example._citizncardproj3.service;

public interface EmailService {
    /**
     * 發送驗證郵件
     */
    void sendVerificationEmail(String email, String token);

    /**
     * 發送密碼重設郵件
     */
    void sendPasswordResetEmail(String email, String token);

    /**
     * 發送密碼變更通知
     */
    void sendPasswordChangeNotification(String email);

    /**
     * 發送訂票成功通知
     */
    void sendBookingConfirmation(String email, String bookingNumber);

    /**
     * 發送訂票取消通知
     */
    void sendBookingCancellation(String email, String bookingNumber, String reason);

    /**
     * 發送訂票提醒
     */
    void sendBookingReminder(String email, String bookingNumber, String movieName, String showTime);

    /**
     * 發送登入通知
     */
    void sendLoginNotification(String email, String ipAddress, String deviceInfo);

    /**
     * 發送帳號鎖定通知
     */
    void sendAccountLockNotification(String email, String reason);

    /**
     * 發送帳號解鎖通知
     */
    void sendAccountUnlockNotification(String email);

    /**
     * 發送歡迎郵件
     */
    void sendWelcomeEmail(String email, String name);
}