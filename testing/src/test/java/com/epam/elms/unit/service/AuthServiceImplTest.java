package com.epam.elms.unit.service;

import com.epam.elms.dto.LoginRequest;
import com.epam.elms.dto.LoginResponse;
import com.epam.elms.dto.RegisterRequest;
import com.epam.elms.entity.Employee;
import com.epam.elms.entity.LeaveBalance;
import com.epam.elms.entity.LeaveType;
import com.epam.elms.repository.EmployeeRepository;
import com.epam.elms.repository.LeaveBalanceRepository;
import com.epam.elms.repository.LeaveTypeRepository;
import com.epam.elms.service.impl.AuthServiceImpl;
import com.epam.elms.service.impl.JwtUtil;
import com.epam.elms.utils.TestDataFactory;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link AuthServiceImpl}.
 * Uses explicit constructor injection to guarantee mock wiring.
 */
public class AuthServiceImplTest {

    @Mock private EmployeeRepository     employeeRepository;
    @Mock private PasswordEncoder        passwordEncoder;
    @Mock private JwtUtil                jwtUtil;
    @Mock private LeaveTypeRepository    leaveTypeRepository;
    @Mock private LeaveBalanceRepository leaveBalanceRepository;

    private AuthServiceImpl authService;
    private AutoCloseable   mocks;

    @BeforeMethod
    public void openMocks() {
        mocks = MockitoAnnotations.openMocks(this);
        // Explicit constructor – matches AuthServiceImpl(repo, encoder, jwt, leaveTypeRepo, leaveBalanceRepo)
        authService = new AuthServiceImpl(
                employeeRepository, passwordEncoder, jwtUtil,
                leaveTypeRepository, leaveBalanceRepository);
    }

    @AfterMethod
    public void closeMocks() throws Exception {
        mocks.close();
    }

    // ═══════════════════════════ login ═══════════════════════════════════════

    @Test(description = "login – valid credentials should return a populated LoginResponse")
    public void login_validCredentials_returnsLoginResponse() {
        Employee employee = TestDataFactory.buildEmployee(1L, "Alice", "alice@epam.com",
                "$2a$10$encodedPassword");

        when(employeeRepository.findByEmail("alice@epam.com"))
                .thenReturn(Optional.of(employee));
        when(passwordEncoder.matches("secret", "$2a$10$encodedPassword"))
                .thenReturn(true);
        when(jwtUtil.generateToken(employee)).thenReturn("jwt.token.value");

        LoginResponse response = authService.login(
                TestDataFactory.buildLoginRequest("alice@epam.com", "secret"));

        assertThat(response).isNotNull();
        assertThat(response.getJwt()).isEqualTo("jwt.token.value");
        assertThat(response.getEmail()).isEqualTo("alice@epam.com");
        assertThat(response.getName()).isEqualTo("Alice");
        assertThat(response.getEmployeeId()).isEqualTo(1L);
    }

    @Test(description = "login – unknown email should throw RuntimeException")
    public void login_unknownEmail_throwsException() {
        when(employeeRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(
                        TestDataFactory.buildLoginRequest("ghost@epam.com", "pwd")))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Invalid email or password");
    }

    @Test(description = "login – wrong password should throw RuntimeException")
    public void login_wrongPassword_throwsException() {
        Employee employee = TestDataFactory.buildEmployee(1L, "Alice", "alice@epam.com",
                "$2a$10$encodedPassword");

        when(employeeRepository.findByEmail("alice@epam.com"))
                .thenReturn(Optional.of(employee));
        when(passwordEncoder.matches("wrong", "$2a$10$encodedPassword"))
                .thenReturn(false);

        assertThatThrownBy(() -> authService.login(
                        TestDataFactory.buildLoginRequest("alice@epam.com", "wrong")))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Invalid email or password");
    }

    @Test(description = "login – role is propagated to the LoginResponse")
    public void login_employeeRole_isReturnedInResponse() {
        Set<String> roles = new HashSet<>();
        roles.add("MANAGER");
        Employee emp = Employee.builder()
                .id(2L).name("Bob").email("bob@epam.com")
                .password("hash").roles(roles)
                .build();

        when(employeeRepository.findByEmail("bob@epam.com")).thenReturn(Optional.of(emp));
        when(passwordEncoder.matches("pass", "hash")).thenReturn(true);
        when(jwtUtil.generateToken(emp)).thenReturn("token");

        LoginResponse resp = authService.login(new LoginRequest("bob@epam.com", "pass"));

        assertThat(resp.getRole()).isEqualTo("MANAGER");
    }

    // ═══════════════════════════ register ════════════════════════════════════

    @Test(description = "register – new employee is saved and leave balances are created")
    public void register_newEmployee_savesAndCreatesBalances() {
        LeaveType annual = TestDataFactory.buildAnnualLeaveType();
        LeaveType sick   = TestDataFactory.buildSickLeaveType();
        Employee  saved  = TestDataFactory.buildEmployee(10L, "Carol", "carol@epam.com", "$2a$10$hash");

        when(passwordEncoder.encode("password123")).thenReturn("$2a$10$hash");
        when(employeeRepository.save(any(Employee.class))).thenReturn(saved);
        when(leaveTypeRepository.findAll()).thenReturn(List.of(annual, sick));
        when(leaveBalanceRepository.save(any(LeaveBalance.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        Employee result = authService.register(
                TestDataFactory.buildRegisterRequest("carol@epam.com", "password123", "Carol", "EMPLOYEE"));

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(10L);
        assertThat(result.getEmail()).isEqualTo("carol@epam.com");
        verify(leaveBalanceRepository, times(2)).save(any(LeaveBalance.class));
    }

    @Test(description = "register – default role is EMPLOYEE when none is specified")
    public void register_noRoleProvided_defaultsToEmployee() {
        Employee saved = TestDataFactory.buildEmployee(11L, "Dave", "dave@epam.com", "hash");

        when(passwordEncoder.encode(anyString())).thenReturn("hash");
        when(employeeRepository.save(any(Employee.class))).thenReturn(saved);
        when(leaveTypeRepository.findAll()).thenReturn(List.of());

        authService.register(new RegisterRequest("dave@epam.com", "pass", "Dave", null));

        verify(employeeRepository).save(argThat(e ->
                e.getRoles() != null && e.getRoles().contains("EMPLOYEE")));
    }

    @Test(description = "register – password is encoded before persisting")
    public void register_passwordIsEncoded() {
        Employee saved = TestDataFactory.buildEmployee(12L, "Eve", "eve@epam.com", "encoded");

        when(passwordEncoder.encode("plaintext")).thenReturn("encoded");
        when(employeeRepository.save(any(Employee.class))).thenReturn(saved);
        when(leaveTypeRepository.findAll()).thenReturn(List.of());

        authService.register(new RegisterRequest("eve@epam.com", "plaintext", "Eve", "EMPLOYEE"));

        verify(passwordEncoder).encode("plaintext");
        verify(employeeRepository).save(argThat(e -> "encoded".equals(e.getPassword())));
    }
}
