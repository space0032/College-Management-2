package com.college.utils;

import com.college.dao.StudentDAO;
import com.college.dao.FacultyDAO;
import com.college.dao.WardenDAO;
import com.college.models.Student;
import com.college.models.Faculty;
import com.college.models.Warden;

/**
 * Utility class to get display names for users based on their role and user ID
 */
public class UserDisplayNameUtil {

    /**
     * Get the display name for a user based on their role and userId
     * 
     * @param userId The user's ID
     * @param role The user's role (STUDENT, FACULTY, ADMIN, WARDEN, etc.)
     * @param username The username (used as fallback if name not found)
     * @return The display name (actual name from student/faculty/warden tables, or username as fallback)
     */
    public static String getDisplayName(int userId, String role, String username) {
        try {
            switch (role.toUpperCase()) {
                case "STUDENT":
                    StudentDAO studentDAO = new StudentDAO();
                    Student student = studentDAO.getStudentByUserId(userId);
                    if (student != null && student.getName() != null) {
                        return student.getName();
                    }
                    break;

                case "FACULTY":
                    FacultyDAO facultyDAO = new FacultyDAO();
                    Faculty faculty = facultyDAO.getFacultyByUserId(userId);
                    if (faculty != null && faculty.getName() != null) {
                        return faculty.getName();
                    }
                    break;

                case "WARDEN":
                    WardenDAO wardenDAO = new WardenDAO();
                    Warden warden = wardenDAO.getWardenByUserId(userId);
                    if (warden != null && warden.getName() != null) {
                        return warden.getName();
                    }
                    break;

                case "ADMIN":
                    // For admin users, try to find if they are also in faculty or warden tables
                    FacultyDAO adminFacultyDAO = new FacultyDAO();
                    Faculty adminFaculty = adminFacultyDAO.getFacultyByUserId(userId);
                    if (adminFaculty != null && adminFaculty.getName() != null) {
                        return adminFaculty.getName();
                    }
                    
                    WardenDAO adminWardenDAO = new WardenDAO();
                    Warden adminWarden = adminWardenDAO.getWardenByUserId(userId);
                    if (adminWarden != null && adminWarden.getName() != null) {
                        return adminWarden.getName();
                    }
                    break;

                default:
                    // For any other role, just return the username
                    break;
            }
        } catch (Exception e) {
            // If any error occurs, fallback to username
            e.printStackTrace();
        }

        // Fallback to username if name not found
        return username;
    }
}
