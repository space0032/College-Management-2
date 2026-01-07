package com.college.services;

import com.college.utils.Logger;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

/**
 * Service to interact with Google Gemini API
 */
public class GeminiService {

    private static final String API_KEY = System.getenv("GOOGLE_API_KEY");
    private static final String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-flash-latest:generateContent?key="
            + (API_KEY != null ? API_KEY : "");

    private final HttpClient httpClient;

    public GeminiService() {
        this.httpClient = HttpClient.newHttpClient();
    }

    public String sendMessage(String userMessage, String context) {
        // API Key check removed as user has configured it.

        try {
            // Construct JSON Payload
            // System instruction + User Message
            String finalPrompt = "SYSTEM CONTEXT (READ ONLY - YOU CANNOT EDIT): " + context + "\n\n" +
                    "USER QUESTION: " + userMessage + "\n\n" +
                    "INSTRUCTIONS: You are a helpful AI assistant for the College Management System. " +
                    "You can see the data on the screen provided in 'SYSTEM CONTEXT'. " +
                    "You cannot edit, delete, or modify any data. You are strictly read-only. " +
                    "Answer the user's question based on the context provided.";

            JsonObject textPart = new JsonObject();
            textPart.add("text", new JsonPrimitive(finalPrompt));

            JsonArray parts = new JsonArray();
            parts.add(textPart);

            JsonObject content = new JsonObject();
            content.add("parts", parts);

            JsonArray contents = new JsonArray();
            contents.add(content);

            JsonObject payload = new JsonObject();
            payload.add("contents", contents);

            String requestBody = payload.toString();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                // Parse Response
                // { "candidates": [{ "content": { "parts": [{ "text": "Response" }] } }] }
                JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();
                JsonArray candidates = json.getAsJsonArray("candidates");
                if (candidates != null && candidates.size() > 0) {
                    JsonObject candidate = candidates.get(0).getAsJsonObject();
                    JsonObject contentObj = candidate.getAsJsonObject("content");
                    JsonArray partsArray = contentObj.getAsJsonArray("parts");
                    if (partsArray != null && partsArray.size() > 0) {
                        return partsArray.get(0).getAsJsonObject().get("text").getAsString();
                    }
                }
            } else {
                Logger.error("Gemini API Error: " + response.statusCode() + " " + response.body());
                return "Sorry, I encountered an error connecting to my brain. (Status: " + response.statusCode() + ")";
            }

        } catch (Exception e) {
            Logger.error("Gemini Service Exception", e);
            return "Error: " + e.getMessage();
        }

        return "I am speechless.";
    }
}
