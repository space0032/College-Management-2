package com.college.api;

import com.college.dao.StudentDAO;
import com.college.dao.FacultyDAO;
import com.college.dao.CourseDAO;
import com.college.dao.DepartmentDAO;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;

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
        try {
            int students = new StudentDAO().getAllStudents().size();
            int faculty = new FacultyDAO().getAllFaculty().size();
            int courses = new CourseDAO().getAllCourses().size();
            int departments = new DepartmentDAO().getAllDepartments().size();

            String json = String.format(
                    "{\"totalStudents\":%d, \"totalFaculty\":%d, \"activeCourses\":%d, \"departments\":%d}",
                    students, faculty, courses, departments);
            sendResponse(t, 200, json);
        } catch (Exception e) {
            e.printStackTrace();
            sendResponse(t, 500, errorJson("Failed to fetch dashboard stats"));
        }
    }
}
