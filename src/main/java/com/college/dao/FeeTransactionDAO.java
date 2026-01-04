package com.college.dao;

import com.college.models.FeeTransaction;
import com.college.utils.DatabaseConnection;
import com.college.utils.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FeeTransactionDAO {

    public boolean recordTransaction(FeeTransaction trans) {
        String sql = "INSERT INTO fee_transactions (transaction_id, student_id, fee_payment_id, amount, type, description, payment_mode, created_by) "
                +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, trans.getTransactionId());
            pstmt.setInt(2, trans.getStudentId());
            if (trans.getFeePaymentId() != null) {
                pstmt.setInt(3, trans.getFeePaymentId());
            } else {
                pstmt.setNull(3, Types.INTEGER);
            }
            pstmt.setBigDecimal(4, trans.getAmount());
            pstmt.setString(5, trans.getType().name());
            pstmt.setString(6, trans.getDescription());
            pstmt.setString(7, trans.getPaymentMode().name());
            pstmt.setInt(8, trans.getCreatedBy());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            Logger.error("Database operation failed", e);
            return false;
        }
    }

    public List<FeeTransaction> getTransactionsByStudent(int studentId) {
        List<FeeTransaction> list = new ArrayList<>();
        String sql = "SELECT * FROM fee_transactions WHERE student_id = ? ORDER BY transaction_date DESC";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, studentId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                FeeTransaction ft = new FeeTransaction();
                ft.setId(rs.getInt("id"));
                ft.setTransactionId(rs.getString("transaction_id"));
                ft.setStudentId(rs.getInt("student_id"));
                ft.setAmount(rs.getBigDecimal("amount"));
                ft.setType(FeeTransaction.Type.valueOf(rs.getString("type")));
                ft.setPaymentMode(FeeTransaction.PaymentMode.valueOf(rs.getString("payment_mode")));
                ft.setTransactionDate(rs.getTimestamp("transaction_date").toLocalDateTime());
                ft.setDescription(rs.getString("description"));
                list.add(ft);
            }
        } catch (SQLException e) {
            Logger.error("Database operation failed", e);
        }
        return list;
    }
}
