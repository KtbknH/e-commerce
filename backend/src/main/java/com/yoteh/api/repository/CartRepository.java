package com.yoteh.api.repository;

import com.yoteh.api.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CartRepository extends JpaRepository<Cart, UUID> {

    Optional<Cart> findByUserId(UUID userId);

    @Query("SELECT c FROM Cart c LEFT JOIN FETCH c.items ci LEFT JOIN FETCH ci.product LEFT JOIN FETCH ci.variant WHERE c.user.id = :userId")
    Optional<Cart> findByUserIdWithItems(@Param("userId") UUID userId);

    boolean existsByUserId(UUID userId);
}
