package org.example._citizncardproj3.model.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example._citizncardproj3.model.entity.Discount;

import javax.validation.constraints.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiscountUpdateRequest {

    /**
     * 優惠名稱
     */
    @Size(max = 100, message = "優惠名稱長度不能超過100個字元")
    private String discountName;

    /**
     * 優惠描述
     */
    @Size(max = 500, message = "優惠描述長度不能超過500個字元")
    private String description;

    /**
     * 優惠值（百分比或固定金額）
     */
    @Positive(message = "優惠值必須大於0")
    @DecimalMax(value = "100", message = "百分比折扣不能超過100%", groups = {PercentageDiscount.class})
    private Double discountValue;

    /**
     * 最低消費金額
     */
    @PositiveOrZero(message = "最低消費金額不能為負數")
    private Double minPurchaseAmount;

    /**
     * 最高折扣金額
     */
    @PositiveOrZero(message = "最高折扣金額不能為負數")
    private Double maxDiscountAmount;

    /**
     * 結束時間
     */
    @Future(message = "結束時間必須是未來時間")
    private LocalDateTime validUntil;

    /**
     * 使用次數限制（每個用戶）
     */
    @PositiveOrZero(message = "使用次數限制不能為負數")
    private Integer userUsageLimit;

    /**
     * 總使用次數限制
     */
    @PositiveOrZero(message = "總使用次數限制不能為負數")
    private Integer totalUsageLimit;

    /**
     * 適用會員等級
     */
    private List<String> applicableMembershipLevels;

    /**
     * 適用電影類別
     */
    private List<String> applicableMovieCategories;

    /**
     * 適用場次時段
     */
    private List<String> applicableShowtimes;

    /**
     * 是否可與其他優惠同時使用
     */
    private Boolean stackable;

    /**
     * 是否啟用
     */
    private Boolean isActive;

    /**
     * 優惠狀態
     */
    private Discount.DiscountStatus status;

    /**
     * 使用條件
     */
    private Map<String, String> conditions;

    /**
     * 百分比折扣驗證群組
     */
    public interface PercentageDiscount {}

    /**
     * 驗證更新請求的有效性
     */
    public boolean isValid() {
        // 至少要有一個欄位需要更新
        return discountName != null ||
                description != null ||
                discountValue != null ||
                minPurchaseAmount != null ||
                maxDiscountAmount != null ||
                validUntil != null ||
                userUsageLimit != null ||
                totalUsageLimit != null ||
                applicableMembershipLevels != null ||
                applicableMovieCategories != null ||
                applicableShowtimes != null ||
                stackable != null ||
                isActive != null ||
                status != null ||
                conditions != null;
    }

    /**
     * 驗證折扣值
     */
    public boolean isValidDiscountValue(Discount.DiscountType discountType) {
        if (discountValue == null) {
            return true; // 不更新折扣值
        }

        if (discountType == Discount.DiscountType.PERCENTAGE) {
            return discountValue > 0 && discountValue <= 100;
        } else if (discountType == Discount.DiscountType.FIXED_AMOUNT) {
            return discountValue > 0;
        }

        return false;
    }

    /**
     * 驗證使用限制
     */
    public boolean isValidUsageLimits() {
        if (userUsageLimit != null && userUsageLimit < 0) {
            return false;
        }
        if (totalUsageLimit != null && totalUsageLimit < 0) {
            return false;
        }
        if (userUsageLimit != null && totalUsageLimit != null) {
            return userUsageLimit <= totalUsageLimit;
        }
        return true;
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
     * 驗證會員等級設定
     */
    public boolean isValidMembershipLevels() {
        if (applicableMembershipLevels == null || applicableMembershipLevels.isEmpty()) {
            return true;
        }
        List<String> validLevels = List.of("REGULAR", "SILVER", "GOLD", "PLATINUM");
        return applicableMembershipLevels.stream().allMatch(validLevels::contains);
    }
}