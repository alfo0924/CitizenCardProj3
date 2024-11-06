package org.example._citizncardproj3.model.entity;

import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "Notifications")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "NotificationID")
    private Long notificationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MemberID", nullable = false)
    private Member member;

    @Column(name = "Title", nullable = false)
    private String title;

    @Column(name = "Content", columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "NotificationType", nullable = false)
    private NotificationType type;

    @Column(name = "IsRead", nullable = false)
    private Boolean isRead;

    @Column(name = "IsSent", nullable = false)
    private Boolean isSent;

    @Column(name = "SendTime", nullable = false)
    private LocalDateTime sendTime;

    @Column(name = "ExpireTime")
    private LocalDateTime expireTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "Priority", nullable = false)
    private NotificationPriority priority;

    @CreationTimestamp
    @Column(name = "CreatedAt", nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "UpdatedAt", nullable = false)
    private LocalDateTime updatedAt;

    // 通知類型枚舉
    @Getter
    public enum NotificationType {
        SYSTEM("系統通知"),
        BOOKING("訂票通知"),
        PAYMENT("支付通知"),
        DISCOUNT("優惠通知"),
        WALLET("錢包通知"),
        SECURITY("安全通知");

        private final String description;

        NotificationType(String description) {
            this.description = description;
        }

    }

    // 通知優先級枚舉
    @Getter
    public enum NotificationPriority {
        LOW("低"),
        MEDIUM("中"),
        HIGH("高"),
        URGENT("緊急");

        private final String description;

        NotificationPriority(String description) {
            this.description = description;
        }

    }

    // 初始化方法
    @PrePersist
    public void prePersist() {
        if (this.isRead == null) {
            this.isRead = false;
        }
        if (this.isSent == null) {
            this.isSent = false;
        }
        if (this.sendTime == null) {
            this.sendTime = LocalDateTime.now();
        }
        if (this.priority == null) {
            this.priority = NotificationPriority.MEDIUM;
        }
    }

    // 業務方法
    public void markAsRead() {
        if (!this.isRead) {
            this.isRead = true;
        }
    }

    public void markAsSent() {
        if (!this.isSent) {
            this.isSent = true;
            this.sendTime = LocalDateTime.now();
        }
    }

    public void setExpiration(LocalDateTime expireTime) {
        if (expireTime.isAfter(LocalDateTime.now())) {
            this.expireTime = expireTime;
        } else {
            throw new IllegalArgumentException("過期時間必須是未來時間");
        }
    }

    public boolean isExpired() {
        return this.expireTime != null &&
                LocalDateTime.now().isAfter(this.expireTime);
    }

    public boolean requiresImmediate() {
        return this.priority == NotificationPriority.URGENT ||
                this.priority == NotificationPriority.HIGH;
    }

    public void updateContent(String title, String content) {
        if (!this.isRead) {
            this.title = title;
            this.content = content;
        } else {
            throw new IllegalStateException("已讀的通知不能修改內容");
        }
    }

    public boolean isVisible() {
        return !this.isExpired() &&
                (this.expireTime == null || LocalDateTime.now().isBefore(this.expireTime));
    }

    @Override
    public String toString() {
        return String.format("Notification{id=%d, type=%s, priority=%s, title='%s', isRead=%s}",
                notificationId, type, priority, title, isRead);
    }
}