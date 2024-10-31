package org.example._citizncardproj3.service;

import org.example._citizncardproj3.model.dto.request.ScheduleCreateRequest;
import org.example._citizncardproj3.model.dto.response.ScheduleResponse;
import org.example._citizncardproj3.model.entity.MovieSchedule;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface MovieScheduleService {

    /**
     * 創建電影場次
     * @param movieId 電影ID
     * @param request 場次創建請求
     * @return 創建的場次
     */
    ScheduleResponse createSchedule(Long movieId, ScheduleCreateRequest request);

    /**
     * 獲取電影場次列表
     * @param movieId 電影ID
     * @param pageable 分頁參數
     * @return 場次列表分頁
     */
    Page<ScheduleResponse> getMovieSchedules(Long movieId, Pageable pageable);

    /**
     * 獲取可用場次
     * @param movieId 電影ID
     * @param startTime 開始時間
     * @param endTime 結束時間
     * @return 可用場次列表
     */
    List<ScheduleResponse> getAvailableSchedules(Long movieId, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 更新場次狀態
     * @param scheduleId 場次ID
     * @param newStatus 新狀態
     */
    void updateScheduleStatus(Long scheduleId, MovieSchedule.ScheduleStatus newStatus);

    /**
     * 取消場次
     * @param scheduleId 場次ID
     */
    void cancelSchedule(Long scheduleId);

    /**
     * 更新可用座位數
     * @param scheduleId 場次ID
     */
    void updateAvailableSeats(Long scheduleId);

    /**
     * 檢查場次是否可訂票
     * @param scheduleId 場次ID
     * @return 是否可訂票
     */
    boolean isScheduleBookable(Long scheduleId);

    /**
     * 獲取場次座位配置
     * @param scheduleId 場次ID
     * @return 座位配置信息
     */
    Map<String, Object> getSeatConfiguration(Long scheduleId);

    /**
     * 檢查座位是否可用
     * @param scheduleId 場次ID
     * @param seatNumber 座位號碼
     * @return 是否可用
     */
    boolean isSeatAvailable(Long scheduleId, String seatNumber);

    /**
     * 鎖定座位
     * @param scheduleId 場次ID
     * @param seatNumbers 座位號碼列表
     * @param duration 鎖定時長(分鐘)
     */
    void lockSeats(Long scheduleId, List<String> seatNumbers, int duration);

    /**
     * 解鎖座位
     * @param scheduleId 場次ID
     * @param seatNumbers 座位號碼列表
     */
    void unlockSeats(Long scheduleId, List<String> seatNumbers);

    /**
     * 獲取場次統計信息
     * @param scheduleId 場次ID
     * @return 統計信息
     */
    Map<String, Object> getScheduleStatistics(Long scheduleId);

    /**
     * 檢查並更新過期場次
     */
    void updateExpiredSchedules();
}