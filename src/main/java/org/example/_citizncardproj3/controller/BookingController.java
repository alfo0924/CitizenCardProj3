package org.example._citizncardproj3.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example._citizncardproj3.model.dto.request.BookingRequest;
import org.example._citizncardproj3.model.dto.response.ApiResponse;
import org.example._citizncardproj3.model.dto.response.BookingResponse;
import org.example._citizncardproj3.model.entity.Booking;
import org.example._citizncardproj3.service.BookingService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/v1/bookings")
@RequiredArgsConstructor
@Tag(name = "訂票", description = "訂票相關API")
public class BookingController {

    private final BookingService bookingService;

    @Operation(summary = "創建訂票")
    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<BookingResponse> createBooking(
            @Valid @RequestBody BookingRequest request,
            Authentication authentication) {
        try {
            String userEmail = authentication.getName();
            BookingResponse booking = bookingService.createBooking(request, userEmail);
            return ResponseEntity.ok(booking);
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new BookingResponse(null, e.getMessage()));
        }
    }

    @Operation(summary = "獲取訂票詳情")
    @GetMapping("/{bookingId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<BookingResponse> getBooking(
            @Parameter(description = "訂票ID") @PathVariable Long bookingId,
            Authentication authentication) {
        try {
            String userEmail = authentication.getName();
            BookingResponse booking = bookingService.getBooking(bookingId, userEmail);
            return ResponseEntity.ok(booking);
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(new BookingResponse(null, e.getMessage()));
        }
    }

    @Operation(summary = "獲取用戶所有訂票")
    @GetMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Page<BookingResponse>> getUserBookings(
            Authentication authentication,
            Pageable pageable) {
        String userEmail = authentication.getName();
        Page<BookingResponse> bookings = bookingService.getUserBookings(userEmail, pageable);
        return ResponseEntity.ok(bookings);
    }

    @Operation(summary = "取消訂票")
    @DeleteMapping("/{bookingId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse> cancelBooking(
            @Parameter(description = "訂票ID") @PathVariable Long bookingId,
            Authentication authentication) {
        try {
            String userEmail = authentication.getName();
            bookingService.cancelBooking(bookingId, userEmail);
            return ResponseEntity.ok(new ApiResponse(true, "訂票取消成功"));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, e.getMessage()));
        }
    }

    @Operation(summary = "檢查座位可用性")
    @GetMapping("/check-seats")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse> checkSeatsAvailability(
            @Parameter(description = "場次ID") @RequestParam Long scheduleId,
            @Parameter(description = "座位號碼列表") @RequestParam List<String> seatNumbers) {
        try {
            boolean available = bookingService.checkSeatsAvailability(scheduleId, seatNumbers);
            return ResponseEntity.ok(new ApiResponse(available,
                    available ? "座位可用" : "座位已被預訂"));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, e.getMessage()));
        }
    }

    @Operation(summary = "更新訂票狀態")
    @PatchMapping("/{bookingId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> updateBookingStatus(
            @Parameter(description = "訂票ID") @PathVariable Long bookingId,
            @Parameter(description = "新狀態") @RequestParam String status) {
        try {
            bookingService.updateBookingStatus(bookingId, Booking.BookingStatus.valueOf(status));
            return ResponseEntity.ok(new ApiResponse(true, "訂票狀態更新成功"));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, e.getMessage()));
        }
    }

    @Operation(summary = "獲取訂票統計")
    @GetMapping("/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getBookingStatistics(
            @Parameter(description = "開始日期") @RequestParam String startDate,
            @Parameter(description = "結束日期") @RequestParam String endDate) {
        try {
            return ResponseEntity.ok(bookingService.getBookingStatistics(startDate, endDate));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, e.getMessage()));
        }
    }
}