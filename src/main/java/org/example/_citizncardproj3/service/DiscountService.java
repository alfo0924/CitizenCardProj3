package org.example._citizncardproj3.service;

import org.example._citizncardproj3.model.dto.request.DiscountCreateRequest;
import org.example._citizncardproj3.model.dto.request.DiscountUpdateRequest;
import org.example._citizncardproj3.model.dto.response.DiscountResponse;
import org.example._citizncardproj3.model.entity.Discount;
import org.example._citizncardproj3.model.entity.DiscountUsage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface DiscountService {

    /**
     * 獲取所有優惠
     * @param activeOnly 是否只顯示有效優惠
     * @param pageable 分頁參數
     * @return 優惠列表分頁
     */
    Page<Discount> getAllDiscounts(boolean activeOnly, Pageable pageable);

    /**
     * 獲取會員可用的優惠
     * @param userEmail 用戶郵箱
     * @return 可用優惠列表
     */
    List<Discount> getValidDiscounts(String userEmail);

    /**
     * 創建優惠
     * @param request 創建請求
     * @return 創建的優惠
     */
    Discount createDiscount(DiscountCreateRequest request);

    /**
     * 更新優惠
     * @param discountId 優惠ID
     * @param request 更新請求
     * @return 更新後的優惠
     */
    Discount updateDiscount(Long discountId, DiscountUpdateRequest request);

    /**
     * 停用優惠
     * @param discountId 優惠ID
     */
    void deactivateDiscount(Long discountId);

    /**
     * 使用優惠
     * @param userEmail 用戶郵箱
     * @param discountCode 優惠碼
     * @param originalAmount 原始金額
     * @return 優惠使用記錄
     */
    DiscountUsage useDiscount(String userEmail, String discountCode, Double originalAmount);

    /**
     * 取消優惠使用
     * @param usageId 使用記錄ID
     */
    void cancelDiscountUsage(Long usageId);

    /**
     * 檢查優惠是否可用
     * @param discountCode 優惠碼
     * @param userEmail 用戶郵箱
     * @return 是否可用
     */
    boolean isDiscountAvailable(String discountCode, String userEmail);

    /**
     * 計算優惠金額
     * @param discountCode 優惠碼
     * @param originalAmount 原始金額
     * @return 優惠金額
     */
    Double calculateDiscountAmount(String discountCode, Double originalAmount);

    /**
     * 獲取優惠使用記錄
     * @param userEmail 用戶郵箱
     * @param pageable 分頁參數
     * @return 使用記錄分頁
     */
    Page<DiscountUsage> getDiscountUsageHistory(String userEmail, Pageable pageable);

    /**
     * 檢查優惠使用次數
     * @param userEmail 用戶郵箱
     * @param discountCode 優惠碼
     * @return 已使用次數
     */
    long getDiscountUsageCount(String userEmail, String discountCode);

    /**
     * 延長優惠有效期
     * @param discountId 優惠ID
     * @param days 延長天數
     * @return 更新後的優惠
     */
    Discount extendValidity(Long discountId, int days);

    /**
     * 批量停用過期優惠
     */
    void deactivateExpiredDiscounts();

    /**
     * 獲取優惠使用統計
     * @param discountId 優惠ID
     * @return 使用統計資訊
     */
    Map<String, Object> getDiscountStatistics(Long discountId);

    /**
     * 檢查優惠碼是否有效
     * @param discountCode 優惠碼
     * @return 是否有效
     */
    boolean validateDiscountCode(String discountCode);

    /**
     * 獲取優惠詳情
     */
    DiscountResponse getDiscount(Long discountId);

    /**
     * 獲取公開優惠列表
     */
    Page<DiscountResponse> getPublicDiscounts(Pageable pageable);

    /**
     * 獲取用戶可用優惠
     */
    List<DiscountResponse> getAvailableDiscounts(String userEmail);

    /**
     * 刪除優惠
     */
    void deleteDiscount(Long discountId);

    /**
     * 使用優惠
     */
    void useDiscount(Long discountId, Long orderId, String userEmail);

    /**
     * 驗證優惠
     */
    boolean validateDiscount(String discountCode);

    /**
     * 獲取優惠統計（按時間範圍）
     */
    Map<String, Object> getDiscountStatistics(LocalDateTime startDate, LocalDateTime endDate);

}