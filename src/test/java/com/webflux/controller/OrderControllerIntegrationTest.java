package com.webflux.controller;

import com.webflux.model.Order;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class OrderControllerIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    private Order order;

    @BeforeEach
    void setUp() {
        order = new Order(null, "John", "Doe", "1234567890", "john.doe@example.com", "123 Main St", 10, null, LocalDateTime.now());
    }

    @Test
    public void testCreateOrderWithValidCustomersCount() {
        webTestClient.post().uri("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(order), Order.class)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(Order.class)
                .value(responseOrder -> {
                    assertThat(responseOrder.getOrderNumber()).isNotNull();
                    assertThat(responseOrder.getOrderDate()).isNotNull();
                    assertThat(responseOrder.getFirstName()).isEqualTo(order.getFirstName());
                    assertThat(responseOrder.getLastName()).isEqualTo(order.getLastName());
                    assertThat(responseOrder.getTelephoneNumber()).isEqualTo(order.getTelephoneNumber());
                    assertThat(responseOrder.getEmail()).isEqualTo(order.getEmail());
                    assertThat(responseOrder.getDeliveryAddress()).isEqualTo(order.getDeliveryAddress());
                    assertThat(responseOrder.getNumberOfCustomers()).isEqualTo(order.getNumberOfCustomers());
                    assertThat(responseOrder.getOrderTotal()).isEqualByComparingTo(BigDecimal.valueOf(order.getNumberOfCustomers() * 1.33));
                });
    }

    @Test
    public void testCreateOrderWithInvalidCustomersCount() {
        Order invalidOrder = new Order(null, "John", "Doe", "1234567890", "john.doe@example.com", "123 Main St", 7, null, LocalDateTime.now());

        webTestClient.post().uri("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(invalidOrder), Order.class)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody(String.class)
                .value(message -> assertThat(message).contains("Invalid number of customers. Valid values are 5, 10, or 15."));
    }

    @Test
    public void testCreateOrderWithMissingFields() {
        Order invalidOrder = new Order(null, "", "", "", "", "", 10, null, LocalDateTime.now());

        webTestClient.post().uri("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(invalidOrder), Order.class)
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
        webTestClient.post().uri("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(order), Order.class)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(Order.class)
                .value(createdOrder -> {
                    Long orderId = createdOrder.getOrderNumber();
                    order.setOrderNumber(orderId);
                    order.setDeliveryAddress("456 New Address");

                    webTestClient.put().uri("/api/orders/{id}", orderId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .body(Mono.just(order), Order.class)
                            .exchange()
                            .expectStatus().isOk()
                            .expectBody(Order.class)
                            .value(updatedOrder -> assertThat(updatedOrder.getDeliveryAddress()).isEqualTo("456 New Address"));
                });
    }

    @Test
    @WithMockUser(username = "user", password = "password", roles = "USER")
    public void testSearchOrdersByCustomerData() {
        Order searchOrder = new Order(null, "Mary", "Key", "1234567890", "john.doe@example.com", "123 Main St", 10, null, LocalDateTime.now());
        webTestClient.post().uri("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(searchOrder), Order.class)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(Order.class)
                .value(createdOrder -> {
                    Map<String, String> searchParams = new HashMap<>();
                    searchParams.put("firstName", "Mary");

                    webTestClient.post().uri("/api/orders/search?limit=10&offset=0")
                            .contentType(MediaType.APPLICATION_JSON)
                            .body(Mono.just(searchParams), Map.class)
                            .exchange()
                            .expectStatus().isOk()
                            .expectBodyList(Order.class)
                            .hasSize(1)
                            .value(orders -> {
                                Order foundOrder = orders.get(0);
                                assertThat(foundOrder.getFirstName()).isEqualTo("Mary");
                            });
                });
    }
}

