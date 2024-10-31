package org.example._citizncardproj3.service;

import org.example._citizncardproj3.model.entity.Transaction;
import org.example._citizncardproj3.model.entity.Wallet;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Map;

public interface WalletService {

    /**
     * 創建錢包
     * @param userEmail 用戶郵箱
     * @param walletType 錢包類型
     * @return 創建的錢包
     */
    Wallet createWallet(String userEmail, Wallet.WalletType walletType);

    /**
     * 儲值
     * @param userEmail 用戶郵箱
     * @param amount 金額
     * @param paymentMethod 支付方式
     * @return 更新後的錢包
     */
    Wallet topUp(String userEmail, Double amount, Transaction.PaymentMethod paymentMethod);

    /**
     * 支付
     * @param userEmail 用戶郵箱
     * @param amount 金額
     * @return 更新後的錢包
     */
    Wallet pay(String userEmail, Double amount);

    /**
     * 獲取餘額
     * @param userEmail 用戶郵箱
     * @return 餘額
     */
    Double getBalance(String userEmail);

    /**
     * 獲取交易記錄
     * @param userEmail 用戶郵箱
     * @param pageable 分頁參數
     * @return 交易記錄分頁
     */
    Page<Transaction> getTransactionHistory(String userEmail, Pageable pageable);

    /**
     * 凍結錢包
     * @param userEmail 用戶郵箱
     * @param reason 凍結原因
     */
    void freezeWallet(String userEmail, String reason);

    /**
     * 解凍錢包
     * @param userEmail 用戶郵箱
     */
    void unfreezeWallet(String userEmail);

    /**
     * 重設每日限額
     */
    void resetDailyLimit();

    /**
     * 重設每月限額
     */
    void resetMonthlyLimit();

    /**
     * 檢查錢包狀態
     * @param userEmail 用戶郵箱
     * @return 錢包狀態
     */
    Wallet.WalletStatus checkWalletStatus(String userEmail);

    /**
     * 更新交易限額
     * @param userEmail 用戶郵箱
     * @param dailyLimit 每日限額
     * @param monthlyLimit 每月限額
     * @return 更新後的錢包
     */
    Wallet updateTransactionLimits(String userEmail, Double dailyLimit, Double monthlyLimit);

    /**
     * 獲取錢包詳情
     * @param userEmail 用戶郵箱
     * @return 錢包詳情
     */
    Map<String, Object> getWalletDetails(String userEmail);

    /**
     * 檢查交易限額
     * @param userEmail 用戶郵箱
     * @param amount 交易金額
     * @return 是否超出限額
     */
    boolean checkTransactionLimits(String userEmail, Double amount);

    /**
     * 獲取點數餘額
     * @param userEmail 用戶郵箱
     * @return 點數餘額
     */
    Integer getPointsBalance(String userEmail);

    /**
     * 增加點數
     * @param userEmail 用戶郵箱
     * @param points 點數
     * @return 更新後的錢包
     */
    Wallet addPoints(String userEmail, Integer points);

    /**
     * 使用點數
     * @param userEmail 用戶郵箱
     * @param points 點數
     * @return 更新後的錢包
     */
    Wallet usePoints(String userEmail, Integer points);
}