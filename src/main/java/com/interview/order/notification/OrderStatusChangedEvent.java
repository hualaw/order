package com.interview.order.notification;

import com.interview.order.entity.Order;
import org.springframework.context.ApplicationEvent;

public class OrderStatusChangedEvent extends ApplicationEvent {
    private final Order order;
    private final int oldStatus;
    private final int newStatus;

    public OrderStatusChangedEvent(Object source, Order order, int oldStatus, int newStatus) {
        super(source);
        this.order = order;
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
    }

    public Order getOrder() {
        return order;
    }

    public int getOldStatus() {
        return oldStatus;
    }

    public int getNewStatus() {
        return newStatus;
    }
}

