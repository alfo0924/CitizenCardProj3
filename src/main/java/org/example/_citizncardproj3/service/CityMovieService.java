package org.example._citizncardproj3.service;

import org.example._citizncardproj3.model.dto.request.MovieCreateRequest;
import org.example._citizncardproj3.model.dto.request.MovieUpdateRequest;
import org.example._citizncardproj3.model.dto.request.ScheduleCreateRequest;
import org.example._citizncardproj3.model.dto.response.MovieResponse;
import org.example._citizncardproj3.model.dto.response.ScheduleResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface CityMovieService {

    /**
     * 獲取所有電影列表
     * @param showingOnly 是否只顯示上映中的電影
     * @param pageable 分頁參數
     * @return 電影列表分頁
     */
    Page<MovieResponse> getAllMovies(Boolean showingOnly, Pageable pageable);

    /**
     * 獲取電影詳情
     * @param movieId 電影ID
     * @return 電影詳情
     */
    MovieResponse getMovie(Long movieId);

    /**
     * 搜索電影
     * @param keyword 關鍵字
     * @param pageable 分頁參數
     * @return 搜索結果分頁
     */
    Page<MovieResponse> searchMovies(String keyword, Pageable pageable);

    /**
     * 獲取電影場次
     * @param movieId 電影ID
     * @param date 日期
     * @return 場次列表
     */
    List<ScheduleResponse> getMovieSchedules(Long movieId, LocalDate date);

    /**
     * 創建電影
     * @param request 創建請求
     * @return 創建的電影
     */
    MovieResponse createMovie(MovieCreateRequest request);

    /**
     * 更新電影資訊
     * @param movieId 電影ID
     * @param request 更新請求
     * @return 更新後的電影
     */
    MovieResponse updateMovie(Long movieId, MovieUpdateRequest request);

    /**
     * 上傳電影海報
     * @param movieId 電影ID
     * @param file 海報文件
     * @return 海報URL
     */
    String uploadPoster(Long movieId, MultipartFile file);

    /**
     * 創建電影場次
     * @param movieId 電影ID
     * @param request 場次創建請求
     * @return 創建的場次
     */
    ScheduleResponse createSchedule(Long movieId, ScheduleCreateRequest request);

    /**
     * 刪除電影場次
     * @param scheduleId 場次ID
     */
    void deleteSchedule(Long scheduleId);

    /**
     * 下架電影
     * @param movieId 電影ID
     */
    void deleteMovie(Long movieId);

    /**
     * 獲取即將上映的電影
     * @param pageable 分頁參數
     * @return 電影列表分頁
     */
    Page<MovieResponse> getUpcomingMovies(Pageable pageable);

    /**
     * 獲取熱映電影
     * @param pageable 分頁參數
     * @return 電影列表分頁
     */
    Page<MovieResponse> getNowShowingMovies(Pageable pageable);

    /**
     * 延長電影上映期間
     * @param movieId 電影ID
     * @param endDate 新的下檔日期
     * @return 更新後的電影
     */
    MovieResponse extendShowingPeriod(Long movieId, LocalDate endDate);

    /**
     * 更新電影評分
     * @param movieId 電影ID
     * @param rating 評分
     * @return 更新後的電影
     */
    MovieResponse updateRating(Long movieId, String rating);

    /**
     * 獲取電影統計資訊
     * @param movieId 電影ID
     * @return 統計資訊
     */
    Map<String, Object> getMovieStatistics(Long movieId);
}