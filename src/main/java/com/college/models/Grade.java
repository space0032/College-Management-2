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
    private String enrollmentNumber;
    private String courseName;
    private String department;
    private int credits; // Added for weighted calculation

    public Grade() {
    }

    public Grade(int studentId, int courseId, String examType, double marksObtained, double maxMarks) {
        this.studentId = studentId;
        this.courseId = courseId;
        this.examType = examType;
        this.maxMarks = maxMarks;
        this.marksObtained = marksObtained;
        this.percentage = computePercentage(marksObtained, maxMarks);
        this.grade = calculateGrade(percentage);
    }

    /**
     * Percentage is based on maxMarks when a real maximum is provided (e.g. a
     * 50-mark exam stores raw marks). When maxMarks is absent (0 / null row,
     * as the web app stores marks directly on a 0-100 scale) the stored marks
     * are treated as the percentage so we never divide by zero (which used to
     * serialize as Infinity/NaN and produce invalid JSON).
     */
    private double computePercentage(double marks, double max) {
        if (max > 0) {
            return (marks / max) * 100.0;
        }
        return marks;
    }

    private String calculateGrade(double percentage) {
        if (percentage >= 90)
            return "A";
        if (percentage >= 75)
            return "B";
        if (percentage >= 60)
            return "C";
        if (percentage >= 50)
            return "D";
        if (percentage >= 40)
            return "E";
        return "F";
    }

    public double getGradePoints() {
        if (grade == null)
            return 0.0;
        switch (grade) {
            case "A":
                return 10.0;
            case "B":
                return 8.0;
            case "C":
                return 7.0;
            case "D":
                return 6.0;
            case "E":
                return 5.0;
            default:
                return 0.0;
        }
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
        this.percentage = computePercentage(marksObtained, this.maxMarks);
        this.grade = calculateGrade(percentage);
    }

    // Alias for getMarksObtained, used by TranscriptService
    public double getMarks() {
        return marksObtained;
    }

    public double getMaxMarks() {
        return maxMarks;
    }

    public void setMaxMarks(double maxMarks) {
        this.maxMarks = maxMarks;
        this.percentage = computePercentage(this.marksObtained, maxMarks);
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

    public String getEnrollmentNumber() {
        return enrollmentNumber;
    }

    public void setEnrollmentNumber(String enrollmentNumber) {
        this.enrollmentNumber = enrollmentNumber;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    private int semester;

    public int getSemester() {
        return semester;
    }

    public void setSemester(int semester) {
        this.semester = semester;
    }

    public int getCredits() {
        return credits;
    }

    public void setCredits(int credits) {
        this.credits = credits;
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
