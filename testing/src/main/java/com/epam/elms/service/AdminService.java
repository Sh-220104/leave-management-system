package com.epam.elms.service;

public interface AdminService {
    void adjustLeaveBalance(Long employeeId, Long leaveTypeId, Double amount);
    void setRole(Long employeeId, String role);
    void createLeaveType(String type, String description);
}
