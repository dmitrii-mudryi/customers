# Spring WebFlux Orders Application

This is a simple Spring WebFlux application to manage orders of customers. The application supports creating, updating, and searching for orders, with validation, security, and Swagger for API documentation.

## Demo Video

[![Watch the video](https://img.youtube.com/vi/I7EmDTu5-m4/maxresdefault.jpg)](https://youtu.be/I7EmDTu5-m4)

## Features

- Create an order of customers, choosing between 5, 10, or 15 customers.
- Update an order within 5 minutes of creation.
- Search orders by customer data.
- Validation of input data.
- Security for search operation (requires authentication).
- Swagger for API documentation.

## Requirements

- Java 17
- Maven
- Lombok
- Docker & Docker Compose (for kafka testing)

## Dependencies
- Spring Boot
- Spring WebFlux
- Spring Data R2DBC
- H2 Database
- Spring Security
- Lombok
- SpringFox (Swagger for API documentation)

## Getting Started

### Clone the Repository

```bash
git clone <repository-url>
cd <repository-directory>
```

### Install

```bash
./mvnw clean install
```

### Run the Application
```bash
./mvnw spring-boot:run
```

### Docker Compose (Optional)
For local testing with kafka use Docker Compose:
```bash
docker-compose up --build
```

### Access app via API Endpoints
With Kafka:
- Use docker and run the command above. This will run stage environment with url http://localhost:8081

Without Kafka:
- This will run dev environment for local API testing with url http://localhost:8082

### Kafka UI
Access via http://localhost:8085

### API Endpoints
#### Create Order
- Endpoint: POST /api/orders
- Note: Allowed 5, 10, 15 customers, non-empty fields, valid email and phone (up to 10 chars)
- Request Body:
```bash
{
    "firstName": "John",
    "lastName": "Doe",
    "telephoneNumber": "1234567890",
    "email": "john.doe@example.com",
    "deliveryAddress": "123 Main St",
    "numberOfCustomers": 10
}
```
- Response: 201 Created
```bash
{
    "orderNumber": 1,
    "firstName": "John",
    "lastName": "Doe",
    "telephoneNumber": "1234567890",
    "email": "john.doe@example.com",
    "deliveryAddress": "123 Main St",
    "numberOfCustomers": 10,
    "orderTotal": 13.30,
    "orderDate": "2024-07-29T12:00:00"
}
```

#### Update Order
- Endpoint: PUT /api/orders/{id}
- Note: Update is allowed within 5 minutes after creation
- Request Body:
```bash
{
    "firstName": "John",
    "lastName": "Doe",
    "telephoneNumber": "1234567890",
    "email": "john.doe@example.com",
    "deliveryAddress": "456 New Address",
    "numberOfCustomers": 15
}
```
- Response: 200 OK
- 
#### Search Orders
- Endpoint: GET /api/orders/search
- Note: Endpoint is secured, please use credentials in Security section. Partial search allowed.
- Query Parameter: query=limit=1&offset=0
```bash
{
    "firstName": "John"
}
```
OR
```bash
{
    "firstName": "J"
}
```
- Response: 200 OK
```bash
[
    {
        "orderNumber": 1,
        "firstName": "John",
        "lastName": "Doe",
        "telephoneNumber": "1234567890",
        "email": "john.doe@example.com",
        "deliveryAddress": "123 Main St",
        "numberOfCustomers": 10,
        "orderTotal": 13.30,
        "orderDate": "2023-07-29T12:00:00"
    }
]
```

### API Documentation
API documentation is available at http://localhost:8082/webjars/swagger-ui/index.html (dev env) after running the application.

### Security
The search operation is secured and requires authentication. Use the following credentials to access the secured endpoint:
- Username: user
- Password: password

### Testing
Run the tests using the following command:
```bash
./mvnw test
```

### Coverage report
Run the tests using the following command:
```bash
./mvnw jacoco:report
```
Go in target - site - jacoco - index.html

### License
This project is licensed under the MIT License.