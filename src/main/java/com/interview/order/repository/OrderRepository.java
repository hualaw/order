package com.interview.order.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.interview.order.entity.Order;
import com.interview.order.entity.OrderStatus;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long>, OrderRepositoryCustom {
    // 使用 OrderRepositoryCustom 提供的 search(...) 实现（避免字符串拼接的 JPQL）
}
