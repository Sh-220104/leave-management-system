package com.epam.elms.service.impl;

import com.epam.elms.dto.LeaveRequestDto;
import com.epam.elms.entity.*;
import com.epam.elms.repository.*;
import com.epam.elms.service.LeaveRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LeaveRequestServiceImpl implements LeaveRequestService {
    private final LeaveRequestRepository leaveRequestRepository;
    private final LeaveTypeRepository leaveTypeRepository;
    private final EmployeeRepository employeeRepository;
    private final LeaveBalanceRepository leaveBalanceRepository;

    @Override
    public List<LeaveRequest> getEmployeeLeaves(Long employeeId) {
        return leaveRequestRepository.findByEmployeeId(employeeId);
    }

    @Override
    public List<LeaveRequestDto> getPendingLeaves() {
        return leaveRequestRepository.findByStatus(LeaveStatus.PENDING)
                .stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    public LeaveRequest applyLeave(LeaveRequestDto dto) {
        Employee employee = employeeRepository.findById(dto.getEmployeeId())
                .orElseThrow(() -> new RuntimeException("Employee not found"));
        LeaveType leaveType = leaveTypeRepository.findById(dto.getLeaveTypeId())
                .orElseThrow(() -> new RuntimeException("Leave type not found"));
        LocalDate startDate = dto.getStartDate();
        LocalDate endDate = dto.getEndDate();
        if (startDate == null || endDate == null)
            throw new IllegalArgumentException("Start and end date must be provided");
        long days = ChronoUnit.DAYS.between(startDate, endDate) + 1;
        if (days <= 0) throw new IllegalArgumentException("End date must be after or equal to start date");
        if (startDate.isBefore(LocalDate.now()))
            throw new IllegalArgumentException("Leave cannot be applied for past dates");
        LeaveBalance balance = leaveBalanceRepository
                .findByEmployeeIdAndLeaveTypeId(employee.getId(), leaveType.getId())
                .orElseThrow(() -> new IllegalArgumentException("No leave balance found for this leave type"));
        if (balance.getBalance() < days) throw new IllegalArgumentException("Leave request exceeds balance");
        LeaveRequest lr = new LeaveRequest();
        lr.setEmployee(employee);
        lr.setLeaveType(leaveType);
        lr.setStartDate(startDate);
        lr.setEndDate(endDate);
        lr.setStatus(LeaveStatus.PENDING);
        lr.setNotes(dto.getReason());
        lr.setCreatedOn(LocalDate.now());
        leaveRequestRepository.save(lr);
        return lr;
    }

    @Override
    public LeaveRequest approveLeave(Long id) {
        LeaveRequest lr = leaveRequestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Leave request not found"));
        if (lr.getStatus() != LeaveStatus.PENDING)
            throw new IllegalArgumentException("Leave request is not pending");
        long days = ChronoUnit.DAYS.between(lr.getStartDate(), lr.getEndDate()) + 1;
        LeaveBalance balance = leaveBalanceRepository
                .findByEmployeeIdAndLeaveTypeId(lr.getEmployee().getId(), lr.getLeaveType().getId())
                .orElseThrow(() -> new IllegalArgumentException("No leave balance found for this leave type"));
        if (balance.getBalance() < days)
            throw new IllegalArgumentException("Leave balance is insufficient for approval");
        balance.setBalance(balance.getBalance() - (int) days);
        leaveBalanceRepository.save(balance);
        lr.setStatus(LeaveStatus.APPROVED);
        leaveRequestRepository.save(lr);
        return lr;
    }

    @Override
    public LeaveRequest rejectLeave(Long id) {
        LeaveRequest lr = leaveRequestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Leave request not found"));
        if (lr.getStatus() != LeaveStatus.PENDING)
            throw new IllegalArgumentException("Leave request is not pending");
        lr.setStatus(LeaveStatus.REJECTED);
        leaveRequestRepository.save(lr);
        return lr;
    }

    private LeaveRequestDto toDto(LeaveRequest req) {
        LeaveRequestDto dto = new LeaveRequestDto();
        dto.setId(req.getId());
        dto.setEmployeeId(req.getEmployee().getId());
        dto.setLeaveTypeId(req.getLeaveType().getId());
        dto.setStartDate(req.getStartDate());
        dto.setEndDate(req.getEndDate());
        dto.setReason(req.getNotes());
        return dto;
    }
}
