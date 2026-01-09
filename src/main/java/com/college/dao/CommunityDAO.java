package com.college.dao;

import com.college.models.Campaign;
import com.college.models.Scholarship;
import com.college.models.ScholarshipApplication;
import com.college.utils.DatabaseConnection;
import com.college.utils.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CommunityDAO {

    // --- Campaigns ---
    public boolean createCampaign(Campaign c) {
        String sql = "INSERT INTO campaigns (title, description, goal_amount, raised_amount, created_by, status) VALUES (?, ?, ?, 0, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, c.getTitle());
            pstmt.setString(2, c.getDescription());
            pstmt.setDouble(3, c.getGoalAmount());
            pstmt.setInt(4, c.getCreatedBy());
            pstmt.setString(5, c.getStatus());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            String msg = e.getMessage().toLowerCase();
            if (msg.contains("not exist") || msg.contains("doesn't exist")) {
                createCampaignTable();
                return createCampaign(c);
            }
            Logger.error("Error creating campaign: " + e.getMessage());
            return false;
        }
    }

    private void createCampaignTable() {
        String sql = "CREATE TABLE IF NOT EXISTS campaigns (" +
                "id SERIAL PRIMARY KEY, " +
                "title VARCHAR(255), " +
                "description TEXT, " +
                "goal_amount DOUBLE, " +
                "raised_amount DOUBLE, " +
                "created_by INT, " +
                "status VARCHAR(50))";
        try (Connection conn = DatabaseConnection.getConnection();
                Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            Logger.error("Error creating campaign table: " + e.getMessage());
        }
    }

    public List<Campaign> getAllCampaigns() {
        List<Campaign> list = new ArrayList<>();
        String sql = "SELECT * FROM campaigns";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql);
                ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                Campaign c = new Campaign();
                c.setId(rs.getInt("id"));
                c.setTitle(rs.getString("title"));
                c.setDescription(rs.getString("description"));
                c.setGoalAmount(rs.getDouble("goal_amount"));
                c.setRaisedAmount(rs.getDouble("raised_amount"));
                c.setCreatedBy(rs.getInt("created_by"));
                c.setStatus(rs.getString("status"));
                list.add(c);
            }
        } catch (SQLException e) {
            String msg = e.getMessage().toLowerCase();
            if (msg.contains("not exist") || msg.contains("doesn't exist")) {
                createCampaignTable();
                return list;
            }
            Logger.error("Error fetching campaigns: " + e.getMessage());
        }
        return list;
    }

    public boolean donateToCampaign(int campaignId, double amount) {
        String sql = "UPDATE campaigns SET raised_amount = raised_amount + ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDouble(1, amount);
            pstmt.setInt(2, campaignId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            Logger.error("Error donating: " + e.getMessage());
            return false;
        }
    }

    // --- Scholarships ---
    public boolean createScholarship(Scholarship s) {
        String sql = "INSERT INTO scholarships (title, description, amount, donor_name, created_by, status) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, s.getTitle());
            pstmt.setString(2, s.getDescription());
            pstmt.setDouble(3, s.getAmount());
            pstmt.setString(4, s.getDonorName());
            pstmt.setInt(5, s.getCreatedBy());
            pstmt.setString(6, s.getStatus());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            String msg = e.getMessage().toLowerCase();
            if (msg.contains("not exist") || msg.contains("doesn't exist")) {
                createScholarshipTable();
                return createScholarship(s);
            }
            Logger.error("Error creating scholarship: " + e.getMessage());
            return false;
        }
    }

    private void createScholarshipTable() {
        String sql = "CREATE TABLE IF NOT EXISTS scholarships (" +
                "id SERIAL PRIMARY KEY, " +
                "title VARCHAR(255), " +
                "description TEXT, " +
                "amount DOUBLE, " +
                "donor_name VARCHAR(255), " +
                "created_by INT, " +
                "status VARCHAR(50))";
        try (Connection conn = DatabaseConnection.getConnection();
                Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            Logger.error("Error creating scholarship table: " + e.getMessage());
        }
    }

    public List<Scholarship> getAllScholarships() {
        List<Scholarship> list = new ArrayList<>();
        String sql = "SELECT * FROM scholarships";
        try (Connection conn = DatabaseConnection.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Scholarship s = new Scholarship();
                s.setId(rs.getInt("id"));
                s.setTitle(rs.getString("title"));
                s.setDescription(rs.getString("description"));
                s.setAmount(rs.getDouble("amount"));
                s.setDonorName(rs.getString("donor_name"));
                s.setCreatedBy(rs.getInt("created_by"));
                s.setStatus(rs.getString("status"));
                list.add(s);
            }
        } catch (SQLException e) {
            String msg = e.getMessage().toLowerCase();
            if (msg.contains("not exist") || msg.contains("doesn't exist")) {
                createScholarshipTable();
                return list;
            }
            Logger.error("Error fetching scholarships: " + e.getMessage());
        }
        return list;
    }

    // --- Applications ---
    public boolean applyForScholarship(ScholarshipApplication app) {
        String sql = "INSERT INTO scholarship_applications (scholarship_id, student_id, statement, status) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, app.getScholarshipId());
            pstmt.setInt(2, app.getStudentId());
            pstmt.setString(3, app.getStatement());
            pstmt.setString(4, app.getStatus());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            String msg = e.getMessage().toLowerCase();
            if (msg.contains("not exist") || msg.contains("doesn't exist")) {
                createApplicationTable();
                return applyForScholarship(app);
            }
            Logger.error("Error applying: " + e.getMessage());
            return false;
        }
    }

    private void createApplicationTable() {
        String sql = "CREATE TABLE IF NOT EXISTS scholarship_applications (" +
                "id SERIAL PRIMARY KEY, " +
                "scholarship_id INT, " +
                "student_id INT, " +
                "statement TEXT, " +
                "status VARCHAR(50), " +
                "FOREIGN KEY (scholarship_id) REFERENCES scholarships(id) ON DELETE CASCADE)";
        try (Connection conn = DatabaseConnection.getConnection();
                Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            Logger.error("Error creating app table: " + e.getMessage());
        }
    }

    public List<ScholarshipApplication> getApplications(int scholarshipId) {
        List<ScholarshipApplication> list = new ArrayList<>();
        String sql = "SELECT sa.*, s.name as student_name FROM scholarship_applications sa " +
                "JOIN students s ON sa.student_id = s.id " +
                "WHERE sa.scholarship_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, scholarshipId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                ScholarshipApplication app = new ScholarshipApplication();
                app.setId(rs.getInt("id"));
                app.setScholarshipId(rs.getInt("scholarship_id"));
                app.setStudentId(rs.getInt("student_id"));
                app.setStudentName(rs.getString("student_name"));
                app.setStatement(rs.getString("statement"));
                app.setStatus(rs.getString("status"));
                list.add(app);
            }
        } catch (SQLException e) {
            String msg = e.getMessage().toLowerCase();
            if (msg.contains("not exist") || msg.contains("doesn't exist")) {
                createApplicationTable();
                return list;
            }
            Logger.error("Error fetching apps: " + e.getMessage());
        }
        return list;
    }

    public boolean updateApplicationStatus(int appId, String status) {
        String sql = "UPDATE scholarship_applications SET status = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, status);
            pstmt.setInt(2, appId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            Logger.error("Error updating app status: " + e.getMessage());
            return false;
        }
    }
}
