package org.example._citizncardproj3.service;

import org.example._citizncardproj3.model.entity.CitizenCard;
import java.util.List;
import java.util.Map;

public interface CitizenCardService {

    /**
     * 創建市民卡
     * @param userEmail 用戶郵箱
     * @param cardType 卡片類型
     * @return 創建的市民卡
     */
    CitizenCard createCard(String userEmail, CitizenCard.CardType cardType);

    /**
     * 啟用市民卡
     * @param cardNumber 卡號
     * @return 啟用後的市民卡
     */
    CitizenCard activateCard(String cardNumber);

    /**
     * 停用市民卡
     * @param cardNumber 卡號
     * @param reason 停用原因
     * @return 停用後的市民卡
     */
    CitizenCard suspendCard(String cardNumber, String reason);

    /**
     * 續期市民卡
     * @param cardNumber 卡號
     * @return 續期後的市民卡
     */
    CitizenCard renewCard(String cardNumber);

    /**
     * 獲取會員的所有市民卡
     * @param userEmail 用戶郵箱
     * @return 市民卡列表
     */
    List<CitizenCard> getMemberCards(String userEmail);

    /**
     * 驗證市民卡有效性
     * @param cardNumber 卡號
     * @return 是否有效
     */
    boolean validateCard(String cardNumber);

    /**
     * 檢查市民卡是否需要續期
     * @param cardNumber 卡號
     * @return 是否需要續期
     */
    boolean needsRenewal(String cardNumber);

    /**
     * 更新市民卡持有人資訊
     * @param cardNumber 卡號
     * @param holderName 持有人姓名
     * @return 更新後的市民卡
     */
    CitizenCard updateHolderInfo(String cardNumber, String holderName);

    /**
     * 申請補發市民卡
     * @param userEmail 用戶郵箱
     * @param oldCardNumber 舊卡號
     * @return 新的市民卡
     */
    CitizenCard reissueCard(String userEmail, String oldCardNumber);

    /**
     * 檢查市民卡權限
     * @param cardNumber 卡號
     * @param privilege 權限類型
     * @return 是否有權限
     */
    boolean checkPrivilege(String cardNumber, String privilege);

    /**
     * 更新市民卡狀態
     * @param cardNumber 卡號
     * @param status 新狀態
     * @return 更新後的市民卡
     */
    CitizenCard updateCardStatus(String cardNumber, CitizenCard.CardStatus status);

    /**
     * 記錄市民卡使用歷史
     * @param cardNumber 卡號
     * @param usage 使用記錄
     */
    void recordCardUsage(String cardNumber, String usage);

    /**
     * 檢查並處理過期的市民卡
     */
    void handleExpiredCards();

    /**
     * 驗證市民卡申請資格
     * @param userEmail 用戶郵箱
     * @param cardType 卡片類型
     * @return 是否符合資格
     */
    boolean validateCardApplicationEligibility(String userEmail, CitizenCard.CardType cardType);

    /**
     * 獲取市民卡使用統計
     * @param cardNumber 卡號
     * @return 使用統計資訊
     */
    Map<String, Object> getCardUsageStatistics(String cardNumber);
}