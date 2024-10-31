package org.example._citizncardproj3.repository;

import org.example._citizncardproj3.model.entity.CitizenCard;
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
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface CitizenCardRepository extends JpaRepository<CitizenCard, String> {

    // 基本查詢方法
    Optional<CitizenCard> findByCardNumber(String cardNumber);

    List<CitizenCard> findByMember(Member member);

    List<CitizenCard> findByMemberAndStatus(Member member, CitizenCard.CardStatus status);

    boolean existsByCardNumber(String cardNumber);

    // 分頁查詢
    Page<CitizenCard> findByCardType(CitizenCard.CardType cardType, Pageable pageable);

    Page<CitizenCard> findByStatus(CitizenCard.CardStatus status, Pageable pageable);

    // 複雜條件查詢
    @Query("SELECT c FROM CitizenCard c WHERE c.member = :member AND c.status = :status " +
            "AND c.expiryDate > :currentDate")
    List<CitizenCard> findValidCards(
            @Param("member") Member member,
            @Param("status") CitizenCard.CardStatus status,
            @Param("currentDate") LocalDate currentDate
    );

    // 即將過期的卡片查詢
    @Query("SELECT c FROM CitizenCard c WHERE c.status = 'ACTIVE' " +
            "AND c.expiryDate BETWEEN :startDate AND :endDate")
    List<CitizenCard> findExpiringCards(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    // 統計查詢
    @Query("SELECT c.cardType, COUNT(c) FROM CitizenCard c GROUP BY c.cardType")
    List<Object[]> countByCardType();

    @Query("SELECT COUNT(c) FROM CitizenCard c WHERE c.status = :status")
    long countByStatus(@Param("status") CitizenCard.CardStatus status);

    // 更新操作
    @Modifying
    @Query("UPDATE CitizenCard c SET c.status = :newStatus WHERE c.cardNumber = :cardNumber")
    int updateCardStatus(
            @Param("cardNumber") String cardNumber,
            @Param("newStatus") CitizenCard.CardStatus newStatus
    );

    @Modifying
    @Query("UPDATE CitizenCard c SET c.expiryDate = :newExpiryDate " +
            "WHERE c.cardNumber = :cardNumber")
    int updateExpiryDate(
            @Param("cardNumber") String cardNumber,
            @Param("newExpiryDate") LocalDate newExpiryDate
    );

    // 批量操作
    @Modifying
    @Query("UPDATE CitizenCard c SET c.status = 'EXPIRED' " +
            "WHERE c.expiryDate < :currentDate AND c.status = 'ACTIVE'")
    int updateExpiredCards(@Param("currentDate") LocalDate currentDate);

    // 使用悲觀鎖查詢
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM CitizenCard c WHERE c.cardNumber = :cardNumber")
    Optional<CitizenCard> findByCardNumberWithLock(@Param("cardNumber") String cardNumber);

    // 自定義查詢
    @Query(value = "SELECT * FROM citizen_cards c " +
            "WHERE c.card_type = :cardType " +
            "AND c.status = 'ACTIVE' " +
            "AND DATE_ADD(c.issue_date, INTERVAL 1 YEAR) > CURRENT_DATE",
            nativeQuery = true)
    List<CitizenCard> findActiveCardsByType(@Param("cardType") String cardType);

    // 查詢特定持卡人的所有有效卡片
    List<CitizenCard> findByHolderNameAndStatusAndExpiryDateAfter(
            String holderName,
            CitizenCard.CardStatus status,
            LocalDate currentDate
    );

    // 查詢需要續期的卡片
    @Query("SELECT c FROM CitizenCard c WHERE c.status = 'ACTIVE' " +
            "AND c.expiryDate <= :warningDate")
    List<CitizenCard> findCardsNeedingRenewal(@Param("warningDate") LocalDate warningDate);

    // 軟刪除
    @Modifying
    @Query("UPDATE CitizenCard c SET c.isDeleted = true WHERE c.cardNumber = :cardNumber")
    int softDeleteCard(@Param("cardNumber") String cardNumber);
}