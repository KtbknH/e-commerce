package com.yoteh.api.repository;

import com.yoteh.api.entity.Order;
import com.yoteh.api.entity.enums.OrderStatus;
import java.time.LocalDateTime;
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

    // ─── Compteurs ──────────────────────────────────────────
    long countByUserId(UUID userId);

    long countByStatus(OrderStatus status);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.user.id = :userId AND o.status = :status")
    long countByUserIdAndStatus(@Param("userId") UUID userId, @Param("status") OrderStatus status);

    boolean existsByOrderNumber(String orderNumber);
}
