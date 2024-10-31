package org.example._citizncardproj3.repository;

import org.example._citizncardproj3.model.entity.Transaction;
import org.example._citizncardproj3.model.entity.Wallet;
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
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    // 基本查詢方法
    Optional<Transaction> findByTransactionNumber(String transactionNumber);

    List<Transaction> findByWallet(Wallet wallet);

    List<Transaction> findByType(Transaction.TransactionType type);

    List<Transaction> findByStatus(Transaction.TransactionStatus status);

    // 分頁查詢
    Page<Transaction> findByWalletOrderByTransactionTimeDesc(
            Wallet wallet,
            Pageable pageable
    );

    Page<Transaction> findByTypeAndStatusOrderByTransactionTimeDesc(
            Transaction.TransactionType type,
            Transaction.TransactionStatus status,
            Pageable pageable
    );

    // 複雜條件查詢
    @Query("SELECT t FROM Transaction t WHERE t.wallet = :wallet " +
            "AND t.transactionTime BETWEEN :startTime AND :endTime " +
            "ORDER BY t.transactionTime DESC")
    List<Transaction> findTransactionHistory(
            @Param("wallet") Wallet wallet,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    // 查詢待處理交易
    @Query("SELECT t FROM Transaction t WHERE t.status = 'PENDING' " +
            "AND t.transactionTime <= :timeout")
    List<Transaction> findPendingTransactions(
            @Param("timeout") LocalDateTime timeout
    );

    // 統計查詢
    @Query("SELECT t.type, COUNT(t), SUM(t.amount) FROM Transaction t " +
            "WHERE t.wallet = :wallet AND t.status = 'COMPLETED' " +
            "GROUP BY t.type")
    List<Object[]> getTransactionStatistics(@Param("wallet") Wallet wallet);

    @Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.wallet = :wallet " +
            "AND t.type = :type AND t.status = 'COMPLETED'")
    Double calculateTotalAmount(
            @Param("wallet") Wallet wallet,
            @Param("type") Transaction.TransactionType type
    );

    // 更新操作
    @Modifying
    @Query("UPDATE Transaction t SET t.status = :newStatus, " +
            "t.completionTime = CURRENT_TIMESTAMP WHERE t.transactionId = :transactionId")
    int updateTransactionStatus(
            @Param("transactionId") Long transactionId,
            @Param("newStatus") Transaction.TransactionStatus newStatus
    );

    // 批量操作
    @Modifying
    @Query("UPDATE Transaction t SET t.status = 'FAILED' " +
            "WHERE t.status = 'PENDING' AND t.transactionTime < :timeout")
    int cancelTimeoutTransactions(@Param("timeout") LocalDateTime timeout);

    // 使用悲觀鎖查詢
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT t FROM Transaction t WHERE t.transactionId = :transactionId")
    Optional<Transaction> findByIdWithLock(@Param("transactionId") Long transactionId);

    // 查詢特定金額範圍的交易
    List<Transaction> findByAmountBetweenAndStatus(
            Double minAmount,
            Double maxAmount,
            Transaction.TransactionStatus status
    );

    // 查詢特定支付方式的交易
    List<Transaction> findByPaymentMethodAndStatus(
            Transaction.PaymentMethod paymentMethod,
            Transaction.TransactionStatus status
    );

    // 查詢關聯訂單的交易
    @Query("SELECT t FROM Transaction t WHERE t.relatedBooking.bookingId = :bookingId")
    Optional<Transaction> findByBookingId(@Param("bookingId") Long bookingId);

    // 查詢每日交易統計
    @Query("SELECT DATE(t.transactionTime), COUNT(t), SUM(t.amount) " +
            "FROM Transaction t WHERE t.wallet = :wallet " +
            "AND t.status = 'COMPLETED' " +
            "GROUP BY DATE(t.transactionTime)")
    List<Object[]> getDailyTransactionStats(@Param("wallet") Wallet wallet);

    // 查詢可退款的交易
    @Query("SELECT t FROM Transaction t WHERE t.status = 'COMPLETED' " +
            "AND t.type = 'PAYMENT' AND t.transactionTime > :refundableTime")
    List<Transaction> findRefundableTransactions(
            @Param("refundableTime") LocalDateTime refundableTime
    );

    // 軟刪除
    @Modifying
    @Query("UPDATE Transaction t SET t.isDeleted = true " +
            "WHERE t.transactionId = :transactionId")
    int softDeleteTransaction(@Param("transactionId") Long transactionId);
}