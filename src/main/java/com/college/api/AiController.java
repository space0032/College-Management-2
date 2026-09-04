package com.college.api;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import com.college.dao.AuditLogDAO;
import com.college.services.ChatResult;
import com.college.services.OpenRouterService;
import com.college.services.TokenRaService;
import com.college.utils.Logger;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayDeque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * AI drafting proxy (OpenRouter). The API key never leaves the server.
 *
 * POST /api/ai/generate  { feature, ...fields } -> { text, model, feature }
 * GET  /api/ai/models    -> { primary, fallback }
 *
 * Supported features (draft-only; nothing is saved by this controller):
 *   "assignment"   (F1, requires USE_AI_FACULTY)  - assignment questions + rubric
 *   "announcement" (A2, requires USE_AI_ADMIN)     - announcement title + body
 *
 * Only whitelisted fields are forwarded to the provider; anything else in
 * the request body is ignored so student PII can never be smuggled through.
 */
public class AiController extends BaseController implements HttpHandler {

    private static final int MAX_PROMPT_CHARS = 2000;
    private static final int RATE_LIMIT_PER_HOUR = 30;
    private static final long RATE_WINDOW_MS = 3600_000L;

    private static final ConcurrentHashMap<Integer, ArrayDeque<Long>> HITS = new ConcurrentHashMap<>();

    private final OpenRouterService ai = new OpenRouterService();
    private final TokenRaService tokenRa = new TokenRaService();

    @Override
    public void handle(HttpExchange t) throws IOException {
        if (handleOptions(t)) return;

        String method = t.getRequestMethod();
        String path = t.getRequestURI().getPath();

        try {
            if (path.endsWith("/ai/models") && "GET".equals(method)) {
                handleModels(t);
            } else if (path.endsWith("/ai/generate") && "POST".equals(method)) {
                handleGenerate(t);
            } else {
                sendResponse(t, 405, errorJson("Method not allowed"));
            }
        } catch (Exception e) {
            sendResponse(t, 500, errorJson("Internal server error"));
        }
    }

    private void handleModels(HttpExchange t) throws IOException {
        if (!requireAuth(t)) return;
        TokenStore.TokenInfo token = getTokenInfo(t);
        boolean canFaculty = hasQuietPermission(token.userId, "USE_AI_FACULTY");
        boolean canAdmin = hasQuietPermission(token.userId, "USE_AI_ADMIN");
        sendResponse(t, 200, JSON.toJson(Map.of(
                "assignment", canFaculty,
                "announcement", canAdmin)));
    }

    @SuppressWarnings("unchecked")
    private void handleGenerate(HttpExchange t) throws IOException {
        Map<String, Object> body;
        try {
            body = JSON.fromJson(readBody(t), Map.class);
        } catch (Exception e) {
            sendResponse(t, 400, errorJson("Invalid JSON"));
            return;
        }
        if (body == null) {
            sendResponse(t, 400, errorJson("Invalid JSON"));
            return;
        }

        String feature = str(body.get("feature"));
        String systemPrompt;
        String userPrompt;
        double temperature;

        if ("assignment".equals(feature)) {
            if (!requirePermission(t, "USE_AI_FACULTY")) return;
            String topic = capped(str(body.get("topic")), 500);
            String courseName = capped(str(body.get("courseName")), 200);
            int count = intValue(body.get("count"), 5, 1, 10);
            if (topic.isEmpty()) {
                sendResponse(t, 400, errorJson("Field 'topic' is required."));
                return;
            }
            systemPrompt = "You are an assistant helping college faculty draft assignments. "
                    + "Given a course name, a topic, and a number of questions, produce: "
                    + "1) one concise assignment title on a line starting with 'Title: ', "
                    + "2) the numbered questions with marks and clear instructions for undergraduate students, "
                    + "3) a short marking rubric in a section starting with 'Rubric:'. "
                    + "Plain text only. No markdown code fences, no placeholders like [insert].";
            userPrompt = "Course: " + (courseName.isEmpty() ? "(not specified)" : courseName)
                    + "\nTopic: " + topic
                    + "\nNumber of questions: " + count
                    + "\nDistribute 100 marks sensibly across the questions.";
            temperature = 0.7;
        } else if ("announcement".equals(feature)) {
            if (!requirePermission(t, "USE_AI_ADMIN")) return;
            String bullets = capped(str(body.get("bullets")), 1000);
            String tone = str(body.get("tone"));
            if (!tone.equals("formal") && !tone.equals("friendly") && !tone.equals("urgent")) {
                tone = "formal";
            }
            String audience = capped(str(body.get("audience")), 50);
            if (bullets.isEmpty()) {
                sendResponse(t, 400, errorJson("Field 'bullets' is required."));
                return;
            }
            systemPrompt = "You are an assistant drafting official college announcements. "
                    + "Given key points, an audience, and a tone, produce: "
                    + "1) a short headline on a line starting with 'Title: ', "
                    + "2) the announcement body of at most 120 words matching the requested tone. "
                    + "Plain text only. No placeholders like [date] or [name]; write complete sentences.";
            userPrompt = "Audience: " + (audience.isEmpty() ? "ALL" : audience)
                    + "\nTone: " + tone
                    + "\nKey points:\n" + bullets;
            temperature = 0.8;
        } else {
            sendResponse(t, 400, errorJson("Unknown feature. Expected 'assignment' or 'announcement'."));
            return;
        }

        if (userPrompt.length() > MAX_PROMPT_CHARS) {
            sendResponse(t, 400, errorJson("Prompt too long (max " + MAX_PROMPT_CHARS + " characters)."));
            return;
        }

        TokenStore.TokenInfo token = getTokenInfo(t);
        if (token == null) {
            sendResponse(t, 401, errorJson("Unauthorized: Missing or invalid token"));
            return;
        }
        if (!checkRateLimit(token.userId)) {
            sendResponse(t, 429, errorJson("Too many AI requests. Please wait a while and retry."));
            return;
        }

        // Provider chain: TokenRa (primary) -> OpenRouter (secondary).
        // TokenRa is skipped when no key is configured; any TokenRa failure
        // fails over to OpenRouter. Only the OpenRouter error reaches the client.
        ChatResult result = null;
        String provider = null;
        if (TokenRaService.isConfigured()) {
            try {
                result = tokenRa.chat(systemPrompt, userPrompt, temperature);
                provider = "tokenra";
            } catch (OpenRouterService.AiException e) {
                Logger.error("TokenRa primary failed, failing over to OpenRouter: " + e.getMessage());
            }
        }
        if (result == null) {
            try {
                result = ai.chatWithFallback(systemPrompt, userPrompt, temperature);
                provider = "openrouter";
            } catch (OpenRouterService.AiException e) {
                sendResponse(t, e.statusHint, errorJson(e.getMessage()));
                return;
            }
        }

        // Audit the usage (hash only — never prompt text; providers may retain prompts).
        AuditLogDAO.logAction(token.userId, token.username, "AI_GENERATE", "AI", null,
                feature + " provider=" + provider + " model=" + result.model
                        + " prompt_sha=" + sha256hex(userPrompt));

        sendResponse(t, 200, JSON.toJson(Map.of(
                "text", result.text,
                "model", result.model,
                "provider", provider,
                "feature", feature)));
    }

    private boolean hasQuietPermission(int userId, String code) {
        try {
            return com.college.utils.PermissionService.getInstance().hasPermission(userId, code);
        } catch (Exception e) {
            return false;
        }
    }

    private boolean checkRateLimit(int userId) {
        long now = System.currentTimeMillis();
        ArrayDeque<Long> dq = HITS.computeIfAbsent(userId, k -> new ArrayDeque<>());
        synchronized (dq) {
            while (!dq.isEmpty() && now - dq.peekFirst() > RATE_WINDOW_MS) {
                dq.pollFirst();
            }
            if (dq.size() >= RATE_LIMIT_PER_HOUR) {
                return false;
            }
            dq.addLast(now);
            return true;
        }
    }

    private static String str(Object o) {
        return o == null ? "" : String.valueOf(o).trim();
    }

    private static String capped(String s, int max) {
        if (s == null) {
            return "";
        }
        s = s.trim();
        return s.length() <= max ? s : s.substring(0, max);
    }

    private static int intValue(Object o, int fallback, int min, int max) {
        try {
            int v = o instanceof Number ? ((Number) o).intValue() : Integer.parseInt(String.valueOf(o).trim());
            return Math.min(max, Math.max(min, v));
        } catch (Exception e) {
            return fallback;
        }
    }

    private static String sha256hex(String s) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(s.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.substring(0, 16);
        } catch (Exception e) {
            return "n/a";
        }
    }
}
