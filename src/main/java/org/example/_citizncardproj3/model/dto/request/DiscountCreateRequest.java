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
public class DiscountCreateRequest {

    /**
     * 優惠名稱
     */
    @NotBlank(message = "優惠名稱不能為空")
    @Size(max = 100, message = "優惠名稱長度不能超過100個字元")
    private String discountName;

    /**
     * 優惠代碼
     */
    @NotBlank(message = "優惠代碼不能為空")
    @Pattern(regexp = "^[A-Z0-9]{6,12}$", message = "優惠代碼必須是6-12位的大寫字母或數字")
    private String discountCode;

    /**
     * 優惠類型
     */
    @NotNull(message = "優惠類型不能為空")
    private Discount.DiscountType discountType;

    /**
     * 優惠值（百分比或固定金額）
     */
    @NotNull(message = "優惠值不能為空")
    @Positive(message = "優惠值必須大於0")
    @DecimalMax(value = "100", message = "百分比折扣不能超過100%", groups = {PercentageDiscount.class})
    private Double discountValue;

    /**
     * 最低消費金額
     */
    @NotNull(message = "最低消費金額不能為空")
    @PositiveOrZero(message = "最低消費金額不能為負數")
    private Double minPurchaseAmount;

    /**
     * 最高折扣金額
     */
    @NotNull(message = "最高折扣金額不能為空")
    @PositiveOrZero(message = "最高折扣金額不能為負數")
    private Double maxDiscountAmount;

    /**
     * 優惠描述
     */
    @Size(max = 500, message = "優惠描述長度不能超過500個字元")
    private String description;

    /**
     * 開始時間
     */
    @NotNull(message = "開始時間不能為空")
    @Future(message = "開始時間必須是未來時間")
    private LocalDateTime validFrom;

    /**
     * 結束時間
     */
    @NotNull(message = "結束時間不能為空")
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
     * 使用條件
     */
    private Map<String, String> conditions;

    /**
     * 百分比折扣驗證群組
     */
    public interface PercentageDiscount {}

    /**
     * 驗證請求的有效性
     */
    public boolean isValid() {
        // 基本驗證
        if (discountName == null || discountCode == null ||
                discountType == null || discountValue == null ||
                validFrom == null || validUntil == null) {
            return false;
        }

        // 時間驗證
        if (validFrom.isAfter(validUntil)) {
            return false;
        }

        // 根據優惠類型驗證折扣值
        if (discountType == Discount.DiscountType.PERCENTAGE) {
            if (discountValue <= 0 || discountValue > 100) {
                return false;
            }
        } else if (discountType == Discount.DiscountType.FIXED_AMOUNT) {
            if (discountValue <= 0) {
                return false;
            }
        }

        // 使用限制驗證
        if (userUsageLimit != null && userUsageLimit < 0) {
            return false;
        }
        if (totalUsageLimit != null && totalUsageLimit < 0) {
            return false;
        }
        if (userUsageLimit != null && totalUsageLimit != null) {
            if (userUsageLimit > totalUsageLimit) {
                return false;
            }
        }

        return true;
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

    /**
     * 驗證電影類別設定
     */
    public boolean isValidMovieCategories() {
        if (applicableMovieCategories == null || applicableMovieCategories.isEmpty()) {
            return true;
        }
        List<String> validCategories = List.of("ACTION", "COMEDY", "DRAMA", "HORROR", "ROMANCE");
        return applicableMovieCategories.stream().allMatch(validCategories::contains);
    }
}