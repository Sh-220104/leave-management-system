package com.epam.elms.unit.controller;

import com.epam.elms.controller.AdminController;
import com.epam.elms.service.AdminService;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link AdminController}.
 * AdminController uses @Autowired field injection – injected via ReflectionTestUtils.
 */
public class AdminControllerTest {

    @Mock private AdminService adminService;

    private AdminController adminController;
    private AutoCloseable   mocks;

    @BeforeMethod
    public void setUp() {
        mocks           = MockitoAnnotations.openMocks(this);
        adminController = new AdminController();
        ReflectionTestUtils.setField(adminController, "adminService", adminService);
    }

    @AfterMethod
    public void tearDown() throws Exception {
        mocks.close();
    }

    // ── PUT /admin/leave-balance/{employeeId}/adjust ───────────────────────────

    @Test(description = "adjustLeaveBalance – valid params returns 200 OK")
    public void adjustLeaveBalance_validParams_returns200() {
        doNothing().when(adminService).adjustLeaveBalance(1L, 1L, 15.0);

        ResponseEntity<?> result = adminController.adjustLeaveBalance(1L, 1L, 15.0);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(adminService).adjustLeaveBalance(1L, 1L, 15.0);
    }

    @Test(description = "adjustLeaveBalance – service exception propagates to caller")
    public void adjustLeaveBalance_serviceThrows_propagatesException() {
        doThrow(new RuntimeException("Leave balance not found"))
                .when(adminService).adjustLeaveBalance(999L, 999L, 10.0);

        assertThatThrownBy(() -> adminController.adjustLeaveBalance(999L, 999L, 10.0))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Leave balance not found");
    }

    @Test(description = "adjustLeaveBalance – delegates to service with correct parameters")
    public void adjustLeaveBalance_delegatesToService() {
        doNothing().when(adminService).adjustLeaveBalance(anyLong(), anyLong(), anyDouble());

        adminController.adjustLeaveBalance(2L, 3L, 20.0);

        verify(adminService, times(1)).adjustLeaveBalance(2L, 3L, 20.0);
    }

    @Test(description = "adjustLeaveBalance – zero amount is a valid call")
    public void adjustLeaveBalance_zeroAmount_returns200() {
        doNothing().when(adminService).adjustLeaveBalance(1L, 1L, 0.0);

        ResponseEntity<?> result = adminController.adjustLeaveBalance(1L, 1L, 0.0);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    // ── POST /admin/role/{employeeId} ─────────────────────────────────────────

    @Test(description = "setRole – valid params returns 200 OK")
    public void setRole_validParams_returns200() {
        doNothing().when(adminService).setRole(1L, "MANAGER");

        ResponseEntity<?> result = adminController.setRole(1L, "MANAGER");

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(adminService).setRole(1L, "MANAGER");
    }

    @Test(description = "setRole – service exception propagates to caller")
    public void setRole_serviceThrows_propagatesException() {
        doThrow(new RuntimeException("Employee not found"))
                .when(adminService).setRole(999L, "ADMIN");

        assertThatThrownBy(() -> adminController.setRole(999L, "ADMIN"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Employee not found");
    }

    @Test(description = "setRole – delegates to service with correct parameters")
    public void setRole_delegatesToService() {
        doNothing().when(adminService).setRole(anyLong(), anyString());

        adminController.setRole(5L, "ADMIN");

        verify(adminService, times(1)).setRole(5L, "ADMIN");
    }

    // ── POST /admin/leave-type ────────────────────────────────────────────────

    @Test(description = "createLeaveType – valid params returns 200 OK")
    public void createLeaveType_validParams_returns200() {
        doNothing().when(adminService).createLeaveType("Maternity", "Paid maternity leave");

        ResponseEntity<?> result = adminController.createLeaveType("Maternity", "Paid maternity leave");

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(adminService).createLeaveType("Maternity", "Paid maternity leave");
    }

    @Test(description = "createLeaveType – service exception propagates to caller")
    public void createLeaveType_serviceThrows_propagatesException() {
        doThrow(new RuntimeException("Duplicate leave type"))
                .when(adminService).createLeaveType("Annual Leave", "duplicate");

        assertThatThrownBy(() -> adminController.createLeaveType("Annual Leave", "duplicate"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Duplicate leave type");
    }

    @Test(description = "createLeaveType – delegates to service with correct type and description")
    public void createLeaveType_delegatesToService() {
        doNothing().when(adminService).createLeaveType(anyString(), anyString());

        adminController.createLeaveType("Study Leave", "For exams");

        verify(adminService, times(1)).createLeaveType("Study Leave", "For exams");
    }
}
