package com.yoteh.api.service.impl;

import com.yoteh.api.dto.request.ApplyPromoRequest;
import com.yoteh.api.dto.request.PromotionRequest;
import com.yoteh.api.dto.response.ApplyPromoResponse;
import com.yoteh.api.dto.response.PromotionResponse;
import com.yoteh.api.dto.response.PromotionStatsResponse;
import com.yoteh.api.dto.response.common.PagedResponse;
import com.yoteh.api.entity.Category;
import com.yoteh.api.entity.Product;
import com.yoteh.api.entity.Promotion;
import com.yoteh.api.entity.enums.PromotionType;
import com.yoteh.api.exception.BadRequestException;
import com.yoteh.api.exception.DuplicateResourceException;
import com.yoteh.api.exception.ResourceNotFoundException;
import com.yoteh.api.mapper.PromotionMapper;
import com.yoteh.api.repository.CategoryRepository;
import com.yoteh.api.repository.ProductRepository;
import com.yoteh.api.repository.PromotionRepository;
import com.yoteh.api.service.PromotionService;
import com.yoteh.api.util.Constants;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PromotionServiceImpl implements PromotionService {

    private final PromotionRepository promotionRepository;
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final PromotionMapper promotionMapper;

    // ── CRUD ──────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public PromotionResponse createPromotion(PromotionRequest request) {
        String code = normalizeCode(request.getCode());
        request.setCode(code);

        if (promotionRepository.existsByCodeAndDeletedAtIsNull(code)) {
            throw new DuplicateResourceException("Promotion", "code", code);
        }

        validateDates(request.getStartsAt(), request.getEndsAt());
        validateValueForType(request.getType(), request.getValue());

        Promotion promotion = promotionMapper.toEntity(request);
        promotion.setUsedCount(0);

        resolveAssociations(
                promotion, request.getApplicableCategoryId(), request.getApplicableProductId());

        Promotion saved = promotionRepository.save(promotion);
        log.info("Promotion créée : code={}, type={}", saved.getCode(), saved.getType());
        return promotionMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public PromotionResponse updatePromotion(UUID id, PromotionRequest request) {
        Promotion promotion = findByIdOrThrow(id);

        String code = normalizeCode(request.getCode());
        request.setCode(code);

        if (promotionRepository.existsByCodeAndIdNotAndDeletedAtIsNull(code, id)) {
            throw new DuplicateResourceException("Promotion", "code", code);
        }

        validateDates(request.getStartsAt(), request.getEndsAt());
        validateValueForType(request.getType(), request.getValue());

        promotionMapper.updateFromRequest(request, promotion);
        resolveAssociations(
                promotion, request.getApplicableCategoryId(), request.getApplicableProductId());

        Promotion saved = promotionRepository.save(promotion);
        log.info("Promotion mise à jour : id={}, code={}", id, saved.getCode());
        return promotionMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void deletePromotion(UUID id) {
        Promotion promotion = findByIdOrThrow(id);
        promotion.setDeletedAt(LocalDateTime.now());
        promotion.setIsActive(false);
        promotionRepository.save(promotion);
        log.info("Promotion supprimée (soft-delete) : id={}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public PromotionResponse getPromotionById(UUID id) {
        return promotionMapper.toResponse(findByIdOrThrow(id));
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<PromotionResponse> getAllPromotions(
            int page, int size, Boolean isActive, Boolean isFlashSale, String search) {

        size = Math.min(size, Constants.MAX_PAGE_SIZE);
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<Promotion> promotionPage =
                promotionRepository.findAllWithFilters(isActive, isFlashSale, search, pageable);

        List<PromotionResponse> list =
                promotionPage.getContent().stream()
                        .map(promotionMapper::toResponse)
                        .collect(Collectors.toList());

        return PagedResponse.of(
                list,
                promotionPage.getNumber(),
                promotionPage.getSize(),
                promotionPage.getTotalElements(),
                promotionPage.getTotalPages(),
                promotionPage.isLast());
    }

    // ── Endpoints publics ──────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<PromotionResponse> getActiveFlashSales() {
        return promotionRepository.findActiveFlashSales(LocalDateTime.now()).stream()
                .map(promotionMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ApplyPromoResponse applyPromoCode(ApplyPromoRequest request) {
        String code = normalizeCode(request.getCode());
        BigDecimal orderAmount = request.getOrderAmount();

        Promotion promotion =
                promotionRepository
                        .findByCodeAndDeletedAtIsNull(code)
                        .orElseThrow(
                                () -> new BadRequestException("Code promo invalide : " + code));

        // Vérifications successives
        checkIsActive(promotion);
        checkDates(promotion);
        checkUsageLimit(promotion);
        checkMinOrderAmount(promotion, orderAmount);

        BigDecimal discountAmount = calculateDiscount(promotion, orderAmount);

        // Incrément atomique — marque le code comme utilisé
        promotionRepository.incrementUsedCount(promotion.getId());
        log.info(
                "Code promo appliqué : code={}, orderAmount={}, discount={}",
                code,
                orderAmount,
                discountAmount);

        return buildApplyResponse(promotion, orderAmount, discountAmount);
    }

    // ── Stats ──────────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public PromotionStatsResponse getPromotionStats(UUID id) {
        Promotion promotion = findByIdOrThrow(id);

        PromotionStatsResponse stats = new PromotionStatsResponse();
        stats.setPromotionId(promotion.getId());
        stats.setCode(promotion.getCode());
        stats.setName(promotion.getName());
        stats.setUsedCount(promotion.getUsedCount());
        stats.setMaxUses(promotion.getMaxUses());
        stats.setActive(Boolean.TRUE.equals(promotion.getIsActive()));
        stats.setFlashSale(Boolean.TRUE.equals(promotion.getIsFlashSale()));

        if (promotion.getMaxUses() != null && promotion.getMaxUses() > 0) {
            double rate = (double) promotion.getUsedCount() / promotion.getMaxUses() * 100.0;
            stats.setUsageRate(Math.round(rate * 100.0) / 100.0);
        } else {
            stats.setUsageRate(null); // illimité
        }

        return stats;
    }

    // ── Helpers privés ────────────────────────────────────────────────────────

    private Promotion findByIdOrThrow(UUID id) {
        return promotionRepository
                .findById(id)
                .filter(p -> p.getDeletedAt() == null)
                .orElseThrow(() -> new ResourceNotFoundException("Promotion", "id", id));
    }

    private String normalizeCode(String code) {
        return code.toUpperCase().trim();
    }

    private void validateDates(LocalDateTime startsAt, LocalDateTime endsAt) {
        if (!endsAt.isAfter(startsAt)) {
            throw new BadRequestException(
                    "La date de fin doit être postérieure à la date de début");
        }
    }

    private void validateValueForType(PromotionType type, BigDecimal value) {
        if (type == PromotionType.PERCENTAGE) {
            if (value.compareTo(BigDecimal.ZERO) <= 0
                    || value.compareTo(new BigDecimal("100")) > 0) {
                throw new BadRequestException("Le pourcentage doit être compris entre 0 et 100");
            }
        } else if (type == PromotionType.FIXED_AMOUNT) {
            if (value.compareTo(BigDecimal.ZERO) <= 0) {
                throw new BadRequestException("Le montant de réduction doit être positif");
            }
        }
    }

    private void resolveAssociations(Promotion promotion, UUID categoryId, UUID productId) {
        if (categoryId != null) {
            Category category =
                    categoryRepository
                            .findById(categoryId)
                            .orElseThrow(
                                    () ->
                                            new ResourceNotFoundException(
                                                    "Catégorie", "id", categoryId));
            promotion.setApplicableCategory(category);
        } else {
            promotion.setApplicableCategory(null);
        }

        if (productId != null) {
            Product product =
                    productRepository
                            .findById(productId)
                            .orElseThrow(
                                    () ->
                                            new ResourceNotFoundException(
                                                    "Produit", "id", productId));
            promotion.setApplicableProduct(product);
        } else {
            promotion.setApplicableProduct(null);
        }
    }

    private void checkIsActive(Promotion promotion) {
        if (!Boolean.TRUE.equals(promotion.getIsActive())) {
            throw new BadRequestException("Ce code promo n'est plus actif");
        }
    }

    private void checkDates(Promotion promotion) {
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(promotion.getStartsAt())) {
            throw new BadRequestException("Ce code promo n'est pas encore valide");
        }
        if (now.isAfter(promotion.getEndsAt())) {
            throw new BadRequestException("Ce code promo a expiré");
        }
    }

    private void checkUsageLimit(Promotion promotion) {
        if (promotion.getMaxUses() != null && promotion.getUsedCount() >= promotion.getMaxUses()) {
            throw new BadRequestException("Ce code promo a atteint sa limite d'utilisation");
        }
    }

    private void checkMinOrderAmount(Promotion promotion, BigDecimal orderAmount) {
        if (promotion.getMinOrderAmount() != null
                && orderAmount.compareTo(promotion.getMinOrderAmount()) < 0) {
            throw new BadRequestException(
                    "Montant minimum requis : " + promotion.getMinOrderAmount() + " XOF");
        }
    }

    /**
     * Calcule la remise selon le type de promotion.
     *
     * <p>PERCENTAGE : value% du montant, plafonné par maxDiscountAmount si défini. FIXED_AMOUNT :
     * montant fixe, plafonné au total de la commande. FREE_SHIPPING : pas de remise monétaire
     * (gérée au niveau des frais de livraison). BUY_X_GET_Y : value% de remise appliqué (logique
     * simplifiée sans SKU dédié).
     */
    private BigDecimal calculateDiscount(Promotion promotion, BigDecimal orderAmount) {
        return switch (promotion.getType()) {
            case PERCENTAGE -> {
                BigDecimal pct =
                        orderAmount
                                .multiply(promotion.getValue())
                                .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
                if (promotion.getMaxDiscountAmount() != null) {
                    pct = pct.min(promotion.getMaxDiscountAmount());
                }
                yield pct;
            }
            case FIXED_AMOUNT -> promotion.getValue().min(orderAmount);
            case FREE_SHIPPING -> BigDecimal.ZERO;
            case BUY_X_GET_Y ->
                    orderAmount
                            .multiply(promotion.getValue())
                            .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        };
    }

    private ApplyPromoResponse buildApplyResponse(
            Promotion promotion, BigDecimal orderAmount, BigDecimal discountAmount) {

        ApplyPromoResponse resp = new ApplyPromoResponse();
        resp.setCode(promotion.getCode());
        resp.setPromotionName(promotion.getName());
        resp.setType(promotion.getType());
        resp.setOriginalAmount(orderAmount);
        resp.setDiscountAmount(discountAmount);
        resp.setFinalAmount(orderAmount.subtract(discountAmount).max(BigDecimal.ZERO));
        resp.setFreeShipping(promotion.getType() == PromotionType.FREE_SHIPPING);

        String desc =
                switch (promotion.getType()) {
                    case PERCENTAGE ->
                            promotion.getValue().stripTrailingZeros().toPlainString()
                                    + "% de réduction";
                    case FIXED_AMOUNT ->
                            promotion.getValue().stripTrailingZeros().toPlainString()
                                    + " XOF de réduction";
                    case FREE_SHIPPING -> "Livraison gratuite";
                    case BUY_X_GET_Y -> "Offre spéciale : " + promotion.getName();
                };
        resp.setDiscountDescription(desc);

        return resp;
    }
}
