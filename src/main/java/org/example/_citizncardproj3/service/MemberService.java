package org.example._citizncardproj3.service;

import org.example._citizncardproj3.model.dto.request.LoginRequest;
import org.example._citizncardproj3.model.dto.request.MemberRegistrationRequest;
import org.example._citizncardproj3.model.dto.request.MemberUpdateRequest;
import org.example._citizncardproj3.model.dto.response.BookingResponse;
import org.example._citizncardproj3.model.dto.response.DiscountUsageResponse;
import org.example._citizncardproj3.model.dto.response.MemberResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

public interface MemberService {

    /**
     * 會員註冊
     * @param request 註冊請求
     * @return 會員響應
     */
    MemberResponse register(MemberRegistrationRequest request);

    /**
     * 會員登入
     * @param request 登入請求
     * @return JWT token
     */
    String login(LoginRequest request);

    /**
     * 更新會員資料
     * @param userEmail 用戶郵箱
     * @param request 更新請求
     * @return 更新後的會員資料
     */
    MemberResponse updateProfile(String userEmail, MemberUpdateRequest request);


    /**
     * 重設密碼
     * @param userEmail 用戶郵箱
     * @param oldPassword 舊密碼
     * @param newPassword 新密碼
     */
    void resetPassword(String userEmail, String oldPassword, String newPassword);

    /**
     * 發送重設密碼郵件
     * @param email 用戶郵箱
     */
    void sendPasswordResetEmail(String email);

    /**
     * 驗證重設密碼令牌
     * @param token 重設密碼令牌
     * @param newPassword 新密碼
     */
    void verifyPasswordResetToken(String token, String newPassword);

    /**
     * 停用會員帳號
     * @param userEmail 用戶郵箱
     * @param reason 停用原因
     */
    void deactivateAccount(String userEmail, String reason);

    /**
     * 啟用會員帳號
     * @param userEmail 用戶郵箱
     */
    void activateAccount(String userEmail);

    /**
     * 檢查郵箱是否已存在
     * @param email 郵箱
     * @return 是否存在
     */
    boolean isEmailExists(String email);

    /**
     * 檢查手機號是否已存在
     * @param phone 手機號
     * @return 是否存在
     */
    boolean isPhoneExists(String phone);

    /**
     * 獲取會員列表
     * @param pageable 分頁參數
     * @return 會員列表分頁
     */
    Page<MemberResponse> getAllMembers(Pageable pageable);

    /**
     * 搜索會員
     * @param keyword 關鍵字
     * @param pageable 分頁參數
     * @return 搜索結果分頁
     */
    Page<MemberResponse> searchMembers(String keyword, Pageable pageable);

    /**
     * 更新會員狀態
     * @param userEmail 用戶郵箱
     * @param status 新狀態
     * @return 更新後的會員資料
     */
    MemberResponse updateMemberStatus(String userEmail, String status);

    /**
     * 獲取會員統計資訊
     * @param userEmail 用戶郵箱
     * @return 統計資訊
     */
    Map<String, Object> getMemberStatistics(String userEmail);

    /**
     * 檢查會員權限
     * @param userEmail 用戶郵箱
     * @param permission 權限
     * @return 是否有權限
     */
    boolean checkPermission(String userEmail, String permission);

    /**
     * 獲取會員個人資料
     */
    MemberResponse getMemberProfile(String userEmail);

    /**
     * 更新會員資料
     */
    MemberResponse updateMemberProfile(String userEmail, MemberUpdateRequest request);

    /**
     * 更新會員頭像
     */
    String updateAvatar(String userEmail, MultipartFile file);

    /**
     * 更改密碼
     */
    void changePassword(String userEmail, String oldPassword, String newPassword);

    /**
     * 取得會員訂單歷史
     */
    Page<BookingResponse> getMemberOrders(String userEmail, Pageable pageable);

    /**
     * 取得會員優惠使用紀錄
     */
    Page<DiscountUsageResponse> getDiscountHistory(String userEmail, Pageable pageable);



    /**
     * 封鎖會員（管理員用）
     */
    void blockMember(Long memberId, String reason);

}