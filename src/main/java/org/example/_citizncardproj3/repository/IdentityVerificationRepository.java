package org.example._citizncardproj3.repository;

import org.example._citizncardproj3.model.entity.IdentityVerification;
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
public interface IdentityVerificationRepository extends JpaRepository<IdentityVerification, Long> {

    // 基本查詢方法
    List<IdentityVerification> findByMember(Member member);

    List<IdentityVerification> findByStatus(IdentityVerification.VerificationStatus status);

    // 分頁查詢
    Page<IdentityVerification> findByStatusOrderByCreatedAtDesc(
            IdentityVerification.VerificationStatus status,
            Pageable pageable
    );

    Page<IdentityVerification> findByVerificationTypeOrderByCreatedAtDesc(
            IdentityVerification.VerificationType type,
            Pageable pageable
    );

    // 複雜條件查詢
    @Query("SELECT v FROM IdentityVerification v WHERE v.member = :member " +
            "AND v.verificationType = :type AND v.status = 'VERIFIED'")
    Optional<IdentityVerification> findValidVerification(
            @Param("member") Member member,
            @Param("type") IdentityVerification.VerificationType type
    );

    // 待處理驗證查詢
    @Query("SELECT v FROM IdentityVerification v WHERE v.status = 'PENDING' " +
            "ORDER BY v.createdAt ASC")
    List<IdentityVerification> findPendingVerifications();

    // 過期驗證查詢
    @Query("SELECT v FROM IdentityVerification v WHERE v.status = 'VERIFIED' " +
            "AND v.verificationTime < :expiryTime")
    List<IdentityVerification> findExpiredVerifications(
            @Param("expiryTime") LocalDateTime expiryTime
    );

    // 統計查詢
    @Query("SELECT v.verificationType, COUNT(v) FROM IdentityVerification v " +
            "GROUP BY v.verificationType")
    List<Object[]> countByVerificationType();

    @Query("SELECT v.status, COUNT(v) FROM IdentityVerification v " +
            "GROUP BY v.status")
    List<Object[]> countByStatus();

    // 更新操作
    @Modifying
    @Query("UPDATE IdentityVerification v SET v.status = :newStatus, " +
            "v.verificationTime = CURRENT_TIMESTAMP " +
            "WHERE v.verificationId = :verificationId")
    int updateVerificationStatus(
            @Param("verificationId") Long verificationId,
            @Param("newStatus") IdentityVerification.VerificationStatus newStatus
    );

    // 批量操作
    @Modifying
    @Query("UPDATE IdentityVerification v SET v.status = 'EXPIRED' " +
            "WHERE v.status = 'VERIFIED' AND v.verificationTime < :expiryTime")
    int expireOldVerifications(@Param("expiryTime") LocalDateTime expiryTime);

    // 使用悲觀鎖查詢
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT v FROM IdentityVerification v WHERE v.verificationId = :id")
    Optional<IdentityVerification> findByIdWithLock(@Param("id") Long id);

    // 查詢特定時間範圍內的驗證記錄
    @Query("SELECT v FROM IdentityVerification v WHERE " +
            "v.createdAt BETWEEN :startTime AND :endTime")
    List<IdentityVerification> findVerificationsInTimeRange(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    // 查詢需要重新驗證的記錄
    @Query("SELECT v FROM IdentityVerification v WHERE v.status = 'VERIFIED' " +
            "AND v.verificationTime < :reVerificationTime")
    List<IdentityVerification> findNeedReVerification(
            @Param("reVerificationTime") LocalDateTime reVerificationTime
    );

    // 查詢驗證失敗記錄
    @Query("SELECT v FROM IdentityVerification v WHERE v.status = 'REJECTED' " +
            "AND v.member = :member ORDER BY v.createdAt DESC")
    List<IdentityVerification> findFailedVerifications(@Param("member") Member member);

    // 根據會員和驗證類型查詢特定狀態的驗證
    Optional<IdentityVerification> findByMemberAndVerificationTypeAndStatus(
            Member member,
            IdentityVerification.VerificationType type,
            IdentityVerification.VerificationStatus status
    );

    // 根據狀態和驗證時間查詢
    List<IdentityVerification> findByStatusAndVerificationTimeBefore(
            IdentityVerification.VerificationStatus status,
            LocalDateTime time
    );
}