package com.yoteh.api.repository;

import com.yoteh.api.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AddressRepository extends JpaRepository<Address, UUID> {

    List<Address> findByUserIdAndDeletedAtIsNullOrderByIsDefaultDesc(UUID userId);

    Optional<Address> findByIdAndUserId(UUID id, UUID userId);

    Optional<Address> findByUserIdAndIsDefaultTrue(UUID userId);

    long countByUserIdAndDeletedAtIsNull(UUID userId);
}
