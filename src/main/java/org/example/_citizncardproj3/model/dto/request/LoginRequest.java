package org.example._citizncardproj3.model.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.io.Serial;
import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "電子郵件不能為空")
    @Email(message = "請輸入有效的電子郵件格式")
    private String email;

    @NotBlank(message = "密碼不能為空")
    @Size(min = 6, max = 64, message = "密碼長度必須在6到64個字符之間")
    @Pattern(
            regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{6,}$",
            message = "密碼必須包含至少一個數字、一個小寫字母、一個大寫字母和一個特殊字符"
    )
    private String password;

    // 可選的記住我功能
    private boolean rememberMe;

    // 可選的驗證碼
    private String captcha;

    // 可選的設備信息
    @Builder.Default
    private DeviceInfo deviceInfo = new DeviceInfo();

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DeviceInfo {
        private String deviceType;
        private String deviceId;
        private String deviceName;
        private String operatingSystem;
        private String browserInfo;
        private String ipAddress;
    }

    // 驗證方法
    @JsonIgnore
    public boolean isValid() {
        return email != null && !email.trim().isEmpty() &&
                password != null && !password.trim().isEmpty() &&
                email.matches("^[A-Za-z0-9+_.-]+@(.+)$") &&
                password.length() >= 6;
    }

    // 密碼強度檢查
    @JsonIgnore
    public PasswordStrength checkPasswordStrength() {
        int score = 0;

        // 長度檢查
        if (password.length() >= 8) score++;
        if (password.length() >= 12) score++;

        // 包含數字
        if (password.matches(".*\\d.*")) score++;

        // 包含小寫字母
        if (password.matches(".*[a-z].*")) score++;

        // 包含大寫字母
        if (password.matches(".*[A-Z].*")) score++;

        // 包含特殊字符
        if (password.matches(".*[@#$%^&+=].*")) score++;

        // 返回密碼強度等級
        if (score >= 6) return PasswordStrength.STRONG;
        if (score >= 4) return PasswordStrength.MEDIUM;
        return PasswordStrength.WEAK;
    }

    // 密碼強度枚舉
    @Getter
    public enum PasswordStrength {
        WEAK("弱"),
        MEDIUM("中"),
        STRONG("強");

        private final String description;

        PasswordStrength(String description) {
            this.description = description;
        }

    }

    // 清理敏感數據
    public void clearSensitiveData() {
        this.password = null;
        this.captcha = null;
    }

    // 構建日誌信息（排除敏感數據）
    @JsonIgnore
    public String toLogString() {
        return String.format("LoginRequest{email='%s', rememberMe=%s, deviceInfo=%s}",
                email, rememberMe, deviceInfo);
    }

    // 驗證碼相關邏輯
    @JsonIgnore
    public boolean requiresCaptcha() {
        // 實現驗證碼要求的邏輯
        // 例如：連續失敗登入次數超過限制時要求輸入驗證碼
        return false; // 根據實際需求實現
    }

    @JsonIgnore
    public boolean validateCaptcha() {
        // 實現驗證碼驗證邏輯
        return captcha != null && !captcha.trim().isEmpty();
    }
}