package com.yoteh.api.repository;

import com.yoteh.api.entity.Payment;
import com.yoteh.api.entity.enums.PaymentProvider;
import com.yoteh.api.entity.enums.PaymentStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    Optional<Payment> findByPaymentReference(String paymentReference);

    Optional<Payment> findByExternalId(String externalId);

    Page<Payment> findByUserId(UUID userId, Pageable pageable);

    List<Payment> findByOrderId(UUID orderId);

    boolean existsByOrderIdAndStatus(UUID orderId, PaymentStatus status);

    /**
     * Liste admin avec filtres optionnels sur statut et provider. Le tri est délégué au Pageable.
     */
    @Query(
            """
      SELECT p FROM Payment p
      WHERE (:status IS NULL OR p.status = :status)
        AND (:provider IS NULL OR p.provider = :provider)
      """)
    Page<Payment> findAllWithFilters(
            @Param("status") PaymentStatus status,
            @Param("provider") PaymentProvider provider,
            Pageable pageable);

    /**
     * Paiements expirés d'un statut donné (PENDING ou PROCESSING) à utiliser dans un scheduler de
     * nettoyage (Chat 13+).
     */
    @Query(
            """
      SELECT p FROM Payment p
      WHERE p.status = :status
        AND p.expiresAt IS NOT NULL
        AND p.expiresAt < :now
      """)
    List<Payment> findExpiredPayments(
            @Param("status") PaymentStatus status, @Param("now") LocalDateTime now);
}
