package org.example._citizncardproj3.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example._citizncardproj3.model.dto.request.DiscountCreateRequest;
import org.example._citizncardproj3.model.dto.request.DiscountUpdateRequest;
import org.example._citizncardproj3.model.dto.response.ApiResponse;
import org.example._citizncardproj3.model.dto.response.DiscountResponse;
import org.example._citizncardproj3.service.DiscountService;
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
import java.util.List;

@RestController
@RequestMapping("/api/v1/discounts")
@RequiredArgsConstructor
@Tag(name = "優惠", description = "優惠管理相關API")
public class DiscountController {

    private final DiscountService discountService;

    @Operation(summary = "創建新優惠")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DiscountResponse> createDiscount(
            @Valid @RequestBody DiscountCreateRequest request) {
        try {
            DiscountResponse discount = DiscountResponse.fromEntity(discountService.createDiscount(request));
            return ResponseEntity.ok(discount);
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new DiscountResponse(null, e.getMessage()));
        }
    }

    @Operation(summary = "獲取優惠詳情")
    @GetMapping("/{discountId}")
    public ResponseEntity<DiscountResponse> getDiscount(
            @Parameter(description = "優惠ID") @PathVariable Long discountId) {
        try {
            DiscountResponse discount = discountService.getDiscount(discountId);
            return ResponseEntity.ok(discount);
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(new DiscountResponse(null, e.getMessage()));
        }
    }

    @Operation(summary = "獲取所有公開優惠")
    @GetMapping("/public")
    public ResponseEntity<Page<DiscountResponse>> getPublicDiscounts(Pageable pageable) {
        Page<DiscountResponse> discounts = discountService.getPublicDiscounts(pageable);
        return ResponseEntity.ok(discounts);
    }

    @Operation(summary = "獲取用戶可用優惠")
    @GetMapping("/available")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<DiscountResponse>> getAvailableDiscounts(Authentication authentication) {
        String userEmail = authentication.getName();
        List<DiscountResponse> discounts = discountService.getAvailableDiscounts(userEmail);
        return ResponseEntity.ok(discounts);
    }

    @Operation(summary = "更新優惠信息")
    @PutMapping("/{discountId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DiscountResponse> updateDiscount(
            @Parameter(description = "優惠ID") @PathVariable Long discountId,
            @Valid @RequestBody DiscountUpdateRequest request) {
        try {
            DiscountResponse discount = DiscountResponse.fromEntity(discountService.updateDiscount(discountId, request));
            return ResponseEntity.ok(discount);
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new DiscountResponse(null, e.getMessage()));
        }
    }

    @Operation(summary = "刪除優惠")
    @DeleteMapping("/{discountId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> deleteDiscount(
            @Parameter(description = "優惠ID") @PathVariable Long discountId) {
        try {
            discountService.deleteDiscount(discountId);
            return ResponseEntity.ok(new ApiResponse(true, "優惠刪除成功"));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, e.getMessage()));
        }
    }

    @Operation(summary = "使用優惠")
    @PostMapping("/{discountId}/use")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse> useDiscount(
            @Parameter(description = "優惠ID") @PathVariable Long discountId,
            @Parameter(description = "訂單ID") @RequestParam Long orderId,
            Authentication authentication) {
        try {
            String userEmail = authentication.getName();
            discountService.useDiscount(discountId, orderId, userEmail);
            return ResponseEntity.ok(new ApiResponse(true, "優惠使用成功"));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, e.getMessage()));
        }
    }

    @Operation(summary = "檢查優惠有效性")
    @GetMapping("/{discountCode}/validate")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse> validateDiscount(
            @Parameter(description = "優惠碼") @PathVariable String discountCode) {
        try {
            boolean isValid = discountService.validateDiscount(discountCode);
            return ResponseEntity.ok(new ApiResponse(isValid,
                    isValid ? "優惠碼有效" : "優惠碼無效或已過期"));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, e.getMessage()));
        }
    }

    @Operation(summary = "獲取優惠使用統計")
    @GetMapping("/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getDiscountStatistics(
            @Parameter(description = "開始日期")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "結束日期")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        try {
            return ResponseEntity.ok(discountService.getDiscountStatistics(startDate, endDate));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, e.getMessage()));
        }
    }
}