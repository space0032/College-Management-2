package com.college.services;

import com.college.utils.EnvConfig;
import com.college.utils.Logger;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

/**
 * Client for the OpenRouter API (https://openrouter.ai/api/v1).
 *
 * The API key is read per-request via EnvConfig so rotation does not require
 * a restart. The key is never logged and never returned to callers.
 *
 * Primary model defaults to Ox Alpha (stealth/ox-alpha). Transport-level or
 * retryable failures fall back once to the configured Nemotron model.
 */
public class OpenRouterService {

    private static final String API_URL = "https://openrouter.ai/api/v1/chat/completions";

    private static final String DEFAULT_PRIMARY = "stealth/ox-alpha";
    private static final String DEFAULT_FALLBACK = "nvidia/nemotron-3-nano-30b-a3b";

    /** Successful chat result: model that produced it + generated text. */
    public static final class ChatResult {
        public final String model;
        public final String text;

        public ChatResult(String model, String text) {
            this.model = model;
            this.text = text;
        }
    }

    /**
     * Failure details. statusHint is the HTTP status the controller should
     * return: 503 = not configured / provider auth (do NOT retry or fall
     * back), 400 = bad caller input, 502 = provider failure (fallback OK).
     */
    public static class AiException extends Exception {
        public final int statusHint;

        public AiException(String message, int statusHint) {
            super(message);
            this.statusHint = statusHint;
        }
    }

    private final HttpClient httpClient;

    public OpenRouterService() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    /**
     * Chat with the primary model, falling back once to the secondary model
     * on retryable (transport / 429 / 5xx / empty) failures.
     */
    public ChatResult chatWithFallback(String systemPrompt, String userPrompt, double temperature)
            throws AiException {
        String apiKey = EnvConfig.get("OPENROUTER_API_KEY");
        if (apiKey == null || apiKey.isBlank() || apiKey.startsWith("your_")) {
            throw new AiException("AI assistance is not configured. Set OPENROUTER_API_KEY.", 503);
        }
        String primary = firstNonBlank(EnvConfig.get("OPENROUTER_PRIMARY_MODEL"), DEFAULT_PRIMARY);
        String fallback = firstNonBlank(EnvConfig.get("OPENROUTER_FALLBACK_MODEL"), DEFAULT_FALLBACK);
        int maxTokens = parseInt(EnvConfig.get("OPENROUTER_MAX_TOKENS"), 1500);
        long timeoutMs = parseLong(EnvConfig.get("OPENROUTER_TIMEOUT_MS"), 30000);

        try {
            return chat(apiKey, primary, systemPrompt, userPrompt, temperature, maxTokens, timeoutMs);
        } catch (AiException e) {
            // Config / auth / caller errors will not be fixed by the fallback.
            if (e.statusHint == 503 || e.statusHint == 400) {
                throw e;
            }
            Logger.error("OpenRouter primary model failed (" + primary + "): " + e.getMessage());
        }

        if (fallback.equals(primary)) {
            throw new AiException("AI provider is temporarily unavailable. Please try again.", 502);
        }
        try {
            return chat(apiKey, fallback, systemPrompt, userPrompt, temperature, maxTokens, timeoutMs);
        } catch (AiException e) {
            Logger.error("OpenRouter fallback model failed (" + fallback + "): " + e.getMessage());
            throw new AiException("AI provider is temporarily unavailable. Please try again.", 502);
        }
    }

    private ChatResult chat(String apiKey, String model, String systemPrompt, String userPrompt,
            double temperature, int maxTokens, long timeoutMs) throws AiException {
        try {
            JsonObject payload = new JsonObject();
            payload.addProperty("model", model);
            JsonArray messages = new JsonArray();
            messages.add(message("system", systemPrompt));
            messages.add(message("user", userPrompt));
            payload.add("messages", messages);
            payload.addProperty("max_tokens", maxTokens);
            payload.addProperty("temperature", temperature);
            // Ox Alpha is a mandatory-reasoning model; "low" keeps short
            // drafting tasks fast. Other models must not receive this param.
            if (model.startsWith("stealth/")) {
                payload.addProperty("reasoning_effort", "low");
            }

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .timeout(Duration.ofMillis(timeoutMs))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .header("X-Title", "College Management System")
                    .POST(HttpRequest.BodyPublishers.ofString(payload.toString(), StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            int status = response.statusCode();
            if (status == 401 || status == 403) {
                Logger.error("OpenRouter auth failure (status " + status + "). Check OPENROUTER_API_KEY.");
                throw new AiException("AI assistance is not configured correctly.", 503);
            }
            if (status == 400) {
                Logger.error("OpenRouter rejected request (400): " + truncate(response.body(), 500));
                throw new AiException("AI request was invalid. Please try again.", 400);
            }
            if (status != 200) {
                throw new AiException("Provider returned status " + status + ".", 502);
            }

            String text = extractContent(response.body());
            if (text == null || text.isBlank()) {
                throw new AiException("Provider returned an empty completion.", 502);
            }
            return new ChatResult(model, text.trim());
        } catch (AiException e) {
            throw e;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AiException("AI request was interrupted.", 502);
        } catch (Exception e) {
            // Transport-level failure (timeout, DNS, connection reset, ...).
            throw new AiException("Could not reach AI provider: " + e.getClass().getSimpleName(), 502);
        }
    }

    private static JsonObject message(String role, String content) {
        JsonObject m = new JsonObject();
        m.addProperty("role", role);
        m.addProperty("content", content);
        return m;
    }

    /** Parse OpenAI-compatible chat response: choices[0].message.content */
    private static String extractContent(String body) {
        try {
            JsonObject json = JsonParser.parseString(body).getAsJsonObject();
            JsonArray choices = json.getAsJsonArray("choices");
            if (choices == null || choices.size() == 0) {
                return null;
            }
            JsonObject message = choices.get(0).getAsJsonObject().getAsJsonObject("message");
            if (message == null || !message.has("content") || message.get("content").isJsonNull()) {
                return null;
            }
            return message.get("content").getAsString();
        } catch (Exception e) {
            Logger.error("OpenRouter response parse failure: " + truncate(body, 300));
            return null;
        }
    }

    private static String firstNonBlank(String value, String fallback) {
        return (value == null || value.isBlank()) ? fallback : value.trim();
    }

    private static int parseInt(String value, int fallback) {
        try {
            return value != null ? Integer.parseInt(value.trim()) : fallback;
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    private static long parseLong(String value, long fallback) {
        try {
            return value != null ? Long.parseLong(value.trim()) : fallback;
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    private static String truncate(String s, int max) {
        if (s == null) {
            return "";
        }
        return s.length() <= max ? s : s.substring(0, max) + "...";
    }
}
