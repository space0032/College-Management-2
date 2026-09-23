package com.college.api;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import com.college.services.ReportDataService;
import com.college.services.PdfReportGenerator;
import com.college.models.VisitorLog;
import com.college.utils.JsonHelper;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class ReportController extends BaseController implements HttpHandler {

    private final ReportDataService reportService = new ReportDataService();
    private final PdfReportGenerator pdfGenerator = new PdfReportGenerator();

    @Override
    public void handle(HttpExchange t) throws IOException {
        if (handleOptions(t))
            return;

        String method = t.getRequestMethod();
        String path = t.getRequestURI().getPath();

        try {
            if (path.matches(".*/reports/visitors/pdf")) {
                if ("POST".equals(method))
                    handleGenerateVisitorPdf(t);
                else
                    sendResponse(t, 405, errorJson("Method not allowed"));
            } else if (path.matches(".*/reports/placements/stats")) {
                if ("GET".equals(method))
                    handleGetPlacementStats(t);
                else
                    sendResponse(t, 405, errorJson("Method not allowed"));
            } else {
                sendResponse(t, 404, errorJson("Not found"));
            }
        } catch (Exception e) {
            sendResponse(t, 500, errorJson(e.getMessage() != null ? e.getMessage() : "Internal server error"));
        }
    }

@SuppressWarnings("unchecked")
    private void handleGenerateVisitorPdf(HttpExchange t) throws IOException {
        if (!requireAnyPermission(t, "MANAGE_REPORT", "VIEW_REPORT")) return;
        String body = readBody(t);
        Map<String, String> map = new com.google.gson.Gson().fromJson(body, Map.class);

        if (map == null || map.get("startDate") == null || map.get("endDate") == null) {
            sendResponse(t, 400, errorJson("startDate and endDate are required (YYYY-MM-DD)"));
            return;
        }

        File tempPdf = null;
        try {
            LocalDate start = LocalDate.parse(map.get("startDate"));
            LocalDate end = LocalDate.parse(map.get("endDate"));
            if (end.isBefore(start)) {
                sendResponse(t, 400, errorJson("endDate must not be before startDate"));
                return;
            }

            List<VisitorLog> logs = reportService.getVisitorLogs(start, end);

            tempPdf = File.createTempFile("visitor_report_", ".pdf");
            pdfGenerator.generateVisitorLogPdf(logs, tempPdf);

            // Avoid leaking the absolute temp path and clean up the file.
            sendResponse(t, 200, "{\"message\":\"PDF generated successfully\", \"count\":" + logs.size() + "}");

        } catch (Exception e) {
            sendResponse(t, 500, errorJson("Failed to generate PDF"));
        } finally {
            if (tempPdf != null && tempPdf.exists()) {
                // Scheduling deletion since native file handles may linger briefly.
                tempPdf.deleteOnExit();
            }
        }
    }

    private void handleGetPlacementStats(HttpExchange t) throws IOException {
        if (!requirePermission(t, "VIEW_REPORT")) return;
        ReportDataService.PlacementStats stats = reportService.getPlacementStats();
        sendResponse(t, 200, JsonHelper.toJson(stats));
    }
}
