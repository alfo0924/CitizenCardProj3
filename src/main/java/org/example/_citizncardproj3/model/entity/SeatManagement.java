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
@Table(name = "seat_management")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeatManagement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long seatId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "venue_id", nullable = false)
    private Venue venue;

    @Column(nullable = false)
    private String seatRow;

    @Column(nullable = false)
    private String seatColumn;

    @Column(nullable = false)
    private String seatLabel;

    @Column(nullable = false)
    private String seatZone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SeatType seatType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SeatStatus status;

    @Column(nullable = false)
    private Boolean isActive;

    @Column(columnDefinition = "TEXT")
    private String maintenanceHistory;

    private LocalDateTime lastMaintenanceDate;

    private LocalDateTime nextMaintenanceDate;

    private String lastMaintenanceBy;

    private String maintenanceNotes;

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
        MAINTENANCE("維護中"),
        DISABLED("停用"),
        RESERVED("保留座");

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
        if (this.isActive == null) {
            this.isActive = true;
        }
        if (this.status == null) {
            this.status = SeatStatus.AVAILABLE;
        }
        if (this.seatLabel == null) {
            this.seatLabel = generateSeatLabel();
        }
    }

    // 生成座位標籤
    private String generateSeatLabel() {
        return this.seatRow + this.seatColumn;
    }

    // 業務方法

    // 設置維護狀態
    public void setMaintenance(String maintainer, String notes) {
        this.status = SeatStatus.MAINTENANCE;
        this.lastMaintenanceDate = LocalDateTime.now();
        this.lastMaintenanceBy = maintainer;
        this.maintenanceNotes = notes;

        // 更新維護歷史
        String maintenanceRecord = String.format(
                "維護時間: %s, 維護人員: %s, 備註: %s\n",
                this.lastMaintenanceDate, maintainer, notes
        );

        if (this.maintenanceHistory == null) {
            this.maintenanceHistory = maintenanceRecord;
        } else {
            this.maintenanceHistory = maintenanceRecord + this.maintenanceHistory;
        }
    }

    // 完成維護
    public void completeMaintenance() {
        if (this.status == SeatStatus.MAINTENANCE) {
            this.status = SeatStatus.AVAILABLE;
            // 設置下次維護時間（例如：3個月後）
            this.nextMaintenanceDate = LocalDateTime.now().plusMonths(3);
        } else {
            throw new IllegalStateException("座位不在維護狀態");
        }
    }

    // 停用座位
    public void disable(String reason) {
        this.status = SeatStatus.DISABLED;
        this.isActive = false;
        this.maintenanceNotes = reason;
    }

    // 啟用座位
    public void enable() {
        this.status = SeatStatus.AVAILABLE;
        this.isActive = true;
    }

    // 設置為保留座
    public void setReserved() {
        this.status = SeatStatus.RESERVED;
    }

    // 更新座位類型
    public void updateSeatType(SeatType newType) {
        this.seatType = newType;
    }

    // 更新座位區域
    public void updateZone(String newZone) {
        this.seatZone = newZone;
    }

    // 檢查是否需要維護
    public boolean needsMaintenance() {
        if (this.nextMaintenanceDate == null) {
            return false;
        }
        return LocalDateTime.now().isAfter(this.nextMaintenanceDate);
    }

    // 檢查座位是否可用
    public boolean isAvailable() {
        return this.status == SeatStatus.AVAILABLE &&
                this.isActive &&
                !this.isDeleted;
    }

    // 檢查是否為特殊座位
    public boolean isSpecialSeat() {
        return this.seatType == SeatType.VIP ||
                this.seatType == SeatType.COUPLE ||
                this.seatType == SeatType.HANDICAP;
    }

    // 用於日誌記錄的方法
    public String toLogString() {
        return String.format("SeatManagement{id=%d, label='%s', type=%s, status=%s}",
                seatId, seatLabel, seatType, status);
    }
}