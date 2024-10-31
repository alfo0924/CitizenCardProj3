package org.example._citizncardproj3.repository;

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
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {

    // 基本查詢方法
    Optional<Member> findByEmail(String email);

    Optional<Member> findByPhone(String phone);

    boolean existsByEmail(String email);

    boolean existsByPhone(String phone);

    // 分頁查詢
    Page<Member> findByStatus(Member.MemberStatus status, Pageable pageable);

    Page<Member> findByRole(Member.Role role, Pageable pageable);

    // 複雜條件查詢
    @Query("SELECT m FROM Member m WHERE m.status = :status " +
            "AND m.lastLoginTime < :lastLoginTime")
    List<Member> findInactiveMembers(
            @Param("status") Member.MemberStatus status,
            @Param("lastLoginTime") LocalDateTime lastLoginTime
    );

    // 搜索查詢
    @Query("SELECT m FROM Member m WHERE " +
            "LOWER(m.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(m.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "m.phone LIKE CONCAT('%', :keyword, '%')")
    Page<Member> searchMembers(@Param("keyword") String keyword, Pageable pageable);

    // 統計查詢
    @Query("SELECT m.status, COUNT(m) FROM Member m GROUP BY m.status")
    List<Object[]> countByStatus();

    @Query("SELECT COUNT(m) FROM Member m WHERE m.createdAt BETWEEN :startDate AND :endDate")
    long countNewMembersInPeriod(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    // 更新操作
    @Modifying
    @Query("UPDATE Member m SET m.status = :newStatus WHERE m.memberId = :memberId")
    int updateMemberStatus(
            @Param("memberId") Long memberId,
            @Param("newStatus") Member.MemberStatus newStatus
    );

    @Modifying
    @Query("UPDATE Member m SET m.failedLoginAttempts = 0 WHERE m.memberId = :memberId")
    int resetFailedLoginAttempts(@Param("memberId") Long memberId);

    // 批量操作
    @Modifying
    @Query("UPDATE Member m SET m.status = 'INACTIVE' " +
            "WHERE m.lastLoginTime < :inactiveDate AND m.status = 'ACTIVE'")
    int deactivateInactiveMembers(@Param("inactiveDate") LocalDateTime inactiveDate);

    // 使用悲觀鎖查詢
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT m FROM Member m WHERE m.memberId = :memberId")
    Optional<Member> findByIdWithLock(@Param("memberId") Long memberId);

    // 查詢生日會員
    @Query("SELECT m FROM Member m WHERE " +
            "MONTH(m.birthday) = MONTH(CURRENT_DATE) AND " +
            "DAY(m.birthday) = DAY(CURRENT_DATE)")
    List<Member> findMembersWithBirthdayToday();

    // 查詢特定年齡範圍的會員
    @Query("SELECT m FROM Member m WHERE " +
            "YEAR(CURRENT_DATE) - YEAR(m.birthday) BETWEEN :minAge AND :maxAge")
    List<Member> findMembersByAgeRange(
            @Param("minAge") int minAge,
            @Param("maxAge") int maxAge
    );

    // 查詢需要更新密碼的會員
    @Query("SELECT m FROM Member m WHERE m.lastPasswordChangeTime < :expiryDate")
    List<Member> findMembersNeedingPasswordUpdate(
            @Param("expiryDate") LocalDateTime expiryDate
    );

    // 查詢最近登入的會員
    @Query("SELECT m FROM Member m WHERE m.lastLoginTime IS NOT NULL " +
            "ORDER BY m.lastLoginTime DESC")
    List<Member> findRecentlyActiveMembers(Pageable pageable);

    // 查詢特定卡片類型的會員
    @Query("SELECT m FROM Member m JOIN m.citizenCards c " +
            "WHERE c.cardType = :cardType")
    List<Member> findMembersByCardType(
            @Param("cardType") String cardType
    );

    // 查詢消費統計
    @Query("SELECT m, COUNT(b), SUM(b.finalAmount) FROM Member m " +
            "LEFT JOIN m.bookings b WHERE b.status = 'COMPLETED' " +
            "GROUP BY m")
    List<Object[]> getMemberBookingStatistics();

    // 軟刪除
    @Modifying
    @Query("UPDATE Member m SET m.isDeleted = true WHERE m.memberId = :memberId")
    int softDeleteMember(@Param("memberId") Long memberId);

    /**
     * 根據多個email查詢會員
     */
    List<Member> findByEmailIn(List<String> emails);

    /**
     * 根據email列表查詢活躍會員
     */
    @Query("SELECT m FROM Member m WHERE m.email IN :emails " +
            "AND m.status = 'ACTIVE' AND m.isDeleted = false")
    List<Member> findActiveByEmailIn(@Param("emails") List<String> emails);

    /**
     * 根據email列表查詢並檢查通知設定
     */
    @Query("SELECT m FROM Member m WHERE m.email IN :emails " +
            "AND m.status = 'ACTIVE' " +
            "AND m.isDeleted = false")
    List<Member> findNotifiableByEmailIn(@Param("emails") List<String> emails);

    /**
     * 批量更新會員的最後通知時間
     */
    @Modifying
    @Query("UPDATE Member m SET m.lastNotificationTime = CURRENT_TIMESTAMP " +
            "WHERE m.email IN :emails")
    void updateLastNotificationTime(@Param("emails") List<String> emails);

}