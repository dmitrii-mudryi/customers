package com.webflux.controller;

import com.webflux.model.Order;
import com.webflux.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequestMapping("/api/orders")
@Validated
public class OrderController {

    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    @Operation(summary = "Create a new order")
    public Mono<ResponseEntity<Order>> createOrder(@Valid @RequestBody Order order) {
        logger.debug("Received request to create order: {}", order);
        return orderService.createOrder(order)
                .map(savedOrder -> ResponseEntity.status(HttpStatus.CREATED).body(savedOrder));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing order")
    public Mono<ResponseEntity<Order>> updateOrder(@PathVariable Long id, @Valid @RequestBody Order order) {
        logger.info("Received request to update order with ID: {}", id);
        return orderService.updateOrder(id, order)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PostMapping("/search")
    @PreAuthorize("hasRole('ROLE_USER')")
    @Operation(summary = "Search orders by customer data")
    public Flux<Order> searchOrdersByCustomerData(@RequestBody Map<String, String> searchParams,
                                                  @RequestParam(defaultValue = "10") int limit,
                                                  @RequestParam(defaultValue = "0") int offset) {
        logger.debug("Received request to search orders with params: {}", searchParams);
        return orderService.searchOrdersByCustomerData(searchParams, limit, offset);
    }
}

