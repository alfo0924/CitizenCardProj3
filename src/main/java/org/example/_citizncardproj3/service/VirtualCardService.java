package org.example._citizncardproj3.service;

import org.example._citizncardproj3.model.entity.VirtualCard;
import org.springframework.data.domain.Pageable;

import java.util.Map;

public interface VirtualCardService {

    /**
     * 創建虛擬卡
     * @param userEmail 用戶郵箱
     * @param phoneNumber 手機號碼
     * @return 創建的虛擬卡
     */
    VirtualCard createVirtualCard(String userEmail, String phoneNumber);

    /**
     * 綁定設備
     * @param userEmail 用戶郵箱
     * @param deviceId 設備ID
     * @param deviceName 設備名稱
     * @return 更新後的虛擬卡
     */
    VirtualCard bindDevice(String userEmail, String deviceId, String deviceName);

    /**
     * 解綁設備
     * @param userEmail 用戶郵箱
     * @param reason 解綁原因
     * @return 更新後的虛擬卡
     */
    VirtualCard unbindDevice(String userEmail, String reason);

    /**
     * 啟用虛擬卡
     * @param userEmail 用戶郵箱
     * @return 更新後的虛擬卡
     */
    VirtualCard activateCard(String userEmail);

    /**
     * 停用虛擬卡
     * @param userEmail 用戶郵箱
     * @param reason 停用原因
     * @return 更新後的虛擬卡
     */
    VirtualCard suspendCard(String userEmail, String reason);

    /**
     * 獲取虛擬卡資訊
     * @param userEmail 用戶郵箱
     * @return 虛擬卡實體
     */
    VirtualCard getVirtualCard(String userEmail);

    /**
     * 更新最後使用時間
     * @param userEmail 用戶郵箱
     */
    void updateLastUsedTime(String userEmail);

    /**
     * 停用未使用的卡片
     */
    void deactivateUnusedCards();

    /**
     * 驗證設備
     * @param userEmail 用戶郵箱
     * @param deviceId 設備ID
     * @return 是否有效
     */
    boolean validateDevice(String userEmail, String deviceId);

    /**
     * 檢查虛擬卡狀態
     * @param userEmail 用戶郵箱
     * @return 卡片狀態
     */
    VirtualCard.CardStatus checkCardStatus(String userEmail);

    /**
     * 更新綁定手機號碼
     * @param userEmail 用戶郵箱
     * @param newPhoneNumber 新手機號碼
     * @return 更新後的虛擬卡
     */
    VirtualCard updatePhoneNumber(String userEmail, String newPhoneNumber);

    /**
     * 獲取虛擬卡使用記錄
     * @param userEmail 用戶郵箱
     * @param pageable 分頁參數
     * @return 使用記錄分頁
     */
    Page<VirtualCardUsage> getUsageHistory(String userEmail, Pageable pageable);

    /**
     * 檢查設備是否已被綁定
     * @param deviceId 設備ID
     * @return 是否已綁定
     */
    boolean isDeviceRegistered(String deviceId);

    /**
     * 獲取虛擬卡統計資訊
     * @param userEmail 用戶郵箱
     * @return 統計資訊
     */
    Map<String, Object> getCardStatistics(String userEmail);
}