package com.epam.elms.service.impl;

import com.epam.elms.repository.LeaveRequestRepository;
import com.epam.elms.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {
    private final LeaveRequestRepository leaveRequestRepository;

    @Override
    public List<?> getLeaveSummary() {
        return leaveRequestRepository.findAll();
    }
}
