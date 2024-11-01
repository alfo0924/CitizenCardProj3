package org.example._citizncardproj3.model.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example._citizncardproj3.model.entity.Transaction;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TransactionResponse {
    private Long transactionId;
    private String transactionNumber;
    private String memberName;
    private String memberEmail;
    private Transaction.TransactionType type;
    private Double amount;
    private Double balanceAfter;
    private Transaction.TransactionStatus status;
    private String description;
    private String paymentMethod;
    private String referenceNumber;
    private String recipientName;
    private String recipientEmail;
    private TransactionDetails details;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime transactionTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime completionTime;

    private String message;  // 用於錯誤訊息

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TransactionDetails {
        private String sourceType;
        private String sourceId;
        private String sourceDescription;
        private Double originalAmount;
        private Double feeAmount;
        private String failureReason;
        private String additionalInfo;
    }

    // 建構子 - 用於錯誤響應
    public TransactionResponse(Transaction transaction, String message) {
        if (transaction != null) {
            this.transactionId = transaction.getTransactionId();
            this.transactionNumber = transaction.getTransactionNumber();
            this.memberName = transaction.getMember().getName();
            this.memberEmail = transaction.getMember().getEmail();
            this.type = transaction.getType();
            this.amount = transaction.getAmount();
            this.balanceAfter = transaction.getBalanceAfter();
            this.status = transaction.getStatus();
            this.description = transaction.getDescription();
            this.paymentMethod = String.valueOf(transaction.getPaymentMethod());
            this.referenceNumber = transaction.getReferenceNumber();
            this.transactionTime = transaction.getTransactionTime();
            this.completionTime = transaction.getCompletionTime();

            // 如果是轉帳交易，設置收款人資訊
            if (transaction.getRecipient() != null) {
                this.recipientName = transaction.getRecipient().getName();
                this.recipientEmail = transaction.getRecipient().getEmail();
            }

            // 設置交易詳情
            if (transaction.getSourceType() != null) {
                this.details = TransactionDetails.builder()
                        .sourceType(transaction.getSourceType())
                        .sourceId(transaction.getSourceId())
                        .sourceDescription(transaction.getSourceDescription())
                        .originalAmount(transaction.getOriginalAmount())
                        .feeAmount(transaction.getFeeAmount())
                        .failureReason(transaction.getFailureReason())
                        .additionalInfo(transaction.getAdditionalInfo())
                        .build();
            }
        }
        this.message = message;
    }

    // 從Transaction實體轉換為DTO
    public static TransactionResponse fromEntity(Transaction transaction) {
        return TransactionResponse.builder()
                .transactionId(transaction.getTransactionId())
                .transactionNumber(transaction.getTransactionNumber())
                .memberName(transaction.getMember().getName())
                .memberEmail(transaction.getMember().getEmail())
                .type(transaction.getType())
                .amount(transaction.getAmount())
                .balanceAfter(transaction.getBalanceAfter())
                .status(transaction.getStatus())
                .description(transaction.getDescription())
                .paymentMethod(String.valueOf(transaction.getPaymentMethod()))
                .referenceNumber(transaction.getReferenceNumber())
                .transactionTime(transaction.getTransactionTime())
                .completionTime(transaction.getCompletionTime())
                .recipientName(transaction.getRecipient() != null ?
                        transaction.getRecipient().getName() : null)
                .recipientEmail(transaction.getRecipient() != null ?
                        transaction.getRecipient().getEmail() : null)
                .details(transaction.getSourceType() != null ?
                        TransactionDetails.builder()
                                .sourceType(transaction.getSourceType())
                                .sourceId(transaction.getSourceId())
                                .sourceDescription(transaction.getSourceDescription())
                                .originalAmount(transaction.getOriginalAmount())
                                .feeAmount(transaction.getFeeAmount())
                                .failureReason(transaction.getFailureReason())
                                .additionalInfo(transaction.getAdditionalInfo())
                                .build() : null)
                .build();
    }

    // 檢查交易是否成功
    public boolean isSuccessful() {
        return status == Transaction.TransactionStatus.COMPLETED;
    }

    // 檢查交易是否可以取消
    public boolean isCancellable() {
        return status == Transaction.TransactionStatus.PENDING;
    }

    // 獲取交易狀態描述
    public String getStatusDescription() {
        return status != null ? status.getDescription() : "未知狀態";
    }

    // 獲取交易類型描述
    public String getTypeDescription() {
        return type != null ? type.getDescription() : "未知類型";
    }

    // 用於日誌記錄的方法
    public String toLogString() {
        return String.format("Transaction{id=%d, number='%s', type=%s, amount=%.2f, status=%s}",
                transactionId, transactionNumber, type, amount, status);
    }
}