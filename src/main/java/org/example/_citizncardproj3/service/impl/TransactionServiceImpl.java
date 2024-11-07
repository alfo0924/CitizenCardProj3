package org.example._citizncardproj3.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example._citizncardproj3.exception.CustomException;
import org.example._citizncardproj3.model.entity.Transaction;
import org.example._citizncardproj3.model.entity.Wallet;
import org.example._citizncardproj3.repository.TransactionRepository;
import org.example._citizncardproj3.repository.WalletRepository;
import org.example._citizncardproj3.service.NotificationService;
import org.example._citizncardproj3.service.TransactionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final WalletRepository walletRepository;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public Transaction createTransaction(Long walletId, Double amount, Transaction.TransactionType type,
                                         Transaction.PaymentMethod paymentMethod) {

        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new CustomException.WalletNotFoundException(walletId));

        // 驗證錢包狀態
        if (!wallet.isUsable()) {
            throw new IllegalStateException("錢包狀態異常");
        }

        // 檢查交易限額
        validateTransactionLimits(wallet, amount);

        // 檢查餘額（如果是支付交易）
        if (type == Transaction.TransactionType.PAYMENT && !wallet.hasEnoughBalance(amount)) {
            throw new CustomException.InsufficientBalanceException();
        }

        // 創建交易記錄
        Transaction transaction = Transaction.builder()
                .wallet(wallet)
                .amount(amount)
                .type(type)
                .status(Transaction.TransactionStatus.PENDING)
                .paymentMethod(paymentMethod)
                .transactionTime(LocalDateTime.now())
                .build();

        transaction = transactionRepository.save(transaction);

        // 處理交易
        processTransaction(transaction);

        return transaction;
    }

    @Override
    @Transactional
    public Transaction completeTransaction(Long transactionId) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new CustomException.TransactionNotFoundException(transactionId));

        if (transaction.getStatus() != Transaction.TransactionStatus.PENDING) {
            throw new IllegalStateException("交易狀態不正確");
        }

        transaction.setStatus(Transaction.TransactionStatus.SUCCESS);
        transaction.setTransactionTime(LocalDateTime.now());
        transaction = transactionRepository.save(transaction);

        // 發送通知
        notificationService.createPaymentNotification(
                transaction.getWallet().getMember().getEmail(),
                transaction.getAmount(),
                transaction.getTransactionNumber()
        );

        return transaction;
    }

    @Override
    @Transactional
    public Transaction refundTransaction(Long transactionId, String reason) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new CustomException.TransactionNotFoundException(transactionId));

        if (!transaction.isRefundable()) {
            throw new IllegalStateException("此交易無法退款");
        }

        // 創建退款交易
        Transaction refundTransaction = Transaction.builder()
                .wallet(transaction.getWallet())
                .amount(transaction.getAmount())
                .type(Transaction.TransactionType.REFUND)
                .status(Transaction.TransactionStatus.PENDING)
                .paymentMethod(Transaction.PaymentMethod.WALLET_BALANCE)
                .transactionNumber(generateRefundTransactionNumber(transaction.getTransactionNumber()))
                .description("退款-原交易號: " + transaction.getTransactionNumber())
                .transactionTime(LocalDateTime.now())
                .build();

        refundTransaction = transactionRepository.save(refundTransaction);

        // 處理退款
        processTransaction(refundTransaction);

        // 更新原交易狀態
        transaction.setStatus(Transaction.TransactionStatus.CANCELLED);
        transaction.setStatusMessage("已退款: " + reason);
        transactionRepository.save(transaction);

        return refundTransaction;
    }

    // 新增生成退款交易編號的輔助方法
    private String generateRefundTransactionNumber(String originalTransactionNumber) {
        return "RF" + originalTransactionNumber;
    }

    @Override
    public Page<Transaction> getWalletTransactions(Long walletId, Pageable pageable) {
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new CustomException.WalletNotFoundException(walletId));

        return transactionRepository.findByWalletOrderByTransactionTimeDesc(wallet, pageable);
    }

    @Override
    public List<Object[]> getTransactionStatistics(Long walletId) {
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new CustomException.WalletNotFoundException(walletId));

        return transactionRepository.getTransactionStatistics(wallet);
    }

    @Override
    public boolean validateTransaction(Long transactionId) {
        return false;
    }

    @Override
    public void cancelTransaction(Long transactionId, String reason) {

    }

    @Override
    public Transaction.TransactionStatus checkTransactionStatus(Long transactionId) {
        return null;
    }

    @Override
    public List<Transaction> getPendingTransactions() {
        return List.of();
    }

    @Override
    public void handleTimeoutTransactions() {

    }

    @Override
    public Transaction getTransactionByNumber(String transactionNumber) {
        return null;
    }

    @Override
    public Page<Transaction> getTransactionsByType(Long walletId, Transaction.TransactionType type, Pageable pageable) {
        return null;
    }

    @Override
    public boolean checkTransactionLimit(Long walletId, Double amount) {
        return false;
    }

    @Override
    public Map<String, Object> getDailyTransactionStats(Long walletId) {
        return Map.of();
    }

    // 私有輔助方法
    private void processTransaction(Transaction transaction) {
        try {
            Wallet wallet = transaction.getWallet();

            switch (transaction.getType()) {
                case DEPOSIT:  // 修改自 TOP_UP
                    wallet.addBalance(transaction.getAmount());
                    break;
                case PAYMENT, TRANSFER:
                    wallet.subtractBalance(transaction.getAmount());
                    break;
                case REFUND:
                    wallet.addBalance(transaction.getAmount());
                    break;
            }

            walletRepository.save(wallet);
            completeTransaction(transaction.getTransactionId());

        } catch (Exception e) {
            transaction.setStatus(Transaction.TransactionStatus.FAILED);
            transactionRepository.save(transaction);
            throw e;
        }
    }

    private void validateTransactionLimits(Wallet wallet, Double amount) {
        // 檢查每日限額
        if (wallet.getDailyTransactionAmount() + amount > wallet.getDailyTransactionLimit()) {
            throw new IllegalStateException("超過每日交易限額");
        }

        // 檢查每月限額
        if (wallet.getMonthlyTransactionAmount() + amount > wallet.getMonthlyTransactionLimit()) {
            throw new IllegalStateException("超過每月交易限額");
        }
    }
}