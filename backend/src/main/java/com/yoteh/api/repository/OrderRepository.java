package com.yoteh.api.repository;

import com.yoteh.api.entity.Order;
import com.yoteh.api.entity.enums.OrderStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {

    // ─── Par numéro de commande ─────────────────────────────
    @Query(
            "SELECT o FROM Order o LEFT JOIN FETCH o.items i "
                    + "LEFT JOIN FETCH i.product LEFT JOIN FETCH i.variant "
                    + "LEFT JOIN FETCH o.user LEFT JOIN FETCH o.shippingAddress "
                    + "LEFT JOIN FETCH o.billingAddress "
                    + "WHERE o.orderNumber = :orderNumber")
    Optional<Order> findByOrderNumberWithDetails(@Param("orderNumber") String orderNumber);

    Optional<Order> findByOrderNumber(String orderNumber);

    // ─── Par ID avec détails ────────────────────────────────
    @Query(
            "SELECT o FROM Order o LEFT JOIN FETCH o.items i "
                    + "LEFT JOIN FETCH i.product LEFT JOIN FETCH i.variant "
                    + "LEFT JOIN FETCH o.user LEFT JOIN FETCH o.shippingAddress "
                    + "LEFT JOIN FETCH o.billingAddress "
                    + "WHERE o.id = :id")
    Optional<Order> findByIdWithDetails(@Param("id") UUID id);

    // ─── Commandes utilisateur ──────────────────────────────
    Page<Order> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    @Query(
            "SELECT o FROM Order o WHERE o.user.id = :userId "
                    + "AND (:status IS NULL OR o.status = :status) "
                    + "ORDER BY o.createdAt DESC")
    Page<Order> findByUserIdAndStatus(
            @Param("userId") UUID userId, @Param("status") OrderStatus status, Pageable pageable);

    // ─── Admin : toutes les commandes avec filtres ──────────
    @Query(
            "SELECT o FROM Order o JOIN o.user u WHERE "
                    + "(:status IS NULL OR o.status = :status) "
                    + "AND (:search IS NULL OR LOWER(o.orderNumber) LIKE LOWER(CONCAT('%', :search, '%')) "
                    + "    OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :search, '%')) "
                    + "    OR LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%'))) "
                    + "AND (:from IS NULL OR o.createdAt >= :from) "
                    + "AND (:to IS NULL OR o.createdAt <= :to)")
    Page<Order> findAllWithFilters(
            @Param("status") OrderStatus status,
            @Param("search") String search,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
            Pageable pageable);

    @Query(
            "SELECT o FROM Order o WHERE o.deletedAt IS NULL "
                    + "AND (:status IS NULL OR o.status = :status) "
                    + "AND (:userId IS NULL OR o.user.id = :userId) "
                    + "AND (:from IS NULL OR o.createdAt >= :from) "
                    + "AND (:to IS NULL OR o.createdAt <= :to) "
                    + "ORDER BY o.createdAt DESC")
    Page<Order> findAllFiltered(
            @Param("status") OrderStatus status,
            @Param("userId") UUID userId,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
            Pageable pageable);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.deletedAt IS NULL AND o.status = :status")
    long countByStatus(@Param("status") OrderStatus status);

    @Query(
            "SELECT COALESCE(SUM(o.total), 0) FROM Order o WHERE o.deletedAt IS NULL "
                    + "AND o.status IN ('PAID', 'PREPARING', 'SHIPPED', 'DELIVERED') "
                    + "AND o.createdAt >= :from AND o.createdAt <= :to")
    BigDecimal sumRevenueForPeriod(
            @Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    @Query(
            "SELECT COUNT(o) FROM Order o WHERE o.deletedAt IS NULL "
                    + "AND o.createdAt >= :from AND o.createdAt <= :to")
    long countForPeriod(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    long countByUserId(UUID userId);

    // Vérifie si l'utilisateur a une commande DELIVERED contenant ce produit
    // Utilisé pour le flag isVerifiedPurchase des avis
    @Query(
            "SELECT CASE WHEN COUNT(o) > 0 THEN true ELSE false END "
                    + "FROM Order o JOIN o.items oi "
                    + "WHERE o.user.id = :userId AND oi.product.id = :productId "
                    + "AND o.status = :status AND o.deletedAt IS NULL")
    boolean existsDeliveredOrderForProduct(
            @Param("userId") UUID userId,
            @Param("productId") UUID productId,
            @Param("status") OrderStatus status);

    // ─── Compteurs ──────────────────────────────────────────
    @Query("SELECT COUNT(o) FROM Order o WHERE o.user.id = :userId AND o.status = :status")
    long countByUserIdAndStatus(@Param("userId") UUID userId, @Param("status") OrderStatus status);

    boolean existsByOrderNumber(String orderNumber);

    @Query(
            "SELECT COALESCE(SUM(o.discountAmount), 0) FROM Order o "
                    + "WHERE o.deletedAt IS NULL "
                    + "AND o.status IN (com.yoteh.api.entity.enums.OrderStatus.PAID, "
                    + "com.yoteh.api.entity.enums.OrderStatus.PREPARING, "
                    + "com.yoteh.api.entity.enums.OrderStatus.SHIPPED, "
                    + "com.yoteh.api.entity.enums.OrderStatus.DELIVERED) "
                    + "AND o.createdAt >= :from AND o.createdAt <= :to")
    BigDecimal sumDiscountForPeriod(
            @Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    @Query(
            "SELECT COALESCE(SUM(o.shippingAmount), 0) FROM Order o "
                    + "WHERE o.deletedAt IS NULL "
                    + "AND o.status IN (com.yoteh.api.entity.enums.OrderStatus.PAID, "
                    + "com.yoteh.api.entity.enums.OrderStatus.PREPARING, "
                    + "com.yoteh.api.entity.enums.OrderStatus.SHIPPED, "
                    + "com.yoteh.api.entity.enums.OrderStatus.DELIVERED) "
                    + "AND o.createdAt >= :from AND o.createdAt <= :to")
    BigDecimal sumShippingForPeriod(
            @Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    @Query(
            "SELECT CAST(o.createdAt AS LocalDate) AS orderDate, "
                    + "COALESCE(SUM(o.total), 0) AS revenue, "
                    + "COUNT(o) AS orderCount "
                    + "FROM Order o WHERE o.deletedAt IS NULL "
                    + "AND o.status IN (com.yoteh.api.entity.enums.OrderStatus.PAID, "
                    + "com.yoteh.api.entity.enums.OrderStatus.PREPARING, "
                    + "com.yoteh.api.entity.enums.OrderStatus.SHIPPED, "
                    + "com.yoteh.api.entity.enums.OrderStatus.DELIVERED) "
                    + "AND o.createdAt >= :from AND o.createdAt <= :to "
                    + "GROUP BY CAST(o.createdAt AS LocalDate) "
                    + "ORDER BY CAST(o.createdAt AS LocalDate) ASC")
    List<Object[]> findRevenueByDay(
            @Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    @Query(
            value =
                    "SELECT TO_CHAR(o.created_at, 'YYYY-MM') AS month, "
                            + "COALESCE(SUM(o.total), 0) AS revenue, "
                            + "COUNT(o.id) AS order_count "
                            + "FROM orders o WHERE o.deleted_at IS NULL "
                            + "AND o.status IN ('PAID', 'PREPARING', 'SHIPPED', 'DELIVERED') "
                            + "AND o.created_at >= :from AND o.created_at <= :to "
                            + "GROUP BY TO_CHAR(o.created_at, 'YYYY-MM') "
                            + "ORDER BY month ASC",
            nativeQuery = true)
    List<Object[]> findRevenueByMonth(
            @Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    // ─── Top clients (userId, orderCount, totalSpent) ─────────
    @Query(
            "SELECT o.user.id, COUNT(o), COALESCE(SUM(o.total), 0) "
                    + "FROM Order o WHERE o.deletedAt IS NULL "
                    + "AND o.status IN (com.yoteh.api.entity.enums.OrderStatus.PAID, "
                    + "com.yoteh.api.entity.enums.OrderStatus.PREPARING, "
                    + "com.yoteh.api.entity.enums.OrderStatus.SHIPPED, "
                    + "com.yoteh.api.entity.enums.OrderStatus.DELIVERED) "
                    + "GROUP BY o.user.id "
                    + "ORDER BY SUM(o.total) DESC")
    List<Object[]> findTopCustomers(Pageable pageable);
}
