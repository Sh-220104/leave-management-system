package com.epam.elms.controller;

import com.epam.elms.entity.LeaveType;
import com.epam.elms.repository.LeaveTypeRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/leave-types")
public class LeaveTypeController {
    private final LeaveTypeRepository leaveTypeRepository;
    public LeaveTypeController(LeaveTypeRepository leaveTypeRepository) {
        this.leaveTypeRepository = leaveTypeRepository;
    }
    @GetMapping
    public List<LeaveType> getAllLeaveTypes() {
        return leaveTypeRepository.findAll();
    }
}
