package org.example._citizncardproj3.model.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MovieUpdateRequest {

    /**
     * 電影名稱
     */
    @Size(max = 100, message = "電影名稱長度不能超過100個字元")
    private String movieName;

    /**
     * 電影描述
     */
    @Size(max = 1000, message = "電影描述長度不能超過1000個字元")
    private String description;

    /**
     * 電影時長(分鐘)
     */
    @Min(value = 30, message = "電影時長不能少於30分鐘")
    @Max(value = 360, message = "電影時長不能超過360分鐘")
    private Integer duration;

    /**
     * 上映日期
     */
    @Future(message = "上映日期必須是未來日期")
    private LocalDate releaseDate;

    /**
     * 下檔日期
     */
    @Future(message = "下檔日期必須是未來日期")
    private LocalDate endDate;

    /**
     * 語言
     */
    @Size(max = 50, message = "語言長度不能超過50個字元")
    private String language;

    /**
     * 字幕
     */
    @Size(max = 50, message = "字幕長度不能超過50個字元")
    private String subtitle;

    /**
     * 導演
     */
    @Size(max = 100, message = "導演名稱長度不能超過100個字元")
    private String director;

    /**
     * 演員列表
     */
    private List<String> cast;

    /**
     * 電影分級
     */
    @Pattern(regexp = "^(普遍級|保護級|輔導級|限制級)$", message = "無效的電影分級")
    private String rating;

    /**
     * 電影類別
     */
    private List<String> categories;

    /**
     * 基本票價
     */
    @DecimalMin(value = "0.0", message = "票價不能為負數")
    @DecimalMax(value = "9999.99", message = "票價不能超過9999.99")
    private Double basePrice;

    /**
     * 特殊放映類型
     */
    private List<String> specialScreeningTypes;

    /**
     * 放映廳要求
     */
    private List<String> venueRequirements;

    /**
     * 是否熱映中
     */
    private Boolean isHot;

    /**
     * 是否推薦
     */
    private Boolean isRecommended;

    /**
     * 額外資訊
     */
    private Map<String, String> additionalInfo;

    /**
     * 驗證更新請求的有效性
     */
    public boolean isValid() {
        // 至少要有一個欄位需要更新
        return movieName != null ||
                description != null ||
                duration != null ||
                releaseDate != null ||
                endDate != null ||
                language != null ||
                subtitle != null ||
                director != null ||
                cast != null ||
                rating != null ||
                categories != null ||
                basePrice != null ||
                specialScreeningTypes != null ||
                venueRequirements != null ||
                isHot != null ||
                isRecommended != null ||
                additionalInfo != null;
    }

    /**
     * 驗證日期邏輯
     */
    public boolean isValidDates() {
        if (releaseDate != null && endDate != null) {
            // 下檔日期必須在上映日期之後至少7天
            return endDate.isAfter(releaseDate) &&
                    endDate.isAfter(releaseDate.plusDays(7));
        }
        return true;
    }

    /**
     * 驗證特殊放映類型
     */
    public boolean isValidSpecialScreeningTypes() {
        if (specialScreeningTypes != null) {
            List<String> validTypes = List.of("2D", "3D", "IMAX", "4DX", "VIP", "數位");
            return specialScreeningTypes.stream()
                    .allMatch(validTypes::contains);
        }
        return true;
    }

    /**
     * 驗證電影類別
     */
    public boolean isValidCategories() {
        if (categories != null) {
            List<String> validCategories = List.of(
                    "動作", "冒險", "喜劇", "劇情", "恐怖",
                    "愛情", "科幻", "動畫", "紀錄片", "其他"
            );
            return categories.stream()
                    .allMatch(validCategories::contains);
        }
        return true;
    }

    /**
     * 驗證放映廳要求
     */
    public boolean isValidVenueRequirements() {
        if (venueRequirements != null) {
            List<String> validRequirements = List.of(
                    "標準", "IMAX", "4DX", "VIP", "親子廳", "數位"
            );
            return venueRequirements.stream()
                    .allMatch(validRequirements::contains);
        }
        return true;
    }
}