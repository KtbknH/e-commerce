package com.yoteh.api.controller;

import com.yoteh.api.dto.request.ForgotPasswordRequest;
import com.yoteh.api.dto.request.LoginRequest;
import com.yoteh.api.dto.request.RefreshTokenRequest;
import com.yoteh.api.dto.request.RegisterRequest;
import com.yoteh.api.dto.request.ResetPasswordRequest;
import com.yoteh.api.dto.response.AuthResponse;
import com.yoteh.api.dto.response.common.ApiResponse;
import com.yoteh.api.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(
        name = "Authentification",
        description = "Endpoints d'inscription, connexion et gestion des tokens")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    // ─── POST /api/v1/auth/register ──────────────────────────

    @PostMapping("/register")
    @Operation(summary = "Inscription", description = "Créer un nouveau compte utilisateur")
    public ResponseEntity<ApiResponse<AuthResponse>> register(
            @Valid @RequestBody RegisterRequest request) {

        log.info("POST /api/v1/auth/register - email: {}", request.getEmail());
        AuthResponse response = authService.register(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(
                        ApiResponse.created(
                                response, "Inscription réussie. Veuillez vérifier votre email."));
    }

    // ─── POST /api/v1/auth/login ─────────────────────────────

    @PostMapping("/login")
    @Operation(summary = "Connexion", description = "Se connecter avec email et mot de passe")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request) {

        log.info("POST /api/v1/auth/login - email: {}", request.getEmail());
        AuthResponse response = authService.login(request);

        return ResponseEntity.ok(ApiResponse.success(response, "Connexion réussie"));
    }

    // ─── POST /api/v1/auth/refresh-token ─────────────────────

    @PostMapping("/refresh-token")
    @Operation(
            summary = "Rafraîchir le token",
            description = "Obtenir un nouveau access token avec le refresh token")
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(
            @Valid @RequestBody RefreshTokenRequest request) {

        log.info("POST /api/v1/auth/refresh-token");
        AuthResponse response = authService.refreshToken(request);

        return ResponseEntity.ok(ApiResponse.success(response, "Token rafraîchi"));
    }

    // ─── POST /api/v1/auth/logout ────────────────────────────

    @PostMapping("/logout")
    @Operation(summary = "Déconnexion", description = "Révoquer le refresh token")
    public ResponseEntity<ApiResponse<Void>> logout() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        log.info("POST /api/v1/auth/logout - email: {}", email);
        authService.logout(email);

        return ResponseEntity.ok(ApiResponse.success(null, "Déconnexion réussie"));
    }

    // ─── POST /api/v1/auth/forgot-password ───────────────────

    @PostMapping("/forgot-password")
    @Operation(
            summary = "Mot de passe oublié",
            description = "Envoyer un email de réinitialisation du mot de passe")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request) {

        log.info("POST /api/v1/auth/forgot-password - email: {}", request.getEmail());
        authService.forgotPassword(request);

        // Toujours retourner succès même si l'email n'existe pas (sécurité)
        return ResponseEntity.ok(
                ApiResponse.success(
                        null, "Si cet email existe, un lien de réinitialisation a été envoyé"));
    }

    // ─── POST /api/v1/auth/reset-password ────────────────────

    @PostMapping("/reset-password")
    @Operation(
            summary = "Réinitialiser le mot de passe",
            description = "Changer le mot de passe avec un token de réinitialisation")
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request) {

        log.info("POST /api/v1/auth/reset-password");
        authService.resetPassword(request);

        return ResponseEntity.ok(
                ApiResponse.success(null, "Mot de passe réinitialisé avec succès"));
    }

    // ─── GET /api/v1/auth/verify-email ───────────────────────

    @GetMapping("/verify-email")
    @Operation(
            summary = "Vérifier l'email",
            description = "Valider l'adresse email avec le token envoyé par email")
    public ResponseEntity<ApiResponse<Void>> verifyEmail(@RequestParam String token) {

        log.info("GET /api/v1/auth/verify-email");
        authService.verifyEmail(token);

        return ResponseEntity.ok(ApiResponse.success(null, "Email vérifié avec succès"));
    }
}
