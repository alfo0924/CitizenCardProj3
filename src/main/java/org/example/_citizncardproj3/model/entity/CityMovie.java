package org.example._citizncardproj3.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example._citizncardproj3.model.dto.request.MovieCreateRequest;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Table(name = "city_movies")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CityMovie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long movieId;

    @Column(unique = true)
    private String movieCode;

    @Column(nullable = false)
    private String movieName;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private Integer duration; // 片長（分鐘）

    @Column(nullable = false)
    private LocalDate releaseDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Column(nullable = false)
    private String language;

    private String subtitle;

    @Column(nullable = false)
    private String director;

    @ElementCollection
    @CollectionTable(name = "movie_cast")
    private List<String> cast;

    private String posterUrl;

    private String trailerUrl;

    @Column(nullable = false)
    private String rating;

    @ElementCollection
    @CollectionTable(name = "movie_categories")
    private List<String> categories;

    @Column(nullable = false)
    private Double basePrice;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MovieStatus status;

    @OneToMany(mappedBy = "movie", cascade = CascadeType.ALL)
    private List<MovieSchedule> schedules;

    @OneToMany(mappedBy = "movie")
    private List<Booking> bookings;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Column(nullable = false)
    private Boolean isDeleted;

    // 電影狀態枚舉
    public enum MovieStatus {
        COMING_SOON("即將上映"),
        NOW_SHOWING("熱映中"),
        END_SHOWING("已下檔");

        private final String description;

        MovieStatus(String description) {
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
        if (this.movieCode == null) {
            this.movieCode = generateMovieCode();
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

    // 更新電影資訊
    public void updateInfo(String movieName, String description, Double basePrice) {
        if (this.status == MovieStatus.END_SHOWING) {
            throw new IllegalStateException("已下檔的電影無法更新資訊");
        }
        this.movieName = movieName;
        this.description = description;
        this.basePrice = basePrice;
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

    // 獲取可用場次列表
    public List<MovieSchedule> getAvailableSchedules() {
        return this.schedules.stream()
                .filter(schedule ->
                        schedule.getStatus() == MovieSchedule.ScheduleStatus.ON_SALE &&
                                schedule.getAvailableSeats() > 0
                )
                .toList();
    }

    // 計算總收入
    public Double calculateTotalRevenue() {
        return this.bookings.stream()
                .filter(booking -> booking.getPaymentStatus() == Booking.PaymentStatus.PAID)
                .mapToDouble(Booking::getFinalAmount)
                .sum();
    }

    // 用於日誌記錄的方法
    public String toLogString() {
        return String.format("CityMovie{id=%d, code='%s', name='%s', status=%s}",
                movieId, movieCode, movieName, status);
    }


    /**
     * 檢查是否有進行中的場次
     */
    public boolean hasActiveSchedules() {
        if (schedules == null || schedules.isEmpty()) {
            return false;
        }
        return schedules.stream()
                .anyMatch(schedule -> schedule.getStatus() != MovieSchedule.ScheduleStatus.ENDED &&
                        schedule.getStatus() != MovieSchedule.ScheduleStatus.CANCELLED);
    }



    /**
     * 電影類別枚舉
     */
    public enum MovieCategory {
        ACTION("動作片"),
        COMEDY("喜劇片"),
        DRAMA("劇情片"),
        HORROR("恐怖片"),
        ROMANCE("愛情片"),
        SCIFI("科幻片"),
        ANIMATION("動畫片"),
        DOCUMENTARY("紀錄片"),
        OTHER("其他");

        private final String description;

        MovieCategory(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }





    /**
     * 設置電影類別
     */
    public void setCategories(List<MovieCategory> categories) {
        this.categories = categories.stream()
                .map(MovieCategory::name)
                .collect(Collectors.toList());
    }

    /**
     * 獲取電影類別列表
     */
    public List<MovieCategory> getMovieCategories() {
        return this.categories.stream()
                .map(MovieCategory::valueOf)
                .collect(Collectors.toList());
    }


}