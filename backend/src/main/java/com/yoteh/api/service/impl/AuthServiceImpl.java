package com.yoteh.api.service.impl;

import com.yoteh.api.dto.request.ForgotPasswordRequest;
import com.yoteh.api.dto.request.LoginRequest;
import com.yoteh.api.dto.request.RefreshTokenRequest;
import com.yoteh.api.dto.request.RegisterRequest;
import com.yoteh.api.dto.request.ResetPasswordRequest;
import com.yoteh.api.dto.response.AuthResponse;
import com.yoteh.api.entity.User;
import com.yoteh.api.entity.enums.LoyaltyLevel;
import com.yoteh.api.entity.enums.UserRole;
import com.yoteh.api.exception.BadRequestException;
import com.yoteh.api.exception.DuplicateResourceException;
import com.yoteh.api.exception.ResourceNotFoundException;
import com.yoteh.api.exception.UnauthorizedException;
import com.yoteh.api.repository.UserRepository;
import com.yoteh.api.security.JwtService;
import com.yoteh.api.service.AuthService;
import com.yoteh.api.service.NotificationService;
import java.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthServiceImpl implements AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final NotificationService notificationService;

    public AuthServiceImpl(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            AuthenticationManager authenticationManager,
            UserDetailsService userDetailsService,
            NotificationService notificationService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.notificationService = notificationService;
    }

    // ─── Inscription ─────────────────────────────────────────

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        log.info("Tentative d'inscription pour: {}", request.getEmail());

        // Vérifier si l'email existe déjà
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("User", "email", request.getEmail());
        }

        // Créer l'utilisateur
        User user = new User();
        user.setEmail(request.getEmail().toLowerCase().trim());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFirstName(request.getFirstName().trim());
        user.setLastName(request.getLastName().trim());
        user.setPhone(request.getPhone());
        user.setRole(UserRole.CLIENT);
        user.setIsActive(true);
        user.setIsVerified(false);
        user.setLoyaltyPoints(0);
        user.setLoyaltyLevel(LoyaltyLevel.BRONZE);
        user.setPreferredLanguage("fr");
        user.setPreferredCurrency("XOF");

        // Générer le token de vérification d'email
        String verificationToken = jwtService.generateEmailVerificationToken(user.getEmail());
        user.setVerificationToken(verificationToken);
        user.setVerificationExpires(LocalDateTime.now().plusHours(24));

        User savedUser = userRepository.save(user);
        notificationService.sendWelcomeEmail(savedUser, verificationToken);
        log.info("Utilisateur créé avec succès: {}", savedUser.getId());

        // Générer les tokens
        UserDetails userDetails = userDetailsService.loadUserByUsername(savedUser.getEmail());
        String accessToken = jwtService.generateAccessToken(userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails);

        // Sauvegarder le refresh token
        savedUser.setRefreshToken(refreshToken);
        savedUser.setLastLogin(LocalDateTime.now());
        userRepository.save(savedUser);

        return buildAuthResponse(savedUser, accessToken, refreshToken);
    }

    // ─── Connexion ───────────────────────────────────────────

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request) {
        log.info("Tentative de connexion pour: {}", request.getEmail());

        try {
            // Authentifier via Spring Security
            Authentication authentication =
                    authenticationManager.authenticate(
                            new UsernamePasswordAuthenticationToken(
                                    request.getEmail().toLowerCase().trim(),
                                    request.getPassword()));

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();

            // Charger l'entité User
            User user =
                    userRepository
                            .findByEmail(request.getEmail().toLowerCase().trim())
                            .orElseThrow(
                                    () ->
                                            new UnauthorizedException(
                                                    "Email ou mot de passe incorrect"));

            if (!user.getIsActive()) {
                throw new UnauthorizedException("Ce compte a été désactivé");
            }

            // Générer les tokens
            String accessToken = jwtService.generateAccessToken(userDetails);
            String refreshToken = jwtService.generateRefreshToken(userDetails);

            // Mettre à jour l'utilisateur
            user.setRefreshToken(refreshToken);
            user.setLastLogin(LocalDateTime.now());
            userRepository.save(user);

            log.info("Connexion réussie pour: {}", user.getEmail());
            return buildAuthResponse(user, accessToken, refreshToken);

        } catch (BadCredentialsException e) {
            log.warn("Échec de connexion pour: {}", request.getEmail());
            throw new UnauthorizedException("Email ou mot de passe incorrect");
        }
    }

    // ─── Rafraîchir le token ─────────────────────────────────

    @Override
    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();

        // Vérifier le type du token
        if (!jwtService.isValidTokenType(refreshToken, "refresh")) {
            throw new UnauthorizedException("Token invalide : ce n'est pas un refresh token");
        }

        // Vérifier l'expiration
        if (jwtService.isTokenExpired(refreshToken)) {
            throw new UnauthorizedException("Le refresh token a expiré, veuillez vous reconnecter");
        }

        // Extraire l'email
        String email = jwtService.extractEmail(refreshToken);

        // Charger l'utilisateur
        User user =
                userRepository
                        .findByEmail(email)
                        .orElseThrow(() -> new UnauthorizedException("Utilisateur non trouvé"));

        // Vérifier que le refresh token correspond
        if (user.getRefreshToken() == null || !user.getRefreshToken().equals(refreshToken)) {
            throw new UnauthorizedException("Refresh token invalide ou révoqué");
        }

        // Générer de nouveaux tokens
        UserDetails userDetails = userDetailsService.loadUserByUsername(email);
        String newAccessToken = jwtService.generateAccessToken(userDetails);
        String newRefreshToken = jwtService.generateRefreshToken(userDetails);

        // Mettre à jour le refresh token (rotation)
        user.setRefreshToken(newRefreshToken);
        userRepository.save(user);

        log.info("Token rafraîchi pour: {}", email);
        return buildAuthResponse(user, newAccessToken, newRefreshToken);
    }

    // ─── Déconnexion ─────────────────────────────────────────

    @Override
    @Transactional
    public void logout(String email) {
        log.info("Déconnexion de: {}", email);

        User user =
                userRepository
                        .findByEmail(email)
                        .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));

        // Révoquer le refresh token
        user.setRefreshToken(null);
        userRepository.save(user);
    }

    // ─── Mot de passe oublié ─────────────────────────────────

    @Override
    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        String email = request.getEmail().toLowerCase().trim();
        log.info("Demande de réinitialisation de mot de passe pour: {}", email);

        // Toujours retourner succès même si l'email n'existe pas (sécurité)
        userRepository
                .findByEmail(email)
                .ifPresent(
                        user -> {
                            // Générer le token de réinitialisation
                            String resetToken = jwtService.generatePasswordResetToken(email);
                            user.setResetPasswordToken(resetToken);
                            user.setResetPasswordExpires(LocalDateTime.now().plusHours(1));
                            userRepository.save(user);
                            notificationService.sendPasswordResetEmail(user, resetToken);
                            log.info("Token de réinitialisation généré pour: {}", email);
                        });
    }

    // ─── Réinitialiser le mot de passe ───────────────────────

    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        String token = request.getToken();

        // Valider le token
        if (jwtService.isTokenExpired(token)) {
            throw new BadRequestException("Le lien de réinitialisation a expiré");
        }

        if (!jwtService.isValidTokenType(token, "password_reset")) {
            throw new BadRequestException("Token invalide");
        }

        String email = jwtService.extractEmail(token);

        User user =
                userRepository
                        .findByEmail(email)
                        .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));

        // Vérifier que le token correspond
        if (user.getResetPasswordToken() == null || !user.getResetPasswordToken().equals(token)) {
            throw new BadRequestException("Token de réinitialisation invalide ou déjà utilisé");
        }

        // Vérifier l'expiration côté BDD aussi
        if (user.getResetPasswordExpires() != null
                && user.getResetPasswordExpires().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Le lien de réinitialisation a expiré");
        }

        // Mettre à jour le mot de passe
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setResetPasswordToken(null);
        user.setResetPasswordExpires(null);
        // Révoquer aussi le refresh token pour forcer la reconnexion
        user.setRefreshToken(null);
        userRepository.save(user);
        notificationService.sendPasswordChangedEmail(user);

        log.info("Mot de passe réinitialisé pour: {}", email);
    }

    // ─── Vérification email ──────────────────────────────────

    @Override
    @Transactional
    public void verifyEmail(String token) {
        // Valider le token
        if (jwtService.isTokenExpired(token)) {
            throw new BadRequestException("Le lien de vérification a expiré");
        }

        if (!jwtService.isValidTokenType(token, "email_verification")) {
            throw new BadRequestException("Token de vérification invalide");
        }

        String email = jwtService.extractEmail(token);

        User user =
                userRepository
                        .findByEmail(email)
                        .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));

        if (user.getIsVerified()) {
            throw new BadRequestException("Cet email est déjà vérifié");
        }

        // Vérifier que le token correspond
        if (user.getVerificationToken() == null || !user.getVerificationToken().equals(token)) {
            throw new BadRequestException("Token de vérification invalide");
        }

        // Marquer comme vérifié
        user.setIsVerified(true);
        user.setVerificationToken(null);
        user.setVerificationExpires(null);
        userRepository.save(user);

        log.info("Email vérifié pour: {}", email);
    }

    // ─── Utilitaire : construire la réponse ──────────────────

    private AuthResponse buildAuthResponse(User user, String accessToken, String refreshToken) {
        return AuthResponse.builder()
                .user(
                        AuthResponse.UserInfo.builder()
                                .id(user.getId().toString())
                                .email(user.getEmail())
                                .firstName(user.getFirstName())
                                .lastName(user.getLastName())
                                .phone(user.getPhone())
                                .avatar(user.getAvatar())
                                .role(user.getRole().name())
                                .isVerified(user.getIsVerified())
                                .preferredLanguage(user.getPreferredLanguage())
                                .preferredCurrency(user.getPreferredCurrency())
                                .loyaltyPoints(user.getLoyaltyPoints())
                                .loyaltyLevel(user.getLoyaltyLevel().name())
                                .build())
                .tokens(
                        AuthResponse.TokenInfo.builder()
                                .accessToken(accessToken)
                                .refreshToken(refreshToken)
                                .expiresIn(jwtService.getAccessTokenExpiration())
                                .build())
                .build();
    }
}
