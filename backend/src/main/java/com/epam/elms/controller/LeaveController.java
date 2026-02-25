package com.epam.elms.controller;

import com.epam.elms.dto.LeaveRequestDto;
import com.epam.elms.entity.LeaveRequest;
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

    @GetMapping("/employee/{employeeId}")
    public List<LeaveRequest> getEmployeeLeaves(@PathVariable Long employeeId) {
        return leaveRequestService.getEmployeeLeaves(employeeId);
    }

    @GetMapping("/pending")
    public ResponseEntity<List<LeaveRequestDto>> getPendingLeaves() {
        return ResponseEntity.ok(leaveRequestService.getPendingLeaves());
    }

    @PostMapping("/apply")
    public ResponseEntity<?> applyLeave(@RequestBody LeaveRequestDto dto) {
        leaveRequestService.applyLeave(dto);
        return ResponseEntity.ok("Leave request submitted.");
    }

    @PutMapping("/{id}/approve")
    public ResponseEntity<?> approveLeave(@PathVariable Long id) {
        leaveRequestService.approveLeave(id);
        return ResponseEntity.ok("Leave request approved.");
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<?> rejectLeave(@PathVariable Long id) {
        leaveRequestService.rejectLeave(id);
        return ResponseEntity.ok("Leave request rejected.");
    }
}
