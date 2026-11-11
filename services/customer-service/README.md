# customer-service

> Customer profiles: the first business microservice of TicketFlow.

## Concept illustrated
A standalone Spring Boot REST service with its own database. It owns customer
data (name, email, phone). Authentication will be delegated to Keycloak later;
this service only manages the business profile.

## Overview
- **Port:** 8081
- **Database:** MySQL (`customer_service` schema)
- **Depends on:** MySQL

| Method | Path | Description |
|---|---|---|
| POST | `/customer-service/api/v1/customers` | Create a customer (201 + Location) |
| GET | `/customer-service/api/v1/customers/{id}` | Get one customer |
| GET | `/customer-service/api/v1/customers` | List customers |
| PUT | `/customer-service/api/v1/customers/{id}` | Update a customer |
| DELETE | `/customer-service/api/v1/customers/{id}` | Delete a customer |

Errors follow RFC 9457 (`ProblemDetail`): 400 validation, 404 not found, 409 duplicate email.

## Package layout
    controllers/   REST endpoints
    service/       business logic
    repos/         Spring Data repositories
    entities/      JPA entities
    dto/           request/response records (the API contract)
    exceptions/    domain exceptions and their HTTP mapping

## Run locally
Start MySQL with `docker compose -f infra/docker-compose.yml up -d mysql`, then:

    ./mvnw -pl services/customer-service spring-boot:run

The default connection is `localhost:3307` with user/password `ticketflow`.
Override it with `DB_HOST`, `DB_PORT`, `DB_USERNAME`, and `DB_PASSWORD`.

    curl -i -X POST localhost:8080/customer-service/api/v1/customers \
      -H 'Content-Type: application/json' \
      -d '{"firstName":"Sara","lastName":"Idrissi","email":"sara@example.com"}'

The service runs locally; only its MySQL database runs in Docker. Start the
database with `docker compose -f infra/docker-compose.yml up -d mysql`.

## Run the tests
    ./mvnw -pl services/customer-service test

## Exercise
1. Add a `GET /api/v1/customers?email=...` search endpoint with a test.

## Common pitfalls
- Exposing JPA entities directly in the API couples your contract to your
  database schema: that is why we use `CustomerResponse`.
- `open-in-view` is disabled on purpose to avoid lazy-loading surprises.
- An in-memory database is fine for learning but never shared between
  services: each service owns its data.
