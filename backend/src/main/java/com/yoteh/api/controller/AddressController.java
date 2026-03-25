package com.yoteh.api.controller;

import com.yoteh.api.dto.request.AddressRequest;
import com.yoteh.api.dto.response.AddressResponse;
import com.yoteh.api.dto.response.common.ApiResponse;
import com.yoteh.api.security.CustomUserDetails;
import com.yoteh.api.service.AddressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users/me/addresses")
@RequiredArgsConstructor
@Tag(name = "Addresses", description = "Gestion des adresses de livraison")
public class AddressController {

    private final AddressService addressService;

    @GetMapping
    @Operation(summary = "Lister mes adresses")
    public ResponseEntity<ApiResponse<List<AddressResponse>>> getMyAddresses(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        List<AddressResponse> addresses = addressService.getMyAddresses(userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success(addresses, "Adresses récupérées"));
    }

    @GetMapping("/{addressId}")
    @Operation(summary = "Obtenir une adresse par ID")
    public ResponseEntity<ApiResponse<AddressResponse>> getAddressById(
            @AuthenticationPrincipal CustomUserDetails userDetails, @PathVariable UUID addressId) {
        AddressResponse address = addressService.getAddressById(userDetails.getId(), addressId);
        return ResponseEntity.ok(ApiResponse.success(address, "Adresse récupérée"));
    }

    @PostMapping
    @Operation(summary = "Ajouter une adresse")
    public ResponseEntity<ApiResponse<AddressResponse>> createAddress(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody AddressRequest request) {
        AddressResponse address = addressService.createAddress(userDetails.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(address, "Adresse créée"));
    }

    @PutMapping("/{addressId}")
    @Operation(summary = "Modifier une adresse")
    public ResponseEntity<ApiResponse<AddressResponse>> updateAddress(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID addressId,
            @Valid @RequestBody AddressRequest request) {
        AddressResponse address =
                addressService.updateAddress(userDetails.getId(), addressId, request);
        return ResponseEntity.ok(ApiResponse.success(address, "Adresse mise à jour"));
    }

    @DeleteMapping("/{addressId}")
    @Operation(summary = "Supprimer une adresse")
    public ResponseEntity<ApiResponse<Void>> deleteAddress(
            @AuthenticationPrincipal CustomUserDetails userDetails, @PathVariable UUID addressId) {
        addressService.deleteAddress(userDetails.getId(), addressId);
        return ResponseEntity.ok(ApiResponse.success(null, "Adresse supprimée"));
    }

    @PatchMapping("/{addressId}/default")
    @Operation(summary = "Définir une adresse comme adresse par défaut")
    public ResponseEntity<ApiResponse<AddressResponse>> setDefaultAddress(
            @AuthenticationPrincipal CustomUserDetails userDetails, @PathVariable UUID addressId) {
        AddressResponse address = addressService.setDefaultAddress(userDetails.getId(), addressId);
        return ResponseEntity.ok(ApiResponse.success(address, "Adresse définie par défaut"));
    }
}
