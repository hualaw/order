package com.interview.order.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.interview.order.entity.Order;
import com.interview.order.entity.OrderStatus;
import com.interview.order.service.OrderService;
import com.interview.order.web.CreateOrderRequest;
import com.interview.order.web.ApiRestResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class OrderControllerTest {

    private MockMvc mockMvc;

    @Mock
    private OrderService orderService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private Order sampleOrder;

    @BeforeEach
    void setUp() {
        // build controller with mocked service using standalone MockMvc
        OrderController controller = new OrderController(orderService);
        this.mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        sampleOrder = new Order();
        sampleOrder.setId(10L);
        sampleOrder.setProductName("Gadget");
        sampleOrder.setCustomer("Bob");
        sampleOrder.setTotalAmount(new BigDecimal("99.99"));
        sampleOrder.setCurrency("USD");
        sampleOrder.setStatus(OrderStatus.CREATED);
        sampleOrder.setCreateTime(LocalDateTime.now());
        sampleOrder.setUpdateTime(LocalDateTime.now());
    }

    @Test
    void createOrder_ok() throws Exception {
        CreateOrderRequest req = new CreateOrderRequest();
        req.setProductName("Gadget");
        req.setCustomer("Bob");
        req.setTotalAmount(new BigDecimal("99.99"));

        when(orderService.createOrder(any(CreateOrderRequest.class))).thenReturn(sampleOrder);

        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value(ApiRestResponse.OK_CODE))
                .andExpect(jsonPath("$.data.id").value(10));

        verify(orderService, times(1)).createOrder(any());
    }

    @Test
    void retrieveOrder_found() throws Exception {
        when(orderService.getOrder(10L)).thenReturn(Optional.of(sampleOrder));

        mockMvc.perform(get("/orders/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ApiRestResponse.OK_CODE))
                .andExpect(jsonPath("$.data.productName").value("Gadget"));
    }

    @Test
    void retrieveOrder_notFound() throws Exception {
        when(orderService.getOrder(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/orders/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(ApiRestResponse.NOT_FOUND_CODE));
    }

    @Test
    void updateOrder_success() throws Exception {
        when(orderService.updateOrderStatus(10L, OrderStatus.COMPLETED.getCode())).thenReturn(OrderService.UpdateResult.SUCCESS);

        mockMvc.perform(patch("/orders/10/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\": " + OrderStatus.COMPLETED.getCode() + "}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ApiRestResponse.OK_CODE));
    }

    @Test
    void updateOrder_notFound() throws Exception {
        when(orderService.updateOrderStatus(99L, OrderStatus.COMPLETED.getCode())).thenReturn(OrderService.UpdateResult.NOT_FOUND);

        mockMvc.perform(patch("/orders/99/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\": " + OrderStatus.COMPLETED.getCode() + "}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(ApiRestResponse.NOT_FOUND_CODE));
    }

    @Test
    void searchOrders_ok() throws Exception {
        Page<Order> page = new PageImpl<>(List.of(sampleOrder), PageRequest.of(0, 10), 1);
        when(orderService.search(nullable(String.class), eq("Bob"), any(), any(), any(), anyInt(), anyInt())).thenReturn(page);

        mockMvc.perform(get("/orders").param("start", "0").param("count", "10").param("customer", "Bob"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ApiRestResponse.OK_CODE))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.orders[0].productName").value("Gadget"));

        verify(orderService, times(1)).search(nullable(String.class), eq("Bob"), any(), any(), any(), anyInt(), anyInt());
    }

    @Test
    void directSearchInvocation() {
        // setup
        OrderController controller = new OrderController(orderService);
        Page<Order> page = new PageImpl<>(List.of(sampleOrder), PageRequest.of(0, 10), 1);
        when(orderService.search(anyString(), anyString(), any(), any(), any(), anyInt(), anyInt())).thenReturn(page);

        try {
            var resp = controller.searchOrders(null, "Bob", null, null, null, 0, 10, null, null);
            System.out.println("[DIRECT] status=" + resp.getStatusCode().value() + " body=" + resp.getBody());
        } catch (Exception ex) {
            System.out.println("[DIRECT] exception: " + ex);
            ex.printStackTrace(System.out);
            throw ex;
        }
    }
}
