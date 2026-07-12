package com.modart00.cinema_reservation_system.service;

import com.modart00.cinema_reservation_system.dto.request.HallRequest;
import com.modart00.cinema_reservation_system.dto.response.HallResponse;
import com.modart00.cinema_reservation_system.entity.Hall;
import com.modart00.cinema_reservation_system.exception.ResourceNotFoundException;
import com.modart00.cinema_reservation_system.repository.HallRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class HallService {
    private final HallRepository hallRepository;

    @Transactional
    public HallResponse createHall(HallRequest request) {
        log.info("Admin attempting to create hall: {}", request.getName());
        Hall hall = new Hall();
        hall.setName(request.getName());
        hall.setTotalRows(request.getTotalRows());
        hall.setSeatsPerRow(request.getSeatsPerRow());
        Hall savedHall = hallRepository.save(hall);
        log.info("Admin created hall with ID: {}", savedHall.getId());
        return toResponse(savedHall);
    }

    public HallResponse getHallById(Long id) {
        log.info("User requested hall with ID: {}", id);
        Hall hall = hallRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Salon bulunamadı"));
        return toResponse(hall);
    }

    public Page<HallResponse> getAllHalls(Pageable pageable) {
        log.info("User requested hall list");
        return hallRepository.findAll(pageable).map(this::toResponse);
    }

    @Transactional
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
