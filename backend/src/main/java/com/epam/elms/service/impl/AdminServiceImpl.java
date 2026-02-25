package com.epam.elms.service.impl;

import com.epam.elms.entity.Employee;
import com.epam.elms.entity.LeaveBalance;
import com.epam.elms.entity.LeaveType;
import com.epam.elms.repository.EmployeeRepository;
import com.epam.elms.repository.LeaveBalanceRepository;
import com.epam.elms.repository.LeaveTypeRepository;
import com.epam.elms.service.AdminService;
import lombok.RequiredArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {
    private final LeaveBalanceRepository leaveBalanceRepository;
    private final EmployeeRepository employeeRepository;
    private final LeaveTypeRepository leaveTypeRepository;

    @Transactional
    @Override
    public void adjustLeaveBalance(Long employeeId, Long leaveTypeId, Double amount) {
        LeaveBalance balance = leaveBalanceRepository.findByEmployeeIdAndLeaveTypeId(employeeId, leaveTypeId)
            .orElseThrow(() -> new RuntimeException("Leave balance not found."));
        balance.setBalance(amount);
        leaveBalanceRepository.save(balance);
        // TODO: Audit log
    }
    
    @Transactional
    @Override
    public void setRole(Long employeeId, String role) {
        Employee emp = employeeRepository.findById(employeeId)
            .orElseThrow(() -> new RuntimeException("Employee not found."));
        emp.getRoles().add(role);
        employeeRepository.save(emp);
        // TODO: Audit
    }

    @Transactional
    @Override
    public void createLeaveType(String type, String description) {
        leaveTypeRepository.save(LeaveType.builder().type(type).description(description).build());
        // TODO: Audit
    }
}