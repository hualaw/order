package com.interview.order.notification;

import com.interview.order.entity.Order;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class NotificationDispatcher {

    private final NotificationProperties props;
    private final EmailNotification emailNotification;
    private final SmsNotification smsNotification;

    public NotificationDispatcher(NotificationProperties props, EmailNotification emailNotification, SmsNotification smsNotification) {
        this.props = props;
        this.emailNotification = emailNotification;
        this.smsNotification = smsNotification;
    }

    @EventListener
    public void onOrderCreated(OrderCreatedEvent event) {
        dispatch(event.getOrder(), "ORDER_CREATED");
    }

    @EventListener
    public void onOrderStatusChanged(OrderStatusChangedEvent event) {
        dispatch(event.getOrder(), "ORDER_STATUS_CHANGED");
    }

    private void dispatch(Order order, String eventType) {
        List<String> types = props.getTypes();
        if (types.isEmpty()) return; // nothing to do

        for (String t : types) {
            if ("email".equalsIgnoreCase(t)) {
                emailNotification.send(order, eventType);
            } else if ("sms".equalsIgnoreCase(t)) {
                smsNotification.send(order, eventType);
            }
        }
    }
}

