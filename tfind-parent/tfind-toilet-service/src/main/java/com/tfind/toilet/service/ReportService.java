package com.tfind.toilet.service;

import com.tfind.toilet.entity.Report;

import java.util.List;

public interface ReportService {

    Report addReport(Report report);

    List<Report> getReportsByToiletId(String toiletId);

    boolean checkReportThreshold(String reportType, String toiletId);

    List<Report> getAllReports();

    void updateReportStatus(String reportId, String status);
}
