package com.college.services;

import com.college.models.VisitorLog;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ExcelReportGenerator {

    public void generateVisitorLogExcel(List<VisitorLog> logs, File dest) throws Exception {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Visitor Logs");

            // Header Row
            Row headerRow = sheet.createRow(0);
            String[] columns = { "Visitor Name", "Phone", "Entry Time", "Exit Time", "Purpose", "Meeting", "Gate",
                    "Status" };

            CellStyle headerStyle = workbook.createCellStyle();
            Font font = workbook.createFont();
            font.setBold(true);
            headerStyle.setFont(font);

            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerStyle);
            }

            // Data Rows
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            int rowNum = 1;
            for (VisitorLog log : logs) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(log.getVisitorName());
                row.createCell(1).setCellValue(log.getVisitorPhone());
                row.createCell(2).setCellValue(log.getEntryTime().format(fmt));
                row.createCell(3).setCellValue(log.getExitTime() != null ? log.getExitTime().format(fmt) : "-");
                row.createCell(4).setCellValue(log.getPurpose());
                row.createCell(5).setCellValue(log.getPersonToMeet());
                row.createCell(6).setCellValue(log.getGateNumber());
                row.createCell(7).setCellValue(log.getStatus());
            }

            // Autosize columns
            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // Write to file
            try (FileOutputStream fileOut = new FileOutputStream(dest)) {
                workbook.write(fileOut);
            }
        }
    }
}
