package com.modart00.cinema_reservation_system.service;

import com.modart00.cinema_reservation_system.dto.request.SeatRequest;
import com.modart00.cinema_reservation_system.dto.response.SeatResponse;
import com.modart00.cinema_reservation_system.entity.Hall;
import com.modart00.cinema_reservation_system.entity.Seat;
import com.modart00.cinema_reservation_system.exception.ResourceNotFoundException;
import com.modart00.cinema_reservation_system.repository.HallRepository;
import com.modart00.cinema_reservation_system.repository.SeatRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SeatService {
    private final SeatRepository seatRepository;
    private final HallRepository hallRepository;

    @Transactional
    @CacheEvict(value = "seatLists", allEntries = true)
    public SeatResponse createSeat(SeatRequest request) {
        log.info("Admin attempting to create seat for hall ID: {}", request.getHallId());
        Hall hall = hallRepository.findById(request.getHallId())
                .orElseThrow(() -> new ResourceNotFoundException("Salon bulunamadı"));
        Seat seat = new Seat();
        seat.setRowNumber(request.getRowNumber());
        seat.setSeatNumber(request.getSeatNumber());
        seat.setSeatType(request.getSeatType());
        seat.setHall(hall);
        Seat savedSeat = seatRepository.save(seat);
        log.info("Admin created seat with ID: {}", savedSeat.getId());
        return toResponse(savedSeat);
    }

    @Cacheable(value = "seats", key = "#id")
    public SeatResponse getSeatById(Long id) {
        log.info("User requested seat with ID: {}", id);
        Seat seat = seatRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Koltuk bulunamadı"));
        return toResponse(seat);
    }

    @Cacheable(value = "seatLists", key = "#pageable.pageNumber + '-' + #pageable.pageSize + '-' + #pageable.sort.toString()")
    public Page<SeatResponse> getAllSeats(Pageable pageable) {
        log.info("User requested seat list");
        return seatRepository.findAll(pageable).map(this::toResponse);
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "seats", key = "#id"),
            @CacheEvict(value = "seatLists", allEntries = true)
    })
    public SeatResponse updateSeat(Long id, SeatRequest request) {
        log.info("Admin attempting to update seat with ID: {}", id);
        Seat seat = seatRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Koltuk bulunamadı"));
        Hall hall = hallRepository.findById(request.getHallId())
                .orElseThrow(() -> new ResourceNotFoundException("Salon bulunamadı"));
        seat.setRowNumber(request.getRowNumber());
        seat.setSeatNumber(request.getSeatNumber());
        seat.setSeatType(request.getSeatType());
        seat.setHall(hall);
        log.info("Admin updated seat with ID: {}", id);
        return toResponse(seatRepository.save(seat));
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "seats", key = "#id"),
            @CacheEvict(value = "seatLists", allEntries = true)
    })
    public void deleteSeat(Long id) {
        log.info("Admin attempting to delete seat with ID: {}", id);
        Seat seat = seatRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Koltuk bulunamadı"));
        seatRepository.delete(seat);
        log.info("Admin deleted seat with ID: {}", id);
    }

    private SeatResponse toResponse(Seat seat) {
        SeatResponse response = new SeatResponse();
        response.setId(seat.getId());
        response.setRowNumber(seat.getRowNumber());
        response.setSeatNumber(seat.getSeatNumber());
        response.setSeatType(seat.getSeatType());
        response.setHallId(seat.getHall().getId());
        return response;
    }
}
