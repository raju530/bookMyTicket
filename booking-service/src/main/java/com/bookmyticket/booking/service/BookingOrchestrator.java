package com.bookmyticket.booking.service;

import com.bookmyticket.booking.client.InventoryClient;
import com.bookmyticket.booking.client.OfferClient;
import com.bookmyticket.booking.client.PaymentClient;
import com.bookmyticket.booking.client.TheatreClient;
import com.bookmyticket.booking.dto.BookingResponse;
import com.bookmyticket.booking.dto.CreateBookingRequest;
import com.bookmyticket.booking.entity.Booking;
import com.bookmyticket.booking.entity.BookingItem;
import com.bookmyticket.booking.repo.BookingItemRepository;
import com.bookmyticket.booking.repo.BookingRepository;
import com.bookmyticket.common.enums.BookingStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class BookingOrchestrator {

    private final BookingRepository bookingRepository;
    private final BookingItemRepository bookingItemRepository;
    private final InventoryClient inventoryClient;
    private final OfferClient offerClient;
    private final TheatreClient theatreClient;
    private final PaymentClient paymentClient;

    public BookingOrchestrator(
            BookingRepository bookingRepository,
            BookingItemRepository bookingItemRepository,
            InventoryClient inventoryClient,
            OfferClient offerClient,
            TheatreClient theatreClient,
            PaymentClient paymentClient
    ) {
        this.bookingRepository = bookingRepository;
        this.bookingItemRepository = bookingItemRepository;
        this.inventoryClient = inventoryClient;
        this.offerClient = offerClient;
        this.theatreClient = theatreClient;
        this.paymentClient = paymentClient;
    }

    @Transactional
    public BookingResponse createBooking(CreateBookingRequest req) {

        Booking booking = Booking.builder()
                .userId(req.userId())
                .city(req.city())
                .theatreId(UUID.fromString(req.theatreId()))
                .showId(UUID.fromString(req.showId()))
                .status(BookingStatus.INITIATED)
                .build();
        booking = bookingRepository.save(booking);

        // Save booking seats
        for (String seatId : req.seatIds()) {
            BookingItem item = new BookingItem();
            item.setBooking(booking);
            item.setSeatId(UUID.fromString(seatId));
            bookingItemRepository.save(item);
        }

        // 1) lock seats
        var lockResp = inventoryClient.lockSeats(booking.getId().toString(), req.showId(), req.seatIds());

        booking.setStatus(BookingStatus.LOCKED);
        booking.setUpdatedAt(Instant.now());
        bookingRepository.save(booking);

        // 2) show details (for base ticket price + afternoon)
        var show = theatreClient.getShow(req.showId());
        boolean isAfternoon = show.isAfternoon();
        double baseTotal = show.baseTicketPrice() * req.seatIds().size();

        // 3) apply offers
        var pricing = offerClient.calculate(req.city(), req.theatreId(), req.showId(), baseTotal, isAfternoon, req.seatIds().size());

        booking.setTotalAmount(pricing.total());
        booking.setDiscountAmount(pricing.discount());
        booking.setFinalAmount(pricing.finalAmount());
        booking.setUpdatedAt(Instant.now());
        bookingRepository.save(booking);

        return new BookingResponse(
                booking.getId().toString(),
                booking.getStatus().name(),
                booking.getTotalAmount(),
                booking.getDiscountAmount(),
                booking.getFinalAmount(),
                lockResp.expiryTime()
        );
    }

    @Transactional
    public BookingResponse confirmBooking(UUID bookingId) {
        Booking booking = bookingRepository.findById(bookingId).orElseThrow();

        if (booking.getStatus() != BookingStatus.LOCKED && booking.getStatus() != BookingStatus.PAYMENT_PENDING) {
            throw new IllegalStateException("Booking is not in a confirmable state: " + booking.getStatus());
        }

        booking.setStatus(BookingStatus.PAYMENT_PENDING);
        booking.setUpdatedAt(Instant.now());
        bookingRepository.save(booking);

        // Simulated payment
        var payResp = paymentClient.pay(booking.getId().toString(), booking.getFinalAmount());
        if (!"SUCCESS".equalsIgnoreCase(payResp.status())) {
            booking.setStatus(BookingStatus.FAILED);
            booking.setUpdatedAt(Instant.now());
            bookingRepository.save(booking);

            inventoryClient.releaseSeats(booking.getId().toString(), booking.getShowId().toString());
            throw new IllegalStateException("Payment failed");
        }

        // Confirm seats
        inventoryClient.confirmSeats(booking.getId().toString(), booking.getShowId().toString());

        booking.setStatus(BookingStatus.CONFIRMED);
        booking.setUpdatedAt(Instant.now());
        bookingRepository.save(booking);

        return new BookingResponse(
                booking.getId().toString(),
                booking.getStatus().name(),
                booking.getTotalAmount(),
                booking.getDiscountAmount(),
                booking.getFinalAmount(),
                null
        );
    }

    @Transactional
    public void cancelBooking(UUID bookingId) {
        Booking booking = bookingRepository.findById(bookingId).orElseThrow();

        if (booking.getStatus() == BookingStatus.CONFIRMED) {
            // In real system: refund workflow + cancellation policy
            // For now: release seats back
            inventoryClient.releaseSeats(booking.getId().toString(), booking.getShowId().toString());
        } else if (booking.getStatus() == BookingStatus.LOCKED || booking.getStatus() == BookingStatus.PAYMENT_PENDING) {
            inventoryClient.releaseSeats(booking.getId().toString(), booking.getShowId().toString());
        }

        booking.setStatus(BookingStatus.CANCELLED);
        booking.setUpdatedAt(Instant.now());
        bookingRepository.save(booking);
    }
}
