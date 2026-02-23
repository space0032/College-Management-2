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
        if (handleOptions(t)) return;

        String method = t.getRequestMethod();
        String path = t.getRequestURI().getPath();

        try {
            if (path.matches(".*/library/books/\\d+")) {
                if ("PUT".equals(method)) handleUpdate(t, path);
                else sendResponse(t, 405, errorJson("Method not allowed"));
            } else if (path.matches(".*/library/books.*")) {
                if ("GET".equals(method)) handleGetAll(t);
                else if ("POST".equals(method)) handleAdd(t);
                else sendResponse(t, 405, errorJson("Method not allowed"));
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
        if (ok) sendResponse(t, 201, JsonHelper.toJson(book));
        else sendResponse(t, 400, errorJson("Failed to add book"));
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
        if (ok) sendResponse(t, 200, JsonHelper.toJson(book));
        else sendResponse(t, 400, errorJson("Failed to update book"));
    }

    private int extractId(String path) {
        String[] parts = path.split("/");
        return Integer.parseInt(parts[parts.length - 1]);
    }
}
