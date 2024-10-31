package org.example._citizncardproj3.repository;

import org.example._citizncardproj3.model.entity.CityMovie;
import org.example._citizncardproj3.model.entity.Member;
import org.example._citizncardproj3.model.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    // 基本查詢方法
    List<Review> findByMember(Member member);

    List<Review> findByMovie(CityMovie movie);

    List<Review> findByStatus(Review.ReviewStatus status);

    // 分頁查詢
    Page<Review> findByMovieAndStatusOrderByReviewTimeDesc(
            CityMovie movie,
            Review.ReviewStatus status,
            Pageable pageable
    );

    Page<Review> findByMemberAndStatusOrderByReviewTimeDesc(
            Member member,
            Review.ReviewStatus status,
            Pageable pageable
    );

    // 複雜條件查詢
    @Query("SELECT r FROM Review r WHERE r.movie = :movie " +
            "AND r.status = 'APPROVED' AND r.isPublic = true " +
            "ORDER BY r.likeCount DESC")
    List<Review> findTopReviews(
            @Param("movie") CityMovie movie,
            Pageable pageable
    );

    // 查詢平均評分
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.movie = :movie " +
            "AND r.status = 'APPROVED'")
    Double calculateAverageRating(@Param("movie") CityMovie movie);

    // 統計查詢
    @Query("SELECT r.rating, COUNT(r) FROM Review r WHERE r.movie = :movie " +
            "AND r.status = 'APPROVED' GROUP BY r.rating")
    List<Object[]> countByRating(@Param("movie") CityMovie movie);

    @Query("SELECT COUNT(r) FROM Review r WHERE r.movie = :movie " +
            "AND r.status = 'APPROVED'")
    long countApprovedReviews(@Param("movie") CityMovie movie);

    // 更新操作
    @Modifying
    @Query("UPDATE Review r SET r.status = :newStatus, " +
            "r.verifiedTime = CURRENT_TIMESTAMP, r.verifiedBy = :verifier " +
            "WHERE r.reviewId = :reviewId")
    int updateReviewStatus(
            @Param("reviewId") Long reviewId,
            @Param("newStatus") Review.ReviewStatus newStatus,
            @Param("verifier") String verifier
    );

    @Modifying
    @Query("UPDATE Review r SET r.likeCount = r.likeCount + 1 " +
            "WHERE r.reviewId = :reviewId")
    int incrementLikeCount(@Param("reviewId") Long reviewId);

    // 批量操作
    @Modifying
    @Query("UPDATE Review r SET r.status = 'HIDDEN' " +
            "WHERE r.likeCount < :threshold AND r.status = 'APPROVED'")
    int hideUnpopularReviews(@Param("threshold") int threshold);

    // 使用悲觀鎖查詢
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM Review r WHERE r.reviewId = :reviewId")
    Optional<Review> findByIdWithLock(@Param("reviewId") Long reviewId);

    // 查詢特定評分的評價
    List<Review> findByMovieAndRatingAndStatusOrderByReviewTimeDesc(
            CityMovie movie,
            Integer rating,
            Review.ReviewStatus status
    );

    // 查詢最近的評價
    @Query("SELECT r FROM Review r WHERE r.movie = :movie " +
            "AND r.status = 'APPROVED' AND r.isPublic = true " +
            "ORDER BY r.reviewTime DESC")
    List<Review> findRecentReviews(
            @Param("movie") CityMovie movie,
            Pageable pageable
    );

    // 查詢需要審核的評價
    @Query("SELECT r FROM Review r WHERE r.status = 'PENDING' " +
            "ORDER BY r.createdAt ASC")
    List<Review> findReviewsNeedingVerification();

    // 查詢特定時間範圍的評價
    @Query("SELECT r FROM Review r WHERE r.movie = :movie " +
            "AND r.reviewTime BETWEEN :startTime AND :endTime " +
            "AND r.status = 'APPROVED'")
    List<Review> findReviewsInTimeRange(
            @Param("movie") CityMovie movie,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    // 查詢匿名評價
    List<Review> findByMovieAndIsAnonymousAndStatusOrderByReviewTimeDesc(
            CityMovie movie,
            Boolean isAnonymous,
            Review.ReviewStatus status
    );

    // 軟刪除
    @Modifying
    @Query("UPDATE Review r SET r.isDeleted = true WHERE r.reviewId = :reviewId")
    int softDeleteReview(@Param("reviewId") Long reviewId);
}