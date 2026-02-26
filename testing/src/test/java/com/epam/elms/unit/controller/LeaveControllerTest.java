package com.epam.elms.unit.controller;

import com.epam.elms.controller.LeaveController;
import com.epam.elms.dto.LeaveRequestDto;
import com.epam.elms.entity.Employee;
import com.epam.elms.entity.LeaveRequest;
import com.epam.elms.entity.LeaveStatus;
import com.epam.elms.entity.LeaveType;
import com.epam.elms.service.LeaveRequestService;
import com.epam.elms.utils.TestDataFactory;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link LeaveController}.
 * Uses explicit constructor injection for reliable mock wiring.
 */
public class LeaveControllerTest {

    @Mock private LeaveRequestService leaveRequestService;

    private LeaveController leaveController;
    private AutoCloseable   mocks;

    @BeforeMethod
    public void setUp() {
        mocks           = MockitoAnnotations.openMocks(this);
        leaveController = new LeaveController(leaveRequestService);
    }

    @AfterMethod
    public void tearDown() throws Exception {
        mocks.close();
    }

    // ── GET /leaves/employee/{id} ─────────────────────────────────────────────

    @Test(description = "getEmployeeLeaves – returns list from service")
    public void getEmployeeLeaves_returnsListFromService() {
        Employee  employee   = TestDataFactory.buildEmployee(1L, "Alice", "alice@epam.com", "hash");
        LeaveType annualLeave = TestDataFactory.buildAnnualLeaveType();
        LocalDate start = LocalDate.now().plusDays(1);
        LocalDate end   = LocalDate.now().plusDays(3);
        LeaveRequest lr = TestDataFactory.buildPendingLeaveRequest(1L, employee, annualLeave, start, end);

        when(leaveRequestService.getEmployeeLeaves(1L)).thenReturn(List.of(lr));

        List<LeaveRequest> result = leaveController.getEmployeeLeaves(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(1L);
        verify(leaveRequestService).getEmployeeLeaves(1L);
    }

    @Test(description = "getEmployeeLeaves – returns empty list when no leaves found")
    public void getEmployeeLeaves_noLeaves_returnsEmptyList() {
        when(leaveRequestService.getEmployeeLeaves(99L)).thenReturn(List.of());

        List<LeaveRequest> result = leaveController.getEmployeeLeaves(99L);

        assertThat(result).isEmpty();
    }

    // ── GET /leaves/pending ───────────────────────────────────────────────────

    @Test(description = "getPendingLeaves – returns 200 OK with list of DTOs")
    public void getPendingLeaves_returns200WithDtoList() {
        LocalDate start = LocalDate.now().plusDays(1);
        LeaveRequestDto dto = TestDataFactory.buildLeaveRequestDto(1L, 1L, start, start.plusDays(2), "Test");
        when(leaveRequestService.getPendingLeaves()).thenReturn(List.of(dto));

        ResponseEntity<List<LeaveRequestDto>> result = leaveController.getPendingLeaves();

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).hasSize(1);
    }

    @Test(description = "getPendingLeaves – empty list returns 200 with empty body")
    public void getPendingLeaves_empty_returns200EmptyList() {
        when(leaveRequestService.getPendingLeaves()).thenReturn(List.of());

        ResponseEntity<List<LeaveRequestDto>> result = leaveController.getPendingLeaves();

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isEmpty();
    }

    // ── POST /leaves/apply ────────────────────────────────────────────────────

    @Test(description = "applyLeave – valid DTO returns 200 OK with confirmation message")
    public void applyLeave_validDto_returns200WithMessage() {
        Employee  employee   = TestDataFactory.buildEmployee(1L, "Alice", "alice@epam.com", "hash");
        LeaveType annualLeave = TestDataFactory.buildAnnualLeaveType();
        LeaveRequest saved = TestDataFactory.buildPendingLeaveRequest(
                10L, employee, annualLeave,
                LocalDate.now().plusDays(1), LocalDate.now().plusDays(3));
        when(leaveRequestService.applyLeave(any(LeaveRequestDto.class))).thenReturn(saved);

        LeaveRequestDto dto = TestDataFactory.buildLeaveRequestDto(
                1L, 1L, LocalDate.now().plusDays(1), LocalDate.now().plusDays(3), "Holiday");

        ResponseEntity<?> result = leaveController.applyLeave(dto);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody().toString()).containsIgnoringCase("submitted");
        verify(leaveRequestService).applyLeave(any(LeaveRequestDto.class));
    }

    @Test(description = "applyLeave – service exception propagates to caller")
    public void applyLeave_serviceThrows_propagatesException() {
        when(leaveRequestService.applyLeave(any()))
                .thenThrow(new IllegalArgumentException("Insufficient balance"));

        assertThatThrownBy(() ->
            leaveController.applyLeave(TestDataFactory.buildLeaveRequestDto(
                    1L, 1L, LocalDate.now().plusDays(1), LocalDate.now().plusDays(2), "Test")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Insufficient balance");
    }

    // ── PUT /leaves/{id}/approve ──────────────────────────────────────────────

    @Test(description = "approveLeave – valid ID returns 200 OK with approval message")
    public void approveLeave_validId_returns200WithMessage() {
        Employee  employee   = TestDataFactory.buildEmployee(1L, "Alice", "alice@epam.com", "hash");
        LeaveType annualLeave = TestDataFactory.buildAnnualLeaveType();
        LeaveRequest approved = TestDataFactory.buildApprovedLeaveRequest(1L, employee, annualLeave);
        when(leaveRequestService.approveLeave(1L)).thenReturn(approved);

        ResponseEntity<?> result = leaveController.approveLeave(1L);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody().toString()).containsIgnoringCase("approved");
        verify(leaveRequestService).approveLeave(1L);
    }

    @Test(description = "approveLeave – service exception propagates to caller")
    public void approveLeave_serviceThrows_propagatesException() {
        when(leaveRequestService.approveLeave(999L))
                .thenThrow(new RuntimeException("Leave request not found"));

        assertThatThrownBy(() -> leaveController.approveLeave(999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Leave request not found");
    }

    // ── PUT /leaves/{id}/reject ───────────────────────────────────────────────

    @Test(description = "rejectLeave – valid ID returns 200 OK with rejection message")
    public void rejectLeave_validId_returns200WithMessage() {
        Employee  employee   = TestDataFactory.buildEmployee(1L, "Alice", "alice@epam.com", "hash");
        LeaveType annualLeave = TestDataFactory.buildAnnualLeaveType();
        LeaveRequest rejected = TestDataFactory.buildPendingLeaveRequest(
                2L, employee, annualLeave,
                LocalDate.now().plusDays(1), LocalDate.now().plusDays(2));
        rejected.setStatus(LeaveStatus.REJECTED);
        when(leaveRequestService.rejectLeave(2L)).thenReturn(rejected);

        ResponseEntity<?> result = leaveController.rejectLeave(2L);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody().toString()).containsIgnoringCase("rejected");
        verify(leaveRequestService).rejectLeave(2L);
    }

    @Test(description = "rejectLeave – service exception propagates to caller")
    public void rejectLeave_serviceThrows_propagatesException() {
        when(leaveRequestService.rejectLeave(999L))
                .thenThrow(new RuntimeException("Leave request not found"));

        assertThatThrownBy(() -> leaveController.rejectLeave(999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Leave request not found");
    }
}
