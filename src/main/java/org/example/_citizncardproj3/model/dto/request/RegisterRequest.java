package org.example._citizncardproj3.model.dto.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example._citizncardproj3.model.entity.Member;

import javax.validation.constraints.*;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RegisterRequest {

    @NotBlank(message = "姓名不能為空")
    @Size(min = 2, max = 50, message = "姓名長度必須在2-50個字符之間")
    private String name;

    @NotBlank(message = "Email不能為空")
    @Email(message = "Email格式不正確")
    private String email;

    @NotBlank(message = "密碼不能為空")
    @Size(min = 8, max = 20, message = "密碼長度必須在8-20個字符之間")
    @Pattern(
            regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$",
            message = "密碼必須包含大小寫字母、數字和特殊字符"
    )
    private String password;

    @NotBlank(message = "確認密碼不能為空")
    private String confirmPassword;

    @NotBlank(message = "手機號碼不能為空")
    @Pattern(regexp = "^09\\d{8}$", message = "手機號碼格式不正確")
    private String phone;

    @NotNull(message = "生日不能為空")
    @Past(message = "生日必須是過去的日期")
    private LocalDate birthDate;

    @Size(max = 200, message = "地址長度不能超過200個字符")
    private String address;

    @Pattern(regexp = "^[A-Z][12]\\d{8}$", message = "身份證字號格式不正確")
    private String idNumber;

    @Size(max = 500)
    private String avatar;

    @Builder.Default
    private Member.Gender gender = Member.Gender.OTHER;

    @Builder.Default
    private boolean subscribeNewsletter = false;

    private String referralCode;

    // 驗證方法
    public void validate() {
        validatePassword();
        validateAge();
    }

    // 驗證密碼
    private void validatePassword() {
        if (!password.equals(confirmPassword)) {
            throw new IllegalArgumentException("密碼與確認密碼不符");
        }
    }

    // 驗證年齡
    private void validateAge() {
        if (birthDate != null) {
            int age = LocalDate.now().getYear() - birthDate.getYear();
            if (age < 12) {
                throw new IllegalArgumentException("年齡必須大於12歲");
            }
        }
    }

    // 檢查密碼強度
    public boolean isPasswordStrong() {
        if (password == null) {
            return false;
        }

        boolean hasUpperCase = false;
        boolean hasLowerCase = false;
        boolean hasNumber = false;
        boolean hasSpecialChar = false;

        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) hasUpperCase = true;
            else if (Character.isLowerCase(c)) hasLowerCase = true;
            else if (Character.isDigit(c)) hasNumber = true;
            else hasSpecialChar = true;
        }

        return hasUpperCase && hasLowerCase && hasNumber && hasSpecialChar;
    }

    // 獲取密碼強度描述
    public String getPasswordStrengthDescription() {
        int strength = 0;
        if (password == null) return "密碼不能為空";

        if (password.length() >= 8) strength++;
        if (password.matches(".*[A-Z].*")) strength++;
        if (password.matches(".*[a-z].*")) strength++;
        if (password.matches(".*\\d.*")) strength++;
        if (password.matches(".*[@#$%^&+=].*")) strength++;

        return switch (strength) {
            case 0, 1 -> "非常弱";
            case 2 -> "弱";
            case 3 -> "中等";
            case 4 -> "強";
            case 5 -> "非常強";
            default -> "未知強度";
        };
    }

    // 用於日誌記錄的方法
    public String toLogString() {
        return String.format("RegisterRequest{name='%s', email='%s', phone='%s'}",
                name, email, phone);
    }

    // 遮蔽敏感資訊的方法
    public String getMaskedIdNumber() {
        if (idNumber == null || idNumber.length() < 10) {
            return null;
        }
        return idNumber.substring(0, 3) + "****" + idNumber.substring(7);
    }

    public String getMaskedPhone() {
        if (phone == null || phone.length() < 10) {
            return null;
        }
        return phone.substring(0, 4) + "****" + phone.substring(8);
    }
}