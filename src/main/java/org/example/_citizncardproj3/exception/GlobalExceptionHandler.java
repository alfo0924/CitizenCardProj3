package org.example._citizncardproj3.exception;

import lombok.extern.slf4j.Slf4j;
import org.example._citizncardproj3.model.dto.response.ApiResponse;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import javax.persistence.EntityNotFoundException;
import java.nio.file.AccessDeniedException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 處理自定義異常
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ApiResponse> handleCustomException(CustomException ex) {
        log.error("CustomException: {}", ex.getMessage());
        return new ResponseEntity<>(
                new ApiResponse(false, ex.getMessage(), ex.getErrorCode()),
                ex.getStatus());
    }

    // 處理參數驗證異常
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse> handleValidationException(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage())
        );

        String errorMessage = errors.entrySet().stream()
                .map(entry -> entry.getKey() + ": " + entry.getValue())
                .collect(Collectors.joining("; "));

        log.error("Validation error: {}", errorMessage);
        return ResponseEntity.badRequest()
                .body(new ApiResponse(false, errorMessage, "VALID_001"));
    }

    // 處理認證相關異常
    @ExceptionHandler({AuthenticationException.class, BadCredentialsException.class})
    public ResponseEntity<ApiResponse> handleAuthenticationException(Exception ex) {
        log.error("Authentication error: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ApiResponse(false, "認證失敗：" + ex.getMessage(), "AUTH_001"));
    }

    // 處理授權相關異常
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse> handleAccessDeniedException(AccessDeniedException ex) {
        log.error("Access denied: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ApiResponse(false, "無權限訪問此資源", "AUTH_002"));
    }

    // 處理資料庫相關異常
    @ExceptionHandler({DataIntegrityViolationException.class, ConstraintViolationException.class})
    public ResponseEntity<ApiResponse> handleDatabaseException(Exception ex) {
        log.error("Database error: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ApiResponse(false, "資料操作違反限制", "DB_001"));
    }

    // 處理實體未找到異常
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ApiResponse> handleEntityNotFoundException(EntityNotFoundException ex) {
        log.error("Entity not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiResponse(false, ex.getMessage(), "DB_002"));
    }

    // 處理檔案上傳相關異常
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiResponse> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException ex) {
        log.error("File upload error: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiResponse(false, "檔案大小超過限制", "FILE_001"));
    }

    // 處理請求參數類型不匹配異常
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String error = String.format("參數 '%s' 的值 '%s' 應該是 %s 類型",
                ex.getName(), ex.getValue(), ex.getRequiredType().getSimpleName());
        log.error("Type mismatch: {}", error);
        return ResponseEntity.badRequest()
                .body(new ApiResponse(false, error, "REQ_001"));
    }

    // 處理缺少必要請求參數異常
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponse> handleMissingParams(MissingServletRequestParameterException ex) {
        String error = String.format("缺少必要參數: %s (%s)", ex.getParameterName(), ex.getParameterType());
        log.error("Missing parameter: {}", error);
        return ResponseEntity.badRequest()
                .body(new ApiResponse(false, error, "REQ_002"));
    }

    // 處理綁定異常
    @ExceptionHandler(BindException.class)
    public ResponseEntity<ApiResponse> handleBindException(BindException ex) {
        String error = ex.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                .collect(Collectors.joining(", "));
        log.error("Binding error: {}", error);
        return ResponseEntity.badRequest()
                .body(new ApiResponse(false, error, "REQ_003"));
    }

    // 處理所有未捕獲的異常
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse> handleException(Exception ex) {
        log.error("Unexpected error: ", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse(false, "系統發生未預期的錯誤，請稍後再試", "SYS_001"));
    }

    // 處理業務邏輯異常
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiResponse> handleIllegalStateException(IllegalStateException ex) {
        log.error("Business logic error: {}", ex.getMessage());
        return ResponseEntity.badRequest()
                .body(new ApiResponse(false, ex.getMessage(), "BIZ_001"));
    }

    // 處理非法參數異常
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse> handleIllegalArgumentException(IllegalArgumentException ex) {
        log.error("Invalid argument: {}", ex.getMessage());
        return ResponseEntity.badRequest()
                .body(new ApiResponse(false, ex.getMessage(), "ARG_001"));
    }
}