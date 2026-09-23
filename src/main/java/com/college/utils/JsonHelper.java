package com.college.utils;

import java.lang.reflect.Field;
import java.util.List;

/**
 * Minimal JSON Helper to avoid external dependencies.
 * Capable of serializing simple objects/lists and deserializing flat objects.
 */
public class JsonHelper {

    public static String toJson(Object obj) {
        if (obj == null)
            return "null";
        if (obj instanceof java.util.Collection) {
            java.util.Collection<?> col = (java.util.Collection<?>) obj;
            StringBuilder sb = new StringBuilder("[");
            int curr = 0;
            int sz = col.size();
            for (Object item : col) {
                sb.append(toJson(item));
                if (curr < sz - 1)
                    sb.append(",");
                curr++;
            }
            sb.append("]");
            return sb.toString();
        }
        if (obj instanceof String) {
            return "\"" + escape((String) obj) + "\"";
        }
        if (obj instanceof Number || obj instanceof Boolean) {
            return obj.toString();
        }
        if (obj instanceof Enum<?>) {
            return "\"" + escape(((Enum<?>) obj).name()) + "\"";
        }
        if (obj instanceof java.util.Date) {
            return "\"" + formatDate((java.util.Date) obj) + "\"";
        }
        if (obj instanceof java.time.LocalDate || obj instanceof java.time.LocalDateTime) {
            return "\"" + obj.toString() + "\"";
        }
        if (obj instanceof java.util.Map) {
            java.util.Map<?, ?> map = (java.util.Map<?, ?>) obj;
            StringBuilder sb = new StringBuilder("{");
            boolean first = true;
            for (java.util.Map.Entry<?, ?> e : map.entrySet()) {
                if (!first) sb.append(",");
                first = false;
                sb.append("\"").append(escape(String.valueOf(e.getKey()))).append("\":");
                sb.append(toJson(e.getValue()));
            }
            sb.append("}");
            return sb.toString();
        }
        // Object serialization (Refection)
        StringBuilder sb = new StringBuilder("{");
        Field[] fields = obj.getClass().getDeclaredFields();
        for (int i = 0; i < fields.length; i++) {
            fields[i].setAccessible(true);
            try {
                String name = fields[i].getName();
                Object value = fields[i].get(obj);
                sb.append("\"").append(name).append("\":");
                sb.append(toJson(value));
                if (i < fields.length - 1)
                    sb.append(",");
            } catch (IllegalAccessException e) {
                // skip
            }
        }
        sb.append("}");
        return sb.toString();
    }

    private static String escape(String s) {
        if (s == null) return "null";
        StringBuilder sb = new StringBuilder(s.length() + 16);
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"': sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '\n': sb.append("\\n"); break;
                case '\r': sb.append("\\r"); break;
                case '\t': sb.append("\\t"); break;
                case '\b': sb.append("\\b"); break;
                case '\f': sb.append("\\f"); break;
                default:
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
            }
        }
        return sb.toString();
    }

    private static String formatDate(java.util.Date date) {
        try {
            java.text.SimpleDateFormat fmt = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
            return fmt.format(date);
        } catch (Exception e) {
            return date.toString();
        }
    }

    // Very basic Parser for flat JSON: {"key":"value", "num":123}
    public static <T> T fromJson(String json, Class<T> clazz) {
        try {
            T obj = clazz.getDeclaredConstructor().newInstance();
            json = json.trim();
            if (json.startsWith("{") && json.endsWith("}")) {
                json = json.substring(1, json.length() - 1).trim();
                List<String> pairs = splitTopLevel(json);
                for (String pair : pairs) {
                    pair = pair.trim();
                    if (pair.isEmpty()) continue;
                    int colon = indexOfColon(pair);
                    if (colon < 0) continue;
                    String key = pair.substring(0, colon).trim();
                    key = stripQuotes(key);
                    String value = pair.substring(colon + 1).trim();
                    setValue(obj, key, value);
                }
            }
            return obj;
        } catch (Exception e) {
            Logger.error("Error deserializing JSON", e);
        }
        return null;
    }

    /**
     * Split a JSON object body into top-level key:value pairs. Commas that
     * appear inside quoted strings (including within URLs/times) are not
     * treated as separators.
     */
    private static List<String> splitTopLevel(String body) {
        List<String> pairs = new java.util.ArrayList<>();
        boolean inQuotes = false;
        boolean escaped = false;
        StringBuilder current = new StringBuilder();
        for (int i = 0; i < body.length(); i++) {
            char c = body.charAt(i);
            if (escaped) {
                current.append(c);
                escaped = false;
                continue;
            }
            if (c == '\\') {
                current.append(c);
                escaped = true;
                continue;
            }
            if (c == '"') {
                inQuotes = !inQuotes;
                current.append(c);
                continue;
            }
            if (c == ',' && !inQuotes) {
                pairs.add(current.toString());
                current.setLength(0);
                continue;
            }
            current.append(c);
        }
        if (current.length() > 0) {
            pairs.add(current.toString());
        }
        return pairs;
    }

    /**
     * Find the first colon that separates a key from its value (ignores colons
     * that appear inside quoted values such as "http://...").
     */
    private static int indexOfColon(String pair) {
        boolean inQuotes = false;
        boolean escaped = false;
        for (int i = 0; i < pair.length(); i++) {
            char c = pair.charAt(i);
            if (escaped) {
                escaped = false;
                continue;
            }
            if (c == '\\') {
                escaped = true;
                continue;
            }
            if (c == '"') {
                inQuotes = !inQuotes;
                continue;
            }
            if (c == ':' && !inQuotes) {
                return i;
            }
        }
        return -1;
    }

    private static String stripQuotes(String s) {
        s = s.trim();
        if (s.length() >= 2 && s.startsWith("\"") && s.endsWith("\"")) {
            return unescapeJsonString(s.substring(1, s.length() - 1));
        }
        return s;
    }

private static void setValue(Object obj, String key, String valueVal) {
        try {
            Field field = obj.getClass().getDeclaredField(key);
            field.setAccessible(true);
            if (field.getType() == String.class) {
                if (valueVal.startsWith("\"") && valueVal.endsWith("\"")) {
                    field.set(obj, unescapeJsonString(valueVal.substring(1, valueVal.length() - 1)));
                } else {
                    field.set(obj, unescapeJsonString(valueVal));
                }
            } else if (field.getType() == int.class || field.getType() == Integer.class) {
                field.set(obj, Integer.parseInt(unescapeJsonString(unquote(valueVal))));
            } else if (field.getType() == double.class || field.getType() == Double.class) {
                field.set(obj, Double.parseDouble(unescapeJsonString(unquote(valueVal))));
            } else if (field.getType() == boolean.class || field.getType() == Boolean.class) {
                field.set(obj, Boolean.parseBoolean(unescapeJsonString(unquote(valueVal))));
            } else if (field.getType() == java.time.LocalDate.class) {
                if (valueVal.startsWith("\"") && valueVal.endsWith("\""))
                    valueVal = valueVal.substring(1, valueVal.length() - 1);
                field.set(obj, java.time.LocalDate.parse(unescapeJsonString(valueVal)));
            } else if (field.getType() == java.time.LocalDateTime.class) {
                if (valueVal.startsWith("\"") && valueVal.endsWith("\""))
                    valueVal = valueVal.substring(1, valueVal.length() - 1);
                field.set(obj, java.time.LocalDateTime.parse(unescapeJsonString(valueVal).replace(" ", "T")));
} else if (field.getType() == java.util.Date.class) {
                String value = unescapeJsonString(unquote(valueVal));
                if (!value.isBlank() && !"null".equals(value)) {
                    try {
                        java.time.OffsetDateTime odt = java.time.OffsetDateTime.parse(value);
                        field.set(obj, java.util.Date.from(odt.toInstant()));
                    } catch (Exception e1) {
                        java.time.LocalDateTime parsed = value.length() == 10
                                ? java.time.LocalDate.parse(value).atStartOfDay()
                                : java.time.LocalDateTime.parse(value.replace(" ", "T"));
                        field.set(obj, java.util.Date.from(parsed.atZone(java.time.ZoneId.systemDefault()).toInstant()));
                    }
                }
            }
            // Add date handling if needed (rudimentary)
        } catch (Exception e) {
            // Field not found or mismatch, ignore
        }
    }

private static String unquote(String value) {
        String normalized = value == null ? "" : value.trim();
        if (normalized.length() >= 2 && normalized.startsWith("\"") && normalized.endsWith("\"")) {
            return normalized.substring(1, normalized.length() - 1);
        }
        return normalized;
    }

    /**
     * Decode the JSON string escapes produced by {@link #escape(String)}.
     */
    private static String unescapeJsonString(String s) {
        if (s == null) return null;
        if (s.indexOf('\\') < 0) return s;
        StringBuilder sb = new StringBuilder(s.length());
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '\\' && i + 1 < s.length()) {
                char n = s.charAt(++i);
                switch (n) {
                    case '"': sb.append('"'); break;
                    case '\\': sb.append('\\'); break;
                    case '/': sb.append('/'); break;
                    case 'n': sb.append('\n'); break;
                    case 'r': sb.append('\r'); break;
                    case 't': sb.append('\t'); break;
                    case 'b': sb.append('\b'); break;
                    case 'f': sb.append('\f'); break;
                    case 'u':
                        if (i + 4 < s.length()) {
                            try {
                                sb.append((char) Integer.parseInt(s.substring(i + 1, i + 5), 16));
                                i += 4;
                            } catch (NumberFormatException e) {
                                sb.append(n);
                            }
                        } else {
                            sb.append(n);
                        }
                        break;
                    default:
                        sb.append(n);
                }
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }
}
