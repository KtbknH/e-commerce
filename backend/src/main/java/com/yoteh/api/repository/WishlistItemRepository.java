package com.yoteh.api.repository;

import com.yoteh.api.entity.WishlistItem;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface WishlistItemRepository extends JpaRepository<WishlistItem, UUID> {

    Page<WishlistItem> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    Optional<WishlistItem> findByUserIdAndProductId(UUID userId, UUID productId);

    boolean existsByUserIdAndProductId(UUID userId, UUID productId);

    @Modifying
    @Query("DELETE FROM WishlistItem wi WHERE wi.user.id = :userId AND wi.product.id = :productId")
    void deleteByUserIdAndProductId(
            @Param("userId") UUID userId, @Param("productId") UUID productId);

    long countByUserId(UUID userId);
}
