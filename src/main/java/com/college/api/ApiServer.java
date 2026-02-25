package com.college.api;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

public class ApiServer {

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(7000), 0);

        // Public endpoints
        server.createContext("/", new RootHandler());
        server.createContext("/api/auth/", new AuthController());

        // Protected endpoints
        server.createContext("/api/students", new ProtectedHandler(new StudentController()));
        server.createContext("/api/fees", new ProtectedHandler(new FeeController()));
        server.createContext("/api/faculty", new ProtectedHandler(new FacultyController()));
        server.createContext("/api/courses", new ProtectedHandler(new CourseController()));
        server.createContext("/api/attendance", new ProtectedHandler(new AttendanceController()));
        server.createContext("/api/library", new ProtectedHandler(new LibraryController()));
        server.createContext("/api/timetable", new ProtectedHandler(new TimetableController()));
        server.createContext("/api/placements", new ProtectedHandler(new PlacementController()));
        server.createContext("/api/hostels", new ProtectedHandler(new HostelController()));
        server.createContext("/api/announcements", new ProtectedHandler(new AnnouncementController()));
        server.createContext("/api/notifications", new ProtectedHandler(new NotificationController()));
        server.createContext("/api/departments", new ProtectedHandler(new DepartmentController()));
        server.createContext("/api/roles", new ProtectedHandler(new RoleController()));
        server.createContext("/api/users", new ProtectedHandler(new UserController()));
        server.createContext("/api/dashboard/stats", new ProtectedHandler(new DashboardController()));
        server.createContext("/api/clubs", new ProtectedHandler(new ClubController()));
        server.createContext("/api/events", new ProtectedHandler(new EventController()));
        server.createContext("/api/grades", new ProtectedHandler(new GradeController()));
        server.createContext("/api/reports", new ProtectedHandler(new ReportController()));
        server.createContext("/api/gatepass", new ProtectedHandler(new GatePassController()));
        server.createContext("/api/visitors", new ProtectedHandler(new VisitorController()));
        server.createContext("/api/calendar", new ProtectedHandler(new CalendarController()));
        server.createContext("/api/scholarships", new ProtectedHandler(new ScholarshipController()));

        server.setExecutor(null);
        server.start();
        System.out.println("API Server started on port 7000");
    }

    static class RootHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            // Handle CORS preflight for root
            if ("OPTIONS".equals(t.getRequestMethod())) {
                t.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
                t.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
                t.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, Authorization");
                t.sendResponseHeaders(204, -1);
                t.getResponseBody().close();
                return;
            }
            String response = "{\"status\":\"College Management API is running\",\"version\":\"2.0\"}";
            t.getResponseHeaders().set("Content-Type", "application/json");
            t.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
            t.sendResponseHeaders(200, response.length());
            OutputStream os = t.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }

    static class ProtectedHandler implements HttpHandler {
        private final HttpHandler delegate;

        public ProtectedHandler(HttpHandler delegate) {
            this.delegate = delegate;
        }

        @Override
        public void handle(HttpExchange t) throws IOException {
            // Handle CORS preflight
            if ("OPTIONS".equals(t.getRequestMethod())) {
                t.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
                t.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
                t.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, Authorization");
                t.sendResponseHeaders(204, -1);
                t.getResponseBody().close();
                return;
            }

            String auth = t.getRequestHeaders().getFirst("Authorization");
            if (auth == null || !auth.startsWith("Bearer ")) {
                String resp = "{\"error\":\"Unauthorized - Token required\"}";
                t.getResponseHeaders().set("Content-Type", "application/json");
                t.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
                byte[] bytes = resp.getBytes();
                t.sendResponseHeaders(401, bytes.length);
                t.getResponseBody().write(bytes);
                t.getResponseBody().close();
                return;
            }

            TokenStore.TokenInfo info = TokenStore.getTokenInfo(auth.substring(7));
            if (info == null) {
                String resp = "{\"error\":\"Unauthorized - Invalid or expired token\"}";
                t.getResponseHeaders().set("Content-Type", "application/json");
                t.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
                byte[] bytes = resp.getBytes();
                t.sendResponseHeaders(401, bytes.length);
                t.getResponseBody().write(bytes);
                t.getResponseBody().close();
                return;
            }
            delegate.handle(t);
        }
    }
}
