package com.college.dao;

import com.college.models.FeeCategory;
import com.college.models.StudentFee;
import com.college.models.FeePayment;
import com.college.utils.DatabaseConnection;
import com.college.utils.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO for Enhanced Fee Management
 */
public class EnhancedFeeDAO {

    /**
     * Get all fee categories
     */
    public List<FeeCategory> getAllCategories() {
        List<FeeCategory> categories = new ArrayList<>();
        String sql = "SELECT * FROM fee_categories WHERE is_active = TRUE ORDER BY category_name";

        try (Connection conn = DatabaseConnection.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                FeeCategory category = new FeeCategory();
                category.setId(rs.getInt("id"));
                category.setCategoryName(rs.getString("category_name"));
                category.setBaseAmount(rs.getDouble("base_amount"));
                category.setDescription(rs.getString("description"));
                category.setActive(rs.getBoolean("is_active"));
                categories.add(category);
            }

        } catch (SQLException e) {
            Logger.error("Database operation failed", e);
        }

        return categories;
    }

    /**
     * Assign fee to student
     */
    public boolean assignFeeToStudent(StudentFee studentFee) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            return assignFeeToStudent(conn, studentFee);
        } catch (SQLException e) {
            Logger.error("Database operation failed", e);
            return false;
        }
    }

    public boolean assignFeeToStudent(Connection conn, StudentFee studentFee) {
        String sql = "INSERT INTO student_fees (student_id, category_id, academic_year, total_amount, due_date) " +
                "VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, studentFee.getStudentId());
            pstmt.setInt(2, studentFee.getCategoryId());
            pstmt.setString(3, studentFee.getAcademicYear());
            pstmt.setDouble(4, studentFee.getTotalAmount());
            if (studentFee.getDueDate() != null) {
                pstmt.setDate(5, new java.sql.Date(studentFee.getDueDate().getTime()));
            } else {
                pstmt.setNull(5, Types.DATE);
            }

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            Logger.error("Database operation failed", e);
            return false;
        }
    }

    /**
     * Add student fee (Convenience method)
     */
    public boolean addStudentFee(int studentId, int categoryId, double amount, Date dueDate) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            return addStudentFee(conn, studentId, categoryId, amount, dueDate);
        } catch (SQLException e) {
            Logger.error("Database operation failed", e);
            return false;
        }
    }

    public boolean addStudentFee(Connection conn, int studentId, int categoryId, double amount, Date dueDate) {
        StudentFee fee = new StudentFee();
        fee.setStudentId(studentId);
        fee.setCategoryId(categoryId);
        fee.setTotalAmount(amount);
        fee.setDueDate(dueDate);
        fee.setAcademicYear(java.time.Year.now().toString()); // Default to current year
        return assignFeeToStudent(conn, fee);
    }

    /**
     * Record payment (Convenience method with validation)
     */
    public boolean recordPayment(int studentFeeId, double amount, Date paymentDate) {
        // Validate amount is positive
        if (amount <= 0) {
            Logger.error("Invalid payment amount: " + amount);
            return false;
        }

        FeePayment payment = new FeePayment();
        payment.setStudentFeeId(studentFeeId);
        payment.setAmount(amount);
        payment.setPaymentDate(paymentDate);
        payment.setPaymentMode("CASH"); // Default
        return recordPayment(payment);
    }

    /**
     * Record payment with validation
     */
    public boolean recordPayment(FeePayment payment) {
        // Validate payment amount
        if (payment.getAmount() <= 0) {
            Logger.error("Invalid payment amount: " + payment.getAmount());
            return false;
        }

        // Check if payment would exceed total fee
        String checkSql = "SELECT total_amount, paid_amount FROM student_fees WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {

            checkStmt.setInt(1, payment.getStudentFeeId());
            ResultSet rs = checkStmt.executeQuery();

            if (rs.next()) {
                double totalAmount = rs.getDouble("total_amount");
                double paidAmount = rs.getDouble("paid_amount");
                double remaining = totalAmount - paidAmount;

                if (payment.getAmount() > remaining) {
                    Logger.warn(String.format("Payment amount %.2f exceeds remaining fee %.2f",
                            payment.getAmount(), remaining));
                    // Optionally allow overpayment or reject it
                    // For now, we'll cap it at remaining amount
                    payment.setAmount(remaining);
                }
            }
        } catch (SQLException e) {
            Logger.error("Failed to validate payment", e);
            return false;
        }

        // Generate receipt number
        String receiptNumber = generateReceiptNumber();
        payment.setReceiptNumber(receiptNumber);

        String sql = "INSERT INTO fee_payments (student_fee_id, payment_date, amount, payment_mode, " +
                "transaction_id, receipt_number, received_by, remarks) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, payment.getStudentFeeId());
            pstmt.setDate(2, new java.sql.Date(payment.getPaymentDate().getTime()));
            pstmt.setDouble(3, payment.getAmount());
            pstmt.setString(4, payment.getPaymentMode());
            pstmt.setString(5, payment.getTransactionId());
            pstmt.setString(6, receiptNumber);
            if (payment.getReceivedBy() != null) {
                pstmt.setInt(7, payment.getReceivedBy());
            } else {
                pstmt.setNull(7, Types.INTEGER);
            }
            pstmt.setString(8, payment.getRemarks());

            if (pstmt.executeUpdate() > 0) {
                // Update student_fees paid amount and status
                updateStudentFeeStatus(payment.getStudentFeeId());
                return true;
            }

        } catch (SQLException e) {
            Logger.error("Database operation failed", e);
        }

        return false;
    }

    /**
     * Get student fees for a student
     */
    public List<StudentFee> getStudentFees(int studentId) {
        List<StudentFee> fees = new ArrayList<>();
        String sql = "SELECT sf.*, s.name as student_name, u.username as student_username, fc.category_name " +
                "FROM student_fees sf " +
                "JOIN students s ON sf.student_id = s.id " +
                "LEFT JOIN users u ON s.user_id = u.id " +
                "JOIN fee_categories fc ON sf.category_id = fc.id " +
                "WHERE sf.student_id = ? ORDER BY sf.academic_year DESC, fc.category_name";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, studentId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                fees.add(extractStudentFeeFromResultSet(rs));
            }

        } catch (SQLException e) {
            Logger.error("Database operation failed", e);
        }

        return fees;
    }

    /**
     * Get all pending fees
     */
    public List<StudentFee> getPendingFees() {
        List<StudentFee> fees = new ArrayList<>();
        String sql = "SELECT sf.*, s.name as student_name, u.username as student_username, fc.category_name " +
                "FROM student_fees sf " +
                "JOIN students s ON sf.student_id = s.id " +
                "LEFT JOIN users u ON s.user_id = u.id " +
                "JOIN fee_categories fc ON sf.category_id = fc.id " +
                "WHERE sf.status != 'PAID' ORDER BY sf.due_date";

        try (Connection conn = DatabaseConnection.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                fees.add(extractStudentFeeFromResultSet(rs));
            }

        } catch (SQLException e) {
            Logger.error("Database operation failed", e);
        }

        return fees;
    }

    /**
     * Get ALL fees (Paid + Pending)
     */
    public List<StudentFee> getAllFees() {
        List<StudentFee> fees = new ArrayList<>();
        String sql = "SELECT sf.*, s.name as student_name, u.username as student_username, fc.category_name " +
                "FROM student_fees sf " +
                "JOIN students s ON sf.student_id = s.id " +
                "LEFT JOIN users u ON s.user_id = u.id " +
                "JOIN fee_categories fc ON sf.category_id = fc.id " +
                "ORDER BY sf.due_date DESC"; // Ordered by date descending

        try (Connection conn = DatabaseConnection.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                fees.add(extractStudentFeeFromResultSet(rs));
            }

        } catch (SQLException e) {
            Logger.error("Database operation failed", e);
        }

        return fees;
    }

    /**
     * Get payment history for a student fee
     */
    public List<FeePayment> getPaymentHistory(int studentFeeId) {
        List<FeePayment> payments = new ArrayList<>();
        String sql = "SELECT * FROM fee_payments WHERE student_fee_id = ? ORDER BY payment_date DESC";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, studentFeeId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                FeePayment payment = new FeePayment();
                payment.setId(rs.getInt("id"));
                payment.setStudentFeeId(rs.getInt("student_fee_id"));
                payment.setPaymentDate(rs.getDate("payment_date"));
                payment.setAmount(rs.getDouble("amount"));
                payment.setPaymentMode(rs.getString("payment_mode"));
                payment.setTransactionId(rs.getString("transaction_id"));
                payment.setReceiptNumber(rs.getString("receipt_number"));

                int receivedBy = rs.getInt("received_by");
                payment.setReceivedBy(rs.wasNull() ? null : receivedBy);

                payment.setRemarks(rs.getString("remarks"));
                payments.add(payment);
            }

        } catch (SQLException e) {
            Logger.error("Database operation failed", e);
        }

        return payments;
    }

    /**
     * Update student fee status based on payments
     */
    private void updateStudentFeeStatus(int studentFeeId) {
        String sql = "UPDATE student_fees SET " +
                "paid_amount = (SELECT COALESCE(SUM(amount), 0) FROM fee_payments WHERE student_fee_id = ?), " +
                "status = CASE " +
                "    WHEN (SELECT COALESCE(SUM(amount), 0) FROM fee_payments WHERE student_fee_id = ?) >= total_amount THEN 'PAID' "
                +
                "    WHEN (SELECT COALESCE(SUM(amount), 0) FROM fee_payments WHERE student_fee_id = ?) > 0 THEN 'PARTIAL' "
                +
                "    ELSE 'PENDING' " +
                "END " +
                "WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, studentFeeId);
            pstmt.setInt(2, studentFeeId);
            pstmt.setInt(3, studentFeeId);
            pstmt.setInt(4, studentFeeId);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            Logger.error("Database operation failed", e);
        }
    }

    /**
     * Generate receipt number
     */
    private String generateReceiptNumber() {
        String sql = "SELECT MAX(CAST(SUBSTRING(receipt_number, 5) AS INTEGER)) as max_num " +
                "FROM fee_payments WHERE receipt_number LIKE 'RCP%'";

        try (Connection conn = DatabaseConnection.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                int maxNum = rs.getInt("max_num");
                return String.format("RCP%06d", maxNum + 1);
            }

        } catch (SQLException e) {
            Logger.error("Database operation failed", e);
        }

        return "RCP000001";
    }

    /**
     * Extract StudentFee from ResultSet
     */
    private StudentFee extractStudentFeeFromResultSet(ResultSet rs) throws SQLException {
        StudentFee fee = new StudentFee();
        fee.setId(rs.getInt("id"));
        fee.setStudentId(rs.getInt("student_id"));
        fee.setCategoryId(rs.getInt("category_id"));
        fee.setAcademicYear(rs.getString("academic_year"));
        fee.setTotalAmount(rs.getDouble("total_amount"));
        fee.setPaidAmount(rs.getDouble("paid_amount"));
        fee.setStatus(rs.getString("status"));
        fee.setDueDate(rs.getDate("due_date"));
        fee.setStudentName(rs.getString("student_name"));
        fee.setCategoryName(rs.getString("category_name"));

        try {
            fee.setStudentUsername(rs.getString("student_username"));
        } catch (SQLException e) {
            // Field might not exist in all queries
        }
        return fee;
    }

    /**
     * Get ALL payment history with search
     */
    public List<FeePayment> searchPaymentHistory(String keyword) {
        List<FeePayment> payments = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT fp.*, s.name as student_name, u.username as enrollment_id, fc.category_name, sf.academic_year "
                        +
                        "FROM fee_payments fp " +
                        "JOIN student_fees sf ON fp.student_fee_id = sf.id " +
                        "JOIN students s ON sf.student_id = s.id " +
                        "LEFT JOIN users u ON s.user_id = u.id " +
                        "JOIN fee_categories fc ON sf.category_id = fc.id ");

        if (keyword != null && !keyword.isEmpty()) {
            sql.append("WHERE s.name ILIKE ? OR fp.receipt_number ILIKE ? OR u.username ILIKE ? ");
        }

        sql.append("ORDER BY fp.payment_date DESC");

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {

            if (keyword != null && !keyword.isEmpty()) {
                String searchPattern = "%" + keyword + "%";
                pstmt.setString(1, searchPattern);
                pstmt.setString(2, searchPattern);
                pstmt.setString(3, searchPattern);
            }

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                FeePayment payment = new FeePayment();
                payment.setId(rs.getInt("id"));
                payment.setStudentFeeId(rs.getInt("student_fee_id"));
                payment.setPaymentDate(rs.getDate("payment_date"));
                payment.setAmount(rs.getDouble("amount"));
                payment.setPaymentMode(rs.getString("payment_mode"));
                payment.setTransactionId(rs.getString("transaction_id"));
                payment.setReceiptNumber(rs.getString("receipt_number"));
                payment.setRemarks(rs.getString("remarks"));

                // Set display fields
                payment.setStudentName(rs.getString("student_name"));
                payment.setStudentEnrollmentId(rs.getString("enrollment_id"));
                payment.setCategoryName(rs.getString("category_name"));
                payment.setAcademicYear(rs.getString("academic_year"));

                payments.add(payment);
            }

        } catch (SQLException e) {
            Logger.error("Database operation failed", e);
        }
        return payments;
    }

    /**
     * Get total amount collected today
     */
    public double getTodaysCollectionAmount() {
        String sql = "SELECT SUM(amount) as total FROM fee_payments WHERE payment_date = CURRENT_DATE";
        try (Connection conn = DatabaseConnection.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getDouble("total");
            }
        } catch (SQLException e) {
            Logger.error("Database operation failed", e);
        }
        return 0.0;
    }

    /**
     * Get total pending amount (across all students)
     */
    public double getTotalPendingAmount() {
        // Calculate: Sum of (total_amount - paid_amount) for all fees where status !=
        // PAID
        String sql = "SELECT SUM(total_amount - paid_amount) as pending FROM student_fees WHERE status != 'PAID'";
        try (Connection conn = DatabaseConnection.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getDouble("pending");
            }
        } catch (SQLException e) {
            Logger.error("Database operation failed", e);
        }
        return 0.0;
    }
}
