package com.modart00.cinema_reservation_system.service;

import com.modart00.cinema_reservation_system.dto.request.MovieRequest;
import com.modart00.cinema_reservation_system.dto.response.MovieResponse;
import com.modart00.cinema_reservation_system.entity.Movie;
import com.modart00.cinema_reservation_system.exception.ResourceNotFoundException;
import com.modart00.cinema_reservation_system.repository.MovieRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MovieService {
    private final MovieRepository movieRepository;

    @Transactional
    public MovieResponse createMovie(MovieRequest request) {
        log.info("Admin attempting to create movie: {}", request.getTitle());
        Movie movie = new Movie();
        setMovieFields(movie, request);
        Movie savedMovie = movieRepository.save(movie);
        log.info("Admin created movie with ID: {}", savedMovie.getId());
        return toResponse(savedMovie);
    }

    public MovieResponse getMovieById(Long id) {
        log.info("User requested movie with ID: {}", id);
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Film bulunamadı"));
        return toResponse(movie);
    }

    public Page<MovieResponse> getAllMovies(Pageable pageable) {
        log.info("User requested movie list");
        return movieRepository.findAll(pageable).map(this::toResponse);
    }

    @Transactional
    public MovieResponse updateMovie(Long id, MovieRequest request) {
        log.info("Admin attempting to update movie with ID: {}", id);
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Film bulunamadı"));
        setMovieFields(movie, request);
        log.info("Admin updated movie with ID: {}", id);
        return toResponse(movieRepository.save(movie));
    }

    @Transactional
    public void deleteMovie(Long id) {
        log.info("Admin attempting to delete movie with ID: {}", id);
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Film bulunamadı"));
        movieRepository.delete(movie);
        log.info("Admin deleted movie with ID: {}", id);
    }

    private void setMovieFields(Movie movie, MovieRequest request) {
        movie.setTitle(request.getTitle());
        movie.setDescription(request.getDescription());
        movie.setDurationMinutes(request.getDurationMinutes());
        movie.setGenre(request.getGenre());
        movie.setAgeRestriction(request.getAgeRestriction());
        movie.setReleaseDate(request.getReleaseDate());
        movie.setPosterUrl(request.getPosterUrl());
        movie.setStatus(request.getStatus());
    }

    private MovieResponse toResponse(Movie movie) {
        MovieResponse response = new MovieResponse();
        response.setId(movie.getId());
        response.setTitle(movie.getTitle());
        response.setDescription(movie.getDescription());
        response.setDurationMinutes(movie.getDurationMinutes());
        response.setGenre(movie.getGenre());
        response.setAgeRestriction(movie.getAgeRestriction());
        response.setReleaseDate(movie.getReleaseDate());
        response.setPosterUrl(movie.getPosterUrl());
        response.setStatus(movie.getStatus());
        return response;
    }
}
