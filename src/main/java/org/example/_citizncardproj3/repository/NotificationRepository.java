package org.example._citizncardproj3.repository;

import org.example._citizncardproj3.model.entity.Member;
import org.example._citizncardproj3.model.entity.Notification;
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
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // 基本查詢方法
    List<Notification> findByMember(Member member);

    List<Notification> findByMemberAndIsRead(Member member, Boolean isRead);

    List<Notification> findByType(Notification.NotificationType type);

    // 分頁查詢
    Page<Notification> findByMemberOrderByCreatedAtDesc(Member member, Pageable pageable);

    Page<Notification> findByTypeAndIsReadOrderByCreatedAtDesc(
            Notification.NotificationType type,
            Boolean isRead,
            Pageable pageable
    );

    // 複雜條件查詢
    @Query("SELECT n FROM Notification n WHERE n.member = :member " +
            "AND n.isRead = false AND n.isDeleted = false " +
            "AND (n.expireTime IS NULL OR n.expireTime > :currentTime)")
    List<Notification> findUnreadValidNotifications(
            @Param("member") Member member,
            @Param("currentTime") LocalDateTime currentTime
    );

    // 查詢需要立即處理的通知
    @Query("SELECT n FROM Notification n WHERE n.member = :member " +
            "AND n.priority IN ('HIGH', 'URGENT') AND n.isRead = false")
    List<Notification> findUrgentNotifications(@Param("member") Member member);

    // 統計查詢
    @Query("SELECT n.type, COUNT(n) FROM Notification n GROUP BY n.type")
    List<Object[]> countByType();

    @Query("SELECT COUNT(n) FROM Notification n WHERE n.member = :member " +
            "AND n.isRead = false")
    long countUnreadNotifications(@Param("member") Member member);

    // 更新操作
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true, n.readTime = CURRENT_TIMESTAMP " +
            "WHERE n.notificationId = :notificationId")
    int markAsRead(@Param("notificationId") Long notificationId);

    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true, n.readTime = CURRENT_TIMESTAMP " +
            "WHERE n.member = :member AND n.isRead = false")
    int markAllAsRead(@Param("member") Member member);

    // 批量操作
    @Modifying
    @Query("UPDATE Notification n SET n.isDeleted = true " +
            "WHERE n.expireTime < :currentTime AND n.isRead = true")
    int deleteExpiredNotifications(@Param("currentTime") LocalDateTime currentTime);

    // 使用悲觀鎖查詢
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT n FROM Notification n WHERE n.notificationId = :id")
    Optional<Notification> findByIdWithLock(@Param("id") Long id);

    // 查詢特定引用的通知
    List<Notification> findByReferenceTypeAndReferenceId(
            String referenceType,
            String referenceId
    );

    // 查詢特定時間範圍的通知
    @Query("SELECT n FROM Notification n WHERE n.member = :member " +
            "AND n.createdAt BETWEEN :startTime AND :endTime " +
            "ORDER BY n.createdAt DESC")
    List<Notification> findNotificationsInTimeRange(
            @Param("member") Member member,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    // 查詢需要重發的通知
    @Query("SELECT n FROM Notification n WHERE n.isSent = false " +
            "AND n.sendTime <= :currentTime")
    List<Notification> findNotificationsToResend(
            @Param("currentTime") LocalDateTime currentTime
    );

    // 查詢特定優先級的通知
    List<Notification> findByMemberAndPriorityOrderByCreatedAtDesc(
            Member member,
            Notification.NotificationPriority priority
    );

    // 查詢可見的通知
    @Query("SELECT n FROM Notification n WHERE n.member = :member " +
            "AND n.isDeleted = false " +
            "AND (n.expireTime IS NULL OR n.expireTime > CURRENT_TIMESTAMP)")
    List<Notification> findVisibleNotifications(@Param("member") Member member);

    // 軟刪除
    @Modifying
    @Query("UPDATE Notification n SET n.isDeleted = true " +
            "WHERE n.notificationId = :notificationId")
    int softDeleteNotification(@Param("notificationId") Long notificationId);
}