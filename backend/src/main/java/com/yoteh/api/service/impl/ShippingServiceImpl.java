package com.yoteh.api.service.impl;

import com.yoteh.api.dto.request.ShippingCalculateRequest;
import com.yoteh.api.dto.request.ShippingZoneRequest;
import com.yoteh.api.dto.response.ShippingCalculateResponse;
import com.yoteh.api.dto.response.ShippingZoneResponse;
import com.yoteh.api.dto.response.common.PagedResponse;
import com.yoteh.api.entity.ShippingZone;
import com.yoteh.api.exception.BadRequestException;
import com.yoteh.api.exception.ResourceNotFoundException;
import com.yoteh.api.mapper.ShippingZoneMapper;
import com.yoteh.api.repository.ShippingZoneRepository;
import com.yoteh.api.service.ShippingService;
import com.yoteh.api.util.Constants;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShippingServiceImpl implements ShippingService {

    private final ShippingZoneRepository shippingZoneRepository;
    private final ShippingZoneMapper shippingZoneMapper;

    // ── Publiques ─────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<ShippingZoneResponse> getActiveZones() {
        return shippingZoneRepository
                .findByIsActiveTrueAndDeletedAtIsNullOrderBySortOrderAsc()
                .stream()
                .map(this::toResponseWithDelivery)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ShippingZoneResponse> findZonesByCity(String city) {
        if (city == null || city.isBlank()) {
            return getActiveZones();
        }
        return shippingZoneRepository.findActiveByCity(city.trim()).stream()
                .map(this::toResponseWithDelivery)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ShippingZoneResponse getActiveZoneById(UUID id) {
        ShippingZone zone =
                shippingZoneRepository
                        .findByIdAndIsActiveTrueAndDeletedAtIsNull(id)
                        .orElseThrow(
                                () ->
                                        new ResourceNotFoundException(
                                                "ShippingZone", "id", id.toString()));
        return toResponseWithDelivery(zone);
    }

    @Override
    @Transactional(readOnly = true)
    public ShippingCalculateResponse calculateShipping(ShippingCalculateRequest request) {
        ShippingZone zone =
                shippingZoneRepository
                        .findByIdAndIsActiveTrueAndDeletedAtIsNull(request.getZoneId())
                        .orElseThrow(
                                () ->
                                        new ResourceNotFoundException(
                                                "ShippingZone",
                                                "id",
                                                request.getZoneId().toString()));

        BigDecimal weightKg =
                request.getWeightKg() != null ? request.getWeightKg() : BigDecimal.ZERO;

        BigDecimal shippingFee = zone.calculateFee(weightKg, request.getOrderTotal());
        boolean isFreeShipping = shippingFee.compareTo(BigDecimal.ZERO) == 0;

        ShippingCalculateResponse response = new ShippingCalculateResponse();
        response.setZoneId(zone.getId());
        response.setZoneName(zone.getName());
        response.setShippingFee(shippingFee);
        response.setIsFreeShipping(isFreeShipping);
        response.setOrderTotal(request.getOrderTotal());
        response.setWeightKg(weightKg);
        response.setEstimatedDaysMin(zone.getEstimatedDaysMin());
        response.setEstimatedDaysMax(zone.getEstimatedDaysMax());
        response.setEstimatedDelivery(
                buildDeliveryLabel(zone.getEstimatedDaysMin(), zone.getEstimatedDaysMax()));
        response.setFreeShippingThreshold(zone.getFreeShippingThreshold());

        log.debug(
                "Calcul livraison zone={} fee={} freeShipping={}",
                zone.getName(),
                shippingFee,
                isFreeShipping);
        return response;
    }

    // ── Admin ─────────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<ShippingZoneResponse> getAllZones(
            int page, int size, String sortBy, String sortDir, String search) {
        int pageSize = Math.min(size, Constants.MAX_PAGE_SIZE);
        Sort sort =
                sortDir.equalsIgnoreCase("asc")
                        ? Sort.by(sortBy).ascending()
                        : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, pageSize, sort);

        Page<ShippingZone> zonePage =
                (search != null && !search.isBlank())
                        ? shippingZoneRepository.findAllWithSearch(search.trim(), pageable)
                        : shippingZoneRepository.findByDeletedAtIsNullOrderBySortOrderAsc(pageable);

        List<ShippingZoneResponse> content =
                zonePage.getContent().stream()
                        .map(this::toResponseWithDelivery)
                        .collect(Collectors.toList());

        return PagedResponse.of(
                content,
                zonePage.getNumber(),
                zonePage.getSize(),
                zonePage.getTotalElements(),
                zonePage.getTotalPages(),
                zonePage.isLast());
    }

    @Override
    @Transactional(readOnly = true)
    public ShippingZoneResponse getZoneById(UUID id) {
        ShippingZone zone =
                shippingZoneRepository
                        .findByIdAndDeletedAtIsNull(id)
                        .orElseThrow(
                                () ->
                                        new ResourceNotFoundException(
                                                "ShippingZone", "id", id.toString()));
        return toResponseWithDelivery(zone);
    }

    @Override
    @Transactional
    public ShippingZoneResponse createZone(ShippingZoneRequest request) {
        if (shippingZoneRepository.existsByNameAndDeletedAtIsNull(request.getName())) {
            throw new BadRequestException(
                    "Une zone portant ce nom existe déjà : " + request.getName());
        }
        validateDays(request);

        ShippingZone zone = shippingZoneMapper.toEntity(request);
        ShippingZone saved = shippingZoneRepository.save(zone);

        log.info("Zone de livraison créée : id={} name={}", saved.getId(), saved.getName());
        return toResponseWithDelivery(saved);
    }

    @Override
    @Transactional
    public ShippingZoneResponse updateZone(UUID id, ShippingZoneRequest request) {
        ShippingZone zone =
                shippingZoneRepository
                        .findByIdAndDeletedAtIsNull(id)
                        .orElseThrow(
                                () ->
                                        new ResourceNotFoundException(
                                                "ShippingZone", "id", id.toString()));

        if (shippingZoneRepository.existsByNameAndIdNotAndDeletedAtIsNull(request.getName(), id)) {
            throw new BadRequestException(
                    "Une zone portant ce nom existe déjà : " + request.getName());
        }
        validateDays(request);

        shippingZoneMapper.updateEntity(request, zone);
        ShippingZone saved = shippingZoneRepository.save(zone);

        log.info("Zone de livraison mise à jour : id={}", id);
        return toResponseWithDelivery(saved);
    }

    @Override
    @Transactional
    public ShippingZoneResponse toggleZoneStatus(UUID id) {
        ShippingZone zone =
                shippingZoneRepository
                        .findByIdAndDeletedAtIsNull(id)
                        .orElseThrow(
                                () ->
                                        new ResourceNotFoundException(
                                                "ShippingZone", "id", id.toString()));

        zone.setIsActive(!Boolean.TRUE.equals(zone.getIsActive()));
        ShippingZone saved = shippingZoneRepository.save(zone);

        log.info("Zone id={} : isActive → {}", id, saved.getIsActive());
        return toResponseWithDelivery(saved);
    }

    @Override
    @Transactional
    public void deleteZone(UUID id) {
        ShippingZone zone =
                shippingZoneRepository
                        .findByIdAndDeletedAtIsNull(id)
                        .orElseThrow(
                                () ->
                                        new ResourceNotFoundException(
                                                "ShippingZone", "id", id.toString()));

        zone.setDeletedAt(LocalDateTime.now());
        zone.setIsActive(false);
        shippingZoneRepository.save(zone);

        log.info("Zone de livraison supprimée (soft delete) : id={}", id);
    }

    // ── Méthodes privées ──────────────────────────────────────────────────────

    private ShippingZoneResponse toResponseWithDelivery(ShippingZone zone) {
        ShippingZoneResponse response = shippingZoneMapper.toResponse(zone);
        response.setEstimatedDelivery(
                buildDeliveryLabel(zone.getEstimatedDaysMin(), zone.getEstimatedDaysMax()));
        return response;
    }

    private String buildDeliveryLabel(Integer min, Integer max) {
        if (min == null && max == null) {
            return "Délai non précisé";
        }
        if (min == null || min.equals(max)) {
            return max + " jour" + (max > 1 ? "s" : "");
        }
        return min + "-" + max + " jours";
    }

    private void validateDays(ShippingZoneRequest request) {
        Integer min = request.getEstimatedDaysMin();
        Integer max = request.getEstimatedDaysMax();
        if (min != null && max != null && min > max) {
            throw new BadRequestException(
                    "Le délai minimum ("
                            + min
                            + ") ne peut pas dépasser le délai maximum ("
                            + max
                            + ")");
        }
    }
}
