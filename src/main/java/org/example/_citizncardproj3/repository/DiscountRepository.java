package org.example._citizncardproj3.repository;

import org.example._citizncardproj3.model.entity.Discount;
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
public interface DiscountRepository extends JpaRepository<Discount, Long> {

    // 基本查詢方法
    Optional<Discount> findByDiscountCode(String discountCode);

    List<Discount> findByIsActiveTrue();

    boolean existsByDiscountCode(String discountCode);

    // 分頁查詢
    Page<Discount> findByIsActiveTrueOrderByValidFromDesc(Pageable pageable);

    Page<Discount> findByDiscountTypeAndIsActiveTrue(
            Discount.DiscountType discountType,
            Pageable pageable
    );

    // 有效優惠查詢
    @Query("SELECT d FROM Discount d WHERE d.isActive = true " +
            "AND :currentTime BETWEEN d.validFrom AND d.validUntil " +
            "AND (d.usageLimit IS NULL OR d.usageCount < d.usageLimit)")
    List<Discount> findValidDiscounts(@Param("currentTime") LocalDateTime currentTime);

    // 即將到期優惠查詢
    @Query("SELECT d FROM Discount d WHERE d.isActive = true " +
            "AND d.validUntil BETWEEN :startTime AND :endTime")
    List<Discount> findExpiringDiscounts(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    // 特定金額可用優惠查詢
    @Query("SELECT d FROM Discount d WHERE d.isActive = true " +
            "AND d.minPurchaseAmount <= :amount " +
            "AND :currentTime BETWEEN d.validFrom AND d.validUntil")
    List<Discount> findApplicableDiscounts(
            @Param("amount") Double amount,
            @Param("currentTime") LocalDateTime currentTime
    );

    // 統計查詢
    @Query("SELECT d.discountType, COUNT(d) FROM Discount d GROUP BY d.discountType")
    List<Object[]> countByDiscountType();

    @Query("SELECT COUNT(d) FROM Discount d WHERE d.isActive = true " +
            "AND :currentTime BETWEEN d.validFrom AND d.validUntil")
    long countActiveDiscounts(@Param("currentTime") LocalDateTime currentTime);

    // 更新操作
    @Modifying
    @Query("UPDATE Discount d SET d.isActive = :active WHERE d.discountId = :discountId")
    int updateDiscountStatus(
            @Param("discountId") Long discountId,
            @Param("active") boolean active
    );

    @Modifying
    @Query("UPDATE Discount d SET d.usageCount = d.usageCount + 1 " +
            "WHERE d.discountId = :discountId")
    int incrementUsageCount(@Param("discountId") Long discountId);

    // 批量操作
    @Modifying
    @Query("UPDATE Discount d SET d.isActive = false " +
            "WHERE d.validUntil < :currentTime AND d.isActive = true")
    int deactivateExpiredDiscounts(@Param("currentTime") LocalDateTime currentTime);

    // 使用悲觀鎖查詢
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT d FROM Discount d WHERE d.discountId = :discountId")
    Optional<Discount> findByIdWithLock(@Param("discountId") Long discountId);

    // 自定義查詢
    List<Discount> findByDiscountValueGreaterThanAndIsActiveTrue(Double minValue);

    List<Discount> findByMaxDiscountAmountLessThanEqual(Double maxAmount);

    // 查詢特定時間範圍內的優惠
    @Query("SELECT d FROM Discount d WHERE " +
            "(d.validFrom BETWEEN :startTime AND :endTime) OR " +
            "(d.validUntil BETWEEN :startTime AND :endTime)")
    List<Discount> findDiscountsInTimeRange(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    // 查詢使用次數最多的優惠
    @Query("SELECT d FROM Discount d WHERE d.isActive = true " +
            "ORDER BY d.usageCount DESC")
    List<Discount> findMostUsedDiscounts(Pageable pageable);

    // 軟刪除
    @Modifying
    @Query("UPDATE Discount d SET d.isDeleted = true WHERE d.discountId = :discountId")
    int softDeleteDiscount(@Param("discountId") Long discountId);
}