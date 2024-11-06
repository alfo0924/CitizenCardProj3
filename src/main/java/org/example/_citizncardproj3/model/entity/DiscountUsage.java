package org.example._citizncardproj3.model.entity;

import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "DiscountUsageHistory")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiscountUsage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "UsageID")
    private Long usageId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MemberID", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "DiscountID", nullable = false)
    private Discount discount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "BookingID")
    private Booking booking;

    @Column(name = "UsageAmount", nullable = false)
    private Double usageAmount;

    @Column(name = "UsageTime", nullable = false)
    private LocalDateTime usageTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "Status", nullable = false)
    private UsageStatus status;

    @CreationTimestamp
    @Column(name = "CreatedAt", nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "UpdatedAt", nullable = false)
    private LocalDateTime updatedAt;

    // 使用狀態枚舉
    @Getter
    public enum UsageStatus {
        PENDING("待使用"),
        USED("已使用"),
        CANCELLED("已取消");

        private final String description;

        UsageStatus(String description) {
            this.description = description;
        }
    }

    // 初始化方法
    @PrePersist
    public void prePersist() {
        if (this.usageTime == null) {
            this.usageTime = LocalDateTime.now();
        }
        if (this.status == null) {
            this.status = UsageStatus.PENDING;
        }
    }

    // 業務方法
    public void use() {
        if (this.status != UsageStatus.PENDING) {
            throw new IllegalStateException("優惠券狀態不正確");
        }
        this.status = UsageStatus.USED;
        this.usageTime = LocalDateTime.now();
    }

    public void cancel() {
        if (this.status != UsageStatus.USED && this.status != UsageStatus.PENDING) {
            throw new IllegalStateException("優惠券狀態不正確");
        }
        this.status = UsageStatus.CANCELLED;
    }

    // 驗證方法
    public boolean validateUsage() {
        if (member == null || discount == null) {
            return false;
        }
        if (!discount.isValid()) {
            return false;
        }
        if (usageAmount < discount.getMinPurchaseAmount()) {
            return false;
        }
        return true;
    }

    // 檢查是否可以取消
    public boolean isCancellable() {
        return this.status == UsageStatus.USED &&
                this.usageTime.plusHours(24).isAfter(LocalDateTime.now());
    }

    // 用於日誌記錄的方法
    @Override
    public String toString() {
        return String.format("DiscountUsage{id=%d, discount='%s', member='%s', amount=%.2f, status=%s}",
                usageId, discount.getDiscountCode(), member.getEmail(), usageAmount, status);
    }
}