package com.epam.elms.service;

import com.epam.elms.dto.LeaveRequestDto;
import com.epam.elms.entity.LeaveRequest;
import java.util.List;

public interface LeaveRequestService {
    List<LeaveRequest> getEmployeeLeaves(Long employeeId);
    List<LeaveRequestDto> getPendingLeaves();
    LeaveRequest applyLeave(LeaveRequestDto dto);
    LeaveRequest approveLeave(Long id);
    LeaveRequest rejectLeave(Long id);
}
