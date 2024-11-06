package org.example._citizncardproj3.model.entity;

import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "SeatBookings")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeatBooking {

    @EmbeddedId
    private SeatBookingId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("bookingId")
    @JoinColumn(name = "BookingID", nullable = false)
    private Booking booking;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("scheduleId")
    @JoinColumn(name = "ScheduleID", nullable = false)
    private MovieSchedule schedule;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("seatId")
    @JoinColumn(name = "SeatID", nullable = false)
    private SeatManagement seat;

    @Enumerated(EnumType.STRING)
    @Column(name = "Status", nullable = false)
    private SeatStatus status;

    @CreationTimestamp
    @Column(name = "CreatedAt", nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "UpdatedAt", nullable = false)
    private LocalDateTime updatedAt;

    // 座位狀態枚舉
    @Getter
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

    }

    // 初始化方法
    @PrePersist
    public void prePersist() {
        if (this.status == null) {
            this.status = SeatStatus.AVAILABLE;
        }
    }

    // 業務方法
    public void book() {
        if (isAvailable()) {
            this.status = SeatStatus.BOOKED;
        } else {
            throw new IllegalStateException("座位無法預訂");
        }
    }

    public void cancelBooking() {
        if (this.status == SeatStatus.BOOKED) {
            this.status = SeatStatus.AVAILABLE;
        } else {
            throw new IllegalStateException("座位未被預訂，無法取消");
        }
    }

    public void occupy() {
        if (this.status == SeatStatus.BOOKED) {
            this.status = SeatStatus.OCCUPIED;
        } else {
            throw new IllegalStateException("座位未被預訂，無法佔用");
        }
    }

    public void lock() {
        if (isAvailable()) {
            this.status = SeatStatus.LOCKED;
        }
    }

    public void unlock() {
        if (this.status == SeatStatus.LOCKED) {
            this.status = SeatStatus.AVAILABLE;
        }
    }

    public void setMaintenance() {
        this.status = SeatStatus.MAINTENANCE;
    }

    public boolean isAvailable() {
        return this.status == SeatStatus.AVAILABLE;
    }

    public boolean isCancellable() {
        return this.status == SeatStatus.BOOKED &&
                LocalDateTime.now().isBefore(
                        LocalDateTime.of(
                                schedule.getShowDate(),
                                schedule.getStartTime()
                        ).minusHours(1)
                );
    }

    public boolean isSpecialSeat() {
        return this.seat.getSeatType() == SeatManagement.SeatType.VIP ||
                this.seat.getSeatType() == SeatManagement.SeatType.COUPLE;
    }

    @Override
    public String toString() {
        return String.format("SeatBooking{bookingId=%d, scheduleId=%d, seatId=%d, status=%s}",
                id.getBookingId(), id.getScheduleId(), id.getSeatId(), status);
    }
}

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
class SeatBookingId implements java.io.Serializable {

    @Column(name = "BookingID")
    private Long bookingId;

    @Column(name = "ScheduleID")
    private Long scheduleId;

    @Column(name = "SeatID")
    private Long seatId;
}