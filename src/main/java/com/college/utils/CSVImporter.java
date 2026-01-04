package com.college.utils;

import com.college.dao.StudentDAO;
import com.college.dao.UserDAO;
import com.college.models.Student;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.sql.Date;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

public class CSVImporter {

    public static class ImportResult {
        public int successCount = 0;
        public int failCount = 0;
        public List<String> errors = new ArrayList<>();

        public String getSummary() {
            StringBuilder sb = new StringBuilder();
            sb.append("Import Complete!\n\n");
            sb.append("✅ Success: ").append(successCount).append(" students\n");
            sb.append("❌ Failed: ").append(failCount).append(" students\n\n");

            if (!errors.isEmpty()) {
                sb.append("Errors:\n");
                for (String error : errors) {
                    sb.append("- ").append(error).append("\n");
                }
            }

            return sb.toString();
        }
    }

    public static ImportResult importStudents(File csvFile) {
        ImportResult result = new ImportResult();
        StudentDAO studentDAO = new StudentDAO();
        UserDAO userDAO = new UserDAO();

        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
            String line;
            int lineNum = 0;

            // Skip header
            br.readLine();
            lineNum++;

            while ((line = br.readLine()) != null) {
                lineNum++;

                try {
                    String[] fields = line.split(",");

                    if (fields.length < 6) {
                        result.errors.add("Line " + lineNum + ": Not enough fields");
                        result.failCount++;
                        continue;
                    }

                    String name = fields[0].trim();
                    String email = fields[1].trim();
                    String phone = fields[2].trim();
                    String department = fields[3].trim();
                    String course = fields[4].trim();
                    int semester = Integer.parseInt(fields[5].trim());
                    String batch = fields.length > 6 ? fields[6].trim() : String.valueOf(LocalDate.now().getYear());
                    String address = fields.length > 7 ? fields[7].trim() : "";

                    // Validate email
                    if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
                        result.errors.add("Line " + lineNum + ": Invalid email - " + email);
                        result.failCount++;
                        continue;
                    }

                    // Validate phone
                    if (!phone.matches("^[0-9]{10}$")) {
                        result.errors.add("Line " + lineNum + ": Invalid phone - " + phone);
                        result.failCount++;
                        continue;
                    }

                    // Generate enrollment number
                    String enrollmentNumber = EnrollmentGenerator.generateStudentEnrollment(department);

                    // Create user account
                    int userId = userDAO.addUser(enrollmentNumber, "123", "STUDENT");

                    if (userId == -1) {
                        result.errors.add("Line " + lineNum + ": Failed to create user account");
                        result.failCount++;
                        continue;
                    }

                    // Create student
                    Student student = new Student();
                    student.setName(name);
                    student.setEmail(email);
                    student.setPhone(phone);
                    student.setDepartment(department);
                    student.setCourse(course);
                    student.setSemester(semester);
                    student.setBatch(batch);
                    student.setAddress(address);
                    student.setUserId(userId);
                    student.setUsername(enrollmentNumber);
                    student.setEnrollmentDate(
                            Date.from(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant()));

                    if (studentDAO.addStudent(student, userId) > 0) {
                        result.successCount++;
                    } else {
                        result.errors.add("Line " + lineNum + ": Failed to add student - " + name);
                        result.failCount++;
                    }

                } catch (Exception e) {
                    result.errors.add("Line " + lineNum + ": " + e.getMessage());
                    result.failCount++;
                }
            }

        } catch (Exception e) {
            result.errors.add("File error: " + e.getMessage());
        }

        return result;
    }
}
