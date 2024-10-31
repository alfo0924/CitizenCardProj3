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
@Table(name = "discount_usage")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiscountUsage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long usageId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "discount_id", nullable = false)
    private Discount discount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id")
    private Booking booking;

    @Column(nullable = false)
    private Double originalAmount;

    @Column(nullable = false)
    private Double discountAmount;

    @Column(nullable = false)
    private Double finalAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UsageStatus status;

    @Column(nullable = false)
    private LocalDateTime usageTime;

    @Column
    private LocalDateTime cancelTime;

    @Column
    private String cancelReason;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Column(nullable = false)
    private Boolean isDeleted;

    // 使用狀態枚舉
    public enum UsageStatus {
        USED("已使用"),
        CANCELLED("已取消"),
        REFUNDED("已退款");

        private final String description;

        UsageStatus(String description) {
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
        if (this.usageTime == null) {
            this.usageTime = LocalDateTime.now();
        }
        if (this.status == null) {
            this.status = UsageStatus.USED;
        }
    }

    // 業務方法

    // 使用優惠
    public void applyDiscount() {
        if (!discount.isValid()) {
            throw new IllegalStateException("優惠券無效或已過期");
        }

        this.discountAmount = discount.calculateDiscount(this.originalAmount);
        this.finalAmount = this.originalAmount - this.discountAmount;
        this.status = UsageStatus.USED;
        this.usageTime = LocalDateTime.now();

        // 更新優惠券使用次數
        discount.use();
    }

    // 取消使用
    public void cancel(String reason) {
        if (this.status != UsageStatus.USED) {
            throw new IllegalStateException("只有已使用的優惠可以取消");
        }

        this.status = UsageStatus.CANCELLED;
        this.cancelTime = LocalDateTime.now();
        this.cancelReason = reason;
    }

    // 退款處理
    public void refund() {
        if (this.status != UsageStatus.USED) {
            throw new IllegalStateException("只有已使用的優惠可以退款");
        }

        this.status = UsageStatus.REFUNDED;
        this.cancelTime = LocalDateTime.now();
        this.cancelReason = "退款處理";
    }

    // 驗證使用資格
    public boolean validateUsage() {
        // 檢查會員是否符合使用條件
        if (member == null || discount == null) {
            return false;
        }

        // 檢查優惠是否有效
        if (!discount.isValid()) {
            return false;
        }

        // 檢查訂單金額是否符合最低消費
        if (originalAmount < discount.getMinPurchaseAmount()) {
            return false;
        }

        // 檢查會員是否已超過使用次數限制
        long userUsageCount = member.getDiscountUsages().stream()
                .filter(usage -> usage.getDiscount().equals(discount))
                .filter(usage -> usage.getStatus() == UsageStatus.USED)
                .count();

        return discount.getUsageLimit() == null || userUsageCount < discount.getUsageLimit();
    }

    // 計算節省金額
    public Double getSavedAmount() {
        return this.discountAmount;
    }

    // 檢查是否可以取消
    public boolean isCancellable() {
        return this.status == UsageStatus.USED &&
                this.usageTime.plusHours(24).isAfter(LocalDateTime.now());
    }

    // 檢查是否可以退款
    public boolean isRefundable() {
        return this.status == UsageStatus.USED &&
                this.booking != null &&
                this.booking.isRefundable();
    }

    // 用於日誌記錄的方法
    public String toLogString() {
        return String.format("DiscountUsage{id=%d, discount='%s', member='%s', amount=%.2f, status=%s}",
                usageId, discount.getDiscountCode(), member.getEmail(), discountAmount, status);
    }
}