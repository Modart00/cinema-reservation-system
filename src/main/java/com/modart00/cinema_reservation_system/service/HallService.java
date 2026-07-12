package com.modart00.cinema_reservation_system.service;

import com.modart00.cinema_reservation_system.dto.request.HallRequest;
import com.modart00.cinema_reservation_system.dto.response.HallResponse;
import com.modart00.cinema_reservation_system.entity.Hall;
import com.modart00.cinema_reservation_system.entity.Seat;
import com.modart00.cinema_reservation_system.entity.SeatType;
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

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class HallService {
    private final HallRepository hallRepository;
    private final SeatRepository seatRepository;

    @Transactional
    @CacheEvict(value = {"hallLists", "seatLists"}, allEntries = true)
    public HallResponse createHall(HallRequest request) {
        log.info("Admin attempting to create hall: {}", request.getName());
        Hall hall = new Hall();
        hall.setName(request.getName());
        hall.setTotalRows(10);
        hall.setSeatsPerRow(10);
        Hall savedHall = hallRepository.save(hall);

        createMissingStandardSeats(savedHall);
        log.info("Admin created hall with ID: {}", savedHall.getId());
        return toResponse(savedHall);
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "halls", key = "#hallId"),
            @CacheEvict(value = {"hallLists", "seatLists"}, allEntries = true)
    })
    public void generateStandardSeats(Long hallId) {
        log.info("Admin attempting to complete standard seats for hall ID: {}", hallId);
        Hall hall = hallRepository.findById(hallId)
                .orElseThrow(() -> new ResourceNotFoundException("Salon bulunamadı"));
        hall.setTotalRows(10);
        hall.setSeatsPerRow(10);
        createMissingStandardSeats(hall);
        hallRepository.save(hall);
        log.info("Admin completed standard seats for hall ID: {}", hallId);
    }

    private void createMissingStandardSeats(Hall hall) {
        List<Seat> seats = new ArrayList<>();
        for (int row = 1; row <= 10; row++) {
            for (int seatNumber = 1; seatNumber <= 10; seatNumber++) {
                if (seatRepository.existsByHallIdAndRowNumberAndSeatNumber(hall.getId(), row, seatNumber)) {
                    continue;
                }
                Seat seat = new Seat();
                seat.setRowNumber(row);
                seat.setSeatNumber(seatNumber);
                seat.setSeatType(SeatType.STANDARD);
                seat.setHall(hall);
                seats.add(seat);
            }
        }
        seatRepository.saveAll(seats);
    }
    @Cacheable(value = "halls", key = "#id")
    public HallResponse getHallById(Long id) {
        log.info("User requested hall with ID: {}", id);
        Hall hall = hallRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Salon bulunamadı"));
        return toResponse(hall);
    }
    @Cacheable(value = "hallLists", key = "#pageable.pageNumber + '-' + #pageable.pageSize + '-' + #pageable.sort.toString()")
    public Page<HallResponse> getAllHalls(Pageable pageable) {
        log.info("User requested hall list");
        return hallRepository.findAll(pageable).map(this::toResponse);
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "halls", key = "#id"),
            @CacheEvict(value = "hallLists", allEntries = true)
    })
    public HallResponse updateHall(Long id, HallRequest request) {
        log.info("Admin attempting to update hall with ID: {}", id);
        Hall hall = hallRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Salon bulunamadı"));
        hall.setName(request.getName());
        hall.setTotalRows(request.getTotalRows());
        hall.setSeatsPerRow(request.getSeatsPerRow());
        log.info("Admin updated hall with ID: {}", id);
        return toResponse(hallRepository.save(hall));
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "halls", key = "#id"),
            @CacheEvict(value = {"hallLists", "seats", "seatLists"}, allEntries = true)
    })
    public void deleteHall(Long id) {
        log.info("Admin attempting to delete hall with ID: {}", id);
        Hall hall = hallRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Salon bulunamadı"));
        hallRepository.delete(hall);
        log.info("Admin deleted hall with ID: {}", id);
    }

    private HallResponse toResponse(Hall hall) {
        HallResponse response = new HallResponse();
        response.setId(hall.getId());
        response.setName(hall.getName());
        response.setTotalRows(hall.getTotalRows());
        response.setSeatsPerRow(hall.getSeatsPerRow());
        return response;
    }
}
