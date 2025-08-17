package org.ebndrnk.orderservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.ebndrnk.orderservice.model.dto.OrderRequest;
import org.ebndrnk.orderservice.model.dto.OrderResponse;
import org.ebndrnk.orderservice.model.dto.UpdateOrderRequest;
import org.ebndrnk.orderservice.model.entity.OrderStatus;
import org.ebndrnk.orderservice.service.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;


    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@RequestBody @Valid OrderRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.createOrder(request));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getOrderById(@PathVariable Long orderId) {
        return ResponseEntity.ok(orderService.getById(orderId));
    }

    @GetMapping
    public ResponseEntity<List<OrderResponse>> getOrdersByIds(@RequestParam List<Long> orderIds) {
        return ResponseEntity.ok(orderService.getByIds(orderIds));
    }

    @GetMapping("/by-status")
    public ResponseEntity<List<OrderResponse>> getOrdersByStatus(@RequestParam List<OrderStatus> statuses) {
        return ResponseEntity.ok(orderService.getByStatuses(statuses));
    }

    @PatchMapping("/{orderId}")
    public ResponseEntity<OrderResponse> updateOrder(
            @PathVariable Long orderId,
            @RequestBody @Valid UpdateOrderRequest request) {
        return ResponseEntity.ok(orderService.updateById(orderId, request));
    }

    @DeleteMapping("/{orderId}")
    public ResponseEntity<Void> deleteOrderById(@PathVariable Long orderId) {
        orderService.deleteById(orderId);
        return ResponseEntity.ok().build();
    }
}
