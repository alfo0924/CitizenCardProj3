package org.example._citizncardproj3.model.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class JwtAuthResponse {
    private String accessToken;
    private String tokenType;
    private String refreshToken;
    private Long expiresIn;
    private String memberName;
    private String memberEmail;
    private String role;
    private Map<String, Object> profile;
    private String message;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime issuedAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime expiresAt;

    // 建構子 - 只有token的情況
    public JwtAuthResponse(String accessToken) {
        this.accessToken = accessToken;
        this.tokenType = "Bearer";
    }

    // 建構子 - 錯誤訊息
    public JwtAuthResponse(String accessToken, String message) {
        this.accessToken = accessToken;
        this.message = message;
    }

    // 建構子 - 完整登入響應
    public JwtAuthResponse(String accessToken, String refreshToken, Long expiresIn,
                           String memberName, String memberEmail, String role) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.tokenType = "Bearer";
        this.expiresIn = expiresIn;
        this.memberName = memberName;
        this.memberEmail = memberEmail;
        this.role = role;
        this.issuedAt = LocalDateTime.now();
        this.expiresAt = LocalDateTime.now().plusSeconds(expiresIn);
    }

    // 檢查token是否過期
    public boolean isExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }

    // 獲取剩餘有效時間（秒）
    public long getRemainingTime() {
        if (expiresAt == null) {
            return 0;
        }
        return java.time.Duration.between(LocalDateTime.now(), expiresAt).getSeconds();
    }

    // 檢查是否需要更新token
    public boolean needsRefresh() {
        if (expiresAt == null) {
            return true;
        }
        // 如果剩餘時間少於5分鐘，建議更新
        return getRemainingTime() < 300;
    }

    // 用於日誌記錄的方法
    public String toLogString() {
        return String.format("JwtAuth{user='%s', role='%s', expires='%s'}",
                memberEmail, role, expiresAt);
    }

    // 獲取授權標頭值
    public String getAuthorizationHeader() {
        return tokenType + " " + accessToken;
    }
}