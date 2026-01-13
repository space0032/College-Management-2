package com.college.services;

import com.college.models.VisitorLog;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.UnitValue;

import java.io.File;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class PdfReportGenerator {

    public void generateVisitorLogPdf(List<VisitorLog> logs, File dests) throws Exception {
        PdfWriter writer = new PdfWriter(dests);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        document.add(new Paragraph("Visitor Log Report").setBold().setFontSize(18));
        document.add(new Paragraph("Generated on: " + java.time.LocalDate.now()));

        Table table = new Table(UnitValue.createPercentArray(new float[] { 3, 3, 3, 4, 3, 2 }));
        table.setWidth(UnitValue.createPercentValue(100));

        // Header
        table.addHeaderCell("Visitor Name");
        table.addHeaderCell("Phone");
        table.addHeaderCell("Entry Time");
        table.addHeaderCell("Purpose");
        table.addHeaderCell("Meeting");
        table.addHeaderCell("Status");

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        for (VisitorLog log : logs) {
            table.addCell(log.getVisitorName());
            table.addCell(log.getVisitorPhone());
            table.addCell(log.getEntryTime().format(fmt));
            table.addCell(log.getPurpose());
            table.addCell(log.getPersonToMeet());
            table.addCell(log.getStatus());
        }

        document.add(table);
        document.close();
    }
}
