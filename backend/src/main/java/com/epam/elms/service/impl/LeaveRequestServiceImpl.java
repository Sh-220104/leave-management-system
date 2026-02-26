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

    // ─────────────────────────────────────────────────────────────────────────
    // READ
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    public List<LeaveRequestDto> getEmployeeLeavesDtos(Long employeeId) {
        return leaveRequestRepository.findByEmployeeId(employeeId)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<LeaveRequestDto> getPendingLeaves() {
        return leaveRequestRepository.findByStatus(LeaveStatus.PENDING)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // APPLY
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    public LeaveRequest applyLeave(LeaveRequestDto dto) {
        Employee employee = employeeRepository.findById(dto.getEmployeeId())
                .orElseThrow(() -> new RuntimeException("Employee not found"));
        LeaveType leaveType = leaveTypeRepository.findById(dto.getLeaveTypeId())
                .orElseThrow(() -> new RuntimeException("Leave type not found"));

        LocalDate startDate = dto.getStartDate();
        LocalDate endDate   = dto.getEndDate();

        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Start and end date must be provided");
        }
        long days = ChronoUnit.DAYS.between(startDate, endDate) + 1;
        if (days <= 0) {
            throw new IllegalArgumentException("End date must be after or equal to start date");
        }
        if (startDate.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Leave cannot be applied for past dates");
        }

        LeaveBalance balance = leaveBalanceRepository
                .findByEmployeeIdAndLeaveTypeId(employee.getId(), leaveType.getId())
                .orElseThrow(() -> new IllegalArgumentException("No leave balance found for this leave type"));

        if (balance.getBalance() < days) {
            throw new IllegalArgumentException("Leave request exceeds balance");
        }

        LeaveRequest lr = new LeaveRequest();
        lr.setEmployee(employee);
        lr.setLeaveType(leaveType);
        lr.setStartDate(startDate);
        lr.setEndDate(endDate);
        lr.setStatus(LeaveStatus.PENDING);
        lr.setNotes(dto.getReason());
        lr.setCreatedOn(LocalDate.now());
        return leaveRequestRepository.save(lr);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // APPROVE
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    public LeaveRequest approveLeave(Long id, String managerComment) {
        LeaveRequest leaveRequest = leaveRequestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Leave request not found: " + id));

        if (leaveRequest.getStatus() != LeaveStatus.PENDING) {
            throw new IllegalArgumentException("Only PENDING leave requests can be approved");
        }

        long days = ChronoUnit.DAYS.between(leaveRequest.getStartDate(), leaveRequest.getEndDate()) + 1;

        LeaveBalance balance = leaveBalanceRepository
                .findByEmployeeIdAndLeaveTypeId(
                        leaveRequest.getEmployee().getId(),
                        leaveRequest.getLeaveType().getId())
                .orElseThrow(() -> new IllegalArgumentException("No leave balance found for this leave type"));

        if (balance.getBalance() < days) {
            throw new IllegalArgumentException("Insufficient leave balance to approve this request");
        }

        // Deduct balance
        balance.setBalance(balance.getBalance() - (int) days);
        leaveBalanceRepository.save(balance);

        // Update request
        leaveRequest.setStatus(LeaveStatus.APPROVED);
        leaveRequest.setDecisionOn(LocalDate.now());
        if (managerComment != null && !managerComment.isBlank()) {
            leaveRequest.setManagerComment(managerComment);
        }
        return leaveRequestRepository.save(leaveRequest);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // REJECT
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    public LeaveRequest rejectLeave(Long id, String managerComment) {
        LeaveRequest leaveRequest = leaveRequestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Leave request not found: " + id));

        if (leaveRequest.getStatus() != LeaveStatus.PENDING) {
            throw new IllegalArgumentException("Only PENDING leave requests can be rejected");
        }

        leaveRequest.setStatus(LeaveStatus.REJECTED);
        leaveRequest.setDecisionOn(LocalDate.now());
        if (managerComment != null && !managerComment.isBlank()) {
            leaveRequest.setManagerComment(managerComment);
        }
        return leaveRequestRepository.save(leaveRequest);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // MAPPER  — ALL fields populated (this was the root cause of the bug)
    // ─────────────────────────────────────────────────────────────────────────

    private LeaveRequestDto toDto(LeaveRequest req) {
        LeaveRequestDto dto = new LeaveRequestDto();
        dto.setId(req.getId());

        // Employee
        dto.setEmployeeId(req.getEmployee().getId());
        dto.setEmployeeName(req.getEmployee().getName());

        // Leave type
        dto.setLeaveTypeId(req.getLeaveType().getId());
        dto.setLeaveTypeName(req.getLeaveType().getType());

        // Dates & notes
        dto.setStartDate(req.getStartDate());
        dto.setEndDate(req.getEndDate());
        dto.setReason(req.getNotes());

        // ── KEY FIX ── status was NEVER set before; buttons were always disabled
        dto.setStatus(req.getStatus() != null ? req.getStatus().name() : null);

        // Manager comment (shown in leave history / manager view)
        dto.setManagerComment(req.getManagerComment());

        return dto;
    }
}
