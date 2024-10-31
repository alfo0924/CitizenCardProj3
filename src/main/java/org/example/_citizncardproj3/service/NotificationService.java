package org.example._citizncardproj3.service;

import org.example._citizncardproj3.model.entity.Member;
import org.example._citizncardproj3.model.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface NotificationService {

    /**
     * 創建通知
     * @param userEmail 用戶郵箱
     * @param title 標題
     * @param content 內容
     * @param type 通知類型
     * @param priority 優先級
     * @return 通知實體
     */
    Notification createNotification(
            String userEmail,
            String title,
            String content,
            Notification.NotificationType type,
            Notification.NotificationPriority priority
    );

    /**
     * 發送系統通知
     * @param title 標題
     * @param content 內容
     * @param userEmails 用戶郵箱列表
     * @param priority 優先級
     */
    void sendSystemNotification(
            String title,
            String content,
            List<String> userEmails,
            Notification.NotificationPriority priority
    );

    /**
     * 標記通知為已讀
     * @param userEmail 用戶郵箱
     * @param notificationId 通知ID
     */
    void markAsRead(String userEmail, Long notificationId);

    /**
     * 標記所有通知為已讀
     * @param userEmail 用戶郵箱
     */
    void markAllAsRead(String userEmail);

    /**
     * 獲取會員通知列表
     * @param userEmail 用戶郵箱
     * @param pageable 分頁參數
     * @return 通知分頁
     */
    Page<Notification> getMemberNotifications(String userEmail, Pageable pageable);

    /**
     * 獲取未讀通知列表
     * @param userEmail 用戶郵箱
     * @return 未讀通知列表
     */
    List<Notification> getUnreadNotifications(String userEmail);

    /**
     * 獲取未讀通知數量
     * @param userEmail 用戶郵箱
     * @return 未讀數量
     */
    long getUnreadCount(String userEmail);

    /**
     * 刪除過期通知
     */
    void deleteExpiredNotifications();

    /**
     * 創建訂票通知
     * @param userEmail 用戶郵箱
     * @param bookingId 訂票ID
     * @param bookingNumber 訂票編號
     */
    void createBookingNotification(String userEmail, Long bookingId, String bookingNumber);

    /**
     * 創建支付通知
     * @param userEmail 用戶郵箱
     * @param amount 金額
     * @param transactionNumber 交易編號
     */
    void createPaymentNotification(String userEmail, Double amount, String transactionNumber);

    /**
     * 創建安全通知
     * @param userEmail 用戶郵箱
     * @param action 操作
     * @param deviceInfo 設備信息
     */
    void createSecurityNotification(String userEmail, String action, String deviceInfo);

    /**
     * 檢查通知是否可見
     * @param notificationId 通知ID
     * @param userEmail 用戶郵箱
     * @return 是否可見
     */
    boolean isNotificationVisible(Long notificationId, String userEmail);

    /**
     * 獲取特定類型的通知
     * @param userEmail 用戶郵箱
     * @param type 通知類型
     * @param pageable 分頁參數
     * @return 通知分頁
     */
    Page<Notification> getNotificationsByType(
            String userEmail,
            Notification.NotificationType type,
            Pageable pageable
    );

    /**
     * 獲取特定優先級的通知
     * @param userEmail 用戶郵箱
     * @param priority 優先級
     * @return 通知列表
     */
    List<Notification> getNotificationsByPriority(
            String userEmail,
            Notification.NotificationPriority priority
    );

    /**
     * 刪除通知
     * @param userEmail 用戶郵箱
     * @param notificationId 通知ID
     */
    void deleteNotification(String userEmail, Long notificationId);
}