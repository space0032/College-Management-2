package com.college.api;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import com.college.utils.MigrationRunner;
import com.college.dao.AuditLogDAO;

public class ApiServer {

    public static void main(String[] args) throws IOException {
        MigrationRunner.runMigrations();

        int port = resolvePort(System.getenv("PORT"));
        HttpServer server = HttpServer.create(new InetSocketAddress("0.0.0.0", port), 0);

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
        server.createContext("/api/assignments", new ProtectedHandler(new AssignmentController()));
        server.createContext("/api/submissions", new ProtectedHandler(new AssignmentController()));
        server.createContext("/api/campaigns", new ProtectedHandler(new CrowdfundingController()));
        server.createContext("/api/settings", new ProtectedHandler(new SettingsController()));
        server.createContext("/api/resources", new ProtectedHandler(new ResourceController()));
        server.createContext("/api/employees", new ProtectedHandler(new EmployeeController()));
        server.createContext("/api/leaves", new ProtectedHandler(new LeaveController()));
        server.createContext("/api/rooms", new ProtectedHandler(new RoomController()));
        server.createContext("/api/workload", new ProtectedHandler(new WorkloadController()));
        server.createContext("/api/payroll", new ProtectedHandler(new PayrollController()));
        server.createContext("/api/syllabus", new ProtectedHandler(new SyllabusController()));
        server.createContext("/api/volunteers", new ProtectedHandler(new VolunteerController()));
        server.createContext("/api/audit", new ProtectedHandler(new AuditController()));

        server.createContext("/api/complaints", new ProtectedHandler(new ComplaintController()));
        server.createContext("/api/hostel/attendance", new ProtectedHandler(new HostelAttendanceController()));
        server.createContext("/api/wardens", new ProtectedHandler(new WardenController()));
        server.createContext("/api/course-registrations", new ProtectedHandler(new CourseRegistrationController()));
        server.createContext("/api/feedback", new ProtectedHandler(new FeedbackController()));
        server.createContext("/api/book-requests", new ProtectedHandler(new BookRequestController()));
        server.createContext("/api/fee-transactions", new ProtectedHandler(new FeeTransactionController()));
        server.createContext("/api/event-details", new ProtectedHandler(new EventDetailsController()));

        server.setExecutor(null);
        server.start();
        System.out.println("API Server started on port " + port);
    }

    static int resolvePort(String configuredPort) {
        if (configuredPort == null || configuredPort.isBlank()) {
            return 7000;
        }
        try {
            int port = Integer.parseInt(configuredPort);
            if (port < 1 || port > 65535) {
                throw new IllegalArgumentException("PORT must be between 1 and 65535");
            }
            return port;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("PORT must be a valid integer", e);
        }
    }

    static class RootHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            // Handle CORS preflight for root
            if ("OPTIONS".equals(t.getRequestMethod())) {
                CorsSupport.addHeaders(t);
                t.sendResponseHeaders(CorsSupport.isOriginAllowed(t) ? 204 : 403, -1);
                t.getResponseBody().close();
                return;
            }
            String response = "{\"status\":\"College Management API is running\",\"version\":\"2.0\"}";
            t.getResponseHeaders().set("Content-Type", "application/json");
            CorsSupport.addHeaders(t);
            if ("HEAD".equals(t.getRequestMethod())) {
                t.sendResponseHeaders(200, -1);
                t.getResponseBody().close();
                return;
            }
            byte[] bytes = response.getBytes(java.nio.charset.StandardCharsets.UTF_8);
            t.sendResponseHeaders(200, bytes.length);
            OutputStream os = t.getResponseBody();
            os.write(bytes);
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
                CorsSupport.addHeaders(t);
                t.sendResponseHeaders(CorsSupport.isOriginAllowed(t) ? 204 : 403, -1);
                t.getResponseBody().close();
                return;
            }

            String auth = t.getRequestHeaders().getFirst("Authorization");
            if (auth == null || !auth.startsWith("Bearer ")) {
                String resp = "{\"error\":\"Unauthorized - Token required\"}";
                t.getResponseHeaders().set("Content-Type", "application/json");
                CorsSupport.addHeaders(t);
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
                CorsSupport.addHeaders(t);
                byte[] bytes = resp.getBytes();
                t.sendResponseHeaders(401, bytes.length);
                t.getResponseBody().write(bytes);
                t.getResponseBody().close();
                return;
            }
            delegate.handle(t);
            recordSuccessfulMutation(t, info);
        }

        private void recordSuccessfulMutation(HttpExchange exchange, TokenStore.TokenInfo info) {
            String method = exchange.getRequestMethod();
            String action = switch (method) {
                case "POST" -> "CREATE";
                case "PUT", "PATCH" -> "UPDATE";
                case "DELETE" -> "DELETE";
                default -> null;
            };
            int status = exchange.getResponseCode();
            if (action == null || status < 200 || status >= 300) {
                return;
            }

            String path = exchange.getRequestURI().getPath();
            String entityType = path.replaceFirst("^/api/", "")
                    .replaceAll("/.*$", "")
                    .replace('-', '_')
                    .toUpperCase(java.util.Locale.ROOT);
            Integer entityId = null;
            String[] segments = path.split("/");
            for (int i = segments.length - 1; i >= 0; i--) {
                try {
                    entityId = Integer.valueOf(segments[i]);
                    break;
                } catch (NumberFormatException ignored) {
                    // Keep looking for a numeric resource identifier.
                }
            }
            AuditLogDAO.logAction(info.userId, info.username, action, entityType, entityId,
                    method + " " + path + " completed with HTTP " + status);
        }
    }
}
