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

@RestController
@RequestMapping("/order")
@Validated
public class OrderController {

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
            return ResponseEntity.ok(ApiRestResponse.success(data));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiRestResponse.error());
        }
    }

    @GetMapping("/retrieve")
    public ResponseEntity<ApiRestResponse<Map<String, Object>>> retrieveOrder(@RequestParam("id") Long id) {
        Optional<Order> o = orderService.getOrder(id);
        if (o.isEmpty()) {
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

        return ResponseEntity.ok(ApiRestResponse.success(data));
    }

    @PatchMapping("/update")
    public ResponseEntity<ApiRestResponse<Object>> updateOrderStatus(@RequestParam("id") Long id,
                                                                      @RequestParam("status") @NotNull @Min(1) @Max(3) Integer statusCode) {
        try {
            OrderService.UpdateResult res = orderService.updateOrderStatus(id, statusCode);
            switch (res) {
                case SUCCESS:
                    return ResponseEntity.ok(ApiRestResponse.success());
                case NOT_FOUND:
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiRestResponse.error(ApiRestResponse.NOT_FOUND_CODE, ApiRestResponse.NOT_FOUND_MSG));
                case NOT_ALLOWED:
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiRestResponse.error(ApiRestResponse.NOT_ALLOWED_CODE, ApiRestResponse.NOT_ALLOWED_MSG));
                default:
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiRestResponse.error(ApiRestResponse.ERROR_CODE, ApiRestResponse.UPDATE_FAILED_MSG));
            }
        } catch (Exception e) {
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
                m.put("createtime", order.getCreateTime() == null ? null : order.getCreateTime().format(dtf));
                m.put("updatetime", order.getUpdateTime() == null ? null : order.getUpdateTime().format(dtf));
                return m;
            }).collect(Collectors.toList());

            Map<String, Object> data = new LinkedHashMap<>();
            data.put("orders", orders);
            data.put("total", pageResult.getTotalElements());

            return ResponseEntity.ok(ApiRestResponse.success(data));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiRestResponse.error());
        }
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiRestResponse<Object>> handleValidationException(ConstraintViolationException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiRestResponse.error(ApiRestResponse.NOT_ALLOWED_CODE, ApiRestResponse.NOT_ALLOWED_MSG));
    }
}
