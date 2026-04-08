package com.yoteh.api.repository;

import com.yoteh.api.entity.Cart;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CartRepository extends JpaRepository<Cart, UUID> {

    @Query(
            "SELECT c FROM Cart c LEFT JOIN FETCH c.items i "
                    + "LEFT JOIN FETCH i.product p LEFT JOIN FETCH p.images "
                    + "LEFT JOIN FETCH i.variant "
                    + "WHERE c.user.id = :userId")
    Optional<Cart> findByUserIdWithItems(@Param("userId") UUID userId);

    Optional<Cart> findByUserId(UUID userId);

    boolean existsByUserId(UUID userId);
}
