package com.webflux.service;

import com.webflux.model.Order;
import com.webflux.repository.CustomOrderRepository;
import com.webflux.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;

@Service
public class OrderService {

    @Value("${spring.profiles.active}")
    private String profile;

    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);
    private static final String TOPIC = "orders";

    private final OrderRepository orderRepository;
    private final CustomOrderRepository customOrderRepository;
    private final KafkaTemplate<String, Order> kafkaTemplate;

    public OrderService(OrderRepository orderRepository,
                        CustomOrderRepository customOrderRepository,
                        KafkaTemplate<String, Order> kafkaTemplate) {
        this.orderRepository = orderRepository;
        this.customOrderRepository = customOrderRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    public Mono<Order> createOrder(Order order) {
        order.setOrderTotal(BigDecimal.valueOf(order.getNumberOfCustomers() * 1.33));
        order.setOrderDate(LocalDateTime.now());
        logger.info("Creating order: {}", order);
        return orderRepository.save(order)
                .doOnSuccess(savedOrder -> {
                    logger.info("Order created: {}", savedOrder);
                    sendMessage(savedOrder);
                });
    }

    public Mono<Order> updateOrder(Long id, Order order) {
        logger.info("Updating order with ID: {}", id);
        return orderRepository.findById(id)
                .flatMap(existingOrder -> {
                    if (Duration.between(existingOrder.getOrderDate(), LocalDateTime.now()).toMinutes() <= 5) {
                        existingOrder.setFirstName(order.getFirstName());
                        existingOrder.setLastName(order.getLastName());
                        existingOrder.setTelephoneNumber(order.getTelephoneNumber());
                        existingOrder.setEmail(order.getEmail());
                        existingOrder.setDeliveryAddress(order.getDeliveryAddress());
                        existingOrder.setNumberOfCustomers(order.getNumberOfCustomers());
                        existingOrder.setOrderTotal(BigDecimal.valueOf(order.getNumberOfCustomers() * 1.33));
                        return orderRepository.save(existingOrder);
                    } else {
                        return Mono.error(new RuntimeException("Order cannot be updated after 5 minutes."));
                    }
                });
    }

    public Flux<Order> searchOrdersByCustomerData(Map<String, String> searchParams, int limit, int offset) {
        logger.info("Searching orders with params: {} - limit: {}, offset: {}", searchParams, limit, offset);
        return customOrderRepository.searchOrdersByCustomerData(searchParams, limit, offset)
            .doOnComplete(() -> logger.info("Search completed with params: {}", searchParams))
            .doOnError(e -> logger.error("Error searching orders with params: {}", searchParams, e));
    }

    private void sendMessage(Order order) {
        if (!"dev".equals(profile)) {
            logger.info("Sending order to Kafka: {}", order);
            kafkaTemplate.send(TOPIC, order);
        } else {
            logger.info("Kafka for dev environment is not configured, it's for local testing purposes only. Use stage environment.");
        }
    }
}
