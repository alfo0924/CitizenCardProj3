package org.example._citizncardproj3.service;

import org.example._citizncardproj3.model.entity.MovieSchedule;
import org.example._citizncardproj3.model.entity.Venue;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface VenueService {

    @Transactional
    Venue createVenue(String venueName, String address, Integer totalSeats);

    /**
     * 創建場地
     * @param venueName 場地名稱
     * @param address 地址
     * @param totalRows 總行數
     * @param totalColumns 總列數
     * @return 創建的場地
     */
    Venue createVenue(String venueName, String address, Integer totalRows, Integer totalColumns);

    /**
     * 設置場地維護
     * @param venueId 場地ID
     * @param maintenanceDate 維護日期
     */
    void setMaintenance(Long venueId, LocalDateTime maintenanceDate);

    @org.springframework.transaction.annotation.Transactional
    void setMaintenance(Long venueId);

    /**
     * 完成場地維護
     * @param venueId 場地ID
     */
    void completeMaintenance(Long venueId);

    /**
     * 更新座位配置
     * @param venueId 場地ID
     * @param newLayout 新座位配置
     */
    void updateSeatingLayout(Long venueId, String newLayout);

    /**
     * 獲取可用場地列表
     * @return 可用場地列表
     */
    List<Venue> getAvailableVenues();

    /**
     * 獲取特定時間可用的場地
     * @param startTime 開始時間
     * @param endTime 結束時間
     * @return 可用場地列表
     */
    List<Venue> getAvailableVenuesForTime(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 獲取場地場次列表
     * @param venueId 場地ID
     * @param pageable 分頁參數
     * @return 場次分頁
     */
    Page<MovieSchedule> getVenueSchedules(Long venueId, Pageable pageable);

    /**
     * 獲取場地使用率
     * @param venueId 場地ID
     * @param startTime 開始時間
     * @param endTime 結束時間
     * @return 使用率統計
     */
    List<Object[]> getVenueUtilization(Long venueId, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 檢查並更新維護狀態
     */
    void checkAndUpdateMaintenanceStatus();

    /**
     * 檢查場地可用性
     * @param venueId 場地ID
     * @param startTime 開始時間
     * @param endTime 結束時間
     * @return 是否可用
     */
    boolean checkVenueAvailability(Long venueId, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 獲取場地詳情
     * @param venueId 場地ID
     * @return 場地實體
     */
    Venue getVenueDetails(Long venueId);

    /**
     * 更新場地資訊
     * @param venueId 場地ID
     * @param venueName 場地名稱
     * @param address 地址
     * @return 更新後的場地
     */
    Venue updateVenueInfo(Long venueId, String venueName, String address);

    /**
     * 獲取場地維護歷史
     * @param venueId 場地ID
     * @param pageable 分頁參數
     * @return 維護歷史列表
     */
    Page<Map<String, Object>> getMaintenanceHistory(Long venueId, Pageable pageable);

    /**
     * 獲取場地座位使用統計
     * @param venueId 場地ID
     * @return 統計資訊
     */
    Map<String, Object> getSeatUsageStatistics(Long venueId);
}