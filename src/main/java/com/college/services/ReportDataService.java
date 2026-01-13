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
        // Since we don't have a direct "getAllApplications" in DAO exposed publicly for
        // stats easily,
        // we might need to add a method to DAO or just fetch counts.
        // For now, let's assume we want Drive Stats.

        int totalDrives = drives.size();
        long activeDrives = drives.stream()
                .filter(d -> d.getDeadline().isAfter(LocalDate.now()))
                .count();

        return new PlacementStats(totalDrives, (int) activeDrives);
    }

    // Simple DTO for Stats
    public static class PlacementStats {
        public int totalDrives;
        public int activeDrives;

        public PlacementStats(int total, int active) {
            this.totalDrives = total;
            this.activeDrives = active;
        }
    }
}
