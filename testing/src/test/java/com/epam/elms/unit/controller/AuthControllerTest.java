package com.epam.elms.unit.controller;

import com.epam.elms.controller.AuthController;
import com.epam.elms.dto.LoginRequest;
import com.epam.elms.dto.LoginResponse;
import com.epam.elms.dto.RegisterRequest;
import com.epam.elms.entity.Employee;
import com.epam.elms.service.AuthService;
import com.epam.elms.utils.TestDataFactory;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link AuthController}.
 * AuthController uses @Autowired field injection, so we use ReflectionTestUtils to inject the mock.
 */
public class AuthControllerTest {

    @Mock private AuthService authService;

    private AuthController authController;
    private AutoCloseable  mocks;

    @BeforeMethod
    public void setUp() {
        mocks          = MockitoAnnotations.openMocks(this);
        authController = new AuthController();
        // AuthController uses @Autowired field injection - inject mock via reflection
        ReflectionTestUtils.setField(authController, "authService", authService);
    }

    @AfterMethod
    public void tearDown() throws Exception {
        mocks.close();
    }

    // ── POST /auth/login ──────────────────────────────────────────────────────

    @Test(description = "login – valid request returns 200 OK with LoginResponse body")
    public void login_validRequest_returns200WithBody() {
        LoginResponse response = LoginResponse.builder()
                .employeeId(1L).jwt("token").name("Alice")
                .email("alice@epam.com").role("EMPLOYEE")
                .build();

        when(authService.login(any(LoginRequest.class))).thenReturn(response);

        ResponseEntity<LoginResponse> result =
                authController.login(TestDataFactory.buildLoginRequest("alice@epam.com", "secret"));

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getJwt()).isEqualTo("token");
        assertThat(result.getBody().getEmail()).isEqualTo("alice@epam.com");
    }

    @Test(description = "login – service exception propagates to caller")
    public void login_serviceThrows_propagatesException() {
        when(authService.login(any())).thenThrow(new RuntimeException("Invalid credentials"));

        assertThatThrownBy(() ->
            authController.login(new LoginRequest("bad@epam.com", "wrong")))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Invalid credentials");
    }

    @Test(description = "login – delegates to AuthService exactly once")
    public void login_delegatesToServiceOnce() {
        when(authService.login(any())).thenReturn(LoginResponse.builder()
                .jwt("t").email("e@epam.com").name("E").employeeId(1L).role("EMPLOYEE").build());

        authController.login(new LoginRequest("e@epam.com", "pass"));

        verify(authService, times(1)).login(any(LoginRequest.class));
    }

    // ── POST /auth/logout ─────────────────────────────────────────────────────

    @Test(description = "logout – always returns 200 OK with empty body")
    public void logout_always_returns200() {
        ResponseEntity<?> result = authController.logout();

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNull();
    }

    @Test(description = "logout – does not call any service method (stateless)")
    public void logout_stateless_noServiceCallMade() {
        authController.logout();

        verifyNoInteractions(authService);
    }

    // ── POST /auth/register ───────────────────────────────────────────────────

    @Test(description = "register – valid request returns 200 OK with created Employee")
    public void register_validRequest_returns200WithEmployee() {
        Employee saved = TestDataFactory.buildEmployee(5L, "Carol", "carol@epam.com", "hash");
        when(authService.register(any(RegisterRequest.class))).thenReturn(saved);

        RegisterRequest req = TestDataFactory.buildRegisterRequest(
                "carol@epam.com", "password", "Carol", "EMPLOYEE");

        ResponseEntity<Employee> result = authController.register(req);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getId()).isEqualTo(5L);
        assertThat(result.getBody().getEmail()).isEqualTo("carol@epam.com");
    }

    @Test(description = "register – service exception propagates to caller")
    public void register_serviceThrows_propagatesException() {
        when(authService.register(any())).thenThrow(new RuntimeException("Email already exists"));

        assertThatThrownBy(() ->
            authController.register(new RegisterRequest("dup@epam.com", "pass", "Dup", "EMPLOYEE")))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Email already exists");
    }

    @Test(description = "register – delegates to AuthService exactly once")
    public void register_delegatesToServiceOnce() {
        when(authService.register(any()))
                .thenReturn(TestDataFactory.buildEmployee(1L, "Test", "test@epam.com", "hash"));

        authController.register(new RegisterRequest("test@epam.com", "pass", "Test", "EMPLOYEE"));

        verify(authService, times(1)).register(any(RegisterRequest.class));
    }

    @Test(description = "register – returned Employee contains the persisted ID")
    public void register_responseBody_containsAssignedId() {
        Employee emp = TestDataFactory.buildEmployee(99L, "Zoe", "zoe@epam.com", "hash");
        when(authService.register(any())).thenReturn(emp);

        ResponseEntity<Employee> result = authController.register(
                new RegisterRequest("zoe@epam.com", "pass", "Zoe", "EMPLOYEE"));

        assertThat(result.getBody().getId()).isEqualTo(99L);
    }
}
