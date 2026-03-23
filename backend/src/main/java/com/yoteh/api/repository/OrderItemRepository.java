package com.yoteh.api.repository;

import com.yoteh.api.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, UUID> {

    List<OrderItem> findByOrderId(UUID orderId);

    @Query("SELECT oi.productName, SUM(oi.quantity) as totalQty FROM OrderItem oi " +
            "WHERE oi.order.deletedAt IS NULL GROUP BY oi.productName ORDER BY totalQty DESC")
    List<Object[]> findTopSellingProducts();

    boolean existsByOrderIdAndProductId(UUID orderId, UUID productId);
}
