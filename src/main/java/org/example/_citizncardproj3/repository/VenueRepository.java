package org.example._citizncardproj3.repository;

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
public interface VenueRepository extends JpaRepository<Venue, Long> {

    // 基本查詢方法
    Optional<Venue> findByVenueName(String venueName);

    List<Venue> findByStatus(Venue.VenueStatus status);

    List<Venue> findByTotalSeatsGreaterThanEqual(Integer minSeats);

    // 分頁查詢
    Page<Venue> findByStatusOrderByCreatedAtDesc(
            Venue.VenueStatus status,
            Pageable pageable
    );

    // 複雜條件查詢
    @Query("SELECT v FROM Venue v WHERE v.status = 'ACTIVE' " +
            "AND v.totalSeats >= :minSeats AND v.isDeleted = false")
    List<Venue> findAvailableVenues(@Param("minSeats") Integer minSeats);

    // 查詢特定時間可用的場地
    @Query("SELECT DISTINCT v FROM Venue v LEFT JOIN v.schedules s " +
            "WHERE v.status = 'ACTIVE' AND " +
            "(s IS NULL OR s.showTime NOT BETWEEN :startTime AND :endTime)")
    List<Venue> findAvailableVenuesForTime(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    // 統計查詢
    @Query("SELECT v.status, COUNT(v) FROM Venue v GROUP BY v.status")
    List<Object[]> countByStatus();

    @Query("SELECT SUM(v.totalSeats) FROM Venue v WHERE v.status = 'ACTIVE'")
    Integer getTotalAvailableSeats();

    // 更新操作
    @Modifying
    @Query("UPDATE Venue v SET v.status = :newStatus WHERE v.venueId = :venueId")
    int updateVenueStatus(
            @Param("venueId") Long venueId,
            @Param("newStatus") Venue.VenueStatus newStatus
    );

    @Modifying
    @Query("UPDATE Venue v SET v.lastMaintenanceDate = CURRENT_TIMESTAMP, " +
            "v.nextMaintenanceDate = :nextMaintenanceDate " +
            "WHERE v.venueId = :venueId")
    int updateMaintenanceInfo(
            @Param("venueId") Long venueId,
            @Param("nextMaintenanceDate") LocalDateTime nextMaintenanceDate
    );

    // 批量操作
    @Modifying
    @Query("UPDATE Venue v SET v.status = 'MAINTENANCE' " +
            "WHERE v.nextMaintenanceDate <= CURRENT_TIMESTAMP " +
            "AND v.status = 'ACTIVE'")
    int updateVenuesNeedingMaintenance();

    // 使用悲觀鎖查詢
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT v FROM Venue v WHERE v.venueId = :venueId")
    Optional<Venue> findByIdWithLock(@Param("venueId") Long venueId);

    // 查詢需要維護的場地
    @Query("SELECT v FROM Venue v WHERE v.nextMaintenanceDate <= :checkDate " +
            "AND v.status != 'MAINTENANCE'")
    List<Venue> findVenuesNeedingMaintenance(
            @Param("checkDate") LocalDateTime checkDate
    );

    // 查詢特定座位配置的場地
    @Query("SELECT v FROM Venue v WHERE v.totalRows >= :minRows " +
            "AND v.totalColumns >= :minColumns")
    List<Venue> findVenuesBySeatingCapacity(
            @Param("minRows") Integer minRows,
            @Param("minColumns") Integer minColumns
    );

    // 查詢場地使用率
    @Query("SELECT v, COUNT(s) FROM Venue v LEFT JOIN v.schedules s " +
            "WHERE s.showTime BETWEEN :startTime AND :endTime " +
            "GROUP BY v")
    List<Object[]> getVenueUtilization(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    // 查詢特定設施的場地
    @Query("SELECT v FROM Venue v WHERE v.facilities LIKE %:facility%")
    List<Venue> findVenuesByFacility(@Param("facility") String facility);

    // 查詢維護歷史
    @Query("SELECT v FROM Venue v WHERE v.lastMaintenanceDate IS NOT NULL " +
            "ORDER BY v.lastMaintenanceDate DESC")
    List<Venue> findVenuesWithMaintenanceHistory(Pageable pageable);

    // 軟刪除
    @Modifying
    @Query("UPDATE Venue v SET v.isDeleted = true WHERE v.venueId = :venueId")
    int softDeleteVenue(@Param("venueId") Long venueId);
}