package org.example._citizncardproj3.model.entity;

import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "Transactions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "TransactionID")
    private Long transactionId;

    @Column(name = "TransactionNumber", nullable = false, unique = true)
    private String transactionNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "WalletID", nullable = false)
    private Wallet wallet;

    @Column(name = "Amount", nullable = false)
    private Double amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "TransactionType", nullable = false)
    private TransactionType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "PaymentMethod", nullable = false)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(name = "Status", nullable = false)
    private TransactionStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "RelatedBookingID")
    private Booking relatedBooking;

    @Column(name = "Description")
    private String description;

    @Column(name = "StatusMessage")
    private String statusMessage;

    @Column(name = "TransactionTime", nullable = false)
    private LocalDateTime transactionTime;

    @CreationTimestamp
    @Column(name = "CreatedAt", nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "UpdatedAt", nullable = false)
    private LocalDateTime updatedAt;

    // 交易類型枚舉
    @Getter
    public enum TransactionType {
        DEPOSIT("充值"),
        PAYMENT("支付"),
        REFUND("退款"),
        TRANSFER("轉帳");

        private final String description;

        TransactionType(String description) {
            this.description = description;
        }

    }

    // 支付方式枚舉
    @Getter
    public enum PaymentMethod {
        WALLET_BALANCE("錢包餘額"),
        CREDIT_CARD("信用卡"),
        STORE_PAYMENT("超商付款");

        private final String description;

        PaymentMethod(String description) {
            this.description = description;
        }

    }

    // 交易狀態枚舉
    @Getter
    public enum TransactionStatus {
        PENDING("處理中"),
        SUCCESS("成功"),
        FAILED("失敗"),
        CANCELLED("已取消");

        private final String description;

        TransactionStatus(String description) {
            this.description = description;
        }

    }

    // 初始化方法
    @PrePersist
    public void prePersist() {
        if (this.status == null) {
            this.status = TransactionStatus.PENDING;
        }
        if (this.transactionTime == null) {
            this.transactionTime = LocalDateTime.now();
        }
        if (this.transactionNumber == null) {
            this.transactionNumber = generateTransactionNumber();
        }
        if (this.paymentMethod == null) {
            this.paymentMethod = PaymentMethod.WALLET_BALANCE;
        }
    }

    // 生成交易編號
    private String generateTransactionNumber() {
        return "TX" + System.currentTimeMillis();
    }

    // 業務方法
    public void complete() {
        if (this.status == TransactionStatus.PENDING) {
            this.status = TransactionStatus.SUCCESS;
        } else {
            throw new IllegalStateException("只有處理中的交易可以完成");
        }
    }

    public void fail(String reason) {
        if (this.status == TransactionStatus.PENDING) {
            this.status = TransactionStatus.FAILED;
            this.statusMessage = reason;
        } else {
            throw new IllegalStateException("只有處理中的交易可以標記為失敗");
        }
    }

    public void cancel(String reason) {
        if (this.status == TransactionStatus.PENDING) {
            this.status = TransactionStatus.CANCELLED;
            this.statusMessage = reason;
        } else {
            throw new IllegalStateException("只有處理中的交易可以取消");
        }
    }

    public boolean isPending() {
        return this.status == TransactionStatus.PENDING;
    }

    public boolean isSuccess() {
        return this.status == TransactionStatus.SUCCESS;
    }

    public boolean isFailedOrCancelled() {
        return this.status == TransactionStatus.FAILED ||
                this.status == TransactionStatus.CANCELLED;
    }

    @Override
    public String toString() {
        return String.format("Transaction{id=%d, number='%s', type=%s, amount=%.2f, status=%s}",
                transactionId, transactionNumber, type, amount, status);
    }
}