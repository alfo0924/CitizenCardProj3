package org.example._citizncardproj3.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example._citizncardproj3.exception.CustomException;
import org.example._citizncardproj3.model.entity.Member;
import org.example._citizncardproj3.model.entity.Notification;
import org.example._citizncardproj3.repository.MemberRepository;
import org.example._citizncardproj3.repository.NotificationRepository;
import org.example._citizncardproj3.service.NotificationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final MemberRepository memberRepository;

    @Override
    @Transactional
    public Notification createNotification(
            String userEmail,
            String title,
            String content,
            Notification.NotificationType type,
            Notification.NotificationPriority priority) {

        Member member = memberRepository.findByEmail(userEmail)
                .orElseThrow(() -> new CustomException.MemberNotFoundException(userEmail));

        Notification notification = Notification.builder()
                .member(member)
                .title(title)
                .content(content)
                .type(type)
                .priority(priority)
                .isRead(false)
                .isSent(false)
                .sendTime(LocalDateTime.now())
                .build();

        return notificationRepository.save(notification);
    }

    @Override
    @Transactional
    public void sendSystemNotification(
            String title,
            String content,
            List<String> userEmails,
            Notification.NotificationPriority priority) {

        List<Member> members = memberRepository.findByEmailIn(userEmails);

        for (Member member : members) {
            Notification notification = Notification.builder()
                    .member(member)
                    .title(title)
                    .content(content)
                    .type(Notification.NotificationType.SYSTEM)
                    .priority(priority)
                    .isRead(false)
                    .isSent(false)
                    .sendTime(LocalDateTime.now())
                    .build();

            notificationRepository.save(notification);
        }
    }

    @Override
    @Transactional
    public void markAsRead(String userEmail, Long notificationId) {
        Member member = memberRepository.findByEmail(userEmail)
                .orElseThrow(() -> new CustomException.MemberNotFoundException(userEmail));

        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("通知不存在"));

        if (!notification.getMember().equals(member)) {
            throw new IllegalStateException("無權限訪問此通知");
        }

        notification.markAsRead();
        notificationRepository.save(notification);
    }

    @Override
    @Transactional
    public void markAllAsRead(String userEmail) {
        Member member = memberRepository.findByEmail(userEmail)
                .orElseThrow(() -> new CustomException.MemberNotFoundException(userEmail));

        notificationRepository.markAllAsRead(member);
    }

    @Override
    public Page<Notification> getMemberNotifications(String userEmail, Pageable pageable) {
        Member member = memberRepository.findByEmail(userEmail)
                .orElseThrow(() -> new CustomException.MemberNotFoundException(userEmail));

        return notificationRepository.findByMemberOrderByCreatedAtDesc(member, pageable);
    }

    @Override
    public List<Notification> getUnreadNotifications(String userEmail) {
        Member member = memberRepository.findByEmail(userEmail)
                .orElseThrow(() -> new CustomException.MemberNotFoundException(userEmail));

        return notificationRepository.findUnreadValidNotifications(
                member,
                LocalDateTime.now()
        );
    }

    @Override
    public long getUnreadCount(String userEmail) {
        Member member = memberRepository.findByEmail(userEmail)
                .orElseThrow(() -> new CustomException.MemberNotFoundException(userEmail));

        return notificationRepository.countUnreadNotifications(member);
    }

    @Override
    @Transactional
    public void deleteExpiredNotifications() {
        LocalDateTime expiryTime = LocalDateTime.now().minusMonths(3); // 3個月後過期
        notificationRepository.deleteExpiredNotifications(expiryTime);
    }

    @Override
    @Transactional
    public void createBookingNotification(String userEmail, Long bookingId, String bookingNumber) {
        String title = "訂票成功通知";
        String content = String.format("您的訂票(編號: %s)已成功完成", bookingNumber);

        createNotification(
                userEmail,
                title,
                content,
                Notification.NotificationType.BOOKING,
                Notification.NotificationPriority.HIGH
        );
    }

    @Override
    @Transactional
    public void createPaymentNotification(String userEmail, Double amount, String transactionNumber) {
        String title = "支付成功通知";
        String content = String.format("您已成功支付 %.2f 元(交易編號: %s)", amount, transactionNumber);

        createNotification(
                userEmail,
                title,
                content,
                Notification.NotificationType.PAYMENT,
                Notification.NotificationPriority.MEDIUM
        );
    }

    @Override
    @Transactional
    public void createSecurityNotification(String userEmail, String action, String deviceInfo) {
        String title = "安全提醒";
        String content = String.format("您的帳戶在 %s 進行了 %s 操作", deviceInfo, action);

        createNotification(
                userEmail,
                title,
                content,
                Notification.NotificationType.SECURITY,
                Notification.NotificationPriority.HIGH
        );
    }
}