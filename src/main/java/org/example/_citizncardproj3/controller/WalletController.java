package org.example._citizncardproj3.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example._citizncardproj3.model.dto.request.TopUpRequest;
import org.example._citizncardproj3.model.dto.request.TransferRequest;
import org.example._citizncardproj3.model.dto.response.ApiResponse;
import org.example._citizncardproj3.model.dto.response.TransactionResponse;
import org.example._citizncardproj3.model.dto.response.WalletResponse;
import org.example._citizncardproj3.service.WalletService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/wallets")
@RequiredArgsConstructor
@Tag(name = "電子錢包", description = "電子錢包相關API")
public class WalletController {

    private final WalletService walletService;

    @Operation(summary = "查詢錢包餘額")
    @GetMapping("/balance")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<WalletResponse> getWalletBalance(Authentication authentication) {
        try {
            String userEmail = authentication.getName();
            WalletResponse wallet = walletService.getWalletBalance(userEmail);
            return ResponseEntity.ok(wallet);
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(new WalletResponse(null, e.getMessage()));
        }
    }

    @Operation(summary = "儲值")
    @PostMapping("/top-up")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<TransactionResponse> topUp(
            @Valid @RequestBody TopUpRequest request,
            Authentication authentication) {
        try {
            String userEmail = authentication.getName();
            TransactionResponse transaction = walletService.topUp(userEmail, request);
            return ResponseEntity.ok(transaction);
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new TransactionResponse(null, e.getMessage()));
        }
    }

    @Operation(summary = "轉帳")
    @PostMapping("/transfer")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<TransactionResponse> transfer(
            @Valid @RequestBody TransferRequest request,
            Authentication authentication) {
        try {
            String userEmail = authentication.getName();
            TransactionResponse transaction = walletService.transfer(userEmail, request);
            return ResponseEntity.ok(transaction);
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new TransactionResponse(null, e.getMessage()));
        }
    }

    @Operation(summary = "查詢交易記錄")
    @GetMapping("/transactions")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Page<TransactionResponse>> getTransactions(
            Authentication authentication,
            @Parameter(description = "開始時間")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @Parameter(description = "結束時間")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
            Pageable pageable) {
        try {
            String userEmail = authentication.getName();
            Page<TransactionResponse> transactions = walletService.getTransactions(userEmail, startTime, endTime, pageable);
            return ResponseEntity.ok(transactions);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @Operation(summary = "查詢單筆交易詳情")
    @GetMapping("/transactions/{transactionId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<TransactionResponse> getTransaction(
            @Parameter(description = "交易ID") @PathVariable Long transactionId,
            Authentication authentication) {
        try {
            String userEmail = authentication.getName();
            TransactionResponse transaction = walletService.getTransaction(transactionId, userEmail);
            return ResponseEntity.ok(transaction);
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(new TransactionResponse(null, e.getMessage()));
        }
    }

    @Operation(summary = "凍結錢包")
    @PostMapping("/freeze")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> freezeWallet(
            @Parameter(description = "用戶Email") @RequestParam String userEmail,
            @Parameter(description = "凍結原因") @RequestParam String reason) {
        try {
            walletService.freezeWallet(userEmail, reason);
            return ResponseEntity.ok(new ApiResponse(true, "錢包已凍結"));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, e.getMessage()));
        }
    }

    @Operation(summary = "解凍錢包")
    @PostMapping("/unfreeze")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> unfreezeWallet(
            @Parameter(description = "用戶Email") @RequestParam String userEmail) {
        try {
            walletService.unfreezeWallet(userEmail);
            return ResponseEntity.ok(new ApiResponse(true, "錢包已解凍"));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, e.getMessage()));
        }
    }
}