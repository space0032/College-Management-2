package com.college.dao;

import com.college.models.Warden;

import com.college.utils.DatabaseConnection;
import com.college.utils.Logger;
import com.college.utils.WardenValidation;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO for Warden Management.
 *
 * Creating a warden auto-generates a WARDEN user account; deleting one removes
 * the linked user and employee records in the same transaction so no orphan
 * accounts are left behind.
 */
public class WardenDAO {

    /**
     * Auto-generated initial password for warden accounts is random so a warden
     * can never be logged into with a well-known default credential. The admin
     * who creates the account receives the generated value via the API response.
     */
    public static String generatePassword() {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789";
        StringBuilder sb = new StringBuilder(12);
        java.security.SecureRandom rnd = new java.security.SecureRandom();
        for (int i = 0; i < 12; i++) {
            sb.append(chars.charAt(rnd.nextInt(chars.length())));
        }
        return sb.toString();
    }

    /** Standard permission codes for the WARDEN role. */
    private static final String[] WARDEN_PERMISSIONS = {
            "VIEW_HOSTEL", "CREATE_HOSTEL", "UPDATE_HOSTEL", "DELETE_HOSTEL", "MANAGE_HOSTEL",
            "VIEW_HOSTEL_ATTENDANCE",
            "VIEW_ROOM", "MANAGE_ROOM", "ROOM_CHECK",
            "VIEW_STUDENT", "MANAGE_STUDENTS",
            "VIEW_ANNOUNCEMENT", "VIEW_NOTIFICATION", "CREATE_ANNOUNCEMENT",
            "VIEW_LEAVE", "UPDATE_LEAVE",
            "VIEW_COMPLAINT", "MANAGE_COMPLAINT",
            "VIEW_GATEPASS", "MANAGE_GATEPASS", "APPROVE_GATE_PASS",
            "VIEW_ATTENDANCE_REPORT", "VIEW_FEES_REPORT"
    };

    private static final String SELECT_SQL = "SELECT w.*, h.name as hostel_name, u.username FROM wardens w " +
            "LEFT JOIN hostels h ON w.hostel_id = h.id " +
            "LEFT JOIN users u ON w.user_id = u.id ";

    /**
     * Add new warden with auto-generated user account.
     */
    public int addWarden(Warden warden) {
        if (WardenValidation.isBlank(warden.getName()) || !WardenValidation.isValidEmail(warden.getEmail())) {
            return -1;
        }
        if (!WardenValidation.isValidPhone(warden.getPhone())) {
            return -1;
        }

        String name = warden.getName().trim();
        String email = warden.getEmail().trim();
        String phone = WardenValidation.trimToNull(warden.getPhone());
        int hostelId = warden.getHostelId();

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false); // Start transaction

            try {
                // Reject duplicate email up front (column is UNIQUE NOT NULL)
                if (isEmailTaken(conn, email)) {
                    conn.rollback();
                    return -1;
                }
                // A "0" / blank hostel ID means "unassigned"; anything else must exist
                if (hostelId > 0 && !hostelExists(conn, hostelId)) {
                    conn.rollback();
                    return -1;
                }

                // 1. Create the WARDEN user account if none is pre-supplied
                int userId = resolveUserId(conn, warden, email);
                if (userId <= 0) {
                    conn.rollback();
                    return -1;
                }

                // 2. Insert Warden
                String sql = "INSERT INTO wardens (name, email, phone, hostel_id, user_id) VALUES (?, ?, ?, ?, ?)";
                try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                    pstmt.setString(1, name);
                    pstmt.setString(2, email);
                    pstmt.setString(3, phone == null ? "" : phone);

                    if (hostelId > 0) {
                        pstmt.setInt(4, hostelId);
                    } else {
                        pstmt.setNull(4, Types.INTEGER);
                    }

                    pstmt.setInt(5, userId);

                    if (pstmt.executeUpdate() > 0) {
                        try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                            if (generatedKeys.next()) {
                                int wardenId = generatedKeys.getInt(1);
                                createEmployeeRecord(conn, warden, userId);
                                conn.commit(); // Commit transaction
                                return wardenId;
                            }
                        }
                    }
                }
                conn.rollback();
                return -1;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            Logger.error("Database operation failed", e);
            return -1;
        }
    }

    /**
     * Get all wardens
     */
    public List<Warden> getAllWardens() {
        List<Warden> wardens = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(SELECT_SQL + "ORDER BY w.name")) {

            while (rs.next()) {
                wardens.add(extractWardenFromResultSet(rs));
            }
        } catch (SQLException e) {
            Logger.error("Database operation failed", e);
            throw new RuntimeException("Failed to load wardens", e);
        }

        return wardens;
    }

    /**
     * Update warden
     */
    public boolean updateWarden(Warden warden) {
        if (WardenValidation.isBlank(warden.getName()) || !WardenValidation.isValidEmail(warden.getEmail())) {
            return false;
        }
        if (!WardenValidation.isValidPhone(warden.getPhone())) {
            return false;
        }

        String sql = "UPDATE wardens SET name = ?, email = ?, phone = ?, hostel_id = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            if (warden.getHostelId() > 0 && !hostelExists(conn, warden.getHostelId())) {
                return false;
            }

            pstmt.setString(1, warden.getName().trim());
            pstmt.setString(2, warden.getEmail().trim());
            pstmt.setString(3, WardenValidation.trimToNull(warden.getPhone()) == null ? "" : warden.getPhone().trim());

            if (warden.getHostelId() > 0) {
                pstmt.setInt(4, warden.getHostelId());
            } else {
                pstmt.setNull(4, Types.INTEGER);
            }

            pstmt.setInt(5, warden.getId());

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            Logger.error("Database operation failed", e);
        }

        return false;
    }

    /**
     * Delete warden together with its linked user and employee records in a
     * single transaction so nothing is orphaned on partial failure.
     */
    public boolean deleteWarden(int id) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);

            try {
                Warden warden = getWardenById(id);
                if (warden == null) {
                    conn.rollback();
                    return false;
                }

                // Remove auto-created employee record (employee_id == warden username)
                if (warden.getUsername() != null && !warden.getUsername().isEmpty()) {
                    try (PreparedStatement pstmt = conn.prepareStatement("DELETE FROM employees WHERE employee_id = ?")) {
                        pstmt.setString(1, warden.getUsername());
                        pstmt.executeUpdate();
                    }
                }

                // Remove the linked user account
                if (warden.getUserId() > 0) {
                    try (PreparedStatement pstmt = conn.prepareStatement("DELETE FROM users WHERE id = ?")) {
                        pstmt.setInt(1, warden.getUserId());
                        pstmt.executeUpdate();
                    }
                }

                // Remove the warden row itself
                try (PreparedStatement pstmt = conn.prepareStatement("DELETE FROM wardens WHERE id = ?")) {
                    pstmt.setInt(1, id);
                    boolean deleted = pstmt.executeUpdate() > 0;
                    if (deleted) {
                        conn.commit();
                    } else {
                        conn.rollback();
                    }
                    return deleted;
                }
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            Logger.error("Database operation failed", e);
        }

        return false;
    }

    /**
     * Get warden by ID
     */
    public Warden getWardenById(int id) {
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(SELECT_SQL + "WHERE w.id = ?")) {

            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return extractWardenFromResultSet(rs);
            }

        } catch (SQLException e) {
            Logger.error("Database operation failed", e);
        }

        return null;
    }

    /**
     * Get warden by User ID
     */
    public Warden getWardenByUserId(int userId) {
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(SELECT_SQL + "WHERE w.user_id = ?")) {

            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return extractWardenFromResultSet(rs);
            }

        } catch (SQLException e) {
            Logger.error("Database operation failed", e);
        }

        return null;
    }

    private int resolveUserId(Connection conn, Warden warden, String email) throws SQLException {
        if (warden.getUserId() > 0) {
            return warden.getUserId();
        }

        String username = generateUniqueUsername(conn);
        if (username == null) {
            return -1;
        }
        warden.setUsername(username); // Set for display back to user

        UserDAO userDAO = new UserDAO();
        int roleId = ensureWardenRole(conn);

        String generatedPassword = generatePassword();
        int userId = userDAO.addUser(conn, username, generatedPassword, "WARDEN", roleId);
        if (userId <= 0) {
            return -1;
        }
        warden.setUserId(userId);
        warden.setGeneratedPassword(generatedPassword);
        return userId;
    }

    /**
     * Resolve (or create) the WARDEN role, then return its id.
     *
     * The V65 migration deleted every non-ADMIN role without recreating WARDEN,
     * which previously caused created warden accounts to be inserted with
     * role_id NULL and therefore zero permissions. Creating the role here (and
     * wiring its permissions) inside the same transaction guarantees every
     * warden account is properly linked to the WARDEN role regardless of which
     * migrations have run. Idempotent: reuses an existing role and only inserts
     * permissions that are not yet assigned.
     */
    private int ensureWardenRole(Connection conn) throws SQLException {
        com.college.models.Role existing = new RoleDAO().getRoleByCode(conn, "WARDEN");
        int roleId = (existing != null) ? existing.getId() : insertWardenRole(conn);
        wireWardenPermissions(conn, roleId);
        return roleId;
    }

    private int insertWardenRole(Connection conn) throws SQLException {
        String sql = "INSERT INTO roles (code, name, description, is_system_role, portal_type) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, "WARDEN");
            pstmt.setString(2, "Hostel Warden");
            pstmt.setString(3, "Manages hostel rooms, gate passes, and resident students");
            pstmt.setBoolean(4, true);
            pstmt.setString(5, "WARDEN");
            if (pstmt.executeUpdate() > 0) {
                try (ResultSet keys = pstmt.getGeneratedKeys()) {
                    if (keys.next()) {
                        return keys.getInt(1);
                    }
                }
            }
        }
        // Fallback in case a concurrent create won the race
        com.college.models.Role reloaded = new RoleDAO().getRoleByCode(conn, "WARDEN");
        if (reloaded != null) {
            return reloaded.getId();
        }
        return -1;
    }

    /**
     * Assign the standard WARDEN permissions. Idempotent per permission code.
     */
    private void wireWardenPermissions(Connection conn, int roleId) {
        if (roleId <= 0) {
            return;
        }
        String sql = "INSERT INTO role_permissions (role_id, permission_id) " +
                "SELECT ?, p.id FROM permissions p WHERE p.code = ? " +
                "AND NOT EXISTS (SELECT 1 FROM role_permissions rp WHERE rp.role_id = ? AND rp.permission_id = p.id)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            for (String code : WARDEN_PERMISSIONS) {
                pstmt.setInt(1, roleId);
                pstmt.setString(2, code);
                pstmt.setInt(3, roleId);
                pstmt.addBatch();
            }
            pstmt.executeBatch();
        } catch (SQLException e) {
            Logger.error("Failed to wire warden role permissions", e);
        }
    }

    /**
     * Pick a WARDENxxxx username that is not already present. Retries a few
     * times before falling back to a timestamped suffix so collisions can never
     * abort a create.
     */
    private String generateUniqueUsername(Connection conn) throws SQLException {
        for (int attempt = 0; attempt < 5; attempt++) {
            String candidate = "WARDEN" + (1000 + (int) (Math.random() * 9000));
            try (PreparedStatement pstmt = conn.prepareStatement("SELECT 1 FROM users WHERE username = ?")) {
                pstmt.setString(1, candidate);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (!rs.next()) {
                        return candidate;
                    }
                }
            }
        }
        return "WARDEN" + (System.currentTimeMillis() % 100_000_000L);
    }

    private boolean isEmailTaken(Connection conn, String email) throws SQLException {
        try (PreparedStatement pstmt = conn.prepareStatement(
                "SELECT 1 FROM wardens WHERE LOWER(email) = LOWER(?)")) {
            pstmt.setString(1, email);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    private boolean hostelExists(Connection conn, int hostelId) throws SQLException {
        try (PreparedStatement pstmt = conn.prepareStatement("SELECT 1 FROM hostels WHERE id = ?")) {
            pstmt.setInt(1, hostelId);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    /**
     * Auto-create a linked employee record for the warden.
     * Idempotent: skips when an employee with the same employee_id already exists.
     */
    private void createEmployeeRecord(Connection conn, Warden warden, int userId) {
        if (userId <= 0) {
            return;
        }

        String username = warden.getUsername();
        if (username == null || username.isEmpty()) {
            return;
        }

        try (PreparedStatement chk = conn.prepareStatement("SELECT 1 FROM employees WHERE employee_id = ?")) {
            chk.setString(1, username);
            try (ResultSet rs = chk.executeQuery()) {
                if (rs.next()) {
                    return; // Already exists — do not duplicate
                }
            }

            String sql = "INSERT INTO employees (employee_id, first_name, last_name, email, phone, designation, join_date, salary, status) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, username);
                pstmt.setString(2, warden.getName() == null ? "" : warden.getName());
                pstmt.setString(3, "");
                pstmt.setString(4, warden.getEmail() == null ? "" : warden.getEmail());
                pstmt.setString(5, warden.getPhone() == null ? "" : warden.getPhone());
                pstmt.setString(6, "WARDEN");
                pstmt.setDate(7, Date.valueOf(java.time.LocalDate.now()));
                pstmt.setBigDecimal(8, new java.math.BigDecimal("40000")); // Warden salary
                pstmt.setString(9, com.college.models.Employee.Status.ACTIVE.name());
                pstmt.executeUpdate();
            }
        } catch (SQLException e) {
            Logger.error("Failed to auto-create employee for warden", e);
            // Best effort — do not fail the whole warden creation
        }
    }

    private Warden extractWardenFromResultSet(ResultSet rs) throws SQLException {
        Warden warden = new Warden();
        warden.setId(rs.getInt("id"));
        warden.setName(rs.getString("name"));
        warden.setEmail(rs.getString("email"));
        warden.setPhone(rs.getString("phone"));

        int hostelId = rs.getInt("hostel_id");
        if (!rs.wasNull()) {
            warden.setHostelId(hostelId);
        }

        int userId = rs.getInt("user_id");
        if (!rs.wasNull()) {
            warden.setUserId(userId);
        }

        try {
            warden.setHostelName(rs.getString("hostel_name"));
        } catch (SQLException e) {
            // Ignore if column not found
        }

        try {
            warden.setUsername(rs.getString("username"));
        } catch (SQLException e) {
            // Ignore
        }

        return warden;
    }
}