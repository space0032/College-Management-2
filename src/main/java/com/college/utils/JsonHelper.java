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
        if (obj instanceof List) {
            List<?> list = (List<?>) obj;
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < list.size(); i++) {
                sb.append(toJson(list.get(i)));
                if (i < list.size() - 1)
                    sb.append(",");
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
        if (obj instanceof java.util.Date) {
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
                field.set(obj, Integer.parseInt(valueVal));
            } else if (field.getType() == double.class || field.getType() == Double.class) {
                field.set(obj, Double.parseDouble(valueVal));
            } else if (field.getType() == boolean.class || field.getType() == Boolean.class) {
                field.set(obj, Boolean.parseBoolean(valueVal));
            }
            // Add date handling if needed (rudimentary)
        } catch (Exception e) {
            // Field not found or mismatch, ignore
        }
    }
}
