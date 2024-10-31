package org.example._citizncardproj3.repository;

import org.example._citizncardproj3.model.entity.Member;
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
public interface WalletRepository extends JpaRepository<Wallet, Long> {

    // 基本查詢方法
    Optional<Wallet> findByMember(Member member);

    List<Wallet> findByWalletType(Wallet.WalletType walletType);

    List<Wallet> findByStatus(Wallet.WalletStatus status);

    // 分頁查詢
    Page<Wallet> findByStatusOrderByCreatedAtDesc(
            Wallet.WalletStatus status,
            Pageable pageable
    );

    // 複雜條件查詢
    @Query("SELECT w FROM Wallet w WHERE w.member = :member " +
            "AND w.status = 'ACTIVE' AND w.balance >= :minBalance")
    List<Wallet> findActiveWalletsWithMinBalance(
            @Param("member") Member member,
            @Param("minBalance") Double minBalance
    );

    // 查詢餘額不足的錢包
    @Query("SELECT w FROM Wallet w WHERE w.balance < :threshold " +
            "AND w.status = 'ACTIVE'")
    List<Wallet> findWalletsWithLowBalance(@Param("threshold") Double threshold);

    // 統計查詢
    @Query("SELECT w.walletType, COUNT(w), SUM(w.balance) FROM Wallet w " +
            "GROUP BY w.walletType")
    List<Object[]> getWalletStatistics();

    @Query("SELECT SUM(w.balance) FROM Wallet w WHERE w.status = 'ACTIVE'")
    Double getTotalActiveBalance();

    // 更新操作
    @Modifying
    @Query("UPDATE Wallet w SET w.balance = w.balance + :amount " +
            "WHERE w.walletId = :walletId")
    int addBalance(
            @Param("walletId") Long walletId,
            @Param("amount") Double amount
    );

    @Modifying
    @Query("UPDATE Wallet w SET w.balance = w.balance - :amount " +
            "WHERE w.walletId = :walletId AND w.balance >= :amount")
    int subtractBalance(
            @Param("walletId") Long walletId,
            @Param("amount") Double amount
    );

    @Modifying
    @Query("UPDATE Wallet w SET w.status = :newStatus " +
            "WHERE w.walletId = :walletId")
    int updateWalletStatus(
            @Param("walletId") Long walletId,
            @Param("newStatus") Wallet.WalletStatus newStatus
    );

    // 批量操作
    @Modifying
    @Query("UPDATE Wallet w SET w.status = 'FROZEN' " +
            "WHERE w.balance < :minBalance AND w.status = 'ACTIVE'")
    int freezeLowBalanceWallets(@Param("minBalance") Double minBalance);

    // 使用悲觀鎖查詢
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT w FROM Wallet w WHERE w.walletId = :walletId")
    Optional<Wallet> findByIdWithLock(@Param("walletId") Long walletId);

    // 查詢交易記錄
    @Query("SELECT w FROM Wallet w WHERE w.member = :member " +
            "AND EXISTS (SELECT t FROM Transaction t WHERE t.wallet = w " +
            "AND t.transactionTime BETWEEN :startTime AND :endTime)")
    List<Wallet> findWalletsWithTransactions(
            @Param("member") Member member,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    // 查詢點數餘額
    @Query("SELECT w FROM Wallet w WHERE w.member = :member " +
            "AND w.walletType = 'POINTS' AND w.pointsBalance > 0")
    List<Wallet> findWalletsWithPoints(@Param("member") Member member);

    // 查詢特定類型的活躍錢包
    @Query("SELECT w FROM Wallet w WHERE w.walletType = :type " +
            "AND w.status = 'ACTIVE' " +
            "ORDER BY w.balance DESC")
    List<Wallet> findActiveWalletsByType(
            @Param("type") Wallet.WalletType type,
            Pageable pageable
    );

    // 查詢需要更新的錢包
    @Query("SELECT w FROM Wallet w WHERE w.lastTransactionTime < :lastActiveTime " +
            "AND w.status = 'ACTIVE'")
    List<Wallet> findWalletsNeedingUpdate(
            @Param("lastActiveTime") LocalDateTime lastActiveTime
    );

    // 軟刪除
    @Modifying
    @Query("UPDATE Wallet w SET w.isDeleted = true WHERE w.walletId = :walletId")
    int softDeleteWallet(@Param("walletId") Long walletId);
}