package com.yoteh.api.repository;

import com.yoteh.api.entity.User;
import com.yoteh.api.entity.enums.LoyaltyLevel;
import com.yoteh.api.entity.enums.UserRole;
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
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);

    Optional<User> findByPhone(String phone);

    boolean existsByEmail(String email);

    boolean existsByPhone(String phone);

    Optional<User> findByVerificationToken(String token);

    Optional<User> findByResetPasswordToken(String token);

    Optional<User> findByRefreshToken(String refreshToken);

    Page<User> findByRole(UserRole role, Pageable pageable);

    Page<User> findByIsActiveTrue(Pageable pageable);

    @Query(
            "SELECT u FROM User u WHERE u.deletedAt IS NULL "
                    + "AND (:role IS NULL OR u.role = :role) "
                    + "AND (:isActive IS NULL OR u.isActive = :isActive) "
                    + "AND (:search IS NULL OR LOWER(u.firstName) LIKE LOWER(CONCAT('%', :search, '%')) "
                    + "OR LOWER(u.lastName) LIKE LOWER(CONCAT('%', :search, '%')) "
                    + "OR LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<User> findAllFiltered(
            @Param("role") UserRole role,
            @Param("isActive") Boolean isActive,
            @Param("search") String search,
            Pageable pageable);

    @Query("SELECT COUNT(u) FROM User u WHERE u.deletedAt IS NULL AND u.role = :role")
    long countByRole(@Param("role") UserRole role);

    // ─── Admin : recherche avec filtres ───────────────────────
    @Query(
            "SELECT u FROM User u WHERE "
                    + "(:search IS NULL OR :search = '' OR "
                    + " LOWER(u.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR "
                    + " LOWER(u.lastName) LIKE LOWER(CONCAT('%', :search, '%')) OR "
                    + " LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')) OR "
                    + " u.phone LIKE CONCAT('%', :search, '%')) "
                    + "AND (:role IS NULL OR u.role = :role) "
                    + "AND (:isActive IS NULL OR u.isActive = :isActive)")
    Page<User> findAllWithFilters(
            @Param("search") String search,
            @Param("role") UserRole role,
            @Param("isActive") Boolean isActive,
            Pageable pageable);

    long countByIsVerifiedTrue();

    long countByIsVerifiedFalse();

    @Query("SELECT COUNT(u) FROM User u WHERE u.createdAt >= :since")
    long countNewUsersSince(@Param("since") LocalDateTime since);

    long countByLoyaltyLevel(LoyaltyLevel loyaltyLevel);

    @Query(
            value =
                    "SELECT TO_CHAR(u.created_at, 'YYYY-MM-DD') AS day, COUNT(u.id) "
                            + "FROM users u "
                            + "WHERE u.created_at >= :from AND u.created_at <= :to "
                            + "GROUP BY TO_CHAR(u.created_at, 'YYYY-MM-DD') "
                            + "ORDER BY day ASC",
            nativeQuery = true)
    List<Object[]> countNewUsersByDay(
            @Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    @Query(
            value =
                    "SELECT TO_CHAR(u.created_at, 'YYYY-MM') AS month, COUNT(u.id) "
                            + "FROM users u "
                            + "WHERE u.created_at >= :from AND u.created_at <= :to "
                            + "GROUP BY TO_CHAR(u.created_at, 'YYYY-MM') "
                            + "ORDER BY month ASC",
            nativeQuery = true)
    List<Object[]> countNewUsersByMonth(
            @Param("from") LocalDateTime from, @Param("to") LocalDateTime to);
}
