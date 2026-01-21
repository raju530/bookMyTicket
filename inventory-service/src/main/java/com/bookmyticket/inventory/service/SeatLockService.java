package com.bookmyticket.inventory.service;

import com.bookmyticket.common.enums.SeatStatus;
import com.bookmyticket.inventory.dto.LockSeatsRequest;
import com.bookmyticket.inventory.dto.LockSeatsResponse;
import com.bookmyticket.inventory.entity.ShowSeatInventory;
import com.bookmyticket.inventory.repo.ShowSeatInventoryRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class SeatLockService {

    private final ShowSeatInventoryRepository repo;
    private final StringRedisTemplate redisTemplate;

    @Value("${inventory.lock-ttl-seconds:300}")
    private long ttlSeconds;

    public SeatLockService(ShowSeatInventoryRepository repo, StringRedisTemplate redisTemplate) {
        this.repo = repo;
        this.redisTemplate = redisTemplate;
    }

    private String redisKey(UUID showId, UUID seatId) {
        return "LOCK:SHOW:" + showId + ":SEAT:" + seatId;
    }

    @Transactional
    public LockSeatsResponse lock(LockSeatsRequest req) {
        UUID bookingId = UUID.fromString(req.bookingId());
        UUID showId = UUID.fromString(req.showId());

        Instant expiry = Instant.now().plus(ttlSeconds, ChronoUnit.SECONDS);

        // Basic best-effort lock: try all seats; if any fails -> rollback and release acquired
        for (String seatIdStr : req.seatIds()) {
            UUID seatId = UUID.fromString(seatIdStr);

            String key = redisKey(showId, seatId);
            Boolean ok = redisTemplate.opsForValue().setIfAbsent(key, bookingId.toString(), ttlSeconds, TimeUnit.SECONDS);

            if (ok == null || !ok) {
                // rollback previously locked in redis
                for (String sid : req.seatIds()) {
                    UUID s = UUID.fromString(sid);
                    String k = redisKey(showId, s);
                    String val = redisTemplate.opsForValue().get(k);
                    if (bookingId.toString().equals(val)) redisTemplate.delete(k);
                }
                throw new IllegalStateException("One or more seats already locked/booked");
            }

            ShowSeatInventory inv = repo.findByShowIdAndSeatId(showId, seatId)
                    .orElseGet(() -> {
                        ShowSeatInventory n = new ShowSeatInventory();
                        n.setShowId(showId);
                        n.setSeatId(seatId);
                        n.setStatus(SeatStatus.AVAILABLE);
                        return n;
                    });

            if (inv.getStatus() == SeatStatus.BOOKED) {
                redisTemplate.delete(key);
                throw new IllegalStateException("Seat already booked");
            }

            inv.setStatus(SeatStatus.LOCKED);
            inv.setLockedByBookingId(bookingId);
            inv.setLockExpiryTime(expiry);
            repo.save(inv);
        }

        return new LockSeatsResponse("LOCKED", expiry.toString());
    }

    @Transactional
    public void confirm(String bookingIdStr, String showIdStr) {
        UUID bookingId = UUID.fromString(bookingIdStr);
        UUID showId = UUID.fromString(showIdStr);

        List<ShowSeatInventory> seats = repo.findByShowId(showId);
        for (ShowSeatInventory inv : seats) {
            if (inv.getStatus() == SeatStatus.LOCKED && bookingId.equals(inv.getLockedByBookingId())) {
                inv.setStatus(SeatStatus.BOOKED);
                inv.setLockExpiryTime(null);
                repo.save(inv);
                redisTemplate.delete(redisKey(showId, inv.getSeatId()));
            }
        }
    }

    @Transactional
    public void release(String bookingIdStr, String showIdStr) {
        UUID bookingId = UUID.fromString(bookingIdStr);
        UUID showId = UUID.fromString(showIdStr);

        List<ShowSeatInventory> seats = repo.findByShowId(showId);
        for (ShowSeatInventory inv : seats) {
            if (inv.getStatus() == SeatStatus.LOCKED && bookingId.equals(inv.getLockedByBookingId())) {
                inv.setStatus(SeatStatus.AVAILABLE);
                inv.setLockedByBookingId(null);
                inv.setLockExpiryTime(null);
                repo.save(inv);
                redisTemplate.delete(redisKey(showId, inv.getSeatId()));
            }
        }
    }
}
