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
@Table(name = "virtual_card_usages")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VirtualCardUsage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long usageId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "virtual_card_id", nullable = false)
    private VirtualCard virtualCard;

    /**
     * 使用類型
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UsageType usageType;

    /**
     * 使用時間
     */
    @CreationTimestamp
    @Column(nullable = false)
    private LocalDateTime usageTime;

    /**
     * 使用地點
     */
    @Column(length = 100)
    private String location;

    /**
     * 設備ID
     */
    @Column(length = 64)
    private String deviceId;

    /**
     * 設備名稱
     */
    @Column(length = 100)
    private String deviceName;

    /**
     * 交易金額（如果適用）
     */
    @Column(precision = 10, scale = 2)
    private Double amount;

    /**
     * 交易描述
     */
    @Column(length = 500)
    private String description;

    /**
     * IP地址
     */
    @Column(length = 45)
    private String ipAddress;

    /**
     * 使用狀態
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UsageStatus status;

    /**
     * 是否已刪除
     */
    @Column(nullable = false)
    private Boolean isDeleted;

    /**
     * 創建時間
     */
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 更新時間
     */
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    /**
     * 使用類型枚舉
     */
    public enum UsageType {
        PAYMENT("支付"),
        IDENTITY_VERIFICATION("身份驗證"),
        ACCESS_CONTROL("門禁使用"),
        TRANSPORTATION("交通使用"),
        LIBRARY("圖書館使用"),
        PARKING("停車場使用"),
        DINING("餐廳消費"),
        SHOPPING("商店消費"),
        OTHER("其他");

        private final String description;

        UsageType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 使用狀態枚舉
     */
    public enum UsageStatus {
        SUCCESS("成功"),
        FAILED("失敗"),
        PENDING("處理中"),
        CANCELLED("已取消"),
        REFUNDED("已退款"),
        EXPIRED("已過期");

        private final String description;

        UsageStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 初始化方法
     */
    @PrePersist
    public void prePersist() {
        if (this.status == null) {
            this.status = UsageStatus.PENDING;
        }
        if (this.isDeleted == null) {
            this.isDeleted = false;
        }
    }

    /**
     * 檢查使用是否成功
     */
    public boolean isSuccessful() {
        return this.status == UsageStatus.SUCCESS;
    }

    /**
     * 取消使用記錄
     */
    public void cancel(String reason) {
        if (this.status != UsageStatus.PENDING && this.status != UsageStatus.SUCCESS) {
            throw new IllegalStateException("當前狀態無法取消");
        }
        this.status = UsageStatus.CANCELLED;
        this.description = reason;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 更新狀態
     */
    public void updateStatus(UsageStatus newStatus, String description) {
        if (this.status == UsageStatus.CANCELLED || this.status == UsageStatus.REFUNDED) {
            throw new IllegalStateException("已取消或退款的記錄無法更新狀態");
        }
        this.status = newStatus;
        if (description != null) {
            this.description = description;
        }
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 退款
     */
    public void refund(String reason) {
        if (this.status != UsageStatus.SUCCESS) {
            throw new IllegalStateException("只有成功的交易可以退款");
        }
        if (this.amount == null || this.amount <= 0) {
            throw new IllegalStateException("非付費交易無法退款");
        }
        this.status = UsageStatus.REFUNDED;
        this.description = reason;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 軟刪除
     */
    public void softDelete() {
        this.isDeleted = true;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 檢查是否可編輯
     */
    public boolean isEditable() {
        return this.status == UsageStatus.PENDING;
    }

    /**
     * 檢查是否可退款
     */
    public boolean isRefundable() {
        return this.status == UsageStatus.SUCCESS &&
                this.amount != null &&
                this.amount > 0 &&
                this.usageType == UsageType.PAYMENT;
    }
}