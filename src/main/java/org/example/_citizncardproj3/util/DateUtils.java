package org.example._citizncardproj3.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class DateUtils {

    private static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd";
    private static final String DEFAULT_DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    private static final String SIMPLE_DATETIME_FORMAT = "yyyyMMddHHmmss";

    /**
     * 將LocalDateTime轉換為指定格式的字串
     * @param dateTime 日期時間
     * @param pattern 格式
     * @return 格式化後的字串
     */
    public static String formatDateTime(LocalDateTime dateTime, String pattern) {
        if (dateTime == null) return null;
        return DateTimeFormatter.ofPattern(pattern).format(dateTime);
    }

    /**
     * 將LocalDateTime轉換為預設格式的字串
     * @param dateTime 日期時間
     * @return 格式化後的字串
     */
    public static String formatDateTime(LocalDateTime dateTime) {
        return formatDateTime(dateTime, DEFAULT_DATETIME_FORMAT);
    }

    /**
     * 將LocalDate轉換為指定格式的字串
     * @param date 日期
     * @param pattern 格式
     * @return 格式化後的字串
     */
    public static String formatDate(LocalDate date, String pattern) {
        if (date == null) return null;
        return DateTimeFormatter.ofPattern(pattern).format(date);
    }

    /**
     * 將LocalDate轉換為預設格式的字串
     * @param date 日期
     * @return 格式化後的字串
     */
    public static String formatDate(LocalDate date) {
        return formatDate(date, DEFAULT_DATE_FORMAT);
    }

    /**
     * 將字串轉換為LocalDateTime
     * @param dateTimeStr 日期時間字串
     * @param pattern 格式
     * @return LocalDateTime物件
     */
    public static LocalDateTime parseDateTime(String dateTimeStr, String pattern) {
        if (dateTimeStr == null || dateTimeStr.trim().isEmpty()) return null;
        return LocalDateTime.parse(dateTimeStr, DateTimeFormatter.ofPattern(pattern));
    }

    /**
     * 將字串轉換為預設格式的LocalDateTime
     * @param dateTimeStr 日期時間字串
     * @return LocalDateTime物件
     */
    public static LocalDateTime parseDateTime(String dateTimeStr) {
        return parseDateTime(dateTimeStr, DEFAULT_DATETIME_FORMAT);
    }

    /**
     * 將字串轉換為LocalDate
     * @param dateStr 日期字串
     * @param pattern 格式
     * @return LocalDate物件
     */
    public static LocalDate parseDate(String dateStr, String pattern) {
        if (dateStr == null || dateStr.trim().isEmpty()) return null;
        return LocalDate.parse(dateStr, DateTimeFormatter.ofPattern(pattern));
    }

    /**
     * 將字串轉換為預設格式的LocalDate
     * @param dateStr 日期字串
     * @return LocalDate物件
     */
    public static LocalDate parseDate(String dateStr) {
        return parseDate(dateStr, DEFAULT_DATE_FORMAT);
    }

    /**
     * 計算兩個日期之間的天數
     * @param start 開始日期
     * @param end 結束日期
     * @return 天數
     */
    public static long daysBetween(LocalDate start, LocalDate end) {
        return ChronoUnit.DAYS.between(start, end);
    }

    /**
     * 計算兩個時間之間的分鐘數
     * @param start 開始時間
     * @param end 結束時間
     * @return 分鐘數
     */
    public static long minutesBetween(LocalDateTime start, LocalDateTime end) {
        return ChronoUnit.MINUTES.between(start, end);
    }

    /**
     * 生成時間戳字串
     * @return 時間戳字串
     */
    public static String generateTimestamp() {
        return formatDateTime(LocalDateTime.now(), SIMPLE_DATETIME_FORMAT);
    }

    /**
     * 檢查日期是否在指定範圍內
     * @param date 待檢查日期
     * @param start 開始日期
     * @param end 結束日期
     * @return 是否在範圍內
     */
    public static boolean isDateInRange(LocalDate date, LocalDate start, LocalDate end) {
        return !date.isBefore(start) && !date.isAfter(end);
    }

    /**
     * 檢查時間是否在指定範圍內
     * @param dateTime 待檢查時間
     * @param start 開始時間
     * @param end 結束時間
     * @return 是否在範圍內
     */
    public static boolean isDateTimeInRange(LocalDateTime dateTime, LocalDateTime start, LocalDateTime end) {
        return !dateTime.isBefore(start) && !dateTime.isAfter(end);
    }

    /**
     * 獲取指定日期的開始時間
     * @param date 日期
     * @return 該日期的開始時間
     */
    public static LocalDateTime getStartOfDay(LocalDate date) {
        return date.atStartOfDay();
    }

    /**
     * 獲取指定日期的結束時間
     * @param date 日期
     * @return 該日期的結束時間
     */
    public static LocalDateTime getEndOfDay(LocalDate date) {
        return date.atTime(23, 59, 59);
    }

    /**
     * 檢查是否過期
     * @param expiryTime 過期時間
     * @return 是否過期
     */
    public static boolean isExpired(LocalDateTime expiryTime) {
        return LocalDateTime.now().isAfter(expiryTime);
    }
}