package com.epam.elms.controller;

import com.epam.elms.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin")
public class AdminController {
    @Autowired
    private AdminService adminService;

    @PutMapping("/leave-balance/{employeeId}/adjust")
    public ResponseEntity<?> adjustLeaveBalance(@PathVariable Long employeeId,
                                                 @RequestParam Long leaveTypeId,
                                                 @RequestParam Double amount) {
        adminService.adjustLeaveBalance(employeeId, leaveTypeId, amount);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/role/{employeeId}")
    public ResponseEntity<?> setRole(@PathVariable Long employeeId,
                                      @RequestParam String role) {
        adminService.setRole(employeeId, role);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/leave-type")
    public ResponseEntity<?> createLeaveType(@RequestParam String type,
                                              @RequestParam String description) {
        adminService.createLeaveType(type, description);
        return ResponseEntity.ok().build();
    }
}
