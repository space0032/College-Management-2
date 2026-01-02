package com.college.models;

/**
 * Grade Model
 * Represents a student's grade in a course
 */
public class Grade {

    private int id;
    private int studentId;
    private int courseId;
    private String examType; // MID_TERM, END_TERM, ASSIGNMENT, QUIZ
    private double marksObtained;
    private double maxMarks;
    private String grade; // A+, A, B+, B, C, D, F
    private double percentage;

    // For display purposes
    private String studentName;
    private String courseName;

    public Grade() {
    }

    public Grade(int studentId, int courseId, String examType, double marksObtained, double maxMarks) {
        this.studentId = studentId;
        this.courseId = courseId;
        this.examType = examType;
        this.marksObtained = marksObtained;
        this.maxMarks = maxMarks;
        this.percentage = (marksObtained / maxMarks) * 100;
        this.grade = calculateGrade(percentage);
    }

    private String calculateGrade(double percentage) {
        if (percentage >= 90)
            return "A+";
        if (percentage >= 80)
            return "A";
        if (percentage >= 70)
            return "B+";
        if (percentage >= 60)
            return "B";
        if (percentage >= 50)
            return "C";
        if (percentage >= 40)
            return "D";
        return "F";
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getStudentId() {
        return studentId;
    }

    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }

    public int getCourseId() {
        return courseId;
    }

    public void setCourseId(int courseId) {
        this.courseId = courseId;
    }

    public String getExamType() {
        return examType;
    }

    public void setExamType(String examType) {
        this.examType = examType;
    }

    public double getMarksObtained() {
        return marksObtained;
    }

    public void setMarksObtained(double marksObtained) {
        this.marksObtained = marksObtained;
        this.percentage = (marksObtained / maxMarks) * 100;
        this.grade = calculateGrade(percentage);
    }

    public double getMaxMarks() {
        return maxMarks;
    }

    public void setMaxMarks(double maxMarks) {
        this.maxMarks = maxMarks;
        this.percentage = (marksObtained / maxMarks) * 100;
        this.grade = calculateGrade(percentage);
    }

    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }

    public double getPercentage() {
        return percentage;
    }

    public void setPercentage(double percentage) {
        this.percentage = percentage;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    private int semester;

    public int getSemester() {
        return semester;
    }

    public void setSemester(int semester) {
        this.semester = semester;
    }

    @Override
    public String toString() {
        return "Grade{" +
                "id=" + id +
                ", studentId=" + studentId +
                ", courseId=" + courseId +
                ", examType='" + examType + '\'' +
                ", marksObtained=" + marksObtained +
                ", grade='" + grade + '\'' +
                '}';
    }
}
