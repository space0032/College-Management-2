package com.college.dao;

import com.college.models.Submission;
import com.college.utils.DatabaseConnection;
import com.college.utils.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SubmissionDAO {

    /**
     * Submit an assignment
     */
    public boolean submitAssignment(Submission submission) {
        String sql = "INSERT INTO submissions (assignment_id, student_id, submission_text, file_path, status, plagiarism_score) "
                + "VALUES (?, ?, ?, ?, ?, ?) "
                + "ON DUPLICATE KEY UPDATE submission_text=VALUES(submission_text), file_path=VALUES(file_path), "
                + "status='SUBMITTED', submitted_at=CURRENT_TIMESTAMP, plagiarism_score=VALUES(plagiarism_score)";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            int plagiarismScore = calculatePlagiarismScore(submission, conn);

            pstmt.setInt(1, submission.getAssignmentId());
            pstmt.setInt(2, submission.getStudentId());
            pstmt.setString(3, submission.getSubmissionText());
            pstmt.setString(4, submission.getFilePath());
            pstmt.setString(5, "SUBMITTED"); // Default status
            pstmt.setInt(6, plagiarismScore);

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            Logger.error("Database operation failed", e);
            return false;
        }
    }

    /**
     * Get submission by student and assignment
     */
    public Submission getSubmission(int assignmentId, int studentId) {
        String sql = "SELECT s.*, st.name as student_name, a.title as assignment_title " +
                "FROM submissions s " +
                "JOIN students st ON s.student_id = st.id " +
                "JOIN assignments a ON s.assignment_id = a.id " +
                "WHERE s.assignment_id = ? AND s.student_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, assignmentId);
            pstmt.setInt(2, studentId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return extractSubmissionFromResultSet(rs);
            }

        } catch (SQLException e) {
            Logger.error("Database operation failed", e);
        }

        return null;
    }

    /**
     * Get all submissions for an assignment
     */
    public List<Submission> getSubmissionsByAssignment(int assignmentId) {
        List<Submission> submissions = new ArrayList<>();
        String sql = "SELECT s.*, st.name as student_name, u.username as student_enrollment_id " +
                "FROM submissions s " +
                "JOIN students st ON s.student_id = st.id " +
                "LEFT JOIN users u ON st.user_id = u.id " +
                "WHERE s.assignment_id = ? ORDER BY s.submitted_at DESC";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, assignmentId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                submissions.add(extractSubmissionFromResultSet(rs));
            }

        } catch (SQLException e) {
            Logger.error("Database operation failed", e);
        }

        return submissions;
    }

    /**
     * Grade submission
     */
    public boolean gradeSubmission(int submissionId, double grade, String feedback) {
        String sql = "UPDATE submissions SET marks_obtained = ?, feedback = ?, status = 'GRADED' WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setDouble(1, grade);
            pstmt.setString(2, feedback);
            pstmt.setInt(3, submissionId);

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            Logger.error("Database operation failed", e);
            return false;
        }
    }

    /**
     * Basic Plagiarism Check
     * Compares the new submission text with all other submissions for the same
     * assignment.
     * Calculates Jaccard similarity coefficient for overlapping words.
     */
    private int calculatePlagiarismScore(Submission newSubmission, Connection conn) {
        if (newSubmission.getSubmissionText() == null || newSubmission.getSubmissionText().isEmpty()) {
            return 0;
        }

        List<String> otherTexts = new ArrayList<>();
        String sql = "SELECT submission_text FROM submissions WHERE assignment_id = ? AND student_id != ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, newSubmission.getAssignmentId());
            pstmt.setInt(2, newSubmission.getStudentId());
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                String text = rs.getString("submission_text");
                if (text != null && !text.isEmpty()) {
                    otherTexts.add(text);
                }
            }

        } catch (SQLException e) {
            Logger.error("Database operation failed", e);
        }

        int maxSimilarity = 0;
        for (String otherText : otherTexts) {
            int similarity = calculateSimilarity(newSubmission.getSubmissionText(), otherText);
            if (similarity > maxSimilarity) {
                maxSimilarity = similarity;
            }
        }

        return maxSimilarity;
    }

    /**
     * Calculate similarity percentage between two texts using Jaccard Similarity of
     * 3-grams (or words)
     * For simplicity, we'll use word overlap.
     */
    private int calculateSimilarity(String text1, String text2) {
        String[] words1 = text1.toLowerCase().replaceAll("[^a-zA-Z0-9 ]", "").split("\\s+");
        String[] words2 = text2.toLowerCase().replaceAll("[^a-zA-Z0-9 ]", "").split("\\s+");

        java.util.Set<String> set1 = new java.util.HashSet<>(java.util.Arrays.asList(words1));
        java.util.Set<String> set2 = new java.util.HashSet<>(java.util.Arrays.asList(words2));

        java.util.Set<String> intersection = new java.util.HashSet<>(set1);
        intersection.retainAll(set2);

        java.util.Set<String> union = new java.util.HashSet<>(set1);
        union.addAll(set2);

        if (union.isEmpty())
            return 0;

        double jaccardIndex = (double) intersection.size() / union.size();
        return (int) (jaccardIndex * 100);
    }

    private Submission extractSubmissionFromResultSet(ResultSet rs) throws SQLException {
        Submission submission = new Submission();
        submission.setId(rs.getInt("id"));
        submission.setAssignmentId(rs.getInt("assignment_id"));
        submission.setStudentId(rs.getInt("student_id"));
        submission.setSubmissionText(rs.getString("submission_text"));
        submission.setFilePath(rs.getString("file_path"));
        submission.setSubmittedAt(rs.getTimestamp("submitted_at"));
        submission.setStatus(rs.getString("status"));

        double grade = rs.getDouble("marks_obtained");
        if (!rs.wasNull())
            submission.setGrade(grade);

        submission.setFeedback(rs.getString("feedback"));
        submission.setPlagiarismScore(rs.getInt("plagiarism_score"));

        try {
            submission.setStudentName(rs.getString("student_name"));
        } catch (SQLException e) {
        }
        try {
            submission.setStudentEnrollmentId(rs.getString("student_enrollment_id"));
        } catch (SQLException e) {
        }
        try {
            submission.setAssignmentTitle(rs.getString("assignment_title"));
        } catch (SQLException e) {
        }

        return submission;
    }
}
