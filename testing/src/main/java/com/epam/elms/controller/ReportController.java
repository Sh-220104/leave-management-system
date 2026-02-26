package com.epam.elms.controller;

import com.epam.elms.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/reports")
public class ReportController {
    @Autowired
    private ReportService reportService;

    @GetMapping("/leaves")
    public ResponseEntity<?> getLeaveReports() {
        return ResponseEntity.ok(reportService.getLeaveSummary());
    }
}
