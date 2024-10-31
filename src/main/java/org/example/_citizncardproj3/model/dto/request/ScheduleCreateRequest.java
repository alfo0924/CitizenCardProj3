package org.example._citizncardproj3.model.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleCreateRequest {

    /**
     * 場地ID
     */
    @NotNull(message = "場地ID不能為空")
    private Long venueId;

    /**
     * 放映廳號
     */
    @NotBlank(message = "放映廳號不能為空")
    @Pattern(regexp = "^[A-Z0-9]{1,5}$", message = "放映廳號格式不正確")
    private String roomNumber;

    /**
     * 放映時間
     */
    @NotNull(message = "放映時間不能為空")
    @Future(message = "放映時間必須是未來時間")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime showTime;

    /**
     * 特殊票價（可選）
     */
    @PositiveOrZero(message = "票價不能為負數")
    @DecimalMax(value = "9999.99", message = "票價不能超過9999.99")
    private Double specialPrice;

    /**
     * 座位配置
     */
    private SeatConfiguration seatConfiguration;

    /**
     * 是否啟用自動座位分配
     */
    private Boolean autoSeatAllocation;

    /**
     * 場次備註
     */
    @Size(max = 500, message = "備註長度不能超過500字")
    private String notes;

    /**
     * 座位配置內部類
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SeatConfiguration {

        /**
         * 總座位數
         */
        @Positive(message = "座位數必須大於0")
        private Integer totalSeats;

        /**
         * 座位類型配置
         */
        private Map<String, String> seatTypes;

        /**
         * 座位價格配置
         */
        private Map<String, Double> seatPrices;

        /**
         * 停用的座位
         */
        private List<String> disabledSeats;

        /**
         * VIP座位
         */
        private List<String> vipSeats;

        /**
         * 情侶座位
         */
        private List<String> coupleSeats;
    }

    /**
     * 驗證請求的有效性
     */
    public boolean isValid() {
        // 基本驗證
        if (venueId == null || roomNumber == null || showTime == null) {
            return false;
        }

        // 時間驗證
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime minAllowedTime = now.plusHours(1);  // 至少提前1小時
        LocalDateTime maxAllowedTime = now.plusMonths(3); // 最多提前3個月

        if (showTime.isBefore(minAllowedTime) || showTime.isAfter(maxAllowedTime)) {
            return false;
        }

        // 票價驗證
        if (specialPrice != null && specialPrice < 0) {
            return false;
        }

        // 座位配置驗證
        if (seatConfiguration != null) {
            // 檢查座位數量
            if (seatConfiguration.getTotalSeats() != null &&
                    seatConfiguration.getTotalSeats() <= 0) {
                return false;
            }

            // 檢查座位價格
            if (seatConfiguration.getSeatPrices() != null) {
                for (Double price : seatConfiguration.getSeatPrices().values()) {
                    if (price < 0 || price > 9999.99) {
                        return false;
                    }
                }
            }

            // 檢查座位類型
            if (seatConfiguration.getSeatTypes() != null) {
                List<String> validTypes = List.of("REGULAR", "VIP", "COUPLE", "DISABLED");
                for (String type : seatConfiguration.getSeatTypes().values()) {
                    if (!validTypes.contains(type)) {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    /**
     * 獲取預計結束時間
     * @param movieDuration 電影時長(分鐘)
     */
    public LocalDateTime getEndTime(int movieDuration) {
        return showTime.plusMinutes(movieDuration);
    }

    /**
     * 檢查是否為特殊票價場次
     * @param basePrice 基本票價
     */
    public boolean isSpecialPricing(Double basePrice) {
        return specialPrice != null && !specialPrice.equals(basePrice);
    }
}