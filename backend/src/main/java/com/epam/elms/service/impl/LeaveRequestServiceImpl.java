package com.epam.elms.service.impl;

import com.epam.elms.dto.LeaveRequestDto;
import com.epam.elms.entity.Employee;
import com.epam.elms.entity.LeaveBalance;
import com.epam.elms.entity.LeaveRequest;
import com.epam.elms.entity.LeaveStatus;
import com.epam.elms.entity.LeaveType;
import com.epam.elms.repository.EmployeeRepository;
import com.epam.elms.repository.LeaveBalanceRepository;
import com.epam.elms.repository.LeaveRequestRepository;
import com.epam.elms.repository.LeaveTypeRepository;
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
        List<LeaveRequest> pending = leaveRequestRepository.findByStatus(LeaveStatus.PENDING);
        return pending.stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    public LeaveRequest applyLeave(LeaveRequestDto dto) {
        Employee employee = employeeRepository.findById(dto.getEmployeeId())
            .orElseThrow(() -> new RuntimeException("Employee not found"));
        LeaveType leaveType = leaveTypeRepository.findById(dto.getLeaveTypeId())
            .orElseThrow(() -> new RuntimeException("Leave type not found"));
        LocalDate startDate = dto.getStartDate();
        LocalDate endDate = dto.getEndDate();
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Start and end date must be provided");
        }
        long days = ChronoUnit.DAYS.between(startDate, endDate) + 1;
        if (days <= 0) throw new IllegalArgumentException("End date must be after or equal to start date");
        if (startDate.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Leave cannot be applied for past dates");
        }
        LeaveBalance balance = leaveBalanceRepository
                .findByEmployeeIdAndLeaveTypeId(employee.getId(), leaveType.getId())
                .orElseThrow(() -> new IllegalArgumentException("No leave balance found for this leave type"));
        if (balance.getBalance() < days) throw new IllegalArgumentException("Leave request exceeds balance");
        LeaveRequest lr = new LeaveRequest();
        lr.setEmployee(employee);
        lr.setLeaveType(leaveType);
        lr.setStartDate(startDate);
        lr.setEndDate(endDate);
        lr.setStatus(LeaveStatus.PENDING); // Use Enum
        lr.setNotes(dto.getReason()); // Use "notes" field in entity
        lr.setCreatedOn(LocalDate.now());
        leaveRequestRepository.save(lr);
        return lr;
    }

    @Override
    public LeaveRequest approveLeave(Long id) {
        LeaveRequest leaveRequest = leaveRequestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Leave request not found"));
        if (leaveRequest.getStatus() != LeaveStatus.PENDING) {
            throw new IllegalArgumentException("Leave request is not pending");
        }
        long days = ChronoUnit.DAYS.between(leaveRequest.getStartDate(), leaveRequest.getEndDate()) + 1;
        LeaveBalance balance = leaveBalanceRepository
                .findByEmployeeIdAndLeaveTypeId(leaveRequest.getEmployee().getId(), leaveRequest.getLeaveType().getId())
                .orElseThrow(() -> new IllegalArgumentException("No leave balance found for this leave type"));
        if (balance.getBalance() < days) {
            throw new IllegalArgumentException("Leave balance is insufficient for approval");
        }
        balance.setBalance(balance.getBalance() - (int)days);
        leaveBalanceRepository.save(balance);
        leaveRequest.setStatus(LeaveStatus.APPROVED);
        leaveRequestRepository.save(leaveRequest);
        return leaveRequest;
    }

    @Override
    public LeaveRequest rejectLeave(Long id) {
        LeaveRequest leaveRequest = leaveRequestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Leave request not found"));
        if (leaveRequest.getStatus() != LeaveStatus.PENDING) {
            throw new IllegalArgumentException("Leave request is not pending");
        }
        leaveRequest.setStatus(LeaveStatus.REJECTED);
        leaveRequestRepository.save(leaveRequest);
        return leaveRequest;
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
