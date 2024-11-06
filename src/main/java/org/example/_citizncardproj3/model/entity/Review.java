package org.example._citizncardproj3.model.entity;

import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "reviews")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reviewId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MemberId", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MovieID", nullable = false)
    private CityMovie movie;

    @Column(nullable = false)
    private Integer rating;

    @Column(length = 100)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String comment;

    @Column(nullable = false)
    private Integer likeCount;

    @Column(nullable = false)
    private Boolean isVerified;

    @Column(nullable = false)
    private Boolean isPublic;

    @Column(nullable = false)
    private Boolean isAnonymous;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReviewStatus status;

    private String adminComment;

    @Column(nullable = false)
    private LocalDateTime reviewTime;

    private LocalDateTime verifiedTime;

    private String verifiedBy;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Column(nullable = false)
    private Boolean isDeleted;

    // 評價狀態枚舉
    @Getter
    public enum ReviewStatus {
        PENDING("待審核"),
        APPROVED("已通過"),
        REJECTED("已拒絕"),
        HIDDEN("已隱藏");

        private final String description;

        ReviewStatus(String description) {
            this.description = description;
        }

    }

    // 初始化方法
    @PrePersist
    public void prePersist() {
        if (this.isDeleted == null) {
            this.isDeleted = false;
        }
        if (this.isVerified == null) {
            this.isVerified = false;
        }
        if (this.isPublic == null) {
            this.isPublic = true;
        }
        if (this.isAnonymous == null) {
            this.isAnonymous = false;
        }
        if (this.likeCount == null) {
            this.likeCount = 0;
        }
        if (this.status == null) {
            this.status = ReviewStatus.PENDING;
        }
        if (this.reviewTime == null) {
            this.reviewTime = LocalDateTime.now();
        }
        validateRating();
    }

    // 業務方法

    // 驗證評分
    private void validateRating() {
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("評分必須在1到5之間");
        }
    }

    // 審核評價
    public void verify(String adminUser, boolean approved, String comment) {
        this.isVerified = true;
        this.verifiedTime = LocalDateTime.now();
        this.verifiedBy = adminUser;
        this.status = approved ? ReviewStatus.APPROVED : ReviewStatus.REJECTED;
        if (comment != null) {
            this.adminComment = comment;
        }
    }

    // 更新評價內容
    public void updateContent(String title, String comment) {
        if (this.status != ReviewStatus.APPROVED) {
            this.title = title;
            this.comment = comment;
            this.status = ReviewStatus.PENDING;
            this.isVerified = false;
        } else {
            throw new IllegalStateException("已通過的評價不能修改");
        }
    }

    // 更新評分
    public void updateRating(Integer rating) {
        if (rating >= 1 && rating <= 5) {
            this.rating = rating;
        } else {
            throw new IllegalArgumentException("評分必須在1到5之間");
        }
    }

    // 點讚
    public void addLike() {
        this.likeCount++;
    }

    // 取消點讚
    public void removeLike() {
        if (this.likeCount > 0) {
            this.likeCount--;
        }
    }

    // 設置匿名
    public void setAnonymous(boolean anonymous) {
        this.isAnonymous = anonymous;
    }

    // 隱藏評價
    public void hide() {
        this.status = ReviewStatus.HIDDEN;
        this.isPublic = false;
    }

    // 顯示評價
    public void show() {
        if (this.status == ReviewStatus.APPROVED) {
            this.isPublic = true;
        }
    }

    // 檢查是否可以編輯
    public boolean isEditable() {
        return this.status != ReviewStatus.APPROVED &&
                !this.isDeleted;
    }

    // 檢查是否可以顯示
    public boolean isVisible() {
        return this.isPublic &&
                this.status == ReviewStatus.APPROVED &&
                !this.isDeleted;
    }

    // 獲取評價者顯示名稱
    public String getReviewerDisplayName() {
        if (this.isAnonymous) {
            return "匿名用戶";
        }
        return this.member != null ? this.member.getName() : "未知用戶";
    }

    // 用於日誌記錄的方法
    public String toLogString() {
        return String.format("Review{id=%d, movie='%s', rating=%d, status=%s}",
                reviewId, movie.getMovieName(), rating, status);
    }
}