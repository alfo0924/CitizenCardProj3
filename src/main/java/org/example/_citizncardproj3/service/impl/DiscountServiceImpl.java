package org.example._citizncardproj3.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example._citizncardproj3.exception.CustomException;
import org.example._citizncardproj3.model.dto.request.DiscountCreateRequest;
import org.example._citizncardproj3.model.dto.request.DiscountUpdateRequest;
import org.example._citizncardproj3.model.dto.response.DiscountResponse;
import org.example._citizncardproj3.model.entity.Discount;
import org.example._citizncardproj3.model.entity.DiscountUsage;
import org.example._citizncardproj3.model.entity.Member;
import org.example._citizncardproj3.repository.DiscountRepository;
import org.example._citizncardproj3.repository.DiscountUsageRepository;
import org.example._citizncardproj3.repository.MemberRepository;
import org.example._citizncardproj3.service.DiscountService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class DiscountServiceImpl implements DiscountService {

    private final DiscountRepository discountRepository;
    private final DiscountUsageRepository discountUsageRepository;
    private final MemberRepository memberRepository;

    @Override
    public Page<Discount> getAllDiscounts(boolean activeOnly, Pageable pageable) {
        if (activeOnly) {
            return discountRepository.findByIsActiveTrueOrderByValidFromDesc(pageable);
        }
        return discountRepository.findAll(pageable);
    }

    @Override
    public List<Discount> getValidDiscounts(String userEmail) {
        Member member = memberRepository.findByEmail(userEmail)
                .orElseThrow(() -> new CustomException.MemberNotFoundException(userEmail));

        LocalDateTime now = LocalDateTime.now();
        List<Discount> validDiscounts = discountRepository.findValidDiscounts(now);

        // 過濾掉已達使用上限的優惠
        return validDiscounts.stream()
                .filter(discount -> isDiscountAvailableForMember(discount, member))
                .toList();
    }

    @Override
    @Transactional
    public Discount createDiscount(DiscountCreateRequest request) {
        validateDiscountRequest(request);

        Discount discount = Discount.builder()
                .discountCode(generateDiscountCode())
                .discountName(request.getDiscountName())
                .discountType(request.getDiscountType())
                .discountValue(request.getDiscountValue())
                .minPurchaseAmount(request.getMinPurchaseAmount())
                .maxDiscountAmount(request.getMaxDiscountAmount())
                .description(request.getDescription())
                .validFrom(request.getValidFrom())
                .validUntil(request.getValidUntil())
                .usageLimit(request.getUsageLimit())
                .usageCount(0)
                .isActive(true)
                .build();

        return discountRepository.save(discount);
    }

    @Override
    @Transactional
    public Discount updateDiscount(Long discountId, DiscountUpdateRequest request) {
        Discount discount = discountRepository.findById(discountId)
                .orElseThrow(() -> new CustomException.DiscountNotFoundException(discountId));

        if (!discount.isActive()) {
            throw new IllegalStateException("無法更新已停用的優惠");
        }

        if (request.getValidUntil() != null) {
            discount.extendValidity(request.getValidUntil());
        }

        if (request.getUsageLimit() != null) {
            discount.increaseUsageLimit(request.getUsageLimit());
        }

        if (request.getDescription() != null) {
            discount.setDescription(request.getDescription());
        }

        return discountRepository.save(discount);
    }

    @Override
    @Transactional
    public void deactivateDiscount(Long discountId) {
        Discount discount = discountRepository.findById(discountId)
                .orElseThrow(() -> new CustomException.DiscountNotFoundException(discountId));

        discount.deactivate();
        discountRepository.save(discount);
    }

    @Override
    @Transactional
    public DiscountUsage useDiscount(String userEmail, String discountCode, Double originalAmount) {
        Member member = memberRepository.findByEmail(userEmail)
                .orElseThrow(() -> new CustomException.MemberNotFoundException(userEmail));

        Discount discount = discountRepository.findByDiscountCode(discountCode)
                .orElseThrow(() -> new CustomException.DiscountNotFoundException(0L));

        if (!discount.isValid()) {
            throw new CustomException.DiscountExpiredException(discountCode);
        }

        if (!isDiscountAvailableForMember(discount, member)) {
            throw new CustomException.DiscountUsedException(discountCode);
        }

        if (!discount.isApplicable(originalAmount)) {
            throw new IllegalStateException("訂單金額未達到優惠使用門檻");
        }

        Double discountAmount = discount.calculateDiscount(originalAmount);
        Double finalAmount = originalAmount - discountAmount;

        DiscountUsage usage = DiscountUsage.builder()
                .member(member)
                .discount(discount)
                .originalAmount(originalAmount)
                .discountAmount(discountAmount)
                .finalAmount(finalAmount)
                .status(DiscountUsage.UsageStatus.USED)
                .usageTime(LocalDateTime.now())
                .build();

        usage = discountUsageRepository.save(usage);
        discount.use();
        discountRepository.save(discount);

        return usage;
    }

    @Override
    @Transactional
    public void cancelDiscountUsage(Long usageId) {
        DiscountUsage usage = discountUsageRepository.findById(usageId)
                .orElseThrow(() -> new IllegalArgumentException("優惠使用記錄不存在"));

        if (!usage.isCancellable()) {
            throw new IllegalStateException("此優惠使用記錄無法取消");
        }

        usage.cancel("用戶取消使用");
        discountUsageRepository.save(usage);
    }

    @Override
    public boolean isDiscountAvailable(String discountCode, String userEmail) {
        return false;
    }

    @Override
    public Double calculateDiscountAmount(String discountCode, Double originalAmount) {
        return 0.0;
    }

    @Override
    public Page<DiscountUsage> getDiscountUsageHistory(String userEmail, Pageable pageable) {
        return null;
    }

    @Override
    public long getDiscountUsageCount(String userEmail, String discountCode) {
        return 0;
    }

    @Override
    public Discount extendValidity(Long discountId, int days) {
        return null;
    }

    @Override
    public void deactivateExpiredDiscounts() {

    }

    @Override
    public Map<String, Object> getDiscountStatistics(Long discountId) {
        return Map.of();
    }

    @Override
    public boolean validateDiscountCode(String discountCode) {
        return false;
    }

    @Override
    public DiscountResponse getDiscount(Long discountId) {
        return null;
    }

    @Override
    public Page<DiscountResponse> getPublicDiscounts(Pageable pageable) {
        return null;
    }

    @Override
    public List<DiscountResponse> getAvailableDiscounts(String userEmail) {
        return List.of();
    }

    @Override
    public void deleteDiscount(Long discountId) {

    }

    @Override
    public void useDiscount(Long discountId, Long orderId, String userEmail) {

    }

    @Override
    public boolean validateDiscount(String discountCode) {
        return false;
    }

    @Override
    public Map<String, Object> getDiscountStatistics(LocalDateTime startDate, LocalDateTime endDate) {
        return Map.of();
    }

    // 私有輔助方法
    private void validateDiscountRequest(DiscountCreateRequest request) {
        if (request.getValidFrom().isAfter(request.getValidUntil())) {
            throw new IllegalArgumentException("結束時間必須晚於開始時間");
        }

        if (request.getMaxDiscountAmount() < 0 || request.getMinPurchaseAmount() < 0) {
            throw new IllegalArgumentException("金額不能為負數");
        }

        if (request.getDiscountType() == Discount.DiscountType.PERCENTAGE &&
                (request.getDiscountValue() <= 0 || request.getDiscountValue() > 100)) {
            throw new IllegalArgumentException("折扣百分比必須在0-100之間");
        }
    }

    private boolean isDiscountAvailableForMember(Discount discount, Member member) {
        if (!discount.isValid()) {
            return false;
        }

        long userUsageCount = discountUsageRepository.countMemberDiscountUsage(member, discount);
        return discount.getUsageLimit() == null || userUsageCount < discount.getUsageLimit();
    }

    private String generateDiscountCode() {
        return "DC" + System.currentTimeMillis();
    }
}