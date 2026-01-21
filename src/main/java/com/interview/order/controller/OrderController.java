package com.interview.order.controller;

import com.interview.order.entity.Order;
import com.interview.order.service.OrderService;
import com.interview.order.web.ApiRestResponse;
import com.interview.order.web.CreateOrderRequest;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.ConstraintViolationException;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/order")
@Validated
public class OrderController {

    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);

    private final OrderService orderService;

    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/create")
    public ResponseEntity<ApiRestResponse<Map<String, Long>>> createOrder(@RequestBody CreateOrderRequest req) {
        try {
            Order saved = orderService.createOrder(req);
            Map<String, Long> data = Map.of("id", saved.getId());
            logger.info("createOrder: created order id={} product={} customer={}", saved.getId(), saved.getProductName(), saved.getCustomer());
            return ResponseEntity.ok(ApiRestResponse.success(data));
        } catch (Exception e) {
            logger.error("createOrder: failed to create order for product={} customer={}.", req == null ? null : req.getProductName(), req == null ? null : req.getCustomer(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiRestResponse.error());
        }
    }

    @GetMapping("/retrieve")
    public ResponseEntity<ApiRestResponse<Map<String, Object>>> retrieveOrder(@RequestParam("id") Long id) {
        try {
            Optional<Order> o = orderService.getOrder(id);
            if (o.isEmpty()) {
                logger.warn("retrieveOrder: order not found id={}", id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiRestResponse.error(ApiRestResponse.NOT_FOUND_CODE, ApiRestResponse.NOT_FOUND_MSG));
            }

            Order order = o.get();
            Map<String, Object> data = Map.of(
                    "productName", order.getProductName(),
                    "totalAmount", order.getTotalAmount(),
                    "status", order.getStatus() == null ? null : order.getStatus().getCode(),
                    "customer", order.getCustomer(),
                    "currency", order.getCurrency(),
                    "createtime", order.getCreateTime() == null ? null : order.getCreateTime().format(dtf),
                    "updatetime", order.getUpdateTime() == null ? null : order.getUpdateTime().format(dtf)
            );

            logger.info("retrieveOrder: returned order id={} product={} customer={}", id, order.getProductName(), order.getCustomer());
            return ResponseEntity.ok(ApiRestResponse.success(data));
        } catch (Exception e) {
            logger.error("retrieveOrder: unexpected error when retrieving order id={}.", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiRestResponse.error());
        }
    }

    @PatchMapping("/update")
    public ResponseEntity<ApiRestResponse<Object>> updateOrderStatus(@RequestParam("id") Long id,
                                                                      @RequestParam("status") @NotNull @Min(1) @Max(3) Integer statusCode) {
        try {
            OrderService.UpdateResult res = orderService.updateOrderStatus(id, statusCode);
            switch (res) {
                case SUCCESS:
                    logger.info("updateOrderStatus: updated order id={} to status={}", id, statusCode);
                    return ResponseEntity.ok(ApiRestResponse.success());
                case NOT_FOUND:
                    logger.warn("updateOrderStatus: order not found id={} status={}", id, statusCode);
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiRestResponse.error(ApiRestResponse.NOT_FOUND_CODE, ApiRestResponse.NOT_FOUND_MSG));
                case NOT_ALLOWED:
                    logger.warn("updateOrderStatus: status update not allowed for order id={} newStatus={}", id, statusCode);
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiRestResponse.error(ApiRestResponse.NOT_ALLOWED_CODE, ApiRestResponse.NOT_ALLOWED_MSG));
                default:
                    logger.error("updateOrderStatus: update failed for order id={} newStatus={}", id, statusCode);
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiRestResponse.error(ApiRestResponse.ERROR_CODE, ApiRestResponse.UPDATE_FAILED_MSG));
            }
        } catch (Exception e) {
            logger.error("updateOrderStatus: unexpected error updating order id={} to status={}.", id, statusCode, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiRestResponse.error());
        }
    }

    @GetMapping("/search")
    public ResponseEntity<ApiRestResponse<Map<String, Object>>> searchOrders(
            @RequestParam(value = "productName", required = false) String productName,
            @RequestParam(value = "customer", required = true) String customer,
            @RequestParam(value = "status", required = false) Integer statusCode,
            @RequestParam(value = "starttime", required = false) String startTimeStr,
            @RequestParam(value = "endtime", required = false) String endTimeStr,
            @RequestParam(value = "start", defaultValue = "0") Integer start,
            @RequestParam(value = "count", defaultValue = "10") Integer count
    ) {
        try {
            if (start < 0 || count <= 0) {
                logger.warn("searchOrders: invalid paging parameters start={} count={}", start, count);
                return ResponseEntity.badRequest().body(ApiRestResponse.error());
            }

            LocalDateTime startTime = null;
            LocalDateTime endTime = null;
            if (startTimeStr != null && !startTimeStr.isBlank()) startTime = LocalDateTime.parse(startTimeStr, dtf);
            if (endTimeStr != null && !endTimeStr.isBlank()) endTime = LocalDateTime.parse(endTimeStr, dtf);

            Page<Order> pageResult = orderService.search(productName, customer, statusCode, startTime, endTime, start, count);

            List<Map<String, Object>> orders = pageResult.getContent().stream().map(order -> {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("productName", order.getProductName());
                m.put("totalAmount", order.getTotalAmount());
                m.put("customer", order.getCustomer());
                m.put("currency", order.getCurrency());
                m.put("status", order.getStatus() == null ? null : order.getStatus().getCode());
                m.put("createtime", order.getCreateTime() == null ? null : order.getCreateTime().format(dtf));
                m.put("updatetime", order.getUpdateTime() == null ? null : order.getUpdateTime().format(dtf));
                return m;
            }).collect(Collectors.toList());

            Map<String, Object> data = new LinkedHashMap<>();
            data.put("orders", orders);
            data.put("total", pageResult.getTotalElements());

            logger.info("searchOrders: returned {} orders for productName={} customer={} status={}", orders.size(), productName, customer, statusCode);
            return ResponseEntity.ok(ApiRestResponse.success(data));
        } catch (Exception e) {
            logger.error("searchOrders: unexpected error during search productName={} customer={} status={}.", productName, customer, statusCode, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiRestResponse.error());
        }
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiRestResponse<Object>> handleValidationException(ConstraintViolationException ex) {
        logger.warn("handleValidationException: validation failure - {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiRestResponse.error(ApiRestResponse.NOT_ALLOWED_CODE, ApiRestResponse.NOT_ALLOWED_MSG));
    }
}
