package com.interview.order.notification;

import com.interview.order.entity.Order;

public abstract class Notification {

    protected final NotificationProperties props;

    protected Notification(NotificationProperties props) {
        this.props = props;
    }

    public abstract void send(Order order, String eventType);
}

