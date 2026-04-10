package com.yoteh.api.repository;

import com.yoteh.api.entity.OrderItem;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, UUID> {

    List<OrderItem> findByOrderId(UUID orderId);

    @Query(
            "SELECT oi.productName, SUM(oi.quantity) AS totalQty "
                    + "FROM OrderItem oi "
                    + "WHERE oi.order.deletedAt IS NULL "
                    + "GROUP BY oi.productName "
                    + "ORDER BY totalQty DESC")
    List<Object[]> findTopSellingProducts();

    boolean existsByOrderIdAndProductId(UUID orderId, UUID productId);

    @Query(
            "SELECT oi.productName, oi.productSku, SUM(oi.quantity), "
                    + "COALESCE(SUM(oi.total), 0) "
                    + "FROM OrderItem oi "
                    + "WHERE oi.order.deletedAt IS NULL "
                    + "AND oi.order.status IN (com.yoteh.api.entity.enums.OrderStatus.PAID, "
                    + "com.yoteh.api.entity.enums.OrderStatus.PREPARING, "
                    + "com.yoteh.api.entity.enums.OrderStatus.SHIPPED, "
                    + "com.yoteh.api.entity.enums.OrderStatus.DELIVERED) "
                    + "GROUP BY oi.productName, oi.productSku "
                    + "ORDER BY SUM(oi.quantity) DESC")
    List<Object[]> findTopSellingProductsWithRevenue(Pageable pageable);

    @Query(
            "SELECT oi.productName, oi.productSku, SUM(oi.quantity), "
                    + "COALESCE(SUM(oi.total), 0) "
                    + "FROM OrderItem oi "
                    + "WHERE oi.order.deletedAt IS NULL "
                    + "AND oi.order.status IN (com.yoteh.api.entity.enums.OrderStatus.PAID, "
                    + "com.yoteh.api.entity.enums.OrderStatus.PREPARING, "
                    + "com.yoteh.api.entity.enums.OrderStatus.SHIPPED, "
                    + "com.yoteh.api.entity.enums.OrderStatus.DELIVERED) "
                    + "GROUP BY oi.productName, oi.productSku "
                    + "ORDER BY SUM(oi.total) DESC")
    List<Object[]> findTopRevenueProducts(Pageable pageable);

    @Query(
            "SELECT SUM(oi.quantity) FROM OrderItem oi "
                    + "WHERE oi.order.deletedAt IS NULL "
                    + "AND oi.order.status IN (com.yoteh.api.entity.enums.OrderStatus.PAID, "
                    + "com.yoteh.api.entity.enums.OrderStatus.PREPARING, "
                    + "com.yoteh.api.entity.enums.OrderStatus.SHIPPED, "
                    + "com.yoteh.api.entity.enums.OrderStatus.DELIVERED) "
                    + "AND oi.order.createdAt >= :from AND oi.order.createdAt <= :to")
    Long sumQuantitySoldForPeriod(
            @Param("from") java.time.LocalDateTime from, @Param("to") java.time.LocalDateTime to);
}
