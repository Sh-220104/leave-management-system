package com.epam.elms.utils;

import com.epam.elms.dto.LeaveRequestDto;
import com.epam.elms.dto.LoginRequest;
import com.epam.elms.dto.RegisterRequest;
import com.epam.elms.entity.Employee;
import com.epam.elms.entity.LeaveBalance;
import com.epam.elms.entity.LeaveRequest;
import com.epam.elms.entity.LeaveStatus;
import com.epam.elms.entity.LeaveType;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

/**
 * Central factory for creating test fixtures.
 * All factory methods return NEW instances so tests stay isolated.
 * Uses mutable sets so tests that call Set.add() do not throw UnsupportedOperationException.
 */
public final class TestDataFactory {

    private TestDataFactory() { /* utility class */ }

    // ── Employee ─────────────────────────────────────────────────────────────

    public static Employee buildEmployee(Long id, String name, String email, String encodedPwd) {
        Set<String> roles = new HashSet<>();
        roles.add("ROLE_EMPLOYEE");
        return Employee.builder()
                .id(id)
                .name(name)
                .email(email)
                .password(encodedPwd)
                .roles(roles)
                .build();
    }

    public static Employee buildAdminEmployee(Long id, String email) {
        Set<String> roles = new HashSet<>();
        roles.add("ROLE_ADMIN");
        return Employee.builder()
                .id(id)
                .name("Admin User")
                .email(email)
                .password("$2a$10$dummy_hash")
                .roles(roles)
                .build();
    }

    // ── LeaveType ─────────────────────────────────────────────────────────────

    public static LeaveType buildLeaveType(Long id, String type, String description) {
        return LeaveType.builder()
                .id(id)
                .type(type)
                .description(description)
                .build();
    }

    public static LeaveType buildAnnualLeaveType() {
        return buildLeaveType(1L, "Annual Leave", "Annual paid leave");
    }

    public static LeaveType buildSickLeaveType() {
        return buildLeaveType(2L, "Sick Leave", "Paid sick leave");
    }

    // ── LeaveBalance ──────────────────────────────────────────────────────────

    public static LeaveBalance buildLeaveBalance(Long id, Employee employee,
                                                  LeaveType leaveType, double balance) {
        return LeaveBalance.builder()
                .id(id)
                .employee(employee)
                .leaveType(leaveType)
                .balance(balance)
                .build();
    }

    // ── LeaveRequest ──────────────────────────────────────────────────────────

    public static LeaveRequest buildPendingLeaveRequest(Long id, Employee employee,
                                                         LeaveType leaveType,
                                                         LocalDate startDate,
                                                         LocalDate endDate) {
        return LeaveRequest.builder()
                .id(id)
                .employee(employee)
                .leaveType(leaveType)
                .startDate(startDate)
                .endDate(endDate)
                .status(LeaveStatus.PENDING)
                .notes("Test leave request")
                .createdOn(LocalDate.now())
                .build();
    }

    public static LeaveRequest buildApprovedLeaveRequest(Long id, Employee employee,
                                                          LeaveType leaveType) {
        return LeaveRequest.builder()
                .id(id)
                .employee(employee)
                .leaveType(leaveType)
                .startDate(LocalDate.now().plusDays(1))
                .endDate(LocalDate.now().plusDays(3))
                .status(LeaveStatus.APPROVED)
                .notes("Already approved")
                .createdOn(LocalDate.now())
                .decisionOn(LocalDate.now())
                .build();
    }

    // ── DTOs ──────────────────────────────────────────────────────────────────

    public static LeaveRequestDto buildLeaveRequestDto(Long employeeId, Long leaveTypeId,
                                                        LocalDate startDate, LocalDate endDate,
                                                        String reason) {
        LeaveRequestDto dto = new LeaveRequestDto();
        dto.setEmployeeId(employeeId);
        dto.setLeaveTypeId(leaveTypeId);
        dto.setStartDate(startDate);
        dto.setEndDate(endDate);
        dto.setReason(reason);
        return dto;
    }

    public static LoginRequest buildLoginRequest(String email, String password) {
        return new LoginRequest(email, password);
    }

    public static RegisterRequest buildRegisterRequest(String email, String password,
                                                        String name, String role) {
        return new RegisterRequest(email, password, name, role);
    }
}
