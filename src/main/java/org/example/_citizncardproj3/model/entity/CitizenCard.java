package org.example._citizncardproj3.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "citizen_cards")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CitizenCard {

    @Id
    @Column(length = 20)
    private String cardNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(nullable = false, length = 100)
    private String holderName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CardType cardType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CardStatus status;

    @Column(nullable = false)
    private LocalDate issueDate;

    @Column(nullable = false)
    private LocalDate expiryDate;

    @Column(length = 200)
    private String remarks;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Column(nullable = false)
    private Boolean isDeleted;

    // 卡片類型枚舉
    public enum CardType {
        GENERAL("一般卡"),
        SENIOR("敬老卡"),
        CHARITY("愛心卡"),
        STUDENT("學生卡");

        private final String description;

        CardType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    // 卡片狀態枚舉
    public enum CardStatus {
        ACTIVE("有效"),
        INACTIVE("未啟用"),
        SUSPENDED("已停用"),
        EXPIRED("已過期"),
        LOST("已遺失");

        private final String description;

        CardStatus(String description) {
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
            this.status = CardStatus.INACTIVE;
        }
        if (this.cardNumber == null) {
            this.cardNumber = generateCardNumber();
        }
    }

    // 生成卡號
    private String generateCardNumber() {
        String prefix;
        switch (this.cardType) {
            case SENIOR:
                prefix = "SR";
                break;
            case CHARITY:
                prefix = "CH";
                break;
            case STUDENT:
                prefix = "ST";
                break;
            default:
                prefix = "GN";
        }
        return prefix + System.currentTimeMillis();
    }

    // 業務方法

    // 啟用卡片
    public void activate() {
        if (this.status == CardStatus.INACTIVE) {
            this.status = CardStatus.ACTIVE;
        } else {
            throw new IllegalStateException("只有未啟用的卡片可以啟用");
        }
    }

    // 停用卡片
    public void suspend(String reason) {
        if (this.status == CardStatus.ACTIVE) {
            this.status = CardStatus.SUSPENDED;
            this.remarks = reason;
        } else {
            throw new IllegalStateException("只有有效的卡片可以停用");
        }
    }

    // 報失卡片
    public void reportLost() {
        if (this.status == CardStatus.ACTIVE) {
            this.status = CardStatus.LOST;
        } else {
            throw new IllegalStateException("只有有效的卡片可以報失");
        }
    }

    // 更新有效期
    public void updateExpiryDate(LocalDate newExpiryDate) {
        if (newExpiryDate.isAfter(LocalDate.now())) {
            this.expiryDate = newExpiryDate;
        } else {
            throw new IllegalArgumentException("新的有效期必須在當前日期之後");
        }
    }

    // 檢查卡片是否有效
    public boolean isValid() {
        return this.status == CardStatus.ACTIVE &&
                !this.isDeleted &&
                LocalDate.now().isBefore(this.expiryDate);
    }

    // 檢查是否需要續期
    public boolean needsRenewal() {
        return this.status == CardStatus.ACTIVE &&
                LocalDate.now().plusMonths(1).isAfter(this.expiryDate);
    }

    // 驗證持卡人年齡是否符合卡片類型
    public boolean validateAgeRequirement() {
        if (this.member == null || this.member.getBirthday() == null) {
            return false;
        }

        int age = member.getAge();
        switch (this.cardType) {
            case SENIOR:
                return age >= 65;
            case STUDENT:
                return age >= 6 && age <= 25;
            case GENERAL:
                return age >= 18;
            default:
                return true;
        }
    }

    // 用於日誌記錄的方法
    public String toLogString() {
        return String.format("CitizenCard{number='%s', holder='%s', type=%s, status=%s}",
                cardNumber, holderName, cardType, status);
    }
}