package org.example._citizncardproj3.model.entity;

import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "Discounts")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Discount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "DiscountID")
    private Long discountId;

    @Column(name = "DiscountCode", unique = true, nullable = false)
    private String discountCode;

    @Column(name = "DiscountName", nullable = false)
    private String discountName;

    @Enumerated(EnumType.STRING)
    @Column(name = "DiscountType", nullable = false)
    private DiscountType discountType;

    @Column(name = "DiscountValue", nullable = false)
    private Double discountValue;

    @Column(name = "MinPurchaseAmount", nullable = false)
    private Double minPurchaseAmount;

    @Column(name = "MaxDiscountAmount", nullable = false)
    private Double maxDiscountAmount;

    @Column(name = "Description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "Terms", columnDefinition = "TEXT")
    private String terms;

    @Column(name = "ValidFrom", nullable = false)
    private LocalDateTime validFrom;

    @Column(name = "ValidUntil", nullable = false)
    private LocalDateTime validUntil;

    @Column(name = "UsageLimit")
    private Integer usageLimit;

    @CreationTimestamp
    @Column(name = "CreatedAt", nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "UpdatedAt", nullable = false)
    private LocalDateTime updatedAt;

    // 添加getter和setter
    // 添加getter和setter
    @Setter
    @Getter
    @Column(name = "IsDeleted", nullable = false)
    private Boolean isDeleted = false;

    @Setter
    @Getter
    @Column(name = "DeletedAt")
    private LocalDateTime deletedAt;

    @OneToMany(mappedBy = "discount")
    private List<DiscountUsage> usageHistory;



    // 優惠類型枚舉
    @Getter
    public enum DiscountType {
        FIXED_AMOUNT("固定金額"),
        PERCENTAGE("百分比折扣"),
        POINTS_DISCOUNT("點數折抵");

        private final String description;

        DiscountType(String description) {
            this.description = description;
        }
    }

    // 初始化方法
    @PrePersist
    public void prePersist() {
        if (this.isDeleted == null) {
            this.isDeleted = false;
        }
    }

    // 業務方法

    // 計算折扣金額
    public Double calculateDiscount(Double originalAmount) {
        if (!isValid() || originalAmount < minPurchaseAmount) {
            return 0.0;
        }

        Double discountAmount;
        switch (discountType) {
            case FIXED_AMOUNT:
                discountAmount = discountValue;
                break;
            case PERCENTAGE:
                discountAmount = originalAmount * (discountValue / 100);
                break;
            case POINTS_DISCOUNT:
                discountAmount = discountValue;
                break;
            default:
                return 0.0;
        }

        // 確保不超過最大折扣金額
        return Math.min(discountAmount, maxDiscountAmount);
    }

    // 檢查優惠券是否有效
    public boolean isValid() {
        if (isDeleted) {
            return false;
        }

        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(validFrom) || now.isAfter(validUntil)) {
            return false;
        }

        return usageLimit == null || getUsageCount() < usageLimit;
    }

    // 獲取使用次數
    public int getUsageCount() {
        return usageHistory != null ? usageHistory.size() : 0;
    }

    // 延長有效期
    public void extendValidity(LocalDateTime newValidUntil) {
        if (newValidUntil.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("新的有效期必須是未來時間");
        }
        this.validUntil = newValidUntil;
    }

    // 檢查是否即將到期
    public boolean isExpiringSoon() {
        return LocalDateTime.now().plusDays(7).isAfter(validUntil);
    }

    // 檢查是否可以應用於特定金額
    public boolean isApplicable(Double amount) {
        return amount >= minPurchaseAmount;
    }

    // 軟刪除
    public void softDelete() {
        this.isDeleted = true;
        this.deletedAt = LocalDateTime.now();
    }

    // 用於日誌記錄的方法
    @Override
    public String toString() {
        return String.format("Discount{id=%d, code='%s', type=%s, value=%.2f, valid=%s}",
                discountId, discountCode, discountType, discountValue, isValid());
    }

}