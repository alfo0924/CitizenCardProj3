package org.example._citizncardproj3.model.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MemberResponse {

    private Long memberId;
    private String email;
    private String name;
    private String phone;
    private String address;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthday;

    private String gender;
    private String avatarUrl;
    private MemberStatus status;
    private List<CitizenCard> citizenCards;
    private WalletInfo walletInfo;
    private MemberStatistics statistics;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastLoginTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime registrationTime;

    private String message;

    // 會員狀態枚舉
    public enum MemberStatus {
        ACTIVE("正常"),
        INACTIVE("未啟用"),
        SUSPENDED("已停權"),
        LOCKED("已鎖定");

        private final String description;

        MemberStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    // 市民卡資訊內部類
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CitizenCard {
        private String cardNumber;
        private String cardType;
        private String cardStatus;

        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate issueDate;

        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate expiryDate;
    }

    // 錢包資訊內部類
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WalletInfo {
        private Long walletId;
        private Double balance;
        private String walletStatus;
        private List<TransactionSummary> recentTransactions;
    }

    // 交易摘要內部類
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TransactionSummary {
        private String transactionType;
        private Double amount;

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime transactionTime;
    }

    // 會員統計資訊內部類
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MemberStatistics {
        private Integer totalBookings;
        private Integer completedBookings;
        private Integer cancelledBookings;
        private Double totalSpent;
        private Integer totalDiscountsUsed;
        private Integer loyaltyPoints;
    }

    // 建構子 - 用於錯誤響應
    public MemberResponse(String message) {
        this.message = message;
    }

    // 建構子 - 複製現有響應並添加消息
    public MemberResponse(MemberResponse response, String message) {
        if (response != null) {
            this.memberId = response.getMemberId();
            this.email = response.getEmail();
            this.name = response.getName();
            this.phone = response.getPhone();
            this.address = response.getAddress();
            this.birthday = response.getBirthday();
            this.gender = response.getGender();
            this.avatarUrl = response.getAvatarUrl();
            this.status = response.getStatus();
            this.citizenCards = response.getCitizenCards();
            this.walletInfo = response.getWalletInfo();
            this.statistics = response.getStatistics();
            this.lastLoginTime = response.getLastLoginTime();
            this.registrationTime = response.getRegistrationTime();
        }
        this.message = message;
    }

    // 輔助方法 - 檢查會員是否可用
    public boolean isActive() {
        return status == MemberStatus.ACTIVE;
    }

    // 輔助方法 - 檢查會員是否有效卡片
    public boolean hasValidCard() {
        if (citizenCards == null || citizenCards.isEmpty()) {
            return false;
        }
        LocalDate now = LocalDate.now();
        return citizenCards.stream()
                .anyMatch(card -> card.getExpiryDate() != null &&
                        card.getExpiryDate().isAfter(now));
    }

    // 輔助方法 - 獲取會員等級
    public String getMembershipLevel() {
        if (statistics == null || statistics.getTotalSpent() == null) {
            return "一般會員";
        }

        Double totalSpent = statistics.getTotalSpent();
        if (totalSpent >= 50000) {
            return "鑽石會員";
        } else if (totalSpent >= 20000) {
            return "金卡會員";
        } else if (totalSpent >= 5000) {
            return "銀卡會員";
        } else {
            return "一般會員";
        }
    }

    // 輔助方法 - 獲取會員狀態描述
    public String getStatusDescription() {
        return status != null ? status.getDescription() : "未知狀態";
    }

    // 用於日誌記錄的方法
    public String toLogString() {
        return String.format("Member{id=%d, email='%s', name='%s', status=%s}",
                memberId, email, name, status);
    }
}