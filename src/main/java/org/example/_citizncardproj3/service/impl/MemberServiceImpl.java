package org.example._citizncardproj3.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example._citizncardproj3.exception.CustomException;
import org.example._citizncardproj3.model.dto.request.LoginRequest;
import org.example._citizncardproj3.model.dto.request.MemberRegistrationRequest;
import org.example._citizncardproj3.model.dto.request.MemberUpdateRequest;
import org.example._citizncardproj3.model.dto.response.BookingResponse;
import org.example._citizncardproj3.model.dto.response.DiscountUsageResponse;
import org.example._citizncardproj3.model.dto.response.MemberResponse;
import org.example._citizncardproj3.model.entity.Member;
import org.example._citizncardproj3.model.entity.Wallet;
import org.example._citizncardproj3.repository.MemberRepository;
import org.example._citizncardproj3.repository.WalletRepository;
import org.example._citizncardproj3.service.MemberService;
import org.example._citizncardproj3.util.JwtUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {

    private final MemberRepository memberRepository;
    private final WalletRepository walletRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    @Override
    @Transactional
    public MemberResponse register(MemberRegistrationRequest request) {
        // 驗證請求
        validateRegistrationRequest(request);

        // 檢查郵箱是否已存在
        if (memberRepository.existsByEmail(request.getEmail())) {
            throw new CustomException.EmailAlreadyExistsException(request.getEmail());
        }

        // 檢查手機號是否已存在
        if (memberRepository.existsByPhone(request.getPhone())) {
            throw new CustomException.PhoneAlreadyExistsException(request.getPhone());
        }

        // 創建會員
        Member member = Member.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .name(request.getName())
                .phone(request.getPhone())
                .birthday(request.getBirthday())
                .gender(request.getGender())
                .address(request.getAddress())
                .status(Member.MemberStatus.INACTIVE)
                .role(Member.Role.ROLE_USER)
                .build();

        member = memberRepository.save(member);

        // 創建錢包
        Wallet wallet = Wallet.builder()
                .member(member)
                .balance(0.0)
                .walletType(Wallet.WalletType.GENERAL)
                .status(Wallet.WalletStatus.ACTIVE)
                .pointsBalance(0)
                .dailyTransactionLimit(50000.0)
                .monthlyTransactionLimit(500000.0)
                .build();

        walletRepository.save(wallet);

        // 啟用會員
        member.activate();
        member = memberRepository.save(member);

        return convertToResponse(member);
    }

    @Override
    public String login(LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );

            Member member = (Member) authentication.getPrincipal();

            // 更新登入資訊
            member.setLastLoginTime(LocalDateTime.now());
            member.setFailedLoginAttempts(0);
            memberRepository.save(member);

            // 生成JWT
            return jwtUtil.generateToken(member);

        } catch (Exception e) {
            Member member = memberRepository.findByEmail(request.getEmail()).orElse(null);
            if (member != null) {
                member.recordLoginFailure();
                memberRepository.save(member);
            }
            throw new CustomException.InvalidCredentialsException();
        }
    }

    @Override
    @Transactional
    public MemberResponse updateProfile(String userEmail, MemberUpdateRequest request) {
        Member member = memberRepository.findByEmail(userEmail)
                .orElseThrow(() -> new CustomException.MemberNotFoundException(userEmail));

        // 更新資料
        if (request.getPhone() != null &&
                !request.getPhone().equals(member.getPhone()) &&
                memberRepository.existsByPhone(request.getPhone())) {
            throw new CustomException.PhoneAlreadyExistsException(request.getPhone());
        }

        member.updateProfile(
                request.getName(),
                request.getPhone(),
                request.getAddress()
        );

        return convertToResponse(memberRepository.save(member));
    }

    @Override
    public MemberResponse getMemberProfile(String userEmail) {
        Member member = memberRepository.findByEmail(userEmail)
                .orElseThrow(() -> new CustomException.MemberNotFoundException(userEmail));
        return convertToResponse(member);
    }

    @Override
    public MemberResponse updateMemberProfile(String userEmail, MemberUpdateRequest request) {
        return null;
    }

    @Override
    public String updateAvatar(String userEmail, MultipartFile file) {
        return "";
    }

    @Override
    public void changePassword(String userEmail, String oldPassword, String newPassword) {

    }

    @Override
    public Page<BookingResponse> getMemberOrders(String userEmail, Pageable pageable) {
        return null;
    }

    @Override
    public Page<DiscountUsageResponse> getDiscountHistory(String userEmail, Pageable pageable) {
        return null;
    }

    @Override
    public void blockMember(Long memberId, String reason) {

    }

    @Override
    @Transactional
    public void resetPassword(String userEmail, String oldPassword, String newPassword) {
        Member member = memberRepository.findByEmail(userEmail)
                .orElseThrow(() -> new CustomException.MemberNotFoundException(userEmail));

        // 驗證舊密碼
        if (!passwordEncoder.matches(oldPassword, member.getPassword())) {
            throw new CustomException.InvalidCredentialsException();
        }

        // 更新密碼
        member.resetPassword(passwordEncoder.encode(newPassword));
        memberRepository.save(member);
    }

    @Override
    public void sendPasswordResetEmail(String email) {

    }

    @Override
    public void verifyPasswordResetToken(String token, String newPassword) {

    }

    @Override
    public void deactivateAccount(String userEmail, String reason) {

    }

    @Override
    public void activateAccount(String userEmail) {

    }

    @Override
    public boolean isEmailExists(String email) {
        return false;
    }

    @Override
    public boolean isPhoneExists(String phone) {
        return false;
    }

    @Override
    public Page<MemberResponse> getAllMembers(Pageable pageable) {
        return null;
    }

    @Override
    public Page<MemberResponse> searchMembers(String keyword, Pageable pageable) {
        return null;
    }

    @Override
    public MemberResponse updateMemberStatus(String userEmail, String status) {
        return null;
    }

    @Override
    public Map<String, Object> getMemberStatistics(String userEmail) {
        return Map.of();
    }

    @Override
    public boolean checkPermission(String userEmail, String permission) {
        return false;
    }

    // 私有輔助方法
    private void validateRegistrationRequest(MemberRegistrationRequest request) {
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new CustomException.PasswordMismatchException();
        }

        if (request.getBirthday() != null &&
                request.getBirthday().isAfter(LocalDateTime.now().toLocalDate())) {
            throw new IllegalArgumentException("生日不能是未來日期");
        }
    }

    private MemberResponse convertToResponse(Member member) {
        return MemberResponse.builder()
                .memberId(member.getMemberId())
                .email(member.getEmail())
                .name(member.getName())
                .phone(member.getPhone())
                .address(member.getAddress())
                .birthday(member.getBirthday())
                .gender(member.getGender().getDescription())
                .status(convertMemberStatus(member.getStatus()))  // 轉換狀態
                .lastLoginTime(member.getLastLoginTime())
                .registrationTime(member.getCreatedAt())
                .build();
    }

    /**
     * 轉換會員狀態
     */
    private MemberResponse.MemberStatus convertMemberStatus(Member.MemberStatus status) {
        if (status == null) {
            return MemberResponse.MemberStatus.INACTIVE;
        }

        switch (status) {
            case ACTIVE:
                return MemberResponse.MemberStatus.ACTIVE;
            case INACTIVE:
                return MemberResponse.MemberStatus.INACTIVE;
            case SUSPENDED:
                return MemberResponse.MemberStatus.SUSPENDED;
            case LOCKED:
                return MemberResponse.MemberStatus.LOCKED;
            default:
                return MemberResponse.MemberStatus.INACTIVE;
        }
    }
}