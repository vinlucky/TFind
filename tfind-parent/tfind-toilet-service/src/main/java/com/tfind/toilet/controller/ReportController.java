package com.tfind.toilet.controller;

import com.tfind.toilet.entity.Report;
import com.tfind.toilet.service.ReportService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/report")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> addReport(
            @RequestBody Report report,
            @RequestAttribute("userId") String userId) {
        report.setOpenid(userId);
        Report created = reportService.addReport(report);
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("data", created);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/toilet/{toiletId}")
    public ResponseEntity<Map<String, Object>> getReportsByToiletId(@PathVariable String toiletId) {
        List<Report> reports = reportService.getReportsByToiletId(toiletId);
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("data", reports);
        return ResponseEntity.ok(result);
    }
}
