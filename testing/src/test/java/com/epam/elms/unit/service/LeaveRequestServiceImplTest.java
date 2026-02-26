package com.epam.elms.unit.service;

import com.epam.elms.dto.LeaveRequestDto;
import com.epam.elms.entity.*;
import com.epam.elms.repository.*;
import com.epam.elms.service.impl.LeaveRequestServiceImpl;
import com.epam.elms.utils.TestDataFactory;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link LeaveRequestServiceImpl}.
 * Uses explicit constructor injection for reliable mock wiring.
 */
public class LeaveRequestServiceImplTest {

    @Mock private LeaveRequestRepository  leaveRequestRepository;
    @Mock private LeaveTypeRepository     leaveTypeRepository;
    @Mock private EmployeeRepository      employeeRepository;
    @Mock private LeaveBalanceRepository  leaveBalanceRepository;

    private LeaveRequestServiceImpl leaveRequestService;
    private AutoCloseable           mocks;

    @BeforeMethod
    public void setUp() {
        mocks = MockitoAnnotations.openMocks(this);
        leaveRequestService = new LeaveRequestServiceImpl(
                leaveRequestRepository, leaveTypeRepository,
                employeeRepository, leaveBalanceRepository);
    }

    @AfterMethod
    public void tearDown() throws Exception { mocks.close(); }

    // ── getEmployeeLeaves ─────────────────────────────────────────────────────

    @Test(description = "getEmployeeLeaves – returns all leave requests for the given employee")
    public void getEmployeeLeaves_existingEmployee_returnsList() {
        Employee  employee   = TestDataFactory.buildEmployee(1L, "Alice", "alice@epam.com", "hash");
        LeaveType leaveType  = TestDataFactory.buildAnnualLeaveType();
        LocalDate start = LocalDate.now().plusDays(1);
        LocalDate end   = LocalDate.now().plusDays(3);
        LeaveRequest lr = TestDataFactory.buildPendingLeaveRequest(1L, employee, leaveType, start, end);

        when(leaveRequestRepository.findByEmployeeId(1L)).thenReturn(List.of(lr));

        List<LeaveRequest> result = leaveRequestService.getEmployeeLeaves(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getEmployee().getId()).isEqualTo(1L);
    }

    @Test(description = "getEmployeeLeaves – returns empty list when employee has no requests")
    public void getEmployeeLeaves_noRequests_returnsEmptyList() {
        when(leaveRequestRepository.findByEmployeeId(99L)).thenReturn(List.of());

        List<LeaveRequest> result = leaveRequestService.getEmployeeLeaves(99L);

        assertThat(result).isEmpty();
    }

    // ── getPendingLeaves ──────────────────────────────────────────────────────

    @Test(description = "getPendingLeaves – maps pending LeaveRequests to DTOs")
    public void getPendingLeaves_withPendingRequests_returnsDtoList() {
        Employee  employee   = TestDataFactory.buildEmployee(1L, "Alice", "alice@epam.com", "hash");
        LeaveType leaveType  = TestDataFactory.buildAnnualLeaveType();
        LocalDate start = LocalDate.now().plusDays(1);
        LocalDate end   = LocalDate.now().plusDays(2);
        LeaveRequest lr = TestDataFactory.buildPendingLeaveRequest(5L, employee, leaveType, start, end);

        when(leaveRequestRepository.findByStatus(LeaveStatus.PENDING)).thenReturn(List.of(lr));

        List<LeaveRequestDto> dtos = leaveRequestService.getPendingLeaves();

        assertThat(dtos).hasSize(1);
        assertThat(dtos.get(0).getEmployeeId()).isEqualTo(1L);
        assertThat(dtos.get(0).getLeaveTypeId()).isEqualTo(1L);
    }

    @Test(description = "getPendingLeaves – empty when there are no pending requests")
    public void getPendingLeaves_noPendingRequests_returnsEmptyList() {
        when(leaveRequestRepository.findByStatus(LeaveStatus.PENDING)).thenReturn(List.of());

        assertThat(leaveRequestService.getPendingLeaves()).isEmpty();
    }

    // ── applyLeave ────────────────────────────────────────────────────────────

    @Test(description = "applyLeave – persists a PENDING leave request when balance is sufficient")
    public void applyLeave_validRequest_savesLeaveRequest() {
        Employee  employee  = TestDataFactory.buildEmployee(1L, "Alice", "alice@epam.com", "hash");
        LeaveType leaveType = TestDataFactory.buildAnnualLeaveType();
        LeaveBalance balance = TestDataFactory.buildLeaveBalance(1L, employee, leaveType, 20.0);

        LocalDate start = LocalDate.now().plusDays(1);
        LocalDate end   = LocalDate.now().plusDays(3); // 3 days

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(leaveTypeRepository.findById(1L)).thenReturn(Optional.of(leaveType));
        when(leaveBalanceRepository.findByEmployeeIdAndLeaveTypeId(1L, 1L))
                .thenReturn(Optional.of(balance));
        when(leaveRequestRepository.save(any(LeaveRequest.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        LeaveRequestDto dto = TestDataFactory.buildLeaveRequestDto(1L, 1L, start, end, "Vacation");

        LeaveRequest saved = leaveRequestService.applyLeave(dto);

        assertThat(saved.getStatus()).isEqualTo(LeaveStatus.PENDING);
        verify(leaveRequestRepository).save(any(LeaveRequest.class));
    }

    @Test(description = "applyLeave – throws when employee is not found")
    public void applyLeave_unknownEmployee_throwsException() {
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        LeaveRequestDto dto = TestDataFactory.buildLeaveRequestDto(999L, 1L,
                LocalDate.now().plusDays(1), LocalDate.now().plusDays(2), "Test");

        assertThatThrownBy(() -> leaveRequestService.applyLeave(dto))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Employee not found");
    }

    @Test(description = "applyLeave – throws when leave type is not found")
    public void applyLeave_unknownLeaveType_throwsException() {
        Employee employee = TestDataFactory.buildEmployee(1L, "Alice", "alice@epam.com", "hash");
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(leaveTypeRepository.findById(999L)).thenReturn(Optional.empty());

        LeaveRequestDto dto = TestDataFactory.buildLeaveRequestDto(1L, 999L,
                LocalDate.now().plusDays(1), LocalDate.now().plusDays(2), "Test");

        assertThatThrownBy(() -> leaveRequestService.applyLeave(dto))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Leave type not found");
    }

    @Test(description = "applyLeave – throws when requested days exceed available balance")
    public void applyLeave_insufficientBalance_throwsIllegalArgumentException() {
        Employee  employee  = TestDataFactory.buildEmployee(1L, "Alice", "alice@epam.com", "hash");
        LeaveType leaveType = TestDataFactory.buildAnnualLeaveType();
        // Only 1 day available
        LeaveBalance thinBalance = TestDataFactory.buildLeaveBalance(1L, employee, leaveType, 1.0);

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(leaveTypeRepository.findById(1L)).thenReturn(Optional.of(leaveType));
        when(leaveBalanceRepository.findByEmployeeIdAndLeaveTypeId(1L, 1L))
                .thenReturn(Optional.of(thinBalance));

        // Requesting 5 days but balance is only 1
        LeaveRequestDto dto = TestDataFactory.buildLeaveRequestDto(1L, 1L,
                LocalDate.now().plusDays(1), LocalDate.now().plusDays(5), "Long trip");

        assertThatThrownBy(() -> leaveRequestService.applyLeave(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Leave request exceeds balance");
    }

    @Test(description = "applyLeave – throws for past start date")
    public void applyLeave_pastStartDate_throwsIllegalArgumentException() {
        Employee  employee  = TestDataFactory.buildEmployee(1L, "Alice", "alice@epam.com", "hash");
        LeaveType leaveType = TestDataFactory.buildAnnualLeaveType();
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(leaveTypeRepository.findById(1L)).thenReturn(Optional.of(leaveType));

        LeaveRequestDto dto = TestDataFactory.buildLeaveRequestDto(1L, 1L,
                LocalDate.now().minusDays(2), LocalDate.now().minusDays(1), "Past leave");

        assertThatThrownBy(() -> leaveRequestService.applyLeave(dto))
                .isInstanceOf(IllegalArgumentException.class);
    }

    // ── approveLeave ──────────────────────────────────────────────────────────

    @Test(description = "approveLeave – sets status to APPROVED and deducts balance")
    public void approveLeave_pendingRequest_approvesAndDeductsBalance() {
        Employee  employee  = TestDataFactory.buildEmployee(1L, "Alice", "alice@epam.com", "hash");
        LeaveType leaveType = TestDataFactory.buildAnnualLeaveType();
        LeaveBalance balance = TestDataFactory.buildLeaveBalance(1L, employee, leaveType, 20.0);

        LocalDate start = LocalDate.now().plusDays(1);
        LocalDate end   = LocalDate.now().plusDays(3); // 3 days
        LeaveRequest lr = TestDataFactory.buildPendingLeaveRequest(1L, employee, leaveType, start, end);

        when(leaveRequestRepository.findById(1L)).thenReturn(Optional.of(lr));
        when(leaveBalanceRepository.findByEmployeeIdAndLeaveTypeId(1L, 1L))
                .thenReturn(Optional.of(balance));
        when(leaveRequestRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(leaveBalanceRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        LeaveRequest result = leaveRequestService.approveLeave(1L);

        assertThat(result.getStatus()).isEqualTo(LeaveStatus.APPROVED);
        assertThat(balance.getBalance()).isLessThan(20.0); // Balance was deducted
    }

    @Test(description = "approveLeave – throws when request is already approved")
    public void approveLeave_alreadyApprovedRequest_throwsException() {
        Employee  employee  = TestDataFactory.buildEmployee(1L, "Alice", "alice@epam.com", "hash");
        LeaveType leaveType = TestDataFactory.buildAnnualLeaveType();
        LeaveRequest lr = TestDataFactory.buildApprovedLeaveRequest(2L, employee, leaveType);

        when(leaveRequestRepository.findById(2L)).thenReturn(Optional.of(lr));

        assertThatThrownBy(() -> leaveRequestService.approveLeave(2L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not pending");
    }

    @Test(description = "approveLeave – throws when leave request not found")
    public void approveLeave_notFound_throwsException() {
        when(leaveRequestRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> leaveRequestService.approveLeave(999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Leave request not found");
    }

    // ── rejectLeave ───────────────────────────────────────────────────────────

    @Test(description = "rejectLeave – sets status to REJECTED for a pending request")
    public void rejectLeave_pendingRequest_setsStatusRejected() {
        Employee  employee  = TestDataFactory.buildEmployee(1L, "Alice", "alice@epam.com", "hash");
        LeaveType leaveType = TestDataFactory.buildAnnualLeaveType();
        LocalDate start = LocalDate.now().plusDays(1);
        LocalDate end   = LocalDate.now().plusDays(2);
        LeaveRequest lr = TestDataFactory.buildPendingLeaveRequest(3L, employee, leaveType, start, end);

        when(leaveRequestRepository.findById(3L)).thenReturn(Optional.of(lr));
        when(leaveRequestRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        LeaveRequest result = leaveRequestService.rejectLeave(3L);

        assertThat(result.getStatus()).isEqualTo(LeaveStatus.REJECTED);
    }

    @Test(description = "rejectLeave – throws when request is already processed")
    public void rejectLeave_alreadyRejected_throwsException() {
        Employee  employee  = TestDataFactory.buildEmployee(1L, "Alice", "alice@epam.com", "hash");
        LeaveType leaveType = TestDataFactory.buildAnnualLeaveType();
        // Build a rejected leave request
        LocalDate start = LocalDate.now().plusDays(1);
        LocalDate end   = LocalDate.now().plusDays(2);
        LeaveRequest lr = TestDataFactory.buildPendingLeaveRequest(4L, employee, leaveType, start, end);
        lr.setStatus(LeaveStatus.REJECTED);

        when(leaveRequestRepository.findById(4L)).thenReturn(Optional.of(lr));

        assertThatThrownBy(() -> leaveRequestService.rejectLeave(4L))
                .isInstanceOf(IllegalArgumentException.class);
    }

    // ── DataProvider-driven tests ─────────────────────────────────────────────

    @DataProvider(name = "leaveDays")
    public Object[][] leaveDaysProvider() {
        return new Object[][] {
            { 1 },
            { 5 },
        };
    }

    @Test(dataProvider = "leaveDays",
          description = "applyLeave – succeeds for various valid leave durations when balance permits")
    public void applyLeave_variousDurations_succeeds(int daysAhead) {
        Employee  employee  = TestDataFactory.buildEmployee(1L, "Alice", "alice@epam.com", "hash");
        LeaveType leaveType = TestDataFactory.buildAnnualLeaveType();
        // 20 days always sufficient
        LeaveBalance balance = TestDataFactory.buildLeaveBalance(1L, employee, leaveType, 20.0);

        LocalDate start = LocalDate.now().plusDays(1);
        LocalDate end   = start.plusDays(daysAhead - 1);

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(leaveTypeRepository.findById(1L)).thenReturn(Optional.of(leaveType));
        when(leaveBalanceRepository.findByEmployeeIdAndLeaveTypeId(1L, 1L))
                .thenReturn(Optional.of(balance));
        when(leaveRequestRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        LeaveRequestDto dto = TestDataFactory.buildLeaveRequestDto(1L, 1L, start, end, "Leave");

        assertThatCode(() -> leaveRequestService.applyLeave(dto))
                .doesNotThrowAnyException();
    }
}
