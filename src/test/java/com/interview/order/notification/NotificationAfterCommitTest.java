package com.interview.order.notification;

import com.interview.order.entity.Order;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;

import static org.mockito.Mockito.*;

/**
 * Verifies that {@link NotificationDispatcher} only sends notifications after the
 * publishing transaction commits (via {@code @TransactionalEventListener(AFTER_COMMIT)}).
 * Uses a minimal hand-built context and a no-DB transaction manager: commit/rollback
 * still trigger the transaction synchronizations the listener hooks into.
 */
class NotificationAfterCommitTest {

    private AnnotationConfigApplicationContext context;
    private EmailNotification emailNotification;
    private SmsNotification smsNotification;
    private PlatformTransactionManager transactionManager;

    @BeforeEach
    void setUp() {
        emailNotification = mock(EmailNotification.class);
        smsNotification = mock(SmsNotification.class);
        NotificationProperties props = mock(NotificationProperties.class);
        when(props.getTypes()).thenReturn(List.of("email", "sms"));

        transactionManager = new AbstractPlatformTransactionManager() {
            @Override
            protected Object doGetTransaction() {
                return new Object();
            }

            @Override
            protected void doBegin(Object transaction, TransactionDefinition definition) {
            }

            @Override
            protected void doCommit(DefaultTransactionStatus status) {
            }

            @Override
            protected void doRollback(DefaultTransactionStatus status) {
            }
        };

        context = new AnnotationConfigApplicationContext();
        context.registerBean("transactionManager", PlatformTransactionManager.class, () -> transactionManager);
        context.registerBean("notificationDispatcher", NotificationDispatcher.class,
                () -> new NotificationDispatcher(props, emailNotification, smsNotification));
        context.refresh();
    }

    @AfterEach
    void tearDown() {
        context.close();
    }

    private Order newOrder() {
        Order order = new Order();
        order.setId(1L);
        order.setProductName("Widget");
        return order;
    }

    @Test
    void orderCreated_notificationSentOnlyAfterCommit() {
        Order order = newOrder();
        TransactionTemplate tx = new TransactionTemplate(transactionManager);

        tx.executeWithoutResult(status -> {
            context.publishEvent(new OrderCreatedEvent(this, order));
            // a plain @EventListener would already have fired here, inside the transaction
            verifyNoInteractions(emailNotification, smsNotification);
        });

        verify(emailNotification).send(order, "ORDER_CREATED");
        verify(smsNotification).send(order, "ORDER_CREATED");
    }

    @Test
    void orderStatusChanged_notificationSentOnlyAfterCommit() {
        Order order = newOrder();
        TransactionTemplate tx = new TransactionTemplate(transactionManager);

        tx.executeWithoutResult(status -> {
            context.publishEvent(new OrderStatusChangedEvent(this, order, 1, 2));
            verifyNoInteractions(emailNotification, smsNotification);
        });

        verify(emailNotification).send(order, "ORDER_STATUS_CHANGED");
        verify(smsNotification).send(order, "ORDER_STATUS_CHANGED");
    }

    @Test
    void notificationSkippedOnRollback() {
        Order order = newOrder();
        TransactionTemplate tx = new TransactionTemplate(transactionManager);

        tx.executeWithoutResult(status -> {
            context.publishEvent(new OrderCreatedEvent(this, order));
            status.setRollbackOnly();
        });

        verifyNoInteractions(emailNotification, smsNotification);
    }

    @Test
    void notificationSkippedWithoutActiveTransaction() {
        // default fallbackExecution=false: events published outside a transaction are dropped
        context.publishEvent(new OrderCreatedEvent(this, newOrder()));

        verifyNoInteractions(emailNotification, smsNotification);
    }
}
