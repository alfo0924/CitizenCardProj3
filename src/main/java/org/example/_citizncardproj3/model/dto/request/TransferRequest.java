package org.example._citizncardproj3.model.dto.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TransferRequest {

    @NotBlank(message = "收款人Email不能為空")
    @Email(message = "收款人Email格式不正確")
    private String recipientEmail;

    @NotNull(message = "轉帳金額不能為空")
    @DecimalMin(value = "1.0", message = "轉帳金額必須大於或等於1")
    @DecimalMax(value = "1000000.0", message = "轉帳金額不能超過100萬")
    @Digits(integer = 7, fraction = 2, message = "金額格式不正確")
    private Double amount;

    @Size(max = 200, message = "備註不能超過200字")
    private String note;

    private String transferCode;  // 轉帳驗證碼（如果需要）

    @Builder.Default
    private LocalDateTime requestTime = LocalDateTime.now();

    // 驗證方法
    public void validate() {
        if (recipientEmail == null || recipientEmail.trim().isEmpty()) {
            throw new IllegalArgumentException("收款人Email不能為空");
        }

        if (amount == null || amount <= 0) {
            throw new IllegalArgumentException("轉帳金額必須大於0");
        }

        if (amount > 1000000) {
            throw new IllegalArgumentException("轉帳金額不能超過100萬");
        }

        if (note != null && note.length() > 200) {
            throw new IllegalArgumentException("備註不能超過200字");
        }
    }

    // 檢查是否需要驗證碼
    public boolean needsVerification() {
        return amount >= 50000;  // 金額大於5萬需要驗證碼
    }

    // 驗證轉帳碼
    public boolean verifyTransferCode(String expectedCode) {
        if (!needsVerification()) {
            return true;
        }
        return transferCode != null && transferCode.equals(expectedCode);
    }

    // 建立交易描述
    public String createTransactionDescription() {
        StringBuilder description = new StringBuilder();
        description.append(String.format("轉帳 %.2f 元給 %s", amount, recipientEmail));
        if (note != null && !note.trim().isEmpty()) {
            description.append(" (").append(note).append(")");
        }
        return description.toString();
    }

    // 用於日誌記錄的方法
    public String toLogString() {
        return String.format("TransferRequest{to=%s, amount=%.2f, time=%s}",
                recipientEmail, amount, requestTime);
    }

    // 遮蔽敏感資訊的方法
    public String getMaskedRecipientEmail() {
        if (recipientEmail == null || recipientEmail.length() < 8) {
            return null;
        }
        int atIndex = recipientEmail.indexOf('@');
        if (atIndex <= 3) {
            return recipientEmail;
        }
        return recipientEmail.substring(0, 3) + "***" +
                recipientEmail.substring(atIndex);
    }

    // 檢查是否為大額轉帳
    public boolean isLargeTransfer() {
        return amount >= 100000;  // 10萬以上視為大額轉帳
    }

    // 計算手續費（如果需要）
    public double calculateFee() {
        if (amount <= 10000) {
            return 0.0;  // 1萬以下免手續費
        }
        return Math.min(amount * 0.001, 500);  // 0.1%手續費，最高500元
    }

    // 獲取總金額（含手續費）
    public double getTotalAmount() {
        return amount + calculateFee();
    }

    // 檢查是否在允許的轉帳時間內
    public boolean isWithinTransferHours() {
        int hour = requestTime.getHour();
        return hour >= 9 && hour < 22;  // 早上9點到晚上10點
    }
}