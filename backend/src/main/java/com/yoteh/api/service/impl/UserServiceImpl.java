package com.yoteh.api.service.impl;

import com.yoteh.api.dto.request.AdminUpdateUserRequest;
import com.yoteh.api.dto.request.ChangePasswordRequest;
import com.yoteh.api.dto.request.UpdateProfileRequest;
import com.yoteh.api.dto.response.UserResponse;
import com.yoteh.api.dto.response.common.PagedResponse;
import com.yoteh.api.entity.User;
import com.yoteh.api.entity.enums.UserRole;
import com.yoteh.api.exception.BadRequestException;
import com.yoteh.api.exception.ResourceNotFoundException;
import com.yoteh.api.mapper.UserMapper;
import com.yoteh.api.repository.AddressRepository;
import com.yoteh.api.repository.OrderRepository;
import com.yoteh.api.repository.UserRepository;
import com.yoteh.api.service.UserService;
import com.yoteh.api.util.Constants;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final AddressRepository addressRepository;
    private final OrderRepository orderRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    // ═══════════════════════════════════════════════════════════
    // PROFIL UTILISATEUR CONNECTÉ
    // ═══════════════════════════════════════════════════════════

    @Override
    public UserResponse getMyProfile(UUID userId) {
        User user = findUserOrThrow(userId);
        long addressCount = addressRepository.countByUserId(userId);
        long orderCount = orderRepository.countByUserId(userId);
        return userMapper.toResponseWithCounts(user, orderCount, addressCount);
    }

    @Override
    @Transactional
    public UserResponse updateMyProfile(UUID userId, UpdateProfileRequest request) {
        User user = findUserOrThrow(userId);

        if (request.getFirstName() != null) {
            user.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            user.setLastName(request.getLastName());
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }
        if (request.getAvatar() != null) {
            user.setAvatar(request.getAvatar());
        }
        if (request.getPreferredLanguage() != null) {
            user.setPreferredLanguage(request.getPreferredLanguage());
        }
        if (request.getPreferredCurrency() != null) {
            user.setPreferredCurrency(request.getPreferredCurrency());
        }

        User saved = userRepository.save(user);
        log.info("Profil mis à jour pour l'utilisateur : {}", userId);
        return userMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void changePassword(UUID userId, ChangePasswordRequest request) {
        // Vérifier que new == confirm
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException("Les mots de passe ne correspondent pas");
        }

        User user = findUserOrThrow(userId);

        // Vérifier le mot de passe actuel
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new BadRequestException("Le mot de passe actuel est incorrect");
        }

        // Vérifier que le nouveau mot de passe est différent
        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new BadRequestException(
                    "Le nouveau mot de passe doit être différent de l'ancien");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        log.info("Mot de passe changé pour l'utilisateur : {}", userId);
    }

    @Override
    @Transactional
    public void deleteMyAccount(UUID userId) {
        User user = findUserOrThrow(userId);
        // Soft delete : on désactive le compte
        user.setIsActive(false);
        userRepository.save(user);
        log.info("Compte désactivé (soft delete) pour l'utilisateur : {}", userId);
    }

    // ═══════════════════════════════════════════════════════════
    // ADMIN : GESTION DES UTILISATEURS
    // ═══════════════════════════════════════════════════════════

    @Override
    public PagedResponse<UserResponse> getAllUsers(
            int page,
            int size,
            String sortBy,
            String sortDir,
            String search,
            String role,
            Boolean isActive) {

        // Sécuriser la pagination
        size = Math.min(size, Constants.MAX_PAGE_SIZE);
        Sort sort =
                sortDir.equalsIgnoreCase("asc")
                        ? Sort.by(sortBy).ascending()
                        : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        // Convertir le rôle en enum si fourni
        UserRole userRole = null;
        if (role != null && !role.isBlank()) {
            try {
                userRole = UserRole.valueOf(role.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new BadRequestException("Rôle invalide : " + role);
            }
        }

        // Requête avec filtres
        Page<User> usersPage =
                userRepository.findAllWithFilters(search, userRole, isActive, pageable);

        Page<UserResponse> responsePage = usersPage.map(userMapper::toResponse);

        return PagedResponse.<UserResponse>builder()
                .content(responsePage.getContent())
                .page(responsePage.getNumber())
                .size(responsePage.getSize())
                .totalElements(responsePage.getTotalElements())
                .totalPages(responsePage.getTotalPages())
                .last(responsePage.isLast())
                .build();
    }

    @Override
    public UserResponse getUserById(UUID userId) {
        User user = findUserOrThrow(userId);
        long addressCount = addressRepository.countByUserId(userId);
        long orderCount = orderRepository.countByUserId(userId);
        return userMapper.toResponseWithCounts(user, orderCount, addressCount);
    }

    @Override
    @Transactional
    public UserResponse adminUpdateUser(UUID userId, AdminUpdateUserRequest request) {
        User user = findUserOrThrow(userId);

        if (request.getRole() != null) {
            user.setRole(request.getRole());
        }
        if (request.getIsActive() != null) {
            user.setIsActive(request.getIsActive());
        }
        if (request.getIsVerified() != null) {
            user.setIsVerified(request.getIsVerified());
        }

        User saved = userRepository.save(user);
        log.info(
                "Admin a mis à jour l'utilisateur {} : role={}, isActive={}",
                userId,
                request.getRole(),
                request.getIsActive());
        return userMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void banUser(UUID userId) {
        User user = findUserOrThrow(userId);
        if (!user.getIsActive()) {
            throw new BadRequestException("L'utilisateur est déjà banni");
        }
        user.setIsActive(false);
        // Invalider le refresh token
        user.setRefreshToken(null);
        userRepository.save(user);
        log.info("Admin a banni l'utilisateur : {}", userId);
    }

    @Override
    @Transactional
    public void unbanUser(UUID userId) {
        User user = findUserOrThrow(userId);
        if (user.getIsActive()) {
            throw new BadRequestException("L'utilisateur n'est pas banni");
        }
        user.setIsActive(true);
        userRepository.save(user);
        log.info("Admin a débanni l'utilisateur : {}", userId);
    }

    // ═══════════════════════════════════════════════════════════
    // UTILITAIRE
    // ═══════════════════════════════════════════════════════════

    private User findUserOrThrow(UUID userId) {
        return userRepository
                .findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
    }
}
