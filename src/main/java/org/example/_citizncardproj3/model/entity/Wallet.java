package org.example._citizncardproj3.model.entity;

import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "wallets")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Wallet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long walletId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", unique = true, nullable = false)
    private Member member;

    @Column(nullable = false)
    private Double balance;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WalletType walletType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WalletStatus status;

    @OneToMany(mappedBy = "wallet", cascade = CascadeType.ALL)
    private List<Transaction> transactions;

    private Integer pointsBalance;

    private LocalDateTime lastTransactionTime;

    private String freezeReason;

    private LocalDateTime freezeTime;

    private LocalDateTime unfreezeTime;

    @Column(nullable = false)
    private Double dailyTransactionLimit;

    @Column(nullable = false)
    private Double monthlyTransactionLimit;

    private Double dailyTransactionAmount;

    private Double monthlyTransactionAmount;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Column(nullable = false)
    private Boolean isDeleted;

    // 錢包類型枚舉
    @Getter
    public enum WalletType {
        GENERAL("一般錢包"),
        POINTS("點數錢包"),
        STUDENT("學生錢包"),
        SENIOR("敬老錢包");

        private final String description;

        WalletType(String description) {
            this.description = description;
        }

    }

    // 錢包狀態枚舉
    @Getter
    public enum WalletStatus {
        ACTIVE("正常"),
        FROZEN("凍結"),
        SUSPENDED("停用"),
        CLOSED("已關閉");

        private final String description;

        WalletStatus(String description) {
            this.description = description;
        }

    }

    // 初始化方法
    @PrePersist
    public void prePersist() {
        if (this.isDeleted == null) {
            this.isDeleted = false;
        }
        if (this.status == null) {
            this.status = WalletStatus.ACTIVE;
        }
        if (this.balance == null) {
            this.balance = 0.0;
        }
        if (this.pointsBalance == null) {
            this.pointsBalance = 0;
        }
        if (this.dailyTransactionLimit == null) {
            this.dailyTransactionLimit = 50000.0;
        }
        if (this.monthlyTransactionLimit == null) {
            this.monthlyTransactionLimit = 500000.0;
        }
        if (this.dailyTransactionAmount == null) {
            this.dailyTransactionAmount = 0.0;
        }
        if (this.monthlyTransactionAmount == null) {
            this.monthlyTransactionAmount = 0.0;
        }
    }

    // 業務方法

    // 增加餘額
    public void addBalance(Double amount) {
        if (this.status != WalletStatus.ACTIVE) {
            throw new IllegalStateException("錢包狀態不正常，無法增加餘額");
        }
        this.balance += amount;
        this.lastTransactionTime = LocalDateTime.now();
    }

    // 扣除餘額
    public void subtractBalance(Double amount) {
        if (this.status != WalletStatus.ACTIVE) {
            throw new IllegalStateException("錢包狀態不正常，無法扣除餘額");
        }
        if (this.balance < amount) {
            throw new IllegalStateException("餘額不足");
        }
        this.balance -= amount;
        this.lastTransactionTime = LocalDateTime.now();
        updateTransactionLimits(amount);
    }

    // 更新交易限額
    private void updateTransactionLimits(Double amount) {
        this.dailyTransactionAmount += amount;
        this.monthlyTransactionAmount += amount;

        if (this.dailyTransactionAmount > this.dailyTransactionLimit) {
            throw new IllegalStateException("已超過每日交易限額");
        }
        if (this.monthlyTransactionAmount > this.monthlyTransactionLimit) {
            throw new IllegalStateException("已超過每月交易限額");
        }
    }

    // 凍結錢包
    public void freeze(String reason) {
        if (this.status == WalletStatus.ACTIVE) {
            this.status = WalletStatus.FROZEN;
            this.freezeReason = reason;
            this.freezeTime = LocalDateTime.now();
        } else {
            throw new IllegalStateException("只有正常狀態的錢包可以凍結");
        }
    }

    // 解凍錢包
    public void unfreeze() {
        if (this.status == WalletStatus.FROZEN) {
            this.status = WalletStatus.ACTIVE;
            this.unfreezeTime = LocalDateTime.now();
        } else {
            throw new IllegalStateException("只有凍結狀態的錢包可以解凍");
        }
    }

    // 增加點數
    public void addPoints(Integer points) {
        if (this.status == WalletStatus.ACTIVE) {
            this.pointsBalance += points;
        } else {
            throw new IllegalStateException("錢包狀態不正常，無法增加點數");
        }
    }

    // 使用點數
    public void usePoints(Integer points) {
        if (this.status != WalletStatus.ACTIVE) {
            throw new IllegalStateException("錢包狀態不正常，無法使用點數");
        }
        if (this.pointsBalance < points) {
            throw new IllegalStateException("點數不足");
        }
        this.pointsBalance -= points;
    }

    // 重設每日交易金額
    public void resetDailyTransactionAmount() {
        this.dailyTransactionAmount = 0.0;
    }

    // 重設每月交易金額
    public void resetMonthlyTransactionAmount() {
        this.monthlyTransactionAmount = 0.0;
    }

    // 檢查錢包是否可用
    public boolean isUsable() {
        return this.status == WalletStatus.ACTIVE &&
                !this.isDeleted;
    }

    // 檢查餘額是否足夠
    public boolean hasEnoughBalance(Double amount) {
        return this.balance >= amount;
    }

    // 用於日誌記錄的方法
    public String toLogString() {
        return String.format("Wallet{id=%d, type=%s, status=%s, balance=%.2f}",
                walletId, walletType, status, balance);
    }
}