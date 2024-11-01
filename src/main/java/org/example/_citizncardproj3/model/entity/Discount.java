package org.example._citizncardproj3.model.entity;

import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "discounts")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Discount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long discountId;

    @Column(unique = true, nullable = false)
    private String discountCode;

    @Column(nullable = false)
    private String discountName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DiscountType discountType;

    @Column(nullable = false)
    private Double discountValue;

    @Column(nullable = false)
    private Double minPurchaseAmount;

    @Column(nullable = false)
    private Double maxDiscountAmount;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private LocalDateTime validFrom;

    @Column(nullable = false)
    private LocalDateTime validUntil;

    private Integer usageLimit;

    private Integer usageCount;

    @OneToMany(mappedBy = "discount")
    private List<DiscountUsage> usageHistory;



    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Column(nullable = false)
    private Boolean isDeleted;

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

    // 使用優惠
    public void use() {
        if (!isValid()) {
            throw new IllegalStateException("優惠券無效或已過期");
        }
        if (usageLimit != null && usageCount >= usageLimit) {
            throw new IllegalStateException("優惠券已達使用上限");
        }
        this.usageCount++;
    }


    // 停用優惠
    public void deactivate() {
        this.isActive = false;
    }

    // 重新啟用優惠
    public void activate() {
        if (LocalDateTime.now().isAfter(validUntil)) {
            throw new IllegalStateException("已過期的優惠不能重新啟用");
        }
        this.isActive = true;
    }

    // 延長有效期
    public void extendValidity(LocalDateTime newValidUntil) {
        if (newValidUntil.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("新的有效期必須是未來時間");
        }
        this.validUntil = newValidUntil;
    }

    // 增加使用次數限制
    public void increaseUsageLimit(Integer additionalLimit) {
        if (additionalLimit <= 0) {
            throw new IllegalArgumentException("增加的使用次數必須大於0");
        }
        if (this.usageLimit == null) {
            this.usageLimit = additionalLimit;
        } else {
            this.usageLimit += additionalLimit;
        }
    }

    // 檢查是否即將到期
    public boolean isExpiringSoon() {
        return LocalDateTime.now().plusDays(7).isAfter(validUntil);
    }

    // 檢查是否可以應用於特定金額
    public boolean isApplicable(Double amount) {
        return amount >= minPurchaseAmount;
    }

    // 用於日誌記錄的方法
    public String toLogString() {
        return String.format("Discount{id=%d, code='%s', type=%s, value=%.2f, valid=%s}",
                discountId, discountCode, discountType, discountValue, isValid());
    }

    // 優惠狀態枚舉
    @Getter
    public enum DiscountStatus {
        DRAFT("草稿"),
        ACTIVE("啟用"),
        INACTIVE("停用"),
        EXPIRED("已過期"),
        DEPLETED("已用完"),
        CANCELLED("已取消"),
        SUSPENDED("暫停使用");

        private final String description;

        DiscountStatus(String description) {
            this.description = description;
        }

    }

    // 新增欄位
    @ElementCollection
    @CollectionTable(name = "discount_membership_levels")
    private List<String> applicableMembershipLevels;

    @ElementCollection
    @CollectionTable(name = "discount_movie_categories")
    private List<String> applicableMovieCategories;

    @ElementCollection
    @CollectionTable(name = "discount_showtimes")
    private List<String> applicableShowtimes;

    @Column
    private Boolean stackable;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DiscountStatus status;

    // 修改初始化方法
    @PrePersist
    public void prePersist() {
        if (this.isDeleted == null) {
            this.isDeleted = false;
        }
        if (this.isActive == null) {
            this.isActive = true;
        }
        if (this.usageCount == null) {
            this.usageCount = 0;
        }
        if (this.status == null) {
            this.status = DiscountStatus.DRAFT;
        }
        if (this.stackable == null) {
            this.stackable = false;
        }
    }

    // 新增業務方法
    public void updateStatus(DiscountStatus newStatus) {
        if (this.status == DiscountStatus.CANCELLED) {
            throw new IllegalStateException("已取消的優惠不能更改狀態");
        }
        this.status = newStatus;
    }

    public boolean isStackable() {
        return stackable != null && stackable;
    }

    public boolean isApplicableToMemberLevel(String membershipLevel) {
        return applicableMembershipLevels == null ||
                applicableMembershipLevels.isEmpty() ||
                applicableMembershipLevels.contains(membershipLevel);
    }

    public boolean isApplicableToMovieCategory(String movieCategory) {
        return applicableMovieCategories == null ||
                applicableMovieCategories.isEmpty() ||
                applicableMovieCategories.contains(movieCategory);
    }

    public boolean isApplicableToShowtime(String showtime) {
        return applicableShowtimes == null ||
                applicableShowtimes.isEmpty() ||
                applicableShowtimes.contains(showtime);
    }

    // 修改isValid方法，加入狀態檢查

    public boolean isValid() {
        if (!isActive || isDeleted || status != DiscountStatus.ACTIVE) {
            return false;
        }

        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(validFrom) || now.isAfter(validUntil)) {
            return false;
        }

        if (usageLimit != null && usageCount >= usageLimit) {
            return false;
        }

        return true;
    }
    // 添加公開的getter和setter方法
    @Setter
    @Getter
    @Column(nullable = false)
    private Boolean isActive = true;  // 設定預設值

    // 如果需要布林值的getter，也可以添加
    public boolean isActive() {
        return isActive != null && isActive;
    }
}