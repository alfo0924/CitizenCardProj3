package org.example._citizncardproj3.repository;

import org.example._citizncardproj3.model.entity.Booking;
import org.example._citizncardproj3.model.entity.Member;
import org.example._citizncardproj3.model.entity.MovieSchedule;
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
public interface BookingRepository extends JpaRepository<Booking, Long> {

    // 基本CRUD方法由JpaRepository提供

    // 根據訂票編號查詢
    Optional<Booking> findByBookingNumber(String bookingNumber);

    // 查詢會員的所有訂票
    Page<Booking> findByMemberOrderByCreatedAtDesc(Member member, Pageable pageable);

    // 查詢會員特定狀態的訂票
    Page<Booking> findByMemberAndStatusOrderByCreatedAtDesc(
            Member member,
            Booking.BookingStatus status,
            Pageable pageable
    );

    // 查詢場次的所有訂票
    List<Booking> findByScheduleAndStatus(
            MovieSchedule schedule,
            Booking.BookingStatus status
    );

    // 查詢特定時間範圍內的訂票
    @Query("SELECT b FROM Booking b WHERE b.createdAt BETWEEN :startTime AND :endTime")
    List<Booking> findBookingsInTimeRange(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    // 查詢未支付且超時的訂票
    @Query("SELECT b FROM Booking b WHERE b.status = 'PENDING' AND b.createdAt < :timeout")
    List<Booking> findExpiredUnpaidBookings(@Param("timeout") LocalDateTime timeout);

    // 取消過期未支付的訂票
    @Modifying
    @Query("UPDATE Booking b SET b.status = 'CANCELLED', b.cancelTime = CURRENT_TIMESTAMP " +
            "WHERE b.status = 'PENDING' AND b.createdAt < :timeout")
    int cancelExpiredBookings(@Param("timeout") LocalDateTime timeout);

    // 查詢會員在指定時間範圍內的訂票數量
    @Query("SELECT COUNT(b) FROM Booking b WHERE b.member = :member " +
            "AND b.createdAt BETWEEN :startTime AND :endTime")
    long countBookingsByMemberInTimeRange(
            @Param("member") Member member,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    // 查詢場次的已訂座位
    @Query("SELECT sb.seatNumber FROM Booking b JOIN b.seatBookings sb " +
            "WHERE b.schedule = :schedule AND b.status != 'CANCELLED'")
    List<String> findBookedSeatsBySchedule(@Param("schedule") MovieSchedule schedule);

    // 使用悲觀鎖查詢訂票信息
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT b FROM Booking b WHERE b.id = :id")
    Optional<Booking> findByIdWithLock(@Param("id") Long id);

    // 統計各狀態的訂票數量
    @Query("SELECT b.status, COUNT(b) FROM Booking b GROUP BY b.status")
    List<Object[]> countBookingsByStatus();

    // 查詢會員的訂票統計信息
    @Query("SELECT COUNT(b), SUM(b.finalAmount) FROM Booking b " +
            "WHERE b.member = :member AND b.status = 'COMPLETED'")
    Object[] getMemberBookingStatistics(@Param("member") Member member);

    // 查詢最近的訂票記錄
    Page<Booking> findTop10ByOrderByCreatedAtDesc(Pageable pageable);

    // 查詢特定金額範圍的訂票
    List<Booking> findByFinalAmountBetween(Double minAmount, Double maxAmount);

    // 查詢使用特定優惠的訂票
    List<Booking> findByDiscountId(Long discountId);

    // 檢查座位是否已被預訂
    @Query("SELECT COUNT(b) > 0 FROM Booking b JOIN b.seatBookings sb " +
            "WHERE b.schedule = :schedule AND sb.seatNumber = :seatNumber " +
            "AND b.status != 'CANCELLED'")
    boolean isSeatBooked(
            @Param("schedule") MovieSchedule schedule,
            @Param("seatNumber") String seatNumber
    );

    // 刪除過期的訂票記錄（軟刪除）
    @Modifying
    @Query("UPDATE Booking b SET b.isDeleted = true " +
            "WHERE b.createdAt < :expiryDate AND b.status = 'CANCELLED'")
    int softDeleteExpiredBookings(@Param("expiryDate") LocalDateTime expiryDate);
}