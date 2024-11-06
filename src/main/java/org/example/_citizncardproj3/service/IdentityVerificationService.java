package org.example._citizncardproj3.service;

import org.example._citizncardproj3.model.entity.IdentityVerification;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface IdentityVerificationService {

    @Transactional
    IdentityVerification submitVerification(
            String userEmail,
            IdentityVerification.VerificationType type,
            MultipartFile documentFile);

    @Transactional
    IdentityVerification processVerification(Long verificationId, boolean approved);

    @Transactional
    IdentityVerification resubmitVerification(Long verificationId);

    /**
     * 提交身份驗證申請
     * @param userEmail 用戶郵箱
     * @param type 驗證類型
     * @param documentNumber 證件號碼
     * @param documentFile 證件文件
     * @return 驗證記錄
     */
    IdentityVerification submitVerification(
            String userEmail,
            IdentityVerification.VerificationType type,
            String documentNumber,
            MultipartFile documentFile
    );

    /**
     * 審核身份驗證
     * @param verificationId 驗證ID
     * @param verifier 審核人
     * @param approved 是否通過
     * @param comment 審核意見
     * @return 更新後的驗證記錄
     */
    IdentityVerification verifyIdentity(
            Long verificationId,
            String verifier,
            boolean approved,
            String comment
    );

    /**
     * 更新驗證文件
     * @param verificationId 驗證ID
     * @param documentNumber 新的證件號碼
     * @param documentFile 新的證件文件
     * @return 更新後的驗證記錄
     */
    IdentityVerification updateDocument(
            Long verificationId,
            String documentNumber,
            MultipartFile documentFile
    );

    /**
     * 獲取會員的驗證記錄
     * @param userEmail 用戶郵箱
     * @return 驗證記錄列表
     */
    List<IdentityVerification> getMemberVerifications(String userEmail);

    /**
     * 獲取待處理的驗證申請
     * @return 待處理的驗證記錄列表
     */
    List<IdentityVerification> getPendingVerifications();

    /**
     * 檢查並更新過期的驗證記錄
     */
    void checkAndUpdateExpiredVerifications();

    /**
     * 驗證證件號碼格式
     * @param documentNumber 證件號碼
     * @param type 驗證類型
     * @return 是否有效
     */
    boolean validateDocumentNumber(String documentNumber, IdentityVerification.VerificationType type);

    /**
     * 檢查驗證記錄是否需要重新驗證
     * @param verificationId 驗證ID
     * @return 是否需要重新驗證
     */
    boolean needsReVerification(Long verificationId);

    /**
     * 獲取特定類型的有效驗證記錄
     * @param userEmail 用戶郵箱
     * @param type 驗證類型
     * @return 驗證記錄
     */
    IdentityVerification getValidVerification(String userEmail, IdentityVerification.VerificationType type);

    /**
     * 取消驗證申請
     * @param verificationId 驗證ID
     * @param reason 取消原因
     */
    void cancelVerification(Long verificationId, String reason);

    /**
     * 獲取驗證統計資訊
     * @param startTime 開始時間
     * @param endTime 結束時間
     * @return 統計資訊
     */
    Map<String, Object> getVerificationStatistics(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 檢查用戶是否完成特定類型的身份驗證
     * @param userEmail 用戶郵箱
     * @param type 驗證類型
     * @return 是否完成驗證
     */
    boolean hasValidVerification(String userEmail, IdentityVerification.VerificationType type);
}