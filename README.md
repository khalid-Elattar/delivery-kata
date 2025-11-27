# Delivery Slot Booking System

A reactive delivery slot booking system built with **Spring Boot 3**, **WebFlux**, and **Domain-Driven Design** (DDD) principles following **Hexagonal Architecture**.

## Table of Contents

- [Overview](#overview)
- [Architecture](#architecture)
- [Tech Stack](#tech-stack)
- [Getting Started](#getting-started)
- [Project Structure](#project-structure)
- [API Endpoints](#api-endpoints)
- [Business Rules](#business-rules)
- [Configuration](#configuration)
- [Testing](#testing)
- [Docker Services](#docker-services)

---

## Overview

This system allows customers to book delivery slots across different delivery modes:

| Mode | Description |
|------|-------------|
| **DRIVE** | Customer picks up at store |
| **DELIVERY** | Standard home delivery |
| **DELIVERY_TODAY** | Same-day delivery |
| **DELIVERY_ASAP** | Express delivery (within 4 hours) |

Each mode has specific business rules for slot duration, available days, time ranges, and booking constraints.

---

## Architecture

The project follows **Hexagonal Architecture** (Ports & Adapters) with DDD tactical patterns:

```
┌─────────────────────────────────────────────────────────────────┐
│                      INTERFACES (REST API)                       │
│   Controllers, DTOs, Mappers, HATEOAS Assemblers                │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                     APPLICATION LAYER                            │
│   Use Cases (Ports In), Services, Commands, Queries             │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                       DOMAIN LAYER                               │
│   Entities, Value Objects, Domain Services, Events              │
│   (Slot, Booking, User, DeliveryMode, BookingValidator)         │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                   INFRASTRUCTURE LAYER                           │
│   Adapters (Ports Out): R2DBC, Redis, Kafka, Security           │
└─────────────────────────────────────────────────────────────────┘
```

### Key Patterns

- **CQRS**: Separate command and query DTOs
- **Event-Driven**: Domain events published via Kafka
- **Repository Pattern**: Abstract persistence behind ports
- **Rich Domain Model**: Business logic in domain entities

---

## Tech Stack

| Category | Technology |
|----------|------------|
| **Framework** | Spring Boot 3.2.5, WebFlux |
| **Language** | Java 21 |
| **Database** | PostgreSQL 16 (R2DBC) |
| **Cache** | Redis 7 |
| **Messaging** | Apache Kafka |
| **Migration** | Liquibase |
| **Security** | Spring Security |
| **API Docs** | SpringDoc OpenAPI 3 |
| **Mapping** | MapStruct |
| **Testing** | JUnit 5, Testcontainers |

---

## Getting Started

### Prerequisites

- Java 21+
- Docker & Docker Compose
- Maven 3.8+

### Quick Start

1. **Start infrastructure services:**

```bash
docker-compose up -d
```

2. **Build and run the application:**

```bash
./mvnw clean spring-boot:run
```

3. **Access the API:**

- API: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui.html
- Kafka UI: http://localhost:8090

### Default Credentials

The system creates a default admin user on startup:

- **Email**: `admin@delivery.com`
- **Password**: `admin123`

---

## Project Structure

```
src/main/java/com/crafteam/delivery/
├── application/                    # Application Layer
│   ├── dto/
│   │   ├── command/               # Write operations (BookSlotCommand, etc.)
│   │   ├── query/                 # Read operations (SlotAvailabilityQuery)
│   │   └── response/              # Response DTOs
│   ├── port/
│   │   ├── in/                    # Input ports (Use Cases)
│   │   └── out/                   # Output ports (Repositories, Publishers)
│   └── service/                   # Use Case implementations
│
├── domain/                         # Domain Layer
│   ├── exception/                 # Domain exceptions
│   ├── event/                     # Domain events
│   ├── model/
│   │   ├── booking/              # Booking aggregate
│   │   ├── slot/                 # Slot aggregate (DeliveryMode, TimeSlot)
│   │   ├── user/                 # User aggregate
│   │   └── shared/               # Shared value objects
│   └── service/                   # Domain services (BookingValidator)
│
├── infrastructure/                 # Infrastructure Layer
│   ├── adapter/out/
│   │   ├── cache/                # Redis cache adapter
│   │   ├── messaging/kafka/      # Kafka event publisher
│   │   ├── persistence/          # R2DBC repositories
│   │   └── security/             # Password encoder
│   └── config/                    # Spring configurations
│
└── interfaces/                     # Interface Layer
    └── rest/
        ├── dto/                   # Web DTOs (request/response)
        ├── hateoas/              # HATEOAS assemblers
        ├── mapper/               # Web mappers
        ├── *Controller.java      # REST controllers
        └── GlobalExceptionHandler.java
```

---

## API Endpoints

### Authentication

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/v1/auth/register` | Register new user |
| `POST` | `/api/v1/auth/login` | Login user |

### Slots

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| `GET` | `/api/v1/slots?mode={mode}&date={date}` | Get available slots | User |
| `GET` | `/api/v1/slots/availability?mode={mode}&date={date}` | Get slot availability with rules | User |
| `GET` | `/api/v1/slots/all` | Get all slots | User |
| `POST` | `/api/v1/slots` | Create a slot | Admin |

### Bookings

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| `POST` | `/api/v1/bookings` | Book a slot | User |
| `GET` | `/api/v1/bookings/{id}` | Get booking by ID | User |
| `GET` | `/api/v1/bookings/user/{userId}` | Get user's bookings | User |
| `DELETE` | `/api/v1/bookings/{id}` | Cancel booking | User |

### Users

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| `GET` | `/api/v1/users/{id}` | Get user profile | User |
| `PUT` | `/api/v1/users/{id}` | Update user profile | User |
| `PUT` | `/api/v1/users/{id}/password` | Change password | User |

---

## Business Rules

### Delivery Modes

| Mode | Slot Duration | Days | Hours | Min Advance | Max Advance | Capacity |
|------|--------------|------|-------|-------------|-------------|----------|
| DRIVE | 1h | Mon-Sat | 08:00-20:00 | 2h | 14 days | 10 |
| DELIVERY | 2h | Mon-Fri | 09:00-21:00 | 24h | 7 days | 5 |
| DELIVERY_TODAY | 1h | 7 days | 10:00-22:00 | 3h | Today only | 3 |
| DELIVERY_ASAP | 30min | 7 days | 08:00-23:00 | 30min | 4h window | 2 |

### Common Rules

| Rule | Description |
|------|-------------|
| **RG01** | One active reservation per slot per user |
| **RG02** | Cannot book slots in the past |
| **RG03** | Slots have maximum capacity |
| **RG04** | Date must be valid for delivery mode |
| **RG05** | Time must be within mode's operating hours |
| **RG06** | Day must be an available day for mode |
| **RG07** | Cancellation allowed until 1h before slot |
| **RG08** | Max 3 active bookings per user |

### Special Rules

- **DELIVERY_TODAY**: Cutoff time at 19:00 (no bookings after)
- **DELIVERY_ASAP**: Maximum 4-hour booking window ahead

---

## Configuration

### Application Properties

Key configuration in `application.yaml`:

```yaml
delivery:
  booking:
    max-active-per-user: 3
    cancellation-deadline-hours: 1

  modes:
    drive:
      slot-duration: 60
      start-time: "08:00"
      end-time: "20:00"
      min-advance-hours: 2
      max-advance-days: 14
      default-capacity: 10

    delivery:
      slot-duration: 120
      start-time: "09:00"
      end-time: "21:00"
      min-advance-hours: 24
      max-advance-days: 7
      default-capacity: 5

    delivery-today:
      slot-duration: 60
      start-time: "10:00"
      end-time: "22:00"
      min-advance-hours: 3
      max-advance-days: 0
      cutoff-time: "19:00"
      default-capacity: 3

    delivery-asap:
      slot-duration: 30
      start-time: "08:00"
      end-time: "23:00"
      min-advance-minutes: 30
      max-advance-hours: 4
      default-capacity: 2
```

### Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `SPRING_R2DBC_URL` | `r2dbc:postgresql://localhost:5432/delivery` | Database URL |
| `SPRING_REDIS_HOST` | `localhost` | Redis host |
| `SPRING_KAFKA_BOOTSTRAP_SERVERS` | `localhost:29092` | Kafka servers |

---

## Testing

### Run All Tests

```bash
./mvnw test
```

### Test Categories

| Test Type | Location | Description |
|-----------|----------|-------------|
| Unit | `domain/model/*Test.java` | Domain model tests |
| Unit | `domain/service/*Test.java` | Domain service tests |
| Integration | `interfaces/rest/*Test.java` | Controller tests |

### Test Coverage

- **DeliveryModeTest**: 50+ tests covering all business rules
- **BookingValidatorTest**: Validation logic tests
- **SlotControllerTest**: REST API tests
- **BookingControllerTest**: Booking flow tests

---

## Docker Services

### Services Overview

| Service | Port | Description |
|---------|------|-------------|
| PostgreSQL | 5432 | Primary database |
| Redis | 6379 | Caching layer |
| Kafka | 29092 | Event streaming |
| Zookeeper | 2181 | Kafka coordination |
| Kafka UI | 8090 | Kafka management |

### Commands

```bash
# Start all services
docker-compose up -d

# View logs
docker-compose logs -f

# Stop services
docker-compose down

# Reset with clean data
docker-compose down -v
```

---

## Domain Events

The system publishes the following events to Kafka:

| Event | Topic | Trigger |
|-------|-------|---------|
| `SlotCreatedEvent` | `slot-events` | Slot created |
| `SlotBookedEvent` | `slot-events` | Slot booked |
| `BookingConfirmedEvent` | `booking-events` | Booking confirmed |
| `BookingCancelledEvent` | `booking-events` | Booking cancelled |

---

## Error Handling

All errors return a consistent format:

```json
{
  "status": 400,
  "code": "MIN_ADVANCE_TIME_NOT_MET",
  "message": "Le mode DRIVE nécessite une réservation au moins 2 heures à l'avance",
  "timestamp": "2024-01-15T10:00:00Z",
  "details": {
    "mode": "DRIVE",
    "requiredAdvance": "2 hours",
    "requestedSlotTime": "2024-01-15T11:00",
    "currentTime": "2024-01-15T10:00"
  }
}
```

### Error Codes

| Code | HTTP Status | Description |
|------|-------------|-------------|
| `SLOT_NOT_FOUND` | 404 | Slot does not exist |
| `BOOKING_NOT_FOUND` | 404 | Booking does not exist |
| `SLOT_NOT_AVAILABLE` | 409 | Slot is fully booked |
| `USER_ALREADY_BOOKED` | 400 | User already booked this slot |
| `MAX_ACTIVE_BOOKINGS_EXCEEDED` | 400 | User has too many active bookings |
| `MIN_ADVANCE_TIME_NOT_MET` | 400 | Not enough advance time |
| `MAX_ADVANCE_DAYS_EXCEEDED` | 400 | Booking too far in advance |
| `INVALID_DATE_FOR_MODE` | 400 | Date not valid for mode |
| `INVALID_TIME_FOR_MODE` | 400 | Time not in operating hours |
| `CUTOFF_TIME_PASSED` | 400 | DELIVERY_TODAY cutoff passed |
| `ASAP_WINDOW_EXCEEDED` | 400 | Outside ASAP 4h window |
| `CANCELLATION_NOT_ALLOWED` | 400 | Too late to cancel |

---

## License

Copyright (c) 2024 CrafTeam. All rights reserved.
