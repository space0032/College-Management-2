package com.college.utils;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Centralized logging utility for the College Management System
 */
public class Logger {

    private static final String LOG_FILE = "app.log";
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public enum Level {
        ERROR, WARN, INFO, DEBUG
    }

    /**
     * Log an error message with exception
     */
    public static void error(String message, Exception e) {
        log(Level.ERROR, message + " - " + e.getMessage(), e);
    }

    /**
     * Log an error message
     */
    public static void error(String message) {
        log(Level.ERROR, message, null);
    }

    /**
     * Log a warning message
     */
    public static void warn(String message) {
        log(Level.WARN, message, null);
    }

    /**
     * Log an info message
     */
    public static void info(String message) {
        log(Level.INFO, message, null);
    }

    /**
     * Log a debug message
     */
    public static void debug(String message) {
        log(Level.DEBUG, message, null);
    }

    /**
     * Internal logging method
     */
    private static void log(Level level, String message, Exception e) {
        String timestamp = LocalDateTime.now().format(formatter);
        String logMessage = String.format("[%s] [%s] %s", timestamp, level, message);

        // Print to console
        System.out.println(logMessage);

        // Write to file
        try (FileWriter fw = new FileWriter(LOG_FILE, true);
                PrintWriter pw = new PrintWriter(fw)) {
            pw.println(logMessage);
            if (e != null) {
                e.printStackTrace(pw);
            }
        } catch (IOException ex) {
            System.err.println("Failed to write to log file: " + ex.getMessage());
        }
    }
}
