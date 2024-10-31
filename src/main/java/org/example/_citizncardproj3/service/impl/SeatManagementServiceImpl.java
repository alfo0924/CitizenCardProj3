package org.example._citizncardproj3.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example._citizncardproj3.exception.CustomException;
import org.example._citizncardproj3.model.entity.SeatManagement;
import org.example._citizncardproj3.model.entity.Venue;
import org.example._citizncardproj3.repository.SeatManagementRepository;
import org.example._citizncardproj3.repository.VenueRepository;
import org.example._citizncardproj3.service.SeatManagementService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SeatManagementServiceImpl implements SeatManagementService {

    private final SeatManagementRepository seatManagementRepository;
    private final VenueRepository venueRepository;

    @Override
    @Transactional
    public SeatManagement createSeat(Long venueId, String seatRow, String seatColumn, SeatManagement.SeatType seatType) {
        // 驗證場地
        Venue venue = venueRepository.findById(venueId)
                .orElseThrow(() -> new CustomException.VenueNotFoundException(venueId));

        // 檢查座位是否已存在
        if (seatManagementRepository.existsByVenueAndSeatRowAndSeatColumn(venue, seatRow, seatColumn)) {
            throw new IllegalStateException("座位已存在");
        }

        // 創建座位
        SeatManagement seat = SeatManagement.builder()
                .venue(venue)
                .seatRow(seatRow)
                .seatColumn(seatColumn)
                .seatLabel(seatRow + seatColumn)
                .seatZone("GENERAL")
                .seatType(seatType)
                .status(SeatManagement.SeatStatus.AVAILABLE)
                .isActive(true)
                .build();

        return seatManagementRepository.save(seat);
    }

    @Override
    @Transactional
    public void setMaintenance(Long seatId, String maintainer, String notes) {
        SeatManagement seat = seatManagementRepository.findById(seatId)
                .orElseThrow(() -> new IllegalArgumentException("座位不存在"));

        seat.setMaintenance(maintainer, notes);
        seatManagementRepository.save(seat);
    }

    @Override
    @Transactional
    public void completeMaintenance(Long seatId) {
        SeatManagement seat = seatManagementRepository.findById(seatId)
                .orElseThrow(() -> new IllegalArgumentException("座位不存在"));

        seat.completeMaintenance();
        seatManagementRepository.save(seat);
    }

    @Override
    @Transactional
    public void updateSeatType(Long seatId, SeatManagement.SeatType newType) {
        SeatManagement seat = seatManagementRepository.findById(seatId)
                .orElseThrow(() -> new IllegalArgumentException("座位不存在"));

        seat.updateSeatType(newType);
        seatManagementRepository.save(seat);
    }

    @Override
    @Transactional
    public void updateSeatZone(Long seatId, String newZone) {
        SeatManagement seat = seatManagementRepository.findById(seatId)
                .orElseThrow(() -> new IllegalArgumentException("座位不存在"));

        seat.updateZone(newZone);
        seatManagementRepository.save(seat);
    }

    @Override
    public List<SeatManagement> getVenueSeats(Long venueId) {
        Venue venue = venueRepository.findById(venueId)
                .orElseThrow(() -> new CustomException.VenueNotFoundException(venueId));

        return seatManagementRepository.findByVenue(venue);
    }

    @Override
    public Page<SeatManagement> getVenueSeatsByType(Long venueId, SeatManagement.SeatType seatType, Pageable pageable) {
        Venue venue = venueRepository.findById(venueId)
                .orElseThrow(() -> new CustomException.VenueNotFoundException(venueId));

        return seatManagementRepository.findByVenueAndSeatTypeOrderBySeatLabelAsc(venue, seatType, pageable);
    }

    @Override
    public List<SeatManagement> getAvailableSeats(Long venueId) {
        Venue venue = venueRepository.findById(venueId)
                .orElseThrow(() -> new CustomException.VenueNotFoundException(venueId));

        return seatManagementRepository.findAvailableSeats(venue);
    }

    @Override
    @Transactional
    public void checkAndUpdateMaintenanceStatus() {
        List<SeatManagement> seatsNeedingMaintenance =
                seatManagementRepository.findSeatsNeedingMaintenance(LocalDateTime.now());

        for (SeatManagement seat : seatsNeedingMaintenance) {
            seat.setStatus(SeatManagement.SeatStatus.MAINTENANCE);
            seatManagementRepository.save(seat);
        }
    }

    @Override
    public List<SeatManagement> getMaintenanceHistory(Long venueId, Pageable pageable) {
        Venue venue = venueRepository.findById(venueId)
                .orElseThrow(() -> new CustomException.VenueNotFoundException(venueId));

        return seatManagementRepository.findMaintenanceHistory(venue, pageable);
    }

    @Override
    public List<Object[]> getSeatTypeStatistics(Long venueId) {
        Venue venue = venueRepository.findById(venueId)
                .orElseThrow(() -> new CustomException.VenueNotFoundException(venueId));

        return seatManagementRepository.countBySeatType(venue);
    }

    @Override
    @Transactional
    public void disableSeat(Long seatId, String reason) {
        SeatManagement seat = seatManagementRepository.findById(seatId)
                .orElseThrow(() -> new IllegalArgumentException("座位不存在"));

        seat.disable(reason);
        seatManagementRepository.save(seat);
    }

    @Override
    @Transactional
    public void enableSeat(Long seatId) {
        SeatManagement seat = seatManagementRepository.findById(seatId)
                .orElseThrow(() -> new IllegalArgumentException("座位不存在"));

        seat.enable();
        seatManagementRepository.save(seat);
    }

    @Override
    public List<SeatManagement> findConsecutiveSeats(Long venueId, String row, int count) {
        Venue venue = venueRepository.findById(venueId)
                .orElseThrow(() -> new CustomException.VenueNotFoundException(venueId));

        return seatManagementRepository.findConsecutiveSeats(venue, row);
    }
}