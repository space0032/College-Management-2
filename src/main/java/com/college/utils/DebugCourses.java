package com.college.utils;

import com.college.dao.CourseDAO;
import com.college.dao.StudentDAO;
import com.college.models.Course;
import com.college.models.Student;
import java.util.List;

public class DebugCourses {
    public static void main(String[] args) {
        System.out.println("=== DEBUGGING COURSE VISIBILITY ===");

        // Check Students
        StudentDAO studentDAO = new StudentDAO();
        List<Student> students = studentDAO.getAllStudents();
        System.out.println("Found " + students.size() + " students.");
        if (!students.isEmpty()) {
            Student s = students.get(0);
            System.out.println(String.format("Student: %s | Dept: '%s' | Sem: %d",
                    s.getName(), s.getDepartment(), s.getSemester()));
        }

        CourseDAO courseDAO = new CourseDAO();
        List<Course> courses = courseDAO.getAllCourses();
        System.out.println("Found " + courses.size() + " courses.");
        for (Course c : courses) {
            System.out.println(String.format("Course: %s | Dept: '%s' | Sem: %d | Type: '%s'",
                    c.getName(), c.getDepartment(), c.getSemester(), c.getCourseType()));
        }

        // Try to find a student to compare
        // Assuming user ID 1 or getting from DB.
        // I'll just check what departments exist.
    }
}
