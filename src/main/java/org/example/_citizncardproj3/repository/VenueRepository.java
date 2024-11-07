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
import java.util.Map;
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
            "WHERE v.status = 'ACTIVE' AND v.isDeleted = false AND " +
            "(s IS NULL OR NOT EXISTS (" +
            "   SELECT 1 FROM MovieSchedule ms " +
            "   WHERE ms.venue = v AND " +
            "   ms.showDate BETWEEN :startTime AND :endTime AND " +
            "   ms.isCancelled = false" +
            "))")
    List<Venue> findAvailableVenuesForTime(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    // 統計查詢
    @Query("SELECT v.status, COUNT(v) FROM Venue v GROUP BY v.status")
    List<Object[]> countByStatus();

    @Query("SELECT SUM(v.totalSeats) FROM Venue v WHERE v.status = 'ACTIVE' AND v.isDeleted = false")
    Integer getTotalAvailableSeats();

    // 更新操作
    @Modifying
    @Query("UPDATE Venue v SET v.status = :newStatus WHERE v.venueId = :venueId")
    int updateVenueStatus(
            @Param("venueId") Long venueId,
            @Param("newStatus") Venue.VenueStatus newStatus
    );

    // 使用悲觀鎖查詢
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT v FROM Venue v WHERE v.venueId = :venueId")
    Optional<Venue> findByIdWithLock(@Param("venueId") Long venueId);

    // 查詢場地使用率
    @Query("SELECT v, COUNT(s) FROM Venue v LEFT JOIN v.schedules s " +
            "WHERE v.venueId = :venueId AND " +
            "s.showDate BETWEEN :startTime AND :endTime AND " +
            "s.isCancelled = false " +
            "GROUP BY v")
    List<Object[]> getVenueUtilization(
            @Param("venueId") Long venueId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    // 軟刪除
    @Modifying
    @Query("UPDATE Venue v SET v.isDeleted = true, v.deletedAt = CURRENT_TIMESTAMP " +
            "WHERE v.venueId = :venueId")
    int softDeleteVenue(@Param("venueId") Long venueId);

    // 檢查場地名稱是否存在
    boolean existsByVenueName(String venueName);

    // 查詢特定時段內可用座位數
    @Query("SELECT v, COUNT(s) FROM Venue v " +
            "LEFT JOIN v.seats s " +
            "WHERE s.status = 'AVAILABLE' AND v.isDeleted = false " +
            "GROUP BY v")
    List<Object[]> findAvailableSeatsInTimeRange(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    // 查詢維護歷史
    @Query("SELECT new map(" +
            "v.status as status, " +
            "v.updatedAt as maintenanceDate, " +
            "v.venueName as venueName) " +
            "FROM Venue v " +
            "WHERE v.venueId = :venueId AND v.status = 'MAINTENANCE' " +
            "ORDER BY v.updatedAt DESC")
    Page<Map<String, Object>> findMaintenanceHistoryByVenueId(
            @Param("venueId") Long venueId,
            Pageable pageable
    );

    // 查詢場地座位使用情況
    @Query("SELECT new map(" +
            "v.venueName as venueName, " +
            "v.totalSeats as totalSeats, " +
            "COUNT(s) as availableSeats) " +
            "FROM Venue v LEFT JOIN v.seats s " +
            "WHERE v.venueId = :venueId AND s.status = 'AVAILABLE' " +
            "GROUP BY v")
    Map<String, Object> findVenueSeatStatus(@Param("venueId") Long venueId);

    // 查詢所有未刪除的場地
    @Query("SELECT v FROM Venue v WHERE v.isDeleted = false ORDER BY v.createdAt DESC")
    List<Venue> findAllActiveVenues();

    // 查詢特定狀態且未刪除的場地
    @Query("SELECT v FROM Venue v WHERE v.status = :status AND v.isDeleted = false")
    List<Venue> findActiveVenuesByStatus(@Param("status") Venue.VenueStatus status);
}