package com.college.utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class EnvConfig {
    private static final Map<String, String> ENV = new HashMap<>();

    static {
        load();
    }

    private static void load() {
        java.io.File f = new java.io.File(".env");
        if (f.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(f))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (line.isEmpty() || line.startsWith("#"))
                        continue;
                    String[] parts = line.split("=", 2);
                    if (parts.length == 2) {
                        ENV.put(parts[0].trim(), parts[1].trim());
                    }
                }
            } catch (IOException e) {
                System.err.println("EnvConfig: Could not load .env file: " + e.getMessage());
            }
        }
    }

    public static String get(String key) {
        return ENV.get(key);
    }
}
