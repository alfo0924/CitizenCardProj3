package org.example._citizncardproj3.repository;

import org.example._citizncardproj3.model.entity.Booking;
import org.example._citizncardproj3.model.entity.MovieSchedule;
import org.example._citizncardproj3.model.entity.SeatBooking;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.persistence.LockModeType;
import java.util.List;
import java.util.Optional;

@Repository
public interface SeatBookingRepository extends JpaRepository<SeatBooking, Long> {

    // 基本查詢方法
    List<SeatBooking> findByBooking(Booking booking);

    List<SeatBooking> findBySchedule(MovieSchedule schedule);

    List<SeatBooking> findByStatus(SeatBooking.SeatStatus status);

    // 分頁查詢
    Page<SeatBooking> findByScheduleOrderBySeatNumberAsc(
            MovieSchedule schedule,
            Pageable pageable
    );

    // 複雜條件查詢
    @Query("SELECT sb FROM SeatBooking sb WHERE sb.schedule = :schedule " +
            "AND sb.status = 'BOOKED' AND sb.seatNumber IN :seatNumbers")
    List<SeatBooking> findBookedSeats(
            @Param("schedule") MovieSchedule schedule,
            @Param("seatNumbers") List<String> seatNumbers
    );

    // 檢查座位是否可用
    @Query("SELECT CASE WHEN COUNT(sb) > 0 THEN false ELSE true END " +
            "FROM SeatBooking sb WHERE sb.schedule = :schedule " +
            "AND sb.seatNumber = :seatNumber AND sb.status IN ('BOOKED', 'OCCUPIED')")
    boolean isSeatAvailable(
            @Param("schedule") MovieSchedule schedule,
            @Param("seatNumber") String seatNumber
    );

    // 統計查詢
    @Query("SELECT sb.seatType, COUNT(sb) FROM SeatBooking sb " +
            "WHERE sb.schedule = :schedule GROUP BY sb.seatType")
    List<Object[]> countBySeatType(@Param("schedule") MovieSchedule schedule);

    @Query("SELECT COUNT(sb) FROM SeatBooking sb WHERE sb.schedule = :schedule " +
            "AND sb.status = 'BOOKED'")
    long countBookedSeats(@Param("schedule") MovieSchedule schedule);

    // 更新操作
    @Modifying
    @Query("UPDATE SeatBooking sb SET sb.status = :newStatus " +
            "WHERE sb.seatBookingId = :seatBookingId")
    int updateSeatStatus(
            @Param("seatBookingId") Long seatBookingId,
            @Param("newStatus") SeatBooking.SeatStatus newStatus
    );

    // 批量操作
    @Modifying
    @Query("UPDATE SeatBooking sb SET sb.status = 'AVAILABLE' " +
            "WHERE sb.booking.status = 'CANCELLED'")
    int releaseSeatsForCancelledBookings();

    // 使用悲觀鎖查詢
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT sb FROM SeatBooking sb WHERE sb.seatBookingId = :id")
    Optional<SeatBooking> findByIdWithLock(@Param("id") Long id);

    // 查詢特定行列的座位
    List<SeatBooking> findByScheduleAndSeatRowAndSeatColumn(
            MovieSchedule schedule,
            String seatRow,
            String seatColumn
    );

    // 查詢特定類型的座位
    List<SeatBooking> findByScheduleAndSeatType(
            MovieSchedule schedule,
            SeatBooking.seattype seatType
    );

    // 查詢連續座位
    @Query("SELECT sb FROM SeatBooking sb WHERE sb.schedule = :schedule " +
            "AND sb.seatRow = :row AND sb.status = 'AVAILABLE' " +
            "AND CAST(sb.seatColumn AS integer) BETWEEN :startCol AND :endCol " +
            "ORDER BY CAST(sb.seatColumn AS integer)")
    List<SeatBooking> findConsecutiveSeats(
            @Param("schedule") MovieSchedule schedule,
            @Param("row") String row,
            @Param("startCol") int startCol,
            @Param("endCol") int endCol
    );

    // 查詢特定價格範圍的座位
    List<SeatBooking> findByScheduleAndPriceBetweenAndStatus(
            MovieSchedule schedule,
            Double minPrice,
            Double maxPrice,
            SeatBooking.SeatStatus status
    );

    // 軟刪除
    @Modifying
    @Query("UPDATE SeatBooking sb SET sb.isDeleted = true " +
            "WHERE sb.seatBookingId = :seatBookingId")
    int softDeleteSeatBooking(@Param("seatBookingId") Long seatBookingId);
}