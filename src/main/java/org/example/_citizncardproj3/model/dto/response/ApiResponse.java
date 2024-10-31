package org.example._citizncardproj3.model.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private boolean success;
    private String message;
    private String errorCode;
    private T data;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;

    private String path;
    private Integer status;

    // 成功響應構造函數
    public ApiResponse(T data) {
        this.success = true;
        this.data = data;
        this.timestamp = LocalDateTime.now();
    }

    // 成功響應構造函數（帶消息）
    public ApiResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
        this.timestamp = LocalDateTime.now();
    }

    // 錯誤響應構造函數
    public ApiResponse(boolean success, String message, String errorCode) {
        this.success = success;
        this.message = message;
        this.errorCode = errorCode;
        this.timestamp = LocalDateTime.now();
    }

    // 完整響應構造函數
    public ApiResponse(boolean success, String message, String errorCode, T data, String path, Integer status) {
        this.success = success;
        this.message = message;
        this.errorCode = errorCode;
        this.data = data;
        this.timestamp = LocalDateTime.now();
        this.path = path;
        this.status = status;
    }

    // 靜態成功響應方法
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(data);
    }

    public static <T> ApiResponse<T> success(String message) {
        return new ApiResponse<>(true, message);
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        ApiResponse<T> response = new ApiResponse<>(true, message);
        response.setData(data);
        return response;
    }

    // 靜態錯誤響應方法
    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, message);
    }

    public static <T> ApiResponse<T> error(String message, String errorCode) {
        return new ApiResponse<>(false, message, errorCode);
    }

    public static <T> ApiResponse<T> error(String message, String errorCode, Integer status) {
        ApiResponse<T> response = new ApiResponse<>(false, message, errorCode);
        response.setStatus(status);
        return response;
    }

    // 自定義響應建構器
    public static class ApiResponseBuilder<T> {
        private boolean success;
        private String message;
        private String errorCode;
        private T data;
        private String path;
        private Integer status;

        public ApiResponseBuilder<T> success(boolean success) {
            this.success = success;
            return this;
        }

        public ApiResponseBuilder<T> message(String message) {
            this.message = message;
            return this;
        }

        public ApiResponseBuilder<T> errorCode(String errorCode) {
            this.errorCode = errorCode;
            return this;
        }

        public ApiResponseBuilder<T> data(T data) {
            this.data = data;
            return this;
        }

        public ApiResponseBuilder<T> path(String path) {
            this.path = path;
            return this;
        }

        public ApiResponseBuilder<T> status(Integer status) {
            this.status = status;
            return this;
        }

        public ApiResponse<T> build() {
            ApiResponse<T> response = new ApiResponse<>();
            response.setSuccess(this.success);
            response.setMessage(this.message);
            response.setErrorCode(this.errorCode);
            response.setData(this.data);
            response.setTimestamp(LocalDateTime.now());
            response.setPath(this.path);
            response.setStatus(this.status);
            return response;
        }
    }

    // 輔助方法
    public boolean hasData() {
        return this.data != null;
    }

    public boolean hasError() {
        return !this.success;
    }

    // 用於日誌記錄的方法
    public String toLogString() {
        return String.format("ApiResponse{success=%s, message='%s', errorCode='%s', path='%s', status=%d}",
                success, message, errorCode, path, status);
    }
}