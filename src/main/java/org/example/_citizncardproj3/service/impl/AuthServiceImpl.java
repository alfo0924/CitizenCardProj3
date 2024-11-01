package org.example._citizncardproj3.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example._citizncardproj3.exception.CustomException;
import org.example._citizncardproj3.model.dto.request.LoginRequest;
import org.example._citizncardproj3.model.dto.request.RegisterRequest;
import org.example._citizncardproj3.model.dto.response.JwtAuthResponse;
import org.example._citizncardproj3.model.entity.Member;
import org.example._citizncardproj3.model.entity.Wallet;
import org.example._citizncardproj3.repository.MemberRepository;
import org.example._citizncardproj3.repository.WalletRepository;
import org.example._citizncardproj3.service.AuthService;

import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final MemberRepository memberRepository;
    private final WalletRepository walletRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Override
    @Transactional
    public Member register(RegisterRequest request) {
        // 檢查Email是否已存在
        if (memberRepository.existsByEmail(request.getEmail())) {
            throw new CustomException.DuplicateEmailException(request.getEmail());
        }

        // 檢查手機號碼是否已存在
        if (memberRepository.existsByPhone(request.getPhone())) {
            throw new CustomException.DuplicatePhoneException(request.getPhone());
        }

        // 創建新會員
        Member member = Member.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone())
                .status(Member.MemberStatus.PENDING)
                .role(Member.MemberRole.USER)
                .registrationTime(LocalDateTime.now())
                .build();

        member = memberRepository.save(member);

        // 創建錢包
        Wallet wallet = Wallet.builder()
                .member(member)
                .balance(0.0)
                .status(Wallet.WalletStatus.ACTIVE)
                .build();

        walletRepository.save(wallet);

        // 發送驗證郵件
        sendVerificationEmail(member);

        return member;
    }

    @Override
    public JwtAuthResponse login(LoginRequest request) {
        return null;
    }

    @Override
    public void initiatePasswordReset(String email) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException.MemberNotFoundException(email));

        // 檢查帳號狀態
        if (member.getStatus() != Member.MemberStatus.ACTIVE) {
            throw new CustomException.AccountNotActiveException(email);
        }

        // 生成重設密碼token
        String token = UUID.randomUUID().toString();
        member.setResetToken(token);
        member.setResetTokenExpiry(LocalDateTime.now().plusHours(24));
        memberRepository.save(member);

        // 發送重設密碼郵件
        emailService.sendPasswordResetEmail(email, token);
    }

    @Override
    @Transactional
    public void resetPassword(String token, String newPassword) {
        Member member = memberRepository.findByResetToken(token)
                .orElseThrow(() -> new CustomException.InvalidTokenException("無效的重設密碼token"));

        // 檢查token是否過期
        if (member.getResetTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new CustomException.TokenExpiredException("重設密碼token已過期");
        }

        // 更新密碼
        member.setPassword(passwordEncoder.encode(newPassword));
        member.setResetToken(null);
        member.setResetTokenExpiry(null);
        member.setLastPasswordChangeTime(LocalDateTime.now());
        memberRepository.save(member);

        // 發送密碼變更通知
        emailService.sendPasswordChangeNotification(member.getEmail());
    }

    @Override
    public boolean validatePasswordResetToken(String token) {
        return memberRepository.findByResetToken(token)
                .map(member -> member.getResetTokenExpiry().isAfter(LocalDateTime.now()))
                .orElse(false);
    }

    @Override
    @Transactional
    public void verifyEmail(String token) {
        Member member = memberRepository.findByVerificationToken(token)
                .orElseThrow(() -> new CustomException.InvalidTokenException("無效的驗證token"));

        // 檢查token是否過期
        if (member.getVerificationTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new CustomException.TokenExpiredException("驗證token已過期");
        }

        // 更新會員狀態
        member.setStatus(Member.MemberStatus.ACTIVE);
        member.setVerificationToken(null);
        member.setVerificationTokenExpiry(null);
        member.setEmailVerifiedTime(LocalDateTime.now());
        memberRepository.save(member);
    }

    @Override
    public void resendVerificationEmail(String email) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException.MemberNotFoundException(email));

        if (member.getStatus() == Member.MemberStatus.ACTIVE) {
            throw new CustomException.ValidationException("此帳號已驗證");
        }

        // 重新發送驗證郵件
        sendVerificationEmail(member);
    }

    @Override
    public void updateLastLoginTime(String email) {

    }

    @Override
    public boolean isEmailVerified(String email) {
        return false;
    }

    @Override
    public boolean isAccountLocked(String email) {
        return false;
    }

    @Override
    public void logout(String userEmail) {

    }

    @Override
    public JwtAuthResponse refreshToken(String refreshToken) {
        return null;
    }

    @Override
    public void changePassword(String userEmail, String oldPassword, String newPassword) {

    }

    @Override
    public Member getCurrentUser(Authentication authentication) {
        return null;
    }

    @Override
    public boolean existsByEmail(String email) {
        return false;
    }

    @Override
    public boolean existsByPhone(String phone) {
        return false;
    }

    @Override
    public void lockAccount(String email, String reason) {

    }

    @Override
    public void unlockAccount(String email) {

    }

    @Override
    public void deactivateAccount(String email, String reason) {

    }

    @Override
    public void activateAccount(String email) {

    }

    @Override
    public boolean isPasswordStrong(String password) {
        return false;
    }

    @Override
    public String generateVerificationCode(String email) {
        return "";
    }

    @Override
    public boolean verifyCode(String email, String code) {
        return false;
    }

    // 私有輔助方法
    private void sendVerificationEmail(Member member) {
        String token = UUID.randomUUID().toString();
        member.setVerificationToken(token);
        member.setVerificationTokenExpiry(LocalDateTime.now().plusDays(7));
        memberRepository.save(member);

        emailService.sendVerificationEmail(member.getEmail(), token);
    }
}