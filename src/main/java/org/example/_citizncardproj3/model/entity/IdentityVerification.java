package org.example._citizncardproj3.model.entity;

import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "IdentityVerifications")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IdentityVerification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "VerificationID")
    private Long verificationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MemberID", nullable = false)
    private Member member;

    @Enumerated(EnumType.STRING)
    @Column(name = "VerificationType", nullable = false)
    private VerificationType verificationType;

    @Enumerated(EnumType.STRING)
    @Column(name = "VerificationStatus", nullable = false)
    private VerificationStatus status;

    @Column(name = "VerificationTime")
    private LocalDateTime verificationTime;

    @CreationTimestamp
    @Column(name = "CreatedAt", nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "UpdatedAt", nullable = false)
    private LocalDateTime updatedAt;

    // 驗證類型枚舉
    @Getter
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

    }

    // 驗證狀態枚舉
    @Getter
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

    }

    // 初始化方法
    @PrePersist
    public void prePersist() {
        if (this.status == null) {
            this.status = VerificationStatus.PENDING;
        }
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
    public void approve() {
        if (this.status != VerificationStatus.IN_PROGRESS) {
            throw new IllegalStateException("只有驗證中的狀態可以通過驗證");
        }
        this.status = VerificationStatus.VERIFIED;
        this.verificationTime = LocalDateTime.now();
    }

    // 驗證拒絕
    public void reject() {
        if (this.status != VerificationStatus.IN_PROGRESS) {
            throw new IllegalStateException("只有驗證中的狀態可以拒絕驗證");
        }
        this.status = VerificationStatus.REJECTED;
    }

    // 設置過期
    public void setExpired() {
        if (this.status == VerificationStatus.VERIFIED) {
            this.status = VerificationStatus.EXPIRED;
        }
    }

    // 檢查是否需要重新驗證
    public boolean needsReVerification() {
        if (this.status != VerificationStatus.VERIFIED) {
            return false;
        }
        // 假設驗證有效期為一年
        return this.verificationTime.plusYears(1).isBefore(LocalDateTime.now());
    }

    // 檢查是否有效
    public boolean isValid() {
        return this.status == VerificationStatus.VERIFIED &&
                this.verificationTime != null &&
                this.verificationTime.plusYears(1).isAfter(LocalDateTime.now());
    }

    // 用於日誌記錄的方法
    @Override
    public String toString() {
        return String.format("IdentityVerification{id=%d, type=%s, status=%s}",
                verificationId, verificationType, status);
    }
}