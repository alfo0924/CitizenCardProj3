package org.example._citizncardproj3.repository;

import org.example._citizncardproj3.model.entity.Member;
import org.example._citizncardproj3.model.entity.SystemLog;
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
public interface SystemLogRepository extends JpaRepository<SystemLog, Long> {

    // 基本查詢方法
    List<SystemLog> findByLogType(SystemLog.LogType logType);

    List<SystemLog> findByLevel(SystemLog.LogLevel level);

    List<SystemLog> findByUser(Member user);

    // 分頁查詢
    Page<SystemLog> findByLogTypeOrderByLogTimeDesc(
            SystemLog.LogType logType,
            Pageable pageable
    );

    Page<SystemLog> findByLevelOrderByLogTimeDesc(
            SystemLog.LogLevel level,
            Pageable pageable
    );

    // 複雜條件查詢
    @Query("SELECT sl FROM SystemLog sl WHERE sl.logTime BETWEEN :startTime AND :endTime " +
            "AND sl.level IN :levels ORDER BY sl.logTime DESC")
    List<SystemLog> findLogsByTimeRangeAndLevels(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("levels") List<SystemLog.LogLevel> levels
    );

    // 查詢錯誤日誌
    @Query("SELECT sl FROM SystemLog sl WHERE sl.level IN ('ERROR', 'CRITICAL') " +
            "AND sl.logTime >= :startTime ORDER BY sl.logTime DESC")
    List<SystemLog> findErrorLogs(@Param("startTime") LocalDateTime startTime);

    // 統計查詢
    @Query("SELECT sl.logType, COUNT(sl) FROM SystemLog sl GROUP BY sl.logType")
    List<Object[]> countByLogType();

    @Query("SELECT sl.level, COUNT(sl) FROM SystemLog sl " +
            "WHERE sl.logTime BETWEEN :startTime AND :endTime GROUP BY sl.level")
    List<Object[]> countByLevelInTimeRange(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    // 查詢特定模組的日誌
    List<SystemLog> findByModuleNameOrderByLogTimeDesc(String moduleName);

    // 查詢特定操作的日誌
    List<SystemLog> findByActionNameOrderByLogTimeDesc(String actionName);

    // 查詢特定IP地址的日誌
    List<SystemLog> findByIpAddressOrderByLogTimeDesc(String ipAddress);

    // 查詢特定響應狀態的日誌
    List<SystemLog> findByResponseStatusOrderByLogTimeDesc(Integer responseStatus);

    // 查詢執行時間超過閾值的日誌
    @Query("SELECT sl FROM SystemLog sl WHERE sl.executionTime > :threshold " +
            "ORDER BY sl.executionTime DESC")
    List<SystemLog> findSlowOperations(@Param("threshold") Long threshold);

    // 查詢系統錯誤日誌
    @Query("SELECT sl FROM SystemLog sl WHERE sl.level = 'ERROR' " +
            "AND sl.logTime >= :startTime " +
            "AND sl.moduleName = :moduleName")
    List<SystemLog> findSystemErrors(
            @Param("startTime") LocalDateTime startTime,
            @Param("moduleName") String moduleName
    );

    // 查詢用戶操作日誌
    @Query("SELECT sl FROM SystemLog sl WHERE sl.user = :user " +
            "AND sl.logTime BETWEEN :startTime AND :endTime " +
            "ORDER BY sl.logTime DESC")
    List<SystemLog> findUserOperations(
            @Param("user") Member user,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    // 查詢安全相關日誌
    @Query("SELECT sl FROM SystemLog sl WHERE sl.logType = 'SECURITY' " +
            "AND sl.level IN ('WARNING', 'ERROR', 'CRITICAL') " +
            "ORDER BY sl.logTime DESC")
    List<SystemLog> findSecurityLogs();

    // 批量清理過期日誌
    @Modifying
    @Query("DELETE FROM SystemLog sl WHERE sl.logTime < :expiryTime " +
            "AND sl.level NOT IN ('ERROR', 'CRITICAL')")
    int deleteExpiredLogs(@Param("expiryTime") LocalDateTime expiryTime);

    // 使用悲觀鎖查詢
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT sl FROM SystemLog sl WHERE sl.logId = :logId")
    Optional<SystemLog> findByIdWithLock(@Param("logId") Long logId);

    // 軟刪除
    @Modifying
    @Query("UPDATE SystemLog sl SET sl.isDeleted = true WHERE sl.logId = :logId")
    int softDeleteLog(@Param("logId") Long logId);
}