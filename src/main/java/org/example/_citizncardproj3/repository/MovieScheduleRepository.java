package org.example._citizncardproj3.repository;

import org.example._citizncardproj3.model.entity.CityMovie;
import org.example._citizncardproj3.model.entity.MovieSchedule;
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
public interface MovieScheduleRepository extends JpaRepository<MovieSchedule, Long> {

    // 基本查詢方法
    List<MovieSchedule> findByMovie(CityMovie movie);

    List<MovieSchedule> findByVenue(Venue venue);

    List<MovieSchedule> findByStatus(MovieSchedule.ScheduleStatus status);

    // 分頁查詢
    Page<MovieSchedule> findByMovieOrderByShowTimeAsc(CityMovie movie, Pageable pageable);

    Page<MovieSchedule> findByVenueOrderByShowTimeAsc(Venue venue, Pageable pageable);

    // 複雜條件查詢
    @Query("SELECT ms FROM MovieSchedule ms WHERE ms.movie = :movie " +
            "AND ms.showTime BETWEEN :startTime AND :endTime " +
            "AND ms.status = 'ON_SALE'")
    List<MovieSchedule> findAvailableSchedules(
            @Param("movie") CityMovie movie,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    // 查詢特定時間範圍的場次
    @Query("SELECT ms FROM MovieSchedule ms WHERE " +
            "ms.showTime >= :startTime AND ms.showTime <= :endTime " +
            "ORDER BY ms.showTime ASC")
    List<MovieSchedule> findSchedulesInTimeRange(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    // 查詢座位可用的場次
    @Query("SELECT ms FROM MovieSchedule ms WHERE ms.movie = :movie " +
            "AND ms.showTime > :currentTime AND ms.availableSeats > 0")
    List<MovieSchedule> findSchedulesWithAvailableSeats(
            @Param("movie") CityMovie movie,
            @Param("currentTime") LocalDateTime currentTime
    );

    // 統計查詢
    @Query("SELECT ms.status, COUNT(ms) FROM MovieSchedule ms GROUP BY ms.status")
    List<Object[]> countByStatus();

    @Query("SELECT COUNT(ms) FROM MovieSchedule ms WHERE ms.venue = :venue " +
            "AND ms.showTime BETWEEN :startTime AND :endTime")
    long countSchedulesByVenue(
            @Param("venue") Venue venue,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    // 更新操作
    @Modifying
    @Query("UPDATE MovieSchedule ms SET ms.status = :newStatus " +
            "WHERE ms.scheduleId = :scheduleId")
    int updateScheduleStatus(
            @Param("scheduleId") Long scheduleId,
            @Param("newStatus") MovieSchedule.ScheduleStatus newStatus
    );

    @Modifying
    @Query("UPDATE MovieSchedule ms SET ms.availableSeats = :availableSeats " +
            "WHERE ms.scheduleId = :scheduleId")
    int updateAvailableSeats(
            @Param("scheduleId") Long scheduleId,
            @Param("availableSeats") Integer availableSeats
    );

    // 批量操作
    @Modifying
    @Query("UPDATE MovieSchedule ms SET ms.status = 'ENDED' " +
            "WHERE ms.showTime < :currentTime AND ms.status = 'ON_SALE'")
    int updateEndedSchedules(@Param("currentTime") LocalDateTime currentTime);

    // 使用悲觀鎖查詢
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT ms FROM MovieSchedule ms WHERE ms.scheduleId = :scheduleId")
    Optional<MovieSchedule> findByIdWithLock(@Param("scheduleId") Long scheduleId);

    // 查詢即將開始的場次
    @Query("SELECT ms FROM MovieSchedule ms WHERE ms.showTime BETWEEN :now AND :future " +
            "AND ms.status = 'ON_SALE' ORDER BY ms.showTime ASC")
    List<MovieSchedule> findUpcomingSchedules(
            @Param("now") LocalDateTime now,
            @Param("future") LocalDateTime future
    );

    // 查詢特定價格範圍的場次
    List<MovieSchedule> findByBasePriceBetweenAndStatus(
            Double minPrice,
            Double maxPrice,
            MovieSchedule.ScheduleStatus status
    );

    // 查詢特定場地的可用場次
    @Query("SELECT ms FROM MovieSchedule ms WHERE ms.venue = :venue " +
            "AND ms.showTime > :currentTime AND ms.availableSeats > 0 " +
            "ORDER BY ms.showTime ASC")
    List<MovieSchedule> findAvailableSchedulesByVenue(
            @Param("venue") Venue venue,
            @Param("currentTime") LocalDateTime currentTime
    );

    // 查詢銷售情況最好的場次
    @Query("SELECT ms FROM MovieSchedule ms WHERE ms.status = 'ON_SALE' " +
            "ORDER BY (ms.totalSeats - ms.availableSeats) DESC")
    List<MovieSchedule> findBestSellingSchedules(Pageable pageable);

    // 軟刪除
    @Modifying
    @Query("UPDATE MovieSchedule ms SET ms.isDeleted = true " +
            "WHERE ms.scheduleId = :scheduleId")
    int softDeleteSchedule(@Param("scheduleId") Long scheduleId);


    /**
     * 檢查場地在指定時間範圍是否有其他場次
     */
    boolean existsByVenueAndShowTimeBetween(
            Venue venue,
            LocalDateTime startTime,
            LocalDateTime endTime
    );

    /**
     * 查詢場地在指定時間範圍的所有場次（包含結束時間）
     */
    @Query("SELECT ms FROM MovieSchedule ms WHERE ms.venue = :venue " +
            "AND ((ms.showTime BETWEEN :startTime AND :endTime) OR " +
            "(ms.endTime BETWEEN :startTime AND :endTime) OR " +
            "(ms.showTime <= :startTime AND ms.endTime >= :endTime))")
    List<MovieSchedule> findConflictingSchedules(
            @Param("venue") Venue venue,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    /**
     * 獲取場次詳細統計信息
     */
    @Query("SELECT new map(" +
            "ms.movie.movieName as movieName, " +
            "ms.venue.venueName as venueName, " +
            "ms.showTime as showTime, " +
            "ms.totalSeats as totalSeats, " +
            "ms.availableSeats as availableSeats, " +
            "(ms.totalSeats - ms.availableSeats) as soldSeats, " +
            "((ms.totalSeats - ms.availableSeats) * ms.basePrice) as revenue) " +
            "FROM MovieSchedule ms WHERE ms.scheduleId = :scheduleId")
    Map<String, Object> getScheduleStatistics(@Param("scheduleId") Long scheduleId);

    /**
     * 查詢特定時段的座位使用率
     */
    @Query("SELECT new map(" +
            "ms.scheduleId as scheduleId, " +
            "ms.movie.movieName as movieName, " +
            "((ms.totalSeats - ms.availableSeats) * 100.0 / ms.totalSeats) as occupancyRate) " +
            "FROM MovieSchedule ms " +
            "WHERE ms.showTime BETWEEN :startTime AND :endTime")
    List<Map<String, Object>> getSeatsOccupancyRate(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    /**
     * 更新場次座位狀態
     */
    @Modifying
    @Query("UPDATE MovieSchedule ms SET " +
            "ms.availableSeats = :availableSeats, " +
            "ms.status = CASE " +
            "WHEN :availableSeats = 0 THEN 'SOLD_OUT' " +
            "ELSE ms.status END " +
            "WHERE ms.scheduleId = :scheduleId")
    int updateSeatsAndStatus(
            @Param("scheduleId") Long scheduleId,
            @Param("availableSeats") Integer availableSeats
    );

    /**
     * 查詢需要自動更新狀態的場次
     */
    @Query("SELECT ms FROM MovieSchedule ms WHERE " +
            "(ms.showTime <= :currentTime AND ms.status = 'ON_SALE') OR " +
            "(ms.endTime <= :currentTime AND ms.status = 'IN_PROGRESS')")
    List<MovieSchedule> findSchedulesNeedingStatusUpdate(
            @Param("currentTime") LocalDateTime currentTime
    );

}