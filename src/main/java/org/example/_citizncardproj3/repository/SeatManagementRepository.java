package org.example._citizncardproj3.repository;

import org.example._citizncardproj3.model.entity.SeatManagement;
import org.example._citizncardproj3.model.entity.Venue;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SeatManagementRepository extends JpaRepository<SeatManagement, Long> {

    // 基本查詢方法
    List<SeatManagement> findByVenue(Venue venue);

    List<SeatManagement> findByStatus(SeatManagement.SeatStatus status);

    List<SeatManagement> findBySeatZone(String seatZone);

    // 分頁查詢
    Page<SeatManagement> findByVenueOrderBySeatLabelAsc(Venue venue, Pageable pageable);

    Page<SeatManagement> findBySeatTypeOrderBySeatLabelAsc(
            SeatManagement.SeatType seatType,
            Pageable pageable
    );

    // 複雜條件查詢
    @Query("SELECT sm FROM SeatManagement sm WHERE sm.venue = :venue " +
            "AND sm.status = 'AVAILABLE' AND sm.isActive = true")
    List<SeatManagement> findAvailableSeats(@Param("venue") Venue venue);

    // 查詢需要維護的座位
    @Query("SELECT sm FROM SeatManagement sm WHERE sm.nextMaintenanceDate <= :currentDate " +
            "AND sm.status != 'MAINTENANCE'")
    List<SeatManagement> findSeatsNeedingMaintenance(
            @Param("currentDate") LocalDateTime currentDate
    );

    // 統計查詢
    @Query("SELECT sm.seatType, COUNT(sm) FROM SeatManagement sm " +
            "WHERE sm.venue = :venue GROUP BY sm.seatType")
    List<Object[]> countBySeatType(@Param("venue") Venue venue);

    @Query("SELECT COUNT(sm) FROM SeatManagement sm WHERE sm.venue = :venue " +
            "AND sm.status = 'AVAILABLE' AND sm.isActive = true")
    long countAvailableSeats(@Param("venue") Venue venue);

    // 更新操作
    @Modifying
    @Query("UPDATE SeatManagement sm SET sm.status = :newStatus, " +
            "sm.lastMaintenanceDate = CURRENT_TIMESTAMP " +
            "WHERE sm.seatId = :seatId")
    int updateSeatStatus(
            @Param("seatId") Long seatId,
            @Param("newStatus") SeatManagement.SeatStatus newStatus
    );

    @Modifying
    @Query("UPDATE SeatManagement sm SET sm.isActive = :active " +
            "WHERE sm.seatId = :seatId")
    int updateSeatActive(
            @Param("seatId") Long seatId,
            @Param("active") boolean active
    );

    // 批量操作
    @Modifying
    @Query("UPDATE SeatManagement sm SET sm.status = 'MAINTENANCE' " +
            "WHERE sm.nextMaintenanceDate <= CURRENT_TIMESTAMP " +
            "AND sm.status = 'AVAILABLE'")
    int updateSeatsToMaintenance();

    // 使用悲觀鎖查詢
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT sm FROM SeatManagement sm WHERE sm.seatId = :seatId")
    Optional<SeatManagement> findByIdWithLock(@Param("seatId") Long seatId);

    // 查詢特定區域的座位
    List<SeatManagement> findByVenueAndSeatZoneAndStatus(
            Venue venue,
            String seatZone,
            SeatManagement.SeatStatus status
    );

    // 查詢特定行列的座位
    List<SeatManagement> findByVenueAndSeatRowAndSeatColumn(
            Venue venue,
            String seatRow,
            String seatColumn
    );

    // 查詢維護歷史
    @Query("SELECT sm FROM SeatManagement sm WHERE sm.venue = :venue " +
            "AND sm.lastMaintenanceDate IS NOT NULL " +
            "ORDER BY sm.lastMaintenanceDate DESC")
    List<SeatManagement> findMaintenanceHistory(
            @Param("venue") Venue venue,
            Pageable pageable
    );

    // 查詢特定類型的可用座位
    @Query("SELECT sm FROM SeatManagement sm WHERE sm.venue = :venue " +
            "AND sm.seatType = :seatType AND sm.status = 'AVAILABLE' " +
            "AND sm.isActive = true")
    List<SeatManagement> findAvailableSeatsByType(
            @Param("venue") Venue venue,
            @Param("seatType") SeatManagement.SeatType seatType
    );

    // 查詢連續座位
    @Query("SELECT sm FROM SeatManagement sm WHERE sm.venue = :venue " +
            "AND sm.seatRow = :row AND sm.status = 'AVAILABLE' " +
            "AND sm.isActive = true " +
            "ORDER BY sm.seatColumn")
    List<SeatManagement> findConsecutiveSeats(
            @Param("venue") Venue venue,
            @Param("row") String row
    );

    // 軟刪除
    @Modifying
    @Query("UPDATE SeatManagement sm SET sm.isDeleted = true " +
            "WHERE sm.seatId = :seatId")
    int softDeleteSeat(@Param("seatId") Long seatId);
}