package org.example._citizncardproj3.model.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example._citizncardproj3.model.entity.CityMovie;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MovieCreateRequest {

    @NotBlank(message = "電影名稱不能為空")
    @Size(max = 100, message = "電影名稱不能超過100個字符")
    private String movieName;

    @NotBlank(message = "電影描述不能為空")
    @Size(max = 2000, message = "電影描述不能超過2000個字符")
    private String description;

    @NotNull(message = "上映日期不能為空")
    @Future(message = "上映日期必須是未來日期")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate releaseDate;

    @NotNull(message = "下檔日期不能為空")
    @Future(message = "下檔日期必須是未來日期")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    @NotBlank(message = "語言不能為空")
    private String language;

    private String subtitle;

    @NotBlank(message = "導演不能為空")
    private String director;

    @NotEmpty(message = "演員列表不能為空")
    private List<String> cast;

    @NotNull(message = "片長不能為空")
    @Positive(message = "片長必須大於0")
    @Max(value = 360, message = "片長不能超過360分鐘")
    private Integer duration;

    @NotBlank(message = "分級不能為空")
    @Pattern(regexp = "^(普遍級|保護級|輔導級|限制級)$", message = "分級必須是：普遍級、保護級、輔導級或限制級")
    private String rating;

    @NotNull(message = "電影類型不能為空")
    private List<CityMovie.MovieCategory> categories;  // 改用CityMovie.MovieCategory
    @NotNull(message = "基本票價不能為空")
    @Positive(message = "基本票價必須大於0")
    private Double basePrice;

    private MultipartFile posterFile;

    private MultipartFile trailerFile;

    // 電影類型枚舉
    public enum MovieCategory {
        ACTION("動作片"),
        COMEDY("喜劇片"),
        DRAMA("劇情片"),
        HORROR("恐怖片"),
        SCIFI("科幻片"),
        ANIMATION("動畫片"),
        DOCUMENTARY("紀錄片");

        private final String description;

        MovieCategory(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    // 場次資訊內部類
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ScheduleInfo {
        @NotNull(message = "場地ID不能為空")
        private Long venueId;

        @NotNull(message = "放映日期不能為空")
        @Future(message = "放映日期必須是未來日期")
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate showDate;

        @NotNull(message = "開始時間不能為空")
        @DateTimeFormat(pattern = "HH:mm")
        private LocalTime startTime;

        private Double specialPrice;
    }

    // 驗證方法
    public boolean isValid() {
        if (!isDateValid()) {
            return false;
        }

        if (!isPriceValid()) {
            return false;
        }

        if (!isFileValid()) {
            return false;
        }

        return true;
    }

    // 日期驗證
    private boolean isDateValid() {
        if (releaseDate == null || endDate == null) {
            return false;
        }

        // 確保下檔日期在上映日期之後
        return endDate.isAfter(releaseDate);
    }

    // 價格驗證
    private boolean isPriceValid() {
        return basePrice != null && basePrice > 0;
    }

    // 文件驗證
    private boolean isFileValid() {
        if (posterFile != null) {
            String contentType = posterFile.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return false;
            }
            // 檢查文件大小（例如：最大5MB）
            if (posterFile.getSize() > 5 * 1024 * 1024) {
                return false;
            }
        }

        if (trailerFile != null) {
            String contentType = trailerFile.getContentType();
            if (contentType == null || !contentType.startsWith("video/")) {
                return false;
            }
            // 檢查文件大小（例如：最大50MB）
            if (trailerFile.getSize() > 50 * 1024 * 1024) {
                return false;
            }
        }

        return true;
    }

    // 清理敏感數據
    public void clearSensitiveData() {
        // 如果有敏感數據需要清理，在這裡實現
    }

    // 構建日誌信息
    public String toLogString() {
        return String.format("MovieCreateRequest{movieName='%s', director='%s', " +
                        "releaseDate='%s', endDate='%s', rating='%s'}",
                movieName, director, releaseDate, endDate, rating);
    }

    public List<String> getCategories() {
        return categories.stream()
                .map(CityMovie.MovieCategory::name)
                .collect(Collectors.toList());
    }

}