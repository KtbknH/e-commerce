package com.yoteh.api.repository;

import com.yoteh.api.entity.LoyaltyTransaction;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface LoyaltyTransactionRepository extends JpaRepository<LoyaltyTransaction, UUID> {

    Page<LoyaltyTransaction> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    @Query(
            "SELECT COALESCE(SUM(lt.points), 0) FROM LoyaltyTransaction lt WHERE lt.user.id = :userId AND lt.type = 'EARN'")
    int sumEarnedPointsByUserId(@Param("userId") UUID userId);

    @Query(
            "SELECT COALESCE(SUM(lt.points), 0) FROM LoyaltyTransaction lt WHERE lt.user.id = :userId AND lt.type = 'REDEEM'")
    int sumRedeemedPointsByUserId(@Param("userId") UUID userId);
}
