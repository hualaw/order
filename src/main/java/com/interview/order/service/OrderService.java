package com.interview.order.service;

import com.interview.order.entity.Order;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;
import java.util.Optional;

public interface OrderService {

    Order createOrder(com.interview.order.web.CreateOrderRequest req);

    Optional<Order> getOrder(Long id);

    enum UpdateResult {
        SUCCESS,
        NOT_FOUND,
        NOT_ALLOWED,
        FAILED
    }

    UpdateResult updateOrderStatus(Long id, int statusCode);

    Page<Order> search(String productName, String customer, Integer statusCode, LocalDateTime startTime, LocalDateTime endTime, int start, int count);
}

