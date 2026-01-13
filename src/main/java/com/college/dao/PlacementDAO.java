package com.college.dao;

import com.college.models.PlacementApplication;
import com.college.models.PlacementCompany;
import com.college.models.PlacementDrive;
import com.college.utils.DatabaseConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PlacementDAO {

    // --- Companies ---

    public List<PlacementCompany> getAllCompanies() {
        List<PlacementCompany> list = new ArrayList<>();
        String sql = "SELECT * FROM placement_companies ORDER BY name";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                list.add(mapCompany(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public void addCompany(PlacementCompany company) {
        String sql = "INSERT INTO placement_companies (name, industry, contact_person, email, phone, website) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, company.getName());
            stmt.setString(2, company.getIndustry());
            stmt.setString(3, company.getContactPerson());
            stmt.setString(4, company.getEmail());
            stmt.setString(5, company.getPhone());
            stmt.setString(6, company.getWebsite());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private PlacementCompany mapCompany(ResultSet rs) throws SQLException {
        return new PlacementCompany(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getString("industry"),
                rs.getString("contact_person"),
                rs.getString("email"),
                rs.getString("phone"),
                rs.getString("website"));
    }

    // --- Drives ---

    public List<PlacementDrive> getAllDrives() {
        List<PlacementDrive> list = new ArrayList<>();
        String sql = "SELECT d.*, c.name as company_name FROM placement_drives d JOIN placement_companies c ON d.company_id = c.id ORDER BY d.drive_date";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                PlacementDrive drive = mapDrive(rs);
                drive.setCompanyName(rs.getString("company_name"));
                list.add(drive);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<PlacementDrive> getUpcomingDrives() {
        List<PlacementDrive> list = new ArrayList<>();
        String sql = "SELECT d.*, c.name as company_name FROM placement_drives d JOIN placement_companies c ON d.company_id = c.id WHERE d.deadline >= CURRENT_DATE ORDER BY d.deadline";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                PlacementDrive drive = mapDrive(rs);
                drive.setCompanyName(rs.getString("company_name"));
                list.add(drive);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public void addDrive(PlacementDrive drive) {
        String sql = "INSERT INTO placement_drives (company_id, job_role, package_lpa, description, drive_date, deadline, eligibility_criteria) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, drive.getCompanyId());
            stmt.setString(2, drive.getJobRole());
            stmt.setDouble(3, drive.getPackageLpa());
            stmt.setString(4, drive.getDescription());
            stmt.setDate(5, java.sql.Date.valueOf(drive.getDriveDate()));
            stmt.setDate(6, java.sql.Date.valueOf(drive.getDeadline()));
            stmt.setString(7, drive.getEligibilityCriteria());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteDrive(int id) {
        String sql = "DELETE FROM placement_drives WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteCompany(int id) {
        String sql = "DELETE FROM placement_companies WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private PlacementDrive mapDrive(ResultSet rs) throws SQLException {
        return new PlacementDrive(
                rs.getInt("id"),
                rs.getInt("company_id"),
                rs.getString("job_role"),
                rs.getDouble("package_lpa"),
                rs.getString("description"),
                rs.getDate("drive_date").toLocalDate(),
                rs.getDate("deadline").toLocalDate(),
                rs.getString("eligibility_criteria"));
    }

    // --- Applications ---

    public boolean hasApplied(int driveId, int studentId) {
        String sql = "SELECT COUNT(*) FROM placement_applications WHERE drive_id = ? AND student_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, driveId);
            stmt.setInt(2, studentId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next())
                return rs.getInt(1) > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void applyForDrive(int driveId, int studentId) {
        String sql = "INSERT INTO placement_applications (drive_id, student_id, status) VALUES (?, ?, 'APPLIED')";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, driveId);
            stmt.setInt(2, studentId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<PlacementApplication> getApplicationsForStudent(int studentId) {
        List<PlacementApplication> list = new ArrayList<>();
        String sql = "SELECT a.*, d.job_role, c.name as company_name FROM placement_applications a " +
                "JOIN placement_drives d ON a.drive_id = d.id " +
                "JOIN placement_companies c ON d.company_id = c.id " +
                "WHERE a.student_id = ? ORDER BY a.applied_at DESC";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, studentId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                PlacementApplication app = mapApplication(rs);
                app.setDriveTitle(rs.getString("job_role"));
                app.setCompanyName(rs.getString("company_name"));
                list.add(app);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<PlacementApplication> getApplicationsForDrive(int driveId) {
        List<PlacementApplication> list = new ArrayList<>();
        String sql = "SELECT a.*, u.username as student_name FROM placement_applications a " +
                "JOIN users u ON a.student_id = u.id " +
                "WHERE a.drive_id = ? ORDER BY a.status, u.username";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, driveId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                PlacementApplication app = mapApplication(rs);
                app.setStudentName(rs.getString("student_name"));
                list.add(app);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public void updateApplicationStatus(int applicationId, String status) {
        String sql = "UPDATE placement_applications SET status = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status);
            stmt.setInt(2, applicationId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void withdrawApplication(int applicationId) {
        String sql = "DELETE FROM placement_applications WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, applicationId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private PlacementApplication mapApplication(ResultSet rs) throws SQLException {
        Timestamp ts = rs.getTimestamp("applied_at");
        return new PlacementApplication(
                rs.getInt("id"),
                rs.getInt("drive_id"),
                rs.getInt("student_id"),
                rs.getString("status"),
                ts != null ? ts.toLocalDateTime() : LocalDateTime.now());
    }
}
