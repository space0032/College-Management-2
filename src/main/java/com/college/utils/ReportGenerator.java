package com.college.utils;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * Report Generator Utility
 * Generates various types of reports with export functionality
 */
public class ReportGenerator {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * Generate CSV file from table data
     */
    public static boolean generateCSV(String filename, String[] headers, List<Object[]> data) {
        try (FileWriter writer = new FileWriter(filename)) {
            // Write headers
            writer.append(String.join(",", headers));
            writer.append("\n");

            // Write data
            for (Object[] row : data) {
                for (int i = 0; i < row.length; i++) {
                    String value = row[i] != null ? row[i].toString() : "";
                    // Escape commas and quotes
                    if (value.contains(",") || value.contains("\"")) {
                        value = "\"" + value.replace("\"", "\"\"") + "\"";
                    }
                    writer.append(value);
                    if (i < row.length - 1) {
                        writer.append(",");
                    }
                }
                writer.append("\n");
            }

            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Calculate percentage
     */
    public static double calculatePercentage(int obtained, int total) {
        if (total == 0)
            return 0;
        return (obtained * 100.0) / total;
    }

    /**
     * Format date range for display
     */
    public static String formatDateRange(LocalDate startDate, LocalDate endDate) {
        return startDate.format(DATE_FORMATTER) + " to " + endDate.format(DATE_FORMATTER);
    }

    /**
     * Generate summary statistics
     */
    public static class Summary {
        private int total;
        private double average;
        private double min;
        private double max;

        public Summary(List<Double> values) {
            if (values.isEmpty()) {
                total = 0;
                average = 0;
                min = 0;
                max = 0;
                return;
            }

            total = values.size();
            double sum = 0;
            min = values.get(0);
            max = values.get(0);

            for (double val : values) {
                sum += val;
                if (val < min)
                    min = val;
                if (val > max)
                    max = val;
            }

            average = sum / total;
        }

        public int getTotal() {
            return total;
        }

        public double getAverage() {
            return average;
        }

        public double getMin() {
            return min;
        }

        public double getMax() {
            return max;
        }
    }

    /**
     * Calculate grade distribution
     */
    public static Map<String, Integer> calculateGradeDistribution(List<String> grades) {
        Map<String, Integer> distribution = new java.util.HashMap<>();
        distribution.put("A+", 0);
        distribution.put("A", 0);
        distribution.put("B", 0);
        distribution.put("C", 0);
        distribution.put("D", 0);
        distribution.put("F", 0);

        for (String grade : grades) {
            if (grade != null && !grade.isEmpty()) {
                distribution.put(grade, distribution.getOrDefault(grade, 0) + 1);
            }
        }

        return distribution;
    }
}
