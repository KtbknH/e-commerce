package com.yoteh.api.repository;

import com.yoteh.api.entity.Payment;
import com.yoteh.api.entity.enums.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    Optional<Payment> findByPaymentReference(String paymentReference);

    Optional<Payment> findByExternalId(String externalId);

    List<Payment> findByOrderIdOrderByCreatedAtDesc(UUID orderId);

    Page<Payment> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    Page<Payment> findByStatusOrderByCreatedAtDesc(PaymentStatus status, Pageable pageable);

    long countByStatus(PaymentStatus status);
}
