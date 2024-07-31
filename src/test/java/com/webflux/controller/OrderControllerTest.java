package com.webflux.controller;

import com.webflux.config.SecurityConfig;
import com.webflux.model.Order;
import com.webflux.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;

@WebFluxTest(OrderController.class)
@Import(SecurityConfig.class)
public class OrderControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private OrderService orderService;

    private Order order;
    private Order savedOrder;

    @BeforeEach
    void setUp() {
        order = new Order(null, "John", "Doe", "1234567890", "john.doe@example.com", "123 Main St", 10, null, null);
        savedOrder = new Order(1L, "John", "Doe", "1234567890", "john.doe@example.com", "123 Main St", 10, BigDecimal.valueOf(13.3), null);

        Mockito.when(orderService.createOrder(order)).thenReturn(Mono.just(savedOrder));
        Mockito.when(orderService.updateOrder(1L, order)).thenReturn(Mono.just(savedOrder));
        Mockito.when(orderService.searchOrdersByCustomerData(anyMap(), any(Integer.class), any(Integer.class))).thenReturn(Flux.just(savedOrder));
    }

    @Test
    public void testCreateOrderWithValidCustomersCount() {
        webTestClient.post().uri("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(order)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(Order.class)
                .isEqualTo(savedOrder);
    }

    @Test
    public void testCreateOrderWithInvalidCustomersCount() {
        Order invalidOrder = new Order(null, "John", "Doe", "1234567890", "john.doe@example.com", "123 Main St", 7, null, LocalDateTime.now());

        webTestClient.post().uri("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(invalidOrder)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody(String.class)
                .isEqualTo("Invalid number of customers. Valid values are 5, 10, or 15.");
    }

    @Test
    public void testCreateOrderWithMissingFields() {
        Order invalidOrder = new Order(null, "", "", "", "", "", 10, null, LocalDateTime.now());

        webTestClient.post().uri("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(invalidOrder)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody(String.class)
                .value(message -> {
                    assertThat(message).contains("First name is required");
                    assertThat(message).contains("Last name is required");
                    assertThat(message).contains("Telephone number is required");
                    assertThat(message).contains("Email is required");
                    assertThat(message).contains("Delivery address is required");
                });
    }

    @Test
    public void testUpdateOrder() {
        webTestClient.put().uri("/api/orders/1")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(order)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Order.class)
                .isEqualTo(savedOrder);
    }

    @Test
    @WithMockUser(username = "user", password = "password", roles = "USER")
    public void testSearchOrdersByCustomerData() {
        Map<String, String> searchParams = new HashMap<>();
        searchParams.put("firstName", "John");

        webTestClient.post().uri("/api/orders/search?limit=10&offset=0")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(searchParams)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Order.class)
                .hasSize(1)
                .contains(savedOrder);
    }
}
