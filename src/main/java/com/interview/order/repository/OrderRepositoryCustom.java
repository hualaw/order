package com.interview.order.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.interview.order.entity.Order;
import com.interview.order.entity.OrderStatus;
import java.time.LocalDateTime;

public interface OrderRepositoryCustom {
    // 动态查询接口，使用 Criteria API 实现以避免字符串拼接 SQL/JPQL
    Page<Order> search(String productName,
                       String customer,
                       OrderStatus status,
                       LocalDateTime startTime,
                       LocalDateTime endTime,
                       Pageable pageable);
}
