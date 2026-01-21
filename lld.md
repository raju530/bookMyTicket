# LLD (Low-Level Design) — BookMyTicket

## 1. Shared Models (`common-lib`)

### 1.1 Enums
- **`BookingStatus`**
  - `INITIATED`, `LOCKED`, `PAYMENT_PENDING`, `CONFIRMED`, `CANCELLED`, `FAILED`
- **`SeatStatus`**
  - `AVAILABLE`, `LOCKED`, `BOOKED`
- **`ShowType`**
  - `MORNING`, `AFTERNOON`, `EVENING`, `NIGHT`

These enums are referenced from multiple services to keep state representation consistent.

---

## 2. Theatre Service — Data Model

### 2.1 `Theatre`
- **Table**: `theatres`
- **Fields**
  - `id : UUID` (PK, generated)
  - `name : String`
  - `city : String`
  - `address : String`
  - `created_at : TIMESTAMP`

### 2.2 `Screen`
- **Table**: `screens`
- **Fields**
  - `id : UUID` (PK, generated)
  - `theatre_id : UUID` (FK → `theatres.id`, `@ManyToOne`)
  - `name : String`
  - `total_seats : INT`
  - `created_at : TIMESTAMP`

### 2.3 `Seat`
- **Table**: `seats`
- **Constraints**
  - Unique: `(screen_id, seat_number)`
- **Fields**
  - `id : UUID` (PK, generated)
  - `screen_id : UUID` (FK → `screens.id`, `@ManyToOne`)
  - `seat_number : String` (e.g. A1, A2)
  - `seat_type : String` (e.g. REGULAR, PREMIUM)
  - `created_at : TIMESTAMP`

### 2.4 `Show`
- **Table**: `shows`
- **Fields**
  - `id : UUID` (PK, generated)
  - `theatre_id : UUID` (FK → `theatres.id`, `@ManyToOne`)
  - `screen_id : UUID` (FK → `screens.id`, `@ManyToOne`)
  - `movie_id : String`
  - `show_date : DATE`
  - `start_time : TIME`
  - `end_time : TIME`
  - `show_type : ShowType` (`@Enumerated(STRING)`)
  - `base_ticket_price : DOUBLE`
  - `created_at : TIMESTAMP`

### 2.5 Theatre DB Relationships
- `Theatre 1 - N Screen`
- `Screen 1 - N Seat`
- `Theatre 1 - N Show`
- `Screen 1 - N Show`

---

## 3. Inventory Service — Data Model

### 3.1 `ShowSeatInventory`
- **Table**: `show_seat_inventory`
- **Constraints**
  - Unique: `(show_id, seat_id)`
- **Fields**
  - `id : UUID` (PK, generated)
  - `show_id : UUID` (references `Show.id` from theatre domain, no explicit JPA relation)
  - `seat_id : UUID` (references `Seat.id`, no explicit JPA relation)
  - `status : SeatStatus` (`AVAILABLE`/`LOCKED`/`BOOKED`)
  - `locked_by_booking_id : UUID` (FK-logical: references `Booking.id`)
  - `lock_expiry_time : TIMESTAMP`
  - `updated_at : TIMESTAMP`

### 3.2 DTOs (selected)
- **`SeatView`**
  - Java record: `SeatView(UUID seatId, SeatStatus status)`
  - Used to expose seat state (per show) to clients / booking service.

### 3.3 Inventory DB Relationships (logical)
- For each `show_id`, there is one `ShowSeatInventory` row per `seat_id`.
- `Booking.id` is used as a **locker** reference when seats are locked.

---

## 4. Booking Service — Data Model

### 4.1 `Booking`
- **Table**: `bookings`
- **Fields**
  - `id : UUID` (PK, generated)
  - `user_id : String`
  - `show_id : UUID`
  - `theatre_id : UUID`
  - `city : String`
  - `status : BookingStatus` (`INITIATED` → `LOCKED` → `PAYMENT_PENDING` → `CONFIRMED` / `FAILED` / `CANCELLED`)
  - `total_amount : DOUBLE`
  - `discount_amount : DOUBLE`
  - `final_amount : DOUBLE`
  - `created_at : TIMESTAMP`
  - `updated_at : TIMESTAMP`

### 4.2 `BookingItem`
- **Table**: `booking_items`
- **Fields**
  - `id : UUID` (PK, generated)
  - `booking_id : UUID` (FK → `bookings.id`, `@ManyToOne`)
  - `seat_id : UUID` (references `Seat.id`, no explicit JPA relation)

### 4.3 Booking DB Relationships
- `Booking 1 - N BookingItem`
- `BookingItem.seat_id` connects logically to `Seat` and to `ShowSeatInventory`.

---

## 5. Other Services — Key Models (Conceptual)

> Only high-level details are captured here; see source for full classes.

### 5.1 Offer Service
- **`PricingRequest` / `PricingResponse` DTOs**
  - Carry show, seats, and pricing details to/from the offer engine.
- **`OfferRule` + concrete rules**
  - `AfternoonTwentyPercentOffer`, `ThirdTicketHalfOffOffer`
  - Implement a strategy/chain for discount calculation.

### 5.2 Payment Service
- **`PayRequest` / `PayResponse` DTOs**
  - Represent payment attempt and outcome (success/failure, reference IDs).
- Service is stateless; no persistent entities.

### 5.3 API Gateway & Controllers
- Expose REST endpoints mapped to service-layer orchestrators.
- Controllers are thin; most logic lives in services and clients (e.g. `BookingOrchestrator`, `InventoryClient`, `OfferClient`, `PaymentClient`, `TheatreClient`).

---

## 6. Cross-Service Object Flow (LLD View)

### 6.1 Create Booking (Lock Seats + Price)
1. Client → `BookingController.createBooking(BookingRequest)`  
2. `BookingOrchestrator`:
   - Creates `Booking` with `INITIATED`.
   - Calls `InventoryClient.lockSeats(showId, seatIds)`:
     - Inventory writes/updates `ShowSeatInventory` entries:
       - `status = LOCKED`
       - `locked_by_booking_id = booking.id`
       - `lock_expiry_time = now + TTL`
   - Calls `OfferClient.price(...)`:
     - Offer engine reads `SeatStatus`, Show info, and applies `OfferRule` chain.
   - Updates `Booking` monetary fields and `status = LOCKED` / `PAYMENT_PENDING`.
3. Response DTO (`BookingResponse`) returns `bookingId`, amounts, and seat info.

### 6.2 Confirm Booking (Payment + Finalize Seats)
1. Client → `BookingController.confirmBooking(bookingId)`  
2. `BookingOrchestrator`:
   - Calls `PaymentClient.pay(...)` with `bookingId` + `finalAmount`.
   - On success:
     - Calls `InventoryClient.confirmSeats(bookingId)`:
       - `ShowSeatInventory` rows with `locked_by_booking_id = bookingId`:
         - `status = BOOKED`
         - `lock_expiry_time` cleared/updated.
     - Sets `Booking.status = CONFIRMED` and updates timestamps.
   - On failure:
     - Optionally triggers unlock in Inventory.
     - Sets `Booking.status = FAILED`.

---

## 7. Schema Summary (Per Service)

### 7.1 Theatre Service (Postgres)
- `theatres(id, name, city, address, created_at)`
- `screens(id, theatre_id, name, total_seats, created_at)`
- `seats(id, screen_id, seat_number, seat_type, created_at)`  
  - Unique `(screen_id, seat_number)`
- `shows(id, theatre_id, screen_id, movie_id, show_date, start_time, end_time, show_type, base_ticket_price, created_at)`

### 7.2 Inventory Service (Postgres + Redis)
- Postgres:
  - `show_seat_inventory(id, show_id, seat_id, status, locked_by_booking_id, lock_expiry_time, updated_at)`  
    - Unique `(show_id, seat_id)`
- Redis:
  - Keys representing seat locks (`showId:seatId` → TTL) mirroring `lock_expiry_time`.

### 7.3 Booking Service (Postgres)
- `bookings(id, user_id, show_id, theatre_id, city, status, total_amount, discount_amount, final_amount, created_at, updated_at)`
- `booking_items(id, booking_id, seat_id)`

---

## 8. Extension Points
- Add **user accounts**:
  - New `users` table and foreign key from `bookings.user_id`.
- Add **auditing**:
  - Created/updated by fields on entities.
- Add **event-driven integration**:
  - Emit domain events (e.g. `BookingConfirmed`) to a message broker for downstream consumers.

