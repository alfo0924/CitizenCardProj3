package org.example._citizncardproj3.model.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WalletResponse {

    private Long walletId;
    private Long memberId;
    private String memberName;
    private String memberEmail;
    private Double balance;
    private WalletStatus status;
    private WalletType walletType;
    private List<TransactionRecord> recentTransactions;
    private WalletStatistics statistics;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastTransactionTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    private String message;

    // 錢包狀態枚舉
    public enum WalletStatus {
        ACTIVE("正常"),
        FROZEN("凍結"),
        SUSPENDED("停用"),
        CLOSED("已關閉");

        private final String description;

        WalletStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    // 錢包類型枚舉
    public enum WalletType {
        GENERAL("一般錢包"),
        POINTS("點數錢包"),
        STUDENT("學生錢包"),
        SENIOR("敬老錢包");

        private final String description;

        WalletType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    // 交易記錄內部類
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TransactionRecord {
        private Long transactionId;
        private String transactionNumber;
        private TransactionType type;
        private Double amount;
        private String description;
        private TransactionStatus status;

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime transactionTime;
    }

    // 交易類型枚舉
    public enum TransactionType {
        TOP_UP("儲值"),
        PAYMENT("支付"),
        REFUND("退款"),
        TRANSFER("轉帳"),
        POINTS_EARNED("獲得點數"),
        POINTS_USED("使用點數");

        private final String description;

        TransactionType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    // 交易狀態枚舉
    public enum TransactionStatus {
        PENDING("處理中"),
        COMPLETED("完成"),
        FAILED("失敗"),
        CANCELLED("已取消");

        private final String description;

        TransactionStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    // 錢包統計內部類
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WalletStatistics {
        private Double totalTopUp;
        private Double totalSpent;
        private Integer totalTransactions;
        private Integer pointsBalance;
        private Integer pointsEarned;
        private Integer pointsUsed;
    }

    // 建構子 - 用於錯誤響應
    public WalletResponse(String message) {
        this.message = message;
    }

    // 建構子 - 複製現有響應並添加消息
    public WalletResponse(WalletResponse response, String message) {
        if (response != null) {
            this.walletId = response.getWalletId();
            this.memberId = response.getMemberId();
            this.memberName = response.getMemberName();
            this.memberEmail = response.getMemberEmail();
            this.balance = response.getBalance();
            this.status = response.getStatus();
            this.walletType = response.getWalletType();
            this.recentTransactions = response.getRecentTransactions();
            this.statistics = response.getStatistics();
            this.lastTransactionTime = response.getLastTransactionTime();
            this.createdAt = response.getCreatedAt();
        }
        this.message = message;
    }

    // 輔助方法 - 檢查錢包是否可用
    public boolean isUsable() {
        return status == WalletStatus.ACTIVE;
    }

    // 輔助方法 - 檢查餘額是否足夠
    public boolean hasEnoughBalance(Double amount) {
        if (amount == null || balance == null) {
            return false;
        }
        return balance >= amount;
    }

    // 輔助方法 - 獲取錢包狀態描述
    public String getStatusDescription() {
        return status != null ? status.getDescription() : "未知狀態";
    }

    // 輔助方法 - 獲取錢包類型描述
    public String getWalletTypeDescription() {
        return walletType != null ? walletType.getDescription() : "未知類型";
    }

    // 輔助方法 - 計算可用點數
    public Integer getAvailablePoints() {
        if (statistics == null) {
            return 0;
        }
        return statistics.getPointsBalance();
    }

    // 用於日誌記錄的方法
    public String toLogString() {
        return String.format("Wallet{id=%d, member='%s', balance=%.2f, status=%s}",
                walletId, memberEmail, balance, status);
    }
}