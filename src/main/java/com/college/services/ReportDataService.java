package com.college.services;

import com.college.dao.PlacementDAO;
import com.college.dao.VisitorDAO;

import com.college.models.PlacementDrive;
import com.college.models.VisitorLog;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class ReportDataService {

    private final VisitorDAO visitorDAO;
    private final PlacementDAO placementDAO;

    public ReportDataService() {
        this.visitorDAO = new VisitorDAO();
        this.placementDAO = new PlacementDAO();
    }

    public List<VisitorLog> getVisitorLogs(LocalDate start, LocalDate end) {
        List<VisitorLog> allLogs = visitorDAO.getAllVisitorLogs();
        return allLogs.stream()
                .filter(log -> {
                    LocalDate entryDate = log.getEntryTime().toLocalDate();
                    return (entryDate.isEqual(start) || entryDate.isAfter(start)) &&
                            (entryDate.isEqual(end) || entryDate.isBefore(end));
                })
                .collect(Collectors.toList());
    }

    public PlacementStats getPlacementStats() {
        List<PlacementDrive> drives = placementDAO.getAllDrives();
        int totalApplications = placementDAO.getTotalApplicationsCount();
        List<java.util.Map<String, Object>> companySummary = placementDAO.getCompanyApplicationSummary();

        int totalDrives = drives.size();
        long activeDrives = drives.stream()
                .filter(d -> d.getDeadline().isAfter(LocalDate.now()))
                .count();

        return new PlacementStats(totalDrives, (int) activeDrives, totalApplications, companySummary);
    }

    // Simple DTO for Stats
    public static class PlacementStats {
        public int totalDrives;
        public int activeDrives;
        public int totalApplications;
        public List<java.util.Map<String, Object>> companySummary;

        public PlacementStats(int total, int active, int totalApps,
                List<java.util.Map<String, Object>> companySummary) {
            this.totalDrives = total;
            this.activeDrives = active;
            this.totalApplications = totalApps;
            this.companySummary = companySummary;
        }
    }
}
