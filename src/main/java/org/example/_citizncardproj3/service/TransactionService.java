package org.example._citizncardproj3.service;

import org.example._citizncardproj3.model.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

public interface TransactionService {

    /**
     * 創建交易
     * @param walletId 錢包ID
     * @param amount 金額
     * @param type 交易類型
     * @param paymentMethod 支付方式
     * @return 交易實體
     */
    Transaction createTransaction(
            Long walletId,
            Double amount,
            Transaction.TransactionType type,
            Transaction.PaymentMethod paymentMethod
    );

    /**
     * 完成交易
     * @param transactionId 交易ID
     * @return 更新後的交易
     */
    Transaction completeTransaction(Long transactionId);

    /**
     * 退款交易
     * @param transactionId 交易ID
     * @param reason 退款原因
     * @return 退款交易實體
     */
    Transaction refundTransaction(Long transactionId, String reason);

    /**
     * 獲取錢包交易記錄
     * @param walletId 錢包ID
     * @param pageable 分頁參數
     * @return 交易記錄分頁
     */
    Page<Transaction> getWalletTransactions(Long walletId, Pageable pageable);

    /**
     * 獲取交易統計
     * @param walletId 錢包ID
     * @return 統計資訊
     */
    List<Object[]> getTransactionStatistics(Long walletId);

    /**
     * 驗證交易
     * @param transactionId 交易ID
     * @return 是否有效
     */
    boolean validateTransaction(Long transactionId);

    /**
     * 取消交易
     * @param transactionId 交易ID
     * @param reason 取消原因
     */
    void cancelTransaction(Long transactionId, String reason);

    /**
     * 檢查交易狀態
     * @param transactionId 交易ID
     * @return 交易狀態
     */
    Transaction.TransactionStatus checkTransactionStatus(Long transactionId);

    /**
     * 獲取待處理交易
     * @return 待處理交易列表
     */
    List<Transaction> getPendingTransactions();

    /**
     * 處理超時交易
     */
    void handleTimeoutTransactions();

    /**
     * 獲取交易詳情
     * @param transactionNumber 交易編號
     * @return 交易實體
     */
    Transaction getTransactionByNumber(String transactionNumber);

    /**
     * 獲取特定類型的交易
     * @param walletId 錢包ID
     * @param type 交易類型
     * @param pageable 分頁參數
     * @return 交易記錄分頁
     */
    Page<Transaction> getTransactionsByType(
            Long walletId,
            Transaction.TransactionType type,
            Pageable pageable
    );

    /**
     * 檢查交易限額
     * @param walletId 錢包ID
     * @param amount 交易金額
     * @return 是否超出限額
     */
    boolean checkTransactionLimit(Long walletId, Double amount);

    /**
     * 獲取每日交易統計
     * @param walletId 錢包ID
     * @return 統計資訊
     */
    Map<String, Object> getDailyTransactionStats(Long walletId);
}