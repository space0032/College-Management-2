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

        // Define routes
        server.createContext("/", new RootHandler());
        server.createContext("/students", new StudentController());
        // Simple manual path checking in handler for fees pending
        server.createContext("/fees", new FeeController());

        server.setExecutor(null); // default executor
        server.start();
        System.out.println("API Server started on port 7000");
    }

    static class RootHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            String response = "College Management API (Native) is Running";
            t.sendResponseHeaders(200, response.length());
            OutputStream os = t.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }
}
