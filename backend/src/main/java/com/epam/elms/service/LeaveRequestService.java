package com.epam.elms.service;

import com.epam.elms.dto.LeaveRequestDto;
import com.epam.elms.entity.LeaveRequest;

import java.util.List;

public interface LeaveRequestService {

    /** Returns all leave requests for an employee as full DTOs (with status, leaveTypeName, etc.) */
    List<LeaveRequestDto> getEmployeeLeavesDtos(Long employeeId);

    /** Returns all PENDING leave requests as DTOs for the Manager Approval page */
    List<LeaveRequestDto> getPendingLeaves();

    /** Applies a new leave request */
    LeaveRequest applyLeave(LeaveRequestDto dto);

    /** Approves a pending leave request; optionally records a manager comment */
    LeaveRequest approveLeave(Long id, String managerComment);

    /** Rejects a pending leave request; optionally records a manager comment */
    LeaveRequest rejectLeave(Long id, String managerComment);
}
