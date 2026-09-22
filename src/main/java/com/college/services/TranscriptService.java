package com.college.services;

import com.college.dao.GradeDAO;
import com.college.models.Grade;
import com.college.models.Student;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service to generate academic transcripts
 */
public class TranscriptService {

    private GradeDAO gradeDAO;

    public TranscriptService() {
        this.gradeDAO = new GradeDAO();
    }

    /**
     * Get transcript summary for a student
     * Calculates SGPA for each semester and overall CGPA
     */
    public TranscriptSummary generateTranscript(Student student) {
        List<Grade> allGrades = gradeDAO.getGradesByStudent(student.getId());

        Map<Integer, Double> semesterGradePoints = new HashMap<>();
        Map<Integer, Integer> semesterCredits = new HashMap<>();

        // Single shared calculator: percentage relative to max marks, weighted
        // by credits, terminal exams only (FINAL / END TERM) so this matches the
        // web CGPA endpoint. Web-entered "END TERM" grades now count.
        for (Grade g : allGrades) {
            if (!Grade.isTerminalExam(g.getExamType())) {
                continue;
            }

            int sem = g.getSemester();
            int credits = Math.max(g.getCredits(), 1); // Default to 1 if missing to avoid zero division
            double max = g.getMaxMarks() > 0 ? g.getMaxMarks() : 100;
            double percentage = (g.getMarksObtained() / max) * 100;
            double points = (percentage / 100) * 10;

            semesterGradePoints.put(sem, semesterGradePoints.getOrDefault(sem, 0.0) + (points * credits));
            semesterCredits.put(sem, semesterCredits.getOrDefault(sem, 0) + credits);
        }

        Map<Integer, Double> sgpaMap = new HashMap<>();
        double totalWeightedPoints = 0;
        int totalCredits = 0;

        for (int sem : semesterGradePoints.keySet()) {
            double pts = semesterGradePoints.get(sem);
            int crd = semesterCredits.get(sem);
            if (crd > 0) {
                sgpaMap.put(sem, pts / crd);
                totalWeightedPoints += pts;
                totalCredits += crd;
            }
        }

        double cgpa = totalCredits > 0 ? totalWeightedPoints / totalCredits : 0.0;

        return new TranscriptSummary(allGrades, sgpaMap, cgpa);
    }

    public static class TranscriptSummary {
        private List<Grade> grades;
        private Map<Integer, Double> semesterSgpa;
        private double cgpa;

        public TranscriptSummary(List<Grade> grades, Map<Integer, Double> semesterSgpa, double cgpa) {
            this.grades = grades;
            this.semesterSgpa = semesterSgpa;
            this.cgpa = cgpa;
        }

        public List<Grade> getGrades() {
            return grades;
        }

        public Map<Integer, Double> getSemesterSgpa() {
            return semesterSgpa;
        }

        public double getCgpa() {
            return cgpa;
        }
    }
}
