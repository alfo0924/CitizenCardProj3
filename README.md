# CitizenCardProj3 |  專案架構

 基於Spring Boot JDK 17
這個架構是市民卡系統，並可以與前端進行CRUD操作和API查詢

```
CitiznCardproj3/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── org/example/_citizncardproj3/
│   │   │       ├── config/                    # 配置類
│   │   │       │   ├── SecurityConfig.java    # Spring Security配置
│   │   │       │   ├── SwaggerConfig.java     # Swagger API文檔配置
│   │   │       │   └── WebConfig.java         # Web相關配置
│   │   │       │
│   │   │       ├── controller/               # 控制層
│   │   │       │   ├── AuthController.java    # 認證相關
│   │   │       │   ├── MemberController.java  # 會員管理
│   │   │       │   ├── MovieController.java   # 電影相關
│   │   │       │   ├── BookingController.java # 訂票相關
│   │   │       │   ├── WalletController.java  # 電子錢包
│   │   │       │   └── DiscountController.java # 優惠管理
│   │   │       │
│   │   │       ├── model/                    # 實體類
│   │   │       │   ├── entity/               # 數據庫實體
│   │   │       │   └── dto/                  # 數據傳輸對象
│   │   │       │
│   │   │       ├── repository/               # 數據訪問層
│   │   │       │   ├── MemberRepository.java
│   │   │       │   ├── MovieRepository.java
│   │   │       │   └── ...
│   │   │       │
│   │   │       ├── service/                  # 業務邏輯層
│   │   │       │   ├── impl/                 # 實現類
│   │   │       │   ├── MemberService.java
│   │   │       │   ├── MovieService.java
│   │   │       │   └── ...
│   │   │       │
│   │   │       ├── security/                 # 安全相關
│   │   │       │   ├── JwtTokenProvider.java
│   │   │       │   └── UserDetailsServiceImpl.java
│   │   │       │
│   │   │       ├── exception/                # 異常處理
│   │   │       │   ├── GlobalExceptionHandler.java
│   │   │       │   └── CustomException.java
│   │   │       │
│   │   │       ├── util/                     # 工具類
│   │   │       │   ├── DateUtils.java
│   │   │       │   └── ValidationUtils.java
│   │   │       │
│   │   │       └── CitiznCardproj3Application.java
│   │   │
│   │   └── resources/
│   │       ├── application.properties        # 主配置文件
│   │       ├── application-dev.properties    # 開發環境配置
│   │       ├── application-prod.properties   # 生產環境配置
│   │       └── sql/                         # SQL腳本
│   │           ├── schema.sql               # 建表語句
│   │           └── data.sql                 # 初始數據
│   │
│   └── test/                                # 測試目錄
│       └── java/
│           └── org/example/_citizncardproj3/
│               ├── controller/
│               ├── service/
│               └── repository/
│
├── .mvn/
├── .idea/
├── target/
├── pom.xml                                  # Maven配置文件
├── .gitignore
└── README.md
```


這個結構包含了以下主要部分：

1. `config/`: 包含配置類，如安全配置。
2. `controller/`: 包含所有的REST控制器。
3. `model/`: 包含所有的實體類。
4. `repository/`: 包含所有的數據訪問接口。
5. `service/`: 包含所有的業務邏輯服務。
6. `util/`: 包含工具類，如JWT工具。


### model、repository和service層結構：

```
├── model/
│   ├── entity/
│   │   ├── Member.java                 # 會員實體
│   │   ├── CitizenCard.java           # 市民卡實體
│   │   ├── IdentityVerification.java  # 身份驗證實體
│   │   ├── VirtualCard.java           # 虛擬卡實體
│   │   ├── CityMovie.java             # 電影實體
│   │   ├── MovieSchedule.java         # 電影場次實體
│   │   ├── Venue.java                 # 場地實體
│   │   ├── SeatManagement.java        # 座位管理實體
│   │   ├── Booking.java               # 訂位實體
│   │   ├── SeatBooking.java           # 座位預訂實體
│   │   ├── Wallet.java                # 電子錢包實體
│   │   ├── Transaction.java           # 交易記錄實體
│   │   ├── Discount.java              # 優惠實體
│   │   ├── DiscountUsage.java         # 優惠使用記錄實體
│   │   ├── SystemLog.java             # 系統日誌實體
│   │   ├── Notification.java          # 通知實體
│   │   └── Review.java                # 評價實體
│   │
│   └── dto/
│       ├── request/
│       │   ├── MemberRegistrationRequest.java
│       │   ├── LoginRequest.java
│       │   ├── BookingRequest.java
│       │   ├── WalletTopUpRequest.java
│       │   └── MovieCreateRequest.java
│       │
│       └── response/
│           ├── MemberResponse.java
│           ├── movieresponse.java
│           ├── BookingResponse.java
│           ├── WalletResponse.java
│           └── ApiResponse.java
│
├── repository/
│   ├── MemberRepository.java
│   ├── CitizenCardRepository.java
│   ├── IdentityVerificationRepository.java
│   ├── VirtualCardRepository.java
│   ├── CityMovieRepository.java
│   ├── MovieScheduleRepository.java
│   ├── VenueRepository.java
│   ├── SeatManagementRepository.java
│   ├── BookingRepository.java
│   ├── SeatBookingRepository.java
│   ├── WalletRepository.java
│   ├── TransactionRepository.java
│   ├── DiscountRepository.java
│   ├── DiscountUsageRepository.java
│   ├── SystemLogRepository.java
│   ├── NotificationRepository.java
│   └── ReviewRepository.java
│
├── service/
│   ├── impl/
│   │   ├── MemberServiceImpl.java
│   │   ├── CitizenCardServiceImpl.java
│   │   ├── IdentityVerificationServiceImpl.java
│   │   ├── VirtualCardServiceImpl.java
│   │   ├── CityMovieServiceImpl.java
│   │   ├── MovieScheduleServiceImpl.java
│   │   ├── VenueServiceImpl.java
│   │   ├── SeatManagementServiceImpl.java
│   │   ├── BookingServiceImpl.java
│   │   ├── WalletServiceImpl.java
│   │   ├── TransactionServiceImpl.java
│   │   ├── DiscountServiceImpl.java
│   │   ├── NotificationServiceImpl.java
│   │   ├── SystemLogServiceImpl.java
│   │   └── ReviewServiceImpl.java
│   │
│   ├── MemberService.java
│   ├── CitizenCardService.java
│   ├── IdentityVerificationService.java
│   ├── VirtualCardService.java
│   ├── CityMovieService.java
│   ├── MovieScheduleService.java
│   ├── VenueService.java
│   ├── SeatManagementService.java
│   ├── BookingService.java
│   ├── WalletService.java
│   ├── TransactionService.java
│   ├── DiscountService.java
│   ├── NotificationService.java
│   ├── SystemLogService.java
│   └── ReviewService.java
```


1. **實體類(entity)**：對應數據庫表的Java類
2. **DTO類**：用於數據傳輸的請求和響應對象
3. **Repository接口**：處理數據訪問的接口
4. **Service接口和實現**：處理業務邏輯的接口和實現類


