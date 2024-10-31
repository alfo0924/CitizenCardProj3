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
@Table(name = "movie_schedules")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MovieSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long scheduleId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "movie_id", nullable = false)
    private CityMovie movie;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "venue_id", nullable = false)
    private Venue venue;

    @Column(nullable = false)
    private String roomNumber;

    @Column(nullable = false)
    private LocalDateTime showTime;

    @Column(nullable = false)
    private LocalDateTime endTime;

    @Column(nullable = false)
    private Double basePrice;

    private Double specialPrice;

    @Column(nullable = false)
    private Integer totalSeats;

    @Column(nullable = false)
    private Integer availableSeats;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ScheduleStatus status;

    @OneToMany(mappedBy = "schedule", cascade = CascadeType.ALL)
    private List<Booking> bookings;

    private LocalDateTime lastBookingTime;

    private Boolean isSpecialEvent;

    private String eventDescription;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Column(nullable = false)
    private Boolean isDeleted;

    // 場次狀態枚舉
    public enum ScheduleStatus {
        NOT_STARTED("未開始"),
        ON_SALE("售票中"),
        FULL("已滿座"),
        ENDED("已結束"),
        CANCELLED("已取消");

        private final String description;

        ScheduleStatus(String description) {
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
            this.status = ScheduleStatus.NOT_STARTED;
        }
        if (this.availableSeats == null) {
            this.availableSeats = this.totalSeats;
        }
        if (this.lastBookingTime == null) {
            // 預設最後訂票時間為放映前30分鐘
            this.lastBookingTime = this.showTime.minusMinutes(30);
        }
    }

    // 業務方法

    // 開始售票
    public void startSale() {
        if (this.status == ScheduleStatus.NOT_STARTED) {
            this.status = ScheduleStatus.ON_SALE;
        } else {
            throw new IllegalStateException("只有未開始的場次可以開始售票");
        }
    }

    // 取消場次
    public void cancel(String reason) {
        if (hasBookings()) {
            throw new IllegalStateException("已有訂票的場次無法取消");
        }
        this.status = ScheduleStatus.CANCELLED;
    }

    // 更新座位數量
    public void updateAvailableSeats() {
        int bookedSeats = this.bookings.stream()
                .filter(booking -> booking.getStatus() != Booking.BookingStatus.CANCELLED)
                .mapToInt(booking -> booking.getSeatBookings().size())
                .sum();
        this.availableSeats = this.totalSeats - bookedSeats;

        if (this.availableSeats == 0) {
            this.status = ScheduleStatus.FULL;
        }
    }

    // 檢查座位是否可用
    public boolean isSeatAvailable(String seatNumber) {
        return this.bookings.stream()
                .filter(booking -> booking.getStatus() != Booking.BookingStatus.CANCELLED)
                .flatMap(booking -> booking.getSeatBookings().stream())
                .noneMatch(seatBooking -> seatBooking.getSeatNumber().equals(seatNumber));
    }

    // 檢查是否可以訂票
    public boolean isBookable() {
        LocalDateTime now = LocalDateTime.now();
        return this.status == ScheduleStatus.ON_SALE &&
                !this.isDeleted &&
                this.availableSeats > 0 &&
                now.isBefore(this.lastBookingTime);
    }

    // 檢查是否有訂票
    public boolean hasBookings() {
        return this.bookings != null &&
                !this.bookings.isEmpty() &&
                this.bookings.stream()
                        .anyMatch(booking -> booking.getStatus() != Booking.BookingStatus.CANCELLED);
    }

    // 計算特殊票價
    public Double calculateSpecialPrice(Member member) {
        if (this.specialPrice != null) {
            return this.specialPrice;
        }

        // 根據會員類型計算特殊票價
        if (member.getCitizenCards() != null && !member.getCitizenCards().isEmpty()) {
            CitizenCard card = member.getCitizenCards().get(0);
            switch (card.getCardType()) {
                case SENIOR:
                    return this.basePrice * 0.5; // 敬老卡半價
                case STUDENT:
                    return this.basePrice * 0.8; // 學生卡8折
                case CHARITY:
                    return this.basePrice * 0.5; // 愛心卡半價
                default:
                    return this.basePrice;
            }
        }
        return this.basePrice;
    }

    // 更新場次狀態
    public void updateStatus() {
        LocalDateTime now = LocalDateTime.now();
        if (now.isAfter(this.endTime)) {
            this.status = ScheduleStatus.ENDED;
        } else if (this.availableSeats == 0) {
            this.status = ScheduleStatus.FULL;
        } else if (now.isAfter(this.showTime) && now.isBefore(this.endTime)) {
            this.status = ScheduleStatus.ON_SALE;
        }
    }

    // 用於日誌記錄的方法
    public String toLogString() {
        return String.format("MovieSchedule{id=%d, movie='%s', showTime=%s, status=%s, availableSeats=%d}",
                scheduleId, movie.getMovieName(), showTime, status, availableSeats);
    }

    /**
     * 檢查場次是否可用
     */
    public boolean isAvailable() {
        LocalDateTime now = LocalDateTime.now();
        return this.status == ScheduleStatus.ON_SALE &&
                this.availableSeats > 0 &&
                now.isBefore(this.showTime) &&
                !this.isDeleted;
    }

}