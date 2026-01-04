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

        // Calculate raw points (Assuming simplistic 10-point scale based on marks for
        // now)
        // In a real system, this would map Letter Grades to Points (A=10, B=9, etc.)
        for (Grade g : allGrades) {
            int sem = g.getSemester();
            int credits = 3; // Default credits if not in Grade model, ideally should be in Course model
            double points = convertMarksToPoints(g.getMarks());

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

    private double convertMarksToPoints(double marks) {
        if (marks >= 90)
            return 10.0;
        if (marks >= 80)
            return 9.0;
        if (marks >= 70)
            return 8.0;
        if (marks >= 60)
            return 7.0;
        if (marks >= 50)
            return 6.0;
        if (marks >= 40)
            return 5.0;
        return 0.0; // Fail
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
