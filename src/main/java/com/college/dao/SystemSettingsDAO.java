package com.college.dao;

import com.college.utils.DatabaseConnection;
import com.college.utils.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SystemSettingsDAO {

    public String getSetting(String key) {
        String sql = "SELECT setting_value FROM system_settings WHERE setting_key = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, key);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("setting_value");
            }
        } catch (SQLException e) {
            Logger.error("Failed to get setting: " + key, e);
        }
        return null;
    }

    public void updateSetting(String key, String value) {
        String sql = "INSERT INTO system_settings (setting_key, setting_value) VALUES (?, ?) " +
                "ON CONFLICT(setting_key) DO UPDATE SET setting_value = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, key);
            stmt.setString(2, value);
            stmt.setString(3, value);
            stmt.executeUpdate();
        } catch (SQLException e) {
            Logger.error("Failed to update setting: " + key, e);
        }
    }
}
