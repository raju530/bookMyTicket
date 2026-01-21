# bookMyTicket (Microservices) - Ready to Run

## Services
- api-gateway (8080)
- theatre-service (8082)
- inventory-service (8083)
- offer-service (8084)
- booking-service (8085)
- payment-service (8086)
- common-lib (shared DTOs/enums)

## Prerequisites
- Docker + Docker Compose
- Java 17 (optional if running via Docker only)

## Run
```bash
docker compose up --build
```

Postgres databases are auto-created by services (Hibernate ddl-auto=update).

## Quick Demo (through API Gateway)
### 1) Create Theatre + Screen + Seats
- POST http://localhost:8080/theatres
- POST http://localhost:8080/theatres/{theatreId}/screens
- POST http://localhost:8080/screens/{screenId}/seats/generate

### 2) Create Show
- POST http://localhost:8080/shows

### 3) Browse Shows by Movie+City+Date
- GET http://localhost:8080/search/shows?city=Bangalore&movieId=...&date=2026-01-21

### 4) Get Seats for a Show
- GET http://localhost:8080/inventory/shows/{showId}/seats

### 5) Create Booking (locks seats + applies offers)
- POST http://localhost:8080/bookings

### 6) Confirm Booking (simulated payment + confirm seats)
- POST http://localhost:8080/bookings/{bookingId}/confirm

## Notes
- Seat locking uses Redis with TTL (default 300 seconds).
- Offers implemented using Strategy + Chain:
  - THIRD_TICKET_50
  - AFTERNOON_20
