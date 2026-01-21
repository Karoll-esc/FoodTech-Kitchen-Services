package com.foodtech.kitchen.infrastructure.rest;

import com.foodtech.kitchen.application.ports.in.GetOrderStatusPort;
import com.foodtech.kitchen.application.ports.in.ProcessOrderPort;
import com.foodtech.kitchen.domain.model.Order;
import com.foodtech.kitchen.domain.model.Task;
import com.foodtech.kitchen.domain.model.TaskStatus;
import com.foodtech.kitchen.infrastructure.rest.dto.CreateOrderRequest;
import com.foodtech.kitchen.infrastructure.rest.dto.CreateOrderResponse;
import com.foodtech.kitchen.infrastructure.rest.mapper.OrderMapper;
import com.foodtech.kitchen.infrastructure.security.Permissions;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private static final String ORDER_SUCCESS_MESSAGE = "Order processed successfully";

    private final ProcessOrderPort processOrderPort;
    private final GetOrderStatusPort getOrderStatusPort;

    public OrderController(ProcessOrderPort processOrderPort,
                           GetOrderStatusPort getOrderStatusPort) {
        this.processOrderPort = processOrderPort;
        this.getOrderStatusPort = getOrderStatusPort;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('" + Permissions.CREATE_ORDERS + "')")
    public ResponseEntity<CreateOrderResponse> createOrder(@RequestBody CreateOrderRequest request) {
        Order order = OrderMapper.toDomain(request);
        List<Task> tasks = processOrderPort.execute(order);
        CreateOrderResponse response = new CreateOrderResponse(
                order.getTableNumber(),
                tasks.size(),
                ORDER_SUCCESS_MESSAGE
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{orderId}/status")
    @PreAuthorize("hasAuthority('" + Permissions.READ_ORDERS + "')")
    public ResponseEntity<Map<String, String>> getOrderStatus(@PathVariable Long orderId) {
        TaskStatus status = getOrderStatusPort.execute(orderId);
        return ResponseEntity.ok(Map.of(
                "orderId", orderId.toString(),
                "status", status.name()
        ));
    }
}