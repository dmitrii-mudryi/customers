package com.webflux.repository;

import com.webflux.model.Order;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Repository
public class CustomOrderRepository {

    private final DatabaseClient databaseClient;

    public CustomOrderRepository(DatabaseClient databaseClient) {
        this.databaseClient = databaseClient;
    }

    public Flux<Order> searchOrdersByCustomerData(Map<String, String> searchParams, int limit, int offset) {
        StringBuilder query = new StringBuilder("SELECT * FROM orders WHERE 1=1");
        searchParams.forEach((key, value) -> {
            if (!value.isEmpty()) {
                String columnName = convertToSnakeCase(key);
                query.append(" AND ").append(columnName).append(" LIKE '%").append(value).append("%'");
            }
        });
        query.append(" LIMIT ").append(limit).append(" OFFSET ").append(offset);

        return databaseClient.sql(query.toString())
                .map((row, metadata) -> new Order(
                        row.get("order_number", Long.class),
                        row.get("first_name", String.class),
                        row.get("last_name", String.class),
                        row.get("telephone_number", String.class),
                        row.get("email", String.class),
                        row.get("delivery_address", String.class),
                        row.get("number_of_customers", Integer.class),
                        row.get("order_total", BigDecimal.class),
                        row.get("order_date", LocalDateTime.class)))
                .all();
    }

    private String convertToSnakeCase(String camelCase) {
        StringBuilder snakeCase = new StringBuilder();
        for (char c : camelCase.toCharArray()) {
            if (Character.isUpperCase(c)) {
                snakeCase.append("_").append(Character.toLowerCase(c));
            } else {
                snakeCase.append(c);
            }
        }
        return snakeCase.toString();
    }
}
