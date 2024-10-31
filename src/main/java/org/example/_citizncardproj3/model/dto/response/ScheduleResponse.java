package org.example._citizncardproj3.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example._citizncardproj3.model.entity.MovieSchedule;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleResponse {

    /**
     * 場次ID
     */
    private Long scheduleId;

    /**
     * 電影ID
     */
    private Long movieId;

    /**
     * 電影名稱
     */
    private String movieName;

    /**
     * 場地ID
     */
    private Long venueId;

    /**
     * 場地名稱
     */
    private String venueName;

    /**
     * 放映廳號
     */
    private String roomNumber;

    /**
     * 放映時間
     */
    private LocalDateTime showTime;

    /**
     * 結束時間
     */
    private LocalDateTime endTime;

    /**
     * 票價
     */
    private Double basePrice;

    /**
     * 可用座位數
     */
    private Integer availableSeats;

    /**
     * 總座位數
     */
    private Integer totalSeats;

    /**
     * 場次狀態
     */
    private MovieSchedule.ScheduleStatus status;

    /**
     * 座位配置信息
     */
    private SeatConfiguration seatConfiguration;

    /**
     * 已訂座位列表
     */
    private List<String> bookedSeats;

    /**
     * 鎖定座位列表
     */
    private List<String> lockedSeats;

    /**
     * 維護中座位列表
     */
    private List<String> maintenanceSeats;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SeatConfiguration {
        private Integer totalRows;
        private Integer seatsPerRow;
        private Map<String, String> seatTypes;  // 座位號碼 -> 座位類型
        private Map<String, Double> seatPrices;  // 座位號碼 -> 座位價格
        private List<String> disabledSeats;  // 停用的座位
    }

    /**
     * 檢查場次是否可訂票
     */
    public boolean isBookable() {
        return status == MovieSchedule.ScheduleStatus.ON_SALE &&
                availableSeats > 0 &&
                showTime.isAfter(LocalDateTime.now());
    }

    /**
     * 獲取剩餘座位百分比
     */
    public double getAvailableSeatsPercentage() {
        if (totalSeats == 0) return 0;
        return (double) availableSeats / totalSeats * 100;
    }

    /**
     * 檢查是否即將開始
     */
    public boolean isStartingSoon() {
        return LocalDateTime.now().plusMinutes(30).isAfter(showTime);
    }

    /**
     * 檢查是否已結束
     */
    public boolean isEnded() {
        return LocalDateTime.now().isAfter(endTime);
    }

    /**
     * 檢查座位是否可用
     */
    public boolean isSeatAvailable(String seatNumber) {
        return !bookedSeats.contains(seatNumber) &&
                !lockedSeats.contains(seatNumber) &&
                !maintenanceSeats.contains(seatNumber) &&
                !seatConfiguration.getDisabledSeats().contains(seatNumber);
    }

    /**
     * 獲取座位價格
     */
    public Double getSeatPrice(String seatNumber) {
        Double seatPrice = seatConfiguration.getSeatPrices().get(seatNumber);
        return seatPrice != null ? seatPrice : basePrice;
    }

    /**
     * 格式化顯示時間
     */
    public String getFormattedShowTime() {
        return showTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }

    /**
     * 格式化顯示結束時間
     */
    public String getFormattedEndTime() {
        return endTime.format(DateTimeFormatter.ofPattern("HH:mm"));
    }

    /**
     * 獲取場次時長（分鐘）
     */
    public long getDurationMinutes() {
        return java.time.Duration.between(showTime, endTime).toMinutes();
    }

    /**
     * 轉換為顯示用字串
     */
    @Override
    public String toString() {
        return String.format(
                "場次: %s\n電影: %s\n時間: %s-%s\n票價: %.2f\n剩餘座位: %d/%d",
                scheduleId,
                movieName,
                getFormattedShowTime(),
                getFormattedEndTime(),
                basePrice,
                availableSeats,
                totalSeats
        );
    }
}