package com.epam.elms.unit.service;

import com.epam.elms.dto.LeaveRequestDto;
import com.epam.elms.entity.*;
import com.epam.elms.exception.BusinessException;
import com.epam.elms.repository.*;
import com.epam.elms.service.impl.LeaveServiceImpl;
import com.epam.elms.utils.TestDataFactory;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link LeaveServiceImpl}.
 * Uses explicit constructor injection for reliable mock wiring.
 */
public class LeaveServiceImplTest {

    @Mock private LeaveRequestRepository  leaveRequestRepository;
    @Mock private EmployeeRepository      employeeRepository;
    @Mock private LeaveTypeRepository     leaveTypeRepository;
    @Mock private LeaveBalanceRepository  leaveBalanceRepository;

    private LeaveServiceImpl leaveService;
    private AutoCloseable    mocks;

    @BeforeMethod
    public void setUp() {
        mocks        = MockitoAnnotations.openMocks(this);
        leaveService = new LeaveServiceImpl(
                leaveRequestRepository, employeeRepository,
                leaveTypeRepository, leaveBalanceRepository);
    }

    @AfterMethod
    public void tearDown() throws Exception { mocks.close(); }

    // ── applyLeaveForUser ─────────────────────────────────────────────────────

    @Test(description = "applyLeaveForUser – valid input saves a PENDING request")
    public void applyLeaveForUser_valid_savesPendingRequest() {
        Employee  employee   = TestDataFactory.buildEmployee(1L, "Alice", "alice@epam.com", "hash");
        LeaveType leaveType  = TestDataFactory.buildAnnualLeaveType();
        LeaveBalance balance = TestDataFactory.buildLeaveBalance(1L, employee, leaveType, 20.0);

        when(employeeRepository.findByEmail("alice@epam.com")).thenReturn(Optional.of(employee));
        when(leaveTypeRepository.findById(1L)).thenReturn(Optional.of(leaveType));
        when(leaveBalanceRepository.findByEmployeeIdAndLeaveTypeId(1L, 1L))
                .thenReturn(Optional.of(balance));
        when(leaveRequestRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        LeaveRequestDto dto = TestDataFactory.buildLeaveRequestDto(1L, 1L,
                LocalDate.now().plusDays(1), LocalDate.now().plusDays(5), "Holiday");

        assertThatCode(() -> leaveService.applyLeaveForUser("alice@epam.com", dto))
                .doesNotThrowAnyException();

        verify(leaveRequestRepository).save(argThat(lr -> lr.getStatus() == LeaveStatus.PENDING));
    }

    @Test(description = "applyLeaveForUser – unknown email throws BusinessException")
    public void applyLeaveForUser_unknownEmail_throwsBusinessException() {
        when(employeeRepository.findByEmail("ghost@epam.com")).thenReturn(Optional.empty());

        LeaveRequestDto dto = TestDataFactory.buildLeaveRequestDto(1L, 1L,
                LocalDate.now().plusDays(1), LocalDate.now().plusDays(3), "Test");

        assertThatThrownBy(() -> leaveService.applyLeaveForUser("ghost@epam.com", dto))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Employee not found");
    }

    @Test(description = "applyLeaveForUser – unknown leave type throws BusinessException")
    public void applyLeaveForUser_unknownLeaveType_throwsBusinessException() {
        Employee employee = TestDataFactory.buildEmployee(1L, "Alice", "alice@epam.com", "hash");
        when(employeeRepository.findByEmail("alice@epam.com")).thenReturn(Optional.of(employee));
        when(leaveTypeRepository.findById(999L)).thenReturn(Optional.empty());

        LeaveRequestDto dto = TestDataFactory.buildLeaveRequestDto(1L, 999L,
                LocalDate.now().plusDays(1), LocalDate.now().plusDays(2), "Test");

        assertThatThrownBy(() -> leaveService.applyLeaveForUser("alice@epam.com", dto))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Leave type not found");
    }

    @Test(description = "applyLeaveForUser – past start date throws BusinessException")
    public void applyLeaveForUser_pastDate_throwsBusinessException() {
        Employee employee = TestDataFactory.buildEmployee(1L, "Alice", "alice@epam.com", "hash");
        LeaveType leaveType = TestDataFactory.buildAnnualLeaveType();
        when(employeeRepository.findByEmail("alice@epam.com")).thenReturn(Optional.of(employee));
        when(leaveTypeRepository.findById(1L)).thenReturn(Optional.of(leaveType));

        LeaveRequestDto dto = TestDataFactory.buildLeaveRequestDto(1L, 1L,
                LocalDate.now().minusDays(1), LocalDate.now().plusDays(1), "Past");

        assertThatThrownBy(() -> leaveService.applyLeaveForUser("alice@epam.com", dto))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Invalid leave dates");
    }

    @Test(description = "applyLeaveForUser – insufficient balance throws BusinessException")
    public void applyLeaveForUser_insufficientBalance_throwsBusinessException() {
        Employee  employee   = TestDataFactory.buildEmployee(1L, "Alice", "alice@epam.com", "hash");
        LeaveType leaveType  = TestDataFactory.buildAnnualLeaveType();
        // Only 2 days available
        LeaveBalance thinBalance = TestDataFactory.buildLeaveBalance(1L, employee, leaveType, 2.0);

        when(employeeRepository.findByEmail("alice@epam.com")).thenReturn(Optional.of(employee));
        when(leaveTypeRepository.findById(1L)).thenReturn(Optional.of(leaveType));
        when(leaveBalanceRepository.findByEmployeeIdAndLeaveTypeId(1L, 1L))
                .thenReturn(Optional.of(thinBalance));

        // Requesting 10 days but only 2 available
        LeaveRequestDto dto = TestDataFactory.buildLeaveRequestDto(1L, 1L,
                LocalDate.now().plusDays(1), LocalDate.now().plusDays(10), "Long trip");

        assertThatThrownBy(() -> leaveService.applyLeaveForUser("alice@epam.com", dto))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Not enough leave balance");
    }

    // ── approveLeave ──────────────────────────────────────────────────────────

    @Test(description = "approveLeave – sets APPROVED and deducts balance correctly")
    public void approveLeave_pendingRequest_approvesAndDeductsBalance() {
        Employee  employee   = TestDataFactory.buildEmployee(1L, "Alice", "alice@epam.com", "hash");
        LeaveType leaveType  = TestDataFactory.buildAnnualLeaveType();
        LeaveBalance balance = TestDataFactory.buildLeaveBalance(1L, employee, leaveType, 20.0);

        LocalDate start = LocalDate.now().plusDays(1);
        LocalDate end   = LocalDate.now().plusDays(5); // 5 days
        LeaveRequest lr = TestDataFactory.buildPendingLeaveRequest(10L, employee, leaveType, start, end);

        when(leaveRequestRepository.findById(10L)).thenReturn(Optional.of(lr));
        when(leaveBalanceRepository.findByEmployeeIdAndLeaveTypeId(1L, 1L))
                .thenReturn(Optional.of(balance));
        when(leaveBalanceRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(leaveRequestRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        leaveService.approveLeave(10L, "Approved by manager");

        assertThat(lr.getStatus()).isEqualTo(LeaveStatus.APPROVED);
        assertThat(lr.getManagerComment()).isEqualTo("Approved by manager");
        assertThat(balance.getBalance()).isEqualTo(15.0); // 20 - 5
    }

    @Test(description = "approveLeave – throws BusinessException when request already processed")
    public void approveLeave_alreadyProcessed_throwsBusinessException() {
        Employee  employee  = TestDataFactory.buildEmployee(1L, "Alice", "alice@epam.com", "hash");
        LeaveType leaveType = TestDataFactory.buildAnnualLeaveType();
        LeaveRequest lr = TestDataFactory.buildApprovedLeaveRequest(11L, employee, leaveType);

        when(leaveRequestRepository.findById(11L)).thenReturn(Optional.of(lr));

        assertThatThrownBy(() -> leaveService.approveLeave(11L, "Late approve"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("already processed");
    }

    @Test(description = "approveLeave – throws BusinessException when leave request not found")
    public void approveLeave_notFound_throwsBusinessException() {
        when(leaveRequestRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> leaveService.approveLeave(999L, "comment"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Leave request not found");
    }

    // ── rejectLeave ───────────────────────────────────────────────────────────

    @Test(description = "rejectLeave – sets REJECTED and saves the comment")
    public void rejectLeave_pendingRequest_setsRejectedWithComment() {
        Employee  employee  = TestDataFactory.buildEmployee(1L, "Alice", "alice@epam.com", "hash");
        LeaveType leaveType = TestDataFactory.buildAnnualLeaveType();
        LocalDate start = LocalDate.now().plusDays(2);
        LocalDate end   = LocalDate.now().plusDays(4);
        LeaveRequest lr = TestDataFactory.buildPendingLeaveRequest(20L, employee, leaveType, start, end);

        when(leaveRequestRepository.findById(20L)).thenReturn(Optional.of(lr));
        when(leaveRequestRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        leaveService.rejectLeave(20L, "Budget freeze");

        assertThat(lr.getStatus()).isEqualTo(LeaveStatus.REJECTED);
        assertThat(lr.getManagerComment()).isEqualTo("Budget freeze");
    }

    @Test(description = "rejectLeave – throws when request is not pending")
    public void rejectLeave_notPending_throwsBusinessException() {
        Employee  employee  = TestDataFactory.buildEmployee(1L, "Alice", "alice@epam.com", "hash");
        LeaveType leaveType = TestDataFactory.buildAnnualLeaveType();
        LeaveRequest lr = TestDataFactory.buildApprovedLeaveRequest(21L, employee, leaveType);

        when(leaveRequestRepository.findById(21L)).thenReturn(Optional.of(lr));

        assertThatThrownBy(() -> leaveService.rejectLeave(21L, "comment"))
                .isInstanceOf(BusinessException.class);
    }

    // ── findLeavesForEmployee ─────────────────────────────────────────────────

    @Test(description = "findLeavesForEmployee – delegates to repository correctly")
    public void findLeavesForEmployee_returnsRepositoryResult() {
        Employee  employee  = TestDataFactory.buildEmployee(1L, "Alice", "alice@epam.com", "hash");
        LeaveType leaveType = TestDataFactory.buildAnnualLeaveType();
        LocalDate start = LocalDate.now().plusDays(1);
        LeaveRequest lr = TestDataFactory.buildPendingLeaveRequest(30L, employee, leaveType,
                start, start.plusDays(1));

        when(leaveRequestRepository.findByEmployeeId(1L)).thenReturn(List.of(lr));

        List<LeaveRequest> result = leaveService.findLeavesForEmployee(1L);

        assertThat(result).hasSize(1);
        verify(leaveRequestRepository).findByEmployeeId(1L);
    }

    // ── getLeaveBalances ──────────────────────────────────────────────────────

    @Test(description = "getLeaveBalances – returns all balances for the employee")
    public void getLeaveBalances_returnsBalanceList() {
        Employee  employee  = TestDataFactory.buildEmployee(1L, "Alice", "alice@epam.com", "hash");
        LeaveType leaveType = TestDataFactory.buildAnnualLeaveType();
        LeaveBalance balance = TestDataFactory.buildLeaveBalance(1L, employee, leaveType, 20.0);

        when(leaveBalanceRepository.findByEmployeeId(1L)).thenReturn(List.of(balance));

        List<?> result = leaveService.getLeaveBalances(1L);

        assertThat(result).hasSize(1);
        verify(leaveBalanceRepository).findByEmployeeId(1L);
    }

    @Test(description = "getLeaveBalances – returns empty list when employee has no balances")
    public void getLeaveBalances_noBalances_returnsEmpty() {
        when(leaveBalanceRepository.findByEmployeeId(99L)).thenReturn(List.of());

        List<?> result = leaveService.getLeaveBalances(99L);

        assertThat(result).isEmpty();
    }
}
