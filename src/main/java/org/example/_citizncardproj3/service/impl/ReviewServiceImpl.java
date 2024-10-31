package org.example._citizncardproj3.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example._citizncardproj3.exception.CustomException;
import org.example._citizncardproj3.model.entity.CityMovie;
import org.example._citizncardproj3.model.entity.Member;
import org.example._citizncardproj3.model.entity.Review;
import org.example._citizncardproj3.repository.CityMovieRepository;
import org.example._citizncardproj3.repository.MemberRepository;
import org.example._citizncardproj3.repository.ReviewRepository;
import org.example._citizncardproj3.service.ReviewService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final MemberRepository memberRepository;
    private final CityMovieRepository movieRepository;

    @Override
    @Transactional
    public Review createReview(String userEmail, Long movieId, String title, String comment, Integer rating) {
        // 驗證會員
        Member member = memberRepository.findByEmail(userEmail)
                .orElseThrow(() -> new CustomException.MemberNotFoundException(userEmail));

        // 驗證電影
        CityMovie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new CustomException.MovieNotFoundException(movieId));

        // 驗證評分範圍
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("評分必須在1到5之間");
        }

        // 檢查是否已評價過
        if (reviewRepository.existsByMemberAndMovie(member, movie)) {
            throw new IllegalStateException("已經評價過此電影");
        }

        Review review = Review.builder()
                .member(member)
                .movie(movie)
                .title(title)
                .comment(comment)
                .rating(rating)
                .likeCount(0)
                .isVerified(false)
                .isPublic(true)
                .isAnonymous(false)
                .status(Review.ReviewStatus.PENDING)
                .reviewTime(LocalDateTime.now())
                .build();

        return reviewRepository.save(review);
    }

    @Override
    @Transactional
    public Review updateReview(String userEmail, Long reviewId, String title, String comment, Integer rating) {
        // 驗證會員
        Member member = memberRepository.findByEmail(userEmail)
                .orElseThrow(() -> new CustomException.MemberNotFoundException(userEmail));

        // 驗證評價
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("評價不存在"));

        // 驗證權限
        if (!review.getMember().equals(member)) {
            throw new IllegalStateException("無權限修改此評價");
        }

        // 驗證評價是否可編輯
        if (!review.isEditable()) {
            throw new IllegalStateException("此評價無法修改");
        }

        // 更新評價內容
        review.updateContent(title, comment);
        if (rating != null) {
            review.updateRating(rating);
        }

        return reviewRepository.save(review);
    }

    @Override
    @Transactional
    public void deleteReview(String userEmail, Long reviewId) {
        // 驗證會員
        Member member = memberRepository.findByEmail(userEmail)
                .orElseThrow(() -> new CustomException.MemberNotFoundException(userEmail));

        // 驗證評價
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("評價不存在"));

        // 驗證權限
        if (!review.getMember().equals(member)) {
            throw new IllegalStateException("無權限刪除此評價");
        }

        reviewRepository.softDeleteReview(reviewId);
    }

    @Override
    @Transactional
    public Review verifyReview(Long reviewId, String verifier, boolean approved, String comment) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("評價不存在"));

        review.verify(verifier, approved, comment);
        return reviewRepository.save(review);
    }

    @Override
    @Transactional
    public Review likeReview(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("評價不存在"));

        review.addLike();
        return reviewRepository.save(review);
    }

    @Override
    public Page<Review> getMovieReviews(Long movieId, Pageable pageable) {
        CityMovie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new CustomException.MovieNotFoundException(movieId));

        return reviewRepository.findByMovieAndStatusOrderByReviewTimeDesc(
                movie,
                Review.ReviewStatus.APPROVED,
                pageable
        );
    }

    @Override
    public Page<Review> getMemberReviews(String userEmail, Pageable pageable) {
        Member member = memberRepository.findByEmail(userEmail)
                .orElseThrow(() -> new CustomException.MemberNotFoundException(userEmail));

        return reviewRepository.findByMemberAndStatusOrderByReviewTimeDesc(
                member,
                Review.ReviewStatus.APPROVED,
                pageable
        );
    }

    @Override
    public Page<Review> getPendingReviews(Pageable pageable) {
        return reviewRepository.findByStatusOrderByReviewTimeDesc(
                Review.ReviewStatus.PENDING,
                pageable
        );
    }

    @Override
    public Double getMovieAverageRating(Long movieId) {
        return reviewRepository.calculateAverageRating(movieId);
    }

    @Override
    @Transactional
    public void hideUnpopularReviews(int threshold) {
        reviewRepository.hideUnpopularReviews(threshold);
    }

    @Override
    public boolean isReviewEditable(Long reviewId, String userEmail) {
        return false;
    }

    @Override
    public Map<String, Object> getReviewStatistics(Long movieId) {
        return Map.of();
    }

    @Override
    public boolean hasUserReviewed(String userEmail, Long movieId) {
        return false;
    }
}