package com.epam.elms.service.impl;

import com.epam.elms.dto.LeaveRequestDto;
import com.epam.elms.entity.*;
import com.epam.elms.exception.BusinessException;
import com.epam.elms.repository.*;
import com.epam.elms.service.LeaveService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LeaveServiceImpl implements LeaveService {
    private final LeaveRequestRepository leaveRequestRepository;
    private final EmployeeRepository employeeRepository;
    private final LeaveTypeRepository leaveTypeRepository;
    private final LeaveBalanceRepository leaveBalanceRepository;

    @Override
    @Transactional
    public void applyLeaveForUser(String email, LeaveRequestDto dto) {
        Employee employee = employeeRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException("Employee not found"));
        LeaveType leaveType = leaveTypeRepository.findById(dto.getLeaveTypeId())
                .orElseThrow(() -> new BusinessException("Leave type not found"));
        LocalDate today = LocalDate.now();
        if (dto.getStartDate() == null || dto.getEndDate() == null
                || dto.getStartDate().isBefore(today)
                || dto.getEndDate().isBefore(dto.getStartDate())) {
            throw new BusinessException("Invalid leave dates or applying for past dates");
        }
        LeaveBalance balance = leaveBalanceRepository
                .findByEmployeeIdAndLeaveTypeId(employee.getId(), leaveType.getId())
                .orElseThrow(() -> new BusinessException("Leave balance entry not found"));
        long requestedDays = dto.getEndDate().toEpochDay() - dto.getStartDate().toEpochDay() + 1;
        if (requestedDays > balance.getBalance()) {
            throw new BusinessException("Not enough leave balance. Balance: " + balance.getBalance());
        }
        LeaveRequest lr = LeaveRequest.builder()
                .employee(employee)
                .leaveType(leaveType)
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .notes(dto.getReason())
                .status(LeaveStatus.PENDING)
                .createdOn(today)
                .build();
        leaveRequestRepository.save(lr);
    }

    @Override
    @Transactional
    public void approveLeave(Long leaveRequestId, String comment) {
        LeaveRequest lr = leaveRequestRepository.findById(leaveRequestId)
                .orElseThrow(() -> new BusinessException("Leave request not found"));
        if (lr.getStatus() != LeaveStatus.PENDING)
            throw new BusinessException("Leave request already processed");
        LeaveBalance balance = leaveBalanceRepository
                .findByEmployeeIdAndLeaveTypeId(lr.getEmployee().getId(), lr.getLeaveType().getId())
                .orElseThrow(() -> new BusinessException("Leave balance entry not found"));
        long requestedDays = lr.getEndDate().toEpochDay() - lr.getStartDate().toEpochDay() + 1;
        if (requestedDays > balance.getBalance())
            throw new BusinessException("Insufficient leave balance on approval.");
        balance.setBalance(balance.getBalance() - requestedDays);
        leaveBalanceRepository.save(balance);
        lr.setStatus(LeaveStatus.APPROVED);
        lr.setManagerComment(comment);
        lr.setDecisionOn(LocalDate.now());
        leaveRequestRepository.save(lr);
    }

    @Override
    @Transactional
    public void rejectLeave(Long leaveRequestId, String comment) {
        LeaveRequest lr = leaveRequestRepository.findById(leaveRequestId)
                .orElseThrow(() -> new BusinessException("Leave request not found"));
        if (lr.getStatus() != LeaveStatus.PENDING)
            throw new BusinessException("Leave request already processed");
        lr.setStatus(LeaveStatus.REJECTED);
        lr.setManagerComment(comment);
        lr.setDecisionOn(LocalDate.now());
        leaveRequestRepository.save(lr);
    }

    @Override
    public List<LeaveRequest> findLeavesForEmployee(Long employeeId) {
        return leaveRequestRepository.findByEmployeeId(employeeId);
    }

    @Override
    public List<?> getLeaveBalances(Long employeeId) {
        return leaveBalanceRepository.findByEmployeeId(employeeId);
    }
}
