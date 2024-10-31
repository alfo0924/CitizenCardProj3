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
@Table(name = "virtual_cards")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VirtualCard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long virtualCardId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", unique = true, nullable = false)
    private Member member;

    @Column(unique = true)
    private String virtualCardNumber;

    @Column(nullable = false)
    private String boundPhoneNumber;

    @Column(length = 100)
    private String deviceId;

    @Column(length = 100)
    private String deviceName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CardStatus status;

    private LocalDateTime lastUsedTime;

    private LocalDateTime boundTime;

    private LocalDateTime unboundTime;

    private String unboundReason;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Column(nullable = false)
    private Boolean isDeleted;

    // 卡片狀態枚舉
    public enum CardStatus {
        ACTIVE("有效"),
        INACTIVE("未啟用"),
        SUSPENDED("已停用"),
        UNBOUND("已解綁");

        private final String description;

        CardStatus(String description) {
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
        if (this.status == null) {
            this.status = CardStatus.INACTIVE;
        }
        if (this.virtualCardNumber == null) {
            this.virtualCardNumber = generateVirtualCardNumber();
        }
    }

    // 生成虛擬卡號
    private String generateVirtualCardNumber() {
        return "VC" + System.currentTimeMillis();
    }

    // 業務方法

    // 綁定設備
    public void bindDevice(String deviceId, String deviceName) {
        if (this.status != CardStatus.INACTIVE && this.status != CardStatus.UNBOUND) {
            throw new IllegalStateException("只有未啟用或已解綁的虛擬卡可以綁定設備");
        }
        this.deviceId = deviceId;
        this.deviceName = deviceName;
        this.boundTime = LocalDateTime.now();
        this.status = CardStatus.ACTIVE;
    }

    // 解綁設備
    public void unbindDevice(String reason) {
        if (this.status == CardStatus.ACTIVE) {
            this.status = CardStatus.UNBOUND;
            this.unboundTime = LocalDateTime.now();
            this.unboundReason = reason;
            this.deviceId = null;
            this.deviceName = null;
        } else {
            throw new IllegalStateException("只有已啟用的虛擬卡可以解綁設備");
        }
    }

    // 啟用虛擬卡
    public void activate() {
        if (this.status == CardStatus.INACTIVE && this.deviceId != null) {
            this.status = CardStatus.ACTIVE;
        } else {
            throw new IllegalStateException("虛擬卡必須先綁定設備才能啟用");
        }
    }

    // 停用虛擬卡
    public void suspend(String reason) {
        if (this.status == CardStatus.ACTIVE) {
            this.status = CardStatus.SUSPENDED;
            this.unboundReason = reason;
        } else {
            throw new IllegalStateException("只有已啟用的虛擬卡可以停用");
        }
    }

    // 更新使用時間
    public void updateLastUsedTime() {
        this.lastUsedTime = LocalDateTime.now();
    }

    // 更新綁定手機號碼
    public void updateBoundPhoneNumber(String newPhoneNumber) {
        if (this.status != CardStatus.SUSPENDED) {
            this.boundPhoneNumber = newPhoneNumber;
        } else {
            throw new IllegalStateException("停用狀態的虛擬卡不能更新手機號碼");
        }
    }

    // 檢查虛擬卡是否可用
    public boolean isUsable() {
        return this.status == CardStatus.ACTIVE &&
                !this.isDeleted &&
                this.deviceId != null;
    }

    // 檢查是否需要重新綁定
    public boolean needsRebinding() {
        return this.status == CardStatus.UNBOUND ||
                this.deviceId == null;
    }

    // 驗證設備
    public boolean validateDevice(String deviceId) {
        return this.deviceId != null &&
                this.deviceId.equals(deviceId);
    }

    // 用於日誌記錄的方法
    public String toLogString() {
        return String.format("VirtualCard{id=%d, number='%s', status=%s, device='%s'}",
                virtualCardId, virtualCardNumber, status, deviceName);
    }
}