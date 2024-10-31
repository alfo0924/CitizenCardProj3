# CitizenCardProj3 |  專案架構

 基於Spring Boot JDK 17
這個架構是市民卡系統，並可以與前端進行CRUD操作和API查詢

```
CitiznCardproj3/
│
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── org/
│   │   │       └── example/
│   │   │           └── citizncardproj3/
│   │   │               ├── CitiznCardproj3Application.java
│   │   │               ├── config/
│   │   │               │   └── SecurityConfig.java
│   │   │               ├── controller/
│   │   │               │   ├── MemberController.java
│   │   │               │   ├── CityMovieController.java
│   │   │               │   ├── BookingController.java
│   │   │               │   └── WalletController.java
│   │   │               ├── model/
│   │   │               │   ├── Member.java
│   │   │               │   ├── CitizenCard.java
│   │   │               │   ├── CityMovie.java
│   │   │               │   ├── Booking.java
│   │   │               │   └── Wallet.java
│   │   │               ├── repository/
│   │   │               │   ├── MemberRepository.java
│   │   │               │   ├── CityMovieRepository.java
│   │   │               │   ├── BookingRepository.java
│   │   │               │   └── WalletRepository.java
│   │   │               ├── service/
│   │   │               │   ├── MemberService.java
│   │   │               │   ├── CityMovieService.java
│   │   │               │   ├── BookingService.java
│   │   │               │   └── WalletService.java
│   │   │               └── util/
│   │   │                   └── JwtUtil.java
│   │   └── resources/
│   │       ├── application.properties
│   │       └── data.sql
│   └── test/
│       └── java/
│           └── org/
│               └── example/
│                   └── citizncardproj3/
│                       └── CitiznCardproj3ApplicationTests.java
│
├── .mvn/
│   └── wrapper/
│       └── maven-wrapper.properties
│
├── .gitignore
├── .gitattributes
├── HELP.md
├── mvnw
├── mvnw.cmd
└── pom.xml
```

這個結構包含了以下主要部分：

1. `config/`: 包含配置類，如安全配置。
2. `controller/`: 包含所有的REST控制器。
3. `model/`: 包含所有的實體類。
4. `repository/`: 包含所有的數據訪問接口。
5. `service/`: 包含所有的業務邏輯服務。
6. `util/`: 包含工具類，如JWT工具。
