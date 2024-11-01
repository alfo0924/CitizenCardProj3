package org.example._citizncardproj3.model.dto.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example._citizncardproj3.model.entity.Transaction;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Digits;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TopUpRequest {

    @NotNull(message = "儲值金額不能為空")
    @DecimalMin(value = "1.0", message = "儲值金額必須大於或等於1")
    @Digits(integer = 10, fraction = 2, message = "金額格式不正確")
    private Double amount;

    @NotNull(message = "支付方式不能為空")
    private Transaction.PaymentMethod paymentMethod;

    private String bankCode;  // 銀行代碼（如果使用銀行轉帳）

    private String cardNumber;  // 信用卡號（如果使用信用卡）

    private String cardHolderName;  // 持卡人姓名

    private String cardExpiryDate;  // 信用卡到期日

    private String cvv;  // 信用卡安全碼

    private String promoCode;  // 促銷代碼（可選）

    @Builder.Default
    private LocalDateTime requestTime = LocalDateTime.now();

    // 驗證方法
    public void validate() {
        if (amount == null || amount <= 0) {
            throw new IllegalArgumentException("儲值金額必須大於0");
        }

        if (paymentMethod == null) {
            throw new IllegalArgumentException("支付方式不能為空");
        }

// 根據支付方式驗證必要欄位
        switch (paymentMethod) {
            case CREDIT_CARD:
            case DEBIT_CARD:
                validateCreditCardInfo();
                break;
            case BANK_TRANSFER:
                validateBankTransferInfo();
                break;
            case MOBILE_PAYMENT:
            case LINE_PAY:
            case JKO_PAY:
            case APPLE_PAY:
            case GOOGLE_PAY:
                // 手機支付可能需要其他驗證
                break;
            case E_WALLET:
                // 電子錢包驗證
                break;
            case CASH:
                // 現金支付驗證
                break;
            default:
                throw new IllegalArgumentException("不支援的支付方式: " + paymentMethod);
        }
    }

    // 驗證信用卡資訊
    private void validateCreditCardInfo() {
        if (cardNumber == null || cardNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("信用卡號不能為空");
        }
        if (cardHolderName == null || cardHolderName.trim().isEmpty()) {
            throw new IllegalArgumentException("持卡人姓名不能為空");
        }
        if (cardExpiryDate == null || cardExpiryDate.trim().isEmpty()) {
            throw new IllegalArgumentException("信用卡到期日不能為空");
        }
        if (cvv == null || cvv.trim().isEmpty()) {
            throw new IllegalArgumentException("安全碼不能為空");
        }
    }

    // 驗證銀行轉帳資訊
    private void validateBankTransferInfo() {
        if (bankCode == null || bankCode.trim().isEmpty()) {
            throw new IllegalArgumentException("銀行代碼不能為空");
        }
    }

    // 用於日誌記錄的方法
    public String toLogString() {
        return String.format("TopUpRequest{amount=%.2f, paymentMethod=%s, requestTime=%s}",
                amount, paymentMethod, requestTime);
    }

    // 遮蔽敏感資訊的方法
    public String getMaskedCardNumber() {
        if (cardNumber == null || cardNumber.length() < 4) {
            return null;
        }
        return "**** **** **** " + cardNumber.substring(cardNumber.length() - 4);
    }

    // 建立交易描述
    public String createTransactionDescription() {
        return String.format("儲值 %.2f 元 (使用%s)",
                amount,
                paymentMethod.getDescription());
    }
}