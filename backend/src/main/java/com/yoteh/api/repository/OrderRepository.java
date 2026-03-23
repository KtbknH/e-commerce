package com.yoteh.api.repository;

import com.yoteh.api.entity.Order;
import com.yoteh.api.entity.enums.OrderStatus;
import java.math.BigDecimal;
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

    Optional<Order> findByOrderNumber(String orderNumber);

    Page<Order> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    Page<Order> findByUserIdAndStatusOrderByCreatedAtDesc(
            UUID userId, OrderStatus status, Pageable pageable);

    Page<Order> findByStatusOrderByCreatedAtDesc(OrderStatus status, Pageable pageable);

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
            "SELECT COALESCE(SUM(o.total), 0) FROM Order o WHERE o.deletedAt IS NULL AND o.status = :status")
    BigDecimal sumTotalByStatus(@Param("status") OrderStatus status);

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
}
