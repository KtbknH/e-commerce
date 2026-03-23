package com.yoteh.api.repository;

import com.yoteh.api.entity.WishlistItem;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WishlistItemRepository extends JpaRepository<WishlistItem, UUID> {

    Page<WishlistItem> findByUserIdAndDeletedAtIsNullOrderByCreatedAtDesc(
            UUID userId, Pageable pageable);

    Optional<WishlistItem> findByUserIdAndProductId(UUID userId, UUID productId);

    boolean existsByUserIdAndProductId(UUID userId, UUID productId);

    void deleteByUserIdAndProductId(UUID userId, UUID productId);

    long countByUserIdAndDeletedAtIsNull(UUID userId);
}
