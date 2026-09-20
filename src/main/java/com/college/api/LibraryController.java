package com.college.api;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import com.college.dao.LibraryDAO;
import com.college.models.Book;
import com.college.utils.JsonHelper;
import java.io.IOException;
import java.util.List;

public class LibraryController extends BaseController implements HttpHandler {

    private final LibraryDAO libraryDAO = new LibraryDAO();

    @Override
    public void handle(HttpExchange t) throws IOException {
        if (handleOptions(t))
            return;

        String method = t.getRequestMethod();
        String path = t.getRequestURI().getPath();

        try {
            if (path.matches(".*/library/books/\\d+")) {
                if ("DELETE".equals(method))
                    handleDeleteBook(t, path);
                else if ("PUT".equals(method))
                    handleUpdate(t, path);
                else
                    sendResponse(t, 405, errorJson("Method not allowed"));
            } else if (path.matches(".*/library/books.*")) {
                if ("GET".equals(method))
                    handleGetAll(t);
                else if ("POST".equals(method))
                    handleAdd(t);
                else
                    sendResponse(t, 405, errorJson("Method not allowed"));
            } else if (path.matches(".*/library/issues/student/[^/]+")) {
                if ("GET".equals(method))
                    handleGetIssuesByStudent(t, path);
                else
                    sendResponse(t, 405, errorJson("Method not allowed"));
            } else if (path.matches(".*/library/issues.*")) {
                if ("GET".equals(method))
                    handleGetAllIssues(t);
                else
                    sendResponse(t, 405, errorJson("Method not allowed"));
            } else if (path.matches(".*/library/issue")) {
                if ("POST".equals(method))
                    handleIssueBook(t);
                else
                    sendResponse(t, 405, errorJson("Method not allowed"));
            } else if (path.matches(".*/library/return/\\d+")) {
                if ("POST".equals(method))
                    handleReturnBook(t, path);
                else
                    sendResponse(t, 405, errorJson("Method not allowed"));
            } else if (path.matches(".*/library/fines/[^/]+")) {
                if ("GET".equals(method))
                    handleGetFines(t, path);
                else
                    sendResponse(t, 405, errorJson("Method not allowed"));
            } else if (path.matches(".*/library/send-reminders")) {
                if ("POST".equals(method))
                    handleSendReminders(t);
                else
                    sendResponse(t, 405, errorJson("Method not allowed"));
            } else {
                sendResponse(t, 404, errorJson("Not found"));
            }
        } catch (Exception e) {
            sendResponse(t, 500, errorJson(e.getMessage() != null ? e.getMessage() : "Internal server error"));
        }
    }

private void handleGetAll(HttpExchange t) throws IOException {
        if (!requirePermission(t, "VIEW_LIBRARY")) return;
        String query = t.getRequestURI().getQuery();
        int page = 0, size = 20;
        if (query != null) {
            java.util.Map<String, String> params = parseQuery(query);
            if (params.containsKey("page")) page = Integer.parseInt(params.get("page"));
            if (params.containsKey("size")) size = Integer.parseInt(params.get("size"));
        }
        List<Book> books = libraryDAO.getAllBooks(page, size);
        int total = libraryDAO.getTotalBookCount();
        sendResponse(t, 200, String.format("{\"content\":%s,\"totalElements\":%d,\"totalPages\":%d,\"number\":%d,\"size\":%d}",
                JsonHelper.toJson(books), total, (int) Math.ceil((double) total / size), page, size));
    }

    private java.util.Map<String, String> parseQuery(String query) {
        java.util.Map<String, String> params = new java.util.HashMap<>();
        for (String pair : query.split("&")) {
            String[] kv = pair.split("=", 2);
            if (kv.length == 2) params.put(kv[0], kv[1]);
        }
        return params;
    }

    private void handleAdd(HttpExchange t) throws IOException {
        if (!requirePermission(t, "CREATE_LIBRARY")) return;
        String body = readBody(t);
        Book book = JsonHelper.fromJson(body, Book.class);
        if (book == null) {
            sendResponse(t, 400, errorJson("Invalid JSON"));
            return;
        }
        boolean ok = libraryDAO.addBook(book);
        if (ok)
            sendResponse(t, 201, JsonHelper.toJson(book));
        else
            sendResponse(t, 400, errorJson("Failed to add book"));
    }

    private void handleUpdate(HttpExchange t, String path) throws IOException {
        if (!requirePermission(t, "UPDATE_LIBRARY")) return;
        int id = extractId(path);
        String body = readBody(t);
        Book book = JsonHelper.fromJson(body, Book.class);
        if (book == null) {
            sendResponse(t, 400, errorJson("Invalid JSON"));
            return;
        }
        book.setId(id);
        boolean ok = libraryDAO.updateBook(book);
        if (ok)
            sendResponse(t, 200, JsonHelper.toJson(book));
        else
            sendResponse(t, 400, errorJson("Failed to update book"));
    }

private void handleGetAllIssues(HttpExchange t) throws IOException {
        if (!requirePermission(t, "VIEW_LIBRARY")) return;
        String query = t.getRequestURI().getQuery();
        int page = 0, size = 20;
        if (query != null) {
            java.util.Map<String, String> params = parseQuery(query);
            if (params.containsKey("page")) page = Integer.parseInt(params.get("page"));
            if (params.containsKey("size")) size = Integer.parseInt(params.get("size"));
        }
        com.college.dao.BookIssueDAO issueDAO = new com.college.dao.BookIssueDAO();
        List<com.college.models.BookIssue> issues = issueDAO.getAllIssuedBooks(page, size);
        int total = issueDAO.getTotalIssuedBookCount();
        sendResponse(t, 200, String.format("{\"content\":%s,\"totalElements\":%d,\"totalPages\":%d,\"number\":%d,\"size\":%d}",
                JsonHelper.toJson(issues), total, (int) Math.ceil((double) total / size), page, size));
    }

    private void handleGetIssuesByStudent(HttpExchange t, String path) throws IOException {
        if (!requirePermission(t, "VIEW_LIBRARY")) return;
        String query = t.getRequestURI().getQuery();
        int page = 0, size = 20;
        if (query != null) {
            java.util.Map<String, String> params = parseQuery(query);
            if (params.containsKey("page")) page = Integer.parseInt(params.get("page"));
            if (params.containsKey("size")) size = Integer.parseInt(params.get("size"));
        }
        int studentId = resolvePathStudentId(path.substring(path.lastIndexOf('/') + 1));
        com.college.dao.BookIssueDAO issueDAO = new com.college.dao.BookIssueDAO();
        List<com.college.models.BookIssue> issues = issueDAO.getIssuedBooksByStudent(studentId, page, size);
        int total = issueDAO.getIssuedBookCountByStudent(studentId);
        sendResponse(t, 200, String.format("{\"content\":%s,\"totalElements\":%d,\"totalPages\":%d,\"number\":%d,\"size\":%d}",
                JsonHelper.toJson(issues), total, (int) Math.ceil((double) total / size), page, size));
    }

    private void handleIssueBook(HttpExchange t) throws IOException {
        if (!requirePermission(t, "MANAGE_LIBRARY")) return;
        String body = readBody(t);
        com.college.models.BookIssue issue = JsonHelper.fromJson(body, com.college.models.BookIssue.class);
        if (issue == null) {
            sendResponse(t, 400, errorJson("Invalid JSON"));
            return;
        }
        com.college.dao.BookIssueDAO issueDAO = new com.college.dao.BookIssueDAO();
        int studentId;
        if (issue.getEnrollmentId() != null && !issue.getEnrollmentId().trim().isEmpty()) {
            studentId = new com.college.dao.StudentDAO().getStudentIdByEnrollment(issue.getEnrollmentId().trim());
            if (studentId <= 0) {
                sendResponse(t, 400, errorJson("Unknown student for the given enrollmentId"));
                return;
            }
            issue.setStudentId(studentId);
        } else if (issue.getStudentId() <= 0) {
            sendResponse(t, 400, errorJson("studentId or enrollmentId is required"));
            return;
        }
        if (!issueDAO.isBookAvailable(issue.getBookId())) {
            sendResponse(t, 400, errorJson("Book is not available"));
            return;
        }
        if (issue.getIssueDate() == null)
            issue.setIssueDate(new java.util.Date());
        if (issue.getDueDate() == null) {
            long twoWeeks = 14L * 24 * 60 * 60 * 1000;
            issue.setDueDate(new java.util.Date(issue.getIssueDate().getTime() + twoWeeks));
        }
        boolean ok = issueDAO.issueBook(issue);
        if (ok)
            sendResponse(t, 200, "{\"message\":\"Book issued successfully\"}");
        else
            sendResponse(t, 400, errorJson("Failed to issue book"));
    }

    @SuppressWarnings("unchecked")
    private void handleReturnBook(HttpExchange t, String path) throws IOException {
        if (!requirePermission(t, "MANAGE_LIBRARY")) return;
        int issueId = extractId(path);
        String body = readBody(t);
        java.util.Map<String, Object> map = new com.google.gson.Gson().fromJson(body, java.util.Map.class);
        int returnedTo = 0;
        if (map != null && map.get("returnedTo") != null) {
            returnedTo = ((Double) map.get("returnedTo")).intValue();
        }
        com.college.dao.BookIssueDAO issueDAO = new com.college.dao.BookIssueDAO();
        boolean ok = issueDAO.returnBook(issueId, returnedTo);
        if (ok)
            sendResponse(t, 200, "{\"message\":\"Book returned successfully\"}");
        else
            sendResponse(t, 400, errorJson("Failed to return book"));
    }

    private void handleGetFines(HttpExchange t, String path) throws IOException {
        if (!requirePermission(t, "VIEW_LIBRARY")) return;
        int studentId = resolvePathStudentId(path.substring(path.lastIndexOf('/') + 1));
        com.college.dao.BookIssueDAO issueDAO = new com.college.dao.BookIssueDAO();
        double fines = issueDAO.getPendingFines(studentId);
        sendResponse(t, 200, String.format("{\"totalFines\": %.2f}", fines));
    }

    private int extractId(String path) {
        String[] parts = path.split("/");
        return Integer.parseInt(parts[parts.length - 1]);
    }

    private void handleSendReminders(HttpExchange t) throws IOException {
        if (!requirePermission(t, "MANAGE_LIBRARY")) return;
        com.college.dao.BookIssueDAO issueDAO = new com.college.dao.BookIssueDAO();
        List<com.college.models.BookIssue> overdueIssues = issueDAO.getAllIssuedBooks().stream()
                .filter(issue -> issue.getDueDate() != null && issue.getDueDate().before(new java.util.Date()))
                .toList();

        int sentCount = 0;
        com.college.dao.NotificationDAO notificationDAO = new com.college.dao.NotificationDAO();
        for (com.college.models.BookIssue issue : overdueIssues) {
            com.college.models.Notification notification = new com.college.models.Notification();
            notification.setRecipientUserId(issue.getStudentId());
            notification.setSubject("Overdue Book Reminder");
            notification.setMessage(String.format("Your book '%s' was due on %s. Please return it to avoid additional fines.", issue.getBookTitle(), issue.getDueDate()));
            notification.setType(com.college.models.Notification.Type.SYSTEM);
            notification.setStatus(com.college.models.Notification.Status.PENDING);
            notification.setCreatedAt(java.time.LocalDateTime.now());
            if (notificationDAO.createNotification(notification)) {
                sentCount++;
            }
        }
        sendResponse(t, 200, String.format("{\"sent\":%d,\"message\":\"Reminders sent to %d students with overdue books\"}", sentCount, sentCount));
    }

    private void handleDeleteBook(HttpExchange t, String path) throws IOException {
        if (!requirePermission(t, "DELETE_LIBRARY")) return;
        int id = extractId(path);
        com.college.dao.LibraryDAO libraryDAO = new com.college.dao.LibraryDAO();
        boolean ok = libraryDAO.deleteBook(id);
        if (ok)
            sendResponse(t, 200, "{\"message\":\"Book deleted successfully\"}");
        else
            sendResponse(t, 400, errorJson("Failed to delete book"));
    }
}
