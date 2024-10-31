package org.example._citizncardproj3.service;

import org.example._citizncardproj3.model.entity.SystemLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public interface SystemLogService {

    /**
     * 創建系統日誌
     * @param logType 日誌類型
     * @param level 日誌級別
     * @param description 描述
     * @param userEmail 用戶郵箱(可選)
     * @param moduleName 模組名稱
     * @param actionName 操作名稱
     * @return 日誌實體
     */
    SystemLog createLog(
            SystemLog.LogType logType,
            SystemLog.LogLevel level,
            String description,
            String userEmail,
            String moduleName,
            String actionName
    );

    /**
     * 創建錯誤日誌
     * @param description 描述
     * @param errorDetail 錯誤詳情
     * @param moduleName 模組名稱
     * @return 日誌實體
     */
    SystemLog createErrorLog(
            String description,
            String errorDetail,
            String moduleName
    );

    /**
     * 創建安全日誌
     * @param description 描述
     * @param userEmail 用戶郵箱
     * @param ipAddress IP地址
     * @param actionName 操作名稱
     * @return 日誌實體
     */
    SystemLog createSecurityLog(
            String description,
            String userEmail,
            String ipAddress,
            String actionName
    );

    /**
     * 根據日誌類型獲取日誌
     * @param logType 日誌類型
     * @param pageable 分頁參數
     * @return 日誌分頁
     */
    Page<SystemLog> getLogsByType(SystemLog.LogType logType, Pageable pageable);

    /**
     * 根據日誌級別獲取日誌
     * @param level 日誌級別
     * @param pageable 分頁參數
     * @return 日誌分頁
     */
    Page<SystemLog> getLogsByLevel(SystemLog.LogLevel level, Pageable pageable);

    /**
     * 獲取錯誤日誌
     * @param startTime 開始時間
     * @return 錯誤日誌列表
     */
    List<SystemLog> getErrorLogs(LocalDateTime startTime);

    /**
     * 獲取安全日誌
     * @return 安全日誌列表
     */
    List<SystemLog> getSecurityLogs();

    /**
     * 獲取用戶操作日誌
     * @param userEmail 用戶郵箱
     * @param startTime 開始時間
     * @param endTime 結束時間
     * @return 操作日誌列表
     */
    List<SystemLog> getUserOperationLogs(
            String userEmail,
            LocalDateTime startTime,
            LocalDateTime endTime
    );

    /**
     * 獲取慢操作日誌
     * @param threshold 時間閾值(毫秒)
     * @return 慢操作日誌列表
     */
    List<SystemLog> getSlowOperations(Long threshold);

    /**
     * 獲取日誌類型統計
     * @return 統計結果
     */
    List<Object[]> getLogTypeStatistics();

    /**
     * 清理舊日誌
     * @param expiryTime 過期時間
     */
    void cleanupOldLogs(LocalDateTime expiryTime);

    /**
     * 獲取特定時間範圍和級別的日誌
     * @param startTime 開始時間
     * @param endTime 結束時間
     * @param levels 日誌級別列表
     * @return 日誌列表
     */
    List<SystemLog> getLogsByTimeRangeAndLevels(
            LocalDateTime startTime,
            LocalDateTime endTime,
            List<SystemLog.LogLevel> levels
    );

    /**
     * 獲取特定模組的日誌
     * @param moduleName 模組名稱
     * @return 日誌列表
     */
    List<SystemLog> getLogsByModule(String moduleName);

    /**
     * 獲取特定操作的日誌
     * @param actionName 操作名稱
     * @return 日誌列表
     */
    List<SystemLog> getLogsByAction(String actionName);

    /**
     * 獲取特定IP地址的日誌
     * @param ipAddress IP地址
     * @return 日誌列表
     */
    List<SystemLog> getLogsByIpAddress(String ipAddress);
}