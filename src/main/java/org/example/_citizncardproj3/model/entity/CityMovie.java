package org.example._citizncardproj3.model.entity;

import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "CityMovies")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CityMovie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MovieID")
    private Long movieId;

    @Column(name = "MovieCode", unique = true)
    private String movieCode;

    @Column(name = "MovieName", nullable = false)
    private String movieName;

    @Column(name = "Description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "Duration", nullable = false)
    private Integer duration;

    @Column(name = "ReleaseDate", nullable = false)
    private LocalDate releaseDate;

    @Column(name = "EndDate", nullable = false)
    private LocalDate endDate;

    @Column(name = "Language", nullable = false)
    private String language;

    @Column(name = "Subtitle")
    private String subtitle;

    @Column(name = "Director", nullable = false)
    private String director;

    @Column(name = "Cast", columnDefinition = "TEXT")
    private String cast;

    @Column(name = "PosterUrl")
    private String posterUrl;

    @Column(name = "TrailerUrl")
    private String trailerUrl;

    @Column(name = "Rating")
    private String rating;

    @Column(name = "CategoryID")
    private Integer categoryId;

    @Enumerated(EnumType.STRING)
    @Column(name = "Status", nullable = false)
    private MovieStatus status;

    @Column(name = "MinAge")
    private Integer minAge;

    @CreationTimestamp
    @Column(name = "CreatedAt", nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "UpdatedAt", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "IsDeleted", nullable = false)
    private Boolean isDeleted;

    @Column(name = "DeletedAt")
    private LocalDateTime deletedAt;

    @OneToMany(mappedBy = "movie", cascade = CascadeType.ALL)
    private List<MovieSchedule> schedules;

    @OneToMany(mappedBy = "movie")
    private List<Booking> bookings;

    // 電影狀態枚舉
    @Getter
    public enum MovieStatus {
        COMING_SOON("即將上映"),
        NOW_SHOWING("熱映中"),
        END_SHOWING("已下檔");

        private final String description;

        MovieStatus(String description) {
            this.description = description;
        }

    }

    // 初始化方法
    @PrePersist
    public void prePersist() {
        if (this.isDeleted == null) {
            this.isDeleted = false;
        }
        if (this.movieCode == null) {
            this.movieCode = generateMovieCode();
        }
        if (this.minAge == null) {
            this.minAge = 0;
        }
        updateStatus();
    }

    // 生成電影代碼
    private String generateMovieCode() {
        return "MV" + System.currentTimeMillis();
    }

    // 業務方法

    // 更新電影狀態
    public void updateStatus() {
        LocalDate now = LocalDate.now();
        if (now.isBefore(releaseDate)) {
            this.status = MovieStatus.COMING_SOON;
        } else if (now.isAfter(endDate)) {
            this.status = MovieStatus.END_SHOWING;
        } else {
            this.status = MovieStatus.NOW_SHOWING;
        }
    }

    // 新增場次
    public void addSchedule(MovieSchedule schedule) {
        if (this.status != MovieStatus.NOW_SHOWING) {
            throw new IllegalStateException("只有正在上映的電影可以新增場次");
        }
        if (schedule.getShowTime().toLocalDate().isAfter(this.endDate)) {
            throw new IllegalArgumentException("場次時間不能超過電影下檔日期");
        }
        this.schedules.add(schedule);
        schedule.setMovie(this);
    }

    // 取消場次
    public void cancelSchedule(MovieSchedule schedule) {
        if (schedule.hasBookings()) {
            throw new IllegalStateException("已有訂票的場次無法取消");
        }
        this.schedules.remove(schedule);
    }

    // 更新updateInfo方法，移除basePrice參數
    public void updateInfo(
            @Size(max = 100, message = "電影名稱長度不能超過100個字元")
            String movieName,
            @Size(max = 1000, message = "電影描述長度不能超過1000個字元")
            String description) {
        if (this.status == MovieStatus.END_SHOWING) {
            throw new IllegalStateException("已下檔的電影無法更新資訊");
        }
        this.movieName = movieName;
        this.description = description;
    }

    // 延長上映期間
    public void extendShowingPeriod(LocalDate newEndDate) {
        if (newEndDate.isBefore(this.endDate)) {
            throw new IllegalArgumentException("新的下檔日期必須晚於原下檔日期");
        }
        this.endDate = newEndDate;
        updateStatus();
    }

    // 檢查是否可以訂票
    public boolean isBookable() {
        return this.status == MovieStatus.NOW_SHOWING &&
                !this.isDeleted &&
                hasAvailableSchedules();
    }

    // 檢查是否有可用場次
    private boolean hasAvailableSchedules() {
        if (this.schedules == null || this.schedules.isEmpty()) {
            return false;
        }
        return this.schedules.stream()
                .anyMatch(schedule ->
                        schedule.getStatus() == MovieSchedule.ScheduleStatus.ON_SALE &&
                                schedule.getAvailableSeats() > 0
                );
    }

    // 軟刪除
    public void softDelete() {
        this.isDeleted = true;
        this.deletedAt = LocalDateTime.now();
    }

    // 用於日誌記錄的方法
    @Override
    public String toString() {
        return String.format("CityMovie{id=%d, code='%s', name='%s', status=%s}",
                movieId, movieCode, movieName, status);
    }

    public boolean hasActiveSchedules() {
        if (this.schedules == null || this.schedules.isEmpty()) {
            return false;
        }
        return this.schedules.stream()
                .anyMatch(schedule -> schedule.getStatus() != MovieSchedule.ScheduleStatus.ENDED &&
                        schedule.getStatus() != MovieSchedule.ScheduleStatus.CANCELLED);
    }

}
