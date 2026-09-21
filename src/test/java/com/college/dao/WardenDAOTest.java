package com.college.dao;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Pure-logic unit tests for warden account settings. Database-backed flows
 * are covered by the manual QA matrix (create / update / delete + permissions).
 */
class WardenDAOTest {

    @Test
    void defaultPasswordIsTestingValue() {
        assertEquals("123", WardenDAO.DEFAULT_PASSWORD);
    }
}