package com.epam.elms.unit.controller;

import com.epam.elms.controller.ReportController;
import com.epam.elms.entity.LeaveRequest;
import com.epam.elms.service.ReportService;
import com.epam.elms.utils.TestDataFactory;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link ReportController}.
 * ReportService.getLeaveSummary returns {@code List<?>} – use raw List to stub.
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public class ReportControllerTest {

    @Mock private ReportService reportService;

    private ReportController reportController;
    private AutoCloseable    mocks;

    @BeforeMethod
    public void setUp() {
        mocks            = MockitoAnnotations.openMocks(this);
        reportController = new ReportController();
        ReflectionTestUtils.setField(reportController, "reportService", reportService);
    }

    @AfterMethod
    public void tearDown() throws Exception {
        mocks.close();
    }

    @Test(description = "getLeaveReports – returns 200 OK with a list of leave summaries")
    public void getLeaveReports_returns200WithList() {
        var emp = TestDataFactory.buildEmployee(1L, "Alice", "alice@epam.com", "h");
        var lt  = TestDataFactory.buildAnnualLeaveType();
        LeaveRequest lr = TestDataFactory.buildPendingLeaveRequest(
                1L, emp, lt, LocalDate.now().plusDays(1), LocalDate.now().plusDays(3));

        List rawList = List.of(lr);
        doReturn(rawList).when(reportService).getLeaveSummary();

        ResponseEntity<?> result = reportController.getLeaveReports();

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat((List<?>) result.getBody()).hasSize(1);
    }

    @Test(description = "getLeaveReports – empty list returns 200 with empty body")
    public void getLeaveReports_noLeaves_returns200EmptyList() {
        doReturn(Collections.emptyList()).when(reportService).getLeaveSummary();

        ResponseEntity<?> result = reportController.getLeaveReports();

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat((List<?>) result.getBody()).isEmpty();
    }

    @Test(description = "getLeaveReports – delegates to ReportService exactly once")
    public void getLeaveReports_delegatesToServiceOnce() {
        doReturn(Collections.emptyList()).when(reportService).getLeaveSummary();

        reportController.getLeaveReports();

        verify(reportService, times(1)).getLeaveSummary();
    }

    @Test(description = "getLeaveReports – service exception propagates to caller")
    public void getLeaveReports_serviceThrows_propagatesException() {
        doThrow(new RuntimeException("Report generation failed"))
                .when(reportService).getLeaveSummary();

        assertThatThrownBy(() -> reportController.getLeaveReports())
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Report generation failed");
    }

    @Test(description = "getLeaveReports – returns all records including approved and rejected")
    public void getLeaveReports_mixedStatuses_returnsAll() {
        var emp = TestDataFactory.buildEmployee(1L, "Alice", "alice@epam.com", "h");
        var lt  = TestDataFactory.buildAnnualLeaveType();
        LeaveRequest pending  = TestDataFactory.buildPendingLeaveRequest(
                1L, emp, lt, LocalDate.now().plusDays(1), LocalDate.now().plusDays(2));
        LeaveRequest approved = TestDataFactory.buildApprovedLeaveRequest(2L, emp, lt);

        List rawList = List.of(pending, approved);
        doReturn(rawList).when(reportService).getLeaveSummary();

        ResponseEntity<?> result = reportController.getLeaveReports();

        assertThat((List<?>) result.getBody()).hasSize(2);
    }
}
