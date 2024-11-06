package org.example._citizncardproj3.model.entity;

import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "Bookings")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "BookingID")
    private Long bookingId;

    @Column(name = "BookingNumber", unique = true, nullable = false, length = 20)
    private String bookingNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MemberID", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ScheduleID", nullable = false)
    private MovieSchedule schedule;

    @Column(name = "TotalAmount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "DiscountApplied", precision = 10, scale = 2)
    private BigDecimal discountApplied;

    @Enumerated(EnumType.STRING)
    @Column(name = "PaymentStatus", nullable = false, length = 20)
    private PaymentStatus paymentStatus;

    @Column(name = "PaymentTime")
    private LocalDateTime paymentTime;

    @Column(name = "RefundAmount", precision = 10, scale = 2)
    private BigDecimal refundAmount;

    @Column(name = "RefundTime")
    private LocalDateTime refundTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "BookingSource", nullable = false, length = 20)
    private BookingSource bookingSource;

    @Enumerated(EnumType.STRING)
    @Column(name = "Status", nullable = false, length = 20)
    private BookingStatus status;

    @Column(name = "SpecialRequests", columnDefinition = "TEXT")
    private String specialRequests;

    @Column(name = "CheckInTime")
    private LocalDateTime checkInTime;

    @Column(name = "CheckInStatus")
    private Boolean checkInStatus;

    @CreationTimestamp
    @Column(name = "CreatedAt", nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "UpdatedAt", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL)
    private List<SeatBooking> seatBookings;

    

    // 訂票來源枚舉
    @Getter
    public enum BookingSource {
        WEBSITE("網站"),
        APP("APP"),
        ONSITE("現場"),
        PHONE("電話");

        private final String description;

        BookingSource(String description) {
            this.description = description;
        }

    }

    // 訂票狀態枚舉
    @Getter
    public enum BookingStatus {
        PENDING("已預訂"),
        CONFIRMED("已確認"),
        COMPLETED("已完成"),
        CANCELLED("已取消");

        private final String description;

        BookingStatus(String description) {
            this.description = description;
        }

    }

    // 支付狀態枚舉
    @Getter
    public enum PaymentStatus {
        UNPAID("未付款"),
        PAID("已付款"),
        REFUNDED("已退款"),
        CANCELLED("已取消");

        private final String description;

        PaymentStatus(String description) {
            this.description = description;
        }

    }

    // 業務方法
    @PrePersist
    public void prePersist() {
        if (this.bookingNumber == null) {
            this.bookingNumber = generateBookingNumber();
        }
        if (this.checkInStatus == null) {
            this.checkInStatus = false;
        }
        if (this.status == null) {
            this.status = BookingStatus.PENDING;
        }
        if (this.paymentStatus == null) {
            this.paymentStatus = PaymentStatus.UNPAID;
        }
        if (this.bookingSource == null) {
            this.bookingSource = BookingSource.WEBSITE;
        }
    }

    private String generateBookingNumber() {
        return "BK" + System.currentTimeMillis();
    }

    public void calculateTotalAmount() {
        if (seatBookings != null && !seatBookings.isEmpty()) {
            this.totalAmount = BigDecimal.valueOf(schedule.getBasePrice())
                    .multiply(BigDecimal.valueOf(seatBookings.size()));
            if (this.discountApplied != null) {
                this.totalAmount = this.totalAmount.subtract(this.discountApplied);
            }
        }
    }

    public void confirm() {
        if (this.status == BookingStatus.PENDING) {
            this.status = BookingStatus.CONFIRMED;
        } else {
            throw new IllegalStateException("只有待確認的訂票可以確認");
        }
    }

    public void complete() {
        if (this.status == BookingStatus.CONFIRMED) {
            this.status = BookingStatus.COMPLETED;
        } else {
            throw new IllegalStateException("只有已確認的訂票可以完成");
        }
    }

    public void cancel(String reason) {
        if (this.status != BookingStatus.CANCELLED) {
            this.status = BookingStatus.CANCELLED;
            this.paymentStatus = PaymentStatus.CANCELLED;
        } else {
            throw new IllegalStateException("訂票已被取消");
        }
    }

    public void checkIn() {
        if (!this.checkInStatus) {
            this.checkInStatus = true;
            this.checkInTime = LocalDateTime.now();
        } else {
            throw new IllegalStateException("已經完成報到");
        }
    }

    public boolean isModifiable() {
        return this.status == BookingStatus.PENDING;
    }

    public boolean isCancellable() {
        return this.status != BookingStatus.CANCELLED &&
                this.status != BookingStatus.COMPLETED;
    }

    public boolean isRefundable() {
        return this.paymentStatus == PaymentStatus.PAID &&
                this.status != BookingStatus.COMPLETED;
    }
}