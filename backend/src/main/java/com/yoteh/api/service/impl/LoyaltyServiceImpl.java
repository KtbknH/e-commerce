package com.yoteh.api.service.impl;

import com.yoteh.api.dto.request.AdjustPointsRequest;
import com.yoteh.api.dto.response.LoyaltyBalanceResponse;
import com.yoteh.api.dto.response.LoyaltyTransactionResponse;
import com.yoteh.api.dto.response.common.PagedResponse;
import com.yoteh.api.entity.LoyaltyTransaction;
import com.yoteh.api.entity.Order;
import com.yoteh.api.entity.User;
import com.yoteh.api.entity.enums.LoyaltyLevel;
import com.yoteh.api.exception.BadRequestException;
import com.yoteh.api.exception.ResourceNotFoundException;
import com.yoteh.api.mapper.LoyaltyTransactionMapper;
import com.yoteh.api.repository.LoyaltyTransactionRepository;
import com.yoteh.api.repository.OrderRepository;
import com.yoteh.api.repository.UserRepository;
import com.yoteh.api.service.LoyaltyService;
import com.yoteh.api.util.Constants;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoyaltyServiceImpl implements LoyaltyService {

    private static final int POINTS_PER_1000_XOF = 1;
    private static final int SILVER_THRESHOLD = 500;
    private static final int GOLD_THRESHOLD = 2000;
    private static final int PLATINUM_THRESHOLD = 5000;

    private final LoyaltyTransactionRepository loyaltyTransactionRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final LoyaltyTransactionMapper loyaltyTransactionMapper;

    // ═══════════════════════════════════════════════════════════
    // CLIENT
    // ═══════════════════════════════════════════════════════════

    @Override
    public LoyaltyBalanceResponse getMyBalance(UUID userId) {
        User user = findUserOrThrow(userId);
        return buildBalanceResponse(user);
    }

    @Override
    public PagedResponse<LoyaltyTransactionResponse> getMyHistory(
            UUID userId, int page, int size, String type) {

        findUserOrThrow(userId);
        size = Math.min(size, Constants.MAX_PAGE_SIZE);
        Pageable pageable = PageRequest.of(page, size);

        Page<LoyaltyTransaction> pageResult;
        if (type != null && !type.isBlank()) {
            pageResult =
                    loyaltyTransactionRepository.findByUserIdAndType(
                            userId, type.toUpperCase(), pageable);
        } else {
            pageResult =
                    loyaltyTransactionRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        }

        List<LoyaltyTransactionResponse> list =
                loyaltyTransactionMapper.toResponseList(pageResult.getContent());

        return PagedResponse.of(
                list,
                pageResult.getNumber(),
                pageResult.getSize(),
                pageResult.getTotalElements(),
                pageResult.getTotalPages(),
                pageResult.isLast());
    }

    // ═══════════════════════════════════════════════════════════
    // LOGIQUE MÉTIER
    // ═══════════════════════════════════════════════════════════

    @Override
    @Transactional
    public void earnPointsFromOrder(UUID userId, UUID orderId, BigDecimal orderTotal) {
        User user = findUserOrThrow(userId);

        // Vérifier que les points n'ont pas déjà été attribués pour cette commande
        if (loyaltyTransactionRepository.existsByOrderIdAndType(orderId, "EARN")) {
            log.warn("Points déjà attribués pour la commande : {}", orderId);
            return;
        }

        Order order =
                orderRepository
                        .findById(orderId)
                        .orElseThrow(
                                () ->
                                        new ResourceNotFoundException(
                                                "Commande", "id", orderId.toString()));

        // Calcul : 1 point par tranche de 1000 XOF
        int earnedPoints =
                orderTotal.divide(BigDecimal.valueOf(1000), 0, RoundingMode.DOWN).intValue()
                        * POINTS_PER_1000_XOF;

        if (earnedPoints <= 0) {
            log.info("Montant insuffisant pour gagner des points (commande {})", orderId);
            return;
        }

        int newBalance = user.getLoyaltyPoints() + earnedPoints;

        // Créer la transaction
        LoyaltyTransaction transaction = new LoyaltyTransaction();
        transaction.setUser(user);
        transaction.setOrder(order);
        transaction.setType("EARN");
        transaction.setPoints(earnedPoints);
        transaction.setBalanceAfter(newBalance);
        transaction.setDescription("Points gagnés sur commande " + order.getOrderNumber());
        transaction.setReference(order.getOrderNumber());
        loyaltyTransactionRepository.save(transaction);

        // Mettre à jour le solde utilisateur
        user.setLoyaltyPoints(newBalance);
        updateLoyaltyLevel(user);
        userRepository.save(user);

        log.info(
                "Utilisateur {} : +{} points (commande {}), solde = {}",
                userId,
                earnedPoints,
                orderId,
                newBalance);
    }

    @Override
    @Transactional
    public void redeemPoints(UUID userId, int points, String description, String reference) {
        if (points <= 0) {
            throw new BadRequestException("Le nombre de points doit être positif");
        }

        User user = findUserOrThrow(userId);

        if (user.getLoyaltyPoints() < points) {
            throw new BadRequestException(
                    "Solde insuffisant. Disponible : "
                            + user.getLoyaltyPoints()
                            + ", demandé : "
                            + points);
        }

        int newBalance = user.getLoyaltyPoints() - points;

        LoyaltyTransaction transaction = new LoyaltyTransaction();
        transaction.setUser(user);
        transaction.setType("REDEEM");
        transaction.setPoints(-points);
        transaction.setBalanceAfter(newBalance);
        transaction.setDescription(description != null ? description : "Utilisation de points");
        transaction.setReference(reference);
        loyaltyTransactionRepository.save(transaction);

        user.setLoyaltyPoints(newBalance);
        updateLoyaltyLevel(user);
        userRepository.save(user);

        log.info(
                "Utilisateur {} : -{} points (utilisation), solde = {}",
                userId,
                points,
                newBalance);
    }

    // ═══════════════════════════════════════════════════════════
    // ADMIN
    // ═══════════════════════════════════════════════════════════

    @Override
    public PagedResponse<LoyaltyTransactionResponse> getAllTransactions(
            int page, int size, String type) {

        size = Math.min(size, Constants.MAX_PAGE_SIZE);
        Pageable pageable = PageRequest.of(page, size);

        Page<LoyaltyTransaction> pageResult;
        if (type != null && !type.isBlank()) {
            pageResult = loyaltyTransactionRepository.findByType(type.toUpperCase(), pageable);
        } else {
            pageResult = loyaltyTransactionRepository.findAllOrderByCreatedAtDesc(pageable);
        }

        List<LoyaltyTransactionResponse> list =
                loyaltyTransactionMapper.toResponseList(pageResult.getContent());

        return PagedResponse.of(
                list,
                pageResult.getNumber(),
                pageResult.getSize(),
                pageResult.getTotalElements(),
                pageResult.getTotalPages(),
                pageResult.isLast());
    }

    @Override
    @Transactional
    public LoyaltyTransactionResponse adjustPoints(UUID userId, AdjustPointsRequest request) {
        User user = findUserOrThrow(userId);

        int adjustedPoints = request.getPoints();
        int newBalance = user.getLoyaltyPoints() + adjustedPoints;

        if (newBalance < 0) {
            throw new BadRequestException(
                    "L'ajustement rendrait le solde négatif. Solde actuel : "
                            + user.getLoyaltyPoints());
        }

        LoyaltyTransaction transaction = new LoyaltyTransaction();
        transaction.setUser(user);
        transaction.setType("ADJUST");
        transaction.setPoints(adjustedPoints);
        transaction.setBalanceAfter(newBalance);
        transaction.setDescription(request.getDescription());
        transaction.setReference(request.getReference());
        loyaltyTransactionRepository.save(transaction);

        user.setLoyaltyPoints(newBalance);
        updateLoyaltyLevel(user);
        userRepository.save(user);

        log.info(
                "Admin : ajustement de {} points pour l'utilisateur {}, solde = {}",
                adjustedPoints,
                userId,
                newBalance);

        return loyaltyTransactionMapper.toResponse(transaction);
    }

    @Override
    public LoyaltyBalanceResponse getUserBalance(UUID userId) {
        User user = findUserOrThrow(userId);
        return buildBalanceResponse(user);
    }

    // ═══════════════════════════════════════════════════════════
    // HELPERS
    // ═══════════════════════════════════════════════════════════

    private User findUserOrThrow(UUID userId) {
        return userRepository
                .findById(userId)
                .orElseThrow(
                        () ->
                                new ResourceNotFoundException(
                                        "Utilisateur", "id", userId.toString()));
    }

    private void updateLoyaltyLevel(User user) {
        int totalEarned = loyaltyTransactionRepository.sumEarnedPointsByUserId(user.getId());
        LoyaltyLevel newLevel;

        if (totalEarned >= PLATINUM_THRESHOLD) {
            newLevel = LoyaltyLevel.PLATINUM;
        } else if (totalEarned >= GOLD_THRESHOLD) {
            newLevel = LoyaltyLevel.GOLD;
        } else if (totalEarned >= SILVER_THRESHOLD) {
            newLevel = LoyaltyLevel.SILVER;
        } else {
            newLevel = LoyaltyLevel.BRONZE;
        }

        if (user.getLoyaltyLevel() != newLevel) {
            log.info(
                    "Utilisateur {} : niveau fidélité {} → {}",
                    user.getId(),
                    user.getLoyaltyLevel(),
                    newLevel);
            user.setLoyaltyLevel(newLevel);
        }
    }

    private LoyaltyBalanceResponse buildBalanceResponse(User user) {
        int totalEarned = loyaltyTransactionRepository.sumEarnedPointsByUserId(user.getId());
        int totalRedeemed =
                Math.abs(loyaltyTransactionRepository.sumRedeemedPointsByUserId(user.getId()));

        LoyaltyLevel currentLevel = user.getLoyaltyLevel();
        String nextLevel = null;
        int pointsToNext = 0;

        switch (currentLevel) {
            case BRONZE:
                nextLevel = "SILVER";
                pointsToNext = Math.max(0, SILVER_THRESHOLD - totalEarned);
                break;
            case SILVER:
                nextLevel = "GOLD";
                pointsToNext = Math.max(0, GOLD_THRESHOLD - totalEarned);
                break;
            case GOLD:
                nextLevel = "PLATINUM";
                pointsToNext = Math.max(0, PLATINUM_THRESHOLD - totalEarned);
                break;
            case PLATINUM:
                nextLevel = null;
                pointsToNext = 0;
                break;
        }

        return LoyaltyBalanceResponse.builder()
                .currentPoints(user.getLoyaltyPoints())
                .loyaltyLevel(currentLevel.name())
                .totalEarned(totalEarned)
                .totalRedeemed(totalRedeemed)
                .pointsToNextLevel(pointsToNext)
                .nextLevel(nextLevel)
                .build();
    }
}
