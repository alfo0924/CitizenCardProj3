package org.example._citizncardproj3.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example._citizncardproj3.model.dto.request.LoginRequest;
import org.example._citizncardproj3.model.dto.request.RegisterRequest;
import org.example._citizncardproj3.model.dto.response.ApiResponse;
import org.example._citizncardproj3.model.dto.response.JwtAuthResponse;
import org.example._citizncardproj3.security.JwtTokenProvider;
import org.example._citizncardproj3.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "認證", description = "認證相關API")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthService authService;

    @Operation(summary = "用戶註冊")
    @PostMapping("/register")
    public ResponseEntity<ApiResponse> register(@Valid @RequestBody RegisterRequest request) {
        try {
            authService.register(request);
            return ResponseEntity.ok(new ApiResponse(true, "註冊成功"));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, e.getMessage()));
        }
    }

    @Operation(summary = "用戶登入")
    @PostMapping("/login")
    public ResponseEntity<JwtAuthResponse> login(@Valid @RequestBody LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = jwtTokenProvider.createToken(authentication.getName());

            return ResponseEntity.ok(new JwtAuthResponse(jwt));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(new JwtAuthResponse(null, "登入失敗：" + e.getMessage()));
        }
    }

    @Operation(summary = "刷新Token")
    @PostMapping("/refresh")
    public ResponseEntity<JwtAuthResponse> refreshToken(HttpServletRequest request) {
        String token = jwtTokenProvider.resolveToken(request);
        if (token != null && jwtTokenProvider.validateToken(token)) {
            String username = jwtTokenProvider.getUsername(token);
            String newToken = jwtTokenProvider.createToken(username);
            return ResponseEntity.ok(new JwtAuthResponse(newToken));
        }
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(new JwtAuthResponse(null, "Token刷新失敗"));
    }

    @Operation(summary = "登出")
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse> logout(HttpServletRequest request) {
        try {
            SecurityContextHolder.clearContext();
            return ResponseEntity.ok(new ApiResponse(true, "登出成功"));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "登出失敗：" + e.getMessage()));
        }
    }

    @Operation(summary = "檢查Token有效性")
    @GetMapping("/check-token")
    public ResponseEntity<ApiResponse> checkToken(HttpServletRequest request) {
        String token = jwtTokenProvider.resolveToken(request);
        if (token != null && jwtTokenProvider.validateToken(token)) {
            return ResponseEntity.ok(new ApiResponse(true, "Token有效"));
        }
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(new ApiResponse(false, "Token無效或已過期"));
    }

    @Operation(summary = "重設密碼請求")
    @PostMapping("/reset-password-request")
    public ResponseEntity<ApiResponse> resetPasswordRequest(@RequestParam String email) {
        try {
            authService.initiatePasswordReset(email);
            return ResponseEntity.ok(new ApiResponse(true, "密碼重設郵件已發送"));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, e.getMessage()));
        }
    }

    @Operation(summary = "重設密碼")
    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse> resetPassword(
            @RequestParam String token,
            @RequestParam String newPassword) {
        try {
            authService.resetPassword(token, newPassword);
            return ResponseEntity.ok(new ApiResponse(true, "密碼重設成功"));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, e.getMessage()));
        }
    }
}