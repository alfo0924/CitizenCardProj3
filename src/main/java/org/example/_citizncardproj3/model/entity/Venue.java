package org.example._citizncardproj3.model.entity;

import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "venues")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Venue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long venueId;

    @Column(nullable = false)
    private String venueName;

    @Column(nullable = false)
    private String address;

    @Column(nullable = false)
    private Integer totalSeats;

    @Column(nullable = false)
    private Integer totalRows;

    @Column(nullable = false)
    private Integer totalColumns;

    @Type(type = "json")
    @Column(columnDefinition = "json")
    private String seatingLayout;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VenueStatus status;

    @OneToMany(mappedBy = "venue", cascade = CascadeType.ALL)
    private List<MovieSchedule> schedules;

    @OneToMany(mappedBy = "venue", cascade = CascadeType.ALL)
    private List<SeatManagement> seats;

    private String facilities;

    private String maintenanceSchedule;

    private LocalDateTime lastMaintenanceDate;

    private LocalDateTime nextMaintenanceDate;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Column(nullable = false)
    private Boolean isDeleted;

    // 場地狀態枚舉
    @Getter
    public enum VenueStatus {
        ACTIVE("營業中"),
        MAINTENANCE("維護中"),
        CLOSED("已關閉"),
        RESERVED("已預約");

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
        // 根據行數和列數生成默認座位布局的JSON字符串
        StringBuilder layout = new StringBuilder();
        layout.append("{\"rows\":[");
        for (int i = 0; i < totalRows; i++) {
            layout.append("{\"row\":\"").append((char)('A' + i)).append("\",\"seats\":[");
            for (int j = 0; j < totalColumns; j++) {
                layout.append("{\"number\":\"").append(j + 1)
                        .append("\",\"type\":\"REGULAR\",\"status\":\"AVAILABLE\"}");
                if (j < totalColumns - 1) layout.append(",");
            }
            layout.append("]}");
            if (i < totalRows - 1) layout.append(",");
        }
        layout.append("]}");
        return layout.toString();
    }

    // 業務方法

    // 設置維護狀態
    public void setMaintenance(LocalDateTime maintenanceDate) {
        if (hasActiveSchedules()) {
            throw new IllegalStateException("場地有未完成的場次，無法進行維護");
        }
        this.status = VenueStatus.MAINTENANCE;
        this.lastMaintenanceDate = LocalDateTime.now();
        this.nextMaintenanceDate = maintenanceDate;
    }

    // 完成維護
    public void completeMaintenance() {
        if (this.status == VenueStatus.MAINTENANCE) {
            this.status = VenueStatus.ACTIVE;
            this.lastMaintenanceDate = LocalDateTime.now();
            // 設置下次維護時間（例如：3個月後）
            this.nextMaintenanceDate = LocalDateTime.now().plusMonths(3);
        }
    }

    // 關閉場地
    public void close() {
        if (hasActiveSchedules()) {
            throw new IllegalStateException("場地有未完成的場次，無法關閉");
        }
        this.status = VenueStatus.CLOSED;
    }

    // 重新開放場地
    public void reopen() {
        if (this.status == VenueStatus.CLOSED) {
            this.status = VenueStatus.ACTIVE;
        }
    }

    // 更新座位布局
    public void updateSeatingLayout(String newLayout) {
        if (hasActiveSchedules()) {
            throw new IllegalStateException("場地有未完成的場次，無法更新座位布局");
        }
        this.seatingLayout = newLayout;
        this.totalSeats = calculateTotalSeats();
    }

    // 計算總座位數
    private Integer calculateTotalSeats() {
        return totalRows * totalColumns;
    }

    // 檢查是否有進行中的場次
    public boolean hasActiveSchedules() {
        if (schedules == null || schedules.isEmpty()) {
            return false;
        }
        LocalDateTime now = LocalDateTime.now();
        return schedules.stream()
                .anyMatch(schedule ->
                        schedule.getShowTime().isAfter(now) &&
                                schedule.getStatus() != MovieSchedule.ScheduleStatus.CANCELLED);
    }

    // 檢查場地是否可用
    public boolean isAvailable() {
        return this.status == VenueStatus.ACTIVE &&
                !this.isDeleted;
    }

    // 檢查是否需要維護
    public boolean needsMaintenance() {
        if (this.nextMaintenanceDate == null) {
            return false;
        }
        return LocalDateTime.now().isAfter(this.nextMaintenanceDate);
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

    // 用於日誌記錄的方法
    public String toLogString() {
        return String.format("Venue{id=%d, name='%s', status=%s, totalSeats=%d}",
                venueId, venueName, status, totalSeats);
    }

    // getter and setter
    @Setter
    @Getter
    @Column(name = "maintenance_notes", length = 500)
    private String maintenanceNotes;

}