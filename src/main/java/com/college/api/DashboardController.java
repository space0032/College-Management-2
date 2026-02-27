package com.college.api;

import com.college.dao.StudentDAO;
import com.college.dao.FacultyDAO;
import com.college.dao.CourseDAO;
import com.college.dao.DepartmentDAO;
import com.college.dao.EnhancedFeeDAO;
import com.college.models.FeePayment;
import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DashboardController extends BaseController implements HttpHandler {

    @Override
    public void handle(HttpExchange t) throws IOException {
        if (handleOptions(t))
            return;

        try {
            if ("GET".equals(t.getRequestMethod())) {
                String path = t.getRequestURI().getPath();
                if (path.equals("/api/dashboard/stats")) {
                    getStats(t);
                } else {
                    sendResponse(t, 404, errorJson("Endpoint not found"));
                }
            } else {
                sendResponse(t, 405, errorJson("Method not allowed"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendResponse(t, 500, errorJson("Internal server error: " + e.getMessage()));
        }
    }

    private void getStats(HttpExchange t) throws IOException {
        if (!requireAuth(t))
            return;
        try {
            StudentDAO studentDAO = new StudentDAO();
            FacultyDAO facultyDAO = new FacultyDAO();
            CourseDAO courseDAO = new CourseDAO();
            DepartmentDAO deptDAO = new DepartmentDAO();

            Map<String, Object> stats = new HashMap<>();
            stats.put("totalStudents", studentDAO.getTotalCount());
            stats.put("totalFaculty", facultyDAO.getTotalFacultyCount());
            stats.put("activeCourses", courseDAO.getTotalCount());
            stats.put("departments", deptDAO.getTotalCount());

            // Trends
            stats.put("studentsThisWeek", studentDAO.getWeeklyCount());
            stats.put("facultyThisWeek", facultyDAO.getWeeklyCount());
            stats.put("coursesThisWeek", courseDAO.getWeeklyCount());

            // Admin tier finance stats
            TokenStore.TokenInfo info = getTokenInfo(t);
            if (info != null && "ADMIN".equalsIgnoreCase(info.role)) {
                EnhancedFeeDAO feeDAO = new EnhancedFeeDAO();
                double totalBilled = feeDAO.getTotalBilledAmount();
                double totalPaid = totalBilled - feeDAO.getTotalPendingAmount();
                double collectionRate = totalBilled > 0 ? (totalPaid / totalBilled) * 100 : 0;

                stats.put("projectedRevenue", totalBilled);
                stats.put("collectionRate", Math.round(collectionRate * 10.0) / 10.0);
                stats.put("todaysCollection", feeDAO.getTodaysCollectionAmount());

                List<FeePayment> recent = feeDAO.getRecentPayments(5);
                stats.put("recentCollections", recent);
            }

            Gson gson = new Gson();
            sendResponse(t, 200, gson.toJson(stats));
        } catch (Exception e) {
            e.printStackTrace();
            sendResponse(t, 500, errorJson("Failed to fetch dashboard stats"));
        }
    }
}
