package com.college.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.college.models.Employee;

class JsonHelperTest {

    @Test
    void serializesEnumsAsJsonStrings() {
        assertEquals("\"ACTIVE\"", JsonHelper.toJson(Employee.Status.ACTIVE));
    }
}
