package org.example._citizncardproj3.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example._citizncardproj3.model.dto.request.MemberUpdateRequest;
import org.example._citizncardproj3.model.dto.response.ApiResponse;
import org.example._citizncardproj3.model.dto.response.MemberResponse;
import org.example._citizncardproj3.service.MemberService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/v1/members")
@RequiredArgsConstructor
@Tag(name = "會員", description = "會員管理相關API")
public class MemberController {

    private final MemberService memberService;

    @Operation(summary = "獲取會員個人資料")
    @GetMapping("/profile")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<MemberResponse> getMemberProfile(Authentication authentication) {
        try {
            String userEmail = authentication.getName();
            MemberResponse profile = memberService.getMemberProfile(userEmail);
            return ResponseEntity.ok(profile);
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(new MemberResponse(null, e.getMessage()));
        }
    }

    @Operation(summary = "更新會員資料")
    @PutMapping("/profile")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<MemberResponse> updateMemberProfile(
            @Valid @RequestBody MemberUpdateRequest request,
            Authentication authentication) {
        try {
            String userEmail = authentication.getName();
            MemberResponse updatedProfile = memberService.updateMemberProfile(userEmail, request);
            return ResponseEntity.ok(updatedProfile);
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new MemberResponse(null, e.getMessage()));
        }
    }

    @Operation(summary = "更新會員頭像")
    @PostMapping("/profile/avatar")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse> updateAvatar(
            @Parameter(description = "頭像文件") @RequestParam("file") MultipartFile file,
            Authentication authentication) {
        try {
            String userEmail = authentication.getName();
            String avatarUrl = memberService.updateAvatar(userEmail, file);
            return ResponseEntity.ok(new ApiResponse(true, avatarUrl));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, e.getMessage()));
        }
    }

    @Operation(summary = "更改密碼")
    @PostMapping("/change-password")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse> changePassword(
            @Parameter(description = "舊密碼") @RequestParam String oldPassword,
            @Parameter(description = "新密碼") @RequestParam String newPassword,
            Authentication authentication) {
        try {
            String userEmail = authentication.getName();
            memberService.changePassword(userEmail, oldPassword, newPassword);
            return ResponseEntity.ok(new ApiResponse(true, "密碼更改成功"));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, e.getMessage()));
        }
    }

    @Operation(summary = "取得會員訂單歷史")
    @GetMapping("/orders")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> getMemberOrders(
            Authentication authentication,
            Pageable pageable) {
        try {
            String userEmail = authentication.getName();
            return ResponseEntity.ok(memberService.getMemberOrders(userEmail, pageable));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, e.getMessage()));
        }
    }

    @Operation(summary = "取得會員優惠使用紀錄")
    @GetMapping("/discount-history")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> getDiscountHistory(
            Authentication authentication,
            Pageable pageable) {
        try {
            String userEmail = authentication.getName();
            return ResponseEntity.ok(memberService.getDiscountHistory(userEmail, pageable));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, e.getMessage()));
        }
    }

    @Operation(summary = "停用會員帳號")
    @PostMapping("/deactivate")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse> deactivateAccount(
            Authentication authentication,
            @Parameter(description = "停用原因") @RequestParam(required = false) String reason) {
        try {
            String userEmail = authentication.getName();
            memberService.deactivateAccount(userEmail, reason);
            return ResponseEntity.ok(new ApiResponse(true, "帳號已停用"));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, e.getMessage()));
        }
    }

    // 管理員專用API
    @Operation(summary = "管理員查看所有會員")
    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<MemberResponse>> getAllMembers(Pageable pageable) {
        try {
            Page<MemberResponse> members = memberService.getAllMembers(pageable);
            return ResponseEntity.ok(members);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "管理員封鎖會員")
    @PostMapping("/admin/block/{memberId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> blockMember(
            @Parameter(description = "會員ID") @PathVariable Long memberId,
            @Parameter(description = "封鎖原因") @RequestParam String reason) {
        try {
            memberService.blockMember(memberId, reason);
            return ResponseEntity.ok(new ApiResponse(true, "會員已被封鎖"));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, e.getMessage()));
        }
    }
}