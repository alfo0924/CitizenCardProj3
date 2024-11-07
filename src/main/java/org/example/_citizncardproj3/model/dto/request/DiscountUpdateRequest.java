package org.example._citizncardproj3.model.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiscountUpdateRequest {

    @Size(max = 100, message = "優惠名稱長度不能超過100個字元")
    private String discountName;

    @Size(max = 500, message = "優惠描述長度不能超過500個字元")
    private String description;

    @Positive(message = "優惠值必須大於0")
    @DecimalMax(value = "100", message = "百分比折扣不能超過100%", groups = {PercentageDiscount.class})
    private Double discountValue;

    @PositiveOrZero(message = "最低消費金額不能為負數")
    private Double minPurchaseAmount;

    @PositiveOrZero(message = "最高折扣金額不能為負數")
    private Double maxDiscountAmount;

    @Future(message = "結束時間必須是未來時間")
    private LocalDateTime validUntil;

    @PositiveOrZero(message = "使用次數限制不能為負數")
    private Integer usageLimit;

    private String terms;

    // 折扣類型枚舉
    public enum DiscountType {
        FIXED_AMOUNT("金額折扣"),
        PERCENTAGE("百分比折扣"),
        POINTS_DISCOUNT("點數折抵");

        private final String description;

        DiscountType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    private DiscountType discountType;

    /**
     * 百分比折扣驗證群組
     */
    public interface PercentageDiscount {}

    /**
     * 驗證更新請求的有效性
     */
    public boolean isValid() {
        return discountName != null ||
                description != null ||
                discountValue != null ||
                minPurchaseAmount != null ||
                maxDiscountAmount != null ||
                validUntil != null ||
                usageLimit != null ||
                terms != null ||
                discountType != null;
    }

    /**
     * 驗證折扣值
     */
    public boolean isValidDiscountValue() {
        if (discountValue == null) {
            return true; // 不更新折扣值
        }

        if (discountType == DiscountType.PERCENTAGE) {
            return discountValue > 0 && discountValue <= 100;
        } else if (discountType == DiscountType.FIXED_AMOUNT) {
            return discountValue > 0;
        }

        return false;
    }

    /**
     * 驗證使用限制
     */
    public boolean isValidUsageLimit() {
        return usageLimit == null || usageLimit >= 0;
    }

    /**
     * 驗證時間設定
     */
    public boolean isValidDateTime() {
        if (validUntil == null) {
            return true;
        }
        return validUntil.isAfter(LocalDateTime.now());
    }

    /**
     * 驗證金額設定
     */
    public boolean isValidAmounts() {
        if (minPurchaseAmount != null && minPurchaseAmount < 0) {
            return false;
        }
        if (maxDiscountAmount != null && maxDiscountAmount < 0) {
            return false;
        }
        if (minPurchaseAmount != null && maxDiscountAmount != null) {
            return maxDiscountAmount <= minPurchaseAmount;
        }
        return true;
    }

    /**
     * 獲取更新的欄位
     */
    public Map<String, Object> getUpdatedFields() {
        Map<String, Object> updates = new java.util.HashMap<>();

        if (discountName != null) updates.put("discountName", discountName);
        if (description != null) updates.put("description", description);
        if (discountValue != null) updates.put("discountValue", discountValue);
        if (minPurchaseAmount != null) updates.put("minPurchaseAmount", minPurchaseAmount);
        if (maxDiscountAmount != null) updates.put("maxDiscountAmount", maxDiscountAmount);
        if (validUntil != null) updates.put("validUntil", validUntil);
        if (usageLimit != null) updates.put("usageLimit", usageLimit);
        if (terms != null) updates.put("terms", terms);
        if (discountType != null) updates.put("discountType", discountType);

        return updates;
    }
}