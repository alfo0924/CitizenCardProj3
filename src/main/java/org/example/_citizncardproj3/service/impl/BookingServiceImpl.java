package org.example._citizncardproj3.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example._citizncardproj3.exception.CustomException;
import org.example._citizncardproj3.model.dto.request.BookingRequest;
import org.example._citizncardproj3.model.dto.response.BookingResponse;
import org.example._citizncardproj3.model.entity.*;
import org.example._citizncardproj3.repository.*;
import org.example._citizncardproj3.service.BookingService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final MovieScheduleRepository scheduleRepository;
    private final SeatBookingRepository seatBookingRepository;
    private final MemberRepository memberRepository;
    private final WalletRepository walletRepository;
    private final DiscountRepository discountRepository;

    @Override
    @Transactional
    public BookingResponse createBooking(String userEmail, BookingRequest request) {
        // 驗證會員
        Member member = memberRepository.findByEmail(userEmail)
                .orElseThrow(() -> new CustomException.MemberNotFoundException(userEmail));

        // 驗證場次
        MovieSchedule schedule = scheduleRepository.findById(request.getScheduleId())
                .orElseThrow(() -> new CustomException.InvalidScheduleException("場次不存在"));

        // 驗證場次是否可訂票
        if (!schedule.isBookable()) {
            throw new CustomException.InvalidScheduleException("此場次無法訂票");
        }

        // 驗證座位
        validateSeats(schedule, request.getSeatNumbers());

        // 計算金額
        double totalAmount = calculateTotalAmount(schedule, request.getSeatNumbers().size());
        double discountAmount = 0.0;

        // 處理優惠券
        if (request.getDiscountCode() != null) {
            Discount discount = discountRepository.findByDiscountCode(request.getDiscountCode())
                    .orElseThrow(() -> new CustomException.DiscountNotFoundException(0L));

            if (!discount.isValid()) {
                throw new CustomException.DiscountExpiredException(request.getDiscountCode());
            }

            discountAmount = discount.calculateDiscount(totalAmount);
        }

        double finalAmount = totalAmount - discountAmount;

        // 檢查錢包餘額
        Wallet wallet = walletRepository.findByMember(member)
                .orElseThrow(() -> new CustomException.WalletNotFoundException(0L));

        if (!wallet.hasEnoughBalance(finalAmount)) {
            throw new CustomException.InsufficientBalanceException();
        }

        // 創建訂票記錄
        Booking booking = Booking.builder()
                .member(member)
                .schedule(schedule)
                .status(Booking.BookingStatus.PENDING)
                .paymentStatus(Booking.PaymentStatus.UNPAID)
                .totalAmount(BigDecimal.valueOf(totalAmount))
                .refundAmount(discountAmount)
                .finalAmount(finalAmount)
                .paymentMethod(String.valueOf(request.getPaymentMethod()))
                .specialRequests(request.getSpecialRequests())
                .build();

        booking = bookingRepository.save(booking);

        // 創建座位預訂記錄
        List<SeatBooking> seatBookings = createSeatBookings(booking, schedule, request.getSeatNumbers());
        booking.setSeatBookings(seatBookings);

        // 處理支付
        processPayment(booking, wallet);

        // 更新場次座位數
        schedule.updateAvailableSeats();
        scheduleRepository.save(schedule);

        return convertToResponse(booking);
    }

    @Override
    @Transactional
    public BookingResponse cancelBooking(String userEmail, Long bookingId, String reason) {
        Booking booking = validateBookingOwnership(userEmail, bookingId);

        if (!booking.isCancellable()) {
            throw new IllegalStateException("此訂票無法取消");
        }

        // 處理退款
        if (booking.isRefundable()) {
            processRefund(booking);
        }

        // 更新訂票狀態
        booking.cancel(reason);
        booking = bookingRepository.save(booking);

        // 釋放座位
        releaseSeats(booking);

        // 更新場次座位數
        MovieSchedule schedule = booking.getSchedule();
        schedule.updateAvailableSeats();
        scheduleRepository.save(schedule);

        return convertToResponse(booking);
    }

    @Override
    public BookingResponse getBooking(String userEmail, Long bookingId) {
        Booking booking = validateBookingOwnership(userEmail, bookingId);
        return convertToResponse(booking);
    }

    @Override
    public Page<BookingResponse> getMemberBookings(String userEmail, Pageable pageable) {
        Member member = memberRepository.findByEmail(userEmail)
                .orElseThrow(() -> new CustomException.MemberNotFoundException(userEmail));

        return bookingRepository.findByMemberOrderByCreatedAtDesc(member, pageable)
                .map(this::convertToResponse);
    }

    @Override
    public boolean isSeatAvailable(Long scheduleId, String seatNumber) {
        return false;
    }

    @Override
    public double calculateTotalAmount(Long scheduleId, int seatCount, String discountCode) {
        return 0;
    }

    @Override
    public boolean processPayment(Long bookingId) {
        return false;
    }

    @Override
    public boolean processRefund(Long bookingId) {
        return false;
    }

    @Override
    public boolean isCancellable(Long bookingId) {
        return false;
    }

    @Override
    public boolean isRefundable(Long bookingId) {
        return false;
    }

    @Override
    public List<String> getBookedSeats(Long scheduleId) {
        return List.of();
    }

    @Override
    public BookingResponse updateBookingStatus(Long bookingId, Booking.BookingStatus status) {
        return null;
    }

    @Override
    public Map<String, Object> getBookingStatistics(String startDate, String endDate) {
        return Map.of();
    }

    @Override
    public void cancelExpiredBookings() {

    }

    @Override
    public BookingResponse createBooking(BookingRequest request, String userEmail) {
        return null;
    }

    @Override
    public BookingResponse getBooking(Long bookingId, String userEmail) {
        return null;
    }

    @Override
    public Page<BookingResponse> getUserBookings(String userEmail, Pageable pageable) {
        return null;
    }

    @Override
    public void cancelBooking(Long bookingId, String userEmail) {

    }

    @Override
    public boolean checkSeatsAvailability(Long scheduleId, List<String> seatNumbers) {
        return false;
    }

    // 私有輔助方法

    private void validateSeats(MovieSchedule schedule, List<String> seatNumbers) {
        for (String seatNumber : seatNumbers) {
            if (!schedule.isSeatAvailable(seatNumber)) {
                throw new CustomException.SeatNotAvailableException(seatNumber);
            }
        }
    }

    private double calculateTotalAmount(MovieSchedule schedule, int seatCount) {
        return schedule.getBasePrice() * seatCount;
    }

    private List<SeatBooking> createSeatBookings(Booking booking, MovieSchedule schedule, List<String> seatNumbers) {
        List<SeatBooking> seatBookings = new ArrayList<>();
        for (String seatNumber : seatNumbers) {
            SeatBooking seatBooking = SeatBooking.builder()
                    .booking(booking)
                    .schedule(schedule)
                    .seatNumber(seatNumber)
                    .status(SeatBooking.SeatStatus.BOOKED)
                    .build();
            seatBookings.add(seatBookingRepository.save(seatBooking));
        }
        return seatBookings;
    }

    private void processPayment(Booking booking, Wallet wallet) {
        wallet.subtractBalance(booking.getFinalAmount());
        walletRepository.save(wallet);

        booking.setPaymentStatus(Booking.PaymentStatus.PAID);
        booking.setStatus(Booking.BookingStatus.CONFIRMED);
        booking.setPaymentTime(LocalDateTime.now());
        bookingRepository.save(booking);
    }

    private void processRefund(Booking booking) {
        Wallet wallet = booking.getMember().getWallet();
        wallet.addBalance(booking.getFinalAmount());
        walletRepository.save(wallet);
    }

    private void releaseSeats(Booking booking) {
        for (SeatBooking seatBooking : booking.getSeatBookings()) {
            seatBooking.setStatus(SeatBooking.SeatStatus.AVAILABLE);
            seatBookingRepository.save(seatBooking);
        }
    }

    private Booking validateBookingOwnership(String userEmail, Long bookingId) {
        Member member = memberRepository.findByEmail(userEmail)
                .orElseThrow(() -> new CustomException.MemberNotFoundException(userEmail));

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new CustomException.BookingNotFoundException(bookingId));

        if (!booking.getMember().equals(member)) {
            throw new CustomException.ValidationException("無權限訪問此訂票記錄");
        }

        return booking;
    }

    private BookingResponse convertToResponse(Booking booking) {
        return BookingResponse.builder()
                .bookingId(booking.getBookingId())
                .bookingNumber(booking.getBookingNumber())
                .memberName(booking.getMember().getName())
                .memberEmail(booking.getMember().getEmail())
                .movieName(booking.getSchedule().getMovie().getMovieName())
                .movieId(booking.getSchedule().getMovie().getMovieId())
                .scheduleId(booking.getSchedule().getScheduleId())
                .showTime(booking.getSchedule().getShowTime())
                .venueName(booking.getSchedule().getVenue().getVenueName())
                .roomNumber(booking.getSchedule().getRoomNumber())
                .seatNumbers(booking.getSeatBookings().stream()
                        .map(SeatBooking::getSeatNumber)
                        .toList())
                // 修改這裡：使用BookingResponse.BookingStatus的fromEntityStatus方法
                .status(BookingResponse.BookingStatus.fromEntityStatus(booking.getStatus()))
                .paymentStatus(BookingResponse.PaymentStatus.fromEntityStatus(booking.getPaymentStatus()))
                .priceDetails(new BookingResponse.PriceDetails(
                        booking.getTotalAmount(),
                        booking.getDiscountAmount(),
                        booking.getFinalAmount(),
                        null))
                .bookingTime(booking.getCreatedAt())
                .paymentTime(booking.getPaymentTime())
                .cancelTime(booking.getCancelTime())
                .cancelReason(booking.getCancelReason())
                .build();
    }
}