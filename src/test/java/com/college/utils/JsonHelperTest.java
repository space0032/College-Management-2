package com.college.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.college.models.Employee;
import com.college.models.Room;

class JsonHelperTest {

    @Test
    void serializesEnumsAsJsonStrings() {
        assertEquals("\"ACTIVE\"", JsonHelper.toJson(Employee.Status.ACTIVE));
    }

    @Test
    void deserializesQuotedNumericFormValues() {
        Room room = JsonHelper.fromJson("{\"hostelId\":\"3\",\"floor\":\"1\",\"capacity\":\"2\"}", Room.class);

        assertEquals(3, room.getHostelId());
        assertEquals(1, room.getFloor());
        assertEquals(2, room.getCapacity());
    }
}
