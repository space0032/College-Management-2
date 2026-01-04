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
            // Handle null join_date gracefully
            if (emp.getJoinDate() != null) {
                pstmt.setDate(7, Date.valueOf(emp.getJoinDate()));
            } else {
                pstmt.setDate(7, Date.valueOf(java.time.LocalDate.now()));
            }
            pstmt.setBigDecimal(8, emp.getSalary());
            pstmt.setString(9, emp.getStatus().name());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            Logger.error("Database operation failed", e);
            return false;
        }
    }

    public boolean updateEmployee(Employee emp) {
        // If ID is 0, it means this is a "User View" of an employee with no profile
        // yet.
        // We must CREATE (Insert) the profile instead of updating.
        if (emp.getId() == 0) {
            return addEmployee(emp);
        }

        String sql = "UPDATE employees SET first_name = ?, last_name = ?, email = ?, phone = ?, " +
                "designation = ?, join_date = ?, salary = ?, status = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, emp.getFirstName());
            pstmt.setString(2, emp.getLastName());
            pstmt.setString(3, emp.getEmail());
            pstmt.setString(4, emp.getPhone());
            pstmt.setString(5, emp.getDesignation());
            // Handle null join_date gracefully
            if (emp.getJoinDate() != null) {
                pstmt.setDate(6, Date.valueOf(emp.getJoinDate()));
            } else {
                pstmt.setDate(6, Date.valueOf(java.time.LocalDate.now()));
            }
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
        // Query users (excluding students) and join with employees to get
        // salary/details if available
        // Also LEFT JOIN faculty table to get faculty email addresses
        // We link users to employees via username = employee_id OR we just treat users
        // as the source
        // The user wants "Designation" to be the Role Name.
        // We'll prioritize User table for existence.

        String sql = "SELECT u.id as u_id, u.username, r.name as role_name, " +
                "e.id as e_id, e.first_name, e.last_name, e.email, e.phone, e.join_date, e.salary, e.status, " +
                "f.email as faculty_email, f.name as faculty_name " +
                "FROM users u " +
                "JOIN roles r ON u.role_id = r.id " +
                "LEFT JOIN employees e ON u.username = e.employee_id " +
                "LEFT JOIN faculty f ON u.id = f.user_id " +
                "WHERE r.code NOT IN ('STUDENT') " +
                "ORDER BY u.username";

        try (Connection conn = DatabaseConnection.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Employee e = new Employee();
                // Map User info
                String username = rs.getString("username");
                String roleName = rs.getString("role_name");
                String facultyEmail = rs.getString("faculty_email");
                String facultyName = rs.getString("faculty_name");

                // If employee record exists, use it
                int empId = rs.getInt("e_id");
                if (rs.wasNull()) {
                    // No employee record yet - synthetic
                    e.setEmployeeId(username); // "in place of id show username"

                    // Use faculty name if available, otherwise username
                    if (facultyName != null && !facultyName.isEmpty()) {
                        e.setFirstName(facultyName);
                        e.setLastName("");
                    } else {
                        e.setFirstName(username);
                        e.setLastName("");
                    }

                    e.setDesignation(roleName); // "designition according to his role"

                    // Use faculty email if available
                    if (facultyEmail != null) {
                        e.setEmail(facultyEmail);
                    }

                    e.setSalary(java.math.BigDecimal.ZERO);
                    e.setStatus(Employee.Status.ACTIVE);
                    e.setId(0); // Not persisted in employees table yet
                } else {
                    e.setId(empId);
                    e.setEmployeeId(username); // Force username as ID as requested
                    e.setFirstName(rs.getString("first_name"));
                    if (e.getFirstName() == null) {
                        // Fall back to faculty name or username
                        e.setFirstName(facultyName != null ? facultyName : username);
                    }
                    e.setLastName(rs.getString("last_name"));

                    // Use employee email, or fall back to faculty email
                    String empEmail = rs.getString("email");
                    e.setEmail(empEmail != null ? empEmail : facultyEmail);

                    e.setPhone(rs.getString("phone"));
                    e.setDesignation(roleName); // Override designation with Role Name to ensure sync

                    java.sql.Date joinDate = rs.getDate("join_date");
                    if (joinDate != null)
                        e.setJoinDate(joinDate.toLocalDate());

                    e.setSalary(rs.getBigDecimal("salary"));
                    String statusStr = rs.getString("status");
                    if (statusStr != null) {
                        e.setStatus(Employee.Status.valueOf(statusStr));
                    } else {
                        e.setStatus(Employee.Status.ACTIVE);
                    }
                }
                list.add(e);
            }

            // Second query: Get standalone employees who are NOT users (or failed to join)
            // This ensures manually added employees (who aren't users yet) are visible
            // We verify they aren't in the list by checking if their employee_id matches
            // any username we just extracted?
            // A simpler way is to use SQL UNION, but H2/MySQL compatibility varies for
            // complex joins.
            // Let's rely on a second separate simple query for robustness given the "left
            // join" complexity.

            // Get all employees
            try (Statement stmt2 = conn.createStatement();
                    ResultSet rs2 = stmt2.executeQuery("SELECT * FROM employees")) {

                while (rs2.next()) {
                    String empIdStr = rs2.getString("employee_id");
                    // Check if this employee was already added via User match
                    boolean exists = list.stream().anyMatch(
                            emp -> emp.getEmployeeId() != null && emp.getEmployeeId().equalsIgnoreCase(empIdStr));

                    if (!exists) {
                        Employee e = new Employee();
                        e.setId(rs2.getInt("id"));
                        e.setEmployeeId(empIdStr);
                        e.setFirstName(rs2.getString("first_name"));
                        e.setLastName(rs2.getString("last_name"));
                        e.setEmail(rs2.getString("email"));
                        e.setPhone(rs2.getString("phone"));
                        e.setDesignation(rs2.getString("designation"));
                        e.setSalary(rs2.getBigDecimal("salary"));
                        java.sql.Date jd = rs2.getDate("join_date");
                        if (jd != null)
                            e.setJoinDate(jd.toLocalDate());

                        String status = rs2.getString("status");
                        e.setStatus(status != null ? Employee.Status.valueOf(status) : Employee.Status.ACTIVE);

                        list.add(e);
                    }
                }
            }

        } catch (SQLException e) {
            Logger.error("Database operation failed", e);
        }
        return list;
    }
}
