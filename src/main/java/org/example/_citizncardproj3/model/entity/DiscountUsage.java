package org.example._citizncardproj3.model.entity;

import lombok.*;
import org.example._citizncardproj3.model.dto.response.DiscountUsageResponse;
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

    @Column(length = 50)
    private String orderNumber;

    @Column(length = 50)
    private String orderType;

    @Column(length = 255)
    private String orderDescription;

    @Column(length = 100)
    private String usageLocation;

    @Column(length = 100)
    private String deviceInfo;

    @Column(length = 50)
    private String operatorId;

    @Column(length = 500)
    private String additionalInfo;

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
    @Getter
    public enum UsageStatus {
        PENDING("待使用"),
        USED("已使用"),
        EXPIRED("已過期"),
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
            this.status = UsageStatus.PENDING;
        }
    }

    // 業務方法
    public void applyDiscount() {
        if (!discount.isValid()) {
            throw new IllegalStateException("優惠券無效或已過期");
        }

        this.discountAmount = discount.calculateDiscount(this.originalAmount);
        this.finalAmount = this.originalAmount - this.discountAmount;
        this.status = UsageStatus.USED;
        this.usageTime = LocalDateTime.now();

        discount.use();
    }

    public void use() {
        if (this.status != UsageStatus.PENDING) {
            throw new IllegalStateException("優惠券狀態不正確");
        }
        this.status = UsageStatus.USED;
        this.usageTime = LocalDateTime.now();
    }

    public void cancel(String reason) {
        if (this.status != UsageStatus.USED && this.status != UsageStatus.PENDING) {
            throw new IllegalStateException("優惠券狀態不正確");
        }
        this.status = UsageStatus.CANCELLED;
        this.cancelTime = LocalDateTime.now();
        this.cancelReason = reason;
    }

    public void refund() {
        if (this.status != UsageStatus.USED) {
            throw new IllegalStateException("只有已使用的優惠可以退款");
        }
        this.status = UsageStatus.REFUNDED;
        this.cancelTime = LocalDateTime.now();
        this.cancelReason = "退款處理";
    }

    public void expire() {
        if (this.status == UsageStatus.PENDING) {
            this.status = UsageStatus.EXPIRED;
        }
    }

    // 驗證方法
    public boolean validateUsage() {
        if (member == null || discount == null) {
            return false;
        }
        if (!discount.isValid()) {
            return false;
        }
        if (originalAmount < discount.getMinPurchaseAmount()) {
            return false;
        }

        long userUsageCount = member.getDiscountUsages().stream()
                .filter(usage -> usage.getDiscount().equals(discount))
                .filter(usage -> usage.getStatus() == UsageStatus.USED)
                .count();

        return discount.getUsageLimit() == null || userUsageCount < discount.getUsageLimit();
    }

    public boolean isCancellable() {
        return this.status == UsageStatus.USED &&
                this.usageTime.plusHours(24).isAfter(LocalDateTime.now());
    }

    public boolean isRefundable() {
        return this.status == UsageStatus.USED &&
                this.booking != null &&
                this.booking.isRefundable();
    }

    public Double getSavedAmount() {
        return this.discountAmount;
    }

    // 用於日誌記錄的方法
    public String toLogString() {
        return String.format("DiscountUsage{id=%d, discount='%s', member='%s', amount=%.2f, status=%s}",
                usageId, discount.getDiscountCode(), member.getEmail(), discountAmount, status);
    }

    // 轉換為DTO的方法
    public static DiscountUsageResponse fromEntity(DiscountUsage usage) {
        Discount discount = usage.getDiscount();
        return DiscountUsageResponse.builder()
                .usageId(usage.getUsageId())
                .discountCode(discount.getDiscountCode())
                .discountName(discount.getDiscountName())
                .discountType(discount.getDiscountType())
                .discountValue(discount.getDiscountValue())
                .minPurchaseAmount(discount.getMinPurchaseAmount())
                .maxDiscountAmount(discount.getMaxDiscountAmount())
                .memberName(usage.getMember().getName())
                .memberEmail(usage.getMember().getEmail())
                .originalAmount(usage.getOriginalAmount())
                .discountedAmount(usage.getDiscountAmount())
                .finalAmount(usage.getFinalAmount())
                .orderNumber(usage.getOrderNumber())
                .orderType(usage.getOrderType())
                .status(usage.getStatus())
                .usageTime(usage.getUsageTime())
                .expiryTime(discount.getValidUntil())
                .details(DiscountUsageResponse.UsageDetails.builder()
                        .orderType(usage.getOrderType())
                        .orderDescription(usage.getOrderDescription())
                        .usageLocation(usage.getUsageLocation())
                        .deviceInfo(usage.getDeviceInfo())
                        .operatorId(usage.getOperatorId())
                        .additionalInfo(usage.getAdditionalInfo())
                        .build())
                .build();
    }
}