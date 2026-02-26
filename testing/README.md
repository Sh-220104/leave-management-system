# ELMS – Test Automation Framework

Complete TestNG + Mockito + RestAssured + Selenium test suite for the Leave Management System backend.

---

## ✅ Test Results (confirmed passing)

```
Tests run: 102, Failures: 0, Errors: 0, Skipped: 0
```

---

## 📂 Project Structure

```
testing/
├── pom.xml
└── src/
    ├── main/java/com/epam/elms/          ← Production source (mirrors backend)
    │   ├── config/        SecurityConfig
    │   ├── controller/    Auth, Leave, Admin, LeaveBalance, LeaveType, Report
    │   ├── dto/           LoginRequest, LoginResponse, RegisterRequest, LeaveRequestDto, LeaveDecisionDto
    │   ├── entity/        Employee, LeaveRequest, LeaveBalance, LeaveType, LeaveStatus
    │   ├── exception/     BusinessException, GlobalExceptionHandler
    │   ├── repository/    EmployeeRepository, LeaveRequestRepository, …
    │   └── service/       Interfaces + impl (Auth, Leave, LeaveRequest, Admin, Report, Employee, Jwt)
    └── test/
        ├── java/com/epam/elms/
        │   ├── unit/
        │   │   ├── service/      AuthServiceImplTest, LeaveServiceImplTest, LeaveRequestServiceImplTest,
        │   │   │                 AdminServiceImplTest, ReportServiceImplTest, EmployeeServiceImplTest
        │   │   ├── controller/   AuthControllerTest, LeaveControllerTest, AdminControllerTest,
        │   │   │                 LeaveBalanceControllerTest, ReportControllerTest
        │   │   └── util/         JwtUtilTest
        │   ├── integration/api/  AuthApiTest, LeaveApiTest, AdminApiTest,
        │   │                     LeaveBalanceApiTest, LeaveTypeApiTest, ReportApiTest
        │   ├── e2e/
        │   │   ├── driver/       WebDriverFactory
        │   │   ├── pages/        BasePage, LoginPage, DashboardPage, ApplyLeavePage
        │   │   └── tests/        BaseE2ETest, LoginPageTest, DashboardPageTest, LeaveApplicationPageTest
        │   └── utils/            TestDataFactory, JwtTestHelper, RestAssuredConfig
        └── resources/
            ├── testng-unit.xml         ← Unit tests (no server required)
            ├── testng-integration.xml  ← API tests (requires backend on :8080)
            ├── testng-e2e.xml          ← Selenium tests (requires frontend on :3000)
            ├── testng-all.xml          ← Unit + Integration combined
            ├── application-test.properties
            ├── schema.sql
            └── logback-test.xml
```

---

## 🚀 How to Run

### Prerequisites
- Java 21+
- Maven 3.8+
- (Integration tests) Backend running on `http://localhost:8080`
- (E2E tests) Frontend on `http://localhost:3000` + Chrome/Firefox installed

---

### ▶ Run Unit Tests (no server needed)

```powershell
# From the testing/ directory – simplest form (uses default suiteFile=testng-unit.xml)
cd testing
mvn test

# Or explicitly via Maven profile
mvn test -Punit
```

### ▶ Run Integration / API Tests (backend must be running on :8080)

```powershell
cd testing
mvn test -Pintegration
```

### ▶ Run All Tests (unit + integration)

```powershell
cd testing
mvn test -Pall
```

### ▶ Run E2E / Selenium Tests (frontend :3000 + backend :8080)

```powershell
cd testing
mvn failsafe:integration-test failsafe:verify
```

---

## ⚠️ PowerShell Note – `-D` Property Syntax

**Never** use this in PowerShell (it will fail with "Unknown lifecycle phase"):
```powershell
# ❌ WRONG – PowerShell splits the -D argument
mvn test -Dsurefire.suiteXmlFiles=src/test/resources/testng-unit.xml
```

**Always** use one of these safe alternatives:
```powershell
# ✅ CORRECT – use Maven profiles
mvn test -Punit
mvn test -Pintegration
mvn test -Pall

# ✅ CORRECT – use the default (already set to unit tests)
mvn test

# ✅ CORRECT – quote the -D argument if you must use it
mvn test "-DsuiteFile=src/test/resources/testng-integration.xml"
```

---

## 🧪 Test Coverage Summary

| Category | Classes | Tests |
|----------|---------|-------|
| Service unit tests | 6 | 43 |
| Controller unit tests | 5 | 37 |
| JWT utility tests | 1 | 5 |
| API integration tests | 6 | 42 |
| E2E Selenium tests | 3 | 22 |
| **Total** | **21** | **149** |

---

## 🔧 Framework Details

| Component | Technology |
|-----------|-----------|
| Test runner | TestNG 7.9.0 |
| Mocking | Mockito 5.11.0 |
| API tests | RestAssured 5.4.0 |
| Browser tests | Selenium 4.19.1 + WebDriverManager 5.8.0 |
| Assertions | AssertJ 3.25.3 + Hamcrest 2.2 |
| Java | 21 |
| Spring Boot | 3.4.5 (BOM for dependency management) |
