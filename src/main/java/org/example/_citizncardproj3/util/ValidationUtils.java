package org.example._citizncardproj3.util;

import org.apache.commons.lang3.StringUtils;
import java.util.regex.Pattern;

public class ValidationUtils {

    // Email驗證正則表達式
    private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";

    // 手機號碼驗證正則表達式 (台灣格式)
    private static final String PHONE_REGEX = "^09\\d{8}$";

    // 身份證字號驗證正則表達式
    private static final String ID_CARD_REGEX = "^[A-Z][1-2]\\d{8}$";

    // 密碼強度驗證正則表達式 (至少8位，包含大小寫字母和數字)
    private static final String PASSWORD_REGEX = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[a-zA-Z\\d]{8,}$";

    /**
     * 驗證Email格式
     * @param email 電子郵件地址
     * @return 是否有效
     */
    public static boolean isValidEmail(String email) {
        if (StringUtils.isBlank(email)) {
            return false;
        }
        return Pattern.compile(EMAIL_REGEX).matcher(email).matches();
    }

    /**
     * 驗證手機號碼格式
     * @param phone 手機號碼
     * @return 是否有效
     */
    public static boolean isValidPhone(String phone) {
        if (StringUtils.isBlank(phone)) {
            return false;
        }
        return Pattern.compile(PHONE_REGEX).matcher(phone).matches();
    }

    /**
     * 驗證身份證字號格式
     * @param idCard 身份證字號
     * @return 是否有效
     */
    public static boolean isValidIdCard(String idCard) {
        if (StringUtils.isBlank(idCard)) {
            return false;
        }
        if (!Pattern.compile(ID_CARD_REGEX).matcher(idCard).matches()) {
            return false;
        }
        return checkIdCardVerification(idCard);
    }

    /**
     * 驗證密碼強度
     * @param password 密碼
     * @return 是否符合要求
     */
    public static boolean isValidPassword(String password) {
        if (StringUtils.isBlank(password)) {
            return false;
        }
        return Pattern.compile(PASSWORD_REGEX).matcher(password).matches();
    }

    /**
     * 驗證金額格式
     * @param amount 金額
     * @return 是否有效
     */
    public static boolean isValidAmount(Double amount) {
        if (amount == null) {
            return false;
        }
        return amount >= 0 && amount <= 999999999.99;
    }

    /**
     * 驗證座位號碼格式
     * @param seatNumber 座位號碼
     * @return 是否有效
     */
    public static boolean isValidSeatNumber(String seatNumber) {
        if (StringUtils.isBlank(seatNumber)) {
            return false;
        }
        // 假設座位號碼格式為：字母+數字，如A1, B12
        return seatNumber.matches("^[A-Z]\\d{1,2}$");
    }

    /**
     * 驗證優惠碼格式
     * @param discountCode 優惠碼
     * @return 是否有效
     */
    public static boolean isValidDiscountCode(String discountCode) {
        if (StringUtils.isBlank(discountCode)) {
            return false;
        }
        // 假設優惠碼格式為：8-12位英數字
        return discountCode.matches("^[A-Za-z0-9]{8,12}$");
    }

    /**
     * 驗證設備ID格式
     * @param deviceId 設備ID
     * @return 是否有效
     */
    public static boolean isValidDeviceId(String deviceId) {
        if (StringUtils.isBlank(deviceId)) {
            return false;
        }
        // 假設設備ID格式為：32位英數字
        return deviceId.matches("^[A-Za-z0-9]{32}$");
    }

    /**
     * 驗證名稱格式
     * @param name 名稱
     * @return 是否有效
     */
    public static boolean isValidName(String name) {
        if (StringUtils.isBlank(name)) {
            return false;
        }
        // 只允許中文、英文、數字和空格
        return name.matches("^[\\u4e00-\\u9fa5A-Za-z0-9\\s]{2,50}$");
    }

    /**
     * 驗證地址格式
     * @param address 地址
     * @return 是否有效
     */
    public static boolean isValidAddress(String address) {
        if (StringUtils.isBlank(address)) {
            return false;
        }
        // 允許中文、英文、數字、空格和常用符號
        return address.matches("^[\\u4e00-\\u9fa5A-Za-z0-9\\s,.-]{5,100}$");
    }

    // 私有輔助方法

    /**
     * 檢查身份證字號驗證碼
     * @param idCard 身份證字號
     * @return 是否有效
     */
    private static boolean checkIdCardVerification(String idCard) {
        // 身份證字號驗證邏輯
        int[] multiply = {1, 9, 8, 7, 6, 5, 4, 3, 2, 1, 1};
        String cityCode = "ABCDEFGHJKLMNPQRSTUVXYWZIO";
        int sum = 0;

        // 將字母轉換為對應的數字
        int firstNumber = cityCode.indexOf(idCard.charAt(0)) + 10;

        // 計算總和
        sum += (firstNumber / 10) * multiply[0];
        sum += (firstNumber % 10) * multiply[1];

        for (int i = 2; i < multiply.length; i++) {
            sum += Character.getNumericValue(idCard.charAt(i-1)) * multiply[i];
        }

        // 檢查驗證碼
        return sum % 10 == 0;
    }
}