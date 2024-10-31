package org.example._citizncardproj3.service;

import org.example._citizncardproj3.model.dto.request.BookingRequest;
import org.example._citizncardproj3.model.dto.response.BookingResponse;
import org.example._citizncardproj3.model.entity.Booking;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface BookingService {

    /**
     * 創建訂票
     * @param userEmail 用戶郵箱
     * @param request 訂票請求
     * @return 訂票響應
     */
    BookingResponse createBooking(String userEmail, BookingRequest request);

    /**
     * 取消訂票
     * @param userEmail 用戶郵箱
     * @param bookingId 訂票ID
     * @param reason 取消原因
     * @return 訂票響應
     */
    BookingResponse cancelBooking(String userEmail, Long bookingId, String reason);

    /**
     * 獲取訂票詳情
     * @param userEmail 用戶郵箱
     * @param bookingId 訂票ID
     * @return 訂票響應
     */
    BookingResponse getBooking(String userEmail, Long bookingId);

    /**
     * 獲取會員訂票列表
     * @param userEmail 用戶郵箱
     * @param pageable 分頁參數
     * @return 訂票響應分頁
     */
    Page<BookingResponse> getMemberBookings(String userEmail, Pageable pageable);

    /**
     * 檢查座位是否可用
     * @param scheduleId 場次ID
     * @param seatNumber 座位號碼
     * @return 座位是否可用
     */
    boolean isSeatAvailable(Long scheduleId, String seatNumber);

    /**
     * 計算訂票總金額
     * @param scheduleId 場次ID
     * @param seatCount 座位數量
     * @param discountCode 優惠碼(可選)
     * @return 總金額
     */
    double calculateTotalAmount(Long scheduleId, int seatCount, String discountCode);

    /**
     * 處理訂票支付
     * @param bookingId 訂票ID
     * @return 支付是否成功
     */
    boolean processPayment(Long bookingId);

    /**
     * 處理訂票退款
     * @param bookingId 訂票ID
     * @return 退款是否成功
     */
    boolean processRefund(Long bookingId);

    /**
     * 檢查訂票是否可以取消
     * @param bookingId 訂票ID
     * @return 是否可以取消
     */
    boolean isCancellable(Long bookingId);

    /**
     * 檢查訂票是否可以退款
     * @param bookingId 訂票ID
     * @return 是否可以退款
     */
    boolean isRefundable(Long bookingId);

    /**
     * 獲取場次已訂座位
     * @param scheduleId 場次ID
     * @return 已訂座位列表
     */
    List<String> getBookedSeats(Long scheduleId);

    /**
     * 更新訂票狀態
     * @param bookingId 訂票ID
     * @param status 新狀態
     * @return 更新後的訂票響應
     */
    BookingResponse updateBookingStatus(Long bookingId, Booking.BookingStatus status);

    /**
     * 檢查並取消過期未支付訂票
     */
    void cancelExpiredBookings();
}