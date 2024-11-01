package org.example._citizncardproj3.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "identity_verifications")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IdentityVerification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long verificationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VerificationType verificationType;

    @Column(nullable = false)
    private String documentNumber;

    @Column(length = 500)
    private String documentUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VerificationStatus status;

    @Column
    private String verificationCode;

    @Column
    private LocalDateTime verificationTime;

    @Column
    private String verifiedBy;

    @Column
    private String rejectionReason;

    @Column(length = 500)
    private String remarks;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Column(nullable = false)
    private Boolean isDeleted;

    // 驗證類型枚舉
    public enum VerificationType {
        ID_CARD("身分證"),
        STUDENT_CARD("學生證"),
        SENIOR_CARD("敬老卡"),
        DISABILITY_CARD("身心障礙證明"),
        PASSPORT("護照");

        private final String description;

        VerificationType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    // 驗證狀態枚舉
    public enum VerificationStatus {
        PENDING("待驗證"),
        IN_PROGRESS("驗證中"),
        VERIFIED("已驗證"),
        REJECTED("已拒絕"),
        EXPIRED("已過期");

        private final String description;

        VerificationStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    // 初始化方法
    @PrePersist
    public void prePersist() {
        if (this.isDeleted == null) {
            this.isDeleted = false;
        }
        if (this.status == null) {
            this.status = VerificationStatus.PENDING;
        }
        if (this.verificationCode == null) {
            this.verificationCode = generateVerificationCode();
        }
    }

    // 生成驗證碼
    private String generateVerificationCode() {
        return "VER" + System.currentTimeMillis();
    }

    // 業務方法

    // 開始驗證
    public void startVerification() {
        if (this.status != VerificationStatus.PENDING) {
            throw new IllegalStateException("只有待驗證狀態可以開始驗證");
        }
        this.status = VerificationStatus.IN_PROGRESS;
    }

    // 驗證通過
    public void approve(String verifiedBy) {
        if (this.status != VerificationStatus.IN_PROGRESS) {
            throw new IllegalStateException("只有驗證中的狀態可以通過驗證");
        }
        this.status = VerificationStatus.VERIFIED;
        this.verificationTime = LocalDateTime.now();
        this.verifiedBy = verifiedBy;
    }

    // 驗證拒絕
    public void reject(String reason) {
        if (this.status != VerificationStatus.IN_PROGRESS) {
            throw new IllegalStateException("只有驗證中的狀態可以拒絕驗證");
        }
        this.status = VerificationStatus.REJECTED;
        this.rejectionReason = reason;
    }

    // 設置過期
    public void setExpired() {
        if (this.status == VerificationStatus.VERIFIED) {
            this.status = VerificationStatus.EXPIRED;
        }
    }

    // 更新文件
    public void updateDocument(String documentNumber, String documentUrl) {
        if (this.status != VerificationStatus.PENDING &&
                this.status != VerificationStatus.REJECTED) {
            throw new IllegalStateException("只有待驗證或已拒絕狀態可以更新文件");
        }
        this.documentNumber = documentNumber;
        this.documentUrl = documentUrl;
        this.status = VerificationStatus.PENDING;
    }

    // 檢查是否需要重新驗證
    public boolean needsReVerification() {
        if (this.status != VerificationStatus.VERIFIED) {
            return false;
        }
        // 假設驗證有效期為一年
        return this.verificationTime.plusYears(1).isBefore(LocalDateTime.now());
    }



    // 用於日誌記錄的方法
    public String toLogString() {
        return String.format("IdentityVerification{id=%d, type=%s, status=%s, document='%s'}",
                verificationId, verificationType, status, documentNumber);
    }

    // 新增欄位
    @Column(name = "valid_until")
    private LocalDateTime validUntil;

    @Column(name = "verification_comment")
    private String verificationComment;

    @Column(name = "attempt_count")
    private Integer attemptCount = 0;

    @Column(name = "last_attempt_time")
    private LocalDateTime lastAttemptTime;

// 新增驗證方法
    /**
     * 驗證身份
     */
    public void verify(String verifier, boolean approved, String comment) {
        if (this.status != VerificationStatus.PENDING &&
                this.status != VerificationStatus.IN_PROGRESS) {
            throw new IllegalStateException("當前狀態無法進行驗證");
        }

        this.verifiedBy = verifier;
        this.verificationTime = LocalDateTime.now();
        this.verificationComment = comment;

        if (approved) {
            this.status = VerificationStatus.VERIFIED;
            this.validUntil = LocalDateTime.now().plusYears(1); // 設定有效期為一年
        } else {
            this.status = VerificationStatus.REJECTED;
            this.rejectionReason = comment;
            this.validUntil = null;
        }
    }

    /**
     * 記錄驗證嘗試
     */
    public void recordAttempt() {
        this.attemptCount++;
        this.lastAttemptTime = LocalDateTime.now();

        // 如果嘗試次數超過3次，自動拒絕
        if (this.attemptCount >= 3) {
            this.status = VerificationStatus.REJECTED;
            this.rejectionReason = "驗證嘗試次數過多";
        }
    }

    /**
     * 檢查是否可以重新嘗試
     */
    public boolean canRetry() {
        return this.status == VerificationStatus.REJECTED &&
                this.attemptCount < 3 &&
                (this.lastAttemptTime == null ||
                        this.lastAttemptTime.plusHours(24).isBefore(LocalDateTime.now()));
    }

    /**
     * 重置驗證狀態
     */
    public void reset() {
        if (this.status != VerificationStatus.REJECTED) {
            throw new IllegalStateException("只有被拒絕的驗證可以重置");
        }
        this.status = VerificationStatus.PENDING;
        this.verifiedBy = null;
        this.verificationTime = null;
        this.verificationComment = null;
        this.rejectionReason = null;
        this.attemptCount = 0;
        this.lastAttemptTime = null;
    }

    // 修改isValid方法
//    @Override
    public boolean isValid() {
        return this.status == VerificationStatus.VERIFIED &&
                !this.isDeleted &&
                this.validUntil != null &&
                this.validUntil.isAfter(LocalDateTime.now());
    }

}