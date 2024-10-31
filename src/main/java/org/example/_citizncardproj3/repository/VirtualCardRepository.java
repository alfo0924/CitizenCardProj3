package org.example._citizncardproj3.repository;

import org.example._citizncardproj3.model.entity.Member;
import org.example._citizncardproj3.model.entity.VirtualCard;
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
public interface VirtualCardRepository extends JpaRepository<VirtualCard, Long> {

    // 基本查詢方法
    Optional<VirtualCard> findByVirtualCardNumber(String virtualCardNumber);

    Optional<VirtualCard> findByMember(Member member);

    Optional<VirtualCard> findByDeviceId(String deviceId);

    List<VirtualCard> findByStatus(VirtualCard.CardStatus status);

    // 分頁查詢
    Page<VirtualCard> findByStatusOrderByCreatedAtDesc(
            VirtualCard.CardStatus status,
            Pageable pageable
    );

    // 複雜條件查詢
    @Query("SELECT vc FROM VirtualCard vc WHERE vc.member = :member " +
            "AND vc.status = 'ACTIVE' AND vc.deviceId IS NOT NULL")
    Optional<VirtualCard> findActiveVirtualCard(
            @Param("member") Member member
    );

    // 查詢需要重新綁定的卡片
    @Query("SELECT vc FROM VirtualCard vc WHERE vc.status = 'UNBOUND' " +
            "OR vc.deviceId IS NULL")
    List<VirtualCard> findCardsNeedingRebinding();

    // 統計查詢
    @Query("SELECT vc.status, COUNT(vc) FROM VirtualCard vc GROUP BY vc.status")
    List<Object[]> countByStatus();

    @Query("SELECT COUNT(vc) FROM VirtualCard vc WHERE vc.member = :member " +
            "AND vc.status = 'ACTIVE'")
    long countActiveCardsByMember(@Param("member") Member member);

    // 更新操作
    @Modifying
    @Query("UPDATE VirtualCard vc SET vc.status = :newStatus " +
            "WHERE vc.virtualCardId = :cardId")
    int updateCardStatus(
            @Param("cardId") Long cardId,
            @Param("newStatus") VirtualCard.CardStatus newStatus
    );

    @Modifying
    @Query("UPDATE VirtualCard vc SET vc.deviceId = :deviceId, " +
            "vc.deviceName = :deviceName, vc.boundTime = CURRENT_TIMESTAMP " +
            "WHERE vc.virtualCardId = :cardId")
    int updateDeviceInfo(
            @Param("cardId") Long cardId,
            @Param("deviceId") String deviceId,
            @Param("deviceName") String deviceName
    );

    // 批量操作
    @Modifying
    @Query("UPDATE VirtualCard vc SET vc.status = 'INACTIVE' " +
            "WHERE vc.lastUsedTime < :inactiveTime")
    int deactivateUnusedCards(
            @Param("inactiveTime") LocalDateTime inactiveTime
    );

    // 使用悲觀鎖查詢
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT vc FROM VirtualCard vc WHERE vc.virtualCardId = :cardId")
    Optional<VirtualCard> findByIdWithLock(@Param("cardId") Long cardId);

    // 查詢特定手機號碼綁定的卡片
    List<VirtualCard> findByBoundPhoneNumber(String phoneNumber);

    // 查詢最近使用的卡片
    @Query("SELECT vc FROM VirtualCard vc WHERE vc.status = 'ACTIVE' " +
            "ORDER BY vc.lastUsedTime DESC")
    List<VirtualCard> findRecentlyUsedCards(Pageable pageable);

    // 查詢需要更新的卡片
    @Query("SELECT vc FROM VirtualCard vc WHERE vc.status = 'ACTIVE' " +
            "AND (vc.lastUsedTime IS NULL OR vc.lastUsedTime < :updateTime)")
    List<VirtualCard> findCardsNeedingUpdate(
            @Param("updateTime") LocalDateTime updateTime
    );

    // 驗證設備
    @Query("SELECT CASE WHEN COUNT(vc) > 0 THEN true ELSE false END " +
            "FROM VirtualCard vc WHERE vc.deviceId = :deviceId " +
            "AND vc.status = 'ACTIVE'")
    boolean isDeviceRegistered(@Param("deviceId") String deviceId);

    // 查詢解綁記錄
    @Query("SELECT vc FROM VirtualCard vc WHERE vc.status = 'UNBOUND' " +
            "AND vc.unboundTime BETWEEN :startTime AND :endTime")
    List<VirtualCard> findUnboundRecords(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    // 軟刪除
    @Modifying
    @Query("UPDATE VirtualCard vc SET vc.isDeleted = true " +
            "WHERE vc.virtualCardId = :cardId")
    int softDeleteCard(@Param("cardId") Long cardId);
}