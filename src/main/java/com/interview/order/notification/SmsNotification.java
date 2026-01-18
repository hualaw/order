package com.interview.order.notification;

import com.interview.order.entity.Order;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SmsNotification extends Notification {

    private static final Logger logger = LoggerFactory.getLogger(SmsNotification.class);

    public SmsNotification(NotificationProperties props) {
        super(props);
    }

    @Override
    public void send(Order order, String eventType) {
        List<String> phones = props.getPhones();
        if (phones.isEmpty()) {
            logger.info("SmsNotification: no phone configured, skipping");
            return;
        }
        // Simulate SMS send via logging. Integrate with SMS provider in real app.
        for (String phone : phones) {
            logger.info("Sending SMS to {} about event {} for order id={} product={}", phone, eventType, order.getId(), order.getProductName());
        }
    }
}

