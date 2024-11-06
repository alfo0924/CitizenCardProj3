package org.example._citizncardproj3.model.entity;

import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Entity
@Table(name = "MovieSchedules")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MovieSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ScheduleID")
    private Long scheduleId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MovieID", nullable = false)
    private CityMovie movie;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "VenueID", nullable = false)
    private Venue venue;

    @Column(name = "ShowDate", nullable = false)
    private LocalDate showDate;

    @Column(name = "StartTime", nullable = false)
    private LocalTime startTime;

    @Column(name = "EndTime", nullable = false)
    private LocalTime endTime;

    @Column(name = "BasePrice", nullable = false)
    private Double basePrice;

    @Column(name = "SpecialPrice")
    private Double specialPrice;

    @Column(name = "TotalSeats", nullable = false)
    private Integer totalSeats;

    @Column(name = "AvailableSeats", nullable = false)
    private Integer availableSeats;

    @Column(name = "RoomNumber", nullable = false)
    private String roomNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "Status", nullable = false)
    private ScheduleStatus status;

    @Column(name = "IsCancelled", nullable = false)
    private Boolean isCancelled;

    @Column(name = "CancellationReason")
    private String cancellationReason;

    @Column(name = "LastBookingTime")
    private LocalDateTime lastBookingTime;

    @Column(name = "IsSpecialEvent")
    private Boolean isSpecialEvent;

    @Column(name = "EventDescription")
    private String eventDescription;

    @CreationTimestamp
    @Column(name = "CreatedAt", nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "UpdatedAt", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "schedule", cascade = CascadeType.ALL)
    private List<Booking> bookings;

    // 場次狀態枚舉
    @Getter
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

    }

    // 初始化方法
    @PrePersist
    public void prePersist() {
        if (this.isCancelled == null) {
            this.isCancelled = false;
        }
        if (this.status == null) {
            this.status = ScheduleStatus.NOT_STARTED;
        }
        if (this.availableSeats == null) {
            this.availableSeats = this.totalSeats;
        }
        if (this.lastBookingTime == null) {
            // 預設最後訂票時間為放映前30分鐘
            this.lastBookingTime = LocalDateTime.of(showDate, startTime).minusMinutes(30);
        }
        if (this.isSpecialEvent == null) {
            this.isSpecialEvent = false;
        }
    }

    // 業務方法
    public void startSale() {
        if (this.status == ScheduleStatus.NOT_STARTED) {
            this.status = ScheduleStatus.ON_SALE;
        } else {
            throw new IllegalStateException("只有未開始的場次可以開始售票");
        }
    }

    public void cancel(String reason) {
        if (hasBookings()) {
            throw new IllegalStateException("已有訂票的場次無法取消");
        }
        this.status = ScheduleStatus.CANCELLED;
        this.isCancelled = true;
        this.cancellationReason = reason;
    }

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

    public boolean isBookable() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime showDateTime = LocalDateTime.of(showDate, startTime);
        return this.status == ScheduleStatus.ON_SALE &&
                !this.isCancelled &&
                this.availableSeats > 0 &&
                now.isBefore(this.lastBookingTime) &&
                now.isBefore(showDateTime);
    }

    public boolean hasBookings() {
        return this.bookings != null &&
                !this.bookings.isEmpty() &&
                this.bookings.stream()
                        .anyMatch(booking -> booking.getStatus() != Booking.BookingStatus.CANCELLED);
    }

    public void updateStatus() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime showDateTime = LocalDateTime.of(showDate, startTime);
        LocalDateTime endDateTime = LocalDateTime.of(showDate, endTime);

        if (now.isAfter(endDateTime)) {
            this.status = ScheduleStatus.ENDED;
        } else if (this.availableSeats == 0) {
            this.status = ScheduleStatus.FULL;
        } else if (now.isAfter(showDateTime) && now.isBefore(endDateTime)) {
            this.status = ScheduleStatus.ON_SALE;
        }
    }

    @Override
    public String toString() {
        return String.format("MovieSchedule{id=%d, movie='%s', showDate=%s, startTime=%s, status=%s}",
                scheduleId, movie.getMovieName(), showDate, startTime, status);
    }
}