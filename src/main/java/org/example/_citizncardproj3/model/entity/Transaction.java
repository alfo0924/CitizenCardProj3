package org.example._citizncardproj3.model.entity;

import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import javax.swing.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {


    @Setter
    @Getter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @Setter
    @Getter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_id")
    private Member recipient;  // 用於轉帳交易的接收方

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long transactionId;

    @Column(unique = true, nullable = false)
    private String transactionNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wallet_id", nullable = false)
    private Wallet wallet;

    @Column(nullable = false)
    private Double amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMethod paymentMethod;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id")
    private Booking relatedBooking;

    @Column(length = 255)
    private String description;

    @Column(length = 100)
    private String referenceNumber;

    private String paymentReference;

    @Column(nullable = false)
    private LocalDateTime transactionTime;

    private LocalDateTime completionTime;

    private LocalDateTime cancellationTime;

    private String cancellationReason;

    @Column(length = 50)
    private String processedBy;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Column(nullable = false)
    private Boolean isDeleted;



    // 交易類型枚舉
    @Getter
    public enum TransactionType {
        TOP_UP("儲值"),
        PAYMENT("支付"),
        REFUND("退款"),
        TRANSFER("轉帳"),
        ADJUSTMENT("調整");

        private final String description;

        TransactionType(String description) {
            this.description = description;
        }

    }

    // 交易狀態枚舉
    public enum TransactionStatus {
        PENDING("處理中"),
        COMPLETED("完成"),
        FAILED("失敗"),
        CANCELLED("已取消"),
        REFUNDED("已退款");

        private final String description;

        TransactionStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    // 支付方式枚舉
    public enum PaymentMethod {
        CREDIT_CARD("信用卡"),
        DEBIT_CARD("金融卡"),
        BANK_TRANSFER("銀行轉帳"),
        MOBILE_PAYMENT("行動支付"),
        E_WALLET("電子錢包"),
        CASH("現金"),
        LINE_PAY("LINE Pay"),
        JKO_PAY("街口支付"),
        APPLE_PAY("Apple Pay"),
        GOOGLE_PAY("Google Pay"),
        WALLET_BALANCE("錢包餘額");  // 添加錢包餘額支付方式

        private final String description;

        PaymentMethod(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }

        public static PaymentMethod fromString(String value) {
            try {
                return PaymentMethod.valueOf(value.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("不支援的支付方式: " + value);
            }
        }

        public boolean isCardPayment() {
            return this == CREDIT_CARD || this == DEBIT_CARD;
        }

        public boolean isMobilePayment() {
            return this == MOBILE_PAYMENT ||
                    this == LINE_PAY ||
                    this == JKO_PAY ||
                    this == APPLE_PAY ||
                    this == GOOGLE_PAY;
        }

        public boolean isWalletPayment() {
            return this == WALLET_BALANCE || this == E_WALLET;
        }

        public boolean needsBankCode() {
            return this == BANK_TRANSFER;
        }
    }

    // 初始化方法
    @PrePersist
    public void prePersist() {
        if (this.isDeleted == null) {
            this.isDeleted = false;
        }
        if (this.status == null) {
            this.status = TransactionStatus.PENDING;
        }
        if (this.transactionTime == null) {
            this.transactionTime = LocalDateTime.now();
        }
        if (this.transactionNumber == null) {
            this.transactionNumber = generateTransactionNumber();
        }
    }

    // 生成交易編號
    private String generateTransactionNumber() {
        return "TX" + System.currentTimeMillis();
    }

    // 業務方法

    // 完成交易
    public void complete() {
        if (this.status == TransactionStatus.PENDING) {
            this.status = TransactionStatus.COMPLETED;
            this.completionTime = LocalDateTime.now();
            updateWalletBalance();
        } else {
            throw new IllegalStateException("只有處理中的交易可以完成");
        }
    }

    // 取消交易
    public void cancel(String reason) {
        if (this.status == TransactionStatus.PENDING) {
            this.status = TransactionStatus.CANCELLED;
            this.cancellationTime = LocalDateTime.now();
            this.cancellationReason = reason;
        } else {
            throw new IllegalStateException("只有處理中的交易可以取消");
        }
    }

    // 退款
    public void refund() {
        if (this.status == TransactionStatus.COMPLETED) {
            this.status = TransactionStatus.REFUNDED;
            createRefundTransaction();
        } else {
            throw new IllegalStateException("只有已完成的交易可以退款");
        }
    }

    // 更新錢包餘額
    private void updateWalletBalance() {
        switch (this.type) {
            case TOP_UP:
            case REFUND:
                wallet.addBalance(this.amount);
                break;
            case PAYMENT:
            case TRANSFER:
                wallet.subtractBalance(this.amount);
                break;
        }
    }

    // 創建退款交易
    private Transaction createRefundTransaction() {
        return Transaction.builder()
                .wallet(this.wallet)
                .amount(this.amount)
                .type(TransactionType.REFUND)
                .status(TransactionStatus.PENDING)
                .paymentMethod(PaymentMethod.WALLET_BALANCE)
                .description("退款: " + this.transactionNumber)
                .referenceNumber(this.transactionNumber)
                .build();
    }

    // 檢查交易是否可以取消
    public boolean isCancellable() {
        return this.status == TransactionStatus.PENDING;
    }

    // 檢查交易是否可以退款
    public boolean isRefundable() {
        return this.status == TransactionStatus.COMPLETED &&
                (this.type == TransactionType.PAYMENT ||
                        this.type == TransactionType.TRANSFER);
    }

    // 用於日誌記錄的方法
    public String toLogString() {
        return String.format("Transaction{id=%d, number='%s', type=%s, amount=%.2f, status=%s}",
                transactionId, transactionNumber, type, amount, status);
    }

}