package org.example._citizncardproj3.model.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
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
public class WalletTopUpRequest {

    @NotNull(message = "儲值金額不能為空")
    @Positive(message = "儲值金額必須大於0")
    @Max(value = 50000, message = "單次儲值金額不能超過50000")
    private Double amount;

    @NotNull(message = "支付方式不能為空")
    private PaymentMethod paymentMethod;

    // 信用卡支付相關資訊
    private CreditCardInfo creditCardInfo;

    // 超商支付相關資訊
    private ConvenienceStoreInfo convenienceStoreInfo;

    // 支付方式枚舉
    public enum PaymentMethod {
        CREDIT_CARD("信用卡支付"),
        CONVENIENCE_STORE("超商支付"),
        BANK_TRANSFER("銀行轉帳");

        private final String description;

        PaymentMethod(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    // 信用卡資訊內部類
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreditCardInfo {
        @NotBlank(message = "信用卡號不能為空")
        @Pattern(regexp = "^[0-9]{16}$", message = "請輸入有效的16位信用卡號")
        private String cardNumber;

        @NotBlank(message = "持卡人姓名不能為空")
        private String cardHolderName;

        @NotBlank(message = "有效期限不能為空")
        @Pattern(regexp = "^(0[1-9]|1[0-2])/([0-9]{2})$", message = "請輸入有效的有效期限 (MM/YY)")
        private String expiryDate;

        @NotBlank(message = "安全碼不能為空")
        @Pattern(regexp = "^[0-9]{3,4}$", message = "請輸入有效的安全碼")
        private String cvv;

        // 驗證信用卡資訊
        public boolean isValid() {
            return isCardNumberValid() &&
                    isExpiryDateValid() &&
                    isCvvValid();
        }

        private boolean isCardNumberValid() {
            if (cardNumber == null) return false;
            String number = cardNumber.replaceAll("\\s+", "");
            return number.matches("^[0-9]{16}$") && luhnCheck(number);
        }

        private boolean isExpiryDateValid() {
            if (expiryDate == null) return false;
            String[] parts = expiryDate.split("/");
            if (parts.length != 2) return false;

            try {
                int month = Integer.parseInt(parts[0]);
                int year = Integer.parseInt(parts[1]) + 2000;
                LocalDateTime now = LocalDateTime.now();
                LocalDateTime cardDate = LocalDateTime.of(year, month, 1, 0, 0);
                return cardDate.isAfter(now);
            } catch (NumberFormatException | java.time.DateTimeException e) {
                return false;
            }
        }

        private boolean isCvvValid() {
            return cvv != null && cvv.matches("^[0-9]{3,4}$");
        }

        // Luhn演算法檢查信用卡號
        private boolean luhnCheck(String number) {
            int sum = 0;
            boolean alternate = false;
            for (int i = number.length() - 1; i >= 0; i--) {
                int n = Integer.parseInt(number.substring(i, i + 1));
                if (alternate) {
                    n *= 2;
                    if (n > 9) {
                        n = (n % 10) + 1;
                    }
                }
                sum += n;
                alternate = !alternate;
            }
            return (sum % 10 == 0);
        }
    }

    // 超商支付資訊內部類
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ConvenienceStoreInfo {
        @NotBlank(message = "超商類型不能為空")
        private String storeType;

        @Pattern(regexp = "^09\\d{8}$", message = "請輸入有效的手機號碼")
        private String contactPhone;

        @Email(message = "請輸入有效的Email地址")
        private String notificationEmail;
    }

    // 驗證方法
    public boolean isValid() {
        if (amount == null || amount <= 0 || amount > 50000) {
            return false;
        }

        if (paymentMethod == null) {
            return false;
        }

        switch (paymentMethod) {
            case CREDIT_CARD:
                return creditCardInfo != null && creditCardInfo.isValid();
            case CONVENIENCE_STORE:
                return convenienceStoreInfo != null &&
                        convenienceStoreInfo.getStoreType() != null &&
                        !convenienceStoreInfo.getStoreType().trim().isEmpty();
            case BANK_TRANSFER:
                return true;
            default:
                return false;
        }
    }

    // 清理敏感數據
    public void clearSensitiveData() {
        if (creditCardInfo != null) {
            creditCardInfo.setCardNumber("************" +
                    creditCardInfo.getCardNumber().substring(12));
            creditCardInfo.setCvv("***");
        }
    }

    // 構建日誌信息
    public String toLogString() {
        return String.format("WalletTopUpRequest{amount=%.2f, paymentMethod=%s}",
                amount, paymentMethod);
    }
}