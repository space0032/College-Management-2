package com.college.dao;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StudentDAOTest {

    @Test
    void missingEnrollmentDateDefaultsToToday() {
        assertEquals(LocalDate.now(), StudentDAO.enrollmentDateOrToday(null).toLocalDate());
    }

    @Test
    void suppliedEnrollmentDateIsPreserved() {
        Date supplied = java.sql.Date.valueOf(LocalDate.of(2025, 7, 15));
        assertEquals(LocalDate.of(2025, 7, 15), StudentDAO.enrollmentDateOrToday(supplied).toLocalDate());
    }
}
