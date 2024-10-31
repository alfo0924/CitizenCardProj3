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
    public Venue createVenue(String venueName, String address, Integer totalRows, Integer totalColumns) {
        // 檢查場地名稱是否已存在
        if (venueRepository.existsByVenueName(venueName)) {
            throw new IllegalStateException("場地名稱已存在");
        }

        // 創建場地
        Venue venue = Venue.builder()
                .venueName(venueName)
                .address(address)
                .totalRows(totalRows)
                .totalColumns(totalColumns)
                .totalSeats(totalRows * totalColumns)
                .status(Venue.VenueStatus.ACTIVE)
                .build();

        venue = venueRepository.save(venue);

        // 初始化座位配置
        initializeSeats(venue);

        return venue;
    }

    @Override
    @Transactional
    public void setMaintenance(Long venueId, LocalDateTime maintenanceDate) {
        Venue venue = venueRepository.findById(venueId)
                .orElseThrow(() -> new CustomException.VenueNotFoundException(venueId));

        if (venue.hasActiveSchedules()) {
            throw new IllegalStateException("場地有未完成的場次，無法進行維護");
        }

        venue.setMaintenance(maintenanceDate);
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
        return venueRepository.findAvailableVenues(50); // 最小座位數設為50
    }

    @Override
    public List<Venue> getAvailableVenuesForTime(LocalDateTime startTime, LocalDateTime endTime) {
        return venueRepository.findAvailableVenuesForTime(startTime, endTime);
    }

    @Override
    public Page<MovieSchedule> getVenueSchedules(Long venueId, Pageable pageable) {
        Venue venue = venueRepository.findById(venueId)
                .orElseThrow(() -> new CustomException.VenueNotFoundException(venueId));

        return scheduleRepository.findByVenueOrderByShowTimeAsc(venue, pageable);
    }

    @Override
    public List<Object[]> getVenueUtilization(Long venueId, LocalDateTime startTime, LocalDateTime endTime) {
        Venue venue = venueRepository.findById(venueId)
                .orElseThrow(() -> new CustomException.VenueNotFoundException(venueId));

        return venueRepository.getVenueUtilization(startTime, endTime);
    }

    @Override
    @Transactional
    public void checkAndUpdateMaintenanceStatus() {
        List<Venue> venuesNeedingMaintenance =
                venueRepository.findVenuesNeedingMaintenance(LocalDateTime.now());

        for (Venue venue : venuesNeedingMaintenance) {
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
        return null;
    }

    @Override
    public Venue updateVenueInfo(Long venueId, String venueName, String address) {
        return null;
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
        for (int row = 0; row < venue.getTotalRows(); row++) {
            String rowLabel = String.valueOf((char)('A' + row));

            for (int col = 1; col <= venue.getTotalColumns(); col++) {
                SeatManagement seat = SeatManagement.builder()
                        .venue(venue)
                        .seatRow(rowLabel)
                        .seatColumn(String.valueOf(col))
                        .seatLabel(rowLabel + col)
                        .seatZone("GENERAL")
                        .seatType(SeatManagement.SeatType.REGULAR)
                        .status(SeatManagement.SeatStatus.AVAILABLE)
                        .isActive(true)
                        .build();

                seatManagementRepository.save(seat);
            }
        }
    }

    private void validateVenueAvailability(Venue venue, LocalDateTime startTime, LocalDateTime endTime) {
        if (venue.getStatus() != Venue.VenueStatus.ACTIVE) {
            throw new IllegalStateException("場地目前無法使用");
        }

        boolean hasConflict = scheduleRepository.existsByVenueAndShowTimeBetween(
                venue, startTime, endTime);

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