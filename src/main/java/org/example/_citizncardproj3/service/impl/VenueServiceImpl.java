package org.example._citizncardproj3.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example._citizncardproj3.exception.CustomException;
import org.example._citizncardproj3.model.entity.MovieSchedule;
import org.example._citizncardproj3.model.entity.SeatManagement;
import org.example._citizncardproj3.model.entity.Venue;
import org.example._citizncardproj3.repository.MovieScheduleRepository;
import org.example._citizncardproj3.repository.SeatManagementRepository;
import org.example._citizncardproj3.repository.VenueRepository;
import org.example._citizncardproj3.service.VenueService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class VenueServiceImpl implements VenueService {

    private final VenueRepository venueRepository;
    private final SeatManagementRepository seatManagementRepository;
    private final MovieScheduleRepository scheduleRepository;

    @Override
    @Transactional
    public Venue createVenue(String venueName, String address, Integer totalSeats) {
        // 檢查場地名稱是否已存在
        if (venueRepository.existsByVenueName(venueName)) {
            throw new IllegalStateException("場地名稱已存在");
        }

        // 創建場地
        Venue venue = Venue.builder()
                .venueName(venueName)
                .address(address)
                .totalSeats(totalSeats)
                .status(Venue.VenueStatus.ACTIVE)
                .seatingLayout(generateDefaultSeatingLayout(totalSeats))
                .build();

        venue = venueRepository.save(venue);

        // 初始化座位配置
        initializeSeats(venue);

        return venue;
    }

    @Override
    public Venue createVenue(String venueName, String address, Integer totalRows, Integer totalColumns) {
        return null;
    }

    @Override
    public void setMaintenance(Long venueId, LocalDateTime maintenanceDate) {

    }

    @Transactional
    @Override
    public void setMaintenance(Long venueId) {
        Venue venue = venueRepository.findById(venueId)
                .orElseThrow(() -> new CustomException.VenueNotFoundException(venueId));

        if (venue.hasActiveSchedules()) {
            throw new IllegalStateException("場地有未完成的場次，無法進行維護");
        }

        venue.setMaintenance();
        venueRepository.save(venue);
    }

    @Override
    @Transactional
    public void completeMaintenance(Long venueId) {
        Venue venue = venueRepository.findById(venueId)
                .orElseThrow(() -> new CustomException.VenueNotFoundException(venueId));

        venue.completeMaintenance();
        venueRepository.save(venue);
    }

    @Override
    @Transactional
    public void updateSeatingLayout(Long venueId, String newLayout) {
        Venue venue = venueRepository.findById(venueId)
                .orElseThrow(() -> new CustomException.VenueNotFoundException(venueId));

        venue.updateSeatingLayout(newLayout);
        venueRepository.save(venue);
    }

    @Override
    public List<Venue> getAvailableVenues() {
        return venueRepository.findByStatusAndIsDeletedFalse(Venue.VenueStatus.ACTIVE);
    }

    @Override
    public List<Venue> getAvailableVenuesForTime(LocalDateTime startTime, LocalDateTime endTime) {
        return venueRepository.findAvailableVenuesForTime(startTime, endTime);
    }

    @Override
    public Page<MovieSchedule> getVenueSchedules(Long venueId, Pageable pageable) {
        Venue venue = venueRepository.findById(venueId)
                .orElseThrow(() -> new CustomException.VenueNotFoundException(venueId));

        return scheduleRepository.findByVenueOrderByShowDateAscStartTimeAsc(venue, pageable);
    }

    @Override
    public List<Object[]> getVenueUtilization(Long venueId, LocalDateTime startTime, LocalDateTime endTime) {
        Venue venue = venueRepository.findById(venueId)
                .orElseThrow(() -> new CustomException.VenueNotFoundException(venueId));

        return scheduleRepository.getVenueUtilization(venueId, startTime, endTime);
    }

    @Override
    @Transactional
    public void checkAndUpdateMaintenanceStatus() {
        List<Venue> venues = venueRepository.findByStatus(Venue.VenueStatus.ACTIVE);

        for (Venue venue : venues) {
            if (!venue.hasActiveSchedules()) {
                venue.setStatus(Venue.VenueStatus.MAINTENANCE);
                venueRepository.save(venue);
            }
        }
    }

    @Override
    public boolean checkVenueAvailability(Long venueId, LocalDateTime startTime, LocalDateTime endTime) {
        return false;
    }

    @Override
    public Venue getVenueDetails(Long venueId) {
        return venueRepository.findById(venueId)
                .orElseThrow(() -> new CustomException.VenueNotFoundException(venueId));
    }

    @Override
    @Transactional
    public Venue updateVenueInfo(Long venueId, String venueName, String address) {
        Venue venue = venueRepository.findById(venueId)
                .orElseThrow(() -> new CustomException.VenueNotFoundException(venueId));

        if (venueName != null && !venueName.equals(venue.getVenueName())) {
            if (venueRepository.existsByVenueName(venueName)) {
                throw new IllegalStateException("場地名稱已存在");
            }
            venue.setVenueName(venueName);
        }

        if (address != null) {
            venue.setAddress(address);
        }

        return venueRepository.save(venue);
    }

    @Override
    public Page<Map<String, Object>> getMaintenanceHistory(Long venueId, Pageable pageable) {
        return null;
    }

    @Override
    public Map<String, Object> getSeatUsageStatistics(Long venueId) {
        return Map.of();
    }

    // 私有輔助方法
    private void initializeSeats(Venue venue) {
        int totalSeats = venue.getTotalSeats();
        for (int i = 1; i <= totalSeats; i++) {
            SeatManagement seat = SeatManagement.builder()
                    .venue(venue)
                    .seatLabel("SEAT-" + i)
                    .seatType(SeatManagement.SeatType.REGULAR)
                    .status(SeatManagement.SeatStatus.AVAILABLE)
                    .isActive(true)
                    .build();

            seatManagementRepository.save(seat);
        }
    }

    private String generateDefaultSeatingLayout(int totalSeats) {
        StringBuilder layout = new StringBuilder();
        layout.append("{\"seats\":[");
        for (int i = 0; i < totalSeats; i++) {
            layout.append("{\"seatId\":").append(i + 1)
                    .append(",\"status\":\"AVAILABLE\"}");
            if (i < totalSeats - 1) layout.append(",");
        }
        layout.append("]}");
        return layout.toString();
    }

    private void validateVenueAvailability(Venue venue, LocalDateTime startTime, LocalDateTime endTime) {
        if (venue.getStatus() != Venue.VenueStatus.ACTIVE) {
            throw new IllegalStateException("場地目前無法使用");
        }

        boolean hasConflict = scheduleRepository.existsByVenueAndShowDateBetween(
                venue, startTime.toLocalDate(), endTime.toLocalDate());

        if (hasConflict) {
            throw new IllegalStateException("場地在該時段已有其他場次");
        }
    }

    private void validateSeatingCapacity(Venue venue, int requiredSeats) {
        if (venue.getTotalSeats() < requiredSeats) {
            throw new IllegalStateException("場地座位數不足");
        }

        int availableSeats = venue.getAvailableSeats();
        if (availableSeats < requiredSeats) {
            throw new IllegalStateException("可用座位數不足");
        }
    }
}