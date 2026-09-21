package com.college.utils;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.math.BigDecimal;
import java.math.RoundingMode;

public final class ManagementValidation {
    private ManagementValidation() {}
    public static JsonObject object(String body) {
        try {
            var value = JsonParser.parseString(body);
            if (!value.isJsonObject()) throw new IllegalArgumentException();
            return value.getAsJsonObject();
        } catch (RuntimeException e) { throw new ManagementException(400, "A valid JSON object is required."); }
    }
    public static int integer(JsonObject body, String key, int min, int max) {
        try {
            var value = body.get(key);
            if (value == null || !value.isJsonPrimitive() || value.getAsJsonPrimitive().isBoolean()) throw new IllegalArgumentException();
            int result = new BigDecimal(value.getAsString()).intValueExact();
            if (result < min || result > max) throw new IllegalArgumentException();
            return result;
        } catch (RuntimeException e) { throw new ManagementException(400, key + " must be a whole number between " + min + " and " + max + "."); }
    }
    public static BigDecimal money(JsonObject body, String key, BigDecimal fallback) {
        if (!body.has(key)) return fallback;
        try {
            BigDecimal value = new BigDecimal(body.get(key).getAsString()).setScale(2, RoundingMode.UNNECESSARY);
            if (value.signum() < 0 || value.compareTo(new BigDecimal("99999999.99")) > 0) throw new IllegalArgumentException();
            return value;
        } catch (RuntimeException e) { throw new ManagementException(400, key + " must be a non-negative amount with at most two decimal places."); }
    }
    public static String text(JsonObject body, String key, boolean required, int max) {
        if (!body.has(key) || body.get(key).isJsonNull()) {
            if (required) throw new ManagementException(400, key + " is required.");
            return "";
        }
        if (!body.get(key).isJsonPrimitive() || !body.get(key).getAsJsonPrimitive().isString()) throw new ManagementException(400, key + " must be text.");
        String value = body.get(key).getAsString().trim();
        if ((required && value.isEmpty()) || value.length() > max) throw new ManagementException(400, "Enter a valid " + key + " (maximum " + max + " characters).");
        return value;
    }
}
