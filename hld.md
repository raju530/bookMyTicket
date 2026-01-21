# HLD (High-Level Design) — BookMyTicket

## Overview
**BookMyTicket** is a Spring Boot–based microservices application for searching shows, viewing seat availability, and booking tickets. A central **API Gateway** exposes a unified HTTP interface and routes requests to domain services.

## Goals
- **Separation of concerns** by domain (theatre, inventory, booking, offers, payment).
- **Consistent external API** through a gateway.
- **Scalable booking flow** with **seat locking** to prevent double-booking.

## Non-Goals (current scope)
- Authentication/authorization and user accounts.
- Distributed tracing, centralized logging, advanced observability.
- Event-driven integration (the system is primarily synchronous HTTP).

## High-Level Architecture

```text
                +----------------------+
                |      Clients         |
                |  (Postman/UI/etc.)   |
                +----------+-----------+
                           |
                           v
                +----------------------+
                |     API Gateway      |
                | (Spring Cloud GW)    |
                +----+---+---+---+-----+
                     |   |   |   |
     +---------------+   |   |   +----------------+
     |                   |   |                    |
     v                   v   v                    v
+------------+     +------------+            +------------+
| Theatre    |     | Inventory  |            | Booking    |
| Service    |     | Service    |            | Service    |
+-----+------+     +-----+------+            +-----+------+
      |                  |                         |
      |                  |                         |
      v                  v                         v
 +-----------+     +-------------+           +-------------+
 | Postgres  |     | Postgres     |           | Postgres     |
 | (theatre) |     | (inventory)  |           | (booking)    |
 +-----------+     +------+-------+           +------+-------+
                         |
                         v
                      +------+
                      | Redis|
                      |(locks)|
                      +------+

                 +------------------+
                 | Offer Service    |
                 +------------------+

                 +------------------+
                 | Payment Service  |
                 +------------------+
```

## Services & Responsibilities

### `api-gateway` (Spring Cloud Gateway)
- Single entry point for clients.
- Routes requests to downstream services.
- Simplifies client integration and hides internal service topology.

### `theatre-service`
- Manages theatre domain:
  - Theatres, screens, seats, shows.
- Provides APIs to:
  - Create theatre/screen/seat layouts.
  - Create shows.
  - Search/browse show metadata (as used by gateway demo flows).

**Data store**: PostgreSQL (JPA/Hibernate).

### `inventory-service`
- Manages seat availability **per show**.
- Implements **seat locking** with TTL to avoid race conditions.
- Provides APIs to:
  - View seat states for a show.
  - Lock seats for a booking attempt.
  - Release/confirm seats as part of booking confirmation.

**Data stores**:
- PostgreSQL for inventory state.
- Redis for lock/TTL behavior.

### `offer-service`
- Computes pricing and discounts via a rule engine.
- Implements offer rules with a strategy/chain approach (examples in README):
  - Third ticket discount
  - Afternoon discount

**Data store**: none required (rules are in code).

### `payment-service`
- Simulates payment processing.
- Provides APIs to “pay” and return a payment result.

**Data store**: none required (currently stateless).

### `booking-service`
- Orchestrates the booking workflow across services:
  - Requests seat locks from Inventory.
  - Requests pricing from Offer service.
  - On confirm, triggers Payment then confirms seats.
- Owns booking records and status transitions.

**Data store**: PostgreSQL for bookings/booking items.

### `common-lib`
- Shared enums/DTO primitives used across services (compile-time sharing).

## Key End-to-End Flows

### 1) Setup (Admin-like operations)
1. Create theatre
2. Create screen
3. Generate seats for a screen
4. Create show

These calls typically go through the **API Gateway** and route to `theatre-service`.

### 2) Browse shows and seats
1. Client searches shows by city/movie/date (routes to `theatre-service`).
2. Client requests seats for a show (routes to `inventory-service`).

### 3) Booking (Seat lock → Offers → Payment → Confirm)

```text
Client
  |
  v
API Gateway
  |
  v
Booking Service
  |
  +--> Inventory Service: lock seats (TTL via Redis)  ---> OK/FAIL
  |
  +--> Offer Service: compute pricing/discounts       ---> price breakdown
  |
  +--> Booking DB: persist booking as PENDING/CREATED
  |
  v
Client receives bookingId + price

Confirm booking:
Client -> Gateway -> Booking Service
  |
  +--> Payment Service: pay (simulated)               ---> success/fail
  |
  +--> Inventory Service: confirm seats               ---> mark booked
  |
  +--> Booking DB: mark CONFIRMED (or FAILED)
```

## Data & Consistency Model
- **Synchronous HTTP** calls are used for orchestration.
- **Seat locking** provides a best-effort concurrency control mechanism:
  - Lock has a TTL (configurable; README mentions 300 seconds default).
  - If payment/confirmation does not happen before TTL expiry, locks are released automatically.
- Booking transitions are handled in the orchestrator; the system favors **simplicity** over fully distributed transactions.

## Deployment Model
- Local development and demo via `docker-compose.yml`.
- Each service is containerized with its own `Dockerfile`.
- Typical topology includes:
  - API Gateway container
  - Each service container
  - Postgres containers (per service database) as configured in compose
  - Redis container for inventory locks

## External Interfaces (high level)
Representative endpoints (as shown in `README.md` demo flow):
- `POST /theatres`, `POST /theatres/{theatreId}/screens`, `POST /screens/{screenId}/seats/generate`
- `POST /shows`
- `GET /search/shows?...`
- `GET /inventory/shows/{showId}/seats`
- `POST /bookings`, `POST /bookings/{bookingId}/confirm`

## Technology Stack
- Java **17**
- Spring Boot **3.x**
- Spring Cloud Gateway (**2023.x** train)
- PostgreSQL (JPA/Hibernate)
- Redis (seat lock TTL)
- Maven multi-module build

