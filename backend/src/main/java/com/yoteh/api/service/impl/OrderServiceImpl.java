package com.yoteh.api.service.impl;

import com.yoteh.api.dto.request.OrderRequest;
import com.yoteh.api.dto.response.OrderListResponse;
import com.yoteh.api.dto.response.OrderResponse;
import com.yoteh.api.dto.response.common.PagedResponse;
import com.yoteh.api.entity.*;
import com.yoteh.api.entity.Address;
import com.yoteh.api.entity.Cart;
import com.yoteh.api.entity.CartItem;
import com.yoteh.api.entity.Order;
import com.yoteh.api.entity.OrderItem;
import com.yoteh.api.entity.User;
import com.yoteh.api.entity.enums.OrderItemStatus;
import com.yoteh.api.entity.enums.OrderStatus;
import com.yoteh.api.exception.BadRequestException;
import com.yoteh.api.exception.ResourceNotFoundException;
import com.yoteh.api.mapper.OrderMapper;
import com.yoteh.api.repository.AddressRepository;
import com.yoteh.api.repository.CartItemRepository;
import com.yoteh.api.repository.CartRepository;
import com.yoteh.api.repository.OrderItemRepository;
import com.yoteh.api.repository.OrderRepository;
import com.yoteh.api.repository.UserRepository;
import com.yoteh.api.service.NotificationService;
import com.yoteh.api.service.OrderService;
import com.yoteh.api.util.Constants;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final AddressRepository addressRepository;
    private final UserRepository userRepository;
    private final OrderMapper orderMapper;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public OrderResponse createOrder(UUID userId, OrderRequest request) {
        User user =
                userRepository
                        .findById(userId)
                        .orElseThrow(
                                () -> new ResourceNotFoundException("Utilisateur", "id", userId));

        // Récupérer le panier avec les articles
        Cart cart =
                cartRepository
                        .findByUserIdWithItems(userId)
                        .orElseThrow(() -> new BadRequestException("Votre panier est vide"));

        List<CartItem> cartItems = cart.getItems();
        if (cartItems == null || cartItems.isEmpty()) {
            throw new BadRequestException("Votre panier est vide");
        }

        // Vérifier l'adresse de livraison
        Address shippingAddress =
                addressRepository
                        .findById(request.getShippingAddressId())
                        .orElseThrow(
                                () ->
                                        new ResourceNotFoundException(
                                                "Adresse de livraison",
                                                "id",
                                                request.getShippingAddressId()));

        if (!shippingAddress.getUser().getId().equals(userId)) {
            throw new BadRequestException("Cette adresse ne vous appartient pas");
        }

        // Vérifier le stock pour chaque article
        for (CartItem cartItem : cartItems) {
            int availableStock;
            String productName;
            if (cartItem.getVariant() != null) {
                availableStock = cartItem.getVariant().getStock();
            } else {
                availableStock = cartItem.getProduct().getStock();
            }
            productName = cartItem.getProduct().getName();
            if (cartItem.getQuantity() > availableStock) {
                throw new BadRequestException(
                        "Stock insuffisant pour "
                                + productName
                                + ". Disponible : "
                                + availableStock);
            }
        }

        // Générer un numéro de commande unique
        String orderNumber = generateOrderNumber();

        // Créer la commande avec snapshot de l'adresse
        Order order = new Order();
        order.setOrderNumber(orderNumber);
        order.setUser(user);
        order.setStatus(OrderStatus.PENDING);
        order.setSubtotal(cart.getSubtotal());
        order.setDiscountAmount(
                cart.getDiscountAmount() != null ? cart.getDiscountAmount() : BigDecimal.ZERO);
        order.setShippingAmount(BigDecimal.ZERO);
        order.setTaxAmount(BigDecimal.ZERO);
        order.setTotal(cart.getTotal());
        order.setCurrency(
                request.getCurrency() != null ? request.getCurrency() : Constants.CURRENCY_XOF);
        order.setPromotionCode(cart.getPromotionCode());
        order.setCustomerNote(request.getCustomerNote());

        // Snapshot de l'adresse de livraison
        order.setShippingFirstName(shippingAddress.getFirstName());
        order.setShippingLastName(shippingAddress.getLastName());
        order.setShippingPhone(shippingAddress.getPhone());
        order.setShippingStreet(shippingAddress.getStreet());
        order.setShippingCity(shippingAddress.getCity());
        order.setShippingState(shippingAddress.getState());
        order.setShippingPostalCode(shippingAddress.getPostalCode());
        order.setShippingCountry(shippingAddress.getCountry());

        Order savedOrder = orderRepository.save(order);

        // Créer les lignes de commande à partir du panier
        List<OrderItem> orderItems = new ArrayList<>();
        for (CartItem cartItem : cartItems) {
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(savedOrder);
            orderItem.setProduct(cartItem.getProduct());
            orderItem.setVariant(cartItem.getVariant());
            orderItem.setProductName(cartItem.getProduct().getName());
            orderItem.setProductSku(
                    cartItem.getVariant() != null
                            ? cartItem.getVariant().getSku()
                            : cartItem.getProduct().getSku());
            orderItem.setProductImage(getPrimaryImageUrl(cartItem.getProduct()));
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setUnitPrice(cartItem.getUnitPrice());
            orderItem.setTotal(cartItem.getLineTotal());
            orderItem.setDiscountAmount(BigDecimal.ZERO);
            orderItem.setStatus(OrderItemStatus.PENDING);

            // Info variante (ex: "Taille: L, Couleur: Rouge")
            if (cartItem.getVariant() != null) {
                orderItem.setVariantInfo(buildVariantInfo(cartItem.getVariant()));
            }

            orderItems.add(orderItem);

            // Décrémenter le stock
            if (cartItem.getVariant() != null) {
                var variant = cartItem.getVariant();
                variant.setStock(variant.getStock() - cartItem.getQuantity());
            } else {
                var product = cartItem.getProduct();
                product.setStock(product.getStock() - cartItem.getQuantity());
            }
        }

        orderItemRepository.saveAll(orderItems);

        // Vider le panier après la commande
        cartItemRepository.deleteAllByCartId(cart.getId());
        cart.setDiscountAmount(BigDecimal.ZERO);
        cart.setPromotionCode(null);
        cartRepository.save(cart);

        log.info("Commande {} créée pour l'utilisateur {}", orderNumber, userId);

        // Recharger avec détails
        Order fullOrder =
                orderRepository.findByIdWithDetails(savedOrder.getId()).orElse(savedOrder);
        return orderMapper.toOrderResponse(fullOrder);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderById(UUID userId, UUID orderId) {
        Order order =
                orderRepository
                        .findByIdWithDetails(orderId)
                        .orElseThrow(
                                () -> new ResourceNotFoundException("Commande", "id", orderId));

        if (!order.getUser().getId().equals(userId)) {
            throw new BadRequestException("Cette commande ne vous appartient pas");
        }

        return orderMapper.toOrderResponse(order);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderByNumber(UUID userId, String orderNumber) {
        Order order =
                orderRepository
                        .findByOrderNumberWithDetails(orderNumber)
                        .orElseThrow(
                                () ->
                                        new ResourceNotFoundException(
                                                "Commande", "numéro", orderNumber));

        if (!order.getUser().getId().equals(userId)) {
            throw new BadRequestException("Cette commande ne vous appartient pas");
        }

        return orderMapper.toOrderResponse(order);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<OrderListResponse> getUserOrders(
            UUID userId, OrderStatus status, int page, int size) {

        size = Math.min(size, Constants.MAX_PAGE_SIZE);
        Pageable pageable = PageRequest.of(page, size);

        Page<Order> orderPage;
        if (status != null) {
            orderPage = orderRepository.findByUserIdAndStatus(userId, status, pageable);
        } else {
            orderPage = orderRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        }

        List<OrderListResponse> content =
                orderMapper.toOrderListResponseList(orderPage.getContent());

        return PagedResponse.of(
                content,
                orderPage.getNumber(),
                orderPage.getSize(),
                orderPage.getTotalElements(),
                orderPage.getTotalPages(),
                orderPage.isLast());
    }

    @Override
    @Transactional
    public OrderResponse cancelOrder(UUID userId, UUID orderId, String reason) {
        Order order =
                orderRepository
                        .findByIdWithDetails(orderId)
                        .orElseThrow(
                                () -> new ResourceNotFoundException("Commande", "id", orderId));

        if (!order.getUser().getId().equals(userId)) {
            throw new BadRequestException("Cette commande ne vous appartient pas");
        }

        if (order.getStatus() != OrderStatus.PENDING && order.getStatus() != OrderStatus.PAID) {
            throw new BadRequestException(
                    "Impossible d'annuler une commande au statut : " + order.getStatus());
        }

        restoreStock(order);

        order.setStatus(OrderStatus.CANCELLED);
        order.setCancelReason(reason);
        order.setCancelledAt(LocalDateTime.now());
        orderRepository.save(order);
        notificationService.sendOrderCancelledEmail(order);

        log.info("Commande {} annulée par l'utilisateur {}", order.getOrderNumber(), userId);
        return orderMapper.toOrderResponse(order);
    }

    // ─── Admin ────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<OrderListResponse> getAllOrders(
            OrderStatus status,
            String search,
            LocalDateTime from,
            LocalDateTime to,
            int page,
            int size) {

        size = Math.min(size, Constants.MAX_PAGE_SIZE);
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<Order> orderPage =
                orderRepository.findAllWithFilters(status, search, from, to, pageable);

        List<OrderListResponse> content =
                orderMapper.toOrderListResponseList(orderPage.getContent());

        return PagedResponse.of(
                content,
                orderPage.getNumber(),
                orderPage.getSize(),
                orderPage.getTotalElements(),
                orderPage.getTotalPages(),
                orderPage.isLast());
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderDetailAdmin(UUID orderId) {
        Order order =
                orderRepository
                        .findByIdWithDetails(orderId)
                        .orElseThrow(
                                () -> new ResourceNotFoundException("Commande", "id", orderId));
        return orderMapper.toOrderResponse(order);
    }

    @Override
    @Transactional
    public OrderResponse updateOrderStatus(UUID orderId, OrderStatus newStatus) {
        Order order =
                orderRepository
                        .findByIdWithDetails(orderId)
                        .orElseThrow(
                                () -> new ResourceNotFoundException("Commande", "id", orderId));

        validateStatusTransition(order.getStatus(), newStatus);

        order.setStatus(newStatus);

        switch (newStatus) {
            case SHIPPED -> order.setShippedAt(LocalDateTime.now());
            case DELIVERED -> order.setDeliveredAt(LocalDateTime.now());
            case CANCELLED -> {
                order.setCancelledAt(LocalDateTime.now());
                restoreStock(order);
            }
            default -> {
                // PENDING, PAID, PREPARING : pas de timestamp spécifique
            }
        }

        orderRepository.save(order);
        log.info("Commande {} passée au statut {}", order.getOrderNumber(), newStatus);

        switch (newStatus) {
            case PAID -> notificationService.sendOrderConfirmationEmail(order);
            case SHIPPED -> notificationService.sendOrderShippedEmail(order);
            case DELIVERED -> notificationService.sendOrderDeliveredEmail(order);
            case CANCELLED -> notificationService.sendOrderCancelledEmail(order);
            default -> {
                // Pas de notification pour les autres statuts
            }
        }
        return orderMapper.toOrderResponse(order);
    }

    // ─── Méthodes privées ───────────────────────────────────

    private String generateOrderNumber() {
        String prefix = "YTH";
        String timestamp = String.valueOf(System.currentTimeMillis()).substring(5);
        String random = String.valueOf((int) (Math.random() * 9000) + 1000);
        String orderNumber = prefix + "-" + timestamp + "-" + random;

        while (orderRepository.existsByOrderNumber(orderNumber)) {
            random = String.valueOf((int) (Math.random() * 9000) + 1000);
            orderNumber = prefix + "-" + timestamp + "-" + random;
        }
        return orderNumber;
    }

    private void validateStatusTransition(OrderStatus currentStatus, OrderStatus newStatus) {
        boolean valid =
                switch (currentStatus) {
                    case PENDING ->
                            newStatus == OrderStatus.PAID || newStatus == OrderStatus.CANCELLED;
                    case PAID ->
                            newStatus == OrderStatus.PREPARING
                                    || newStatus == OrderStatus.CANCELLED;
                    case PREPARING ->
                            newStatus == OrderStatus.SHIPPED || newStatus == OrderStatus.CANCELLED;
                    case SHIPPED -> newStatus == OrderStatus.DELIVERED;
                    case DELIVERED -> newStatus == OrderStatus.REFUNDED;
                    case CANCELLED -> newStatus == OrderStatus.REFUNDED;
                    case REFUNDED -> false;
                };

        if (!valid) {
            throw new BadRequestException(
                    "Transition de statut invalide : " + currentStatus + " → " + newStatus);
        }
    }

    private void restoreStock(Order order) {
        if (order.getItems() == null) {
            return;
        }
        for (OrderItem item : order.getItems()) {
            if (item.getVariant() != null) {
                var variant = item.getVariant();
                variant.setStock(variant.getStock() + item.getQuantity());
            } else if (item.getProduct() != null) {
                var product = item.getProduct();
                product.setStock(product.getStock() + item.getQuantity());
            }
        }
    }

    private String getPrimaryImageUrl(Product product) {
        if (product.getImages() == null || product.getImages().isEmpty()) {
            return null;
        }
        return product.getImages().stream()
                .filter(ProductImage::getIsPrimary)
                .findFirst()
                .map(ProductImage::getUrl)
                .orElse(product.getImages().get(0).getUrl());
    }

    private String buildVariantInfo(ProductVariant variant) {
        StringBuilder info = new StringBuilder();
        if (variant.getSize() != null) {
            info.append("Taille: ").append(variant.getSize());
        }
        if (variant.getColor() != null) {
            if (info.length() > 0) {
                info.append(", ");
            }
            info.append("Couleur: ").append(variant.getColor());
        }
        if (variant.getMaterial() != null) {
            if (info.length() > 0) {
                info.append(", ");
            }
            info.append("Matière: ").append(variant.getMaterial());
        }
        return info.length() > 0 ? info.toString() : variant.getSku();
    }
}
