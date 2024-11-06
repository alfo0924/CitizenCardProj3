package org.example._citizncardproj3.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example._citizncardproj3.exception.CustomException;
import org.example._citizncardproj3.model.dto.request.TopUpRequest;
import org.example._citizncardproj3.model.dto.request.TransferRequest;
import org.example._citizncardproj3.model.dto.response.TransactionResponse;
import org.example._citizncardproj3.model.dto.response.WalletResponse;
import org.example._citizncardproj3.model.entity.Member;
import org.example._citizncardproj3.model.entity.Transaction;
import org.example._citizncardproj3.model.entity.Wallet;
import org.example._citizncardproj3.repository.MemberRepository;
import org.example._citizncardproj3.repository.TransactionRepository;
import org.example._citizncardproj3.repository.WalletRepository;
import org.example._citizncardproj3.service.NotificationService;
import org.example._citizncardproj3.service.WalletService;
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
public class WalletServiceImpl implements WalletService {

    private final WalletRepository walletRepository;
    private final MemberRepository memberRepository;
    private final TransactionRepository transactionRepository;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public Wallet createWallet(String userEmail, Wallet.WalletType walletType) {
        Member member = memberRepository.findByEmail(userEmail)
                .orElseThrow(() -> new CustomException.MemberNotFoundException(userEmail));

        // 檢查是否已有錢包
        if (walletRepository.findByMember(member).isPresent()) {
            throw new IllegalStateException("會員已有錢包");
        }

        // 創建錢包
        Wallet wallet = Wallet.builder()
                .member(member)
                .balance(0.0)
                .walletType(walletType)
                .status(Wallet.WalletStatus.ACTIVE)
                .pointsBalance(0)
                .dailyTransactionLimit(50000.0)
                .monthlyTransactionLimit(500000.0)
                .dailyTransactionAmount(0.0)
                .monthlyTransactionAmount(0.0)
                .build();

        return walletRepository.save(wallet);
    }

    @Override
    @Transactional
    public Wallet topUp(String userEmail, Double amount, Transaction.PaymentMethod paymentMethod) {
        Member member = memberRepository.findByEmail(userEmail)
                .orElseThrow(() -> new CustomException.MemberNotFoundException(userEmail));

        Wallet wallet = walletRepository.findByMember(member)
                .orElseThrow(() -> new CustomException.WalletNotFoundException(0L));

        // 驗證錢包狀態
        if (!wallet.isUsable()) {
            throw new IllegalStateException("錢包狀態異常");
        }

        // 創建儲值交易
        Transaction transaction = Transaction.builder()
                .wallet(wallet)
                .amount(amount)
                .type(Transaction.TransactionType.TOP_UP)
                .status(Transaction.TransactionStatus.PENDING)
                .paymentMethod(paymentMethod)
                .transactionTime(LocalDateTime.now())
                .build();

        transaction = transactionRepository.save(transaction);

        // 更新錢包餘額
        wallet.addBalance(amount);
        wallet = walletRepository.save(wallet);

        // 完成交易
        transaction.setStatus(Transaction.TransactionStatus.SUCCESS);
        transaction.setCompletionTime(LocalDateTime.now());
        transactionRepository.save(transaction);

        // 發送通知
        notificationService.createPaymentNotification(
                userEmail,
                amount,
                transaction.getTransactionNumber()
        );

        return wallet;
    }

    @Override
    @Transactional
    public Wallet pay(String userEmail, Double amount) {
        Member member = memberRepository.findByEmail(userEmail)
                .orElseThrow(() -> new CustomException.MemberNotFoundException(userEmail));

        Wallet wallet = walletRepository.findByMember(member)
                .orElseThrow(() -> new CustomException.WalletNotFoundException(0L));

        // 驗證錢包狀態和餘額
        if (!wallet.isUsable()) {
            throw new IllegalStateException("錢包狀態異常");
        }

        if (!wallet.hasEnoughBalance(amount)) {
            throw new CustomException.InsufficientBalanceException();
        }

        // 檢查交易限額
        validateTransactionLimits(wallet, amount);

        // 創建支付交易
        Transaction transaction = Transaction.builder()
                .wallet(wallet)
                .amount(amount)
                .type(Transaction.TransactionType.PAYMENT)
                .status(Transaction.TransactionStatus.PENDING)
                .paymentMethod(Transaction.PaymentMethod.WALLET_BALANCE)
                .transactionTime(LocalDateTime.now())
                .build();

        transaction = transactionRepository.save(transaction);

        // 扣除錢包餘額
        wallet.subtractBalance(amount);
        wallet = walletRepository.save(wallet);

        // 完成交易
        transaction.setStatus(Transaction.TransactionStatus.SUCCESS);
        transaction.setCompletionTime(LocalDateTime.now());
        transactionRepository.save(transaction);

        return wallet;
    }

    @Override
    public Double getBalance(String userEmail) {
        Member member = memberRepository.findByEmail(userEmail)
                .orElseThrow(() -> new CustomException.MemberNotFoundException(userEmail));

        Wallet wallet = walletRepository.findByMember(member)
                .orElseThrow(() -> new CustomException.WalletNotFoundException(0L));

        return wallet.getBalance();
    }

    @Override
    public Page<Transaction> getTransactionHistory(String userEmail, Pageable pageable) {
        Member member = memberRepository.findByEmail(userEmail)
                .orElseThrow(() -> new CustomException.MemberNotFoundException(userEmail));

        Wallet wallet = walletRepository.findByMember(member)
                .orElseThrow(() -> new CustomException.WalletNotFoundException(0L));

        return transactionRepository.findByWalletOrderByTransactionTimeDesc(wallet, pageable);
    }

    @Override
    @Transactional
    public void freezeWallet(String userEmail, String reason) {
        Member member = memberRepository.findByEmail(userEmail)
                .orElseThrow(() -> new CustomException.MemberNotFoundException(userEmail));

        Wallet wallet = walletRepository.findByMember(member)
                .orElseThrow(() -> new CustomException.WalletNotFoundException(0L));

        wallet.freeze(reason);
        walletRepository.save(wallet);

        // 發送通知
        notificationService.createSecurityNotification(
                userEmail,
                "錢包凍結",
                "系統"
        );
    }

    @Override
    public void unfreezeWallet(String userEmail) {

    }

    @Override
    @Transactional
    public void resetDailyLimit() {
        List<Wallet> wallets = walletRepository.findAll();
        for (Wallet wallet : wallets) {
            wallet.resetDailyTransactionAmount();
            walletRepository.save(wallet);
        }
    }

    @Override
    @Transactional
    public void resetMonthlyLimit() {
        List<Wallet> wallets = walletRepository.findAll();
        for (Wallet wallet : wallets) {
            wallet.resetMonthlyTransactionAmount();
            walletRepository.save(wallet);
        }
    }

    @Override
    public Wallet.WalletStatus checkWalletStatus(String userEmail) {
        return null;
    }

    @Override
    public Wallet updateTransactionLimits(String userEmail, Double dailyLimit, Double monthlyLimit) {
        return null;
    }

    @Override
    public Map<String, Object> getWalletDetails(String userEmail) {
        return Map.of();
    }

    @Override
    public boolean checkTransactionLimits(String userEmail, Double amount) {
        return false;
    }

    @Override
    public Integer getPointsBalance(String userEmail) {
        return 0;
    }

    @Override
    public Wallet addPoints(String userEmail, Integer points) {
        return null;
    }

    @Override
    public WalletResponse getWalletBalance(String userEmail) {
        return null;
    }

    @Override
    public TransactionResponse topUp(String userEmail, TopUpRequest request) {
        return null;
    }

    @Override
    public TransactionResponse transfer(String userEmail, TransferRequest request) {
        return null;
    }

    @Override
    public Page<TransactionResponse> getTransactions(String userEmail, LocalDateTime startTime, LocalDateTime endTime, Pageable pageable) {
        return null;
    }

    @Override
    public TransactionResponse getTransaction(Long transactionId, String userEmail) {
        return null;
    }

    @Override
    public Wallet usePoints(String userEmail, Integer points) {
        return null;
    }

    // 私有輔助方法
    private void validateTransactionLimits(Wallet wallet, Double amount) {
        if (wallet.getDailyTransactionAmount() + amount > wallet.getDailyTransactionLimit()) {
            throw new IllegalStateException("超過每日交易限額");
        }

        if (wallet.getMonthlyTransactionAmount() + amount > wallet.getMonthlyTransactionLimit()) {
            throw new IllegalStateException("超過每月交易限額");
        }
    }
}