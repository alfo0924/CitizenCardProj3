package org.example._citizncardproj3.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class CustomException extends RuntimeException {
    private final String message;
    private final HttpStatus status;
    private final String errorCode;

    public CustomException(String message, HttpStatus status, String errorCode) {
        super(message);
        this.message = message;
        this.status = status;
        this.errorCode = errorCode;
    }

    // 認證相關異常
    public static class AuthenticationException extends CustomException {
        public AuthenticationException(String message) {
            super(message, HttpStatus.UNAUTHORIZED, "AUTH_001");
        }
    }

    public static class InvalidCredentialsException extends CustomException {
        public InvalidCredentialsException() {
            super("帳號或密碼錯誤", HttpStatus.UNAUTHORIZED, "AUTH_002");
        }
    }

    public static class TokenExpiredException extends CustomException {
        public TokenExpiredException() {
            super("Token已過期，請重新登入", HttpStatus.UNAUTHORIZED, "AUTH_003");
        }
    }

    // 會員相關異常
    public static class MemberNotFoundException extends CustomException {
        public MemberNotFoundException(String email) {
            super("找不到會員: " + email, HttpStatus.NOT_FOUND, "MEMBER_001");
        }
    }

    public static class DuplicateEmailException extends CustomException {
        public DuplicateEmailException(String email) {
            super("Email已被註冊: " + email, HttpStatus.CONFLICT, "MEMBER_002");
        }
    }

    public static class AccountLockedException extends CustomException {
        public AccountLockedException() {
            super("帳號已被鎖定，請聯繫客服", HttpStatus.FORBIDDEN, "MEMBER_003");
        }
    }

    // 電影相關異常
    public static class MovieNotFoundException extends CustomException {
        public MovieNotFoundException(Long movieId) {
            super("找不到電影: " + movieId, HttpStatus.NOT_FOUND, "MOVIE_001");
        }
    }

    public static class InvalidScheduleException extends CustomException {
        public InvalidScheduleException(String message) {
            super(message, HttpStatus.BAD_REQUEST, "MOVIE_002");
        }
    }

    public static class SeatNotAvailableException extends CustomException {
        public SeatNotAvailableException(String seatNumber) {
            super("座位已被預訂: " + seatNumber, HttpStatus.CONFLICT, "MOVIE_003");
        }
    }

    // 訂票相關異常
    public static class BookingNotFoundException extends CustomException {
        public BookingNotFoundException(Long bookingId) {
            super("找不到訂票紀錄: " + bookingId, HttpStatus.NOT_FOUND, "BOOKING_001");
        }
    }

    public static class BookingCancelledException extends CustomException {
        public BookingCancelledException(Long bookingId) {
            super("訂票已取消: " + bookingId, HttpStatus.BAD_REQUEST, "BOOKING_002");
        }
    }

    public static class BookingTimeExpiredException extends CustomException {
        public BookingTimeExpiredException() {
            super("已超過可訂票時間", HttpStatus.BAD_REQUEST, "BOOKING_003");
        }
    }

    // 電子錢包相關異常
    public static class InsufficientBalanceException extends CustomException {
        public InsufficientBalanceException() {
            super("餘額不足", HttpStatus.BAD_REQUEST, "WALLET_001");
        }
    }

    public static class WalletNotFoundException extends CustomException {
        public WalletNotFoundException(Long walletId) {
            super("找不到錢包: " + walletId, HttpStatus.NOT_FOUND, "WALLET_002");
        }
    }

    public static class WalletFreezedException extends CustomException {
        public WalletFreezedException() {
            super("錢包已被凍結", HttpStatus.FORBIDDEN, "WALLET_003");
        }
    }

    // 優惠相關異常
    public static class DiscountNotFoundException extends CustomException {
        public DiscountNotFoundException(Long discountId) {
            super("找不到優惠: " + discountId, HttpStatus.NOT_FOUND, "DISCOUNT_001");
        }
    }

    public static class DiscountExpiredException extends CustomException {
        public DiscountExpiredException(String discountCode) {
            super("優惠已過期: " + discountCode, HttpStatus.BAD_REQUEST, "DISCOUNT_002");
        }
    }

    public static class DiscountUsedException extends CustomException {
        public DiscountUsedException(String discountCode) {
            super("優惠已使用: " + discountCode, HttpStatus.BAD_REQUEST, "DISCOUNT_003");
        }
    }

    // 文件相關異常
    public static class FileUploadException extends CustomException {
        public FileUploadException(String message) {
            super(message, HttpStatus.BAD_REQUEST, "FILE_001");
        }
    }

    public static class InvalidFileTypeException extends CustomException {
        public InvalidFileTypeException(String message) {
            super(message, HttpStatus.BAD_REQUEST, "FILE_002");
        }
    }

    // 系統相關異常
    public static class SystemException extends CustomException {
        public SystemException(String message) {
            super(message, HttpStatus.INTERNAL_SERVER_ERROR, "SYS_001");
        }
    }

    public static class ValidationException extends CustomException {
        public ValidationException(String message) {
            super(message, HttpStatus.BAD_REQUEST, "SYS_002");
        }
    }


    // 場地相關異常
    public static class VenueNotFoundException extends CustomException {
        public VenueNotFoundException(Long venueId) {
            super("找不到場地: " + venueId, HttpStatus.NOT_FOUND, "VENUE_001");
        }
    }

    public static class VenueNotAvailableException extends CustomException {
        public VenueNotAvailableException(String message) {
            super(message, HttpStatus.CONFLICT, "VENUE_002");
        }
    }

    public static class VenueMaintenanceException extends CustomException {
        public VenueMaintenanceException(Long venueId) {
            super("場地正在維護中: " + venueId, HttpStatus.CONFLICT, "VENUE_003");
        }
    }

    public static class VenueCapacityException extends CustomException {
        public VenueCapacityException(String message) {
            super(message, HttpStatus.BAD_REQUEST, "VENUE_004");
        }
    }

    // 座位相關異常
    public static class SeatNotFoundException extends CustomException {
        public SeatNotFoundException(String seatLabel) {
            super("找不到座位: " + seatLabel, HttpStatus.NOT_FOUND, "SEAT_001");
        }
    }

    public static class SeatMaintenanceException extends CustomException {
        public SeatMaintenanceException(String seatLabel) {
            super("座位維護中: " + seatLabel, HttpStatus.CONFLICT, "SEAT_002");
        }
    }

    public static class SeatLockedException extends CustomException {
        public SeatLockedException(String seatLabel) {
            super("座位已被鎖定: " + seatLabel, HttpStatus.CONFLICT, "SEAT_003");
        }
    }

    public static class InvalidSeatConfigurationException extends CustomException {
        public InvalidSeatConfigurationException(String message) {
            super(message, HttpStatus.BAD_REQUEST, "SEAT_004");
        }
    }

    // 交易相關異常
    public static class TransactionNotFoundException extends CustomException {
        public TransactionNotFoundException(Long transactionId) {
            super("找不到交易記錄: " + transactionId, HttpStatus.NOT_FOUND, "TRANSACTION_001");
        }
    }

    public static class TransactionFailedException extends CustomException {
        public TransactionFailedException(String message) {
            super(message, HttpStatus.BAD_REQUEST, "TRANSACTION_002");
        }
    }

    public static class TransactionTimeoutException extends CustomException {
        public TransactionTimeoutException(Long transactionId) {
            super("交易已超時: " + transactionId, HttpStatus.REQUEST_TIMEOUT, "TRANSACTION_003");
        }
    }

    public static class DuplicateTransactionException extends CustomException {
        public DuplicateTransactionException(String transactionNumber) {
            super("重複的交易編號: " + transactionNumber, HttpStatus.CONFLICT, "TRANSACTION_004");
        }
    }

    public static class InvalidTransactionStatusException extends CustomException {
        public InvalidTransactionStatusException(String message) {
            super(message, HttpStatus.BAD_REQUEST, "TRANSACTION_005");
        }
    }

    public static class TransactionLimitExceededException extends CustomException {
        public TransactionLimitExceededException(String message) {
            super(message, HttpStatus.FORBIDDEN, "TRANSACTION_006");
        }
    }


    /**
     * 場次不存在異常
     */
    public static class ScheduleNotFoundException extends CustomException {
        public ScheduleNotFoundException(Long scheduleId) {
            super("找不到場次: " + scheduleId, HttpStatus.NOT_FOUND, "SCHEDULE_001");
        }
    }

    /**
     * 場次衝突異常
     */
    public static class ScheduleConflictException extends CustomException {
        public ScheduleConflictException(String message) {
            super(message, HttpStatus.CONFLICT, "SCHEDULE_002");
        }
    }

    /**
     * 場次已取消異常
     */
    public static class ScheduleCancelledException extends CustomException {
        public ScheduleCancelledException(Long scheduleId) {
            super("場次已取消: " + scheduleId, HttpStatus.BAD_REQUEST, "SCHEDULE_003");
        }
    }

    public static class EmailAlreadyExistsException extends CustomException {
        public EmailAlreadyExistsException(String email) {
            super("Email已被註冊: " + email, HttpStatus.CONFLICT, "MEMBER_002");
        }
    }

    public static class PhoneAlreadyExistsException extends CustomException {
        public PhoneAlreadyExistsException(String phone) {
            super("手機號碼已被註冊: " + phone, HttpStatus.CONFLICT, "MEMBER_003");
        }
    }

    public static class PasswordMismatchException extends CustomException {
        public PasswordMismatchException() {
            super("密碼不一致", HttpStatus.BAD_REQUEST, "MEMBER_004");
        }
    }

    public static class DuplicatePhoneException extends RuntimeException {
        public DuplicatePhoneException(String phone) {
            super("手機號碼已被使用: " + phone);
        }
    }

    public static class AccountNotActiveException extends RuntimeException {
        public AccountNotActiveException(String email) {
            super("帳號尚未啟用: " + email);
        }
    }

    public static class InvalidTokenException extends RuntimeException {
        public InvalidTokenException(String message) {
            super(message);
        }
    }

}