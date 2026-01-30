package com.interview.order.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.interview.order.entity.Order;
import com.interview.order.entity.OrderStatus;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    //fuzzy match for productName, exact match for customer and status, createTime between startTime and endTime
    @Query("SELECT o FROM Order o WHERE " +
            "(:productName IS NULL OR LOWER(o.productName) LIKE LOWER(CONCAT('%',:productName,'%'))) AND " +
            "(:customer IS NULL OR o.customer = :customer) AND " +
            "(:status IS NULL OR o.status = :status) AND " +
            "(:startTime IS NULL OR o.createTime >= :startTime) AND " +
            "(:endTime IS NULL OR o.createTime <= :endTime)")
    Page<Order> search(
            @Param("productName") String productName,
            @Param("customer") String customer,
            @Param("status") OrderStatus status,
            @Param("startTime") java.time.LocalDateTime startTime,
            @Param("endTime") java.time.LocalDateTime endTime,
            Pageable pageable

            //Querydsl
    );
}
