package org.example._citizncardproj3.model.entity;

import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "Venues")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Venue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "VenueID")
    private Long venueId;

    @Column(name = "VenueName", nullable = false)
    private String venueName;

    @Column(name = "Address", nullable = false)
    private String address;

    @Column(name = "TotalSeats", nullable = false)
    private Integer totalSeats;

    @Type(type = "json")
    @Column(name = "SeatingLayout", columnDefinition = "json")
    private String seatingLayout;

    @Enumerated(EnumType.STRING)
    @Column(name = "Status", nullable = false)
    private VenueStatus status;

    @Column(name = "IsDeleted", nullable = false)
    private Boolean isDeleted;

    @Column(name = "DeletedAt")
    private LocalDateTime deletedAt;

    @CreationTimestamp
    @Column(name = "CreatedAt", nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "UpdatedAt", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "venue", cascade = CascadeType.ALL)
    private List<MovieSchedule> schedules;

    @OneToMany(mappedBy = "venue", cascade = CascadeType.ALL)
    private List<SeatManagement> seats;

    // 場地狀態枚舉
    @Getter
    public enum VenueStatus {
        ACTIVE("營業中"),
        MAINTENANCE("裝修中"),
        CLOSED("已關閉");

        private final String description;

        VenueStatus(String description) {
            this.description = description;
        }

    }

    // 初始化方法
    @PrePersist
    public void prePersist() {
        if (this.isDeleted == null) {
            this.isDeleted = false;
        }
        if (this.status == null) {
            this.status = VenueStatus.ACTIVE;
        }
        if (this.seatingLayout == null) {
            this.seatingLayout = generateDefaultSeatingLayout();
        }
    }

    // 生成默認座位布局
    private String generateDefaultSeatingLayout() {
        StringBuilder layout = new StringBuilder();
        layout.append("{\"seats\":[");
        for (int i = 0; i < totalSeats; i++) {
            layout.append("{\"seatId\":").append(i + 1)
                    .append(",\"status\":\"AVAILABLE\"}");
            if (i < totalSeats - 1) layout.append(",");
        }
        layout.append("]}");
        return layout.toString();
    }

    // 業務方法
    public void setMaintenance(LocalDateTime maintenanceDate) {
        if (hasActiveSchedules()) {
            throw new IllegalStateException("場地有未完成的場次，無法進行維護");
        }
        this.status = VenueStatus.MAINTENANCE;
    }

    public void completeMaintenance() {
        if (this.status == VenueStatus.MAINTENANCE) {
            this.status = VenueStatus.ACTIVE;
        }
    }

    public void close() {
        if (hasActiveSchedules()) {
            throw new IllegalStateException("場地有未完成的場次，無法關閉");
        }
        this.status = VenueStatus.CLOSED;
    }

    public void reopen() {
        if (this.status == VenueStatus.CLOSED) {
            this.status = VenueStatus.ACTIVE;
        }
    }

    public void updateSeatingLayout(String newLayout) {
        if (hasActiveSchedules()) {
            throw new IllegalStateException("場地有未完成的場次，無法更新座位布局");
        }
        this.seatingLayout = newLayout;
    }

    // 檢查是否有進行中的場次
    public boolean hasActiveSchedules() {
        if (schedules == null || schedules.isEmpty()) {
            return false;
        }
        LocalDateTime now = LocalDateTime.now();
        return schedules.stream()
                .anyMatch(schedule ->
                        LocalDateTime.of(schedule.getShowDate(), schedule.getStartTime())
                                .isAfter(now) &&
                                !schedule.getIsCancelled());
    }

    // 檢查場地是否可用
    public boolean isAvailable() {
        return this.status == VenueStatus.ACTIVE &&
                !this.isDeleted;
    }

    // 獲取可用座位數
    public int getAvailableSeats() {
        if (seats == null) {
            return 0;
        }
        return (int) seats.stream()
                .filter(seat -> seat.getStatus() == SeatManagement.SeatStatus.AVAILABLE)
                .count();
    }

    // 軟刪除
    public void softDelete() {
        this.isDeleted = true;
        this.deletedAt = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return String.format("Venue{id=%d, name='%s', status=%s, totalSeats=%d}",
                venueId, venueName, status, totalSeats);
    }
}