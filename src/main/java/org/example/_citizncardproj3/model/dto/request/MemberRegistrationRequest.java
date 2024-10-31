package org.example._citizncardproj3.model.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.*;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberRegistrationRequest {

    @NotBlank(message = "電子郵件不能為空")
    @Email(message = "請輸入有效的電子郵件格式")
    private String email;

    @NotBlank(message = "密碼不能為空")
    @Size(min = 8, max = 64, message = "密碼長度必須在8到64個字符之間")
    @Pattern(
            regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$",
            message = "密碼必須包含至少一個數字、一個小寫字母、一個大寫字母和一個特殊字符"
    )
    private String password;

    @NotBlank(message = "確認密碼不能為空")
    private String confirmPassword;

    @NotBlank(message = "姓名不能為空")
    @Size(min = 2, max = 50, message = "姓名長度必須在2到50個字符之間")
    private String name;

    @NotBlank(message = "手機號碼不能為空")
    @Pattern(regexp = "^09\\d{8}$", message = "請輸入有效的手機號碼格式")
    private String phone;

    @NotNull(message = "生日不能為空")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Past(message = "生日必須是過去的日期")
    private LocalDate birthday;

    @NotBlank(message = "性別不能為空")
    @Pattern(regexp = "^(男|女|其他)$", message = "性別必須是：男、女或其他")
    private String gender;

    @Size(max = 200, message = "地址長度不能超過200個字符")
    private String address;

    @NotNull(message = "卡片類型不能為空")
    private CardType cardType;

    // 卡片類型枚舉
    public enum CardType {
        GENERAL("一般卡"),
        SENIOR("敬老卡"),
        CHARITY("愛心卡"),
        STUDENT("學生卡");

        private final String description;

        CardType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    // 驗證方法
    public boolean isValid() {
        if (!isPasswordValid()) {
            return false;
        }

        if (!isPasswordMatch()) {
            return false;
        }

        if (!isAgeValid()) {
            return false;
        }

        return isCardTypeValid();
    }

    // 密碼複雜度驗證
    private boolean isPasswordValid() {
        return password != null &&
                password.matches("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$");
    }

    // 確認密碼匹配
    private boolean isPasswordMatch() {
        return password != null && password.equals(confirmPassword);
    }

    // 年齡驗證（根據卡片類型）
    private boolean isAgeValid() {
        if (birthday == null) {
            return false;
        }

        int age = LocalDate.now().getYear() - birthday.getYear();

        switch (cardType) {
            case SENIOR:
                return age >= 65;
            case STUDENT:
                return age >= 6 && age <= 25;
            case GENERAL:
                return age >= 18;
            default:
                return true;
        }
    }

    // 卡片類型驗證
    private boolean isCardTypeValid() {
        if (cardType == null) {
            return false;
        }

        int age = LocalDate.now().getYear() - birthday.getYear();

        switch (cardType) {
            case SENIOR:
                if (age < 65) {
                    return false;
                }
                break;
            case STUDENT:
                if (age < 6 || age > 25) {
                    return false;
                }
                break;
            case CHARITY:
                // 愛心卡需要額外的驗證邏輯
                break;
            case GENERAL:
                if (age < 18) {
                    return false;
                }
                break;
        }
        return true;
    }

    // 清理敏感數據
    public void clearSensitiveData() {
        this.password = null;
        this.confirmPassword = null;
    }

    // 構建日誌信息（排除敏感數據）
    public String toLogString() {
        return String.format("MemberRegistrationRequest{email='%s', name='%s', phone='%s', " +
                        "birthday='%s', gender='%s', cardType='%s'}",
                email, name, phone, birthday, gender, cardType);
    }
}