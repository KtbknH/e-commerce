package com.yoteh.api.repository;

import com.yoteh.api.entity.CartItem;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, UUID> {

    List<CartItem> findByCartId(UUID cartId);

    @Query(
            "SELECT ci FROM CartItem ci "
                    + "WHERE ci.cart.id = :cartId AND ci.product.id = :productId "
                    + "AND (ci.variant.id = :variantId OR (:variantId IS NULL AND ci.variant IS NULL))")
    Optional<CartItem> findByCartIdAndProductIdAndVariantId(
            @Param("cartId") UUID cartId,
            @Param("productId") UUID productId,
            @Param("variantId") UUID variantId);

    @Modifying
    @Query("DELETE FROM CartItem ci WHERE ci.cart.id = :cartId")
    void deleteAllByCartId(@Param("cartId") UUID cartId);

    long countByCartId(UUID cartId);
}
