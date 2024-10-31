package org.example._citizncardproj3.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "bookings")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long bookingId;

    @Column(unique = true, nullable = false)
    private String bookingNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "movie_id", nullable = false)
    private CityMovie movie;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_id", nullable = false)
    private MovieSchedule schedule;

    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL)
    private List<SeatBooking> seatBookings;

    @Column(nullable = false)
    private Double totalAmount;

    @Column(nullable = false)
    private Double discountAmount;

    @Column(nullable = false)
    private Double finalAmount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "discount_id")
    private Discount discount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookingStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus paymentStatus;

    @Column(length = 50)
    private String paymentMethod;

    @Column
    private LocalDateTime paymentTime;

    @Column
    private LocalDateTime cancelTime;

    @Column
    private String cancelReason;

    @Column(length = 500)
    private String specialRequests;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Column
    private Boolean isDeleted;

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

        public String getDescription() {
            return description;
        }
    }

    // 業務方法

    // 生成訂票編號
    @PrePersist
    public void generateBookingNumber() {
        if (this.bookingNumber == null) {
            this.bookingNumber = "BK" + System.currentTimeMillis();
        }
        if (this.isDeleted == null) {
            this.isDeleted = false;
        }
    }

    // 計算總金額
    public void calculateTotalAmount() {
        if (seatBookings != null && !seatBookings.isEmpty()) {
            this.totalAmount = schedule.getBasePrice() * seatBookings.size();
            this.discountAmount = calculateDiscountAmount();
            this.finalAmount = this.totalAmount - this.discountAmount;
        }
    }

    // 計算折扣金額
    private Double calculateDiscountAmount() {
        if (discount != null) {
            return discount.calculateDiscount(this.totalAmount);
        }
        return 0.0;
    }

    // 確認訂票
    public void confirm() {
        if (this.status == BookingStatus.PENDING) {
            this.status = BookingStatus.CONFIRMED;
        } else {
            throw new IllegalStateException("只有待付款狀態的訂票可以確認");
        }
    }

    // 完成訂票
    public void complete() {
        if (this.status == BookingStatus.CONFIRMED) {
            this.status = BookingStatus.COMPLETED;
        } else {
            throw new IllegalStateException("只有已確認狀態的訂票可以完成");
        }
    }

    // 取消訂票
    public void cancel(String reason) {
        if (this.status != BookingStatus.CANCELLED) {
            this.status = BookingStatus.CANCELLED;
            this.cancelTime = LocalDateTime.now();
            this.cancelReason = reason;
        } else {
            throw new IllegalStateException("訂票已經被取消");
        }
    }

    // 支付訂票
    public void pay(String paymentMethod) {
        if (this.paymentStatus == PaymentStatus.UNPAID) {
            this.paymentStatus = PaymentStatus.PAID;
            this.paymentMethod = paymentMethod;
            this.paymentTime = LocalDateTime.now();
            this.confirm();
        } else {
            throw new IllegalStateException("訂票已支付或已退款");
        }
    }

    // 退款
    public void refund() {
        if (this.paymentStatus == PaymentStatus.PAID) {
            this.paymentStatus = PaymentStatus.REFUNDED;
            this.cancel("申請退款");
        } else {
            throw new IllegalStateException("只有已支付的訂票可以退款");
        }
    }

    // 檢查是否可以修改
    public boolean isModifiable() {
        return this.status == BookingStatus.PENDING;
    }

    // 檢查是否可以取消
    public boolean isCancellable() {
        return this.status != BookingStatus.CANCELLED &&
                this.status != BookingStatus.COMPLETED;
    }

    // 檢查是否可以退款
    public boolean isRefundable() {
        return this.paymentStatus == PaymentStatus.PAID &&
                this.status != BookingStatus.COMPLETED;
    }
}