package org.example._citizncardproj3.model.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberUpdateRequest {

    /**
     * 會員姓名
     */
    @Size(min = 2, max = 50, message = "姓名長度必須在2-50個字元之間")
    private String name;

    /**
     * 手機號碼
     */
    @Pattern(regexp = "^09\\d{8}$", message = "手機號碼格式不正確")
    private String phone;

    /**
     * 生日
     */
    @Past(message = "生日必須是過去的日期")
    private LocalDate birthday;

    /**
     * 性別
     */
    @Pattern(regexp = "^(MALE|FEMALE|OTHER)$", message = "性別只能是MALE、FEMALE或OTHER")
    private String gender;

    /**
     * 地址
     */
    @Size(max = 200, message = "地址長度不能超過200個字元")
    private String address;

    /**
     * 緊急聯絡人
     */
    private EmergencyContact emergencyContact;

    /**
     * 通知設定
     */
    private NotificationSettings notificationSettings;

    /**
     * 個人偏好設定
     */
    private PreferenceSettings preferenceSettings;

    /**
     * 身份證字號
     */
    @Pattern(regexp = "^[A-Z][12]\\d{8}$", message = "身份證字號格式不正確")
    private String idNumber;

    /**
     * 會員卡類型
     */
    private String cardType;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EmergencyContact {
        @NotBlank(message = "緊急聯絡人姓名不能為空")
        @Size(min = 2, max = 50, message = "緊急聯絡人姓名長度必須在2-50個字元之間")
        private String name;

        @NotBlank(message = "緊急聯絡人電話不能為空")
        @Pattern(regexp = "^09\\d{8}$", message = "緊急聯絡人電話格式不正確")
        private String phone;

        @Size(max = 50, message = "關係長度不能超過50個字元")
        private String relationship;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NotificationSettings {
        private boolean emailNotification;
        private boolean smsNotification;
        private boolean pushNotification;
        private boolean marketingNotification;

        @Size(max = 100, message = "自定義通知時間格式不正確")
        private String customNotificationTime;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PreferenceSettings {
        private String language;
        private String timezone;
        private List<String> favoriteMovieGenres;
        private List<String> preferredSeatingAreas;
        private Map<String, String> customPreferences;
        private Boolean autoBooking;
        private String preferredPaymentMethod;
    }

    /**
     * 驗證更新請求的有效性
     */
    public boolean isValid() {
        // 至少要有一個欄位需要更新
        return name != null ||
                phone != null ||
                birthday != null ||
                gender != null ||
                address != null ||
                idNumber != null ||
                cardType != null ||
                emergencyContact != null ||
                notificationSettings != null ||
                preferenceSettings != null;
    }

    /**
     * 驗證手機號碼格式
     */
    public boolean isValidPhone() {
        if (phone == null) {
            return true;
        }
        return phone.matches("^09\\d{8}$");
    }

    /**
     * 驗證生日是否合理
     */
    public boolean isValidBirthday() {
        if (birthday == null) {
            return true;
        }
        LocalDate minDate = LocalDate.now().minusYears(120);
        return birthday.isAfter(minDate) && birthday.isBefore(LocalDate.now());
    }

    /**
     * 驗證身份證字號格式
     */
    public boolean isValidIdNumber() {
        if (idNumber == null) {
            return true;
        }
        return idNumber.matches("^[A-Z][12]\\d{8}$");
    }

    /**
     * 驗證會員卡類型
     */
    public boolean isValidCardType() {
        if (cardType == null) {
            return true;
        }
        List<String> validTypes = List.of("REGULAR", "SENIOR", "STUDENT", "DISABILITY");
        return validTypes.contains(cardType);
    }

    /**
     * 驗證緊急聯絡人資料
     */
    public boolean isValidEmergencyContact() {
        if (emergencyContact == null) {
            return true;
        }
        return emergencyContact.getName() != null &&
                emergencyContact.getPhone() != null &&
                emergencyContact.getPhone().matches("^09\\d{8}$");
    }
}