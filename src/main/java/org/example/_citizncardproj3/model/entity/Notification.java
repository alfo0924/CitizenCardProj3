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
@Table(name = "notifications")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long notificationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationPriority priority;

    @Column(nullable = false)
    private Boolean isRead;

    @Column(nullable = false)
    private Boolean isSent;

    @Column(nullable = false)
    private LocalDateTime sendTime;

    private LocalDateTime readTime;

    private LocalDateTime expireTime;

    @Column(length = 100)
    private String actionUrl;

    @Column(length = 50)
    private String referenceId;

    @Column(length = 50)
    private String referenceType;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Column(nullable = false)
    private Boolean isDeleted;

    // 通知類型枚舉
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

        public String getDescription() {
            return description;
        }
    }

    // 通知優先級枚舉
    public enum NotificationPriority {
        LOW("低"),
        MEDIUM("中"),
        HIGH("高"),
        URGENT("緊急");

        private final String description;

        NotificationPriority(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    // 初始化方法
    @PrePersist
    public void prePersist() {
        if (this.isDeleted == null) {
            this.isDeleted = false;
        }
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

    // 標記為已讀
    public void markAsRead() {
        if (!this.isRead) {
            this.isRead = true;
            this.readTime = LocalDateTime.now();
        }
    }

    // 標記為已發送
    public void markAsSent() {
        if (!this.isSent) {
            this.isSent = true;
            this.sendTime = LocalDateTime.now();
        }
    }

    // 設置過期時間
    public void setExpiration(LocalDateTime expireTime) {
        if (expireTime.isAfter(LocalDateTime.now())) {
            this.expireTime = expireTime;
        } else {
            throw new IllegalArgumentException("過期時間必須是未來時間");
        }
    }

    // 檢查是否過期
    public boolean isExpired() {
        return this.expireTime != null &&
                LocalDateTime.now().isAfter(this.expireTime);
    }

    // 檢查是否需要立即處理
    public boolean requiresImmediate() {
        return this.priority == NotificationPriority.URGENT ||
                this.priority == NotificationPriority.HIGH;
    }

    // 更新通知內容
    public void updateContent(String title, String content) {
        if (!this.isRead) {
            this.title = title;
            this.content = content;
        } else {
            throw new IllegalStateException("已讀的通知不能修改內容");
        }
    }

    // 添加操作連結
    public void addAction(String actionUrl) {
        this.actionUrl = actionUrl;
    }

    // 設置關聯引用
    public void setReference(String referenceType, String referenceId) {
        this.referenceType = referenceType;
        this.referenceId = referenceId;
    }

    // 檢查通知是否可見
    public boolean isVisible() {
        return !this.isDeleted &&
                !this.isExpired() &&
                (this.expireTime == null || LocalDateTime.now().isBefore(this.expireTime));
    }

    // 用於日誌記錄的方法
    public String toLogString() {
        return String.format("Notification{id=%d, type=%s, priority=%s, title='%s', isRead=%s}",
                notificationId, type, priority, title, isRead);
    }
}