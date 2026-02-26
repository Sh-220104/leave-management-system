package com.epam.elms.controller;

import com.epam.elms.dto.LeaveDecisionDto;
import com.epam.elms.dto.LeaveRequestDto;
import com.epam.elms.service.LeaveRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/leaves")
@RequiredArgsConstructor
public class LeaveController {

    private final LeaveRequestService leaveRequestService;

    /**
     * GET /leaves/employee/{employeeId}
     * Returns all leave requests for the given employee as DTOs (includes status, leaveTypeName, etc.)
     */
    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<List<LeaveRequestDto>> getEmployeeLeaves(@PathVariable Long employeeId) {
        return ResponseEntity.ok(leaveRequestService.getEmployeeLeavesDtos(employeeId));
    }

    /**
     * GET /leaves/pending
     * Returns all PENDING leave requests (for Manager Approval page).
     */
    @GetMapping("/pending")
    public ResponseEntity<List<LeaveRequestDto>> getPendingLeaves() {
        return ResponseEntity.ok(leaveRequestService.getPendingLeaves());
    }

    /**
     * POST /leaves/apply
     * Employee submits a new leave request.
     */
    @PostMapping("/apply")
    public ResponseEntity<?> applyLeave(@RequestBody LeaveRequestDto dto) {
        leaveRequestService.applyLeave(dto);
        return ResponseEntity.ok("Leave request submitted.");
    }

    /**
     * PUT /leaves/{id}/approve
     * Manager approves a leave request. Optional body: { "comment": "..." }
     */
    @PutMapping("/{id}/approve")
    public ResponseEntity<?> approveLeave(
            @PathVariable Long id,
            @RequestBody(required = false) LeaveDecisionDto decisionDto) {
        String comment = decisionDto != null ? decisionDto.getComment() : null;
        leaveRequestService.approveLeave(id, comment);
        return ResponseEntity.ok("Leave request approved.");
    }

    /**
     * PUT /leaves/{id}/reject
     * Manager rejects a leave request. Optional body: { "comment": "..." }
     */
    @PutMapping("/{id}/reject")
    public ResponseEntity<?> rejectLeave(
            @PathVariable Long id,
            @RequestBody(required = false) LeaveDecisionDto decisionDto) {
        String comment = decisionDto != null ? decisionDto.getComment() : null;
        leaveRequestService.rejectLeave(id, comment);
        return ResponseEntity.ok("Leave request rejected.");
    }
}
