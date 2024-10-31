package org.example._citizncardproj3.service;

import org.example._citizncardproj3.model.entity.SeatManagement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface SeatManagementService {

    /**
     * 創建座位
     * @param venueId 場地ID
     * @param seatRow 座位行
     * @param seatColumn 座位列
     * @param seatType 座位類型
     * @return 創建的座位
     */
    SeatManagement createSeat(Long venueId, String seatRow, String seatColumn, SeatManagement.SeatType seatType);

    /**
     * 設置座位維護狀態
     * @param seatId 座位ID
     * @param maintainer 維護人員
     * @param notes 維護備註
     */
    void setMaintenance(Long seatId, String maintainer, String notes);

    /**
     * 完成座位維護
     * @param seatId 座位ID
     */
    void completeMaintenance(Long seatId);

    /**
     * 更新座位類型
     * @param seatId 座位ID
     * @param newType 新座位類型
     */
    void updateSeatType(Long seatId, SeatManagement.SeatType newType);

    /**
     * 更新座位區域
     * @param seatId 座位ID
     * @param newZone 新區域
     */
    void updateSeatZone(Long seatId, String newZone);

    /**
     * 獲取場地所有座位
     * @param venueId 場地ID
     * @return 座位列表
     */
    List<SeatManagement> getVenueSeats(Long venueId);

    /**
     * 獲取特定類型的座位
     * @param venueId 場地ID
     * @param seatType 座位類型
     * @param pageable 分頁參數
     * @return 座位分頁
     */
    Page<SeatManagement> getVenueSeatsByType(Long venueId, SeatManagement.SeatType seatType, Pageable pageable);

    /**
     * 獲取可用座位
     * @param venueId 場地ID
     * @return 可用座位列表
     */
    List<SeatManagement> getAvailableSeats(Long venueId);

    /**
     * 檢查並更新維護狀態
     */
    void checkAndUpdateMaintenanceStatus();

    /**
     * 獲取維護歷史記錄
     * @param venueId 場地ID
     * @param pageable 分頁參數
     * @return 維護記錄列表
     */
    List<SeatManagement> getMaintenanceHistory(Long venueId, Pageable pageable);

    /**
     * 獲取座位類型統計
     * @param venueId 場地ID
     * @return 統計資訊
     */
    List<Object[]> getSeatTypeStatistics(Long venueId);

    /**
     * 停用座位
     * @param seatId 座位ID
     * @param reason 停用原因
     */
    void disableSeat(Long seatId, String reason);

    /**
     * 啟用座位
     * @param seatId 座位ID
     */
    void enableSeat(Long seatId);

    /**
     * 查找連續座位
     * @param venueId 場地ID
     * @param row 座位行
     * @param count 座位數量
     * @return 連續座位列表
     */
    List<SeatManagement> findConsecutiveSeats(Long venueId, String row, int count);
}