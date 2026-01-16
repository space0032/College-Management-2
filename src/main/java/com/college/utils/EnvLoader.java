package com.college.utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.file.Paths;

public class EnvLoader {
    public static void load() {
        try (BufferedReader reader = new BufferedReader(new FileReader(".env"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                int idx = line.indexOf('=');
                if (idx > 0) {
                    String key = line.substring(0, idx).trim();
                    String value = line.substring(idx + 1).trim();
                    System.setProperty(key, value);
                }
            }
        } catch (Exception e) {
            Logger.warn("Could not load .env file: " + e.getMessage());
        }
    }
}
