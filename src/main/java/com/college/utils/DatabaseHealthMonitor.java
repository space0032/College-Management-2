package com.college.utils;

import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.Statement;

/**
 * Background health monitor that checks PostgreSQL availability.
 * 
 * When the application is running in H2 fallback mode, this monitor:
 * 1. Polls PostgreSQL every 30 seconds with a lightweight "SELECT 1"
 * 2. When PostgreSQL becomes available, triggers a data sync (H2 → PG)
 * 3. After successful sync, switches the active database back to PostgreSQL
 * 4. Continues monitoring even after recovery (to detect future failures)
 * 
 * Runs as a daemon thread — won't prevent JVM shutdown.
 */
public class DatabaseHealthMonitor implements Runnable {

    /** How often to check PostgreSQL (in milliseconds) */
    private static final int CHECK_INTERVAL_MS = 30_000; // 30 seconds

    /** How long to wait after a failed sync before retrying (in milliseconds) */
    private static final int RETRY_DELAY_MS = 60_000; // 1 minute

    /** Maximum consecutive failures before increasing interval */
    private static final int MAX_FAST_FAILURES = 10;

    /** Extended interval after many failures (in milliseconds) */
    private static final int EXTENDED_INTERVAL_MS = 120_000; // 2 minutes

    private volatile boolean running = true;
    private int consecutiveFailures = 0;

    @Override
    public void run() {
        Logger.info("[HealthMonitor] Database health monitor started. Checking PostgreSQL every " 
                    + (CHECK_INTERVAL_MS / 1000) + "s.");

        while (running) {
            try {
                // Calculate current check interval
                int interval = (consecutiveFailures > MAX_FAST_FAILURES) 
                               ? EXTENDED_INTERVAL_MS 
                               : CHECK_INTERVAL_MS;

                Thread.sleep(interval);

                if (!DatabaseConnection.isUsingFallback()) {
                    // We're on PostgreSQL — just do a quick health check
                    if (!quickHealthCheck()) {
                        Logger.warn("[HealthMonitor] PostgreSQL health check failed while active!");
                        // The next getConnection() call will trigger failover
                    }
                    consecutiveFailures = 0;
                    continue;
                }

                // We're on H2 fallback — try to connect to PostgreSQL
                Logger.debug("[HealthMonitor] Checking PostgreSQL availability...");

                if (testPostgresDirectly()) {
                    Logger.info("[HealthMonitor] ✓ PostgreSQL is back online!");
                    consecutiveFailures = 0;

                    // Step 1: Sync data from H2 to PostgreSQL
                    int pendingChanges = ChangeTracker.getPendingChangeCount();
                    Logger.info("[HealthMonitor] " + pendingChanges + " pending changes to sync.");

                    if (pendingChanges > 0) {
                        Logger.info("[HealthMonitor] Starting data sync: H2 → PostgreSQL...");
                        boolean syncSuccess = DataSyncEngine.syncToPostgres();

                        if (!syncSuccess) {
                            Logger.warn("[HealthMonitor] Sync completed with errors. Will retry on next check.");
                            continue; // Don't switch back if sync had errors
                        }

                        Logger.info("[HealthMonitor] Data sync completed successfully.");
                    }

                    // Step 2: Switch back to PostgreSQL
                    DatabaseConnection.switchToPostgres();
                    Logger.info("[HealthMonitor] ✓ Switched back to PostgreSQL (Primary).");

                } else {
                    consecutiveFailures++;
                    if (consecutiveFailures <= 3 || consecutiveFailures % 10 == 0) {
                        Logger.debug("[HealthMonitor] PostgreSQL still unavailable. " +
                                    "(Check #" + consecutiveFailures + ")");
                    }
                }

            } catch (InterruptedException e) {
                Logger.info("[HealthMonitor] Health monitor interrupted, stopping.");
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                Logger.error("[HealthMonitor] Unexpected error in health monitor", e);
                consecutiveFailures++;
            }
        }

        Logger.info("[HealthMonitor] Database health monitor stopped.");
    }

    /**
     * Test PostgreSQL connectivity directly (bypassing the HikariCP pool).
     * This avoids pool-level caching of connection state.
     */
    private boolean testPostgresDirectly() {
        HikariDataSource pgDs = DatabaseConnection.getPostgresDataSource();
        
        if (pgDs == null || pgDs.isClosed()) {
            return false;
        }

        try (Connection conn = pgDs.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("SELECT 1");
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Quick health check on the currently active database
     */
    private boolean quickHealthCheck() {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("SELECT 1");
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Stop the health monitor
     */
    public void stop() {
        running = false;
        Logger.info("[HealthMonitor] Stop requested.");
    }

    /**
     * Check if the monitor is still running
     */
    public boolean isRunning() {
        return running;
    }
}
