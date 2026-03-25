package com.yoteh.api.controller;

import com.yoteh.api.dto.request.AdminUpdateUserRequest;
import com.yoteh.api.dto.request.ChangePasswordRequest;
import com.yoteh.api.dto.request.UpdateProfileRequest;
import com.yoteh.api.dto.response.UserResponse;
import com.yoteh.api.dto.response.common.ApiResponse;
import com.yoteh.api.dto.response.common.PagedResponse;
import com.yoteh.api.security.CustomUserDetails;
import com.yoteh.api.service.UserService;
import com.yoteh.api.util.Constants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Users", description = "Gestion des utilisateurs")
public class UserController {

    private final UserService userService;

    // ═══════════════════════════════════════════════════════════
    // PROFIL UTILISATEUR CONNECTÉ
    // ═══════════════════════════════════════════════════════════

    @GetMapping("/users/me")
    @Operation(summary = "Obtenir mon profil")
    public ResponseEntity<ApiResponse<UserResponse>> getMyProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        UserResponse response = userService.getMyProfile(userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success(response, "Profil récupéré"));
    }

    @PutMapping("/users/me")
    @Operation(summary = "Mettre à jour mon profil")
    public ResponseEntity<ApiResponse<UserResponse>> updateMyProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody UpdateProfileRequest request) {
        UserResponse response = userService.updateMyProfile(userDetails.getId(), request);
        return ResponseEntity.ok(ApiResponse.success(response, "Profil mis à jour"));
    }

    @PutMapping("/users/me/password")
    @Operation(summary = "Changer mon mot de passe")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody ChangePasswordRequest request) {
        userService.changePassword(userDetails.getId(), request);
        return ResponseEntity.ok(ApiResponse.success(null, "Mot de passe modifié avec succès"));
    }

    @DeleteMapping("/users/me")
    @Operation(summary = "Supprimer mon compte (soft delete)")
    public ResponseEntity<ApiResponse<Void>> deleteMyAccount(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        userService.deleteMyAccount(userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success(null, "Compte supprimé avec succès"));
    }

    // ═══════════════════════════════════════════════════════════
    // ADMIN : GESTION DES UTILISATEURS
    // ═══════════════════════════════════════════════════════════

    @GetMapping("/admin/users")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Lister tous les utilisateurs (admin)")
    public ResponseEntity<ApiResponse<PagedResponse<UserResponse>>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = Constants.DEFAULT_SORT_BY) String sortBy,
            @RequestParam(defaultValue = Constants.DEFAULT_SORT_DIR) String sortDir,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) Boolean isActive) {
        PagedResponse<UserResponse> response =
                userService.getAllUsers(page, size, sortBy, sortDir, search, role, isActive);
        return ResponseEntity.ok(ApiResponse.success(response, "Liste des utilisateurs"));
    }

    @GetMapping("/admin/users/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Obtenir un utilisateur par ID (admin)")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable UUID userId) {
        UserResponse response = userService.getUserById(userId);
        return ResponseEntity.ok(ApiResponse.success(response, "Utilisateur récupéré"));
    }

    @PatchMapping("/admin/users/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Modifier un utilisateur (admin : rôle, statut)")
    public ResponseEntity<ApiResponse<UserResponse>> adminUpdateUser(
            @PathVariable UUID userId, @Valid @RequestBody AdminUpdateUserRequest request) {
        UserResponse response = userService.adminUpdateUser(userId, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Utilisateur mis à jour"));
    }

    @PatchMapping("/admin/users/{userId}/ban")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Bannir un utilisateur (admin)")
    public ResponseEntity<ApiResponse<Void>> banUser(@PathVariable UUID userId) {
        userService.banUser(userId);
        return ResponseEntity.ok(ApiResponse.success(null, "Utilisateur banni"));
    }

    @PatchMapping("/admin/users/{userId}/unban")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Débannir un utilisateur (admin)")
    public ResponseEntity<ApiResponse<Void>> unbanUser(@PathVariable UUID userId) {
        userService.unbanUser(userId);
        return ResponseEntity.ok(ApiResponse.success(null, "Utilisateur débanni"));
    }
}
