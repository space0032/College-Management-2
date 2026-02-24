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
                if ("PUT".equals(method))
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
            } else if (path.matches(".*/library/issues/student/\\d+")) {
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
            } else if (path.matches(".*/library/fines/\\d+")) {
                if ("GET".equals(method))
                    handleGetFines(t, path);
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
        List<Book> books = libraryDAO.getAllBooks();
        sendResponse(t, 200, JsonHelper.toJson(books));
    }

    private void handleAdd(HttpExchange t) throws IOException {
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
        com.college.dao.BookIssueDAO issueDAO = new com.college.dao.BookIssueDAO();
        sendResponse(t, 200, JsonHelper.toJson(issueDAO.getAllIssuedBooks()));
    }

    private void handleGetIssuesByStudent(HttpExchange t, String path) throws IOException {
        int studentId = extractId(path);
        com.college.dao.BookIssueDAO issueDAO = new com.college.dao.BookIssueDAO();
        sendResponse(t, 200, JsonHelper.toJson(issueDAO.getIssuedBooksByStudent(studentId)));
    }

    private void handleIssueBook(HttpExchange t) throws IOException {
        String body = readBody(t);
        com.college.models.BookIssue issue = JsonHelper.fromJson(body, com.college.models.BookIssue.class);
        if (issue == null) {
            sendResponse(t, 400, errorJson("Invalid JSON"));
            return;
        }
        com.college.dao.BookIssueDAO issueDAO = new com.college.dao.BookIssueDAO();
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
        int studentId = extractId(path);
        com.college.dao.BookIssueDAO issueDAO = new com.college.dao.BookIssueDAO();
        double fines = issueDAO.getPendingFines(studentId);
        sendResponse(t, 200, String.format("{\"totalFines\": %.2f}", fines));
    }

    private int extractId(String path) {
        String[] parts = path.split("/");
        return Integer.parseInt(parts[parts.length - 1]);
    }
}
