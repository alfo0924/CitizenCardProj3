package org.example._citizncardproj3.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "seat_bookings")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeatBooking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long seatBookingId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_id", nullable = false)
    private MovieSchedule schedule;

    @Column(nullable = false)
    private String seatNumber;

    @Column(nullable = false)
    private String seatRow;

    @Column(nullable = false)
    private String seatColumn;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SeatType seatType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SeatStatus status;

    @Column(nullable = false)
    private Double price;

    private String specialRequirements;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Column(nullable = false)
    private Boolean isDeleted;

    // 座位類型枚舉
    public enum SeatType {
        REGULAR("一般座位"),
        VIP("VIP座位"),
        COUPLE("情侶座"),
        HANDICAP("無障礙座位");

        private final String description;

        SeatType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    // 座位狀態枚舉
    public enum SeatStatus {
        AVAILABLE("可用"),
        BOOKED("已預訂"),
        OCCUPIED("已佔用"),
        LOCKED("已鎖定"),
        MAINTENANCE("維護中");

        private final String description;

        SeatStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    // 初始化方法
    @PrePersist
    public void prePersist() {
        if (this.isDeleted == null) {
            this.isDeleted = false;
        }
        if (this.status == null) {
            this.status = SeatStatus.AVAILABLE;
        }
        if (this.seatNumber == null) {
            this.seatNumber = generateSeatNumber();
        }
    }

    // 生成座位編號
    private String generateSeatNumber() {
        return this.seatRow + this.seatColumn;
    }

    // 業務方法

    // 預訂座位
    public void book() {
        if (isAvailable()) {
            this.status = SeatStatus.BOOKED;
        } else {
            throw new IllegalStateException("座位無法預訂");
        }
    }

    // 取消預訂
    public void cancelBooking() {
        if (this.status == SeatStatus.BOOKED) {
            this.status = SeatStatus.AVAILABLE;
        } else {
            throw new IllegalStateException("座位未被預訂，無法取消");
        }
    }

    // 佔用座位
    public void occupy() {
        if (this.status == SeatStatus.BOOKED) {
            this.status = SeatStatus.OCCUPIED;
        } else {
            throw new IllegalStateException("座位未被預訂，無法佔用");
        }
    }

    // 鎖定座位
    public void lock() {
        if (isAvailable()) {
            this.status = SeatStatus.LOCKED;
        }
    }

    // 解鎖座位
    public void unlock() {
        if (this.status == SeatStatus.LOCKED) {
            this.status = SeatStatus.AVAILABLE;
        }
    }

    // 設置維護狀態
    public void setMaintenance() {
        this.status = SeatStatus.MAINTENANCE;
    }

    // 計算座位價格
    public void calculatePrice() {
        double basePrice = this.schedule.getBasePrice();
        switch (this.seatType) {
            case VIP:
                this.price = basePrice * 1.5; // VIP座位加價50%
                break;
            case COUPLE:
                this.price = basePrice * 1.3; // 情侶座加價30%
                break;
            case HANDICAP:
                this.price = basePrice * 0.8; // 無障礙座位優惠20%
                break;
            default:
                this.price = basePrice;
        }
    }

    // 檢查座位是否可用
    public boolean isAvailable() {
        return this.status == SeatStatus.AVAILABLE &&
                !this.isDeleted;
    }

    // 檢查座位是否可以取消
    public boolean isCancellable() {
        return this.status == SeatStatus.BOOKED &&
                LocalDateTime.now().isBefore(this.schedule.getShowTime().minusHours(1));
    }

    // 檢查是否為特殊座位
    public boolean isSpecialSeat() {
        return this.seatType == SeatType.VIP ||
                this.seatType == SeatType.COUPLE ||
                this.seatType == SeatType.HANDICAP;
    }

    // 用於日誌記錄的方法
    public String toLogString() {
        return String.format("SeatBooking{id=%d, seat='%s', type=%s, status=%s}",
                seatBookingId, seatNumber, seatType, status);
    }
}