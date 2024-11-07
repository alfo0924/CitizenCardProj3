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
        if (venueRepository.existsByVenueName(venueName)) {
            throw new IllegalStateException("場地名稱已存在");
        }

        // 計算預設的行列數
        int totalRows = (int) Math.ceil(Math.sqrt(totalSeats));
        int totalColumns = (int) Math.ceil((double) totalSeats / totalRows);

        Venue venue = Venue.builder()
                .venueName(venueName)
                .address(address)
                .totalSeats(totalSeats)
                .status(Venue.VenueStatus.ACTIVE)
                .seatingLayout(generateGridSeatingLayout(totalRows, totalColumns))
                .isDeleted(false)
                .build();

        venue = venueRepository.save(venue);
        initializeGridSeats(venue, totalRows, totalColumns);
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
    public void setMaintenance(Long venueId) {
        Venue venue = venueRepository.findById(venueId)
                .orElseThrow(() -> new CustomException.VenueNotFoundException(venueId));

        if (venue.hasActiveSchedules()) {
            throw new IllegalStateException("場地有未完成的場次，無法進行維護");
        }

        venue.setStatus(Venue.VenueStatus.MAINTENANCE);
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
        return venueRepository.findByStatus(Venue.VenueStatus.ACTIVE);
    }    @Override
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
        return List.of();
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

    // 新增檢查場地可用性的方法
    @Override
    public boolean checkVenueAvailability(Long venueId, LocalDateTime startTime, LocalDateTime endTime) {
        Venue venue = venueRepository.findById(venueId)
                .orElseThrow(() -> new CustomException.VenueNotFoundException(venueId));

        if (venue.getStatus() != Venue.VenueStatus.ACTIVE) {
            return false;
        }

        List<MovieSchedule> conflictingSchedules = scheduleRepository.findConflictingSchedules(
                venue, startTime, endTime);
        return conflictingSchedules.isEmpty();
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

    // 新增獲取維護歷史記錄的方法
    @Override
    public Page<Map<String, Object>> getMaintenanceHistory(Long venueId, Pageable pageable) {
        return venueRepository.findMaintenanceHistoryByVenueId(venueId, pageable);
    }

    @Override
    public Map<String, Object> getSeatUsageStatistics(Long venueId) {
        Venue venue = venueRepository.findById(venueId)
                .orElseThrow(() -> new CustomException.VenueNotFoundException(venueId));

        int totalSeats = venue.getTotalSeats();
        int availableSeats = venue.getAvailableSeats();
        int occupiedSeats = totalSeats - availableSeats;

        return Map.of(
                "totalSeats", totalSeats,
                "availableSeats", availableSeats,
                "occupiedSeats", occupiedSeats,
                "occupancyRate", (double) occupiedSeats / totalSeats * 100
        );
    }

    // 實作缺失的createVenue方法
    @Override
    @Transactional
    public Venue createVenue(String venueName, String address, Integer totalRows, Integer totalColumns) {
        if (venueRepository.existsByVenueName(venueName)) {
            throw new IllegalStateException("場地名稱已存在");
        }

        int totalSeats = totalRows * totalColumns;
        Venue venue = Venue.builder()
                .venueName(venueName)
                .address(address)
                .totalSeats(totalSeats)
                .status(Venue.VenueStatus.ACTIVE)
                .seatingLayout(generateGridSeatingLayout(totalRows, totalColumns))
                .isDeleted(false)
                .build();

        venue = venueRepository.save(venue);
        initializeGridSeats(venue, totalRows, totalColumns);
        return venue;
    }

    // 修正generateDefaultSeatingLayout方法
    private String generateDefaultSeatingLayout(int totalSeats) {
        StringBuilder layout = new StringBuilder();
        layout.append("{\"seats\":[");
        for (int i = 0; i < totalSeats; i++) {
            layout.append("{\"seatId\":").append(i + 1)
                    .append(",\"row\":").append((i / 10) + 1)
                    .append(",\"column\":").append((i % 10) + 1)
                    .append(",\"status\":\"AVAILABLE\"}");
            if (i < totalSeats - 1) {
                layout.append(",");
            }
        }
        layout.append("]}");
        return layout.toString();
    }

    // 新增網格座位布局生成方法
    private String generateGridSeatingLayout(int totalRows, int totalColumns) {
        StringBuilder layout = new StringBuilder();
        layout.append("{\"seats\":[");
        int seatId = 1;
        for (int row = 1; row <= totalRows; row++) {
            for (int col = 1; col <= totalColumns; col++) {
                layout.append("{\"seatId\":").append(seatId)
                        .append(",\"row\":").append(row)
                        .append(",\"column\":").append(col)
                        .append(",\"status\":\"AVAILABLE\"}");
                if (seatId < (totalRows * totalColumns)) {
                    layout.append(",");
                }
                seatId++;
            }
        }
        layout.append("]}");
        return layout.toString();
    }

    // 修正initializeSeats方法
    private void initializeGridSeats(Venue venue, int totalRows, int totalColumns) {
        int seatId = 1;
        for (int row = 1; row <= totalRows; row++) {
            for (int col = 1; col <= totalColumns; col++) {
                SeatManagement seat = SeatManagement.builder()
                        .venue(venue)
                        .seatLabel(String.format("R%dC%d", row, col))
                        .seatId((long) seatId)
                        .seatRow(String.valueOf(row))
                        .seatColumn(String.valueOf(col))
                        .seatType(SeatManagement.SeatType.REGULAR)
                        .status(SeatManagement.SeatStatus.AVAILABLE)
                        .isActive(true)
                        .build();
                seatManagementRepository.save(seat);
            }
        }
    }




}

