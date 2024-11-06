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
@Table(name = "NotificationSettings")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "SettingsID")
    private Long settingsId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MemberID", nullable = false)
    private Member member;

    @Column(name = "EmailNotification", nullable = false)
    private Boolean emailNotification = true;

    @Column(name = "SMSNotification", nullable = false)
    private Boolean smsNotification = true;

    @Column(name = "PushNotification", nullable = false)
    private Boolean pushNotification = true;

    @Column(name = "MarketingNotification", nullable = false)
    private Boolean marketingNotification = false;

    @Column(name = "LastNotificationTime")
    private LocalDateTime lastNotificationTime;

    @Column(name = "NotificationLanguage", length = 10)
    private String notificationLanguage = "zh-TW";

    @Column(name = "DailyStartTime")
    private Integer dailyStartTime = 9;

    @Column(name = "DailyEndTime")
    private Integer dailyEndTime = 21;

    @Column(name = "DoNotDisturb", nullable = false)
    private Boolean doNotDisturb = false;

    @CreationTimestamp
    @Column(name = "CreatedAt", nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "UpdatedAt", nullable = false)
    private LocalDateTime updatedAt;

    // 初始化方法
    @PrePersist
    public void prePersist() {
        if (this.emailNotification == null) {
            this.emailNotification = true;
        }
        if (this.smsNotification == null) {
            this.smsNotification = true;
        }
        if (this.pushNotification == null) {
            this.pushNotification = true;
        }
        if (this.marketingNotification == null) {
            this.marketingNotification = false;
        }
        if (this.notificationLanguage == null) {
            this.notificationLanguage = "zh-TW";
        }
        if (this.dailyStartTime == null) {
            this.dailyStartTime = 9;
        }
        if (this.dailyEndTime == null) {
            this.dailyEndTime = 21;
        }
        if (this.doNotDisturb == null) {
            this.doNotDisturb = false;
        }
    }

    // 業務方法
    public void updateLastNotificationTime() {
        this.lastNotificationTime = LocalDateTime.now();
    }

    public boolean isInNotificationPeriod() {
        if (Boolean.TRUE.equals(doNotDisturb)) {
            return false;
        }
        int currentHour = LocalDateTime.now().getHour();
        return currentHour >= dailyStartTime && currentHour < dailyEndTime;
    }

    public boolean needsNotification(LocalDateTime checkTime) {
        if (lastNotificationTime == null) {
            return true;
        }
        return lastNotificationTime.isBefore(checkTime) && isInNotificationPeriod();
    }

    public void enableAllNotifications() {
        this.emailNotification = true;
        this.smsNotification = true;
        this.pushNotification = true;
        this.marketingNotification = true;
        this.doNotDisturb = false;
    }

    public void disableAllNotifications() {
        this.emailNotification = false;
        this.smsNotification = false;
        this.pushNotification = false;
        this.marketingNotification = false;
        this.doNotDisturb = true;
    }

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

    public boolean isNotificationEnabled(NotificationType type) {
        if (Boolean.TRUE.equals(doNotDisturb)) {
            return false;
        }

        return switch (type) {
            case EMAIL -> Boolean.TRUE.equals(emailNotification);
            case SMS -> Boolean.TRUE.equals(smsNotification);
            case PUSH -> Boolean.TRUE.equals(pushNotification);
            case MARKETING -> Boolean.TRUE.equals(marketingNotification);
        };
    }

    public enum NotificationType {
        EMAIL,
        SMS,
        PUSH,
        MARKETING
    }
}