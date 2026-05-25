package com.college.utils;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Background Scheduler for the PostgreSQL -> H2 Data Replicator.
 * Reads the sync interval from environment configuration and schedules
 * the replication snapshot job to run periodically.
 */
public class SnapshotScheduler {

    private static ScheduledExecutorService scheduler;
    private static int intervalMinutes = 0;

    /**
     * Start the background data replicator scheduler.
     */
    public static void start() {
        if (scheduler != null && !scheduler.isShutdown()) {
            return; // Already running
        }

        // Read interval from config
        String envInterval = EnvConfig.get("PG_TO_H2_SYNC_INTERVAL_MINUTES");
        if (envInterval != null && !envInterval.trim().isEmpty()) {
            try {
                intervalMinutes = Integer.parseInt(envInterval.trim());
            } catch (NumberFormatException e) {
                Logger.warn("[SnapshotScheduler] Invalid interval in config: " + envInterval);
            }
        }

        if (intervalMinutes <= 0) {
            Logger.info("[SnapshotScheduler] PostgreSQL to H2 continuous replication is disabled.");
            return;
        }

        scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "Snapshot-Scheduler-Thread");
            t.setDaemon(true); // Don't block JVM shutdown
            return t;
        });

        // Run the first snapshot a short time after startup (e.g., 20 seconds),
        // then run periodically based on configured interval.
        long initialDelayMs = 20_000;

        scheduler.scheduleAtFixedRate(() -> {
            try {
                // Ensure H2 is initialized if it wasn't already.
                // We do this via DatabaseConnection which we will expose.
                DatabaseConnection.ensureH2Initialized();
                
                PostgresToH2Replicator.replicate();
            } catch (Exception e) {
                Logger.error("[SnapshotScheduler] Unexpected error during scheduled snapshot", e);
            }
        }, initialDelayMs, intervalMinutes * 60_000L, TimeUnit.MILLISECONDS);

        Logger.info("[SnapshotScheduler] Started. Next snapshot in 20s, then every " + intervalMinutes + " minutes.");
    }

    /**
     * Stop the scheduler
     */
    public static void stop() {
        if (scheduler != null) {
            scheduler.shutdownNow();
            Logger.info("[SnapshotScheduler] Stopped.");
        }
    }
}
