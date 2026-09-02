package com.college.utils;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Dual-database connection manager with automatic failover.
 * 
 * PRIMARY:  PostgreSQL (remote/main database)
 * FALLBACK: H2 (local embedded database, file-based)
 * 
 * Behavior:
 * 1. On startup, tries to connect to PostgreSQL
 * 2. If PostgreSQL is unreachable, falls back to H2
 * 3. A background health monitor checks PostgreSQL every 30 seconds
 * 4. When PostgreSQL recovers, H2 data is synced back and primary is restored
 * 
 * All 43+ DAOs call getConnection() — failover is 100% transparent.
 */
public class DatabaseConnection {

    /**
     * Enum representing which database is currently active
     */
    public enum ActiveDatabase {
        POSTGRES("PostgreSQL (Primary)"),
        H2("H2 (Fallback)");

        private final String displayName;
        ActiveDatabase(String displayName) { this.displayName = displayName; }
        public String getDisplayName() { return displayName; }
    }

    // ─── Connection Pools ────────────────────────────────────────────────
    private static HikariDataSource pgDataSource;
    private static HikariDataSource h2DataSource;

    // ─── State ───────────────────────────────────────────────────────────
    private static final AtomicReference<ActiveDatabase> activeDb = new AtomicReference<>(ActiveDatabase.POSTGRES);
    private static final ReentrantLock switchLock = new ReentrantLock();
    private static volatile boolean h2Initialized = false;
    private static volatile boolean healthMonitorStarted = false;
    private static volatile boolean initialized = false;

    // ─── PostgreSQL Configuration ────────────────────────────────────────
    private static String PG_URL = "jdbc:postgresql://localhost:5432/college_db";
    private static String PG_USERNAME = "postgres";
    private static String PG_PASSWORD = "password";

    // ─── H2 Configuration ────────────────────────────────────────────────
    private static String H2_DB_PATH = "./data/college_fallback";
    private static boolean H2_ENABLED = true;

    // ─── Listeners ───────────────────────────────────────────────────────
    private static volatile DatabaseStatusListener statusListener;

    /**
     * Callback interface for UI notifications on database status changes
     */
    public interface DatabaseStatusListener {
        void onDatabaseSwitch(ActiveDatabase newActiveDb, String message);
        void onSyncComplete(boolean success, String message);
    }

    // ─── Initialization ──────────────────────────────────────────────────

    static {
        loadEnv();
    }

    private static synchronized void ensureInitialized() {
        if (!initialized) {
            initialized = true;
            initPgDataSource();
        }
    }

    /**
     * Load database configuration from .env file
     */
    private static void loadEnv() {
        String envUrl = EnvConfig.get("DB_URL");
        String envUser = EnvConfig.get("DB_USER");
        String envPass = EnvConfig.get("DB_PASSWORD");
        String envH2Path = EnvConfig.get("H2_DB_PATH");
        String envH2Enabled = EnvConfig.get("H2_ENABLED");

        if (envUrl != null) PG_URL = envUrl;
        if (envUser != null) PG_USERNAME = envUser;
        if (envPass != null) PG_PASSWORD = envPass;
        if (envH2Path != null) H2_DB_PATH = envH2Path;
        if (envH2Enabled != null) H2_ENABLED = Boolean.parseBoolean(envH2Enabled);

        System.out.println("Database Config Loaded:");
        System.out.println("  PostgreSQL URL: " + PG_URL);
        System.out.println("  PostgreSQL User: " + PG_USERNAME);
        System.out.println("  H2 Fallback Path: " + H2_DB_PATH);
        System.out.println("  H2 Fallback Enabled: " + H2_ENABLED);
        // Do not print password
    }

    /**
     * Initialize the PostgreSQL HikariCP connection pool
     */
    private static void initPgDataSource() {
        try {
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(PG_URL);
            config.setUsername(PG_USERNAME);
            config.setPassword(PG_PASSWORD);

            // Pool settings optimized for desktop/remote usage
            config.setMaximumPoolSize(15);
            config.setMinimumIdle(1);
            config.setIdleTimeout(60000);        // 1 minute
            config.setConnectionTimeout(10000);  // 10 seconds (reduced for faster failover)
            config.setMaxLifetime(600000);        // 10 minutes
            config.setKeepaliveTime(30000);       // Keepalive every 30s
            config.setLeakDetectionThreshold(10000);
            config.setDriverClassName("org.postgresql.Driver");

            // Pool name for logging
            config.setPoolName("PG-Primary-Pool");

            pgDataSource = new HikariDataSource(config);
            Logger.info("[DatabaseConnection] PostgreSQL connection pool initialized.");

            // Verify connection
            try (Connection conn = pgDataSource.getConnection()) {
                if (conn != null && !conn.isClosed()) {
                    activeDb.set(ActiveDatabase.POSTGRES);
                    SqlDialectAdapter.setDialect(SqlDialectAdapter.Dialect.POSTGRESQL);
                    Logger.info("[DatabaseConnection] PostgreSQL connection verified successfully.");
                    
                    // Start continuous replication scheduler (if configured)
                    SnapshotScheduler.start();
                }
            }

        } catch (Exception e) {
            Logger.warn("[DatabaseConnection] PostgreSQL unavailable: " + e.getMessage());
            System.err.println("PostgreSQL unavailable. Checking H2 fallback...");

            // Try H2 fallback on startup
            if (H2_ENABLED) {
                switchToH2("PostgreSQL unavailable at startup");
            } else {
                System.err.println("CRITICAL: PostgreSQL unavailable and H2 fallback is disabled!");
            }
        }
    }

    /**
     * Public helper to ensure H2 is initialized (used by Replicator)
     */
    public static void ensureH2Initialized() {
        if (H2_ENABLED && (!h2Initialized || h2DataSource == null)) {
            initH2DataSource();
        }
    }

    /**
     * Initialize the H2 HikariCP connection pool (lazy — only on first failover)
     */
    private static synchronized void initH2DataSource() {
        if (h2DataSource != null && !h2DataSource.isClosed()) {
            return; // Already initialized
        }

        try {
            // Ensure the data directory exists
            java.io.File dataDir = new java.io.File(H2_DB_PATH).getParentFile();
            if (dataDir != null && !dataDir.exists()) {
                dataDir.mkdirs();
                Logger.info("[DatabaseConnection] Created H2 data directory: " + dataDir.getAbsolutePath());
            }

            HikariConfig config = new HikariConfig();
            // MODE=PostgreSQL for maximum SQL compatibility
            // AUTO_SERVER=TRUE allows multiple processes to access the same DB file
            config.setJdbcUrl("jdbc:h2:file:" + H2_DB_PATH + ";MODE=PostgreSQL;AUTO_SERVER=TRUE;DATABASE_TO_LOWER=TRUE");
            config.setUsername("sa");
            config.setPassword("");

            // H2 pool settings (smaller — it's local)
            config.setMaximumPoolSize(10);
            config.setMinimumIdle(1);
            config.setIdleTimeout(120000);       // 2 minutes
            config.setConnectionTimeout(5000);   // 5 seconds
            config.setMaxLifetime(1800000);      // 30 minutes
            config.setDriverClassName("org.h2.Driver");
            config.setPoolName("H2-Fallback-Pool");

            h2DataSource = new HikariDataSource(config);
            Logger.info("[DatabaseConnection] H2 fallback connection pool initialized.");

            // Initialize H2 schema if first time
            if (!h2Initialized) {
                H2SchemaInitializer.initialize(h2DataSource);
                ChangeTracker.initialize(h2DataSource);
                h2Initialized = true;
                Logger.info("[DatabaseConnection] H2 schema and change tracking initialized.");
            }

        } catch (Exception e) {
            System.err.println("CRITICAL: Failed to initialize H2 fallback: " + e.getMessage());
            Logger.error("Failed to initialize H2 fallback", e);
        }
    }

    // ─── Connection Retrieval ────────────────────────────────────────────

    /**
     * Private constructor to prevent instantiation
     */
    private DatabaseConnection() {
    }

    /**
     * Get a database connection. Transparently handles failover.
     * 
     * Logic:
     * 1. If active DB is POSTGRES, try to get a PG connection
     * 2. If PG fails, switch to H2 and return H2 connection
     * 3. If active DB is H2, return H2 connection directly
     * 
     * @return Connection from the currently active database
     * @throws SQLException if no connection is available from any source
     */
    public static Connection getConnection() throws SQLException {
        ensureInitialized();
        ActiveDatabase current = activeDb.get();

        if (current == ActiveDatabase.POSTGRES) {
            return getPostgresConnection();
        } else {
            return getH2Connection();
        }
    }

    /**
     * Try to get a PostgreSQL connection, fail over to H2 if needed
     */
    private static Connection getPostgresConnection() throws SQLException {
        if (pgDataSource == null || pgDataSource.isClosed()) {
            if (H2_ENABLED) {
                switchToH2("PostgreSQL pool is not initialized");
                return getH2Connection();
            }
            throw new SQLException("PostgreSQL DataSource is not initialized and H2 fallback is disabled.");
        }

        try {
            Connection conn = pgDataSource.getConnection();
            if (conn != null && !conn.isClosed()) {
                return conn;
            }
        } catch (SQLException e) {
            Logger.warn("[DatabaseConnection] PostgreSQL connection failed: " + e.getMessage());
            if (H2_ENABLED) {
                switchToH2("PostgreSQL connection failed: " + e.getMessage());
                return getH2Connection();
            }
            throw e;
        }

        throw new SQLException("Unable to obtain a valid connection from any source.");
    }

    /**
     * Get an H2 fallback connection
     */
    private static Connection getH2Connection() throws SQLException {
        if (h2DataSource == null || h2DataSource.isClosed()) {
            initH2DataSource();
        }

        if (h2DataSource == null) {
            throw new SQLException("H2 fallback DataSource could not be initialized.");
        }

        return h2DataSource.getConnection();
    }

    // ─── Failover Control ────────────────────────────────────────────────

    /**
     * Switch the active database to H2 fallback
     */
    private static void switchToH2(String reason) {
        switchLock.lock();
        try {
            if (activeDb.get() == ActiveDatabase.H2) {
                return; // Already on H2
            }

            Logger.warn("[DatabaseConnection] ⚠ SWITCHING TO H2 FALLBACK — Reason: " + reason);
            System.out.println("═══════════════════════════════════════════════════════════");
            System.out.println("  ⚠  DATABASE FAILOVER: Switching to H2 (Local Fallback)");
            System.out.println("  Reason: " + reason);
            System.out.println("  Data will be synced back when PostgreSQL recovers.");
            System.out.println("═══════════════════════════════════════════════════════════");

            // Initialize H2 if needed
            initH2DataSource();

            // Switch active database
            activeDb.set(ActiveDatabase.H2);
            SqlDialectAdapter.setDialect(SqlDialectAdapter.Dialect.H2);

            // Enable change tracking
            ChangeTracker.setTrackingEnabled(true);

            // Start health monitor if not already running
            startHealthMonitor();

            // Notify listener
            if (statusListener != null) {
                statusListener.onDatabaseSwitch(ActiveDatabase.H2,
                        "Running in offline mode. Data will sync when PostgreSQL recovers.");
            }

        } finally {
            switchLock.unlock();
        }
    }

    /**
     * Switch the active database back to PostgreSQL (called after sync completes)
     */
    public static void switchToPostgres() {
        switchLock.lock();
        try {
            if (activeDb.get() == ActiveDatabase.POSTGRES) {
                return; // Already on PostgreSQL
            }

            Logger.info("[DatabaseConnection] ✓ SWITCHING BACK TO POSTGRESQL (Primary)");
            System.out.println("═══════════════════════════════════════════════════════════");
            System.out.println("  ✓  DATABASE RECOVERED: Switching back to PostgreSQL");
            System.out.println("  All H2 data has been synced successfully.");
            System.out.println("═══════════════════════════════════════════════════════════");

            // Disable change tracking
            ChangeTracker.setTrackingEnabled(false);

            // Switch active database
            activeDb.set(ActiveDatabase.POSTGRES);
            SqlDialectAdapter.setDialect(SqlDialectAdapter.Dialect.POSTGRESQL);

            // Notify listener
            if (statusListener != null) {
                statusListener.onDatabaseSwitch(ActiveDatabase.POSTGRES,
                        "PostgreSQL connection restored. All data has been synced.");
            }

        } finally {
            switchLock.unlock();
        }
    }

    // ─── Health Monitoring ───────────────────────────────────────────────

    /**
     * Start the background health monitor thread
     */
    private static void startHealthMonitor() {
        if (healthMonitorStarted) {
            return;
        }
        healthMonitorStarted = true;

        DatabaseHealthMonitor monitor = new DatabaseHealthMonitor();
        Thread monitorThread = new Thread(monitor, "DB-Health-Monitor");
        monitorThread.setDaemon(true); // Won't prevent JVM shutdown
        monitorThread.start();

        Logger.info("[DatabaseConnection] Background health monitor started.");
    }

    // ─── Status Queries ──────────────────────────────────────────────────

    /**
     * Check if the application is currently using the H2 fallback
     */
    public static boolean isUsingFallback() {
        ensureInitialized();
        return activeDb.get() == ActiveDatabase.H2;
    }

    /**
     * Get the currently active database
     */
    public static ActiveDatabase getActiveDatabase() {
        ensureInitialized();
        return activeDb.get();
    }

    /**
     * Test if PostgreSQL is currently reachable (without switching)
     */
    public static boolean testPostgresConnection() {
        if (pgDataSource == null || pgDataSource.isClosed()) {
            // Try to reinitialize the PG pool
            try {
                initPgDataSource();
            } catch (Exception e) {
                return false;
            }
        }

        try (Connection conn = pgDataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("SELECT 1");
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Test database connection (uses the active database)
     */
    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            System.err.println("Test connection failed: " + e.getMessage());
            return false;
        }
    }

    /**
     * Get the raw PostgreSQL DataSource (for sync engine use only)
     */
    static HikariDataSource getPostgresDataSource() {
        return pgDataSource;
    }

    /**
     * Get the raw H2 DataSource (for sync engine use only)
     */
    static HikariDataSource getH2DataSource() {
        return h2DataSource;
    }

    // ─── Listener Management ─────────────────────────────────────────────

    /**
     * Set a listener for database status changes (for UI notifications)
     */
    public static void setStatusListener(DatabaseStatusListener listener) {
        statusListener = listener;
    }

    /**
     * Get the current status listener
     */
    static DatabaseStatusListener getStatusListener() {
        return statusListener;
    }

    // ─── Shutdown ────────────────────────────────────────────────────────

    /**
     * Shutdown all connection pools gracefully
     */
    public static void shutdown() {
        Logger.info("[DatabaseConnection] Shutting down all connection pools...");

        if (pgDataSource != null && !pgDataSource.isClosed()) {
            pgDataSource.close();
            Logger.info("[DatabaseConnection] PostgreSQL pool closed.");
        }

        if (h2DataSource != null && !h2DataSource.isClosed()) {
            h2DataSource.close();
            Logger.info("[DatabaseConnection] H2 pool closed.");
        }

        Logger.info("[DatabaseConnection] All connection pools shut down.");
    }

    // Register shutdown hook
    static {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Closing database connection pools...");
            shutdown();
            System.out.println("Database connection pools closed.");
        }));
    }
}
