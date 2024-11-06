package org.example._citizncardproj3.model.entity;

import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "CitizenCards")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CitizenCard {

    @Id
    @Column(name = "CardNumber", length = 20)
    private String cardNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MemberID", nullable = false)
    private Member member;

    @Column(name = "HolderName", nullable = false, length = 100)
    private String holderName;

    @Enumerated(EnumType.STRING)
    @Column(name = "CardType", nullable = false)
    private CardType cardType;

    @Column(name = "IssueDate", nullable = false)
    private LocalDate issueDate;

    @Column(name = "ExpiryDate", nullable = false)
    private LocalDate expiryDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "CardStatus", nullable = false)
    private CardStatus cardStatus;

    @CreationTimestamp
    @Column(name = "CreatedAt", nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "UpdatedAt", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "IsDeleted", nullable = false)
    private Boolean isDeleted;

    @Column(name = "DeletedAt")
    private LocalDateTime deletedAt;

    // 卡片類型枚舉
    @Getter
    public enum CardType {
        GENERAL("一般卡"),
        SENIOR("敬老卡"),
        CHARITY("愛心卡"),
        STUDENT("學生卡");

        private final String description;

        CardType(String description) {
            this.description = description;
        }

    }

    // 卡片狀態枚舉
    @Getter
    public enum CardStatus {
        ACTIVE("有效"),
        SUSPENDED("停用"),
        EXPIRED("過期");

        private final String description;

        CardStatus(String description) {
            this.description = description;
        }

    }

    // 初始化方法
    @PrePersist
    public void prePersist() {
        if (this.isDeleted == null) {
            this.isDeleted = false;
        }
        if (this.cardStatus == null) {
            this.cardStatus = CardStatus.ACTIVE;
        }
        if (this.cardNumber == null) {
            this.cardNumber = generateCardNumber();
        }
        if (this.issueDate == null) {
            this.issueDate = LocalDate.now();
        }
    }

    // 生成卡號
    private String generateCardNumber() {
        String prefix = switch (this.cardType) {
            case SENIOR -> "SR";
            case CHARITY -> "CH";
            case STUDENT -> "ST";
            default -> "GN";
        };
        return prefix + System.currentTimeMillis();
    }

    // 業務方法
    // 啟用卡片
    public void activate() {
        if (this.cardStatus == CardStatus.SUSPENDED) {
            this.cardStatus = CardStatus.ACTIVE;
        } else {
            throw new IllegalStateException("只有停用的卡片可以啟用");
        }
    }
    // 停用卡片
    public void suspend(String reason) {
        if (this.cardStatus == CardStatus.ACTIVE) {
            this.cardStatus = CardStatus.SUSPENDED;
        } else {
            throw new IllegalStateException("只有有效的卡片可以停用");
        }
    }

    // 更新有效期
    public void updateExpiryDate(LocalDate newExpiryDate) {
        if (newExpiryDate.isAfter(LocalDate.now())) {
            this.expiryDate = newExpiryDate;
            if (this.cardStatus == CardStatus.EXPIRED) {
                this.cardStatus = CardStatus.ACTIVE;
            }
        } else {
            throw new IllegalArgumentException("新的有效期必須在當前日期之後");
        }
    }

    // 檢查卡片是否有效
    public boolean isValid() {
        return this.cardStatus == CardStatus.ACTIVE &&
                !this.isDeleted &&
                LocalDate.now().isBefore(this.expiryDate);
    }

    // 檢查是否需要續期
    public boolean needsRenewal() {
        return this.cardStatus == CardStatus.ACTIVE &&
                LocalDate.now().plusMonths(1).isAfter(this.expiryDate);
    }

    // 驗證持卡人年齡是否符合卡片類型
    public boolean validateAgeRequirement() {
        if (this.member == null || this.member.getBirthday() == null) {
            return false;
        }

        int age = member.getAge();
        return switch (this.cardType) {
            case SENIOR -> age >= 65;
            case STUDENT -> age >= 6 && age <= 25;
            case GENERAL -> age >= 18;
            default -> true;
        };
    }

    // 軟刪除
    public void softDelete() {
        this.isDeleted = true;
        this.deletedAt = LocalDateTime.now();
    }

    // 用於日誌記錄的方法
    @Override
    public String toString() {
        return String.format("CitizenCard{number='%s', holder='%s', type=%s, status=%s}",
                cardNumber, holderName, cardType, cardStatus);
    }
}