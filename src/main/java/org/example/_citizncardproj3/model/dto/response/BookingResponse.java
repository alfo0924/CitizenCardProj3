package org.example._citizncardproj3.model.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example._citizncardproj3.model.entity.Booking;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BookingResponse {

    private Long bookingId;
    private String bookingNumber;
    private String memberName;
    private String memberEmail;
    private String movieName;
    private Long movieId;
    private Long scheduleId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime showTime;

    private String venueName;
    private String roomNumber;
    private List<String> seatNumbers;
    private BookingStatus status;
    private PaymentStatus paymentStatus;

    private PriceDetails priceDetails;
    private String discountCode;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime bookingTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime paymentTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime cancelTime;

    private String cancelReason;
    private String message;

    // 訂票狀態枚舉
    public enum BookingStatus {
        PENDING("待付款"),
        CONFIRMED("已確認"),
        COMPLETED("已完成"),
        CANCELLED("已取消");

        private final String description;

        BookingStatus(String description) {
            this.description = description;
        }

        public static BookingStatus fromEntityStatus(Booking.BookingStatus status) {
            switch (status) {
                case PENDING:
                    return PENDING;
                case CONFIRMED:
                    return CONFIRMED;
                case COMPLETED:
                    return COMPLETED;
                case CANCELLED:
                    return CANCELLED;
                default:
                    throw new IllegalArgumentException("Unknown booking status: " + status);
            }
        }

        public String getDescription() {
            return description;
        }
    }

    // 支付狀態枚舉
    public enum PaymentStatus {
        UNPAID("未付款"),
        PAID("已付款"),
        REFUNDED("已退款"),
        FAILED("付款失敗");

        private final String description;

        PaymentStatus(String description) {
            this.description = description;
        }

        public static PaymentStatus fromEntityStatus(Booking.PaymentStatus paymentStatus) {
            switch (paymentStatus) {
                case UNPAID:
                    return UNPAID;
                case PAID:
                    return PAID;
                case REFUNDED:
                    return REFUNDED;
                case FAILED:
                    return FAILED;
                default:
                    throw new IllegalArgumentException("Unknown payment status: " + paymentStatus);
            }
        }

        public String getDescription() {
            return description;
        }
    }
    // 價格詳情內部類
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PriceDetails {
        private Double originalPrice;
        private Double discountAmount;
        private Double finalPrice;
        private String discountDescription;
    }

    // 座位資訊內部類
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SeatInfo {
        private String seatNumber;
        private String seatType;
        private Double price;
    }

    // 建構子 - 用於錯誤響應
    public BookingResponse(String message) {
        this.message = message;
    }

    // 建構子 - 複製現有響應並添加消息
    public BookingResponse(BookingResponse response, String message) {
        if (response != null) {
            this.bookingId = response.getBookingId();
            this.bookingNumber = response.getBookingNumber();
            this.memberName = response.getMemberName();
            this.memberEmail = response.getMemberEmail();
            this.movieName = response.getMovieName();
            this.movieId = response.getMovieId();
            this.scheduleId = response.getScheduleId();
            this.showTime = response.getShowTime();
            this.venueName = response.getVenueName();
            this.roomNumber = response.getRoomNumber();
            this.seatNumbers = response.getSeatNumbers();
            this.status = response.getStatus();
            this.paymentStatus = response.getPaymentStatus();
            this.priceDetails = response.getPriceDetails();
            this.discountCode = response.getDiscountCode();
            this.bookingTime = response.getBookingTime();
            this.paymentTime = response.getPaymentTime();
            this.cancelTime = response.getCancelTime();
            this.cancelReason = response.getCancelReason();
        }
        this.message = message;
    }

    // 輔助方法 - 檢查訂票是否可以取消
    public boolean isCancellable() {
        if (status == BookingStatus.CANCELLED) {
            return false;
        }

        if (showTime == null) {
            return false;
        }

        // 例如：只能在放映前24小時取消
        return LocalDateTime.now().plusHours(24).isBefore(showTime);
    }

    // 輔助方法 - 檢查訂票是否可以退款
    public boolean isRefundable() {
        if (paymentStatus != PaymentStatus.PAID) {
            return false;
        }

        return isCancellable();
    }

    // 輔助方法 - 獲取訂票狀態描述
    public String getStatusDescription() {
        if (status == null) {
            return "未知狀態";
        }
        return status.getDescription();
    }

    // 輔助方法 - 獲取支付狀態描述
    public String getPaymentStatusDescription() {
        if (paymentStatus == null) {
            return "未知狀態";
        }
        return paymentStatus.getDescription();
    }

    // 用於日誌記錄的方法
    public String toLogString() {
        return String.format("Booking{id=%d, number='%s', movie='%s', status=%s, paymentStatus=%s}",
                bookingId, bookingNumber, movieName, status, paymentStatus);
    }
}