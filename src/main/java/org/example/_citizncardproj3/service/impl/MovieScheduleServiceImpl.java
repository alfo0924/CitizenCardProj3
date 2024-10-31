package org.example._citizncardproj3.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example._citizncardproj3.exception.CustomException;
import org.example._citizncardproj3.model.dto.request.ScheduleCreateRequest;
import org.example._citizncardproj3.model.dto.response.ScheduleResponse;
import org.example._citizncardproj3.model.entity.CityMovie;
import org.example._citizncardproj3.model.entity.MovieSchedule;
import org.example._citizncardproj3.model.entity.Venue;
import org.example._citizncardproj3.repository.CityMovieRepository;
import org.example._citizncardproj3.repository.MovieScheduleRepository;
import org.example._citizncardproj3.repository.VenueRepository;
import org.example._citizncardproj3.service.MovieScheduleService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MovieScheduleServiceImpl implements MovieScheduleService {

    private final MovieScheduleRepository scheduleRepository;
    private final CityMovieRepository movieRepository;
    private final VenueRepository venueRepository;

    @Override
    @Transactional
    public ScheduleResponse createSchedule(Long movieId, ScheduleCreateRequest request) {
        // 驗證電影
        CityMovie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new CustomException.MovieNotFoundException(movieId));

        // 驗證場地
        Venue venue = venueRepository.findById(request.getVenueId())
                .orElseThrow(() -> new CustomException.VenueNotFoundException(request.getVenueId()));

        // 驗證場次時間
        validateScheduleTime(movie, request.getShowTime());

        // 檢查場地在該時段是否可用
        validateVenueAvailability(venue, request.getShowTime());

        // 創建場次
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

        schedule = scheduleRepository.save(schedule);
        return convertToResponse(schedule);
    }

    @Override
    public Page<ScheduleResponse> getMovieSchedules(Long movieId, Pageable pageable) {
        CityMovie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new CustomException.MovieNotFoundException(movieId));

        return scheduleRepository.findByMovieOrderByShowTimeAsc(movie, pageable)
                .map(this::convertToResponse);
    }

    @Override
    public List<ScheduleResponse> getAvailableSchedules(Long movieId, LocalDateTime startTime, LocalDateTime endTime) {
        CityMovie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new CustomException.MovieNotFoundException(movieId));

        return scheduleRepository.findAvailableSchedules(movie, startTime, endTime)
                .stream()
                .map(this::convertToResponse)
                .toList();
    }

    @Override
    @Transactional
    public void updateScheduleStatus(Long scheduleId, MovieSchedule.ScheduleStatus newStatus) {
        MovieSchedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new CustomException.ScheduleNotFoundException(scheduleId));

        schedule.setStatus(newStatus);
        scheduleRepository.save(schedule);
    }

    @Override
    @Transactional
    public void cancelSchedule(Long scheduleId) {
        MovieSchedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new CustomException.ScheduleNotFoundException(scheduleId));

        if (schedule.hasBookings()) {
            throw new IllegalStateException("已有訂票的場次無法取消");
        }

        schedule.setStatus(MovieSchedule.ScheduleStatus.CANCELLED);
        scheduleRepository.save(schedule);
    }

    @Override
    @Transactional
    public void updateAvailableSeats(Long scheduleId) {
        MovieSchedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new CustomException.ScheduleNotFoundException(scheduleId));

        schedule.updateAvailableSeats();
        scheduleRepository.save(schedule);
    }

    // 私有輔助方法
    private void validateScheduleTime(CityMovie movie, LocalDateTime showTime) {
        if (showTime.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("場次時間不能早於現在");
        }

        if (showTime.toLocalDate().isAfter(movie.getEndDate())) {
            throw new IllegalArgumentException("場次時間不能晚於電影下檔日期");
        }

        if (movie.getStatus() != CityMovie.MovieStatus.NOW_SHOWING) {
            throw new IllegalStateException("只有上映中的電影可以新增場次");
        }
    }

    private void validateVenueAvailability(Venue venue, LocalDateTime showTime) {
        if (venue.getStatus() != Venue.VenueStatus.ACTIVE) {
            throw new IllegalStateException("場地目前無法使用");
        }

        // 檢查場地在該時段是否已有其他場次
        boolean hasConflict = scheduleRepository.findByVenueAndShowTimeBetween(
                venue,
                showTime.minusMinutes(30),  // 預留30分鐘清場時間
                showTime.plusMinutes(180)    // 假設電影最長3小時
        ).size() > 0;

        if (hasConflict) {
            throw new IllegalStateException("場地在該時段已有其他場次");
        }
    }

    private ScheduleResponse convertToResponse(MovieSchedule schedule) {
        return ScheduleResponse.builder()
                .scheduleId(schedule.getScheduleId())
                .movieId(schedule.getMovie().getMovieId())
                .movieName(schedule.getMovie().getMovieName())
                .venueName(schedule.getVenue().getVenueName())
                .roomNumber(schedule.getRoomNumber())
                .showTime(schedule.getShowTime())
                .endTime(schedule.getEndTime())
                .basePrice(schedule.getBasePrice())
                .availableSeats(schedule.getAvailableSeats())
                .totalSeats(schedule.getTotalSeats())
                .status(schedule.getStatus())
                .build();
    }
}