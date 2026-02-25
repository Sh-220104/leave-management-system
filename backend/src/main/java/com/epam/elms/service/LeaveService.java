package com.epam.elms.service;

import com.epam.elms.dto.LeaveRequestDto;
import com.epam.elms.entity.LeaveRequest;
import java.util.List;

public interface LeaveService {
    void applyLeaveForUser(String email, LeaveRequestDto dto);
    
    void approveLeave(Long leaveRequestId, String comment);
    void rejectLeave(Long leaveRequestId, String comment);

    List<LeaveRequest> findLeavesForEmployee(Long employeeId);

    List<?> getLeaveBalances(Long employeeId);
}