package com.college.dao;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pure-logic unit tests for warden account settings. Database-backed flows
 * are covered by the manual QA matrix (create / update / delete + permissions).
 */
class WardenDAOTest {

    @Test
    void generatedPasswordIsSecureRandom() {
        String p = WardenDAO.generatePassword();
        assertTrue(p.length() >= 8, "generated password must be at least 8 chars");
        assertNotEquals("123", p, "no well-known default password allowed");
        assertNotEquals(WardenDAO.generatePassword(), p, "passwords must be random, not constant");
    }
}