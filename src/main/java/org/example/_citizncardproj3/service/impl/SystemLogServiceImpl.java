package org.example._citizncardproj3.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example._citizncardproj3.exception.CustomException;
import org.example._citizncardproj3.model.entity.Member;
import org.example._citizncardproj3.model.entity.SystemLog;
import org.example._citizncardproj3.repository.MemberRepository;
import org.example._citizncardproj3.repository.SystemLogRepository;
import org.example._citizncardproj3.service.SystemLogService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SystemLogServiceImpl implements SystemLogService {

    private final SystemLogRepository systemLogRepository;
    private final MemberRepository memberRepository;

    @Override
    @Transactional
    public SystemLog createLog(
            SystemLog.LogType logType,
            SystemLog.LogLevel level,
            String description,
            String userEmail,
            String moduleName,
            String actionName) {

        SystemLog systemLog = SystemLog.builder()
                .logType(logType)
                .level(level)
                .description(description)
                .moduleName(moduleName)
                .actionName(actionName)
                .logTime(LocalDateTime.now())
                .build();

        // 如果有提供用戶信息，則關聯用戶
        if (userEmail != null) {
            Member user = memberRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new CustomException.MemberNotFoundException(userEmail));
            systemLog.setUser(user);
        }

        return systemLogRepository.save(systemLog);
    }

    @Override
    @Transactional
    public SystemLog createErrorLog(
            String description,
            String errorDetail,
            String moduleName) {

        SystemLog systemLog = SystemLog.builder()
                .logType(SystemLog.LogType.ERROR)
                .level(SystemLog.LogLevel.ERROR)
                .description(description)
                .operationDetail(errorDetail)
                .moduleName(moduleName)
                .logTime(LocalDateTime.now())
                .build();

        return systemLogRepository.save(systemLog);
    }

    @Override
    @Transactional
    public SystemLog createSecurityLog(
            String description,
            String userEmail,
            String ipAddress,
            String actionName) {

        Member user = null;
        if (userEmail != null) {
            user = memberRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new CustomException.MemberNotFoundException(userEmail));
        }

        SystemLog systemLog = SystemLog.builder()
                .logType(SystemLog.LogType.SECURITY)
                .level(SystemLog.LogLevel.WARNING)
                .description(description)
                .user(user)
                .ipAddress(ipAddress)
                .actionName(actionName)
                .logTime(LocalDateTime.now())
                .build();

        return systemLogRepository.save(systemLog);
    }

    @Override
    public Page<SystemLog> getLogsByType(SystemLog.LogType logType, Pageable pageable) {
        return systemLogRepository.findByLogTypeOrderByLogTimeDesc(logType, pageable);
    }

    @Override
    public Page<SystemLog> getLogsByLevel(SystemLog.LogLevel level, Pageable pageable) {
        return systemLogRepository.findByLevelOrderByLogTimeDesc(level, pageable);
    }

    @Override
    public List<SystemLog> getErrorLogs(LocalDateTime startTime) {
        // 設定要查詢的錯誤級別
        List<SystemLog.LogLevel> errorLevels = List.of(
                SystemLog.LogLevel.ERROR,
                SystemLog.LogLevel.CRITICAL
        );
        return systemLogRepository.findErrorLogs(errorLevels, startTime);
    }
    @Override
    public List<SystemLog> getSecurityLogs() {
        // 設定要查詢的安全日誌級別
        List<SystemLog.LogLevel> securityLevels = List.of(
                SystemLog.LogLevel.WARNING,
                SystemLog.LogLevel.ERROR,
                SystemLog.LogLevel.CRITICAL
        );
        return systemLogRepository.findSecurityLogs(
                SystemLog.LogType.SECURITY,
                securityLevels
        );
    }

    @Override
    public List<SystemLog> getUserOperationLogs(String userEmail, LocalDateTime startTime, LocalDateTime endTime) {
        Member user = memberRepository.findByEmail(userEmail)
                .orElseThrow(() -> new CustomException.MemberNotFoundException(userEmail));

        return systemLogRepository.findUserOperations(user, startTime, endTime);
    }

    @Override
    public List<SystemLog> getSlowOperations(Long threshold) {
        return systemLogRepository.findSlowOperations(threshold);
    }

    @Override
    public List<Object[]> getLogTypeStatistics() {
        return systemLogRepository.countByLogType();
    }


    @Override
    @Transactional
    public void cleanupOldLogs(LocalDateTime expiryTime) {
        // 設定不要刪除的重要日誌級別
        List<SystemLog.LogLevel> excludedLevels = List.of(
                SystemLog.LogLevel.ERROR,
                SystemLog.LogLevel.CRITICAL
        );
        systemLogRepository.deleteExpiredLogs(expiryTime, excludedLevels);
    }

    @Override
    public List<SystemLog> getLogsByTimeRangeAndLevels(
            LocalDateTime startTime,
            LocalDateTime endTime,
            List<SystemLog.LogLevel> levels) {
        return systemLogRepository.findLogsByTimeRangeAndLevels(startTime, endTime, levels);
    }

    @Override
    public List<SystemLog> getLogsByModule(String moduleName) {
        return systemLogRepository.findByModuleNameOrderByLogTimeDesc(moduleName);
    }

    @Override
    public List<SystemLog> getLogsByAction(String actionName) {
        return systemLogRepository.findByActionNameOrderByLogTimeDesc(actionName);
    }

    @Override
    public List<SystemLog> getLogsByIpAddress(String ipAddress) {
        return systemLogRepository.findByIpAddressOrderByLogTimeDesc(ipAddress);
    }
}