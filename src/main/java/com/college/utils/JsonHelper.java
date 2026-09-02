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
            return "\"" + obj.toString() + "\"";
        }
        if (obj instanceof java.time.LocalDate || obj instanceof java.time.LocalDateTime) {
            return "\"" + obj.toString() + "\"";
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
        return s.replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
    }

    // Very basic Parser for flat JSON: {"key":"value", "num":123}
    public static <T> T fromJson(String json, Class<T> clazz) {
        try {
            T obj = clazz.getDeclaredConstructor().newInstance();
            json = json.trim();
            if (json.startsWith("{") && json.endsWith("}")) {
                json = json.substring(1, json.length() - 1);
                String[] pairs = json.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)"); // split by comma not in quotes
                for (String pair : pairs) {
                    String[] kv = pair.split(":", 2);
                    if (kv.length == 2) {
                        String key = kv[0].trim().replaceAll("\"", "");
                        String value = kv[1].trim();
                        setValue(obj, key, value);
                    }
                }
            }
            return obj;
        } catch (Exception e) {
            Logger.error("Error deserializing JSON", e);
        }
        return null;
    }

    private static void setValue(Object obj, String key, String valueVal) {
        try {
            Field field = obj.getClass().getDeclaredField(key);
            field.setAccessible(true);
            if (field.getType() == String.class) {
                if (valueVal.startsWith("\"") && valueVal.endsWith("\"")) {
                    field.set(obj, valueVal.substring(1, valueVal.length() - 1));
                } else {
                    field.set(obj, valueVal);
                }
            } else if (field.getType() == int.class || field.getType() == Integer.class) {
                field.set(obj, Integer.parseInt(unquote(valueVal)));
            } else if (field.getType() == double.class || field.getType() == Double.class) {
                field.set(obj, Double.parseDouble(unquote(valueVal)));
            } else if (field.getType() == boolean.class || field.getType() == Boolean.class) {
                field.set(obj, Boolean.parseBoolean(unquote(valueVal)));
            } else if (field.getType() == java.time.LocalDate.class) {
                if (valueVal.startsWith("\"") && valueVal.endsWith("\""))
                    valueVal = valueVal.substring(1, valueVal.length() - 1);
                field.set(obj, java.time.LocalDate.parse(valueVal));
            } else if (field.getType() == java.time.LocalDateTime.class) {
                if (valueVal.startsWith("\"") && valueVal.endsWith("\""))
                    valueVal = valueVal.substring(1, valueVal.length() - 1);
                field.set(obj, java.time.LocalDateTime.parse(valueVal.replace(" ", "T")));
            } else if (field.getType() == java.util.Date.class) {
                String value = unquote(valueVal);
                if (!value.isBlank() && !"null".equals(value)) {
                    java.time.LocalDateTime parsed = value.length() == 10
                            ? java.time.LocalDate.parse(value).atStartOfDay()
                            : java.time.LocalDateTime.parse(value.replace(" ", "T"));
                    field.set(obj, java.util.Date.from(parsed.atZone(java.time.ZoneId.systemDefault()).toInstant()));
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
}
