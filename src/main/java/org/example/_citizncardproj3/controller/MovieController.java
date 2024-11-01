package org.example._citizncardproj3.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example._citizncardproj3.model.dto.request.MovieCreateRequest;
import org.example._citizncardproj3.model.dto.request.MovieUpdateRequest;
import org.example._citizncardproj3.model.dto.request.ScheduleCreateRequest;
import org.example._citizncardproj3.model.dto.response.ApiResponse;
import org.example._citizncardproj3.model.dto.response.MovieResponse;
import org.example._citizncardproj3.model.dto.response.ScheduleResponse;
import org.example._citizncardproj3.service.CityMovieService;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/movies")
@RequiredArgsConstructor
@Tag(name = "電影", description = "電影管理相關API")
public class MovieController {

    private final CityMovieService movieService;  // 改為CityMovieService

    @Operation(summary = "獲取所有電影")
    @GetMapping("/public")
    public ResponseEntity<Page<MovieResponse>> getAllMovies(
            @Parameter(description = "是否只顯示上映中") @RequestParam(required = false) Boolean showingOnly,
            Pageable pageable) {
        Page<MovieResponse> movies = movieService.getAllMovies(showingOnly, pageable);
        return ResponseEntity.ok(movies);
    }

    @Operation(summary = "獲取電影詳情")
    @GetMapping("/public/{movieId}")
    public ResponseEntity<MovieResponse> getMovie(
            @Parameter(description = "電影ID") @PathVariable Long movieId) {
        try {
            MovieResponse movie = movieService.getMovie(movieId);
            return ResponseEntity.ok(movie);
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(new MovieResponse(null, e.getMessage()));
        }
    }

    @Operation(summary = "搜索電影")
    @GetMapping("/public/search")
    public ResponseEntity<Page<MovieResponse>> searchMovies(
            @Parameter(description = "搜索關鍵字") @RequestParam String keyword,
            Pageable pageable) {
        Page<MovieResponse> movies = movieService.searchMovies(keyword, pageable);
        return ResponseEntity.ok(movies);
    }

    @Operation(summary = "獲取電影場次")
    @GetMapping("/public/{movieId}/schedules")
    public ResponseEntity<List<ScheduleResponse>> getMovieSchedules(
            @Parameter(description = "電影ID") @PathVariable Long movieId,
            @Parameter(description = "日期")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        try {
            List<ScheduleResponse> schedules = movieService.getMovieSchedules(movieId, date);
            return ResponseEntity.ok(schedules);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    // 管理員API
    @Operation(summary = "新增電影")
    @PostMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MovieResponse> createMovie(
            @Valid @RequestBody MovieCreateRequest request) {
        try {
            MovieResponse movie = movieService.createMovie(request);
            return ResponseEntity.ok(movie);
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new MovieResponse(null, e.getMessage()));
        }
    }

    @Operation(summary = "更新電影資訊")
    @PutMapping("/admin/{movieId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MovieResponse> updateMovie(
            @Parameter(description = "電影ID") @PathVariable Long movieId,
            @Valid @RequestBody MovieUpdateRequest request) {
        try {
            MovieResponse movie = movieService.updateMovie(movieId, request);
            return ResponseEntity.ok(movie);
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new MovieResponse(null, e.getMessage()));
        }
    }

    @Operation(summary = "上傳電影海報")
    @PostMapping("/admin/{movieId}/poster")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> uploadPoster(
            @Parameter(description = "電影ID") @PathVariable Long movieId,
            @RequestParam("file") MultipartFile file) {
        try {
            String posterUrl = movieService.uploadPoster(movieId, file);
            return ResponseEntity.ok(new ApiResponse(true, posterUrl));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, e.getMessage()));
        }
    }

    @Operation(summary = "新增電影場次")
    @PostMapping("/admin/{movieId}/schedules")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ScheduleResponse> createSchedule(
            @Parameter(description = "電影ID") @PathVariable Long movieId,
            @Valid @RequestBody ScheduleCreateRequest request) {
        try {
            ScheduleResponse schedule = movieService.createSchedule(movieId, request);
            return ResponseEntity.ok(schedule);
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ScheduleResponse(null, e.getMessage()));
        }
    }

    @Operation(summary = "刪除電影場次")
    @DeleteMapping("/admin/schedules/{scheduleId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> deleteSchedule(
            @Parameter(description = "場次ID") @PathVariable Long scheduleId) {
        try {
            movieService.deleteSchedule(scheduleId);
            return ResponseEntity.ok(new ApiResponse(true, "場次刪除成功"));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, e.getMessage()));
        }
    }

    @Operation(summary = "下架電影")
    @DeleteMapping("/admin/{movieId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> deleteMovie(
            @Parameter(description = "電影ID") @PathVariable Long movieId) {
        try {
            movieService.deleteMovie(movieId);
            return ResponseEntity.ok(new ApiResponse(true, "電影下架成功"));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, e.getMessage()));
        }
    }
}