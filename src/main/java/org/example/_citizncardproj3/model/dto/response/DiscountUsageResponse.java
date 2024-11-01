package org.example._citizncardproj3.model.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example._citizncardproj3.model.entity.Discount;
import org.example._citizncardproj3.model.entity.DiscountUsage;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DiscountUsageResponse {
    private Long usageId;
    private String discountCode;
    private String discountName;
    private Discount.DiscountType discountType;
    private Double discountValue;
    private Double minPurchaseAmount;
    private Double maxDiscountAmount;
    private String memberName;
    private String memberEmail;
    private Double originalAmount;
    private Double discountedAmount;
    private Double finalAmount;
    private String orderNumber;
    private String orderType;
    private DiscountUsage.UsageStatus status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime usageTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime expiryTime;

    private UsageDetails details;
    private String message;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UsageDetails {
        private String orderType;
        private String orderDescription;
        private String usageLocation;
        private String deviceInfo;
        private String operatorId;
        private String additionalInfo;
    }

    // 建構子 - 用於一般響應
    public static DiscountUsageResponse fromEntity(DiscountUsage usage) {
        Discount discount = usage.getDiscount();
        return DiscountUsageResponse.builder()
                .usageId(usage.getUsageId())
                .discountCode(discount.getDiscountCode())
                .discountName(discount.getDiscountName())
                .discountType(discount.getDiscountType())
                .discountValue(discount.getDiscountValue())
                .minPurchaseAmount(discount.getMinPurchaseAmount())
                .maxDiscountAmount(discount.getMaxDiscountAmount())
                .memberName(usage.getMember().getName())
                .memberEmail(usage.getMember().getEmail())
                .originalAmount(usage.getOriginalAmount())
                .discountedAmount(usage.getDiscountAmount())
                .finalAmount(usage.getFinalAmount())
                .orderNumber(usage.getOrderNumber())
                .orderType(usage.getOrderType())
                .status(usage.getStatus())
                .usageTime(usage.getUsageTime())
                .expiryTime(discount.getValidUntil())
                .details(UsageDetails.builder()
                        .orderType(usage.getOrderType())
                        .orderDescription(usage.getOrderDescription())
                        .usageLocation(usage.getUsageLocation())
                        .deviceInfo(usage.getDeviceInfo())
                        .operatorId(usage.getOperatorId())
                        .additionalInfo(usage.getAdditionalInfo())
                        .build())
                .build();
    }

    // 建構子 - 用於錯誤響應
    public DiscountUsageResponse(DiscountUsage usage, String message) {
        if (usage != null) {
            Discount discount = usage.getDiscount();
            this.usageId = usage.getUsageId();
            this.discountCode = discount.getDiscountCode();
            this.discountName = discount.getDiscountName();
            this.discountType = discount.getDiscountType();
            this.discountValue = discount.getDiscountValue();
            this.status = usage.getStatus();
            this.usageTime = usage.getUsageTime();
        }
        this.message = message;
    }

    // 檢查優惠是否已過期
    public boolean isExpired() {
        return expiryTime != null && LocalDateTime.now().isAfter(expiryTime);
    }

    // 獲取優惠描述
    public String getDiscountDescription() {
        if (discountType == Discount.DiscountType.PERCENTAGE) {
            return String.format("%.0f%% 折扣", discountValue);
        } else {
            return String.format("折抵 $%.0f", discountValue);
        }
    }

    // 獲取狀態描述
    public String getStatusDescription() {
        return status != null ? status.getDescription() : "未知狀態";
    }

    // 用於日誌記錄的方法
    public String toLogString() {
        return String.format("DiscountUsage{id=%d, code='%s', order='%s', amount=%.2f, status=%s}",
                usageId, discountCode, orderNumber, finalAmount, status);
    }
}