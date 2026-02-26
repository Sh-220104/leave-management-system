package com.epam.elms.unit.controller;

import com.epam.elms.controller.LeaveBalanceController;
import com.epam.elms.entity.Employee;
import com.epam.elms.entity.LeaveBalance;
import com.epam.elms.entity.LeaveType;
import com.epam.elms.service.LeaveService;
import com.epam.elms.utils.TestDataFactory;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link LeaveBalanceController}.
 * LeaveService.getLeaveBalances returns {@code List<?>} – use raw List to stub.
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public class LeaveBalanceControllerTest {

    @Mock private LeaveService leaveService;

    private LeaveBalanceController leaveBalanceController;
    private AutoCloseable          mocks;

    @BeforeMethod
    public void setUp() {
        mocks                  = MockitoAnnotations.openMocks(this);
        leaveBalanceController = new LeaveBalanceController();
        ReflectionTestUtils.setField(leaveBalanceController, "leaveService", leaveService);
    }

    @AfterMethod
    public void tearDown() throws Exception {
        mocks.close();
    }

    @Test(description = "getBalance – returns 200 OK with list of LeaveBalance objects")
    public void getBalance_validEmployeeId_returns200WithList() {
        Employee  emp   = TestDataFactory.buildEmployee(1L, "Alice", "alice@epam.com", "hash");
        LeaveType lt    = TestDataFactory.buildAnnualLeaveType();
        LeaveBalance bal = TestDataFactory.buildLeaveBalance(1L, emp, lt, 20.0);

        List rawList = List.of(bal);
        doReturn(rawList).when(leaveService).getLeaveBalances(1L);

        ResponseEntity<?> result = leaveBalanceController.getBalance(1L);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isInstanceOf(List.class);
        assertThat((List<?>) result.getBody()).hasSize(1);
    }

    @Test(description = "getBalance – returns 200 OK with empty list when no balances exist")
    public void getBalance_noBalances_returns200EmptyList() {
        doReturn(Collections.emptyList()).when(leaveService).getLeaveBalances(99L);

        ResponseEntity<?> result = leaveBalanceController.getBalance(99L);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat((List<?>) result.getBody()).isEmpty();
    }

    @Test(description = "getBalance – delegates to leaveService exactly once")
    public void getBalance_delegatesToServiceOnce() {
        doReturn(Collections.emptyList()).when(leaveService).getLeaveBalances(1L);

        leaveBalanceController.getBalance(1L);

        verify(leaveService, times(1)).getLeaveBalances(1L);
    }

    @Test(description = "getBalance – service exception propagates to caller")
    public void getBalance_serviceThrows_propagatesException() {
        doThrow(new RuntimeException("Employee not found"))
                .when(leaveService).getLeaveBalances(anyLong());

        assertThatThrownBy(() -> leaveBalanceController.getBalance(999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Employee not found");
    }

    @Test(description = "getBalance – returns multiple balances when employee has multiple leave types")
    public void getBalance_multipleBalances_returnsAllEntries() {
        Employee  emp    = TestDataFactory.buildEmployee(1L, "Alice", "alice@epam.com", "hash");
        LeaveType annual = TestDataFactory.buildAnnualLeaveType();
        LeaveType sick   = TestDataFactory.buildSickLeaveType();

        LeaveBalance bal1 = TestDataFactory.buildLeaveBalance(1L, emp, annual, 20.0);
        LeaveBalance bal2 = TestDataFactory.buildLeaveBalance(2L, emp, sick,   10.0);

        List rawList = List.of(bal1, bal2);
        doReturn(rawList).when(leaveService).getLeaveBalances(1L);

        ResponseEntity<?> result = leaveBalanceController.getBalance(1L);

        assertThat((List<?>) result.getBody()).hasSize(2);
    }
}
