package com.yoteh.api.service.impl;

import com.yoteh.api.dto.request.AddressRequest;
import com.yoteh.api.dto.response.AddressResponse;
import com.yoteh.api.entity.Address;
import com.yoteh.api.entity.User;
import com.yoteh.api.exception.BadRequestException;
import com.yoteh.api.exception.ResourceNotFoundException;
import com.yoteh.api.mapper.AddressMapper;
import com.yoteh.api.repository.AddressRepository;
import com.yoteh.api.repository.UserRepository;
import com.yoteh.api.service.AddressService;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AddressServiceImpl implements AddressService {

    private final AddressRepository addressRepository;
    private final UserRepository userRepository;
    private final AddressMapper addressMapper;

    private static final int MAX_ADDRESSES = 10;

    @Override
    public List<AddressResponse> getMyAddresses(UUID userId) {
        List<Address> addresses =
                addressRepository.findByUserIdOrderByIsDefaultDescCreatedAtDesc(userId);
        return addresses.stream().map(addressMapper::toResponse).collect(Collectors.toList());
    }

    @Override
    public AddressResponse getAddressById(UUID userId, UUID addressId) {
        Address address = findAddressOrThrow(userId, addressId);
        return addressMapper.toResponse(address);
    }

    @Override
    @Transactional
    public AddressResponse createAddress(UUID userId, AddressRequest request) {
        // Vérifier la limite d'adresses
        long count = addressRepository.countByUserId(userId);
        if (count >= MAX_ADDRESSES) {
            throw new BadRequestException(
                    "Vous avez atteint la limite de " + MAX_ADDRESSES + " adresses");
        }

        User user =
                userRepository
                        .findById(userId)
                        .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        Address address = addressMapper.toEntity(request);
        address.setUser(user);

        // Si c'est la première adresse ou si demandée par défaut, la définir comme défaut
        if (count == 0 || Boolean.TRUE.equals(request.getIsDefault())) {
            resetDefaultAddresses(userId);
            address.setIsDefault(true);
        } else {
            address.setIsDefault(false);
        }

        Address saved = addressRepository.save(address);
        log.info("Adresse créée pour l'utilisateur {} : {}", userId, saved.getId());
        return addressMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public AddressResponse updateAddress(UUID userId, UUID addressId, AddressRequest request) {
        Address address = findAddressOrThrow(userId, addressId);

        addressMapper.updateEntity(request, address);

        // Gérer le changement de défaut
        if (Boolean.TRUE.equals(request.getIsDefault()) && !address.getIsDefault()) {
            resetDefaultAddresses(userId);
            address.setIsDefault(true);
        }

        Address saved = addressRepository.save(address);
        log.info("Adresse {} mise à jour pour l'utilisateur {}", addressId, userId);
        return addressMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void deleteAddress(UUID userId, UUID addressId) {
        Address address = findAddressOrThrow(userId, addressId);
        boolean wasDefault = Boolean.TRUE.equals(address.getIsDefault());

        addressRepository.delete(address);

        // Si c'était l'adresse par défaut, promouvoir la plus récente
        if (wasDefault) {
            addressRepository
                    .findFirstByUserIdOrderByCreatedAtDesc(userId)
                    .ifPresent(
                            newDefault -> {
                                newDefault.setIsDefault(true);
                                addressRepository.save(newDefault);
                            });
        }

        log.info("Adresse {} supprimée pour l'utilisateur {}", addressId, userId);
    }

    @Override
    @Transactional
    public AddressResponse setDefaultAddress(UUID userId, UUID addressId) {
        Address address = findAddressOrThrow(userId, addressId);

        if (Boolean.TRUE.equals(address.getIsDefault())) {
            throw new BadRequestException("Cette adresse est déjà l'adresse par défaut");
        }

        resetDefaultAddresses(userId);
        address.setIsDefault(true);
        Address saved = addressRepository.save(address);

        log.info("Adresse {} définie par défaut pour l'utilisateur {}", addressId, userId);
        return addressMapper.toResponse(saved);
    }

    // ═══════════════════════════════════════════════════════════
    // UTILITAIRES
    // ═══════════════════════════════════════════════════════════

    private Address findAddressOrThrow(UUID userId, UUID addressId) {
        return addressRepository
                .findByIdAndUserId(addressId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Address", "id", addressId));
    }

    private void resetDefaultAddresses(UUID userId) {
        addressRepository.resetDefaultByUserId(userId);
    }
}
