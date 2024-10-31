package org.example._citizncardproj3.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example._citizncardproj3.exception.CustomException;
import org.example._citizncardproj3.model.dto.request.MovieCreateRequest;
import org.example._citizncardproj3.model.dto.request.MovieUpdateRequest;
import org.example._citizncardproj3.model.dto.request.ScheduleCreateRequest;
import org.example._citizncardproj3.model.dto.response.MovieResponse;
import org.example._citizncardproj3.model.dto.response.ScheduleResponse;
import org.example._citizncardproj3.model.entity.CityMovie;
import org.example._citizncardproj3.model.entity.MovieSchedule;
import org.example._citizncardproj3.model.entity.Venue;
import org.example._citizncardproj3.repository.CityMovieRepository;
import org.example._citizncardproj3.repository.MovieScheduleRepository;
import org.example._citizncardproj3.repository.VenueRepository;
import org.example._citizncardproj3.service.CityMovieService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CityMovieServiceImpl implements CityMovieService {

    private final CityMovieRepository movieRepository;
    private final MovieScheduleRepository scheduleRepository;
    private final VenueRepository venueRepository;

    @Override
    public Page<MovieResponse> getAllMovies(Boolean showingOnly, Pageable pageable) {
        if (Boolean.TRUE.equals(showingOnly)) {
            return movieRepository.findByStatusOrderByReleaseDateDesc(
                    CityMovie.MovieStatus.NOW_SHOWING,
                    pageable
            ).map(this::convertToResponse);
        }
        return movieRepository.findAll(pageable).map(this::convertToResponse);
    }

    @Override
    public MovieResponse getMovie(Long movieId) {
        CityMovie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new CustomException.MovieNotFoundException(movieId));
        return convertToResponse(movie);
    }

    @Override
    public Page<MovieResponse> searchMovies(String keyword, Pageable pageable) {
        return movieRepository.searchMovies(keyword, pageable)
                .map(this::convertToResponse);
    }

    @Override
    public List<ScheduleResponse> getMovieSchedules(Long movieId, LocalDate date) {
        CityMovie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new CustomException.MovieNotFoundException(movieId));

        return scheduleRepository.findByMovieAndShowTimeBetween(
                        movie,
                        date.atStartOfDay(),
                        date.plusDays(1).atStartOfDay()
                ).stream().map(this::convertToScheduleResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public MovieResponse createMovie(MovieCreateRequest request) {
        validateNewMovie(request);

        CityMovie movie = CityMovie.builder()
                .movieName(request.getMovieName())
                .description(request.getDescription())
                .duration(request.getDuration())
                .releaseDate(request.getReleaseDate())
                .endDate(request.getEndDate())
                .language(request.getLanguage())
                .subtitle(request.getSubtitle())
                .director(request.getDirector())
                .cast(request.getCast())
                .rating(request.getRating())
                .categories(request.getCategories())
                .basePrice(request.getBasePrice())
                .build();

        movie.updateStatus();
        return convertToResponse(movieRepository.save(movie));
    }

    @Override
    @Transactional
    public MovieResponse updateMovie(Long movieId, MovieUpdateRequest request) {
        CityMovie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new CustomException.MovieNotFoundException(movieId));

        if (movie.getStatus() == CityMovie.MovieStatus.END_SHOWING) {
            throw new IllegalStateException("已下檔的電影無法更新");
        }

        movie.updateInfo(request.getMovieName(), request.getDescription(), request.getBasePrice());
        if (request.getEndDate() != null) {
            movie.extendShowingPeriod(request.getEndDate());
        }

        return convertToResponse(movieRepository.save(movie));
    }

    @Override
    @Transactional
    public String uploadPoster(Long movieId, MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("檔案不能為空");
        }

        CityMovie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new CustomException.MovieNotFoundException(movieId));

        // TODO: 實作檔案上傳邏輯
        String posterUrl = "http://example.com/posters/" + movieId;
        movie.setPosterUrl(posterUrl);
        movieRepository.save(movie);

        return posterUrl;
    }

    @Override
    @Transactional
    public ScheduleResponse createSchedule(Long movieId, ScheduleCreateRequest request) {
        CityMovie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new CustomException.MovieNotFoundException(movieId));

        Venue venue = venueRepository.findById(request.getVenueId())
                .orElseThrow(() -> new IllegalArgumentException("場地不存在"));

        validateNewSchedule(movie, request);

        MovieSchedule schedule = MovieSchedule.builder()
                .movie(movie)
                .venue(venue)
                .roomNumber(request.getRoomNumber())
                .showTime(request.getShowTime())
                .endTime(request.getShowTime().plusMinutes(movie.getDuration()))
                .basePrice(request.getSpecialPrice() != null ?
                        request.getSpecialPrice() : movie.getBasePrice())
                .totalSeats(venue.getTotalSeats())
                .availableSeats(venue.getTotalSeats())
                .status(MovieSchedule.ScheduleStatus.NOT_STARTED)
                .build();

        return convertToScheduleResponse(scheduleRepository.save(schedule));
    }

    @Override
    @Transactional
    public void deleteSchedule(Long scheduleId) {
        MovieSchedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new IllegalArgumentException("場次不存在"));

        if (schedule.hasBookings()) {
            throw new IllegalStateException("已有訂票的場次無法刪除");
        }

        scheduleRepository.delete(schedule);
    }

    @Override
    @Transactional
    public void deleteMovie(Long movieId) {
        CityMovie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new CustomException.MovieNotFoundException(movieId));

        if (movie.hasActiveSchedules()) {
            throw new IllegalStateException("電影有未完成的場次，無法下架");
        }

        movie.setStatus(CityMovie.MovieStatus.END_SHOWING);
        movieRepository.save(movie);
    }

    @Override
    public Page<MovieResponse> getUpcomingMovies(Pageable pageable) {
        return null;
    }

    @Override
    public Page<MovieResponse> getNowShowingMovies(Pageable pageable) {
        return null;
    }

    @Override
    public MovieResponse extendShowingPeriod(Long movieId, LocalDate endDate) {
        return null;
    }

    @Override
    public MovieResponse updateRating(Long movieId, String rating) {
        return null;
    }

    @Override
    public Map<String, Object> getMovieStatistics(Long movieId) {
        return Map.of();
    }

    // 私有輔助方法
    private void validateNewMovie(MovieCreateRequest request) {
        if (request.getReleaseDate().isAfter(request.getEndDate())) {
            throw new IllegalArgumentException("下檔日期必須晚於上映日期");
        }

        if (movieRepository.existsByMovieName(request.getMovieName())) {
            throw new IllegalStateException("電影名稱已存在");
        }
    }

    private void validateNewSchedule(CityMovie movie, ScheduleCreateRequest request) {
        if (movie.getStatus() != CityMovie.MovieStatus.NOW_SHOWING) {
            throw new IllegalStateException("只有上映中的電影可以新增場次");
        }

        if (request.getShowTime().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("場次時間不能早於現在");
        }

        if (request.getShowTime().toLocalDate().isAfter(movie.getEndDate())) {
            throw new IllegalArgumentException("場次時間不能晚於電影下檔日期");
        }
    }

    private MovieResponse convertToResponse(CityMovie movie) {
        return MovieResponse.builder()
                .movieId(movie.getMovieId())
                .movieName(movie.getMovieName())
                .description(movie.getDescription())
                .releaseDate(movie.getReleaseDate())
                .endDate(movie.getEndDate())
                .language(movie.getLanguage())
                .subtitle(movie.getSubtitle())
                .director(movie.getDirector())
                .cast(movie.getCast())
                .duration(movie.getDuration())
                .rating(movie.getRating())
                .posterUrl(movie.getPosterUrl())
                .status(movie.getStatus())
                .build();
    }

    private ScheduleResponse convertToScheduleResponse(MovieSchedule schedule) {
        return ScheduleResponse.builder()
                .scheduleId(schedule.getScheduleId())
                .movieId(schedule.getMovie().getMovieId())
                .movieName(schedule.getMovie().getMovieName())
                .venueName(schedule.getVenue().getVenueName())
                .roomNumber(schedule.getRoomNumber())
                .showTime(schedule.getShowTime())
                .basePrice(schedule.getBasePrice())
                .availableSeats(schedule.getAvailableSeats())
                .status(schedule.getStatus())
                .build();
    }
}