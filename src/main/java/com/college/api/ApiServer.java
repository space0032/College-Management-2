package com.college.api;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

public class ApiServer {

    public static void main(String[] args) throws IOException {
        // Create server on port 7000
        HttpServer server = HttpServer.create(new InetSocketAddress(7000), 0);

        // Define routes (Protected)
        server.createContext("/", new RootHandler()); // Public
        server.createContext("/students", new ProtectedHandler(new StudentController()));
        server.createContext("/fees", new ProtectedHandler(new FeeController()));

        server.setExecutor(null); // default executor
        server.start();
        System.out.println("API Server started on port 7000");
    }

    static class RootHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            // Public endpoint check
            String response = "College Management API (Native) is Running. Use /students, /fees endpoints.";
            t.sendResponseHeaders(200, response.length());
            OutputStream os = t.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }

    // Wrapper to enforce Auth on other handlers
    static class ProtectedHandler implements HttpHandler {
        private final HttpHandler delegate;

        public ProtectedHandler(HttpHandler delegate) {
            this.delegate = delegate;
        }

        @Override
        public void handle(HttpExchange t) throws IOException {
            if (!ApiAuthMiddleware.isAuthenticated(t)) {
                ApiAuthMiddleware.sendUnauthorized(t);
                return;
            }
            delegate.handle(t);
        }
    }
}
