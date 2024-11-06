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

    boolean existsByDiscountCode(String discountCode);

    // 分頁查詢
    Page<Discount> findByIsDeletedFalseOrderByValidFromDesc(Pageable pageable);

    Page<Discount> findByDiscountTypeAndIsDeletedFalse(
            Discount.DiscountType discountType,
            Pageable pageable
    );

    // 有效優惠查詢
    @Query("SELECT d FROM Discount d WHERE d.isDeleted = false " +
            "AND :currentTime BETWEEN d.validFrom AND d.validUntil " +
            "AND (d.usageLimit IS NULL OR " +
            "(SELECT COUNT(u) FROM DiscountUsage u WHERE u.discount = d AND u.status = 'USED') < d.usageLimit)")
    List<Discount> findValidDiscounts(@Param("currentTime") LocalDateTime currentTime);

    // 即將到期優惠查詢
    @Query("SELECT d FROM Discount d WHERE d.isDeleted = false " +
            "AND d.validUntil BETWEEN :startTime AND :endTime")
    List<Discount> findExpiringDiscounts(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    // 特定金額可用優惠查詢
    @Query("SELECT d FROM Discount d WHERE d.isDeleted = false " +
            "AND d.minPurchaseAmount <= :amount " +
            "AND :currentTime BETWEEN d.validFrom AND d.validUntil")
    List<Discount> findApplicableDiscounts(
            @Param("amount") Double amount,
            @Param("currentTime") LocalDateTime currentTime
    );

    // 統計查詢
    @Query("SELECT d.discountType, COUNT(d) FROM Discount d " +
            "WHERE d.isDeleted = false GROUP BY d.discountType")
    List<Object[]> countByDiscountType();

    @Query("SELECT COUNT(d) FROM Discount d WHERE d.isDeleted = false " +
            "AND :currentTime BETWEEN d.validFrom AND d.validUntil")
    long countActiveDiscounts(@Param("currentTime") LocalDateTime currentTime);

    // 更新操作
    @Modifying
    @Query("UPDATE Discount d SET d.isDeleted = true, d.deletedAt = CURRENT_TIMESTAMP " +
            "WHERE d.discountId = :discountId")
    int softDeleteDiscount(@Param("discountId") Long discountId);

    // 使用悲觀鎖查詢
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT d FROM Discount d WHERE d.discountId = :discountId")
    Optional<Discount> findByIdWithLock(@Param("discountId") Long discountId);

    // 自定義查詢
    List<Discount> findByDiscountValueGreaterThanAndIsDeletedFalse(Double minValue);

    List<Discount> findByMaxDiscountAmountLessThanEqual(Double maxAmount);

    // 查詢特定時間範圍內的優惠
    @Query("SELECT d FROM Discount d WHERE d.isDeleted = false AND " +
            "((d.validFrom BETWEEN :startTime AND :endTime) OR " +
            "(d.validUntil BETWEEN :startTime AND :endTime))")
    List<Discount> findDiscountsInTimeRange(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    // 查詢使用次數最多的優惠
    @Query("SELECT d FROM Discount d WHERE d.isDeleted = false " +
            "ORDER BY (SELECT COUNT(u) FROM DiscountUsage u WHERE u.discount = d AND u.status = 'USED') DESC")
    List<Discount> findMostUsedDiscounts(Pageable pageable);

    // 查詢優惠使用次數
    @Query("SELECT COUNT(u) FROM DiscountUsage u WHERE u.discount.discountId = :discountId " +
            "AND u.status = 'USED'")
    long getDiscountUsageCount(@Param("discountId") Long discountId);


    @Query("SELECT d FROM Discount d WHERE d.isDeleted = false ORDER BY d.validFrom DESC")
    Page<Discount> findAllActive(Pageable pageable);



}