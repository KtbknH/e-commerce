package com.yoteh.api.service;

import com.yoteh.api.dto.request.AdminUpdateUserRequest;
import com.yoteh.api.dto.request.ChangePasswordRequest;
import com.yoteh.api.dto.request.UpdateProfileRequest;
import com.yoteh.api.dto.response.UserResponse;
import com.yoteh.api.dto.response.common.PagedResponse;
import java.util.UUID;

public interface UserService {

    // ─── Profil utilisateur connecté ──────────────────────────
    UserResponse getMyProfile(UUID userId);

    UserResponse updateMyProfile(UUID userId, UpdateProfileRequest request);

    void changePassword(UUID userId, ChangePasswordRequest request);

    void deleteMyAccount(UUID userId);

    // ─── Admin : gestion des utilisateurs ─────────────────────
    PagedResponse<UserResponse> getAllUsers(
            int page,
            int size,
            String sortBy,
            String sortDir,
            String search,
            String role,
            Boolean isActive);

    UserResponse getUserById(UUID userId);

    UserResponse adminUpdateUser(UUID userId, AdminUpdateUserRequest request);

    void banUser(UUID userId);

    void unbanUser(UUID userId);
}
