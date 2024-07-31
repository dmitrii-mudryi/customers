package com.webflux.service;

import com.webflux.model.Order;
import com.webflux.repository.CustomOrderRepository;
import com.webflux.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CustomOrderRepository customOrderRepository;

    @Mock
    private KafkaTemplate<String, Order> kafkaTemplate;

    @InjectMocks
    private OrderService orderService;

    @Captor
    private ArgumentCaptor<Order> orderCaptor;

    private Order order;
    private Order savedOrder;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        order = new Order(null, "John", "Doe", "1234567890", "john.doe@example.com", "123 Main St", 10, null, LocalDateTime.now());
        savedOrder = new Order(1L, "John", "Doe", "1234567890", "john.doe@example.com", "123 Main St", 10, BigDecimal.valueOf(13.3), LocalDateTime.now());
    }

    @Test
    public void testCreateOrder() {
        when(orderRepository.save(any(Order.class))).thenReturn(Mono.just(savedOrder));

        CompletableFuture<SendResult<String, Order>> future = CompletableFuture.completedFuture(new SendResult<>(null, null));
        when(kafkaTemplate.send(anyString(), any(Order.class))).thenReturn(future);

        Mono<Order> result = orderService.createOrder(order);

        StepVerifier.create(result)
                .expectNext(savedOrder)
                .verifyComplete();

        verify(orderRepository, times(1)).save(orderCaptor.capture());
        verify(kafkaTemplate, times(1)).send(eq("orders"), orderCaptor.capture());

        Order capturedOrder = orderCaptor.getValue();
        assertThat(capturedOrder.getFirstName()).isEqualTo("John");
        assertThat(capturedOrder.getLastName()).isEqualTo("Doe");
        assertThat(capturedOrder.getNumberOfCustomers()).isEqualTo(10);
        assertThat(capturedOrder.getOrderTotal()).isEqualByComparingTo(BigDecimal.valueOf(13.30));
    }

    @Test
    public void testUpdateOrderWithin5Minutes() {
        when(orderRepository.findById(anyLong())).thenReturn(Mono.just(savedOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(Mono.just(savedOrder));

        Mono<Order> result = orderService.updateOrder(1L, order);

        StepVerifier.create(result)
                .expectNext(savedOrder)
                .verifyComplete();
    }

    @Test
    public void testUpdateOrderAfter5Minutes() {
        LocalDateTime fiveMinutesAgo = LocalDateTime.now().minusMinutes(6);
        Order existingOrder = new Order(1L, "John", "Doe", "1234567890", "john.doe@example.com", "123 Main St", 10, BigDecimal.valueOf(13.3), fiveMinutesAgo);
        Mockito.when(orderRepository.findById(1L)).thenReturn(Mono.just(existingOrder));

        Order updatedOrder = new Order(1L, "John", "Doe", "1234567890", "john.doe@example.com", "456 Elm St", 15, BigDecimal.valueOf(19.95), fiveMinutesAgo);

        StepVerifier.create(orderService.updateOrder(1L, updatedOrder))
                .expectErrorMatches(throwable -> throwable instanceof RuntimeException && throwable.getMessage().equals("Order cannot be updated after 5 minutes."))
                .verify();
    }

    @Test
    public void testSearchOrdersByCustomerData() {
        when(customOrderRepository.searchOrdersByCustomerData(anyMap(), any(Integer.class), any(Integer.class))).thenReturn(Flux.just(savedOrder));
        Map<String, String> searchParams = new HashMap<>();
        searchParams.put("firstName", "John");

        Flux<Order> result = orderService.searchOrdersByCustomerData(searchParams, 10, 0);

        StepVerifier.create(result)
                .expectNext(savedOrder)
                .verifyComplete();
    }
}
