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
 * Client for TokenRa's OpenAI-compatible API — the primary provider in the
 * AI drafting chain (OpenRouter is the secondary).
 *
 * Route facts (tokenra.io/docs, Aug 2026): POST {base}/v1/chat/completions
 * with {@code Authorization: Bearer <key>}, model {@code stealth/ox-alpha},
 * standard Chat Completions body. Only documented fields (model, messages,
 * max_tokens, temperature) are sent; reasoning/tool params are unconfirmed
 * on this route and deliberately omitted.
 *
 * Base URL, key and model are env-configurable so route changes never need
 * a code edit. The key is never logged and never returned to callers.
 */
public class TokenRaService {

    public static final String DEFAULT_API_URL = "https://tokenra.io/v1/chat/completions";
    private static final String DEFAULT_MODEL = "stealth/ox-alpha";

    private final HttpClient httpClient;

    public TokenRaService() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    /** True when a usable key is configured; otherwise the controller skips TokenRa. */
    public static boolean isConfigured() {
        String key = EnvConfig.get("TOKENRA_API_KEY");
        return key != null && !key.isBlank() && !key.startsWith("your_");
    }

    /**
     * Single attempt against TokenRa. Any failure throws AiException and the
     * caller (AiController) fails over to OpenRouter.
     */
    public ChatResult chat(String systemPrompt, String userPrompt, double temperature)
            throws OpenRouterService.AiException {
        if (!isConfigured()) {
            throw new OpenRouterService.AiException("TokenRa is not configured.", 503);
        }
        String apiUrl = firstNonBlank(EnvConfig.get("TOKENRA_API_URL"), DEFAULT_API_URL);
        String model = firstNonBlank(EnvConfig.get("TOKENRA_MODEL"), DEFAULT_MODEL);
        int maxTokens = parseInt(EnvConfig.get("TOKENRA_MAX_TOKENS"), 1500);
        long timeoutMs = parseLong(EnvConfig.get("TOKENRA_TIMEOUT_MS"), 25000);

        try {
            JsonObject payload = new JsonObject();
            payload.addProperty("model", model);
            JsonArray messages = new JsonArray();
            messages.add(message("system", systemPrompt));
            messages.add(message("user", userPrompt));
            payload.add("messages", messages);
            payload.addProperty("max_tokens", maxTokens);
            payload.addProperty("temperature", temperature);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl))
                    .timeout(Duration.ofMillis(timeoutMs))
                    .header("Authorization", "Bearer " + EnvConfig.get("TOKENRA_API_KEY"))
                    .header("Content-Type", "application/json")
                    .header("X-Title", "College Management System")
                    .POST(HttpRequest.BodyPublishers.ofString(payload.toString(), StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            int status = response.statusCode();
            if (status == 401 || status == 403) {
                Logger.error("TokenRa auth failure (status " + status + "). Check TOKENRA_API_KEY.");
                throw new OpenRouterService.AiException("TokenRa rejected the API key.", 503);
            }
            if (status == 400) {
                Logger.error("TokenRa rejected request (400): " + truncate(response.body(), 500));
                throw new OpenRouterService.AiException("TokenRa rejected the request.", 400);
            }
            if (status != 200) {
                throw new OpenRouterService.AiException("TokenRa returned status " + status + ".", 502);
            }

            String text = extractContent(response.body());
            if (text == null || text.isBlank()) {
                throw new OpenRouterService.AiException("TokenRa returned an empty completion.", 502);
            }
            return new ChatResult("tokenra/" + model, text.trim());
        } catch (OpenRouterService.AiException e) {
            throw e;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new OpenRouterService.AiException("TokenRa request was interrupted.", 502);
        } catch (Exception e) {
            // Transport-level failure (timeout, DNS, connection reset, ...).
            throw new OpenRouterService.AiException(
                    "Could not reach TokenRa: " + e.getClass().getSimpleName(), 502);
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
            Logger.error("TokenRa response parse failure: " + truncate(body, 300));
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
