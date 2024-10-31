package org.example._citizncardproj3.repository;

import org.example._citizncardproj3.model.entity.CityMovie;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.persistence.LockModeType;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface CityMovieRepository extends JpaRepository<CityMovie, Long> {

    // 基本查詢方法
    Optional<CityMovie> findByMovieCode(String movieCode);

    List<CityMovie> findByStatus(CityMovie.MovieStatus status);

    boolean existsByMovieCode(String movieCode);

    // 分頁查詢
    Page<CityMovie> findByStatusOrderByReleaseDateDesc(
            CityMovie.MovieStatus status,
            Pageable pageable
    );

    Page<CityMovie> findByStatusAndReleaseDateGreaterThanEqual(
            CityMovie.MovieStatus status,
            LocalDate date,
            Pageable pageable
    );

    // 複雜條件查詢
    @Query("SELECT m FROM CityMovie m WHERE m.status = 'NOW_SHOWING' " +
            "AND :currentDate BETWEEN m.releaseDate AND m.endDate")
    List<CityMovie> findCurrentlyShowing(@Param("currentDate") LocalDate currentDate);

    @Query("SELECT m FROM CityMovie m WHERE m.releaseDate > :currentDate " +
            "AND m.status = 'COMING_SOON' ORDER BY m.releaseDate ASC")
    List<CityMovie> findUpcomingMovies(@Param("currentDate") LocalDate currentDate);

    // 搜索查詢
    @Query("SELECT m FROM CityMovie m WHERE " +
            "LOWER(m.movieName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(m.director) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<CityMovie> searchMovies(@Param("keyword") String keyword, Pageable pageable);

    // 統計查詢
    @Query("SELECT m.status, COUNT(m) FROM CityMovie m GROUP BY m.status")
    List<Object[]> countByStatus();

    @Query("SELECT COUNT(m) FROM CityMovie m WHERE m.releaseDate BETWEEN :startDate AND :endDate")
    long countMoviesInPeriod(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    // 更新操作
    @Modifying
    @Query("UPDATE CityMovie m SET m.status = :newStatus WHERE m.movieId = :movieId")
    int updateMovieStatus(
            @Param("movieId") Long movieId,
            @Param("newStatus") CityMovie.MovieStatus newStatus
    );

    @Modifying
    @Query("UPDATE CityMovie m SET m.endDate = :newEndDate WHERE m.movieId = :movieId")
    int extendShowingPeriod(
            @Param("movieId") Long movieId,
            @Param("newEndDate") LocalDate newEndDate
    );

    // 批量操作
    @Modifying
    @Query("UPDATE CityMovie m SET m.status = 'END_SHOWING' " +
            "WHERE m.endDate < :currentDate AND m.status = 'NOW_SHOWING'")
    int updateEndedMovies(@Param("currentDate") LocalDate currentDate);

    // 使用悲觀鎖查詢
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT m FROM CityMovie m WHERE m.movieId = :movieId")
    Optional<CityMovie> findByIdWithLock(@Param("movieId") Long movieId);

    // 分類查詢
    List<CityMovie> findByCategoriesContaining(String category);

    // 價格範圍查詢
    List<CityMovie> findByBasePriceBetween(Double minPrice, Double maxPrice);

    // 語言查詢
    List<CityMovie> findByLanguageAndSubtitle(String language, String subtitle);

    // 評分查詢
    List<CityMovie> findByRatingOrderByReleaseDateDesc(String rating);

    // 日期範圍查詢
    @Query("SELECT m FROM CityMovie m WHERE " +
            "(m.releaseDate BETWEEN :startDate AND :endDate) OR " +
            "(m.endDate BETWEEN :startDate AND :endDate)")
    List<CityMovie> findMoviesInDateRange(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    // 熱門電影查詢
    @Query("SELECT m FROM CityMovie m JOIN m.bookings b " +
            "GROUP BY m HAVING COUNT(b) > :threshold " +
            "ORDER BY COUNT(b) DESC")
    List<CityMovie> findPopularMovies(@Param("threshold") long threshold);

    // 軟刪除
    @Modifying
    @Query("UPDATE CityMovie m SET m.isDeleted = true WHERE m.movieId = :movieId")
    int softDeleteMovie(@Param("movieId") Long movieId);

    /**
     * 檢查電影名稱是否存在
     */
    boolean existsByMovieName(String movieName);

}