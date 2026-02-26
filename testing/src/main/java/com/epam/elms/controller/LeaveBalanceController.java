package com.epam.elms.controller;

import com.epam.elms.service.LeaveService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/balance")
public class LeaveBalanceController {
    @Autowired
    private LeaveService leaveService;

    @GetMapping("/{employeeId}")
    public ResponseEntity<?> getBalance(@PathVariable Long employeeId) {
        return ResponseEntity.ok(leaveService.getLeaveBalances(employeeId));
    }
}
