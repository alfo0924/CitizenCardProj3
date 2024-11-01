package org.example._citizncardproj3.service;

import org.example._citizncardproj3.model.dto.request.LoginRequest;
import org.example._citizncardproj3.model.dto.request.RegisterRequest;
import org.example._citizncardproj3.model.dto.response.JwtAuthResponse;
import org.example._citizncardproj3.model.entity.Member;
import org.springframework.security.core.Authentication;

public interface AuthService {
    /**
     * 用戶註冊
     */
    Member register(RegisterRequest request);

    /**
     * 用戶登入
     */
    JwtAuthResponse login(LoginRequest request);

    /**
     * 發送密碼重設郵件
     */
    void initiatePasswordReset(String email);

    /**
     * 重設密碼
     */
    void resetPassword(String token, String newPassword);

    /**
     * 驗證重設密碼的token
     */
    boolean validatePasswordResetToken(String token);

    /**
     * 驗證Email
     */
    void verifyEmail(String token);

    /**
     * 重新發送驗證郵件
     */
    void resendVerificationEmail(String email);

    /**
     * 更新最後登入時間
     */
    void updateLastLoginTime(String email);

    /**
     * 檢查帳號是否已驗證
     */
    boolean isEmailVerified(String email);

    /**
     * 檢查帳號是否被鎖定
     */
    boolean isAccountLocked(String email);

    /**
     * 登出
     */
    void logout(String userEmail);

    /**
     * 刷新Token
     */
    JwtAuthResponse refreshToken(String refreshToken);

    /**
     * 變更密碼
     */
    void changePassword(String userEmail, String oldPassword, String newPassword);

    /**
     * 取得當前登入用戶
     */
    Member getCurrentUser(Authentication authentication);

    /**
     * 檢查Email是否已存在
     */
    boolean existsByEmail(String email);

    /**
     * 檢查手機號碼是否已存在
     */
    boolean existsByPhone(String phone);

    /**
     * 鎖定帳號
     */
    void lockAccount(String email, String reason);

    /**
     * 解鎖帳號
     */
    void unlockAccount(String email);

    /**
     * 停用帳號
     */
    void deactivateAccount(String email, String reason);

    /**
     * 啟用帳號
     */
    void activateAccount(String email);

    /**
     * 檢查密碼強度
     */
    boolean isPasswordStrong(String password);

    /**
     * 產生驗證碼
     */
    String generateVerificationCode(String email);

    /**
     * 驗證驗證碼
     */
    boolean verifyCode(String email, String code);
}