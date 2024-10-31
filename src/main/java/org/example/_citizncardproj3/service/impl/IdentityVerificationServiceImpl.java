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

    @Override
    @Transactional
    public IdentityVerification submitVerification(
            String userEmail,
            IdentityVerification.VerificationType type,
            String documentNumber,
            MultipartFile documentFile) {

        Member member = memberRepository.findByEmail(userEmail)
                .orElseThrow(() -> new CustomException.MemberNotFoundException(userEmail));

        // 檢查是否有進行中的驗證
        Optional<IdentityVerification> existingVerification = verificationRepository
                .findValidVerification(member, type);

        if (existingVerification.isPresent()) {
            throw new IllegalStateException("已有進行中的身份驗證");
        }

        // 驗證文件
        validateDocument(documentFile);

        // 上傳文件並獲取URL
        String documentUrl = uploadDocument(documentFile);

        // 創建驗證記錄
        IdentityVerification verification = IdentityVerification.builder()
                .member(member)
                .verificationType(type)
                .documentNumber(documentNumber)
                .documentUrl(documentUrl)
                .status(IdentityVerification.VerificationStatus.PENDING)
                .build();

        return verificationRepository.save(verification);
    }

    @Override
    @Transactional
    public IdentityVerification verifyIdentity(
            Long verificationId,
            String verifier,
            boolean approved,
            String comment) {

        IdentityVerification verification = verificationRepository.findById(verificationId)
                .orElseThrow(() -> new IllegalArgumentException("驗證記錄不存在"));

        if (verification.getStatus() != IdentityVerification.VerificationStatus.PENDING) {
            throw new IllegalStateException("只有待驗證的記錄可以進行驗證");
        }

        verification.verify(verifier, approved, comment);
        return verificationRepository.save(verification);
    }

    @Override
    @Transactional
    public IdentityVerification updateDocument(
            Long verificationId,
            String documentNumber,
            MultipartFile documentFile) {

        IdentityVerification verification = verificationRepository.findById(verificationId)
                .orElseThrow(() -> new IllegalArgumentException("驗證記錄不存在"));

        if (verification.getStatus() != IdentityVerification.VerificationStatus.REJECTED) {
            throw new IllegalStateException("只有被拒絕的驗證可以更新文件");
        }

        // 驗證文件
        validateDocument(documentFile);

        // 上傳新文件
        String documentUrl = uploadDocument(documentFile);

        // 更新文件信息
        verification.updateDocument(documentNumber, documentUrl);
        return verificationRepository.save(verification);
    }

    @Override
    public List<IdentityVerification> getMemberVerifications(String userEmail) {
        Member member = memberRepository.findByEmail(userEmail)
                .orElseThrow(() -> new CustomException.MemberNotFoundException(userEmail));

        return verificationRepository.findByMember(member);
    }

    @Override
    public List<IdentityVerification> getPendingVerifications() {
        return verificationRepository.findPendingVerifications();
    }

    @Override
    @Transactional
    public void checkAndUpdateExpiredVerifications() {
        // 設定過期時間（例如：一年）
        LocalDateTime expiryTime = LocalDateTime.now().minusYears(1);

        List<IdentityVerification> expiredVerifications =
                verificationRepository.findExpiredVerifications(expiryTime);

        for (IdentityVerification verification : expiredVerifications) {
            verification.setExpired();
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
        return false;
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

    private String uploadDocument(MultipartFile file) {
        // TODO: 實作文件上傳邏輯
        // 這裡應該實現實際的文件上傳功能，例如上傳到雲存儲
        return "http://example.com/documents/" + System.currentTimeMillis();
    }

    private boolean isDocumentValid(String documentNumber, IdentityVerification.VerificationType type) {
        // TODO: 實作文件驗證邏輯
        // 這裡應該實現實際的文件驗證邏輯，例如檢查證件號碼格式
        switch (type) {
            case ID_CARD:
                return documentNumber.matches("[A-Z][1-2]\\d{8}");
            case STUDENT_CARD:
                return documentNumber.length() == 10;
            case DISABILITY_CARD:
                return documentNumber.matches("\\d{8}");
            default:
                return true;
        }
    }
}