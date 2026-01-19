package com.interview.order.service.impl;

import com.interview.order.entity.Order;
import com.interview.order.entity.OrderStatus;
import com.interview.order.notification.OrderCreatedEvent;
import com.interview.order.notification.OrderStatusChangedEvent;
import com.interview.order.repository.OrderRepository;
import com.interview.order.service.OrderService;
import com.interview.order.web.CreateOrderRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private OrderServiceImpl orderService;

    @Captor
    ArgumentCaptor<ApplicationEvent> eventCaptor;

    @BeforeEach
    void setUp() {
    }

    @Test
    void createOrder_shouldSaveAndPublishEvent() {
        CreateOrderRequest req = new CreateOrderRequest();
        req.setProductName("Widget");
        req.setCustomer("Alice");
        req.setTotalAmount(new BigDecimal("12.34"));

        Order saved = new Order();
        saved.setId(1L);
        saved.setProductName(req.getProductName());
        saved.setCustomer(req.getCustomer());
        saved.setTotalAmount(req.getTotalAmount());
        saved.setCurrency("RMB");
        saved.setStatus(OrderStatus.CREATED);
        saved.setCreateTime(LocalDateTime.now());
        saved.setUpdateTime(LocalDateTime.now());

        when(orderRepository.save(any(Order.class))).thenReturn(saved);

        Order result = orderService.createOrder(req);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        verify(orderRepository, times(1)).save(any(Order.class));

        verify(eventPublisher, times(1)).publishEvent(eventCaptor.capture());
        ApplicationEvent published = eventCaptor.getValue();
        assertThat(published).isInstanceOf(OrderCreatedEvent.class);
        OrderCreatedEvent evt = (OrderCreatedEvent) published;
        assertThat(evt.getOrder().getId()).isEqualTo(1L);
    }

    @Test
    void updateOrderStatus_success() {
        Order order = new Order();
        order.setId(2L);
        order.setStatus(OrderStatus.CREATED);
        order.setUpdateTime(LocalDateTime.now());

        when(orderRepository.findById(2L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        OrderService.UpdateResult res = orderService.updateOrderStatus(2L, OrderStatus.COMPLETED.getCode());

        assertEquals(OrderService.UpdateResult.SUCCESS, res);
        verify(orderRepository, times(1)).save(any(Order.class));
        verify(eventPublisher, times(1)).publishEvent(eventCaptor.capture());
        ApplicationEvent published = eventCaptor.getValue();
        assertThat(published).isInstanceOf(OrderStatusChangedEvent.class);
        OrderStatusChangedEvent evt = (OrderStatusChangedEvent) published;
        assertThat(evt.getOrder().getId()).isEqualTo(2L);
    }

    @Test
    void updateOrderStatus_notFound() {
        when(orderRepository.findById(99L)).thenReturn(Optional.empty());

        OrderService.UpdateResult res = orderService.updateOrderStatus(99L, OrderStatus.COMPLETED.getCode());

        assertEquals(OrderService.UpdateResult.NOT_FOUND, res);
        verify(orderRepository, never()).save(any());
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void updateOrderStatus_notAllowed_when_current_not_created() {
        Order order = new Order();
        order.setId(3L);
        order.setStatus(OrderStatus.COMPLETED);
        when(orderRepository.findById(3L)).thenReturn(Optional.of(order));

        OrderService.UpdateResult res = orderService.updateOrderStatus(3L, OrderStatus.CANCELLED.getCode());

        assertEquals(OrderService.UpdateResult.NOT_ALLOWED, res);
        verify(orderRepository, never()).save(any());
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void updateOrderStatus_notAllowed_when_invalid_code() {
        Order order = new Order();
        order.setId(4L);
        order.setStatus(OrderStatus.CREATED);
        when(orderRepository.findById(4L)).thenReturn(Optional.of(order));

        OrderService.UpdateResult res = orderService.updateOrderStatus(4L, 999);

        assertEquals(OrderService.UpdateResult.NOT_ALLOWED, res);
        verify(orderRepository, never()).save(any());
        verify(eventPublisher, never()).publishEvent(any());
    }
}
