package org.example._citizncardproj3.model.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MovieResponse {

    private Long movieId;
    private String movieCode;
    private String movieName;
    private String description;
    private Integer duration; // 片長（分鐘）

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate releaseDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    private String language;
    private String subtitle;
    private String director;
    private List<String> cast;
    private String posterUrl;
    private String trailerUrl;
    private String rating;
    private List<String> categories;
    private MovieStatus status;
    private Double basePrice;
    private List<Schedule> schedules;
    private MovieStatistics statistics;
    private String message;

    // 電影狀態枚舉
    public enum MovieStatus {
        COMING_SOON("即將上映"),
        NOW_SHOWING("熱映中"),
        END_SHOWING("已下檔");

        private final String description;

        MovieStatus(String description) {
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
    public static class Schedule {
        private Long scheduleId;
        private String venueName;
        private String roomNumber;

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime showTime;

        private Double price;
        private Integer totalSeats;
        private Integer availableSeats;
        private String scheduleStatus;
    }

    // 統計資訊內部類
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MovieStatistics {
        private Integer totalBookings;
        private Integer totalViewers;
        private Double totalRevenue;
        private Double averageRating;
        private Integer reviewCount;
    }

    // 建構子 - 用於錯誤響應
    public MovieResponse(String message) {
        this.message = message;
    }

    // 建構子 - 複製現有響應並添加消息
    public MovieResponse(MovieResponse response, String message) {
        if (response != null) {
            this.movieId = response.getMovieId();
            this.movieCode = response.getMovieCode();
            this.movieName = response.getMovieName();
            this.description = response.getDescription();
            this.duration = response.getDuration();
            this.releaseDate = response.getReleaseDate();
            this.endDate = response.getEndDate();
            this.language = response.getLanguage();
            this.subtitle = response.getSubtitle();
            this.director = response.getDirector();
            this.cast = response.getCast();
            this.posterUrl = response.getPosterUrl();
            this.trailerUrl = response.getTrailerUrl();
            this.rating = response.getRating();
            this.categories = response.getCategories();
            this.status = response.getStatus();
            this.basePrice = response.getBasePrice();
            this.schedules = response.getSchedules();
            this.statistics = response.getStatistics();
        }
        this.message = message;
    }

    // 輔助方法 - 檢查電影是否可訂票
    public boolean isBookable() {
        if (status != MovieStatus.NOW_SHOWING) {
            return false;
        }
        return schedules != null && !schedules.isEmpty() &&
                schedules.stream().anyMatch(s -> s.getAvailableSeats() > 0);
    }

    // 輔助方法 - 獲取最近可用場次
    public Schedule getNextAvailableSchedule() {
        if (schedules == null || schedules.isEmpty()) {
            return null;
        }
        LocalDateTime now = LocalDateTime.now();
        return schedules.stream()
                .filter(s -> s.getShowTime().isAfter(now) && s.getAvailableSeats() > 0)
                .findFirst()
                .orElse(null);
    }

    // 輔助方法 - 計算折扣價格
    public Double calculateDiscountPrice(Double discountRate) {
        if (basePrice == null || discountRate == null) {
            return null;
        }
        return basePrice * (1 - discountRate);
    }

    // 輔助方法 - 獲取電影狀態描述
    public String getStatusDescription() {
        return status != null ? status.getDescription() : "未知狀態";
    }

    // 輔助方法 - 檢查是否為新上映電影
    public boolean isNewRelease() {
        if (releaseDate == null) {
            return false;
        }
        return releaseDate.isAfter(LocalDate.now().minusDays(7));
    }

    // 用於日誌記錄的方法
    public String toLogString() {
        return String.format("Movie{id=%d, name='%s', status=%s, schedules=%d}",
                movieId, movieName, status,
                schedules != null ? schedules.size() : 0);
    }
}