package com.epam.elms.unit.service;

import com.epam.elms.entity.Employee;
import com.epam.elms.entity.LeaveBalance;
import com.epam.elms.entity.LeaveType;
import com.epam.elms.repository.EmployeeRepository;
import com.epam.elms.repository.LeaveBalanceRepository;
import com.epam.elms.repository.LeaveTypeRepository;
import com.epam.elms.service.impl.AdminServiceImpl;
import com.epam.elms.utils.TestDataFactory;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link AdminServiceImpl}.
 * Uses explicit constructor injection to avoid Mockito field-injection issues.
 */
public class AdminServiceImplTest {

    @Mock private LeaveBalanceRepository leaveBalanceRepository;
    @Mock private EmployeeRepository     employeeRepository;
    @Mock private LeaveTypeRepository    leaveTypeRepository;

    private AdminServiceImpl adminService;
    private AutoCloseable    mocks;
    private Employee         employee;
    private LeaveType        annualLeave;
    private LeaveBalance     balance;

    @BeforeMethod
    public void setUp() {
        mocks       = MockitoAnnotations.openMocks(this);
        // Explicit constructor injection – guarantees correct mocks are wired
        adminService = new AdminServiceImpl(leaveBalanceRepository, employeeRepository, leaveTypeRepository);
        employee     = TestDataFactory.buildEmployee(1L, "Alice", "alice@epam.com", "hash");
        annualLeave  = TestDataFactory.buildAnnualLeaveType();
        balance      = TestDataFactory.buildLeaveBalance(1L, employee, annualLeave, 20.0);
    }

    @AfterMethod
    public void tearDown() throws Exception { mocks.close(); }

    // ── adjustLeaveBalance ────────────────────────────────────────────────────

    @Test(description = "adjustLeaveBalance – updates the balance to the specified amount")
    public void adjustLeaveBalance_existingBalance_updatesAmount() {
        when(leaveBalanceRepository.findByEmployeeIdAndLeaveTypeId(1L, 1L))
                .thenReturn(Optional.of(balance));
        when(leaveBalanceRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        adminService.adjustLeaveBalance(1L, 1L, 15.0);

        assertThat(balance.getBalance()).isEqualTo(15.0);
        verify(leaveBalanceRepository).save(balance);
    }

    @Test(description = "adjustLeaveBalance – throws RuntimeException when balance not found")
    public void adjustLeaveBalance_notFound_throwsRuntimeException() {
        when(leaveBalanceRepository.findByEmployeeIdAndLeaveTypeId(1L, 99L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminService.adjustLeaveBalance(1L, 99L, 10.0))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Leave balance not found");
    }

    @Test(description = "adjustLeaveBalance – zero amount is a valid adjustment")
    public void adjustLeaveBalance_zeroAmount_setsZero() {
        // Fresh balance object for this test
        LeaveBalance freshBalance = TestDataFactory.buildLeaveBalance(2L, employee, annualLeave, 20.0);
        when(leaveBalanceRepository.findByEmployeeIdAndLeaveTypeId(1L, 1L))
                .thenReturn(Optional.of(freshBalance));
        when(leaveBalanceRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        adminService.adjustLeaveBalance(1L, 1L, 0.0);

        assertThat(freshBalance.getBalance()).isEqualTo(0.0);
    }

    // ── setRole ───────────────────────────────────────────────────────────────

    @Test(description = "setRole – adds the specified role to the employee's role set")
    public void setRole_existingEmployee_addsRole() {
        Employee freshEmployee = TestDataFactory.buildEmployee(1L, "Alice", "alice@epam.com", "hash");
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(freshEmployee));
        when(employeeRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        adminService.setRole(1L, "ADMIN");

        assertThat(freshEmployee.getRoles()).contains("ADMIN");
        verify(employeeRepository).save(freshEmployee);
    }

    @Test(description = "setRole – throws RuntimeException when employee not found")
    public void setRole_employeeNotFound_throwsRuntimeException() {
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminService.setRole(999L, "ADMIN"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Employee not found");
    }

    @Test(description = "setRole – assigning existing role is idempotent (Set semantics)")
    public void setRole_duplicateRole_remainsIdempotent() {
        Employee freshEmployee = TestDataFactory.buildEmployee(1L, "Alice", "alice@epam.com", "hash");
        int sizeBefore = freshEmployee.getRoles().size();

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(freshEmployee));
        when(employeeRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        adminService.setRole(1L, "ROLE_EMPLOYEE"); // assign same role again

        assertThat(freshEmployee.getRoles().size()).isEqualTo(sizeBefore);
    }

    // ── createLeaveType ───────────────────────────────────────────────────────

    @Test(description = "createLeaveType – saves a new LeaveType with correct type and description")
    public void createLeaveType_validArgs_savesLeaveType() {
        when(leaveTypeRepository.save(any(LeaveType.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        adminService.createLeaveType("Maternity Leave", "Paid maternity leave");

        verify(leaveTypeRepository).save(argThat(lt ->
                "Maternity Leave".equals(lt.getType()) &&
                "Paid maternity leave".equals(lt.getDescription())));
    }

    @Test(description = "createLeaveType – multiple distinct leave types can be created")
    public void createLeaveType_multipleTypes_eachSavedSeparately() {
        when(leaveTypeRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        adminService.createLeaveType("Paternity Leave", "Paternity");
        adminService.createLeaveType("Bereavement Leave", "Bereavement");

        verify(leaveTypeRepository, times(2)).save(any(LeaveType.class));
    }
}
