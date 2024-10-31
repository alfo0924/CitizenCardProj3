package org.example._citizncardproj3.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example._citizncardproj3.exception.CustomException;
import org.example._citizncardproj3.model.entity.CitizenCard;
import org.example._citizncardproj3.model.entity.IdentityVerification;
import org.example._citizncardproj3.model.entity.Member;
import org.example._citizncardproj3.repository.CitizenCardRepository;
import org.example._citizncardproj3.repository.IdentityVerificationRepository;
import org.example._citizncardproj3.repository.MemberRepository;
import org.example._citizncardproj3.service.CitizenCardService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CitizenCardServiceImpl implements CitizenCardService {

    private final CitizenCardRepository citizenCardRepository;
    private final MemberRepository memberRepository;
    private final IdentityVerificationRepository verificationRepository;

    @Override
    @Transactional
    public CitizenCard createCard(String userEmail, CitizenCard.CardType cardType) {
        // 驗證會員
        Member member = memberRepository.findByEmail(userEmail)
                .orElseThrow(() -> new CustomException.MemberNotFoundException(userEmail));

        // 檢查是否已有相同類型的卡片
        boolean hasExistingCard = citizenCardRepository.findByMember(member).stream()
                .anyMatch(card -> card.getCardType() == cardType && card.isValid());

        if (hasExistingCard) {
            throw new IllegalStateException("已擁有相同類型的有效卡片");
        }

        // 驗證身份資格
        validateCardTypeEligibility(member, cardType);

        // 創建新卡片
        CitizenCard card = CitizenCard.builder()
                .member(member)
                .holderName(member.getName())
                .cardType(cardType)
                .status(CitizenCard.CardStatus.INACTIVE)
                .issueDate(LocalDate.now())
                .expiryDate(calculateExpiryDate(cardType))
                .build();

        return citizenCardRepository.save(card);
    }

    @Override
    @Transactional
    public CitizenCard activateCard(String cardNumber) {
        CitizenCard card = citizenCardRepository.findByCardNumber(cardNumber)
                .orElseThrow(() -> new IllegalArgumentException("卡片不存在"));

        // 檢查身份驗證狀態
        Optional<IdentityVerification> verification = verificationRepository.findValidVerification(
                card.getMember(),
                IdentityVerification.VerificationType.ID_CARD
        );

        if (verification.isEmpty()) {
            throw new IllegalStateException("需要完成身份驗證才能啟用卡片");
        }

        card.activate();
        return citizenCardRepository.save(card);
    }

    @Override
    @Transactional
    public CitizenCard suspendCard(String cardNumber, String reason) {
        CitizenCard card = citizenCardRepository.findByCardNumber(cardNumber)
                .orElseThrow(() -> new IllegalArgumentException("卡片不存在"));

        card.suspend(reason);
        return citizenCardRepository.save(card);
    }

    @Override
    @Transactional
    public CitizenCard renewCard(String cardNumber) {
        CitizenCard card = citizenCardRepository.findByCardNumber(cardNumber)
                .orElseThrow(() -> new IllegalArgumentException("卡片不存在"));

        // 檢查是否需要續期
        if (!card.needsRenewal()) {
            throw new IllegalStateException("卡片尚未到需要續期的時間");
        }

        // 驗證續期資格
        validateCardTypeEligibility(card.getMember(), card.getCardType());

        // 更新有效期
        card.updateExpiryDate(calculateExpiryDate(card.getCardType()));
        return citizenCardRepository.save(card);
    }

    @Override
    public List<CitizenCard> getMemberCards(String userEmail) {
        Member member = memberRepository.findByEmail(userEmail)
                .orElseThrow(() -> new CustomException.MemberNotFoundException(userEmail));

        return citizenCardRepository.findByMember(member);
    }

    @Override
    public boolean validateCard(String cardNumber) {
        return citizenCardRepository.findByCardNumber(cardNumber)
                .map(CitizenCard::isValid)
                .orElse(false);
    }

    // 私有輔助方法

    private void validateCardTypeEligibility(Member member, CitizenCard.CardType cardType) {
        int age = member.getAge();

        switch (cardType) {
            case SENIOR:
                if (age < 65) {
                    throw new IllegalStateException("需要年滿65歲才能申請敬老卡");
                }
                break;
            case STUDENT:
                if (age < 6 || age > 25) {
                    throw new IllegalStateException("學生卡僅供6-25歲申請");
                }
                break;
            case CHARITY:
                // 檢查是否有相關證明文件
                boolean hasVerification = verificationRepository.findValidVerification(
                        member,
                        IdentityVerification.VerificationType.DISABILITY_CARD
                ).isPresent();

                if (!hasVerification) {
                    throw new IllegalStateException("需要提供相關證明文件才能申請愛心卡");
                }
                break;
            case GENERAL:
                if (age < 18) {
                    throw new IllegalStateException("需要年滿18歲才能申請一般卡");
                }
                break;
        }
    }

    private LocalDate calculateExpiryDate(CitizenCard.CardType cardType) {
        LocalDate now = LocalDate.now();

        switch (cardType) {
            case SENIOR:
                return now.plusYears(5);  // 敬老卡5年有效期
            case STUDENT:
                return now.plusYears(1);  // 學生卡1年有效期
            case CHARITY:
                return now.plusYears(3);  // 愛心卡3年有效期
            default:
                return now.plusYears(10); // 一般卡10年有效期
        }
    }
}