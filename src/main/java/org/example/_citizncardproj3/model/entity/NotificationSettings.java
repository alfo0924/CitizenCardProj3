package org.example._citizncardproj3.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notification_settings")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long settingsId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    /**
     * 電子郵件通知
     */
    @Column(name = "email_notification", nullable = false)
    private boolean emailNotification = true;

    /**
     * 簡訊通知
     */
    @Column(name = "sms_notification", nullable = false)
    private boolean smsNotification = true;

    /**
     * 推播通知
     */
    @Column(name = "push_notification", nullable = false)
    private boolean pushNotification = true;

    /**
     * 行銷通知
     */
    @Column(name = "marketing_notification", nullable = false)
    private boolean marketingNotification = false;

    /**
     * 最後通知時間
     */
    @Column(name = "last_notification_time")
    private LocalDateTime lastNotificationTime;

    /**
     * 通知語言
     */
    @Column(name = "notification_language", length = 10)
    private String notificationLanguage = "zh-TW";

    /**
     * 每日通知時段開始
     */
    @Column(name = "daily_start_time")
    private Integer dailyStartTime = 9; // 9:00

    /**
     * 每日通知時段結束
     */
    @Column(name = "daily_end_time")
    private Integer dailyEndTime = 21; // 21:00

    /**
     * 是否啟用勿擾模式
     */
    @Column(name = "do_not_disturb")
    private boolean doNotDisturb = false;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    /**
     * 初始化方法
     */
    @PrePersist
    public void prePersist() {
        if (this.notificationLanguage == null) {
            this.notificationLanguage = "zh-TW";
        }
        if (this.dailyStartTime == null) {
            this.dailyStartTime = 9;
        }
        if (this.dailyEndTime == null) {
            this.dailyEndTime = 21;
        }
    }

    /**
     * 更新最後通知時間
     */
    public void updateLastNotificationTime() {
        this.lastNotificationTime = LocalDateTime.now();
    }

    /**
     * 檢查是否在通知時段內
     */
    public boolean isInNotificationPeriod() {
        if (doNotDisturb) {
            return false;
        }

        int currentHour = LocalDateTime.now().getHour();
        return currentHour >= dailyStartTime && currentHour < dailyEndTime;
    }

    /**
     * 檢查是否需要發送通知
     */
    public boolean needsNotification(LocalDateTime checkTime) {
        if (lastNotificationTime == null) {
            return true;
        }
        return lastNotificationTime.isBefore(checkTime) && isInNotificationPeriod();
    }

    /**
     * 啟用所有通知
     */
    public void enableAllNotifications() {
        this.emailNotification = true;
        this.smsNotification = true;
        this.pushNotification = true;
        this.marketingNotification = true;
        this.doNotDisturb = false;
    }

    /**
     * 停用所有通知
     */
    public void disableAllNotifications() {
        this.emailNotification = false;
        this.smsNotification = false;
        this.pushNotification = false;
        this.marketingNotification = false;
        this.doNotDisturb = true;
    }

    /**
     * 設置通知時段
     */
    public void setNotificationPeriod(Integer startTime, Integer endTime) {
        if (startTime < 0 || startTime > 23 || endTime < 0 || endTime > 23) {
            throw new IllegalArgumentException("時間必須在0-23之間");
        }
        if (startTime >= endTime) {
            throw new IllegalArgumentException("開始時間必須早於結束時間");
        }
        this.dailyStartTime = startTime;
        this.dailyEndTime = endTime;
    }

    /**
     * 檢查特定類型的通知是否啟用
     */
    public boolean isNotificationEnabled(NotificationType type) {
        if (doNotDisturb) {
            return false;
        }

        switch (type) {
            case EMAIL:
                return emailNotification;
            case SMS:
                return smsNotification;
            case PUSH:
                return pushNotification;
            case MARKETING:
                return marketingNotification;
            default:
                return false;
        }
    }

    /**
     * 通知類型枚舉
     */
    public enum NotificationType {
        EMAIL,
        SMS,
        PUSH,
        MARKETING
    }
}