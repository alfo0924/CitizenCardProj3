package org.example._citizncardproj3.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example._citizncardproj3.exception.CustomException;
import org.example._citizncardproj3.model.entity.Member;
import org.example._citizncardproj3.model.entity.VirtualCard;
import org.example._citizncardproj3.repository.MemberRepository;
import org.example._citizncardproj3.repository.VirtualCardRepository;
import org.example._citizncardproj3.service.NotificationService;
import org.example._citizncardproj3.service.VirtualCardService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class VirtualCardServiceImpl implements VirtualCardService {

    private final VirtualCardRepository virtualCardRepository;
    private final MemberRepository memberRepository;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public VirtualCard createVirtualCard(String userEmail, String phoneNumber) {
        Member member = memberRepository.findByEmail(userEmail)
                .orElseThrow(() -> new CustomException.MemberNotFoundException(userEmail));

        // 檢查是否已有虛擬卡
        if (virtualCardRepository.findByMember(member).isPresent()) {
            throw new IllegalStateException("會員已有虛擬卡");
        }

        // 創建虛擬卡
        VirtualCard virtualCard = VirtualCard.builder()
                .member(member)
                .boundPhoneNumber(phoneNumber)
                .status(VirtualCard.CardStatus.INACTIVE)
                .build();

        virtualCard = virtualCardRepository.save(virtualCard);

        // 發送通知
        notificationService.createSecurityNotification(
                userEmail,
                "虛擬卡創建",
                "系統"
        );

        return virtualCard;
    }

    @Override
    @Transactional
    public VirtualCard bindDevice(String userEmail, String deviceId, String deviceName) {
        Member member = memberRepository.findByEmail(userEmail)
                .orElseThrow(() -> new CustomException.MemberNotFoundException(userEmail));

        VirtualCard virtualCard = virtualCardRepository.findByMember(member)
                .orElseThrow(() -> new IllegalStateException("會員尚未創建虛擬卡"));

        // 檢查設備是否已被綁定
        if (virtualCardRepository.isDeviceRegistered(deviceId)) {
            throw new IllegalStateException("此設備已被其他虛擬卡綁定");
        }

        virtualCard.bindDevice(deviceId, deviceName);
        virtualCard = virtualCardRepository.save(virtualCard);

        // 發送通知
        notificationService.createSecurityNotification(
                userEmail,
                "設備綁定",
                deviceName
        );

        return virtualCard;
    }

    @Override
    @Transactional
    public VirtualCard unbindDevice(String userEmail, String reason) {
        Member member = memberRepository.findByEmail(userEmail)
                .orElseThrow(() -> new CustomException.MemberNotFoundException(userEmail));

        VirtualCard virtualCard = virtualCardRepository.findByMember(member)
                .orElseThrow(() -> new IllegalStateException("會員尚未創建虛擬卡"));

        String deviceName = virtualCard.getDeviceName();
        virtualCard.unbindDevice(reason);
        virtualCard = virtualCardRepository.save(virtualCard);

        // 發送通知
        notificationService.createSecurityNotification(
                userEmail,
                "設備解綁",
                deviceName
        );

        return virtualCard;
    }

    @Override
    @Transactional
    public VirtualCard activateCard(String userEmail) {
        Member member = memberRepository.findByEmail(userEmail)
                .orElseThrow(() -> new CustomException.MemberNotFoundException(userEmail));

        VirtualCard virtualCard = virtualCardRepository.findByMember(member)
                .orElseThrow(() -> new IllegalStateException("會員尚未創建虛擬卡"));

        if (virtualCard.getDeviceId() == null) {
            throw new IllegalStateException("需要先綁定設備才能啟用虛擬卡");
        }

        virtualCard.activate();
        return virtualCardRepository.save(virtualCard);
    }

    @Override
    @Transactional
    public VirtualCard suspendCard(String userEmail, String reason) {
        Member member = memberRepository.findByEmail(userEmail)
                .orElseThrow(() -> new CustomException.MemberNotFoundException(userEmail));

        VirtualCard virtualCard = virtualCardRepository.findByMember(member)
                .orElseThrow(() -> new IllegalStateException("會員尚未創建虛擬卡"));

        virtualCard.suspend(reason);
        virtualCard = virtualCardRepository.save(virtualCard);

        // 發送通知
        notificationService.createSecurityNotification(
                userEmail,
                "虛擬卡停用",
                "系統"
        );

        return virtualCard;
    }

    @Override
    public VirtualCard getVirtualCard(String userEmail) {
        Member member = memberRepository.findByEmail(userEmail)
                .orElseThrow(() -> new CustomException.MemberNotFoundException(userEmail));

        return virtualCardRepository.findByMember(member)
                .orElseThrow(() -> new IllegalStateException("會員尚未創建虛擬卡"));
    }

    @Override
    @Transactional
    public void updateLastUsedTime(String userEmail) {
        Member member = memberRepository.findByEmail(userEmail)
                .orElseThrow(() -> new CustomException.MemberNotFoundException(userEmail));

        VirtualCard virtualCard = virtualCardRepository.findByMember(member)
                .orElseThrow(() -> new IllegalStateException("會員尚未創建虛擬卡"));

        virtualCard.updateLastUsedTime();
        virtualCardRepository.save(virtualCard);
    }

    @Override
    @Transactional
    public void deactivateUnusedCards() {
        // 設定未使用時間閾值（例如：3個月）
        LocalDateTime threshold = LocalDateTime.now().minusMonths(3);
        virtualCardRepository.deactivateUnusedCards(threshold);
    }

    @Override
    public boolean validateDevice(String userEmail, String deviceId) {
        Member member = memberRepository.findByEmail(userEmail)
                .orElseThrow(() -> new CustomException.MemberNotFoundException(userEmail));

        VirtualCard virtualCard = virtualCardRepository.findByMember(member)
                .orElseThrow(() -> new IllegalStateException("會員尚未創建虛擬卡"));

        return virtualCard.validateDevice(deviceId);
    }
}