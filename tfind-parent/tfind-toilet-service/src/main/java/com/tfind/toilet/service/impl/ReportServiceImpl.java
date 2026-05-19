package com.tfind.toilet.service.impl;

import com.tfind.toilet.entity.Report;
import com.tfind.toilet.service.BerkeleyDbService;
import com.tfind.toilet.service.ReportService;
import com.tfind.toilet.service.ToiletService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ReportServiceImpl implements ReportService {

    private static final String COLLECTION = "reports";
    private static final int REPORT_THRESHOLD = 5;

    private final BerkeleyDbService berkeleyDbService;
    private final ToiletService toiletService;

    public ReportServiceImpl(BerkeleyDbService berkeleyDbService, ToiletService toiletService) {
        this.berkeleyDbService = berkeleyDbService;
        this.toiletService = toiletService;
    }

    @Override
    public Report addReport(Report report) {
        report.setId(UUID.randomUUID().toString());
        report.setStatus("pending");
        report.setCreateTime(System.currentTimeMillis());

        String generatedId = berkeleyDbService.save(COLLECTION, report);
        report.setId(generatedId);

        if (checkReportThreshold(report.getReportType(), report.getToiletId())) {
            toiletService.updateToiletStatus(report.getToiletId(), "closed");
        }

        return report;
    }

    @Override
    public List<Report> getReportsByToiletId(String toiletId) {
        return berkeleyDbService.query(COLLECTION, "/toiletId = :1", Report.class);
    }

    @Override
    public boolean checkReportThreshold(String reportType, String toiletId) {
        List<Report> reports = getReportsByToiletId(toiletId);
        long count = reports.stream()
                .filter(r -> reportType.equals(r.getReportType()) && "pending".equals(r.getStatus()))
                .count();
        return count >= REPORT_THRESHOLD;
    }

    @Override
    public List<Report> getAllReports() {
        return berkeleyDbService.query(COLLECTION, null, Report.class);
    }

    @Override
    public void updateReportStatus(String reportId, String status) {
        Report report = berkeleyDbService.getById(COLLECTION, reportId, Report.class);
        if (report != null) {
            report.setStatus(status);
            berkeleyDbService.update(COLLECTION, reportId, report);
        }
    }
}
