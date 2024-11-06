package org.example._citizncardproj3.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example._citizncardproj3.exception.CustomException;
import org.example._citizncardproj3.model.entity.IdentityVerification;
import org.example._citizncardproj3.model.entity.Member;
import org.example._citizncardproj3.repository.IdentityVerificationRepository;
import org.example._citizncardproj3.repository.MemberRepository;
import org.example._citizncardproj3.service.IdentityVerificationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class IdentityVerificationServiceImpl implements IdentityVerificationService {

    private final IdentityVerificationRepository verificationRepository;
    private final MemberRepository memberRepository;

    @Transactional
    @Override
    public IdentityVerification submitVerification(
            String userEmail,
            IdentityVerification.VerificationType type,
            MultipartFile documentFile) {

        Member member = memberRepository.findByEmail(userEmail)
                .orElseThrow(() -> new CustomException.MemberNotFoundException(userEmail));

        // 檢查是否有進行中的驗證
        Optional<IdentityVerification> existingVerification = verificationRepository
                .findByMemberAndVerificationTypeAndStatus(
                        member,
                        type,
                        IdentityVerification.VerificationStatus.PENDING
                );

        if (existingVerification.isPresent()) {
            throw new IllegalStateException("已有進行中的身份驗證");
        }

        // 驗證文件
        validateDocument(documentFile);

        // 創建驗證記錄
        IdentityVerification verification = IdentityVerification.builder()
                .member(member)
                .verificationType(type)
                .status(IdentityVerification.VerificationStatus.PENDING)
                .build();

        return verificationRepository.save(verification);
    }

    @Transactional
    @Override
    public IdentityVerification processVerification(Long verificationId, boolean approved) {
        IdentityVerification verification = verificationRepository.findById(verificationId)
                .orElseThrow(() -> new IllegalArgumentException("驗證記錄不存在"));

        if (verification.getStatus() != IdentityVerification.VerificationStatus.PENDING) {
            throw new IllegalStateException("只有待驗證的記錄可以進行驗證");
        }

        if (approved) {
            verification.setStatus(IdentityVerification.VerificationStatus.VERIFIED);
            verification.setVerificationTime(LocalDateTime.now());
        } else {
            verification.setStatus(IdentityVerification.VerificationStatus.REJECTED);
        }

        return verificationRepository.save(verification);
    }

    @Transactional
    @Override
    public IdentityVerification resubmitVerification(Long verificationId) {
        IdentityVerification verification = verificationRepository.findById(verificationId)
                .orElseThrow(() -> new IllegalArgumentException("驗證記錄不存在"));

        if (verification.getStatus() != IdentityVerification.VerificationStatus.REJECTED) {
            throw new IllegalStateException("只有被拒絕的驗證可以重新提交");
        }

        verification.setStatus(IdentityVerification.VerificationStatus.PENDING);
        return verificationRepository.save(verification);
    }

    @Override
    public IdentityVerification submitVerification(String userEmail, IdentityVerification.VerificationType type, String documentNumber, MultipartFile documentFile) {
        return null;
    }

    @Override
    public IdentityVerification verifyIdentity(Long verificationId, String verifier, boolean approved, String comment) {
        return null;
    }

    @Override
    public IdentityVerification updateDocument(Long verificationId, String documentNumber, MultipartFile documentFile) {
        return null;
    }

    @Override
    public List<IdentityVerification> getMemberVerifications(String userEmail) {
        Member member = memberRepository.findByEmail(userEmail)
                .orElseThrow(() -> new CustomException.MemberNotFoundException(userEmail));

        return verificationRepository.findByMember(member);
    }

    @Override
    public List<IdentityVerification> getPendingVerifications() {
        return verificationRepository.findByStatus(IdentityVerification.VerificationStatus.PENDING);
    }

    @Override
    @Transactional
    public void checkAndUpdateExpiredVerifications() {
        LocalDateTime expiryTime = LocalDateTime.now().minusYears(1);
        List<IdentityVerification> verifications = verificationRepository
                .findByStatusAndVerificationTimeBefore(
                        IdentityVerification.VerificationStatus.VERIFIED,
                        expiryTime
                );

        for (IdentityVerification verification : verifications) {
            verification.setStatus(IdentityVerification.VerificationStatus.EXPIRED);
            verificationRepository.save(verification);
        }
    }

    @Override
    public boolean validateDocumentNumber(String documentNumber, IdentityVerification.VerificationType type) {
        return false;
    }

    @Override
    public boolean needsReVerification(Long verificationId) {
        return false;
    }

    @Override
    public IdentityVerification getValidVerification(String userEmail, IdentityVerification.VerificationType type) {
        return null;
    }

    @Override
    public void cancelVerification(Long verificationId, String reason) {

    }

    @Override
    public Map<String, Object> getVerificationStatistics(LocalDateTime startTime, LocalDateTime endTime) {
        return Map.of();
    }

    @Override
    public boolean hasValidVerification(String userEmail, IdentityVerification.VerificationType type) {
        Member member = memberRepository.findByEmail(userEmail)
                .orElseThrow(() -> new CustomException.MemberNotFoundException(userEmail));

        return verificationRepository
                .findByMemberAndVerificationTypeAndStatus(
                        member,
                        type,
                        IdentityVerification.VerificationStatus.VERIFIED
                )
                .isPresent();
    }

    // 私有輔助方法
    private void validateDocument(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("文件不能為空");
        }

        // 檢查文件大小（例如：最大5MB）
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new IllegalArgumentException("文件大小不能超過5MB");
        }

        // 檢查文件類型
        String contentType = file.getContentType();
        if (contentType == null ||
                !(contentType.equals("image/jpeg") ||
                        contentType.equals("image/png") ||
                        contentType.equals("application/pdf"))) {
            throw new IllegalArgumentException("不支援的文件類型");
        }
    }
}