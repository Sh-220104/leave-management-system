package com.epam.elms.config;

import com.epam.elms.entity.LeaveType;
import com.epam.elms.repository.LeaveTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class LeaveTypeInitializer implements ApplicationRunner {
    @Autowired
    private LeaveTypeRepository leaveTypeRepository;

    @Override
    public void run(ApplicationArguments args) {
        if (leaveTypeRepository.count() == 0) {
            LeaveType annual = new LeaveType();
            annual.setType("Annual Leave");
            annual.setDescription("Annual paid leave");
            leaveTypeRepository.save(annual);

            LeaveType sick = new LeaveType();
            sick.setType("Sick Leave");
            sick.setDescription("Paid sick leave");
            leaveTypeRepository.save(sick);

            LeaveType casual = new LeaveType();
            casual.setType("Casual Leave");
            casual.setDescription("Personal casual leave");
            leaveTypeRepository.save(casual);
        }
    }
}