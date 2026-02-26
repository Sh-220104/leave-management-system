package com.epam.elms.unit.service;

import com.epam.elms.entity.Employee;
import com.epam.elms.entity.LeaveRequest;
import com.epam.elms.entity.LeaveStatus;
import com.epam.elms.entity.LeaveType;
import com.epam.elms.repository.LeaveRequestRepository;
import com.epam.elms.service.impl.ReportServiceImpl;
import com.epam.elms.utils.TestDataFactory;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link ReportServiceImpl}.
 * Uses explicit constructor injection for reliable mock wiring.
 */
public class ReportServiceImplTest {

    @Mock private LeaveRequestRepository leaveRequestRepository;

    private ReportServiceImpl reportService;
    private AutoCloseable     mocks;

    @BeforeMethod
    public void setUp() {
        mocks         = MockitoAnnotations.openMocks(this);
        reportService = new ReportServiceImpl(leaveRequestRepository);
    }

    @AfterMethod
    public void tearDown() throws Exception { mocks.close(); }

    @Test(description = "getLeaveSummary – returns all leave requests from the repository")
    public void getLeaveSummary_returnsAllLeaveRequests() {
        Employee  employee    = TestDataFactory.buildEmployee(1L, "Alice", "alice@epam.com", "h");
        LeaveType annualLeave = TestDataFactory.buildAnnualLeaveType();
        LeaveRequest lr1 = TestDataFactory.buildPendingLeaveRequest(1L, employee, annualLeave,
                LocalDate.now().plusDays(1), LocalDate.now().plusDays(3));
        LeaveRequest lr2 = TestDataFactory.buildApprovedLeaveRequest(2L, employee, annualLeave);

        when(leaveRequestRepository.findAll()).thenReturn(List.of(lr1, lr2));

        List<?> summary = reportService.getLeaveSummary();

        assertThat(summary).hasSize(2);
        verify(leaveRequestRepository).findAll();
    }

    @Test(description = "getLeaveSummary – returns empty list when no leave requests exist")
    public void getLeaveSummary_noRequests_returnsEmptyList() {
        when(leaveRequestRepository.findAll()).thenReturn(List.of());

        List<?> summary = reportService.getLeaveSummary();

        assertThat(summary).isEmpty();
    }

    @Test(description = "getLeaveSummary – result contains both PENDING and APPROVED requests")
    public void getLeaveSummary_mixedStatuses_allIncluded() {
        Employee  employee    = TestDataFactory.buildEmployee(1L, "Alice", "alice@epam.com", "h");
        LeaveType annualLeave = TestDataFactory.buildAnnualLeaveType();

        LeaveRequest pending  = TestDataFactory.buildPendingLeaveRequest(1L, employee, annualLeave,
                LocalDate.now().plusDays(1), LocalDate.now().plusDays(2));
        LeaveRequest approved = TestDataFactory.buildApprovedLeaveRequest(2L, employee, annualLeave);
        LeaveRequest rejected = TestDataFactory.buildPendingLeaveRequest(3L, employee, annualLeave,
                LocalDate.now().plusDays(5), LocalDate.now().plusDays(6));
        rejected.setStatus(LeaveStatus.REJECTED);

        when(leaveRequestRepository.findAll()).thenReturn(List.of(pending, approved, rejected));

        List<?> summary = reportService.getLeaveSummary();

        assertThat(summary).hasSize(3);
    }
}
