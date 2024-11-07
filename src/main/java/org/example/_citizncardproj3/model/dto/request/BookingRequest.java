package org.example._citizncardproj3.model.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingRequest {

    @NotNull(message = "場次ID不能為空")
    private Long scheduleId;

    @NotNull(message = "電影ID不能為空")
    private Long movieId;

    @NotEmpty(message = "座位號碼不能為空")
    @Size(min = 1, max = 4, message = "一次最多可訂購4個座位")
    private List<String> seatNumbers;

    private String discountCode;

    @NotNull(message = "支付方式不能為空")
    private PaymentMethod paymentMethod;

    private String specialRequests;

    // 支付方式枚舉
    @Getter
    public enum PaymentMethod {
        WALLET("錢包支付"),
        CREDIT_CARD("信用卡支付"),
        CONVENIENCE_STORE("超商支付");

        private final String description;

        PaymentMethod(String description) {
            this.description = description;
        }

    }

    // 訂票驗證相關的內部類
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SeatValidation {
        private String seatNumber;
        private boolean isAvailable;
        private String message;
    }

    // 支付相關的內部類
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaymentDetails {
        @NotNull(message = "支付金額不能為空")
        private Double amount;

        private String cardNumber;

        private String cardHolderName;

        private String expiryDate;

        private String cvv;

        // 只有在使用信用卡支付時才進行驗證
        public boolean validateCreditCardDetails() {
            if (cardNumber == null || cardNumber.trim().isEmpty()) {
                return false;
            }
            if (cardHolderName == null || cardHolderName.trim().isEmpty()) {
                return false;
            }
            if (expiryDate == null || !expiryDate.matches("\\d{2}/\\d{2}")) {
                return false;
            }
            if (cvv == null || !cvv.matches("\\d{3,4}")) {
                return false;
            }
            return true;
        }
    }

    // 優惠相關的內部類
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DiscountValidation {
        private String discountCode;
        private boolean isValid;
        private Double discountAmount;
        private String message;
    }

    // 訂票確認相關的內部類
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BookingConfirmation {
        private String bookingNumber;
        private Double originalAmount;
        private Double discountAmount;
        private Double finalAmount;
        private List<String> seatNumbers;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private String showTime;
        private String movieName;
        private String venueName;
        private String paymentStatus;
    }

    // 驗證方法
    public boolean validateRequest() {
        // 基本驗證
        if (scheduleId == null || movieId == null ||
                seatNumbers == null || seatNumbers.isEmpty()) {
            return false;
        }

        // 座位數量驗證
        if (seatNumbers.size() > 4) {
            return false;
        }

        // 支付方式驗證
        if (paymentMethod == null) {
            return false;
        }

        // 如果使用優惠券，驗證優惠碼
        if (discountCode != null && discountCode.trim().isEmpty()) {
            return false;
        }

        return true;
    }

    // 計算預估金額
    public Double calculateEstimatedAmount(Double basePrice) {
        if (basePrice == null || seatNumbers == null) {
            return 0.0;
        }
        return basePrice * seatNumbers.size();
    }
}