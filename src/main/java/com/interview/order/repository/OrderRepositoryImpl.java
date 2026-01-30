package com.interview.order.repository;

import com.interview.order.entity.Order;
import com.interview.order.entity.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Repository
@Transactional(readOnly = true)
public class OrderRepositoryImpl implements OrderRepositoryCustom {

    @PersistenceContext
    private EntityManager em;

    @Override
    public Page<Order> search(String productName, String customer, OrderStatus status, LocalDateTime startTime, LocalDateTime endTime, Pageable pageable) {
        CriteriaBuilder cb = em.getCriteriaBuilder();

        // 主查询
        CriteriaQuery<Order> cq = cb.createQuery(Order.class);
        Root<Order> root = cq.from(Order.class);
        List<Predicate> predicates = new ArrayList<>();

        // 参数表达式（命名参数）
        ParameterExpression<String> pProduct = cb.parameter(String.class, "productName");
        ParameterExpression<String> pCustomer = cb.parameter(String.class, "customer");
        ParameterExpression<OrderStatus> pStatus = cb.parameter(OrderStatus.class, "status");
        ParameterExpression<LocalDateTime> pStart = cb.parameter(LocalDateTime.class, "startTime");
        ParameterExpression<LocalDateTime> pEnd = cb.parameter(LocalDateTime.class, "endTime");

        if (productName != null) {
            // 使用参数绑定构造 LIKE，避免字符串直接拼接到 JPQL/SQL
            Expression<String> lowered = cb.lower(root.get("productName"));
            // pattern: '%' || lower(:productName) || '%'
            Expression<String> pattern = cb.concat(cb.concat(cb.literal("%"), cb.lower(pProduct)), cb.literal("%"));
            predicates.add(cb.like(lowered, pattern));
        }
        if (customer != null) {
            predicates.add(cb.equal(root.get("customer"), pCustomer));
        }
        if (status != null) {
            predicates.add(cb.equal(root.get("status"), pStatus));
        }
        if (startTime != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("createTime"), pStart));
        }
        if (endTime != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get("createTime"), pEnd));
        }

        cq.where(predicates.toArray(new Predicate[0]));

        // 可根据 pageable 的 Sort 添加 order by（此处简化为不处理 Sort）
        TypedQuery<Order> query = em.createQuery(cq);

        // 绑定参数到主查询（只在对应值非 null 时绑定）
        if (productName != null) query.setParameter("productName", productName);
        if (customer != null) query.setParameter("customer", customer);
        if (status != null) query.setParameter("status", status);
        if (startTime != null) query.setParameter("startTime", startTime);
        if (endTime != null) query.setParameter("endTime", endTime);

        // 分页
        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());
        List<Order> content = query.getResultList();

        // 计数查询
        CriteriaQuery<Long> countQ = cb.createQuery(Long.class);
        Root<Order> countRoot = countQ.from(Order.class);
        List<Predicate> countPredicates = new ArrayList<>();

        if (productName != null) {
            Expression<String> lowered = cb.lower(countRoot.get("productName"));
            Expression<String> pattern = cb.concat(cb.concat(cb.literal("%"), cb.lower(cb.parameter(String.class, "productName"))), cb.literal("%"));
            countPredicates.add(cb.like(lowered, pattern));
        }
        if (customer != null) {
            countPredicates.add(cb.equal(countRoot.get("customer"), cb.parameter(String.class, "customer")));
        }
        if (status != null) {
            countPredicates.add(cb.equal(countRoot.get("status"), cb.parameter(OrderStatus.class, "status")));
        }
        if (startTime != null) {
            countPredicates.add(cb.greaterThanOrEqualTo(countRoot.get("createTime"), cb.parameter(LocalDateTime.class, "startTime")));
        }
        if (endTime != null) {
            countPredicates.add(cb.lessThanOrEqualTo(countRoot.get("createTime"), cb.parameter(LocalDateTime.class, "endTime")));
        }

        countQ.select(cb.count(countRoot));
        if (!countPredicates.isEmpty()) {
            countQ.where(countPredicates.toArray(new Predicate[0]));
        }

        TypedQuery<Long> countQuery = em.createQuery(countQ);
        if (productName != null) countQuery.setParameter("productName", productName);
        if (customer != null) countQuery.setParameter("customer", customer);
        if (status != null) countQuery.setParameter("status", status);
        if (startTime != null) countQuery.setParameter("startTime", startTime);
        if (endTime != null) countQuery.setParameter("endTime", endTime);

        long total = countQuery.getSingleResult();

        return new PageImpl<>(content, pageable, total);
    }
}
