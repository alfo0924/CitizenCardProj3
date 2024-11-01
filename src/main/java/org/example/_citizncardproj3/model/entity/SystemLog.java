package org.example._citizncardproj3.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "system_logs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long logId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LogType logType;

    @Column(nullable = false)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private Member user;

    @Column(length = 45)
    private String ipAddress;

    @Column(length = 255)
    private String userAgent;

    @Type(type = "json")
    @Column(columnDefinition = "json")
    private String operationDetail;

    @Column(nullable = false)
    private LocalDateTime logTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LogLevel level;

    @Column(length = 100)
    private String moduleName;

    @Column(length = 50)
    private String actionName;

    @Column(length = 255)
    private String requestUrl;

    @Column
    private Integer responseStatus;

    @Column
    private Long executionTime;

    @CreationTimestamp
    private LocalDateTime createdAt;

    // 日誌類型枚舉
    public enum LogType {
        SYSTEM("系統操作"),
        ERROR("錯誤"),
        SECURITY("安全事件"),
        TRANSACTION("交易記錄"),
        USER_ACTIVITY("會員活動");

        private final String description;

        LogType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    // 日誌級別枚舉
    public enum LogLevel {
        INFO("資訊"),
        WARNING("警告"),
        ERROR("錯誤"),
        CRITICAL("嚴重");

        private final String description;

        LogLevel(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }



    // 業務方法

    // 創建系統操作日誌
    public static SystemLog createSystemLog(String description, Member user) {
        return SystemLog.builder()
                .logType(LogType.SYSTEM)
                .description(description)
                .user(user)
                .level(LogLevel.INFO)
                .build();
    }

    // 創建錯誤日誌
    public static SystemLog createErrorLog(String description, Exception ex) {
        return SystemLog.builder()
                .logType(LogType.ERROR)
                .description(description)
                .level(LogLevel.ERROR)
                .operationDetail(ex.toString())
                .build();
    }

    // 創建安全事件日誌
    public static SystemLog createSecurityLog(String description, Member user, String ipAddress) {
        return SystemLog.builder()
                .logType(LogType.SECURITY)
                .description(description)
                .user(user)
                .ipAddress(ipAddress)
                .level(LogLevel.WARNING)
                .build();
    }

    // 創建交易日誌
    public static SystemLog createTransactionLog(String description, Member user, String transactionDetail) {
        return SystemLog.builder()
                .logType(LogType.TRANSACTION)
                .description(description)
                .user(user)
                .operationDetail(transactionDetail)
                .level(LogLevel.INFO)
                .build();
    }

    // 添加操作詳情
    public void addOperationDetail(String detail) {
        this.operationDetail = detail;
    }

    // 設置執行時間
    public void setExecutionTimeInMillis(long startTime) {
        this.executionTime = System.currentTimeMillis() - startTime;
    }

    // 更新日誌級別
    public void updateLogLevel(LogLevel newLevel) {
        this.level = newLevel;
    }

    // 檢查是否為錯誤日誌
    public boolean isError() {
        return this.level == LogLevel.ERROR ||
                this.level == LogLevel.CRITICAL;
    }

    // 檢查是否需要立即處理
    public boolean requiresImmediate() {
        return this.level == LogLevel.CRITICAL;
    }

    // 格式化日誌信息
    public String formatLogMessage() {
        return String.format("[%s] [%s] [%s] %s - User: %s, IP: %s",
                logTime,
                logType,
                level,
                description,
                user != null ? user.getEmail() : "System",
                ipAddress != null ? ipAddress : "N/A");
    }

    // 用於日誌記錄的方法
    @Override
    public String toString() {
        return formatLogMessage();
    }
    @Column(nullable = false)
    private Boolean isDeleted = false;  // 添加軟刪除欄位，預設為false

    // 添加getter和setter
    public Boolean getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(Boolean deleted) {
        isDeleted = deleted;
    }

    // 添加初始化方法
    @PrePersist
    public void prePersist() {
        if (this.isDeleted == null) {
            this.isDeleted = false;
        }
        if (this.logTime == null) {
            this.logTime = LocalDateTime.now();
        }
    }
}