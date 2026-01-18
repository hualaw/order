package com.interview.order.service.impl;

import com.interview.order.entity.Order;
import com.interview.order.entity.OrderStatus;
import com.interview.order.notification.OrderCreatedEvent;
import com.interview.order.notification.OrderStatusChangedEvent;
import com.interview.order.repository.OrderRepository;
import com.interview.order.service.OrderService;
import com.interview.order.web.CreateOrderRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Autowired
    public OrderServiceImpl(OrderRepository orderRepository, ApplicationEventPublisher eventPublisher) {
        this.orderRepository = orderRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional
    public Order createOrder(CreateOrderRequest req) {
        Order order = new Order();
        order.setProductName(req.getProductName());
        order.setCustomer(req.getCustomer());
        order.setTotalAmount(req.getTotalAmount());
        order.setCurrency(req.getCurrency() == null ? "RMB" : req.getCurrency());
        order.setStatus(OrderStatus.CREATED);
        LocalDateTime now = LocalDateTime.now();
        order.setCreateTime(now);
        order.setUpdateTime(now);
        Order saved = orderRepository.save(order);

        // publish event
        eventPublisher.publishEvent(new OrderCreatedEvent(this, saved));
        return saved;
    }

    @Override
    public Optional<Order> getOrder(Long id) {
        return orderRepository.findById(id);
    }

    @Override
    @Transactional
    public UpdateResult updateOrderStatus(Long id, int statusCode) {
        Optional<Order> o = orderRepository.findById(id);
        if (o.isEmpty()) return UpdateResult.NOT_FOUND;

        Order order = o.get();
        OrderStatus current = order.getStatus();
        if (current == null || current != OrderStatus.CREATED) {
            return UpdateResult.NOT_ALLOWED;
        }

        OrderStatus target;
        try {
            target = OrderStatus.fromCode(statusCode);
        } catch (IllegalArgumentException ex) {
            return UpdateResult.NOT_ALLOWED;
        }

        if (target == OrderStatus.COMPLETED || target == OrderStatus.CANCELLED) {
            int oldCode = current == null ? 0 : current.getCode();
            order.setStatus(target);
            order.setUpdateTime(LocalDateTime.now());
            orderRepository.save(order);

            // publish status changed event
            eventPublisher.publishEvent(new OrderStatusChangedEvent(this, order, oldCode, target.getCode()));
            return UpdateResult.SUCCESS;
        } else {
            return UpdateResult.NOT_ALLOWED;
        }
    }

    @Override
    public Page<Order> search(String productName, String customer, Integer statusCode, LocalDateTime startTime, LocalDateTime endTime, int start, int count) {
        OrderStatus status = null;
        if (statusCode != null) {
            try {
                status = OrderStatus.fromCode(statusCode);
            } catch (IllegalArgumentException ex) {
                status = null; // let repository handle null => no filter
            }
        }
        int page = Math.max(0, start / Math.max(1, count));
        Pageable pageable = PageRequest.of(page, Math.max(1, count));
        return orderRepository.search(productName, customer, status, startTime, endTime, pageable);
    }
}
