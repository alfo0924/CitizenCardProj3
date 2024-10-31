package org.example._citizncardproj3.model.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example._citizncardproj3.model.entity.Discount;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DiscountResponse {

    private Long discountId;
    private String discountCode;
    private String discountName;
    private Discount.DiscountType discountType;
    private Double discountValue;
    private Double minPurchaseAmount;
    private Double maxDiscountAmount;
    private String description;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime validFrom;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime validUntil;

    private Integer usageLimit;
    private Integer usageCount;
    private Boolean isActive;
    private List<String> applicableMembershipLevels;
    private List<String> applicableProductCategories;
    private Boolean stackable;
    private UsageStatistics usageStatistics;
    private String message;  // 用於錯誤訊息

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UsageStatistics {
        private Integer totalUsages;
        private Integer uniqueUsers;
        private Double totalDiscountAmount;
        private Double averageDiscountAmount;
        private Double usageRate;  // 使用率（已使用次數/限制次數）
    }

    // 建構子 - 用於錯誤響應
    public DiscountResponse(Discount discount, String message) {
        if (discount != null) {
            this.discountId = discount.getDiscountId();
            this.discountCode = discount.getDiscountCode();
            this.discountName = discount.getDiscountName();
            this.discountType = discount.getDiscountType();
            this.discountValue = discount.getDiscountValue();
            this.minPurchaseAmount = discount.getMinPurchaseAmount();
            this.maxDiscountAmount = discount.getMaxDiscountAmount();
            this.description = discount.getDescription();
            this.validFrom = discount.getValidFrom();
            this.validUntil = discount.getValidUntil();
            this.usageLimit = discount.getUsageLimit();
            this.usageCount = discount.getUsageCount();
            this.isActive = discount.getIsActive();
            this.stackable = discount.getStackable();
        }
        this.message = message;
    }

    // 從Discount實體轉換為DTO
    public static DiscountResponse fromEntity(Discount discount) {
        return DiscountResponse.builder()
                .discountId(discount.getDiscountId())
                .discountCode(discount.getDiscountCode())
                .discountName(discount.getDiscountName())
                .discountType(discount.getDiscountType())
                .discountValue(discount.getDiscountValue())
                .minPurchaseAmount(discount.getMinPurchaseAmount())
                .maxDiscountAmount(discount.getMaxDiscountAmount())
                .description(discount.getDescription())
                .validFrom(discount.getValidFrom())
                .validUntil(discount.getValidUntil())
                .usageLimit(discount.getUsageLimit())
                .usageCount(discount.getUsageCount())
                .isActive(discount.getIsActive())
                .stackable(discount.getStackable())
                .build();
    }

    // 從Discount實體轉換為詳細DTO（包含統計資訊）
    public static DiscountResponse fromEntityWithStats(Discount discount, UsageStatistics stats) {
        DiscountResponse response = fromEntity(discount);
        response.setUsageStatistics(stats);
        return response;
    }

    // 檢查優惠是否有效
    public boolean isValid() {
        LocalDateTime now = LocalDateTime.now();
        return isActive &&
                now.isAfter(validFrom) &&
                now.isBefore(validUntil) &&
                (usageLimit == null || usageCount < usageLimit);
    }

    // 計算剩餘使用次數
    public Integer getRemainingUsages() {
        if (usageLimit == null) {
            return null;
        }
        return Math.max(0, usageLimit - usageCount);
    }

    // 計算使用率
    public Double getUsageRate() {
        if (usageLimit == null || usageLimit == 0) {
            return null;
        }
        return (double) usageCount / usageLimit * 100;
    }

    // 檢查是否即將到期（7天內）
    public boolean isExpiringSoon() {
        LocalDateTime now = LocalDateTime.now();
        return now.plusDays(7).isAfter(validUntil);
    }

    // 獲取優惠描述
    public String getDiscountDescription() {
        if (discountType == Discount.DiscountType.PERCENTAGE) {
            return String.format("%.0f%% 折扣", discountValue);
        } else {
            return String.format("折抵 $%.0f", discountValue);
        }
    }

    // 用於日誌記錄的方法
    public String toLogString() {
        return String.format("Discount{id=%d, code='%s', type=%s, value=%.2f, valid=%s}",
                discountId, discountCode, discountType, discountValue, isValid());
    }
}