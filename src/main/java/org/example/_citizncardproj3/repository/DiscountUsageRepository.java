package org.example._citizncardproj3.repository;

import org.example._citizncardproj3.model.entity.Discount;
import org.example._citizncardproj3.model.entity.DiscountUsage;
import org.example._citizncardproj3.model.entity.Member;
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
public interface DiscountUsageRepository extends JpaRepository<DiscountUsage, Long> {

    // 基本查詢方法
    List<DiscountUsage> findByMember(Member member);

    List<DiscountUsage> findByDiscount(Discount discount);

    List<DiscountUsage> findByStatus(DiscountUsage.UsageStatus status);

    // 分頁查詢
    Page<DiscountUsage> findByMemberOrderByUsageTimeDesc(Member member, Pageable pageable);

    Page<DiscountUsage> findByDiscountOrderByUsageTimeDesc(Discount discount, Pageable pageable);

    // 複雜條件查詢
    @Query("SELECT du FROM DiscountUsage du WHERE du.member = :member " +
            "AND du.usageTime BETWEEN :startTime AND :endTime")
    List<DiscountUsage> findMemberUsageHistory(
            @Param("member") Member member,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    // 檢查優惠使用次數
    @Query("SELECT COUNT(du) FROM DiscountUsage du WHERE du.member = :member " +
            "AND du.discount = :discount AND du.status = 'USED'")
    long countMemberDiscountUsage(
            @Param("member") Member member,
            @Param("discount") Discount discount
    );

    // 統計查詢
    @Query("SELECT du.status, COUNT(du) FROM DiscountUsage du GROUP BY du.status")
    List<Object[]> countByStatus();

    @Query("SELECT SUM(du.usageAmount) FROM DiscountUsage du WHERE du.status = 'USED' " +
            "AND du.usageTime BETWEEN :startTime AND :endTime")
    Double calculateTotalDiscountAmount(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    // 更新操作
    @Modifying
    @Query("UPDATE DiscountUsage du SET du.status = :newStatus WHERE du.usageId = :usageId")
    int updateUsageStatus(
            @Param("usageId") Long usageId,
            @Param("newStatus") DiscountUsage.UsageStatus newStatus
    );

    // 批量操作
    @Modifying
    @Query("UPDATE DiscountUsage du SET du.status = 'CANCELLED' " +
            "WHERE du.booking.status = 'CANCELLED' AND du.status = 'USED'")
    int cancelUsageForCancelledBookings();

    // 使用悲觀鎖查詢
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT du FROM DiscountUsage du WHERE du.usageId = :usageId")
    Optional<DiscountUsage> findByIdWithLock(@Param("usageId") Long usageId);

    // 查詢特定金額範圍的使用記錄
    List<DiscountUsage> findByUsageAmountBetween(Double minAmount, Double maxAmount);

    // 查詢最近的使用記錄
    @Query("SELECT du FROM DiscountUsage du WHERE du.member = :member " +
            "ORDER BY du.usageTime DESC")
    List<DiscountUsage> findRecentUsage(
            @Param("member") Member member,
            Pageable pageable
    );

    // 查詢使用次數最多的優惠
    @Query("SELECT du.discount, COUNT(du) as useCount FROM DiscountUsage du " +
            "WHERE du.status = 'USED' GROUP BY du.discount " +
            "ORDER BY useCount DESC")
    List<Object[]> findMostUsedDiscounts(Pageable pageable);

    // 查詢會員優惠使用統計
    @Query("SELECT du.member, COUNT(du) as useCount, SUM(du.usageAmount) as totalAmount " +
            "FROM DiscountUsage du WHERE du.status = 'USED' GROUP BY du.member")
    List<Object[]> getMemberUsageStatistics();

    // 查詢可退款的使用記錄
    @Query("SELECT du FROM DiscountUsage du WHERE du.status = 'USED' " +
            "AND du.usageTime > :refundableTime")
    List<DiscountUsage> findRefundableUsages(
            @Param("refundableTime") LocalDateTime refundableTime
    );

    // 計數查詢
    long countByMemberAndDiscountAndStatus(
            Member member,
            Discount discount,
            DiscountUsage.UsageStatus status
    );
}