package com.college.dao;

import com.college.models.Employee;
import com.college.utils.DatabaseConnection;
import com.college.utils.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EmployeeDAO {

    public boolean addEmployee(Employee emp) {
        String sql = "INSERT INTO employees (employee_id, first_name, last_name, email, phone, designation, join_date, salary, status) "
                +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, emp.getEmployeeId());
            pstmt.setString(2, emp.getFirstName());
            pstmt.setString(3, emp.getLastName());
            pstmt.setString(4, emp.getEmail());
            pstmt.setString(5, emp.getPhone());
            pstmt.setString(6, emp.getDesignation());
            pstmt.setDate(7, Date.valueOf(emp.getJoinDate()));
            pstmt.setBigDecimal(8, emp.getSalary());
            pstmt.setString(9, emp.getStatus().name());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            Logger.error("Database operation failed", e);
            return false;
        }
    }

    public boolean updateEmployee(Employee emp) {
        String sql = "UPDATE employees SET first_name = ?, last_name = ?, email = ?, phone = ?, " +
                "designation = ?, join_date = ?, salary = ?, status = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, emp.getFirstName());
            pstmt.setString(2, emp.getLastName());
            pstmt.setString(3, emp.getEmail());
            pstmt.setString(4, emp.getPhone());
            pstmt.setString(5, emp.getDesignation());
            pstmt.setDate(6, Date.valueOf(emp.getJoinDate()));
            pstmt.setBigDecimal(7, emp.getSalary());
            pstmt.setString(8, emp.getStatus().name());
            pstmt.setInt(9, emp.getId());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            Logger.error("Failed to update employee", e);
            return false;
        }
    }

    public List<Employee> getAllEmployees() {
        List<Employee> list = new ArrayList<>();
        String sql = "SELECT * FROM employees ORDER BY last_name";
        try (Connection conn = DatabaseConnection.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Employee e = new Employee();
                e.setId(rs.getInt("id"));
                e.setEmployeeId(rs.getString("employee_id"));
                e.setFirstName(rs.getString("first_name"));
                e.setLastName(rs.getString("last_name"));
                e.setEmail(rs.getString("email"));
                e.setPhone(rs.getString("phone"));
                e.setDesignation(rs.getString("designation"));
                java.sql.Date joinDate = rs.getDate("join_date");
                if (joinDate != null)
                    e.setJoinDate(joinDate.toLocalDate());
                e.setSalary(rs.getBigDecimal("salary"));
                e.setStatus(Employee.Status.valueOf(rs.getString("status")));
                list.add(e);
            }
        } catch (SQLException e) {
            Logger.error("Database operation failed", e);
        }
        return list;
    }
}
