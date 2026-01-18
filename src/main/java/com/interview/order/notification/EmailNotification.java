package com.interview.order.notification;

import com.interview.order.entity.Order;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class EmailNotification extends Notification {

    private static final Logger logger = LoggerFactory.getLogger(EmailNotification.class);

    public EmailNotification(NotificationProperties props) {
        super(props);
    }

    @Override
    public void send(Order order, String eventType) {
        List<String> emails = props.getEmails();
        if (emails.isEmpty()) {
            logger.info("EmailNotification: no recipient configured, skipping");
            return;
        }
        // Simulate sending via logs; replace with real mail sender if needed
        for (String to : emails) {
            logger.info("Sending EMAIL to {} about event {} for order id={} product={}", to, eventType, order.getId(), order.getProductName());
        }
    }
}

