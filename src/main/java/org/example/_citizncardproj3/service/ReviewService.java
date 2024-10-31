package org.example._citizncardproj3.service;

import org.example._citizncardproj3.model.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Map;

public interface ReviewService {

    /**
     * 創建評價
     * @param userEmail 用戶郵箱
     * @param movieId 電影ID
     * @param title 標題
     * @param comment 評論內容
     * @param rating 評分
     * @return 評價實體
     */
    Review createReview(String userEmail, Long movieId, String title, String comment, Integer rating);

    /**
     * 更新評價
     * @param userEmail 用戶郵箱
     * @param reviewId 評價ID
     * @param title 標題
     * @param comment 評論內容
     * @param rating 評分
     * @return 更新後的評價
     */
    Review updateReview(String userEmail, Long reviewId, String title, String comment, Integer rating);

    /**
     * 刪除評價
     * @param userEmail 用戶郵箱
     * @param reviewId 評價ID
     */
    void deleteReview(String userEmail, Long reviewId);

    /**
     * 審核評價
     * @param reviewId 評價ID
     * @param verifier 審核人
     * @param approved 是否通過
     * @param comment 審核意見
     * @return 更新後的評價
     */
    Review verifyReview(Long reviewId, String verifier, boolean approved, String comment);

    /**
     * 點讚評價
     * @param reviewId 評價ID
     * @return 更新後的評價
     */
    Review likeReview(Long reviewId);

    /**
     * 獲取電影評價列表
     * @param movieId 電影ID
     * @param pageable 分頁參數
     * @return 評價分頁
     */
    Page<Review> getMovieReviews(Long movieId, Pageable pageable);

    /**
     * 獲取會員評價列表
     * @param userEmail 用戶郵箱
     * @param pageable 分頁參數
     * @return 評價分頁
     */
    Page<Review> getMemberReviews(String userEmail, Pageable pageable);

    /**
     * 獲取待審核評價列表
     * @param pageable 分頁參數
     * @return 評價分頁
     */
    Page<Review> getPendingReviews(Pageable pageable);

    /**
     * 獲取電影平均評分
     * @param movieId 電影ID
     * @return 平均評分
     */
    Double getMovieAverageRating(Long movieId);

    /**
     * 隱藏不受歡迎的評價
     * @param threshold 點讚數閾值
     */
    void hideUnpopularReviews(int threshold);

    /**
     * 檢查評價是否可編輯
     * @param reviewId 評價ID
     * @param userEmail 用戶郵箱
     * @return 是否可編輯
     */
    boolean isReviewEditable(Long reviewId, String userEmail);

    /**
     * 獲取評價統計信息
     * @param movieId 電影ID
     * @return 統計信息
     */
    Map<String, Object> getReviewStatistics(Long movieId);

    /**
     * 檢查用戶是否已評價過電影
     * @param userEmail 用戶郵箱
     * @param movieId 電影ID
     * @return 是否已評價
     */
    boolean hasUserReviewed(String userEmail, Long movieId);
}